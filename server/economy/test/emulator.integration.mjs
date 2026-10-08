import {initializeApp, deleteApp} from 'firebase-admin/app';
import {getFirestore, Timestamp} from 'firebase-admin/firestore';
import assert from 'node:assert/strict';
import {executeEconomy} from '../service.mjs';
if (!/^127\.0\.0\.1:\d+$/.test(process.env.FIRESTORE_EMULATOR_HOST ?? '')) throw new Error('Isolated emulator required; production writes prohibited.');
const app = initializeApp({projectId: 'demo-coach-tests'}, 'economy-tests'), db = getFirestore(app);
const auth = (uid, admin = false) => ({uid, token: {admin, email: `${uid}@test.invalid`, firebase: {sign_in_provider: 'password'}}});
const owner = auth('economy-server'), admin = auth('economy-admin', true), other = auth('economy-other');
const base = {role: 'creador', secondaryRole: 'streamer', blueEssence: 2500, orangeEssence: 300, premiumUntil: Timestamp.fromMillis(Date.now() + 90 * 86400000)};
let passed = 0;
async function test(name, fn) { await fn(); console.log(`PASS server economy: ${name}`); passed++; }
const call = (action, id, fields = {}, caller = owner) => executeEconomy(db, caller, {action, id, ...fields});
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
  console.log(`PASS ${passed} server economy integration cases`);
} finally { await deleteApp(app); }
