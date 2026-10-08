import { initializeApp, applicationDefault, getApp } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';
import { getFirestore, FieldValue, Timestamp } from 'firebase-admin/firestore';
import { getStorage } from 'firebase-admin/storage';
import { mediaObjects, buckets, legacyExternalMedia } from './media.mjs';
import {conversationCleanup} from './conversations.mjs';
export const PROJECT = process.env.FIRESTORE_EMULATOR_HOST ? 'demo-coach-tests' : 'wild-rift-drafting';
initializeApp({ credential: applicationDefault(), projectId: PROJECT });
export const db = getFirestore();
export const auth = getAuth();
export async function account(uid) {
  try {
    const user = await auth.getUser(uid);
    // The SDK's UTC date string loses milliseconds; compare the authoritative raw login time.
    const emulator = process.env.FIREBASE_AUTH_EMULATOR_HOST;
    const token = emulator ? {access_token:'owner'} : await getApp().options.credential.getAccessToken();
    const base = emulator ? `http://${emulator}/identitytoolkit.googleapis.com` : 'https://identitytoolkit.googleapis.com';
    const response = await fetch(`${base}/v1/projects/${PROJECT}/accounts:lookup`, {
      method:'POST', headers:{Authorization:`Bearer ${token.access_token}`,'Content-Type':'application/json'},
      body:JSON.stringify({localId:[uid]})
    });
    if (!response.ok) throw new Error('Cannot verify the authoritative sign-in time');
    const record = (await response.json()).users?.[0];
    if (!record) return null;
    const login = record.lastLoginAt ? Number(record.lastLoginAt) : 0;
    if (!Number.isFinite(login)) throw new Error('Cannot verify the authoritative sign-in time');
    return {...user.toJSON(),lastSignInTimeMillis:login};
  }
  catch (error) { if (error.code === 'auth/user-not-found') return null; throw error; }
}
export const ref = uid => db.collection('account_deletions').doc(uid);
export function normalized(snapshot) {
  if (!snapshot.exists) return null;
  const data = snapshot.data();
  return {...data, requestedAt: data.requestedAt?.toMillis?.(), completedAt: data.completedAt?.toMillis?.(), cancelledAt:data.cancelledAt?.toMillis?.()};
}
export async function compareUpdate(uid, requestId, action) {
  return db.runTransaction(async tx => {
    const snapshot = await tx.get(ref(uid));
    if (!snapshot.exists || snapshot.get('requestId') !== requestId) return false;
    action(tx, snapshot.ref); return true;
  });
}
// Email matching is restricted to legacy records with no different owner UID.
export function owned(data, uid, email) {
  const owner = data.userId || data.ownerUid || data.uid;
  if (owner) return owner === uid;
  return email && ['userEmail','email','sponsorEmail'].some(key => data[key] === email);
}
async function deleteQuery(query, uid, email, predicate=owned) {
  let last;
  do {
    const page = await (last ? query.startAfter(last) : query).limit(100).get();
    for (const doc of page.docs) if (predicate(doc.data(), uid, email)) await db.recursiveDelete(doc.ref);
    last = page.size === 100 ? page.docs.at(-1) : null;
  } while (last);
}
async function cleanSharedRecords(uid,email) {
  for(const name of ['support_reports','moderator_requests']) {
    await deleteQuery(db.collection(name).where('targetUid','==',uid),uid,email,data=>data.targetUid===uid);
    if(email)await deleteQuery(db.collection(name).where('targetEmail','==',email),uid,email,
      data=>!data.targetUid || data.targetUid===uid);
    const authored=await db.collection(name).where('requestedByUid','==',uid).get();
    for(const document of authored.docs)await db.runTransaction(async tx=>{
      const fresh=await tx.get(document.ref);
      if(fresh.get('requestedByUid')===uid)tx.update(document.ref,
        {requestedByUid:FieldValue.delete(),requestedByName:'Moderador'});
    });
  }
  // Scan primary tickets without a new collection-group index, then clean their mirrors.
  let last;
  do {
    const query=db.collection('support_reports');
    const page=await(last?query.startAfter(last):query).limit(100).get();
    for(const document of page.docs)await db.runTransaction(async tx=>{
      const ticket=await tx.get(document.ref);
      if(!ticket.exists)return;
      const owner=ticket.get('userId');
      const mirror=owner?db.doc(`users/${owner}/messages/${document.id}`):null;
      const inbox=mirror?await tx.get(mirror):null;
      for(const snapshot of [ticket,inbox].filter(item=>item?.exists)) {
        const patch=conversationCleanup(snapshot.data(),uid,email);
        if(patch) {
          if(Object.hasOwn(patch,'repliedAt'))patch.repliedAt=Number.isFinite(patch.repliedAt)&&patch.repliedAt>0
            ?Timestamp.fromMillis(patch.repliedAt):FieldValue.delete();
          tx.update(snapshot.ref,patch);
        }
      }
    });
    last=page.size===100?page.docs.at(-1):null;
  }while(last);
}
export async function purgeAccountData(uid, email) {
  const plan = new Map();
  const external = new Set((await ref(uid).get()).get('legacyExternalMedia') || []);
  const addMedia = items => items.forEach(item => plan.set(`${item.bucket}/${item.path}`,item));
  const inspect = entry => { addMedia(mediaObjects(entry)); legacyExternalMedia(entry).forEach(url => external.add(url)); };
  addMedia((await ref(uid).get()).get('mediaObjects') || []);
  const liveNotices = (await db.collection('system_config').doc('app_notices').get()).get('notices') || [];
  for (const entry of liveNotices) if (email && entry.sponsorEmail === email) inspect(entry);
  if (email) {
    const notices = await db.collection('pending_sponsor_ads').where('sponsorEmail','==',email).get();
    for (const doc of notices.docs) if (owned(doc.data(),uid,email)) inspect(doc.data());
  }
  // Include abandoned uploads which no longer have a notice referencing them.
  if (!process.env.FIRESTORE_EMULATOR_HOST) for (const bucket of buckets) {
    try {
      const [files] = await getStorage().bucket(bucket).getFiles({prefix:'notice_videos/'});
      for (const file of files) if (file.metadata?.metadata?.uploaderUid === uid)
        addMedia([{bucket,path:file.name}]);
    } catch (error) { if (error.code !== 404) throw error; }
  }
  await ref(uid).update({mediaObjects:[...plan.values()],legacyExternalMedia:[...external]});
  if (external.size) throw new Error('Legacy external media requires verified provider withdrawal');
  for (const item of plan.values()) {
    const file = getStorage().bucket(item.bucket).file(item.path);
    let metadata;
    try { [metadata] = await file.getMetadata(); }
    catch (error) { if (error.code === 404) continue; throw error; }
    const owner = metadata.metadata?.uploaderUid;
    // Legacy uploads have no verifiable owner. Do not erase another account's file.
    if (!owner || item.bucket !== 'wild-rift-drafting.firebasestorage.app' || !item.path.startsWith(`notice_videos/${owner}/`))
      throw new Error('Legacy media ownership requires verification before deletion');
    if (owner === uid) await file.delete({ignoreNotFound:true});
  }
  for (const name of ['support_reports', 'cash_redemptions', 'streamer_click_metrics', 'pending_sponsor_ads', 'economy_requests', 'economy_results']) {
    await deleteQuery(db.collection(name).where('userId','==',uid), uid, email);
    if (email) for (const field of ['userEmail','email','sponsorEmail'])
      await deleteQuery(db.collection(name).where(field,'==',email), uid, email);
  }
  await cleanSharedRecords(uid,email);
  // Messages belonging to the account can also be mirrored outside its own profile.
  // A collection-group index is not assumed; parent-specific records are deleted recursively.
  const departing = (await db.doc(`users/${uid}`).get()).data();
  for (const creatorUid of (departing?.subscribedCreators || [])) {
    if (!/^[a-zA-Z0-9_-]{1,128}$/.test(creatorUid) || creatorUid === uid) continue;
    await db.runTransaction(async tx => {
      const creator = db.doc(`users/${creatorUid}`), membership = creator.collection('creator_subscribers').doc(uid);
      const parent = await tx.get(creator), entry = await tx.get(membership);
      if (entry.get('userId') !== uid) return;
      if (entry.get('active') && parent.exists) tx.update(creator,
        {creatorSubscriberCount: Math.max(0, (parent.get('creatorSubscriberCount') || 0) - 1)});
      tx.delete(membership);
    });
  }
  const subscribers = await db.collection('users').where('subscribedCreators','array-contains',uid).get();
  for (const doc of subscribers.docs) await doc.ref.update({subscribedCreators: FieldValue.arrayRemove(uid)});
  for (const [id, field, predicate] of [
    ['streamer_live','entries', entry => entry.userId === uid],
    ['app_notices','notices', entry => Boolean(email && entry.sponsorEmail === email)]
  ]) await db.runTransaction(async tx => {
    const document = db.collection('system_config').doc(id), snapshot = await tx.get(document);
    const entries = snapshot.get(field);
    if (Array.isArray(entries)) {
      const filtered = entries.filter(entry => !predicate(entry));
      if (filtered.length !== entries.length) tx.update(document, {[field]:filtered});
    }
  });
  await db.recursiveDelete(db.collection('streamer_requests').doc(uid));
  await db.recursiveDelete(db.collection('users').doc(uid));
}
export const adapter = {
  read: async uid => normalized(await ref(uid).get()), account,
  claim: async (uid, expected, user, now) => db.runTransaction(async tx => {
    const snapshot = await tx.get(ref(uid)), current = normalized(snapshot);
    if (!current || current.requestId !== expected.requestId ||
        !['PENDING','PROCESSING','DATA_PURGED'].includes(current.status)) return null;
    if (now < current.requestedAt + 60 * 86400000) return null;
    if (current.status === 'PENDING') {
      const fields = {status:'PROCESSING', startedAt:FieldValue.serverTimestamp(),
        accountEmail:user?.email || '', wasDisabled:Boolean(user?.disabled)};
      tx.update(snapshot.ref, fields);
      return {...current, ...fields};
    }
    return current;
  }),
  disable: uid => auth.updateUser(uid,{disabled:true}),
  enable: uid => auth.updateUser(uid,{disabled:false}),
  cancel: (uid,id) => compareUpdate(uid,id,(tx,document) => tx.update(document,
    {status:'CANCELLED',cancelledAt:FieldValue.serverTimestamp(),accountEmail:FieldValue.delete(),wasDisabled:FieldValue.delete()})),
  purge: purgeAccountData,
  markPurged: (uid,id) => compareUpdate(uid,id,(tx,document) => tx.update(document,{status:'DATA_PURGED', dataPurgedAt:FieldValue.serverTimestamp()})),
  deleteIdentity: async uid => { try { await auth.deleteUser(uid); } catch (error) { if (error.code !== 'auth/user-not-found') throw error; } },
  complete: (uid,id,now) => compareUpdate(uid,id,(tx,document) => tx.update(document,
    {status:'COMPLETED', completedAt:Timestamp.fromMillis(now), accountEmail:FieldValue.delete(), wasDisabled:FieldValue.delete(),mediaObjects:FieldValue.delete(),legacyExternalMedia:FieldValue.delete()})),
  remove: (uid,id) => compareUpdate(uid,id,(tx,document) => tx.delete(document))
};
export async function permissions() {
  const required = ['datastore.entities.get','datastore.entities.list','datastore.entities.create','datastore.entities.delete','datastore.entities.update',
    'firebaseauth.users.get','firebaseauth.users.delete','firebaseauth.users.update','storage.objects.get','storage.objects.list','storage.objects.delete'];
  const token = await getApp().options.credential.getAccessToken();
  const response = await fetch(`https://cloudresourcemanager.googleapis.com/v1/projects/${PROJECT}:testIamPermissions`, {
    method:'POST', headers:{Authorization:`Bearer ${token.access_token}`,'Content-Type':'application/json'},
    body:JSON.stringify({permissions:required})
  });
  if (!response.ok) throw new Error(`IAM verification HTTP ${response.status}`);
  const actual = (await response.json()).permissions || [];
  const missing = required.filter(permission => !actual.includes(permission));
  if (missing.length) throw new Error(`Missing lifecycle permissions: ${missing.join(', ')}`);
  const sample = await auth.listUsers(1);
  if (sample.users.length) await account(sample.users[0].uid);
  await db.collection('system_config').doc('account_deletion_service').get();
}
export async function heartbeat() {
  await db.collection('system_config').doc('account_deletion_service').set({enabled:true,checkedAt:FieldValue.serverTimestamp(),graceDays:60});
}
