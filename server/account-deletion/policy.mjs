export const GRACE_MS = 60 * 24 * 60 * 60 * 1000;
export const SECURITY_RETENTION_MS = 24 * 60 * 60 * 1000;
export function validRequest(request) {
  return request && Number.isFinite(request.requestedAt) && request.requestedAt > 0 &&
    request.graceDays === 60 && typeof request.requestId === 'string' && request.requestId.length === 36;
}
export function deadline(request) { return request.requestedAt + GRACE_MS; }
export function returnedDuringRecovery(request, account) {
  const login = account?.lastSignInTimeMillis ?? Date.parse(account?.metadata?.lastSignInTime || '');
  return validRequest(request) && Number.isFinite(login) && login > request.requestedAt && login < deadline(request);
}
export function eligible(request, now) {
  return validRequest(request) && now >= deadline(request) && ['PENDING', 'PROCESSING', 'DATA_PURGED'].includes(request.status);
}
/** Privileged operations are injected so tests exercise the same ordering as the worker. */
export async function processRequest(uid, adapter, now) {
  const request = await adapter.read(uid);
  if (!validRequest(request)) return 'skipped';
  if (request.status === 'CANCELLED') {
    if (Number.isFinite(request.cancelledAt) && request.cancelledAt + SECURITY_RETENTION_MS <= now)
      await adapter.remove(uid, request.requestId);
    return 'skipped';
  }
  if (request.status === 'COMPLETED') {
    if (request.completedAt + SECURITY_RETENTION_MS <= now) await adapter.remove(uid, request.requestId);
    return 'skipped';
  }
  const account = await adapter.account(uid);
  if (request.status === 'PENDING' && returnedDuringRecovery(request, account)) {
    await adapter.cancel(uid, request.requestId); return 'cancelled';
  }
  if (!eligible(request, now)) return 'skipped';
  const locked = await adapter.claim(uid, request, account, now);
  if (!locked) return 'skipped';
  if (account) await adapter.disable(uid);
  const freshAccount = account ? await adapter.account(uid) : null;
  // A login racing the claim is checked again after new sign-ins are blocked.
  if (locked.status !== 'DATA_PURGED' && returnedDuringRecovery(locked, freshAccount)) {
    if (freshAccount && !locked.wasDisabled) await adapter.enable(uid);
    await adapter.cancel(uid, locked.requestId); return 'cancelled';
  }
  if (locked.status !== 'DATA_PURGED') {
    await adapter.purge(uid, locked.accountEmail || account?.email || '');
    await adapter.markPurged(uid, locked.requestId);
  }
  if (freshAccount) await adapter.deleteIdentity(uid);
  await adapter.complete(uid, locked.requestId, now);
  return 'deleted';
}
