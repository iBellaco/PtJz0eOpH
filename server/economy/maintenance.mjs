import {initializeApp, applicationDefault} from 'firebase-admin/app';
import {getFirestore, FieldValue} from 'firebase-admin/firestore';
import {pathToFileURL} from 'node:url';
import {integer, DAY} from './policy.mjs';

/** Run by the existing hourly trusted worker; never infer a refund from a legacy client budget. */
export async function maintainEconomy(db, now = Date.now()) {
  integer(now); const stats = {refundedSponsors: 0, expiredRequests: 0}; let last;
  do {
    const query = db.collection('pending_sponsor_ads').where('isApproved', '==', false);
    const page = await (last ? query.startAfter(last) : query).limit(100).get();
    for (const notice of page.docs) {
      const refunded = await db.runTransaction(async tx => {
        const fresh = await tx.get(notice.ref), data = fresh.data();
        if (!data || data.isApproved || data.refundedAtMillis || !Number.isSafeInteger(data.createdAt) || data.createdAt + 7 * DAY > now || !Number.isSafeInteger(data.chargedEssence) || data.chargedEssence <= 0 || !/^[a-zA-Z0-9_-]{1,128}$/.test(data.userId ?? '')) return false;
        const profile = db.doc(`users/${data.userId}`), account = (await tx.get(profile)).data();
        const deletion = (await tx.get(db.doc(`account_deletions/${data.userId}`))).data();
        if (!account || ['PROCESSING', 'DATA_PURGED', 'COMPLETED'].includes(deletion?.status)) return false;
        const paid = (await tx.get(profile.collection('economy_operations').doc(notice.id))).data();
        if (paid?.kind !== 'SPONSOR' || paid.cost !== data.chargedEssence) return false;
        const id = `sponsor_refund_${notice.id}`, operation = profile.collection('economy_operations').doc(id);
        if ((await tx.get(operation)).exists) return false;
        const record = {id, timestamp: now, durationMillis: 0, source: 'SPONSOR_REFUND', planName: 'Anuncio no aceptado en siete días', status: 'Rechazado y reembolsado', amount: `+${data.chargedEssence} EA`};
        tx.update(profile, {blueEssence: integer(integer(account.blueEssence ?? 0) + data.chargedEssence)});
        tx.create(operation, {id, userId: data.userId, actorUid: 'service', kind: 'SPONSOR_REFUND', cost: data.chargedEssence, receipt: record, createdAt: FieldValue.serverTimestamp(), timestamp: now});
        tx.create(profile.collection('subscription_history').doc(id), record);
        tx.update(notice.ref, {refundedAtMillis: now, status: 'REFUNDED', isEnabled: false});
        return true;
      });
      if (refunded) stats.refundedSponsors++;
    }
    last = page.size === 100 ? page.docs.at(-1) : null;
  } while (last);
  const expired = await db.collection('cash_redemptions').where('historyDeleteAtMillis', '<=', now).limit(400).get();
  const removable = expired.docs.filter(doc => ['PAID', 'REJECTED'].includes(doc.get('status')));
  if (removable.length) { const batch = db.batch(); removable.forEach(doc => batch.delete(doc.ref)); await batch.commit(); }
  stats.expiredRequests = removable.length;
  return stats;
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  initializeApp({credential: applicationDefault(), projectId: 'wild-rift-drafting'});
  try { console.log('ECONOMY_MAINTENANCE:', JSON.stringify(await maintainEconomy(getFirestore()))); }
  catch { console.error('Economy maintenance failed; no successful refund is claimed.'); process.exitCode = 1; }
}
