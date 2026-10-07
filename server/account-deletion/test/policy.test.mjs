import test from 'node:test';
import assert from 'node:assert/strict';
import {GRACE_MS, SECURITY_RETENTION_MS, eligible, processRequest} from '../policy.mjs';
const start = Date.UTC(2026, 0, 1);
const initial = () => ({requestId:'12345678-1234-1234-1234-123456789012',userId:'owner',status:'PENDING',requestedAt:start,graceDays:60});
function fixture() {
  const state = {request:initial(),identity:{email:'owner@test.invalid',disabled:false,metadata:{lastSignInTime:new Date(start-1000).toISOString()}},
    ownData:true,otherData:true,calls:[],failure:null};
  const call = name => {state.calls.push(name); if(state.failure===name)throw new Error('temporary failure');};
  const adapter = {
    read:async()=>state.request&&{...state.request},account:async()=>state.identity&&structuredClone(state.identity),
    claim:async(uid,request,user)=>{call('claim'); if(state.request?.requestId!==request.requestId || state.request.status==='CANCELLED')return null;
      if(state.request.status==='PENDING')state.request={...state.request,status:'PROCESSING',accountEmail:user?.email||'',wasDisabled:Boolean(user?.disabled)};
      return {...state.request};},
    disable:async()=>{call('disable');state.identity.disabled=true;},enable:async()=>{call('enable');state.identity.disabled=false;},
    cancel:async()=>{call('cancel');state.request.status='CANCELLED';},
    purge:async()=>{call('purge');state.ownData=false;},markPurged:async()=>{call('purged');state.request.status='DATA_PURGED';},
    deleteIdentity:async()=>{call('identity');state.identity=null;},complete:async(uid,id,now)=>{call('complete');state.request={...state.request,status:'COMPLETED',completedAt:now};delete state.request.accountEmail;},
    remove:async()=>{call('remove');state.request=null;}
  }; return {state,adapter};
}
test('the deadline is exactly 60 elapsed days and no deletion happens earlier',async()=>{
  const {state,adapter}=fixture(); assert.equal(eligible(state.request,start+GRACE_MS-1),false);
  assert.equal(await processRequest('owner',adapter,start+GRACE_MS-1),'skipped');assert.deepEqual(state.calls,[]);
  assert.equal(await processRequest('owner',adapter,start+GRACE_MS),'deleted'); assert.equal(state.ownData,false);assert.equal(state.identity,null);assert.equal(state.otherData,true);
  assert.ok(state.calls.indexOf('purge')<state.calls.indexOf('identity'));
});
test('an explicit return within recovery cancels even when processing runs much later',async()=>{
  const {state,adapter}=fixture();state.identity.metadata.lastSignInTime=new Date(start+GRACE_MS-1).toISOString();
  assert.equal(await processRequest('owner',adapter,start+GRACE_MS+100000),'cancelled'); assert.equal(state.ownData,true); assert.equal(state.identity.disabled,false);
});
test('the original sign-in and a sign-in at the deadline do not extend the recovery window',async()=>{
  for(const login of [start,start+GRACE_MS]){const {state,adapter}=fixture();state.identity.metadata.lastSignInTime=new Date(login).toISOString();
    assert.equal(await processRequest('owner',adapter,start+GRACE_MS+1),'deleted');}
});
test('a cancellation or replacement racing the claim cannot erase account data',async()=>{
  const {state,adapter}=fixture();const claim=adapter.claim;adapter.claim=async(...args)=>{state.request.status='CANCELLED';return claim(...args);};
  assert.equal(await processRequest('owner',adapter,start+GRACE_MS),'skipped');assert.equal(state.ownData,true);assert.equal(state.identity.disabled,false);
});
test('a sign-in racing the lock is rechecked after identity disabling',async()=>{
  const {state,adapter}=fixture();const disable=adapter.disable;adapter.disable=async()=>{await disable();state.identity.metadata.lastSignInTime=new Date(start+1000).toISOString();};
  assert.equal(await processRequest('owner',adapter,start+GRACE_MS),'cancelled');assert.equal(state.ownData,true);assert.equal(state.identity.disabled,false);
});
test('cancellation preserves an identity that was already disabled before the request',async()=>{
  const {state,adapter}=fixture();state.identity.disabled=true;const disable=adapter.disable;
  adapter.disable=async()=>{await disable();state.identity.metadata.lastSignInTime=new Date(start+1000).toISOString();};
  assert.equal(await processRequest('owner',adapter,start+GRACE_MS),'cancelled');assert.equal(state.identity.disabled,true);assert.ok(!state.calls.includes('enable'));
});
test('a purge failure cannot report completion or delete credentials; retry completes',async()=>{
  const {state,adapter}=fixture();state.failure='purge';await assert.rejects(processRequest('owner',adapter,start+GRACE_MS));
  assert.equal(state.request.status,'PROCESSING');assert.ok(state.identity);assert.ok(!state.calls.includes('identity'));
  state.failure=null;assert.equal(await processRequest('owner',adapter,start+GRACE_MS+1000),'deleted');
});
test('a credential deletion failure resumes after the persisted data checkpoint',async()=>{
  const {state,adapter}=fixture();state.failure='identity';await assert.rejects(processRequest('owner',adapter,start+GRACE_MS));
  assert.equal(state.request.status,'DATA_PURGED');state.failure=null;
  assert.equal(await processRequest('owner',adapter,start+GRACE_MS+1000),'deleted');assert.equal(state.calls.filter(n=>n==='purge').length,1);
});
test('an already removed identity is cleaned idempotently without deleting a new identity',async()=>{
  const {state,adapter}=fixture();state.identity=null;assert.equal(await processRequest('owner',adapter,start+GRACE_MS),'deleted');
  assert.ok(!state.calls.includes('identity'));assert.equal(state.otherData,true);
});
test('the minimal security record expires after 24 hours and includes no email',async()=>{
  const {state,adapter}=fixture();await processRequest('owner',adapter,start+GRACE_MS);assert.ok(!('accountEmail' in state.request));
  await processRequest('owner',adapter,start+GRACE_MS+SECURITY_RETENTION_MS-1);assert.ok(state.request);
  await processRequest('owner',adapter,start+GRACE_MS+SECURITY_RETENTION_MS);assert.equal(state.request,null);
});
test('malformed or shortened recovery requests cannot trigger deletion',async()=>{
  for(const change of [{graceDays:0},{requestedAt:NaN},{requestId:'short'}]) {const {state,adapter}=fixture();Object.assign(state.request,change);
    assert.equal(await processRequest('owner',adapter,start+GRACE_MS),'skipped');assert.deepEqual(state.calls,[]);}
});
test('server timestamp fractions and a return in the same second are preserved',async()=>{
  const {state,adapter}=fixture();state.request.requestedAt=start+0.123;
  state.identity.lastSignInTimeMillis=start+0.5;
  assert.equal(await processRequest('owner',adapter,start+GRACE_MS+1),'cancelled');
  assert.equal(state.ownData,true);
});
