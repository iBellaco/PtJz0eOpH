import assert from 'node:assert/strict';
import {cleanupUnverified,storeAdapter} from '../cleanup-unverified.mjs';
if (!/^127\.0\.0\.1:\d+$/.test(process.env.FIRESTORE_EMULATOR_HOST || '') ||
    !/^127\.0\.0\.1:\d+$/.test(process.env.FIREBASE_AUTH_EMULATOR_HOST || ''))
  throw new Error('Isolated loopback emulators required');
const {auth,db,PROJECT}=await import('../store.mjs');
assert.equal(PROJECT,'demo-coach-tests');
const ids=['cleanup-fixture-stale','cleanup-fixture-staff','cleanup-fixture-benefits','cleanup-fixture-verified'];
for(const uid of ids) {
  await auth.createUser({uid,email:`${uid}@test.invalid`,password:'OnlyFixture123!',emailVerified:uid.endsWith('verified')});
  await db.doc(`users/${uid}`).set(uid.endsWith('staff')?{role:'moderador'}:uid.endsWith('benefits')?{role:'free',orangeEssence:10}:{role:'free'});
}
const adapter=storeAdapter(auth,db);
// Only these synthetic accounts can enter the fixture's destructive scan.
adapter.listUsers=async()=>({users:await Promise.all(ids.map(uid=>auth.getUser(uid)))});
const realPending=adapter.pending;
adapter.pending=async()=>(await realPending()).filter(uid=>ids.includes(uid));
const future=Date.now()+8*86400000;
const result=await cleanupUnverified({adapter,apply:true,now:future});
assert.equal(result.deleted,1); assert.deepEqual(result.errors,[]);
await assert.rejects(auth.getUser(ids[0]),error=>error.code==='auth/user-not-found');
assert.equal((await db.doc(`users/${ids[0]}`).get()).exists,false);
for(const uid of ids.slice(1)) {
  assert.ok(await auth.getUser(uid)); assert.equal((await db.doc(`users/${uid}`).get()).exists,true);
}
assert.equal((await db.doc(`unverified_cleanup_jobs/${ids[0]}`).get()).exists,false);
console.log('UNVERIFIED_CLEANUP_INTEGRATION: stale fixture removed; staff, benefits and verified identities preserved; private recovery queue completed.');
