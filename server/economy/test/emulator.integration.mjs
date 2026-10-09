import {initializeApp, deleteApp} from 'firebase-admin/app';
import {getFirestore, Timestamp} from 'firebase-admin/firestore';
import assert from 'node:assert/strict';
import {getAuth} from 'firebase-admin/auth';
import {processQueue} from '../queue.mjs';
import {executeEconomy} from '../service.mjs';
import {maintainEconomy} from '../maintenance.mjs';
if (!/^127\.0\.0\.1:\d+$/.test(process.env.FIRESTORE_EMULATOR_HOST ?? '')) throw new Error('Isolated emulator required; production writes prohibited.');
const app = initializeApp({projectId: 'demo-coach-tests'}, 'economy-tests'), db = getFirestore(app);
const auth = (uid, admin = false) => ({uid, token: {admin, email: `${uid}@test.invalid`, firebase: {sign_in_provider: 'password'}}});
const owner = auth('economy-server'), admin = auth('economy-admin', true), other = auth('economy-other');
const base = {role: 'creador', secondaryRole: 'streamer', blueEssence: 2500, orangeEssence: 300, premiumUntil: Timestamp.fromMillis(Date.now() + 90 * 86400000)};
let passed = 0;
async function test(name, fn) { await fn(); console.log(`PASS server economy: ${name}`); passed++; }
const call = (action, id, fields = {}, caller = action === 'REDEEM' ? auth(owner.uid, true) : owner) => executeEconomy(db, caller, {action, id, ...fields});
try {
  for (const uid of [owner.uid, admin.uid, other.uid]) await db.doc(`users/${uid}`).set({...base, role: uid === admin.uid ? 'admin' : 'creador'});
  await test('all four Premium prices, inherited deadline and roles', async () => {
    for (const [plan, currency, price, days] of [['MONTHLY', 'BLUE', 100, 30], ['MONTHLY', 'ORANGE', 9, 30], ['ANNUAL', 'BLUE', 1100, 365], ['ANNUAL', 'ORANGE', 95, 365]]) {
      const ref = db.doc(`users/${owner.uid}`), before = (await ref.get()).data(), field = currency === 'BLUE' ? 'blueEssence' : 'orangeEssence';
      await call('PURCHASE', `premium_${plan}_${currency}`, {plan, currency}); const after = (await ref.get()).data();
      const old = before.premiumUntil.toMillis?.() ?? before.premiumUntil;
      assert.equal(after.premiumUntil, old + days * 86400000); assert.equal(after[field], before[field] - price);
      assert.equal(after.role, before.role); assert.equal(after.secondaryRole, before.secondaryRole);
    }
  });
  await test('retry is idempotent and a changed payload cannot reuse its identifier', async () => {
    const ref = db.doc(`users/${owner.uid}`), before = (await ref.get()).data();
    await call('PURCHASE', 'premium_MONTHLY_BLUE', {plan: 'MONTHLY', currency: 'BLUE'});
    assert.deepEqual((await ref.get()).data(), before);
    await assert.rejects(call('PURCHASE', 'premium_MONTHLY_BLUE', {plan: 'ANNUAL', currency: 'BLUE'}), /identificador/);
  });
  await test('two concurrent purchases cannot overdraw', async () => {
    await db.doc('users/economy-poor').set({role: 'free', orangeEssence: 10});
    const outcomes = await Promise.allSettled(['one', 'two'].map(id => call('PURCHASE', `concurrent_${id}`, {plan: 'MONTHLY', currency: 'ORANGE'}, auth('economy-poor'))));
    assert.equal(outcomes.filter(r => r.status === 'fulfilled').length, 1); assert.equal((await db.doc('users/economy-poor').get()).get('orangeEssence'), 1);
  });
  const destination = {amount: 10, network: 'ERC20', wallet: '0x1111111111111111111111111111111111111111', binanceEmail: '', expectedFee: 5};
  await test('redemption saves the debit, receipt and private conversation together', async () => {
    const before = (await db.doc(`users/${owner.uid}`).get()).get('orangeEssence');
    await call('REDEEM', 'cash_server_ten', destination); await call('REDEEM', 'cash_server_ten', destination);
    assert.equal((await db.doc(`users/${owner.uid}`).get()).get('orangeEssence'), before - 15);
    assert.equal((await db.doc('cash_redemptions/cash_server_ten').get()).get('status'), 'PENDING');
    assert.equal((await db.doc('support_reports/payment_cash_server_ten').get()).get('staffVisible'), false);
  });
  await test('forged admin fields cannot resolve or credit balances', async () => {
    await db.doc(`users/${other.uid}`).update({admin: true, role: 'admin', rankBorder: 'ADMIN'});
    await assert.rejects(call('RESOLVE', 'fake_resolution', {redemptionId: 'cash_server_ten', paid: false}, other), /permisos/);
    await assert.rejects(call('ADJUST', 'fake_adjustment', {uid: owner.uid, amount: 100, currency: 'ORANGE', addition: true, notify: false}, other), /permisos/);
    await assert.rejects(executeEconomy(db, null, {action: 'CLEANUP', id: 'cleanup_guest'}), /sesión/);
  });
  await test('concurrent rejection refunds exactly once and cannot become paid afterwards', async () => {
    const ref = db.doc(`users/${owner.uid}`), before = (await ref.get()).get('orangeEssence');
    await Promise.all(['one', 'two'].map(id => call('RESOLVE', `resolution_${id}`, {redemptionId: 'cash_server_ten', paid: false}, admin)));
    assert.equal((await ref.get()).get('orangeEssence'), before + 15);
    await assert.rejects(call('RESOLVE', 'resolution_paid', {redemptionId: 'cash_server_ten', paid: true}, admin), /pendiente/);
    assert.equal((await db.doc(`users/${owner.uid}/subscription_history/cash_server_ten`).get()).get('status'), 'Rechazado y reembolsado');
  });
  await test('only a signed administrator can redeem; profile roles and flags never debit', async () => {
    const ref=db.doc(`users/${other.uid}`), before=(await ref.get()).data();
    for (const role of ['admin','moderador','streamer','creador','creador_lvl2','creador_lvl3','creador_lvl4','creador_lvl5','premium','free']) {
      await ref.update({role,admin:true,secondaryRole:'admin'});
      await assert.rejects(call('REDEEM', `denied_cash_${role}`, destination, other), /permisos/);
      assert.equal((await ref.get()).get('orangeEssence'),before.orangeEssence);
    }
  });
  await test('a delayed legacy role command cannot overwrite a newer direct assignment', async()=>{
    const ref=db.doc(`users/${other.uid}`);
    await ref.update({role:'free',last_role_update:Timestamp.fromMillis(Date.now())});
    await assert.rejects(executeEconomy(db,admin,{action:'ROLE',id:'stale_role_226',uid:other.uid,role:'creador'},Date.now,{requestedAtMillis:Date.now()-60000}), /reciente/);
    assert.equal((await ref.get()).get('role'),'free');
  });
  await test('server-owned fees reject a stale quote without debiting', async () => {
    await db.doc('app_config/binance_fees').set({ERC20: 7});
    const before = (await db.doc(`users/${owner.uid}`).get()).get('orangeEssence');
    await assert.rejects(call('REDEEM', 'cash_stale_quote', destination), /comisión/);
    assert.equal((await db.doc(`users/${owner.uid}`).get()).get('orangeEssence'), before);
    await db.doc('app_config/binance_fees').delete();
  });
  await test('admin adjustment records the actual clipped debit and a notification', async () => {
    await call('ADJUST', 'adjust_server_credit', {uid: 'economy-poor', amount: 25, currency: 'ORANGE', addition: true, notify: true, title: 'Ajuste', customBody: ''}, admin);
    await call('ADJUST', 'adjust_server_debit', {uid: 'economy-poor', amount: 100, currency: 'ORANGE', addition: false, notify: false, title: '', customBody: ''}, admin);
    assert.equal((await db.doc('users/economy-poor').get()).get('orangeEssence'), 0);
    assert.equal((await db.doc('users/economy-poor/subscription_history/adjust_server_debit').get()).get('amount'), '-26 EN');
  });
  await test('Premium grants preserve both roles and can be revoked only by a claim', async () => {
    const before = (await db.doc(`users/${owner.uid}`).get()).data();
    await call('PREMIUM_GRANT', 'premium_server_gift', {uid: owner.uid, days: 7, extend: true}, admin);
    const after = (await db.doc(`users/${owner.uid}`).get()).data(); assert.equal(after.premiumUntil, before.premiumUntil + 7 * 86400000);
    assert.equal(after.role, before.role); assert.equal(after.secondaryRole, before.secondaryRole);
    await assert.rejects(call('PREMIUM_REMOVE', 'premium_self_remove', {uid: owner.uid}), /permisos/);
  });
  await test('removing Premium resets premium and moderator to free while preserving other roles', async () => {
    const expires = Timestamp.fromMillis(Date.now() + 30 * 86400000);
    await assert.rejects(call('PREMIUM_REMOVE', 'premium_remove_admin', {uid: admin.uid}, admin), /vitalicio/);
    assert.equal((await db.doc(`users/${admin.uid}`).get()).get('role'), 'admin');
    await db.doc('users/premium-removal-premium').set({...base, role: 'premium', secondaryRole: '', premiumUntil: expires});
    await call('PREMIUM_REMOVE', 'premium_remove_primary', {uid: 'premium-removal-premium'}, admin);
    let account = (await db.doc('users/premium-removal-premium').get()).data();
    assert.equal(account.role, 'free'); assert.equal(account.premiumUntil, 0); assert.equal(account.subscriptionPlan, 'FREE');
    await db.doc('users/premium-removal-moderator').set({...base, role: 'moderador', secondaryRole: 'moderador', premiumUntil: expires});
    await call('PREMIUM_REMOVE', 'premium_remove_moderator', {uid: 'premium-removal-moderator'}, admin);
    account = (await db.doc('users/premium-removal-moderator').get()).data();
    assert.equal(account.role, 'free'); assert.equal(account.secondaryRole, ''); assert.equal(account.premiumUntil, 0);
    await db.doc('users/premium-removal-creator').set({...base, role: 'creador', secondaryRole: 'streamer', premiumUntil: expires});
    await call('PREMIUM_REMOVE', 'premium_remove_creator', {uid: 'premium-removal-creator'}, admin);
    account = (await db.doc('users/premium-removal-creator').get()).data();
    assert.equal(account.role, 'creador'); assert.equal(account.secondaryRole, 'streamer'); assert.equal(account.premiumUntil, 0);
    await db.doc('users/premium-removal-secondary-moderator').set({...base, role: 'creador', secondaryRole: 'moderador', premiumUntil: expires});
    await call('PREMIUM_REMOVE', 'premium_remove_secondary_moderator', {uid: 'premium-removal-secondary-moderator'}, admin);
    account = (await db.doc('users/premium-removal-secondary-moderator').get()).data();
    assert.equal(account.role, 'creador'); assert.equal(account.secondaryRole, ''); assert.equal(account.premiumUntil, 0);
  });
  await test('deletion and suspension block financial operations without recreating accounts', async () => {
    await db.doc(`account_deletions/${owner.uid}`).set({status: 'PENDING'});
    await assert.rejects(call('REDEEM', 'cash_deleted_user', destination), /eliminación/);
    await db.doc(`account_deletions/${owner.uid}`).delete(); await db.doc(`users/${owner.uid}`).update({banned: true});
    await assert.rejects(call('REDEEM', 'cash_banned_user', destination), /suspendida/);
    await call('ROLE', 'role_unban_user', {uid: owner.uid, role: 'creador'}, admin);
    assert.equal((await db.doc(`users/${owner.uid}`).get()).get('banned'), false);
  });
  await test('creator payment credits once and no membership is cached before a charge', async () => {
    await db.doc('users/economy-creator').set({role: 'creador', orangeEssence: 0});
    const before = (await db.doc(`users/${owner.uid}`).get()).get('orangeEssence');
    await Promise.all(['one', 'two'].map(id => call('SUBSCRIBE', `subscribe_${id}`, {creatorUid: 'economy-creator'})));
    assert.equal((await db.doc(`users/${owner.uid}`).get()).get('orangeEssence'), before - 5);
    assert.equal((await db.doc('users/economy-creator').get()).get('orangeEssence'), 2);
    await call('UNSUBSCRIBE', 'unsubscribe_creator', {creatorUid: 'economy-creator'});
    assert.equal((await db.doc('users/economy-creator').get()).get('creatorSubscriberCount'), 0);
  });
  await test('sponsor submission has one server-priced debit and rejects invented prices', async () => {
    await db.doc(`users/${owner.uid}`).update({secondaryRole: 'patrocinador'});
    const notice = {title: 'Anuncio', content: 'Contenido', videoUrl: '', expandedImageUrl: '', externalUrl: '', durationValue: 1, durationUnit: 'day'};
    await assert.rejects(call('SPONSOR', 'sponsor_zero_price', {notice, expectedCost: 0}), /costo/);
    await call('SPONSOR', 'sponsor_valid_price', {notice, expectedCost: 27});
    assert.equal((await db.doc('pending_sponsor_ads/sponsor_valid_price').get()).get('chargedEssence'), 27);
  });
  await test('an unaccepted sponsor receives exactly one refund after seven server days', async () => {
    const now = Date.now(), profile = db.doc(`users/${owner.uid}`), before = (await profile.get()).get('blueEssence');
    await db.doc('pending_sponsor_ads/sponsor_valid_price').update({createdAt: now - 7 * 86400000 - 1000});
    await db.doc('pending_sponsor_ads/legacy_untrusted_budget').set({userId: owner.uid, isApproved: false, createdAt: now - 8 * 86400000, chargedEssence: 100000});
    await Promise.all([maintainEconomy(db, now), maintainEconomy(db, now)]);
    assert.equal((await profile.get()).get('blueEssence'), before + 27);
    assert.equal((await db.doc('pending_sponsor_ads/sponsor_valid_price').get()).get('status'), 'REFUNDED');
    assert.equal((await db.doc(`users/${owner.uid}/subscription_history/sponsor_refund_sponsor_valid_price`).get()).get('amount'), '+27 EA');
    await maintainEconomy(db, now + 1); assert.equal((await profile.get()).get('blueEssence'), before + 27);
  });
  await test('retention removes expired resolved requests and preserves pending money', async () => {
    const now = Date.now();
    await db.doc('cash_redemptions/cash_server_ten').update({historyDeleteAtMillis: now - 1});
    await db.doc('cash_redemptions/pending_retention_guard').set({userId: owner.uid, status: 'PENDING', historyDeleteAtMillis: now - 1});
    await maintainEconomy(db, now);
    assert.equal((await db.doc('cash_redemptions/cash_server_ten').get()).exists, false);
    assert.equal((await db.doc('cash_redemptions/pending_retention_guard').get()).exists, true);
  });
  const identities = getAuth(app), queueUid='economy-queue-user';
  await identities.createUser({uid:queueUid,email:'queue@test.invalid',password:'Isolated-test-password-123'});
  await db.doc(`users/${queueUid}`).set({role:'free',blueEssence:500,orangeEssence:100});
  const queueRef=db.doc(`economy_requests/${queueUid}`);
  const queued=(id,fields={})=>({userId:queueUid,operationId:id,authTime:Math.floor(Date.now()/1000),status:'PENDING',schema:2,
    createdAt:Timestamp.now(),payload:{action:'PURCHASE',id,plan:'MONTHLY',currency:'BLUE',...fields}});
  await test('concurrent private workers execute one charge and preserve a private result',async()=>{
    await queueRef.set(queued('queue_concurrent_01'));
    await Promise.all([processQueue(db,identities),processQueue(db,identities)]);
    assert.equal((await db.doc(`users/${queueUid}`).get()).get('blueEssence'),400);
    assert.equal((await queueRef.get()).get('status'),'COMPLETED');
    assert.equal((await db.doc(`economy_results/${queueUid}~queue_concurrent_01`).get()).get('result.ok'),true);
  });
  await test('an interrupted worker recovers an expired lease without charging the committed operation again',async()=>{
    const request=queued('queue_lost_commit_01');
    await executeEconomy(db,auth(queueUid),request.payload);
    await queueRef.set({...request,status:'PROCESSING',lease:'lost-worker',leaseUntil:Date.now()-1});
    await processQueue(db,identities);
    assert.equal((await db.doc(`users/${queueUid}`).get()).get('blueEssence'),300);
    assert.equal((await queueRef.get()).get('status'),'COMPLETED');
  });
  await test('transient failures recover the same request without a second charge',async()=>{
    const before=(await db.doc(`users/${queueUid}`).get()).get('blueEssence');
    await queueRef.set(queued('queue_transient_01'));
    let now=Date.now();
    await processQueue(db,{getUser:async()=>{throw new Error('temporary service error');}},{clock:()=>now});
    assert.equal((await queueRef.get()).get('status'),'PROCESSING');
    now=(await queueRef.get()).get('leaseUntil')+1;
    await processQueue(db,identities,{clock:()=>now});
    assert.equal((await queueRef.get()).get('status'),'COMPLETED');
    assert.equal((await db.doc(`users/${queueUid}`).get()).get('blueEssence'),before-100);
  });
  await test('repeated unconfirmed failures require review without opening another charge',async()=>{
    const before=(await db.doc(`users/${queueUid}`).get()).get('blueEssence');
    await queueRef.set(queued('queue_review_01'));
    let now=Date.now();
    const unavailable={getUser:async()=>{throw new Error('unconfirmed response');}};
    for(let attempt=1;attempt<=8;attempt++) {
      await processQueue(db,unavailable,{clock:()=>now});
      const current=(await queueRef.get()).data();
      assert.equal(current.attempts,attempt);
      now=(current.leaseUntil ?? now)+1;
    }
    assert.equal((await queueRef.get()).get('status'),'REVIEW');
    await processQueue(db,identities,{clock:()=>now+86400000});
    assert.equal((await queueRef.get()).get('status'),'REVIEW');
    assert.equal((await db.doc(`users/${queueUid}`).get()).get('blueEssence'),before);
    await processQueue(db,identities,{clock:()=>now+86400000,manualReview:true});
    assert.equal((await queueRef.get()).get('status'),'COMPLETED');
    assert.equal((await db.doc(`users/${queueUid}`).get()).get('blueEssence'),before-100);
  });
  await test('manual review recognizes an earlier committed debit without charging twice',async()=>{
    const request=queued('queue_review_committed_01');
    const before=(await db.doc(`users/${queueUid}`).get()).get('blueEssence');
    await executeEconomy(db,auth(queueUid),request.payload);
    await queueRef.set({...request,status:'REVIEW',attempts:8,reviewReason:'UNCONFIRMED'});
    await processQueue(db,identities,{manualReview:true});
    assert.equal((await queueRef.get()).get('status'),'COMPLETED');
    assert.equal((await db.doc(`users/${queueUid}`).get()).get('blueEssence'),before-100);
  });
  await test('a queued document cannot grant admin authority; current Auth claims govern execution',async()=>{
    const before=(await db.doc(`users/${queueUid}`).get()).get('blueEssence');
    await db.doc(`users/${queueUid}`).update({role:'admin',admin:true});
    await queueRef.set(queued('queue_forged_admin_01',{action:'ADJUST',uid:queueUid,amount:500,currency:'BLUE',addition:true,notify:false}));
    await processQueue(db,identities);
    assert.equal((await db.doc(`users/${queueUid}`).get()).get('blueEssence'),before);
    assert.equal((await queueRef.get()).get('status'),'FAILED');
    await identities.setCustomUserClaims(queueUid,{admin:true});
    const request=queued('queue_revoked_admin_01',{action:'ADJUST',uid:queueUid,amount:500,currency:'BLUE',addition:true,notify:false});
    delete request.payload.plan;
    await queueRef.set(request);await identities.setCustomUserClaims(queueUid,{});
    await processQueue(db,identities);
    assert.equal((await db.doc(`users/${queueUid}`).get()).get('blueEssence'),before);
    assert.equal((await queueRef.get()).get('error.code'),'permission-denied');
  });
  await test('disabled accounts and revoked sessions cannot execute queued payments',async()=>{
    const before=(await db.doc(`users/${queueUid}`).get()).get('blueEssence');
    await identities.updateUser(queueUid,{disabled:true});
    await queueRef.set(queued('queue_disabled_01'));await processQueue(db,identities);
    assert.equal((await queueRef.get()).get('error.code'),'unauthenticated');
    await identities.updateUser(queueUid,{disabled:false});
    await queueRef.set(queued('queue_revoked_session_01'));
    const revoked={getUser:async uid=>({...await identities.getUser(uid),tokensValidAfterTime:new Date(Date.now()+60000).toUTCString()})};
    await processQueue(db,revoked);
    assert.equal((await queueRef.get()).get('error.code'),'unauthenticated');
    assert.equal((await db.doc(`users/${queueUid}`).get()).get('blueEssence'),before);
  });
  await test('temporary results expire without deleting the durable receipt or pending request',async()=>{
    const result=db.doc(`economy_results/${queueUid}~queue_concurrent_01`);
    await result.update({expiresAtMillis:Date.now()-1});
    await queueRef.set(queued('queue_retention_guard_01'));
    await maintainEconomy(db);
    assert.equal((await result.get()).exists,false);
    assert.equal((await db.doc(`users/${queueUid}/subscription_history/queue_concurrent_01`).get()).exists,true);
    assert.equal((await queueRef.get()).get('status'),'PENDING');
    await queueRef.delete();
  });
  await identities.deleteUser(queueUid);
  console.log(`PASS ${passed} server economy integration cases`);
} finally { await deleteApp(app); }
