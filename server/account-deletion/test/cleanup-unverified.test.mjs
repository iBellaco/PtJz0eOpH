import test from 'node:test';
import assert from 'node:assert/strict';
import { unverifiedBefore, protectedProfile, cleanupUnverified } from '../cleanup-unverified.mjs';

const now = Date.parse('2026-10-22T00:00:00Z');
const cutoff = now - 7 * 24 * 60 * 60 * 1000;

test('only old unverified non-guest accounts are candidates', () => {
  assert.equal(unverifiedBefore({ uid: 'old', email: 'old@example.com', emailVerified: false, providerData: [{providerId:'password'}],
    metadata: { creationTime: '2026-10-01T00:00:00Z' } }, cutoff), true);
  assert.equal(unverifiedBefore({ uid: 'new', email: 'new@example.com', emailVerified: false,
    metadata: { creationTime: '2026-10-21T00:00:00Z' } }, cutoff), false);
  assert.equal(unverifiedBefore({ uid: 'verified', email: 'v@example.com', emailVerified: true,
    metadata: { creationTime: '2026-10-01T00:00:00Z' } }, cutoff), false);
  assert.equal(unverifiedBefore({ uid: 'guest', email: 'coach.guest.reader@gmail.com', emailVerified: false,
    metadata: { creationTime: '2026-10-01T00:00:00Z' } }, cutoff), false);
});

const old = {uid:'old', email:'old@test.invalid', emailVerified:false,
  providerData:[{providerId:'password'}], metadata:{creationTime:'2026-10-01'}};
function fixture() {
  const jobs = new Set(), removed = [];
  const adapter = {
    listUsers: async () => ({users:[old]}), getUser: async () => old,
    profile: async () => ({role:'free'}), hasActivity: async () => false,
    enqueue: async uid => jobs.add(uid), complete: async uid => jobs.delete(uid),
    pending: async () => [...jobs], deleteUser: async uid => removed.push(uid), deleteProfile: async () => {},
  };
  return {adapter,jobs,removed};
}
test('protects active, privileged and non-password identities', () => {
  for (const patch of [{disabled:true},{customClaims:{admin:true}}, {providerData:[{providerId:'google.com'}]},
    {metadata:{...old.metadata,lastSignInTime:'2026-10-21'}}, {email:'COACH.GUEST.READER@GMAIL.COM'}])
    assert.equal(unverifiedBefore({...old,...patch},cutoff),false);
  for (const profile of [{role:'admin'},{secondaryRole:'moderador'},{blueEssence:1}, {premiumUntil:1},
    {premiumUntil:'invalid'},{subscriptionPlan:'MONTHLY'},{protected:true},{subscribedCreators:['creator']}])
    assert.equal(protectedProfile(profile),true);
  assert.equal(protectedProfile({role:'free'}),false);
});
test('dry run never enqueues or deletes', async () => {
  const f=fixture(), result=await cleanupUnverified({adapter:f.adapter,now});
  assert.equal(result.candidates,1); assert.equal(f.jobs.size,0); assert.deepEqual(f.removed,[]);
});
test('verification during scan prevents deletion', async () => {
  const f=fixture(); f.adapter.getUser=async()=>({...old,emailVerified:true});
  const result=await cleanupUnverified({adapter:f.adapter,now,apply:true});
  assert.equal(result.skipped,1); assert.deepEqual(f.removed,[]); assert.equal(f.jobs.size,0);
});
test('explicit protection, history and newly granted benefits prevent deletion', async () => {
  for(const scenario of ['uid','history','benefit']) {
    const f=fixture(); let reads=0;
    if(scenario==='history')f.adapter.hasActivity=async()=>true;
    if(scenario==='benefit')f.adapter.profile=async()=>++reads===1?{}:{role:'premium'};
    await cleanupUnverified({adapter:f.adapter,now,apply:true,protectedUids:scenario==='uid'?['old']:[]});
    assert.deepEqual(f.removed,[]);
  }
});
test('profile failure is reported and recovered after identity removal', async () => {
  const f=fixture(); f.adapter.deleteProfile=async()=>{throw Object.assign(new Error('private detail'),{code:'unavailable'});};
  const failed=await cleanupUnverified({adapter:f.adapter,now,apply:true});
  assert.deepEqual(failed.errors,[{stage:'remove-account',code:'unavailable'}]); assert.equal(f.jobs.size,1);
  f.adapter.getUser=async()=>null; f.adapter.listUsers=async()=>({users:[]}); f.adapter.deleteProfile=async()=>{};
  const retry=await cleanupUnverified({adapter:f.adapter,now,apply:true});
  assert.equal(retry.recovered,1); assert.equal(f.jobs.size,0);
});
test('rejects retention shorter than seven days', async () => {
  await assert.rejects(cleanupUnverified({adapter:fixture().adapter,now,maxAgeDays:0}),/retention/);
});
