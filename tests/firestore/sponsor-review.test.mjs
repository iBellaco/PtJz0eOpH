import {readFileSync} from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment, assertFails, assertSucceeds} from '@firebase/rules-unit-testing';
import {doc, setDoc, getDoc, getDocFromServer, getDocs, getDocsFromServer, collection, query, where, runTransaction, updateDoc, deleteDoc, serverTimestamp} from 'firebase/firestore';
const env = await initializeTestEnvironment({projectId:'demo-coach-tests', firestore:{host:'127.0.0.1',port:8088,
  rules:readFileSync(new URL('../../firestore.rules',import.meta.url),'utf8')}});
const admin = env.authenticatedContext('admin',{admin:true,email_verified:true}).firestore();
const owner = env.authenticatedContext('owner',{email_verified:true}).firestore();
const stranger = env.authenticatedContext('stranger',{email_verified:true}).firestore();
const missingAdmin = env.authenticatedContext('missing-admin',{admin:true,email_verified:true}).firestore();
const bannedAdmin = env.authenticatedContext('banned-admin',{admin:true,email_verified:true}).firestore();
async function resolve(store,id,approve) {
  const ref=doc(store,'pending_sponsor_ads',id), registry=doc(store,'system_config/app_notices'),
    publicRef=doc(store,`app_notices/${id}`);
  return runTransaction(store,async tx=>{
    const request=await tx.get(ref), current=await tx.get(registry), data=request.data();
    assert.ok(data && !data.refundedAtMillis && !['REJECTED','REFUNDED'].includes(data.status));
    assert.ok(approve || !data.isApproved);
    const notices=(current.data()?.notices??[]).filter(n=>n.id!==id);
    if(approve) {
      // Mirror SponsorReviewRepository.resolve, including the status transition.
      const now=Date.now();
      const changes={status:'APROBADA',isApproved:true,isEnabled:true,
        approvedAtMillis:data.isApproved ? data.approvedAtMillis : now,
        expiresAtMillis:data.isApproved ? data.expiresAtMillis : now+86400000};
      tx.update(ref,changes);
      const {status,...publicChanges}=changes;
      const published={id,title:data.title,tag:'PUBLICIDAD',...publicChanges};
      tx.set(registry,{notices:[...notices,published]},{merge:true});
      tx.set(publicRef,published);
    } else {
      const operationId=`sponsor_review_${id}_reject_test`, queue=doc(store,'economy_requests',`admin~${operationId}`);
      await tx.get(queue);
      tx.update(ref,{status:'REJECTED',isApproved:false,isEnabled:false});
      tx.set(registry,{notices},{merge:true});
      tx.delete(publicRef);
      tx.set(queue,{userId:'admin',operationId,payload:{action:'SPONSOR_RESOLVE',id:operationId,sponsorId:id,approved:false},
        status:'PENDING',schema:2,createdAt:serverTimestamp(),authTime:0});
    }
  });
}
try {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async ctx=>{
    const db=ctx.firestore();
    // Administrative authorization also requires an existing, active profile.
    for (const [uid, role, banned] of [['admin','admin',false],['owner','patrocinador',false],
      ['stranger','free',false],['banned-admin','admin',true]]) {
      await setDoc(doc(db,'users',uid),{role,banned});
    }
    await setDoc(doc(db,'system_config/app_notices'),{streamerIntervalValue:15,notices:[{id:'existing',title:'Conservar'}]});
    await setDoc(doc(db,'app_notices/reject'),{id:'reject',title:'Retirar',tag:'PUBLICIDAD',isApproved:true,isEnabled:true});
    for(const id of ['approve','reject','refunded']) await setDoc(doc(db,'pending_sponsor_ads',id),{
      userId:'owner',title:'Oferta',isApproved:false,isEnabled:false,...(id==='refunded'?{refundedAtMillis:123}:{})});
  });
  assert.equal((await assertSucceeds(getDocs(query(collection(owner,'pending_sponsor_ads'),where('userId','==','owner'))))).size,3);
  await assertFails(getDoc(doc(stranger,'pending_sponsor_ads/approve')));
  await assertFails(resolve(owner,'approve',true));
  await assertFails(resolve(missingAdmin,'approve',true));
  await assertFails(resolve(bannedAdmin,'approve',true));
  assert.equal((await getDoc(doc(owner,'pending_sponsor_ads/approve'))).get('isApproved'),false);
  // Invalid transitions must remain forbidden even for an active administrator.
  await assertFails(updateDoc(doc(admin,'pending_sponsor_ads/approve'),{status:'APROBADA',isApproved:false,isEnabled:true}));
  await assertFails(updateDoc(doc(admin,'pending_sponsor_ads/approve'),{status:'APROBADA',isApproved:true,isEnabled:false}));
  await assertFails(updateDoc(doc(admin,'pending_sponsor_ads/approve'),{status:'ARBITRARY'}));
  await assertSucceeds(resolve(admin,'approve',true));
  assert.equal((await getDoc(doc(owner,'pending_sponsor_ads/approve'))).get('isApproved'),true);
  const registry=await getDoc(doc(admin,'system_config/app_notices'));
  assert.deepEqual(registry.get('notices').map(n=>n.id),['existing','approve']);
  assert.equal(registry.get('streamerIntervalValue'),15);
  const publicAd=await assertSucceeds(getDoc(doc(stranger,'app_notices/approve')));
  assert.equal(publicAd.get('tag'),'PUBLICIDAD');
  assert.equal(publicAd.get('isApproved'),true);
  assert.equal(publicAd.get('sponsorEmail'),undefined);
  await assertSucceeds(resolve(admin,'approve',true));
  // Reopen with a fresh session and bypass the cache: the approval must persist,
  // leave the pending list, and be readable/renderable on another user's Home.
  const reopened=env.authenticatedContext('admin',{admin:true,email_verified:true}).firestore();
  const saved=await getDocFromServer(doc(reopened,'pending_sponsor_ads/approve'));
  assert.equal(saved.get('status'),'APROBADA');
  const pending=await getDocsFromServer(query(collection(reopened,'pending_sponsor_ads'),where('isApproved','==',false)));
  assert.ok(!pending.docs.some(row=>row.id==='approve'));
  const home=await getDocsFromServer(collection(stranger,'app_notices'));
  const visible=home.docs.find(row=>row.id==='approve');
  assert.ok(visible && visible.get('isApproved') && visible.get('isEnabled') && visible.get('title') && visible.get('expiresAtMillis')>Date.now());
  assert.equal(saved.get('expiresAtMillis'),publicAd.get('expiresAtMillis'));
  await assertFails(updateDoc(doc(admin,'pending_sponsor_ads/approve'),{status:'PENDING',isApproved:false}));
  await assertSucceeds(resolve(admin,'reject',false));
  assert.equal((await getDoc(doc(owner,'pending_sponsor_ads/reject'))).get('status'),'REJECTED');
  assert.equal((await getDoc(doc(stranger,'app_notices/reject'))).exists(),false);
  assert.equal((await getDoc(doc(admin,'economy_requests/admin~sponsor_review_reject_reject_test'))).get('payload.action'),'SPONSOR_RESOLVE');
  await assertFails(updateDoc(doc(owner,'pending_sponsor_ads/reject'),{status:'PENDING'}));
  await assertFails(updateDoc(doc(admin,'pending_sponsor_ads/reject'),{isApproved:true,isEnabled:true}));
  await assertFails(updateDoc(doc(admin,'pending_sponsor_ads/refunded'),{isApproved:true}));
  // The old client sent this extra field; rules reject it.
  await assertFails(updateDoc(doc(admin,'pending_sponsor_ads/approve'),{approvedAt:1000}));
  await env.withSecurityRulesDisabled(async context => {
    const db = context.firestore();
    await setDoc(doc(db,'pending_sponsor_ads/delete_me'), {id:'delete_me', userId:'owner', title:'Borrar', isApproved:true, isEnabled:true, status:'APROBADA'});
    await setDoc(doc(db,'app_notices/delete_me'), {id:'delete_me', title:'Borrar', tag:'PUBLICIDAD', isApproved:true, isEnabled:true});
  });
  await assertSucceeds(deleteDoc(doc(owner,'app_notices/delete_me')));
  await assertSucceeds(deleteDoc(doc(owner,'pending_sponsor_ads/delete_me')));
  await assertFails(deleteDoc(doc(stranger,'pending_sponsor_ads/approve')));
  console.log('PASS exact Android approval payload, server-confirmed reopening, public Home visibility, idempotent approval, ownership, rejection and refunded protection');
} finally {
  // cleanup() closes clients but does not remove emulator documents. This
  // suite publishes a live ad; leaving it behind changes occupancy-based
  // pricing in the economy suite that runs next in CI.
  try { await env.clearFirestore(); }
  finally { await env.cleanup(); }
}
