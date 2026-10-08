import {FieldValue} from 'firebase-admin/firestore';
import {allowedAccount, balance, cashRedemption, deadline, ensure, fingerprint, integer, premiumPurchase, sponsorPrice, validateInput, DAY} from './policy.mjs';

const serverTime = () => FieldValue.serverTimestamp();
const adminActions = new Set(['RESOLVE', 'ADJUST', 'PREMIUM_GRANT', 'PREMIUM_REMOVE', 'ROLE', 'CLEANUP']);
const receipt = (id, now, source, planName, amount, status = 'Completado', days = 0) => ({id, timestamp: now, durationMillis: days * DAY, source, planName, amount, status});
function visible(account, uid) {
  return {uid, role: account.role ?? 'free', secondaryRole: account.secondaryRole ?? '', blueEssence: balance(account, 'BLUE'), orangeEssence: balance(account, 'ORANGE'), premiumUntil: deadline(account.premiumUntil), subscriptionPlan: account.subscriptionPlan ?? ''};
}
export async function executeEconomy(db, auth, raw, clock = Date.now) {
  ensure(auth && auth.uid && auth.token?.firebase?.sign_in_provider !== 'anonymous' && auth.token?.email !== 'coach.guest.reader@gmail.com', 'unauthenticated', 'Inicia sesión');
  const input = validateInput(raw), admin = auth.token?.admin === true;
  ensure(!adminActions.has(input.action) || admin, 'permission-denied', 'No tienes permisos para esta operación');
  const now = integer(clock()), digest = fingerprint(input), operator = db.doc(`users/${auth.uid}`);
  if (input.action === 'CLEANUP') {
    const active = await operator.get(), deletion = await db.doc(`account_deletions/${auth.uid}`).get();
    allowedAccount(active.data(), deletion.data());
    const old = await db.collection('cash_redemptions').where('status', 'in', ['PAID', 'REJECTED']).limit(400).get();
    const expired = old.docs.filter(d => (d.get('historyDeleteAtMillis') ?? ((d.get('resolvedAtMillis') || now) + 14 * DAY)) <= now);
    const batch = db.batch(); for (const doc of expired) batch.delete(doc.ref); if (expired.length) await batch.commit();
    return {ok: true, count: expired.length};
  }
  return db.runTransaction(async tx => {
    const operatorDoc = await tx.get(operator), operatorDeletion = await tx.get(db.doc(`account_deletions/${auth.uid}`));
    allowedAccount(operatorDoc.data(), operatorDeletion.data());
    let uid = input.uid ?? auth.uid, redemption, requestRef;
    if (input.action === 'RESOLVE') {
      ensure(typeof input.redemptionId === 'string' && /^[a-zA-Z0-9_-]{8,80}$/.test(input.redemptionId) && typeof input.paid === 'boolean', 'invalid-argument', 'Solicitud no válida');
      requestRef = db.doc(`cash_redemptions/${input.redemptionId}`); redemption = (await tx.get(requestRef)).data();
      ensure(redemption && /^[a-zA-Z0-9_-]{1,128}$/.test(redemption.userId), 'not-found', 'Solicitud no disponible'); uid = redemption.userId;
    }
    const profile = db.doc(`users/${uid}`), accountDoc = uid === auth.uid ? operatorDoc : await tx.get(profile);
    const account = accountDoc.data(); ensure(account, 'not-found', 'Usuario no disponible');
    const deletion = uid === auth.uid ? operatorDeletion : await tx.get(db.doc(`account_deletions/${uid}`));
    // Settlement/refund of an existing request remains possible during recovery,
    // but a completed purge must never recreate an account or its personal data.
    if (input.action !== 'RESOLVE' && input.action !== 'ROLE') allowedAccount(account, deletion.data());
    else ensure(!['PROCESSING', 'DATA_PURGED', 'COMPLETED'].includes(deletion.data()?.status), 'failed-precondition', 'La cuenta no está disponible');
    const operation = profile.collection('economy_operations').doc(input.id), previous = await tx.get(operation);
    if (previous.exists) {
      ensure(previous.get('fingerprint') === digest && previous.get('actorUid') === auth.uid, 'already-exists', 'El identificador ya pertenece a otra operación');
      return {ok: true, account: visible(account, uid)};
    }
    const updates = {}, writes = []; let record, metadata = {};
    if (input.action === 'PURCHASE') {
      const result = premiumPurchase(account, input.plan, input.currency, now, admin);
      updates[input.currency === 'BLUE' ? 'blueEssence' : 'orangeEssence'] = result.remaining;
      updates.premiumUntil = result.until;
      updates.subscriptionPlan = input.plan === 'MONTHLY' ? 'Suscripción Premium mensual' : 'Suscripción Premium anual';
      record = receipt(input.id, now, 'ESSENCE_PURCHASE', updates.subscriptionPlan, `-${result.cost} ${input.currency === 'BLUE' ? 'EA' : 'EN'}`, 'Completado', result.days);
      metadata = {kind: 'PREMIUM', plan: input.plan, currency: input.currency, cost: result.cost, premiumUntil: result.until};
    } else if (input.action === 'REDEEM') {
      const request = db.doc(`cash_redemptions/${input.id}`), existing = await tx.get(request);
      ensure(!existing.exists, 'already-exists', 'El identificador ya pertenece a otra solicitud');
      const config = await tx.get(db.doc('app_config/binance_fees'));
      const result = cashRedemption(account, input, admin, config.data());
      updates.orangeEssence = result.remaining;
      record = receipt(input.id, now, 'CASH_REDEMPTION', 'Canje de Esencia Naranja', `-${result.totalCost} EN`, 'Pendiente');
      metadata = {kind: 'CASH', currency: 'ORANGE', cost: input.amount, usd: input.amount, network: result.network, wallet: result.wallet, binanceEmail: result.binanceEmail, fee: result.fee, totalCost: result.totalCost};
      writes.push([request, {id: input.id, userId: uid, email: auth.token.email ?? '', userName: account.name ?? '', amount: input.amount, usd: input.amount, paymentCurrency: 'USDT', network: result.network, wallet: result.wallet, binanceEmail: result.binanceEmail, fee: result.fee, totalDeducted: result.totalCost, status: 'PENDING', requestedAt: serverTime(), requestedAtMillis: now}]);
      const id = `payment_${input.id}`, text = `Solicitud de pago: ${input.amount} USDT. Total descontado: ${result.totalCost} EN. Plazo de revisión: 24 a 72 horas.`;
      const ticket = {id, reportId: id, redemptionId: input.id, userId: uid, userEmail: auth.token.email ?? '', userName: account.name ?? 'Usuario', title: 'Solicitud de pago USDT', description: text, content: text, tag: 'PAGO', type: 'PAGO', panel: 'HISTORY', staffVisible: false, status: 'PENDING', staffRead: false, isRead: true, userRead: true, userCanReply: false, timestamp: now, createdAt: serverTime(), conversation: [
        {id: `${id}_initial`, senderName: account.name ?? 'Usuario', senderRole: 'USER', senderUid: uid, text, timestampMillis: now, isGreeting: false},
        {id: `${id}_system`, senderName: 'Sistema Coach', senderRole: 'SYSTEM', senderUid: '', text: 'Hola. El sistema ha recibido tu mensaje. El equipo de Coach te responderá aquí.', timestampMillis: now + 1, isGreeting: true}]};
      writes.push([db.doc(`support_reports/${id}`), ticket], [profile.collection('messages').doc(id), ticket]);
    } else if (input.action === 'RESOLVE') {
      const status = input.paid ? 'PAID' : 'REJECTED';
      if (redemption.status === status) return {ok: true, account: visible(account, uid)};
      ensure(redemption.status === 'PENDING', 'failed-precondition', 'La solicitud no se encuentra pendiente');
      const refund = integer(redemption.totalDeducted ?? redemption.amount + (redemption.fee ?? 0), 1);
      if (!input.paid) updates.orangeEssence = integer(balance(account, 'ORANGE') + refund);
      const ticketRef = db.doc(`support_reports/payment_${input.redemptionId}`), ticket = (await tx.get(ticketRef)).data() ?? {};
      const historyRef = profile.collection('subscription_history').doc(input.redemptionId), history = (await tx.get(historyRef)).data() ?? {};
      const text = input.paid ? `Tu solicitud de pago de ${redemption.amount} USDT ha sido procesada y pagada con éxito.` : `Tu solicitud de pago de ${redemption.amount} USDT ha sido rechazada y las esencias han sido reembolsadas.`;
      const id = `payment_${input.redemptionId}`, conversation = [...(ticket.conversation ?? []), {id: `${input.id}_reply`, senderName: 'Administrador', senderRole: 'SUPPORT', senderUid: auth.uid, text, timestampMillis: now, isGreeting: false}];
      const updated = {...ticket, id, reportId: id, userId: uid, title: 'Solicitud de pago USDT', tag: 'PAGO', type: 'PAGO', panel: 'HISTORY', staffVisible: false, status: input.paid ? 'RESUELTO' : 'RECHAZADO', isCompleted: true, conversation, adminReply: text, lastAdminReply: text, repliedBy: 'Administrador', repliedAt: serverTime(), lastMessageAt: serverTime(), isRead: false, userRead: false, hasNewAdminReply: true, hasNewReply: true, staffRead: true};
      writes.push([requestRef, {...redemption, status, resolvedAt: serverTime(), resolvedAtMillis: now, historyDeleteAtMillis: now + 14 * DAY}], [historyRef, {...history, status: input.paid ? 'Completado' : 'Rechazado y reembolsado'}], [ticketRef, updated], [profile.collection('messages').doc(id), {...updated, content: text, timestamp: now}]);
      record = receipt(input.id, now, 'CASH_RESOLUTION', 'Resolución de canje', input.paid ? '0 EN' : `+${refund} EN`);
      metadata = {kind: 'RESOLUTION', redemptionId: input.redemptionId, status, refunded: input.paid ? 0 : refund};
    } else if (input.action === 'ADJUST') {
      const amount = integer(input.amount, 1), old = balance(account, input.currency);
      ensure(typeof input.addition === 'boolean' && typeof input.notify === 'boolean', 'invalid-argument', 'Operación no válida');
      const change = input.addition ? amount : -Math.min(old, amount), field = input.currency === 'BLUE' ? 'blueEssence' : 'orangeEssence', abbreviation = input.currency === 'BLUE' ? 'EA' : 'EN';
      updates[field] = integer(old + change);
      record = receipt(input.id, now, 'ADMIN_ESSENCE_ADJUSTMENT', input.addition ? 'Regalo de Esencias' : 'Ajuste de Esencias', `${change > 0 ? '+' : ''}${change} ${abbreviation}`, input.addition ? 'Añadido por Administrador' : 'Descontado por Administrador');
      metadata = {kind: 'ADJUSTMENT', currency: input.currency, delta: change};
      if (input.notify) {
        ensure(typeof input.title === 'string' && input.title.length <= 200 && typeof input.customBody === 'string' && input.customBody.length <= 10000, 'invalid-argument', 'Mensaje no válido');
        const message = {id: input.id, title: input.title, content: input.customBody || `Se ha realizado un ajuste de ${record.amount} en tu saldo.`, tag: 'GENERAL', panel: 'HISTORY', timestamp: now, isRead: false, historyRecord: record};
        writes.push([profile.collection('messages').doc(input.id), message]); updates.hasUnreadMessages = true; updates.unreadMessagesCount = FieldValue.increment(1);
      }
    } else if (input.action === 'PREMIUM_GRANT' || input.action === 'PREMIUM_REMOVE') {
      ensure(!['admin', 'administrador'].includes(account.role), 'failed-precondition', 'El acceso de administración es vitalicio.');
      const days = input.action === 'PREMIUM_GRANT' ? integer(input.days, 1, 3650) : 0;
      ensure(input.action !== 'PREMIUM_GRANT' || typeof input.extend === 'boolean', 'invalid-argument', 'Operación no válida');
      updates.premiumUntil = days ? integer(Math.max(input.extend ? deadline(account.premiumUntil) : 0, now) + days * DAY) : 0;
      updates.subscriptionPlan = days ? 'ADMIN_GIFT' : 'FREE'; updates.lastModifiedByAdmin = now;
      record = receipt(input.id, now, days ? 'ADMIN_GIFT' : 'ADMIN_REVOCATION', days ? 'Suscripción Premium regalada' : 'Suscripción Premium retirada', days ? 'Regalo' : '0', 'Completado', days);
    } else if (input.action === 'ROLE') {
      ensure(['free', 'premium', 'banned', 'moderador', 'streamer', 'patrocinador', 'creador', 'creador_lvl2', 'creador_lvl3', 'creador_lvl4', 'creador_lvl5'].includes(input.role), 'invalid-argument', 'Rol no válido');
      Object.assign(updates, {role: input.role, banned: input.role === 'banned', last_role_update: now, bannedTimestamp: input.role === 'banned' ? now : 0});
      if (input.role === 'banned') updates.sessionToken = '';
      if (input.role === 'free') { updates.premiumUntil = 0; updates.subscriptionPlan = 'FREE'; }
      if (input.role === 'premium' && deadline(account.premiumUntil) <= now) updates.premiumUntil = now + 30 * DAY;
      record = receipt(input.id, now, 'ADMIN_ROLE_CHANGE', 'Rol actualizado', '0');
    } else if (input.action === 'SUBSCRIBE' || input.action === 'UNSUBSCRIBE') {
      ensure(typeof input.creatorUid === 'string' && /^[a-zA-Z0-9_-]{1,128}$/.test(input.creatorUid) && input.creatorUid !== uid, 'invalid-argument', 'Creador no válido');
      const subscriptions = Array.isArray(account.subscribedCreators) ? account.subscribedCreators : [];
      const subscribed = subscriptions.includes(input.creatorUid);
      if (subscribed === (input.action === 'SUBSCRIBE')) return {ok: true, account: visible(account, uid)};
      const creatorRef = db.doc(`users/${input.creatorUid}`), creator = (await tx.get(creatorRef)).data();
      const creatorDeletion = (await tx.get(db.doc(`account_deletions/${input.creatorUid}`))).data();
      const entry = creatorRef.collection('creator_subscribers').doc(uid), membership = (await tx.get(entry)).data();
      if (input.action === 'SUBSCRIBE') {
        allowedAccount(creator, creatorDeletion);
        const levels = {creador: [50, 50], creador_lvl2: [150, 60], creador_lvl3: [250, 70], creador_lvl4: [350, 80], creador_lvl5: [500, 80], moderador: [99999, 80], streamer: [99999, 80], admin: [99999, 80]};
        const level = levels[creator.role] ?? levels[creator.secondaryRole]; ensure(level, 'failed-precondition', 'Creador no válido');
        const legacy = await tx.get(db.collection('users').where('subscribedCreators', 'array-contains', input.creatorUid));
        const count = integer(creator.creatorSubscriberCount ?? legacy.size);
        ensure(count < level[0], 'failed-precondition', 'El creador alcanzó su límite de suscriptores');
        const remaining = balance(account, 'ORANGE') - 5; ensure(remaining >= 0, 'failed-precondition', 'Esencias insuficientes');
        const rawReward = 5 * level[1] / 100, reward = rawReward === 2.5 ? 2 : Math.round(rawReward);
        updates.orangeEssence = remaining; updates.subscribedCreators = [...subscriptions, input.creatorUid];
        record = receipt(input.id, now, 'CREATOR_SUBSCRIPTION', 'Suscripción a Creador', '-5 EN');
        ensure(!(await tx.get(creatorRef.collection('economy_operations').doc(input.id))).exists && !(await tx.get(creatorRef.collection('subscription_history').doc(input.id))).exists, 'already-exists', 'El identificador ya pertenece a otra operación');
        const creatorReceipt = receipt(input.id, now, 'CREATOR_SUBSCRIPTION_REWARD', 'Pago por Suscriptor', `+${reward} EN`, 'Añadido por Suscripción');
        writes.push([entry, {userId: uid, operationId: input.id, active: true, subscribedAt: serverTime()}], [creatorRef.collection('subscription_history').doc(input.id), creatorReceipt], [creatorRef.collection('economy_operations').doc(input.id), {id: input.id, kind: 'CREATOR_REWARD', userId: input.creatorUid, actorUid: 'service', timestamp: now, createdAt: serverTime(), cost: 5, reward, receipt: creatorReceipt}], [creatorRef.collection('messages').doc(input.id), {id: input.id, title: '¡Nueva Suscripción con Esencia Naranja!', content: `Un usuario se ha suscrito a tu perfil. Se añadieron ${reward} EN.`, tag: 'GENERAL', panel: 'CREATOR', timestamp: now, isRead: false}]);
        tx.update(creatorRef, {orangeEssence: integer(balance(creator, 'ORANGE') + reward), creatorSubscriberCount: count + 1, hasUnreadMessages: true, unreadMessagesCount: FieldValue.increment(1)});
        metadata = {kind: 'CREATOR_SUBSCRIPTION', creatorUid: input.creatorUid, cost: 5, reward};
      } else {
        updates.subscribedCreators = subscriptions.filter(v => v !== input.creatorUid);
        if (creator && membership?.active) {
          tx.update(creatorRef, {creatorSubscriberCount: Math.max(0, integer(creator.creatorSubscriberCount ?? 1) - 1)});
          tx.delete(entry);
        }
        record = receipt(input.id, now, 'CREATOR_UNSUBSCRIBE', 'Suscripción a Creador retirada', '0 EN');
      }
    } else if (input.action === 'SPONSOR') {
      ensure(admin || [account.role, account.secondaryRole].includes('patrocinador'), 'permission-denied', 'No tienes permisos para esta operación');
      const registry = await tx.get(db.doc('system_config/app_notices'));
      const active = (registry.get('notices') ?? []).filter(n => ['PUBLICIDAD', 'ADS'].includes((n.tag ?? '').toUpperCase()) && n.isApproved && n.isEnabled && (!n.expiresAtMillis || n.expiresAtMillis > now)).length;
      const price = sponsorPrice(input.notice, active);
      ensure(integer(input.expectedCost) === price.cost, 'failed-precondition', 'El costo ha cambiado. Revisa el total antes de confirmar.');
      const pendingRef = db.doc(`pending_sponsor_ads/${input.id}`);
      ensure(!(await tx.get(pendingRef)).exists, 'already-exists', 'El identificador ya pertenece a otro anuncio');
      const remaining = balance(account, 'BLUE') - price.cost; ensure(remaining >= 0, 'failed-precondition', 'Esencias insuficientes');
      updates.blueEssence = remaining;
      const n = input.notice;
      const notice = {id: input.id, title: n.title, content: n.content, videoUrl: n.videoUrl, expandedImageUrl: n.expandedImageUrl, externalUrl: n.externalUrl, titleColor: /^#[a-fA-F0-9]{6}$/.test(n.titleColor ?? '') ? n.titleColor : '#FFD700', tag: 'PUBLICIDAD', budget: price.budget, budgetUnit: n.durationUnit, durationValue: n.durationValue, durationUnit: n.durationUnit, isApproved: false, isEnabled: false, sponsorEmail: auth.token.email ?? '', userId: uid, chargedEssence: price.cost, createdAt: now};
      writes.push([pendingRef, notice]);
      record = receipt(input.id, now, 'SPONSOR_SUBMISSION', 'Anuncio enviado a revisión', `-${price.cost} EA`, 'Pendiente');
      metadata = {kind: 'SPONSOR', cost: price.cost, currency: 'BLUE'};
    }
    updates.lastEconomyOperation = input.id;
    tx.update(profile, updates);
    tx.create(operation, {id: input.id, userId: uid, actorUid: auth.uid, action: input.action, fingerprint: digest, timestamp: now, createdAt: serverTime(), receipt: record, ...metadata});
    tx.create(profile.collection('subscription_history').doc(input.id), record);
    for (const [ref, data] of writes) tx.set(ref, data);
    return {ok: true, account: visible({...account, ...updates}, uid)};
  });
}
