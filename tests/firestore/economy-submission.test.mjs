import { readFileSync } from 'node:fs';
import assert from 'node:assert/strict';
import { initializeTestEnvironment, assertSucceeds, assertFails } from '@firebase/rules-unit-testing';
import { doc, setDoc, getDoc, getDocs, collection, query, where, runTransaction, serverTimestamp } from 'firebase/firestore';

const env = await initializeTestEnvironment({ projectId: 'demo-coach-tests', firestore: {
  host: '127.0.0.1', port: 8088, rules: readFileSync(new URL('../../firestore.rules', import.meta.url), 'utf8')
} });
const owner = env.authenticatedContext('owner', { email: 'owner@test.invalid', email_verified: true, admin: true }).firestore();
const other = env.authenticatedContext('other', { email: 'other@test.invalid', email_verified: true }).firestore();
function command(id, action = 'SPONSOR', uid = 'owner') {
  return { userId: uid, operationId: id, payload: { action, id }, status: 'PENDING', schema: 2,
    createdAt: serverTimestamp(), authTime: 0 };
}
async function submit(path, id, action) {
  return runTransaction(owner, async tx => {
    const ref = doc(owner, 'economy_requests', path);
    const existing = await tx.get(ref);
    if (!existing.exists()) tx.set(ref, command(id, action));
    return existing.exists();
  });
}
try {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async ctx => {
    const store = ctx.firestore();
    await setDoc(doc(store, 'users/owner'), { role: 'admin' });
    await setDoc(doc(store, 'users/other'), { role: 'free' });
  });
  assert.equal(await assertSucceeds(submit('owner', 'adjust_first_0001', 'ADJUST')), false);
  console.log('PASS first administrative transaction reads its missing request and creates it');
  assert.equal(await assertSucceeds(submit('owner~sponsor_first_0001', 'sponsor_first_0001')), false);
  assert.equal(await assertSucceeds(submit('owner~sponsor_second_0002', 'sponsor_second_0002')), false);
  assert.equal(await assertSucceeds(submit('owner~sponsor_first_0001', 'sponsor_first_0001')), true);
  assert.equal((await assertSucceeds(getDocs(query(collection(owner, 'economy_requests'), where('userId', '==', 'owner'))))).size, 3);
  console.log('PASS independent advertisements coexist with an adjustment and retries reuse the request');
  assert.equal((await assertSucceeds(getDocs(query(collection(owner, 'economy_requests'), where('payload.action', '==', 'SPONSOR'))))).size, 2);
  await assertFails(getDocs(query(collection(other, 'economy_requests'), where('payload.action', '==', 'SPONSOR'))));
  await assertFails(setDoc(doc(owner, 'economy_requests/owner~retired_redeem'), command('retired_redeem', 'REDEEM')));
  console.log('PASS sponsor reviewers can see queued ads and retired redemptions cannot be submitted');
  await assertFails(getDoc(doc(other, 'economy_requests/owner~missing_0001')));
  await assertFails(getDoc(doc(other, 'economy_requests/owner~sponsor_first_0001')));
  await assertFails(getDocs(collection(other, 'economy_requests')));
  await assertFails(getDocs(query(collection(other, 'economy_requests'), where('userId', '==', 'owner'))));
  console.log('PASS other accounts cannot inspect absent, existing or listed requests');
  await env.withSecurityRulesDisabled(async ctx => {
    await setDoc(doc(ctx.firestore(), 'economy_requests/owner~completed_0001'), { ...command('completed_0001'), status: 'COMPLETED' });
  });
  await assertFails(setDoc(doc(other, 'economy_requests/owner~completed_0001'), command('replacement_0001', 'SPONSOR', 'other')));
  await assertFails(setDoc(doc(other, 'economy_requests/other'), command('forged_adjust_0001', 'ADJUST', 'other')));
  console.log('PASS completed requests cannot be stolen and an unsigned account cannot adjust balances');
} finally { await env.cleanup(); }
