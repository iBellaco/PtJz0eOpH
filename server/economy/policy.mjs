import {createHash} from 'node:crypto';

export class EconomyError extends Error {
  constructor(code, message) { super(message); this.code = code; }
}
export function ensure(ok, code, message) { if (!ok) throw new EconomyError(code, message); }
export const DAY = 86400000;
export const FEES = {BEP20: 1, TRC20: 2, ERC20: 5};
const plans = {MONTHLY: {days: 30, BLUE: 100, ORANGE: 9}, ANNUAL: {days: 365, BLUE: 1100, ORANGE: 95}};
const contracts = new Set(['txlaq63xg1nazckpwkhvzw7csemlmeqcdj', '0xdac17f958d2ee523a2206206994597c13d831ec7', '0x55d398326f99059ff775485246999027b3197955']);
export function integer(value, min = 0, max = Number.MAX_SAFE_INTEGER) {
  ensure(Number.isSafeInteger(value) && value >= min && value <= max, 'invalid-argument', 'Cantidad no válida'); return value;
}
export function deadline(value) {
  if (typeof value?.toMillis === 'function') return integer(value.toMillis());
  if (value == null) return 0;
  if (typeof value === 'string') value = /^\d+$/.test(value) ? Number(value) : Date.parse(value);
  return integer(value);
}
export function allowedAccount(account, deletion) {
  ensure(account && !account.banned && ![account.role, account.secondaryRole].includes('banned'), 'permission-denied', 'Cuenta suspendida');
  ensure(!['PENDING', 'PROCESSING', 'DATA_PURGED', 'COMPLETED'].includes(deletion?.status), 'failed-precondition', 'La cuenta tiene una solicitud de eliminación');
}
export function balance(account, currency) {
  ensure(['BLUE', 'ORANGE'].includes(currency), 'invalid-argument', 'Moneda no válida');
  return integer(account[currency === 'BLUE' ? 'blueEssence' : 'orangeEssence'] ?? 0);
}
export function premiumPurchase(account, plan, currency, now, admin = false) {
  const price = plans[plan]; ensure(price && ['BLUE', 'ORANGE'].includes(currency), 'invalid-argument', 'Plan no válido');
  ensure(!admin && ![account.role, account.secondaryRole].some(r => ['admin', 'administrador', 'moderador'].includes(r)), 'failed-precondition', 'Tu acceso premium es vitalicio');
  const remaining = balance(account, currency) - price[currency]; ensure(remaining >= 0, 'failed-precondition', 'Esencias insuficientes');
  return {remaining, cost: price[currency], days: price.days, until: integer(Math.max(deadline(account.premiumUntil), now) + price.days * DAY)};
}
export function validWallet(network, wallet) {
  if (typeof wallet !== 'string' || wallet !== wallet.trim() || contracts.has(wallet.toLowerCase())) return false;
  if (network === 'BEP20' || network === 'ERC20') return /^0x[0-9a-fA-F]{40}$/.test(wallet) && !/^0x0{40}$/.test(wallet);
  if (network !== 'TRC20' || !/^T[1-9A-HJ-NP-Za-km-z]{33}$/.test(wallet)) return false;
  const alphabet = '123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz';
  let number = 0n; for (const char of wallet) number = number * 58n + BigInt(alphabet.indexOf(char));
  let hex = number.toString(16); if (hex.length % 2) hex = '0' + hex;
  const bytes = Buffer.from(hex, 'hex');
  return bytes.length === 25 && bytes[0] === 0x41 && createHash('sha256').update(createHash('sha256').update(bytes.subarray(0, 21)).digest()).digest().subarray(0, 4).equals(bytes.subarray(21));
}
export function cashRedemption(account, input, admin = false, fees = FEES) {
  fees ??= FEES;
  ensure(admin || [account.role, account.secondaryRole].some(r => ['moderador', 'streamer', 'creador', 'creador_lvl2', 'creador_lvl3', 'creador_lvl4', 'creador_lvl5'].includes(r)), 'permission-denied', 'Canje no disponible para este rol');
  ensure([10, 25, 50].includes(input.amount), 'invalid-argument', 'Cantidad no válida');
  const binanceEmail = (input.binanceEmail ?? '').trim().toLowerCase();
  const network = input.network ?? '', wallet = input.wallet ?? '';
  ensure(binanceEmail ? binanceEmail.length <= 254 && /^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$/.test(binanceEmail) && !network && !wallet : validWallet(network, wallet), 'invalid-argument', 'Destino de pago no válido');
  const fee = binanceEmail ? 0 : integer(fees[network] ?? FEES[network], 0, 100);
  const totalCost = input.amount + fee, remaining = balance(account, 'ORANGE') - totalCost;
  ensure(remaining >= 0, 'failed-precondition', 'Esencias insuficientes');
  ensure(input.expectedFee === fee, 'failed-precondition', 'La comisión ha cambiado. Revisa el total antes de confirmar.');
  return {remaining, fee, totalCost, binanceEmail, network, wallet};
}
export function fingerprint(input) {
  const ordered = value => Array.isArray(value) ? value.map(ordered) : value && typeof value === 'object' ? Object.fromEntries(Object.keys(value).sort().map(key => [key, ordered(value[key])])) : value;
  return createHash('sha256').update(JSON.stringify(ordered(input))).digest('hex');
}
export function sponsorPrice(notice, activeCount) {
  ensure(notice && typeof notice === 'object' && !Array.isArray(notice), 'invalid-argument', 'Anuncio no válido');
  const maximum = {hour: 12, day: 3, week: 2, month: 6};
  const count = integer(notice.durationValue, 1, maximum[notice.durationUnit] ?? 0);
  for (const [key, limit] of [['title', 200], ['content', 10000], ['videoUrl', 2000], ['expandedImageUrl', 2000], ['externalUrl', 2000]]) {
    ensure(typeof notice[key] === 'string' && notice[key].length <= limit, 'invalid-argument', 'Anuncio no válido');
    if (key.endsWith('Url') && notice[key]) ensure(/^https:\/\//i.test(notice[key]), 'invalid-argument', 'Enlace no válido');
  }
  ensure(notice.title.trim() && notice.content.trim(), 'invalid-argument', 'Anuncio no válido');
  const media = [notice.videoUrl, notice.expandedImageUrl].filter(Boolean);
  const cents = ({hour: 50, day: 250, week: 1000, month: 3000}[notice.durationUnit] + (media.some(url => /\.mp4(?:[?#]|$)/i.test(url)) ? 100 : media.length ? 50 : 0) + (notice.externalUrl ? 50 : 0)) * count;
  const discounted = Math.max(50, Math.round(cents * 10 / (9 + integer(activeCount))));
  return {budget: discounted / 100, cost: Math.floor(discounted / 10)};
}
export function validateInput(input) {
  ensure(input && typeof input === 'object' && !Array.isArray(input), 'invalid-argument', 'Operación no válida');
  const fields = {
    PURCHASE: ['plan', 'currency'], REDEEM: ['amount', 'network', 'wallet', 'binanceEmail', 'expectedFee'],
    RESOLVE: ['redemptionId', 'paid'], ADJUST: ['uid', 'amount', 'currency', 'addition', 'notify', 'title', 'customBody'],
    PREMIUM_GRANT: ['uid', 'days', 'extend'], PREMIUM_REMOVE: ['uid'], ROLE: ['uid', 'role'],
    SPONSOR: ['notice', 'expectedCost'], SUBSCRIBE: ['creatorUid'], UNSUBSCRIBE: ['creatorUid'], CLEANUP: []
  };
  ensure(fields[input.action] && Object.keys(input).every(k => ['action', 'id', ...fields[input.action]].includes(k)), 'invalid-argument', 'Operación no válida');
  ensure(typeof input.id === 'string' && /^[a-zA-Z0-9_-]{8,80}$/.test(input.id), 'invalid-argument', 'Identificador no válido');
  if (input.uid !== undefined) ensure(typeof input.uid === 'string' && /^[a-zA-Z0-9_-]{1,128}$/.test(input.uid), 'invalid-argument', 'Usuario no válido');
  return input;
}
