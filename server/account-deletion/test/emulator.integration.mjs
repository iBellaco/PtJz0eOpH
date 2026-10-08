import assert from 'node:assert/strict';
import {Timestamp} from 'firebase-admin/firestore';
import {processRequest,GRACE_MS} from '../policy.mjs';
if (!process.env.FIRESTORE_EMULATOR_HOST || !process.env.FIREBASE_AUTH_EMULATOR_HOST)
  throw new Error('This integration test requires both local emulators; production access is prohibited.');
const {db,auth,adapter,PROJECT}=await import('../store.mjs');
assert.equal(PROJECT,'demo-coach-tests');
const now=Date.now(),uid='deletion-integration-owner',other='deletion-integration-other';
const email='deletion-owner@test.invalid';
await auth.createUser({uid,email,password:'EmulatorOnlyPass123!'});
await auth.createUser({uid:other,email:'deletion-other@test.invalid',password:'EmulatorOnlyPass123!'});
await db.doc(`users/${uid}`).set({role:'free',email,subscribedCreators:[other]});
await db.doc(`users/${uid}/messages/private`).set({userId:uid,photos:['private-test-image']});
await db.doc(`users/${uid}/subscription_history/receipt`).set({amount:'test',userId:uid});
await db.doc(`users/${other}`).set({role:'free',subscribedCreators:[uid,'keep-creator'],creatorSubscriberCount:1});
await db.doc(`users/${other}/creator_subscribers/${uid}`).set({userId:uid,active:true});
await db.doc('support_reports/deletion-owned').set({userId:uid,userEmail:email});
await db.doc('support_reports/deletion-foreign').set({userId:other,userEmail:email});
await db.doc('support_reports/deletion-legacy').set({userEmail:email});
await db.doc('cash_redemptions/deletion-owned').set({userId:uid});
await db.doc(`streamer_requests/${uid}`).set({userId:uid});
await db.doc(`streamer_requests/${uid}/history/private`).set({userId:uid});
await db.doc('streamer_click_metrics/deletion-owned').set({userId:uid});
await db.doc('pending_sponsor_ads/deletion-owned').set({sponsorEmail:email});
await db.doc('moderator_requests/deletion-target').set({targetUid:uid,targetEmail:email,requestedByUid:other});
await db.doc('support_reports/deletion-moderation-target').set({targetUid:uid,targetEmail:email,requestedByUid:other});
await db.doc('moderator_requests/deletion-authored').set({targetUid:other,requestedByUid:uid,requestedByName:'deleted person'});
const sharedConversation=[{senderUid:other,senderRole:'USER',text:'keep private question'},
  {senderUid:uid,senderRole:'SUPPORT',senderName:'deleted person',text:'erase answer'}];
const sharedTicket={userId:other,conversation:sharedConversation,adminReply:'erase answer',repliedBy:'deleted person'};
await db.doc('support_reports/deletion-shared').set(sharedTicket);
await db.doc(`users/${other}/messages/deletion-shared`).set(sharedTicket);
await db.doc('system_config/streamer_live').set({entries:[{userId:uid},{userId:other}]});
await db.doc('system_config/app_notices').set({notices:[{sponsorEmail:email},{sponsorEmail:'other@test.invalid'}]});
await db.doc(`account_deletions/${uid}`).set({userId:uid,requestId:'12345678-1234-1234-1234-123456789012',status:'PENDING',graceDays:60,requestedAt:Timestamp.fromMillis(now-GRACE_MS-1000)});
assert.equal(await processRequest(uid,adapter,now),'deleted');
for(const path of [`users/${uid}`,`users/${uid}/messages/private`,`users/${uid}/subscription_history/receipt`,
  'support_reports/deletion-owned','support_reports/deletion-legacy','cash_redemptions/deletion-owned',
  `streamer_requests/${uid}`,`streamer_requests/${uid}/history/private`,'streamer_click_metrics/deletion-owned','pending_sponsor_ads/deletion-owned',
  'moderator_requests/deletion-target','support_reports/deletion-moderation-target'])
  assert.equal((await db.doc(path).get()).exists,false,path);
assert.equal((await db.doc('support_reports/deletion-foreign').get()).exists,true);
for(const path of ['support_reports/deletion-shared',`users/${other}/messages/deletion-shared`]) {
  const record=(await db.doc(path).get()).data();
  assert.deepEqual(record.conversation,[sharedConversation[0]]);assert.equal(record.adminReply,'');assert.equal(record.repliedBy,'');
}
const authored=(await db.doc('moderator_requests/deletion-authored').get()).data();
assert.equal(authored.targetUid,other);assert.equal(authored.requestedByUid,undefined);assert.equal(authored.requestedByName,'Moderador');
assert.deepEqual((await db.doc(`users/${other}`).get()).get('subscribedCreators'),['keep-creator']);
assert.equal((await db.doc(`users/${other}/creator_subscribers/${uid}`).get()).exists,false);
assert.equal((await db.doc(`users/${other}`).get()).get('creatorSubscriberCount'),0);
assert.deepEqual((await db.doc('system_config/streamer_live').get()).get('entries'),[{userId:other}]);
await assert.rejects(auth.getUser(uid),error=>error.code==='auth/user-not-found');
assert.ok(await auth.getUser(other));
const tombstone=(await db.doc(`account_deletions/${uid}`).get()).data();
assert.equal(tombstone.status,'COMPLETED');assert.equal(tombstone.accountEmail,undefined);assert.equal(tombstone.mediaObjects,undefined);
assert.equal(await processRequest(uid,adapter,now+1),'skipped');
console.log('ACCOUNT_DELETION_INTEGRATION: identity and associated records removed; unrelated account preserved; retries idempotent.');

const recovery='deletion-integration-recovery',recoveryEmail='deletion-recovery@test.invalid';
await auth.createUser({uid:recovery,email:recoveryEmail,password:'EmulatorOnlyPass123!'});
await db.doc(`users/${recovery}`).set({role:'free',email:recoveryEmail});
await db.doc(`account_deletions/${recovery}`).set({userId:recovery,requestId:'12345678-1234-1234-1234-123456789013',status:'PENDING',graceDays:60,requestedAt:Timestamp.fromMillis(now-86400000)});
const response=await fetch(`http://${process.env.FIREBASE_AUTH_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=emulator-only`,{
  method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({email:recoveryEmail,password:'EmulatorOnlyPass123!',returnSecureToken:true})});
assert.equal(response.ok,true);
assert.equal(await processRequest(recovery,adapter,Date.now()),'cancelled');
assert.equal((await db.doc(`account_deletions/${recovery}`).get()).get('status'),'CANCELLED');
assert.equal((await db.doc(`users/${recovery}`).get()).exists,true);assert.equal((await auth.getUser(recovery)).disabled,false);
console.log('ACCOUNT_DELETION_INTEGRATION: real emulator sign-in cancels recovery without erasing account data.');
const held='deletion-integration-legacy',heldEmail='deletion-legacy@test.invalid';
await auth.createUser({uid:held,email:heldEmail,password:'EmulatorOnlyPass123!'});
await db.doc(`users/${held}`).set({email:heldEmail,role:'free'});
await db.doc('system_config/app_notices').set({notices:[{sponsorEmail:heldEmail,videoUrl:'https://files.catbox.moe/legacy.mp4'}]});
await db.doc(`account_deletions/${held}`).set({userId:held,requestId:'12345678-1234-1234-1234-123456789014',status:'PENDING',graceDays:60,requestedAt:Timestamp.fromMillis(now-GRACE_MS-1000)});
await assert.rejects(processRequest(held,adapter,Date.now()),/verified provider withdrawal/);
assert.equal((await db.doc(`account_deletions/${held}`).get()).get('status'),'PROCESSING');
assert.equal((await db.doc(`users/${held}`).get()).exists,true);
assert.equal((await auth.getUser(held)).disabled,true);
console.log('ACCOUNT_DELETION_INTEGRATION: unverified legacy external media cannot falsely complete or erase credentials.');
process.exit(0);
