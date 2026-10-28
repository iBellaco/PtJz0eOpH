import {readFileSync} from 'node:fs';
import assert from 'node:assert/strict';
import {initializeTestEnvironment, assertFails, assertSucceeds} from '@firebase/rules-unit-testing';
import {doc, setDoc, getDoc, getDocs, collection, query, where, runTransaction, updateDoc} from 'firebase/firestore';
const env = await initializeTestEnvironment({projectId:'demo-coach-tests', firestore:{host:'127.0.0.1',port:8088,
  rules:readFileSync(new URL('../../firestore.rules',import.meta.url),'utf8')}});
const admin = env.authenticatedContext('admin',{admin:true,email_verified:true}).firestore();
const owner = env.authenticatedContext('owner',{email_verified:true}).firestore();
const stranger = env.authenticatedContext('stranger',{email_verified:true}).firestore();
const missingAdmin = env.authenticatedContext('missing-admin',{admin:true,email_verified:true}).firestore();
const bannedAdmin = env.authenticatedContext('banned-admin',{admin:true,email_verified:true}).firestore();
async function resolve(store,id,approve) {
  const ref=doc(store,'pending_sponsor_ads',id), registry=doc(store,'system_config/app_notices');
  return runTransaction(store,async tx=>{
    const request=await tx.get(ref), current=await tx.get(registry), data=request.data();
    assert.ok(data && !data.isApproved && !data.refundedAtMillis);
    const notices=(current.data()?.notices??[]).filter(n=>n.id!==id);
    if(approve) {
      const changes={isApproved:true,isEnabled:true,approvedAtMillis:1000,expiresAtMillis:86401000};
      tx.update(ref,changes);
      tx.set(registry,{notices:[...notices,{id,title:data.title,tag:'PUBLICIDAD',...changes}]},{merge:true});
    } else { tx.delete(ref); tx.set(registry,{notices},{merge:true}); }
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
    for(const id of ['approve','reject','refunded']) await setDoc(doc(db,'pending_sponsor_ads',id),{
      userId:'owner',title:'Oferta',isApproved:false,isEnabled:false,...(id==='refunded'?{refundedAtMillis:123}:{})});
  });
  assert.equal((await assertSucceeds(getDocs(query(collection(owner,'pending_sponsor_ads'),where('userId','==','owner'))))).size,3);
  await assertFails(getDoc(doc(stranger,'pending_sponsor_ads/approve')));
  await assertFails(resolve(owner,'approve',true));
  await assertFails(resolve(missingAdmin,'approve',true));
  await assertFails(resolve(bannedAdmin,'approve',true));
  assert.equal((await getDoc(doc(owner,'pending_sponsor_ads/approve'))).get('isApproved'),false);
  await assertSucceeds(resolve(admin,'approve',true));
  assert.equal((await getDoc(doc(owner,'pending_sponsor_ads/approve'))).get('isApproved'),true);
  const registry=await getDoc(doc(admin,'system_config/app_notices'));
  assert.deepEqual(registry.get('notices').map(n=>n.id),['existing','approve']);
  assert.equal(registry.get('streamerIntervalValue'),15);
  await assert.rejects(resolve(admin,'approve',true));
  await assertSucceeds(resolve(admin,'reject',false));
  assert.equal((await getDoc(doc(admin,'pending_sponsor_ads/reject'))).exists(),false);
  await assertFails(updateDoc(doc(admin,'pending_sponsor_ads/refunded'),{isApproved:true}));
  // The old client sent this extra field; rules reject it.
  await assertFails(updateDoc(doc(admin,'pending_sponsor_ads/approve'),{approvedAt:1000}));
  console.log('PASS sponsor ownership, active administrator, atomic publication, repeat approval, rejection and refunded protection');
} finally { await env.cleanup(); }
