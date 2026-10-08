import {test} from 'node:test';
import assert from 'node:assert/strict';
import {allowedAccount, cashRedemption, premiumPurchase, validWallet, validateInput, integer, sponsorPrice, fingerprint, DAY} from '../policy.mjs';
const account = {role: 'creador', blueEssence: 2000, orangeEssence: 200, premiumUntil: 9000000000000};
for (const [plan, currency, days, cost] of [['MONTHLY', 'BLUE', 30, 100], ['MONTHLY', 'ORANGE', 30, 9], ['ANNUAL', 'BLUE', 365, 1100], ['ANNUAL', 'ORANGE', 365, 95]]) {
  test(`${plan} ${currency} inherits existing time and uses the fixed price`, () => {
    const result = premiumPurchase(account, plan, currency, 1000);
    assert.equal(result.until, account.premiumUntil + days * DAY); assert.equal(result.cost, cost);
  });
}
test('expired access starts at server time and cannot overdraw', () => {
  assert.equal(premiumPurchase({...account, premiumUntil: 0}, 'MONTHLY', 'ORANGE', 2000).until, 2000 + 30 * DAY);
  assert.throws(() => premiumPurchase({...account, orangeEssence: 8}, 'MONTHLY', 'ORANGE', 2000), /insuficientes/);
});
for (const role of ['admin', 'administrador', 'moderador']) test(`lifetime ${role} cannot buy redundant Premium`, () => assert.throws(() => premiumPurchase({...account, role}, 'MONTHLY', 'ORANGE', 2000), /vitalicio/));
for (const invalid of [-1, NaN, Infinity, '5', 1.5, Number.MAX_SAFE_INTEGER + 1]) test(`reject invalid quantity ${invalid}`, () => assert.throws(() => integer(invalid)));
test('wallet validation rejects contracts, zero addresses and invalid Tron checksums', () => {
  assert.equal(validWallet('ERC20', '0x1111111111111111111111111111111111111111'), true);
  assert.equal(validWallet('BEP20', '0x0000000000000000000000000000000000000000'), false);
  assert.equal(validWallet('TRC20', 'TLa2f6VPqDgRE67v1736s7bJ8Ray5wYjU7'), true);
  assert.equal(validWallet('TRC20', 'TLa2f6VPqDgRE67v1736s7bJ8Ray5wYjU8'), false);
  assert.equal(validWallet('ERC20', '0xdac17f958d2ee523a2206206994597c13d831ec7'), false);
});
for (const [network, expectedFee] of [['BEP20', 1], ['ERC20', 5], ['TRC20', 2]]) test(`${network} debits the server fee and rejects stale quotes`, () => {
  const wallet = network === 'TRC20' ? 'TLa2f6VPqDgRE67v1736s7bJ8Ray5wYjU7' : '0x1111111111111111111111111111111111111111';
  const input = {amount: 10, network, wallet, expectedFee};
  assert.equal(cashRedemption(account, input, true).remaining, 190 - expectedFee);
  assert.throws(() => cashRedemption(account, {...input, expectedFee: 0}, true), /comisión/);
});
test('Binance email requires an exclusive destination and has no network fee', () => {
  const input = {amount: 25, binanceEmail: 'Recipient@example.com', expectedFee: 0};
  assert.equal(cashRedemption(account, input, true).totalCost, 25);
  assert.throws(() => cashRedemption(account, {...input, network: 'ERC20'}, true), /Destino/);
  assert.throws(() => cashRedemption(account, {...input, binanceEmail: 'invalid'}, true), /Destino/);
});
test('free accounts cannot redeem and an admin document flag is not a claim', () => assert.throws(() => cashRedemption({...account, role: 'free', admin: true}, {amount: 10}), /administrador/));
for (const status of ['PENDING', 'PROCESSING', 'DATA_PURGED', 'COMPLETED']) test(`deletion ${status} prevents a new financial operation`, () => assert.throws(() => allowedAccount(account, {status})));
test('banned accounts cannot transact through an inherited role', () => assert.throws(() => allowedAccount({...account, secondaryRole: 'banned'})));
test('payload rejects price, actor and arbitrary document paths', () => {
  for (const forged of [{cost: 0}, {uid: 'other'}, {actorUid: 'admin'}]) assert.throws(() => validateInput({action: 'PURCHASE', id: 'purchase_1234', plan: 'MONTHLY', currency: 'BLUE', ...forged}));
  assert.throws(() => validateInput({action: 'ADJUST', id: 'adjust_1234', uid: '../admin'}));
});
test('sponsor pricing uses server publication count and verified fields', () => {
  const notice = {title: 'Anuncio', content: 'Contenido', videoUrl: '', expandedImageUrl: '', externalUrl: '', durationValue: 1, durationUnit: 'day'};
  assert.deepEqual(sponsorPrice(notice, 1), {budget: 2.5, cost: 25});
  assert.deepEqual(sponsorPrice(notice, 0), {budget: 2.78, cost: 27});
  assert.throws(() => sponsorPrice({...notice, durationValue: 100}, 1));
  assert.throws(() => sponsorPrice({...notice, externalUrl: 'javascript:alert(1)'}, 1));
});

test('retry fingerprints ignore JSON object order without ignoring altered quantities', () => {
  assert.equal(fingerprint({action: 'SPONSOR', notice: {title: 'A', content: 'B'}}), fingerprint({notice: {content: 'B', title: 'A'}, action: 'SPONSOR'}));
  assert.notEqual(fingerprint({amount: 10}), fingerprint({amount: 25}));
});

for (const role of ['admin','administrador','moderador','streamer','creador','creador_lvl2','creador_lvl3','creador_lvl4','creador_lvl5','free','premium','patrocinador']) {
  test(`redemption rejects ${role} without a signed claim`,()=>assert.throws(()=>cashRedemption({...account,role,secondaryRole:'admin'}, {amount:10}), /administrador/));
}
