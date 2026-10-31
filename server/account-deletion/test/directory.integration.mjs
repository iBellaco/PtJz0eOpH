import assert from 'node:assert/strict';
import {reconcileDirectory,productionAdapter} from '../reconcile-directory.mjs';
import {db,auth} from '../store.mjs';
assert.ok(process.env.FIRESTORE_EMULATOR_HOST?.startsWith('127.0.0.1:'));
assert.ok(process.env.FIREBASE_AUTH_EMULATOR_HOST?.startsWith('127.0.0.1:'));
const ids=['directory-live','directory-orphan'];
try {
  await auth.createUser({uid:ids[0]});
  for(const uid of ids)await db.doc(`users/${uid}`).set({role:'free',blueEssence:123});
  const adapter=await productionAdapter();
  // Scope the real adapter to this fixture; other emulator fixtures are unrelated.
  adapter.profiles=async function*(){yield await Promise.all(ids.map(async uid=>{
    const doc=await db.doc(`users/${uid}`).get();return {uid,authAccountExists:doc.get('authAccountExists')};
  }));};
  let result=await reconcileDirectory(adapter);
  assert.equal(result.errors,0);assert.equal(result.hidden,1);assert.equal(result.restored,1);
  assert.equal((await db.doc(`users/${ids[1]}`).get()).get('blueEssence'),123);
  result=await reconcileDirectory(adapter);assert.equal(result.unchanged,2);
  await auth.createUser({uid:ids[1]});
  result=await reconcileDirectory(adapter);assert.equal(result.restored,1);
  assert.equal((await db.doc(`users/${ids[1]}`).get()).get('authAccountExists'),true);
  console.log('PASS authoritative directory reconciliation, idempotency, restoration, preserved balance');
}finally{
  for(const uid of ids){await auth.deleteUser(uid).catch(()=>{});await db.doc(`users/${uid}`).delete();}
}
