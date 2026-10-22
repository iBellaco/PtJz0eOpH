import {pathToFileURL} from 'node:url';

export const DEFAULT_MAX_AGE_DAYS = 7;
export const GUEST_EMAIL = 'coach.guest.reader@gmail.com';

export function unverifiedBefore(user, cutoff) {
  const created = Date.parse(user.metadata?.creationTime || '');
  const login = Date.parse(user.metadata?.lastSignInTime || '');
  return user.email?.toLowerCase() !== GUEST_EMAIL && Boolean(user.email) &&
    user.providerData?.length === 1 && user.providerData[0].providerId === 'password' &&
    !Object.keys(user.customClaims || {}).length && !user.disabled && user.emailVerified === false &&
    (!Number.isFinite(login) || login < cutoff) &&
    Number.isFinite(created) && created < cutoff;
}

export function protectedProfile(profile) {
  if (!profile) return false;
  return !['', 'free'].includes(String(profile.role ?? '').toLowerCase()) ||
    Boolean(profile.secondaryRole || profile.admin || profile.protected || profile.premiumGranted) ||
    ['premiumUntil', 'blueEssence', 'orangeEssence', 'creatorSubscriberCount'].some(key =>
      profile[key] != null && profile[key] !== 0) ||
    Boolean(profile.subscriptionPlan && !['FREE', 'free'].includes(profile.subscriptionPlan)) ||
    Boolean(profile.subscribedCreators?.length);
}

export async function cleanupUnverified({ apply = false, now = Date.now(), maxAgeDays = DEFAULT_MAX_AGE_DAYS,
  adapter, protectedUids = [] } = {}) {
  if (!adapter) throw new Error('Cleanup adapter required');
  if (!Number.isFinite(maxAgeDays) || maxAgeDays < DEFAULT_MAX_AGE_DAYS || !Number.isFinite(now))
    throw new Error('Invalid cleanup retention');
  const cutoff = now - maxAgeDays * 24 * 60 * 60 * 1000;
  let pageToken;
  const result = {inspected: 0, candidates: 0, deleted: 0, recovered: 0, skipped: 0, errors: [], applied: apply, maxAgeDays};
  const fail = (stage, error) => result.errors.push({stage, code: /^[\w/-]+$/.test(error.code || '') ? error.code : 'operation-failed'});
  const finish = async uid => {
    if (protectedUids.includes(uid) || protectedProfile(await adapter.profile(uid)) || await adapter.hasActivity(uid))
      throw Object.assign(new Error('Protected data requires manual recovery'), {code:'cleanup/protected-profile'});
    await adapter.deleteProfile(uid);
    await adapter.complete(uid);
  };
  // A durable queue covers crashes between identity removal and profile cleanup.
  if (apply) for (const uid of await adapter.pending()) {
    try {
      if (await adapter.getUser(uid)) { await adapter.complete(uid); continue; }
      await finish(uid);
      result.recovered++;
    } catch (error) { fail('recover-profile', error); }
  }
  do {
    const page = await adapter.listUsers(1000, pageToken);
    for (const user of page.users) {
      result.inspected++;
      if (!unverifiedBefore(user, cutoff)) continue;
      try {
        if (protectedUids.includes(user.uid) || protectedProfile(await adapter.profile(user.uid)) ||
            await adapter.hasActivity(user.uid)) { result.skipped++; continue; }
        result.candidates++;
        if (!apply) continue;
        await adapter.enqueue(user.uid);
        // Recheck identity and benefits immediately before removal.
        const fresh = await adapter.getUser(user.uid);
        if (!fresh || !unverifiedBefore(fresh, cutoff) || protectedProfile(await adapter.profile(user.uid)) ||
            await adapter.hasActivity(user.uid)) {
          await adapter.complete(user.uid);
          result.skipped++;
          continue;
        }
        await adapter.deleteUser(user.uid);
        await finish(user.uid);
        result.deleted++;
      } catch (error) { fail('remove-account', error); }
    }
    pageToken = page.pageToken;
  } while (pageToken);
  return result;
}

export function storeAdapter(auth, db) {
  const queue = db.collection('unverified_cleanup_jobs');
  return {
    listUsers: (limit, token) => auth.listUsers(limit, token),
    getUser: async uid => {
      try { return await auth.getUser(uid); }
      catch (error) { if (error.code === 'auth/user-not-found') return null; throw error; }
    },
    profile: async uid => (await db.doc(`users/${uid}`).get()).data(),
    hasActivity: async uid => {
      if ((await db.doc(`users/${uid}`).listCollections()).length) return true;
      for (const path of [`account_deletions/${uid}`, `economy_requests/${uid}`, `streamer_requests/${uid}`])
        if ((await db.doc(path).get()).exists) return true;
      for (const name of ['economy_results', 'cash_redemptions', 'pending_sponsor_ads', 'support_reports'])
        if (!(await db.collection(name).where('userId', '==', uid).limit(1).get()).empty) return true;
      return false;
    },
    pending: async () => (await queue.limit(100).get()).docs.map(doc => doc.id),
    enqueue: uid => queue.doc(uid).set({status: 'PENDING', updatedAt: new Date()}),
    complete: uid => queue.doc(uid).delete(),
    deleteUser: uid => auth.deleteUser(uid),
    deleteProfile: uid => db.recursiveDelete(db.doc(`users/${uid}`)),
  };
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const {auth, db} = await import('./store.mjs');
  const apply = process.argv.includes('--apply');
  const result = await cleanupUnverified({ apply, adapter: storeAdapter(auth, db),
    protectedUids: (process.env.COACH_PROTECTED_ACCOUNT_UIDS || '').split(',').map(value => value.trim()).filter(Boolean) });
  console.log('UNVERIFIED_CLEANUP:', JSON.stringify(result));
  if (result.errors.length) process.exitCode = 1;
}
