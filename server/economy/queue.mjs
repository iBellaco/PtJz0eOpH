import {FieldValue} from 'firebase-admin/firestore';
import {randomUUID} from 'node:crypto';
import {executeEconomy} from './service.mjs';
import {ensure, EconomyError} from './policy.mjs';

/** One private outstanding command per actor; balances and receipts remain server-only. */
export async function processQueue(db, auth, {clock = Date.now, limit = 30, leaseMillis = 120000, manualReview = false} = {}) {
  const stats = {completed: 0, failed: 0, review: 0, skipped: 0};
  const requests = db.collection('economy_requests'), startedAt = clock();
  // Separate pending commands from expired leases so one broken command cannot block new users.
  let pending, recovery, review = {docs: []};
  try {
    [pending, recovery, review] = await Promise.all([
      requests.where('status', '==', 'PENDING').orderBy('createdAt').limit(limit).get(),
      requests.where('status', '==', 'PROCESSING').where('leaseUntil', '<=', startedAt).orderBy('leaseUntil').limit(limit).get(),
      manualReview ? requests.where('status', '==', 'REVIEW').orderBy('createdAt').limit(limit).get() : {docs: []}
    ]);
  } catch (error) {
    if (error.code !== 9 && error.code !== 'failed-precondition') throw error;
    // The service remains usable while newly published composite indexes build.
    console.warn('Economy indexes are not ready; processing bounded requests with the single-field indexes.');
    [pending, recovery, review] = await Promise.all([
      requests.where('status', '==', 'PENDING').limit(limit).get(),
      requests.where('leaseUntil', '<=', startedAt).limit(limit).get(),
      manualReview ? requests.where('status', '==', 'REVIEW').limit(limit).get() : {docs: []}
    ]);
  }
  const rows = (manualReview ? review.docs : [...pending.docs, ...recovery.docs]).sort((a, b) =>
    (a.get('createdAt')?.toMillis?.() ?? 0) - (b.get('createdAt')?.toMillis?.() ?? 0)).slice(0, limit);
  for (const row of rows) {
    const lease = randomUUID(), now = clock();
    const command = await db.runTransaction(async tx => {
      const fresh = await tx.get(row.ref), data = fresh.data();
      if (!data || !['PENDING', 'PROCESSING', ...(manualReview ? ['REVIEW'] : [])].includes(data.status)
        || (data.status === 'PROCESSING' && data.leaseUntil > now)) return null;
      const attempts = (data.attempts ?? 0) + 1;
      tx.update(row.ref, {status: 'PROCESSING', lease, leaseUntil: now + leaseMillis, attempts});
      return {...data, attempts};
    });
    if (!command) { stats.skipped++; continue; }
    let result, failure;
    try {
      ensure(command.userId === row.id && command.payload?.id === command.operationId, 'invalid-argument', 'Solicitud no válida');
      const user = await auth.getUser(row.id);
      ensure(!user.disabled && user.providerData?.some(provider => provider.providerId !== 'anonymous') && user.email !== 'coach.guest.reader@gmail.com', 'unauthenticated', 'Inicia sesión');
      const revokedAt = Date.parse(user.tokensValidAfterTime ?? '') || 0;
      ensure(Number.isInteger(command.authTime) && command.authTime * 1000 >= revokedAt, 'unauthenticated', 'Inicia sesión');
      // Read current signed claims from Auth, never privileges supplied in the command.
      result = await executeEconomy(db, {uid: row.id, token: {...user.customClaims, email: user.email ?? '', firebase: {sign_in_provider: 'password'}}}, command.payload, clock, {requestedAtMillis: command.createdAt?.toMillis?.() ?? 0});
    } catch (error) {
      failure = error instanceof EconomyError ? {code: error.code, message: error.message}
        : error.code === 'auth/user-not-found' ? {code: 'unauthenticated', message: 'Inicia sesión'} : null;
      if (!failure) {
        // A commit may have succeeded even if its response was lost. Keep the same ID
        // and require human reconciliation after bounded retries; never start a new charge.
        const review = command.attempts >= 8;
        await db.runTransaction(async tx => {
          const fresh = await tx.get(row.ref);
          if (!fresh.exists || fresh.get('lease') !== lease || fresh.get('operationId') !== command.operationId) return;
          tx.update(row.ref, review ? {status: 'REVIEW', lease: FieldValue.delete(), leaseUntil: FieldValue.delete(),
            reviewAt: FieldValue.serverTimestamp(), reviewReason: 'UNCONFIRMED'} :
            {leaseUntil: clock() + Math.min(3600000, leaseMillis * 2 ** Math.min(command.attempts - 1, 5))});
        });
        stats[review ? 'review' : 'skipped']++;
        continue;
      }
    }
    await db.runTransaction(async tx => {
      const fresh = await tx.get(row.ref);
      if (!fresh.exists || fresh.get('lease') !== lease || fresh.get('operationId') !== command.operationId) return;
      const outcome = {operationId: command.operationId, status: failure ? 'FAILED' : 'COMPLETED', result: failure ? null : result,
        error: failure ?? null, completedAt: FieldValue.serverTimestamp()};
      tx.update(row.ref, {...outcome, lease: FieldValue.delete(), leaseUntil: FieldValue.delete()});
      tx.set(db.doc(`economy_results/${row.id}~${command.operationId}`), {...outcome, userId: row.id, expiresAtMillis: clock() + 86400000});
    });
    stats[failure ? 'failed' : 'completed']++;
  }
  return stats;
}
