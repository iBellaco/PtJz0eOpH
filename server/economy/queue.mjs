import {FieldValue} from 'firebase-admin/firestore';
import {randomUUID} from 'node:crypto';
import {executeEconomy} from './service.mjs';
import {ensure, EconomyError} from './policy.mjs';

/** One private outstanding command per actor; balances and receipts remain server-only. */
export async function processQueue(db, auth, {clock = Date.now, limit = 30, leaseMillis = 120000} = {}) {
  const stats = {completed: 0, failed: 0, skipped: 0};
  const rows = await db.collection('economy_requests').where('status', 'in', ['PENDING', 'PROCESSING']).limit(limit).get();
  for (const row of rows.docs) {
    const lease = randomUUID(), now = clock();
    const command = await db.runTransaction(async tx => {
      const fresh = await tx.get(row.ref), data = fresh.data();
      if (!data || !['PENDING', 'PROCESSING'].includes(data.status) || (data.status === 'PROCESSING' && data.leaseUntil > now)) return null;
      tx.update(row.ref, {status: 'PROCESSING', lease, leaseUntil: now + leaseMillis});
      return data;
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
      result = await executeEconomy(db, {uid: row.id, token: {...user.customClaims, email: user.email ?? '', firebase: {sign_in_provider: 'password'}}}, command.payload, clock);
    } catch (error) {
      failure = error instanceof EconomyError ? {code: error.code, message: error.message}
        : error.code === 'auth/user-not-found' ? {code: 'unauthenticated', message: 'Inicia sesión'} : null;
      if (!failure) { stats.skipped++; continue; } // Retry the same ID after the lease; never claim failure after a lost commit response.
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
