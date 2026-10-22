import {pathToFileURL} from 'node:url';

/** Authentication, not a leftover profile, determines whether an account exists.
 * Never deletes profiles, balances, history, or identities. Failed lookups fail closed.
 */
export async function reconcileDirectory(adapter) {
  const report = {checked:0, hidden:0, restored:0, unchanged:0, errors:0};
  for await (const page of adapter.profiles()) for (const profile of page) {
    report.checked++;
    try {
      const exists = await adapter.exists(profile.uid);
      if (typeof exists !== 'boolean') throw new Error('Indeterminate identity');
      if (profile.authAccountExists === exists) { report.unchanged++; continue; }
      if (await adapter.mark(profile.uid, exists)) {
        if (exists) report.restored++; else report.hidden++;
      }
    } catch { report.errors++; }
  }
  return report;
}

export async function productionAdapter() {
  const {db, auth} = await import('./store.mjs');
  const {FieldValue} = await import('firebase-admin/firestore');
  return {
    async *profiles() {
      let cursor;
      do {
        const query = db.collection('users').orderBy('__name__').limit(100);
        const page = await (cursor ? query.startAfter(cursor) : query).get();
        yield page.docs.map(doc => ({uid:doc.id, authAccountExists:doc.get('authAccountExists')}));
        cursor = page.size === 100 ? page.docs.at(-1) : null;
      } while (cursor);
    },
    async exists(uid) {
      try { await auth.getUser(uid); return true; }
      catch (error) { if (error.code === 'auth/user-not-found') return false; throw error; }
    },
    mark: (uid, exists) => db.runTransaction(async tx => {
      const ref = db.collection('users').doc(uid);
      const fresh = await tx.get(ref);
      if (!fresh.exists || fresh.get('authAccountExists') === exists) return false;
      tx.update(ref, {authAccountExists:exists, authAccountCheckedAt:FieldValue.serverTimestamp()});
      return true;
    })
  };
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const report = await reconcileDirectory(await productionAdapter());
  console.log(JSON.stringify(report)); // Counts only: no email addresses or UIDs in job logs.
  if (report.errors) process.exitCode = 1;
}
