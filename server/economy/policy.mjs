import {createHash} from 'node:crypto';

export class EconomyError extends Error {
  constructor(code, message) { super(message); this.code = code; }
}
export function ensure(ok, code, message) { if (!ok) throw new EconomyError(code, message); }
export const DAY = 86400000;
export const FEES = {BEP20: 1, TRC20: 2, ERC20: 5};
const plans = {
  MONTHLY: {days: 30, BLUE: 100, ORANGE: 9, label: '1 mes'},
  SIX_MONTHS: {days: 180, BLUE: 550, ORANGE: 50, label: '6 meses'},
  ANNUAL: {days: 365, BLUE: 1100, ORANGE: 95, label: '1 año'},
  EIGHTEEN_MONTHS: {days: 545, BLUE: 1580, ORANGE: 138, label: '1 año y 6 meses'},
  TWO_YEARS: {days: 730, BLUE: 2050, ORANGE: 175, label: '2 años'}
};
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
export function hasSponsorRole(account, admin = false) {
  if (admin) return true;
  return [account?.role, account?.secondaryRole]
    .filter(value => typeof value === 'string')
    .map(value => value.trim().toLowerCase())
    .some(value => value === 'patrocinador' || value === 'patrocinio' || value === 'sponsor');
}
export function balance(account, currency) {
  ensure(['BLUE', 'ORANGE'].includes(currency), 'invalid-argument', 'Moneda no válida');
  return integer(account[currency === 'BLUE' ? 'blueEssence' : 'orangeEssence'] ?? 0);
}
export function premiumPurchase(account, plan, currency, now, admin = false) {
  const price = plans[plan]; ensure(price && ['BLUE', 'ORANGE'].includes(currency), 'invalid-argument', 'Plan no válido');
  ensure(!admin && ![account.role, account.secondaryRole].some(r => ['admin', 'administrador', 'moderador'].includes(r)), 'failed-precondition', 'Tu acceso premium es vitalicio');
  const remaining = balance(account, currency) - price[currency]; ensure(remaining >= 0, 'failed-precondition', 'Esencias insuficientes');
  return {remaining, cost: price[currency], days: price.days, label: price.label, until: integer(Math.max(deadline(account.premiumUntil), now) + price.days * DAY)};
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
  ensure(admin === true, 'permission-denied', 'Canje disponible únicamente para el administrador');
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
  const languageScope = String(notice.languageScope ?? 'ALL').trim().toUpperCase();
  ensure(['ALL', 'ES', 'PT', 'BOTH'].includes(languageScope), 'invalid-argument', 'Idioma de anuncio no válido');
  const count = integer(notice.durationValue, 1, maximum[notice.durationUnit] ?? 0);
  for (const [key, limit] of [['title', 200], ['content', 10000], ['videoUrl', 350000], ['videoUrlEs', 350000], ['videoUrlPt', 350000], ['expandedImageUrl', 350000], ['externalUrl', 2000]]) {
    const value = notice[key] ?? '';
    ensure(typeof value === 'string' && value.length <= limit, 'invalid-argument', 'Anuncio no válido');
    if (key === 'externalUrl' && value) ensure(/^https:\/\//i.test(value), 'invalid-argument', 'Enlace no válido');
    if (key !== 'externalUrl' && key.endsWith('Url') && value) {
      ensure(/^https:\/\//i.test(value) || /^data:(?:image|video)\/[a-z0-9.+-]+;base64,/i.test(value), 'invalid-argument', 'Multimedia no válida');
    }
  }
  const videoUrl = String(notice.videoUrl ?? ''), videoUrlEs = String(notice.videoUrlEs ?? ''), videoUrlPt = String(notice.videoUrlPt ?? '');
  ensure(videoUrl.trim() || videoUrlEs.trim() || videoUrlPt.trim() || (notice.title.trim() && notice.content.trim()), 'invalid-argument', 'Anuncio no válido');
  if (languageScope === 'BOTH') {
    ensure(videoUrlEs.trim() && videoUrlPt.trim(), 'invalid-argument', 'Selecciona un banner horizontal para cada idioma');
  }
  const media = [...new Set([videoUrl, videoUrlEs, videoUrlPt, notice.expandedImageUrl].filter(Boolean))];
  const languageMultiplier = languageScope === 'BOTH' ? 2 : 1;
  const cents = ({hour: 50, day: 250, week: 1000, month: 3000}[notice.durationUnit] + (media.some(url => /\.mp4(?:[?#]|$)/i.test(url)) ? 100 : media.length ? 50 : 0) + (notice.externalUrl ? 50 : 0)) * count * languageMultiplier;
  const discounted = Math.max(50, Math.round(cents * 10 / (10 + Math.min(10, Math.max(0, integer(activeCount) - 1)))));
  return {budget: discounted / 100, cost: Math.floor(discounted / 10)};
}
export function validateInput(input) {
  ensure(input && typeof input === 'object' && !Array.isArray(input), 'invalid-argument', 'Operación no válida');
  const fields = {
    PURCHASE: ['plan', 'currency', 'language'], REDEEM: ['amount', 'network', 'wallet', 'binanceEmail', 'expectedFee'],
    RESOLVE: ['redemptionId', 'paid'], ADJUST: ['uid', 'amount', 'currency', 'addition', 'notify', 'title', 'customBody'],
    PREMIUM_GRANT: ['uid', 'days', 'extend'], PREMIUM_REMOVE: ['uid'], ROLE: ['uid', 'role'],
    SPONSOR: ['notice', 'expectedCost'], SPONSOR_RESOLVE: ['sponsorId', 'approved', 'queuedRequestId'], SUBSCRIBE: ['creatorUid'], UNSUBSCRIBE: ['creatorUid'], CLEANUP: []
  };
  ensure(fields[input.action] && Object.keys(input).every(k => ['action', 'id', ...fields[input.action]].includes(k)), 'invalid-argument', 'Operación no válida');
  ensure(typeof input.id === 'string' && /^[a-zA-Z0-9_-]{8,80}$/.test(input.id), 'invalid-argument', 'Identificador no válido');
  if (input.uid !== undefined) ensure(typeof input.uid === 'string' && /^[a-zA-Z0-9_-]{1,128}$/.test(input.uid), 'invalid-argument', 'Usuario no válido');
  if (input.language !== undefined) ensure(['es', 'pt'].includes(input.language), 'invalid-argument', 'Idioma no válido');
  return input;
}
