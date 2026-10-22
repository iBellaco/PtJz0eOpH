import { auth, db } from './store.mjs';

export const DEFAULT_MAX_AGE_DAYS = 7;
export const GUEST_EMAIL = 'coach.guest.reader@gmail.com';

export function unverifiedBefore(user, cutoff) {
  const created = Date.parse(user.metadata?.creationTime || '');
  return user.email !== GUEST_EMAIL && user.emailVerified === false &&
    Number.isFinite(created) && created < cutoff;
}

export async function cleanupUnverified({ apply = false, now = Date.now(), maxAgeDays = DEFAULT_MAX_AGE_DAYS } = {}) {
  const cutoff = now - maxAgeDays * 24 * 60 * 60 * 1000;
  let pageToken;
  let inspected = 0;
  let candidates = 0;
  let deleted = 0;
  do {
    const page = await auth.listUsers(1000, pageToken);
    for (const user of page.users) {
      inspected += 1;
      if (!unverifiedBefore(user, cutoff)) continue;
      candidates += 1;
      if (!apply) continue;
      await auth.deleteUser(user.uid);
      await db.collection('users').doc(user.uid).delete().catch(() => {});
      deleted += 1;
    }
    pageToken = page.pageToken;
  } while (pageToken);
  return { inspected, candidates, deleted, applied: apply, maxAgeDays };
}

if (import.meta.url === `file://${process.argv[1]}`) {
  const apply = process.argv.includes('--apply');
  const result = await cleanupUnverified({ apply });
  console.log(`UNVERIFIED_CLEANUP: inspected=${result.inspected} candidates=${result.candidates} deleted=${result.deleted} applied=${apply}`);
}
