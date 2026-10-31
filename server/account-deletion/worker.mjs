import { adapter, db, permissions, heartbeat } from './store.mjs';
import { processRequest } from './policy.mjs';
try {
  await permissions();
  const stats = {deleted:0,cancelled:0,skipped:0,failed:0};
  let last;
  do {
    const query = db.collection('account_deletions');
    const page = await (last ? query.startAfter(last) : query).limit(100).get();
    for (const doc of page.docs) {
      try { stats[await processRequest(doc.id, adapter, Date.now())]++; }
      catch { stats.failed++; }
    }
    last = page.size === 100 ? page.docs.at(-1) : null;
  } while (last);
  if (stats.failed) {
    console.error(`ACCOUNT_DELETION_WORKER: ${stats.failed} requests require retry; no identifying data logged.`);
    process.exitCode = 1;
  } else await heartbeat();
  console.log('ACCOUNT_DELETION_WORKER:', JSON.stringify(stats));
} catch {
  console.error('ACCOUNT_DELETION_WORKER: connection or permissions failed; no success claimed.');
  process.exitCode = 1;
}
