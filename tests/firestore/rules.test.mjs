import { readFileSync } from 'node:fs';
import assert from 'node:assert/strict';
import { initializeTestEnvironment, assertSucceeds, assertFails } from '@firebase/rules-unit-testing';
import { doc, setDoc, getDoc, updateDoc, deleteDoc, getDocs, getCountFromServer, collection, collectionGroup, query, where, runTransaction, writeBatch, serverTimestamp, Timestamp, increment, onSnapshot } from 'firebase/firestore';
const env = await initializeTestEnvironment({ projectId: 'demo-coach-tests', firestore: { host: '127.0.0.1', port: 8088, rules: readFileSync(new URL('../../firestore.rules', import.meta.url), 'utf8') } });
const db = uid => env.authenticatedContext(uid, { email: `${uid}@test.invalid` }).firestore();
const user = db('user'), moderator = db('mod'), admin = db('admin'), other = db('other'), streamer = db('s1');
const greeting = 'Hola. El sistema ha recibido tu mensaje. El equipo de Coach te responderá aquí. Ningún miembro del staff te pedirá información privada sobre tu cuenta de juego ni sobre tu vida personal.';
const initial = id => [{ id: `${id}_initial`, senderRole: 'USER', senderUid: 'user', text: 'Ayuda', timestampMillis: 100 }, { id: `${id}_system`, senderRole: 'SYSTEM', senderUid: '', text: greeting, timestampMillis: 101 }];
const ticket = (id, tag = 'SOPORTE') => ({ userId: 'user', userEmail: 'user@test.invalid', tag, type: tag, staffVisible: tag !== 'PATROCINADOR', userCanReply:false, status: 'PENDING', conversation: initial(id), userRead: true, isRead: true, hasNewAdminReply: false });
let count = 0;
async function test(name, action) { await action(); count++; console.log(`PASS ${name}`); }
try {
  await env.clearFirestore();
  await env.withSecurityRulesDisabled(async context => {
    const store = context.firestore();
    for (const [uid, role] of [['user','free'],['other','free'],['mod','moderador'],['admin','admin'],['s1','streamer'],['s2','streamer']]) await setDoc(doc(store, 'users', uid), { role, email: `${uid}@test.invalid`, registeredDevices: [] });
    await setDoc(doc(store, 'system_config', 'streamer_live'), { entries: [] });
  });
  await test('same hardware reuses its slot while concurrent logins leave only one session owner',async()=>{
    const ref=doc(user,'users/user');
    async function register(device,token) {
      return runTransaction(user,async tx=>{
        const old=(await tx.get(ref)).data(); const devices=[...new Set(old.registeredDevices||[])];
        if(!devices.includes(device)){assert.ok(devices.length<2);devices.push(device);}
        tx.update(ref,{registeredDevices:devices,lastDeviceId:device,sessionToken:token,sessionStartedAt:serverTimestamp(),is_online:true});
      });
    }
    await assertSucceeds(register('WRD_DEVICE_first','first-login'));
    await assertSucceeds(register('WRD_DEVICE_first','reinstalled-login'));
    assert.deepEqual((await getDoc(ref)).data().registeredDevices,['WRD_DEVICE_first']);
    await Promise.all([register('WRD_DEVICE_first','phone-one'),register('WRD_DEVICE_second','phone-two')]);
    const active=(await getDoc(ref)).data();
    assert.equal(active.registeredDevices.length,2);
    assert.ok(['phone-one','phone-two'].includes(active.sessionToken));
    const stale=active.sessionToken==='phone-one'?'phone-two':'phone-one';
    await assertSucceeds(runTransaction(user,async tx=>{
      const account=(await tx.get(ref)).data();
      if(account.sessionToken===stale)tx.update(ref,{is_online:false});
    }));
    assert.equal((await getDoc(ref)).data().is_online,true);
    await assert.rejects(register('WRD_DEVICE_third','third-phone'));
    assert.deepEqual((await getDoc(ref)).data(),active);
  });
  await test('only the administrator can aggregate publication history and save consumption samples', async()=>{
    await env.withSecurityRulesDisabled(async context => {
      await setDoc(doc(context.firestore(),'streamer_requests/s1/history/storage-test'),{status:'REJECTED',channelName:'Canal'});
    });
    await assertSucceeds(getDocs(collectionGroup(admin,'history')));
    await assertFails(getDocs(collectionGroup(moderator,'history')));
    await assertFails(getDocs(collectionGroup(user,'history')));
    await assertSucceeds(setDoc(doc(admin,'system_config/saved_data_consumption'),{estimatedContentBytes:2048,sampledAt:Date.now()}));
    await assertFails(setDoc(doc(user,'system_config/saved_data_consumption'),{estimatedContentBytes:0}));
  });
  const economy = db('economy');
  const future = Date.now() + 86400000 * 90;
  await env.withSecurityRulesDisabled(async context => {
    const store=context.firestore();
    await setDoc(doc(store, 'users/economy'), { role:'creador', secondaryRole:'streamer', email:'economy@test.invalid', blueEssence:2000, orangeEssence:120, premiumUntil:Timestamp.fromMillis(future) });
    await setDoc(doc(store, 'users/concurrent'), {role:'free', email:'concurrent@test.invalid', orangeEssence:10});
  });
  function purchase(store, uid, id, plan='MONTHLY', currency='BLUE', override={}) {
    return runTransaction(store, async tx => {
      const profile = doc(store, `users/${uid}`), operation = doc(store, `users/${uid}/economy_operations/${id}`);
      if ((await tx.get(operation)).exists()) return;
      const account = (await tx.get(profile)).data(), timestamp=Date.now();
      const field=currency==='BLUE'?'blueEssence':'orangeEssence', days=plan==='MONTHLY'?30:365;
      const cost=plan==='MONTHLY'?(currency==='BLUE'?100:9):(currency==='BLUE'?1100:95);
      const old=account.premiumUntil instanceof Timestamp?account.premiumUntil.toMillis():(account.premiumUntil||0);
      const until=Math.max(timestamp,old)+days*86400000;
      const receipt={id,timestamp,durationMillis:days*86400000,planName:'Premium',status:'Completado',amount:`-${cost}`,source:'ESSENCE_PURCHASE'};
      tx.set(operation,{id,userId:uid,kind:'PREMIUM',plan,currency,cost,premiumUntil:until,timestamp,createdAt:serverTimestamp(),receipt,...override});
      tx.update(profile,{[field]:(account[field]||0)-cost,premiumUntil:until,subscriptionPlan:receipt.planName,lastEconomyOperation:id});
      tx.set(doc(store,`users/${uid}/subscription_history/${id}`),receipt);
    });
  }
  await test('essence plans use exact prices and inherit time without changing either role',async()=>{
    for(const [id,plan,currency,days,price] of [['monthly-blue','MONTHLY','BLUE',30,100],['monthly-orange','MONTHLY','ORANGE',30,9],['annual-blue','ANNUAL','BLUE',365,1100],['annual-orange','ANNUAL','ORANGE',365,95]]) {
      const before=(await getDoc(doc(economy,'users/economy'))).data();
      await assertSucceeds(purchase(economy,'economy',id,plan,currency));
      const after=(await getDoc(doc(economy,'users/economy'))).data();
      const old=before.premiumUntil instanceof Timestamp?before.premiumUntil.toMillis():before.premiumUntil;
      assert.equal(after.premiumUntil,old+days*86400000);assert.equal(after.role,'creador');assert.equal(after.secondaryRole,'streamer');
      const field=currency==='BLUE'?'blueEssence':'orangeEssence'; assert.equal(after[field],before[field]-price);
      assert.equal((await getDoc(doc(economy,`users/economy/economy_operations/${id}`))).data().cost,price);
    }
  });
  await test('purchase retries are idempotent and receipts cannot be overwritten',async()=>{
    const before=(await getDoc(doc(economy,'users/economy'))).data();
    await assertSucceeds(purchase(economy,'economy','monthly-blue'));
    assert.deepEqual((await getDoc(doc(economy,'users/economy'))).data(),before);
    await assertFails(updateDoc(doc(economy,'users/economy/economy_operations/monthly-blue'),{cost:0}));
    await assertFails(updateDoc(doc(economy,'users/economy/subscription_history/monthly-blue'),{amount:'0'}));
  });
  await test('users cannot credit essence, forge purchases or change entitlement separately',async()=>{
    await assertFails(updateDoc(doc(economy,'users/economy'),{orangeEssence:100000}));
    await assertFails(purchase(economy,'economy','wrong-price','MONTHLY','BLUE',{cost:1}));
    await assertFails(purchase(economy,'economy','wrong-duration','MONTHLY','BLUE',{premiumUntil:Date.now()+999999999999}));
    await assertFails(setDoc(doc(economy,'users/economy/economy_operations/standalone'),{userId:'economy',id:'standalone',createdAt:serverTimestamp(),receipt:{}}));
    await assertFails(setDoc(doc(economy,'users/economy/subscription_history/fake'),{source:'ESSENCE_PURCHASE',amount:'-9 EN'}));
    assert.equal((await getDoc(doc(economy,'users/economy/economy_operations/wrong-price'))).exists(),false);
  });
  await test('concurrent purchases cannot overdraw a balance',async()=>{
    const store=db('concurrent');
    const result=await Promise.allSettled([purchase(store,'concurrent','first','MONTHLY','ORANGE'),purchase(store,'concurrent','second','MONTHLY','ORANGE')]);
    assert.equal(result.filter(r=>r.status==='fulfilled').length,1);
    assert.equal((await getDoc(doc(store,'users/concurrent'))).data().orangeEssence,1);
    assert.equal((await getDocs(collection(store,'users/concurrent/subscription_history'))).size,1);
  });
  function redeem(id, amount, override={}, store=economy, uid="economy") {
    return runTransaction(store,async tx=>{
      const profile=doc(store,`users/${uid}`),operation=doc(store,`users/${uid}/economy_operations/${id}`);
      if((await tx.get(operation)).exists())return;
      const account=(await tx.get(profile)).data(),timestamp=Date.now();
      const binanceEmail=(override.binanceEmail||'').trim().toLowerCase();
      const network=binanceEmail?'':(override.network??'ERC20');
      const wallet=binanceEmail?'':(override.wallet??'0x1111111111111111111111111111111111111111');
      const fee=binanceEmail?0:(network==='BEP20'?1:(network==='TRC20'?2:(network==='ERC20'?5:-1)));
      const totalCost=amount+fee;
      const receipt={id,timestamp,durationMillis:0,planName:'Canje de Esencia Naranja',status:'Pendiente',amount:`-${totalCost} EN`,source:'CASH_REDEMPTION'};
      tx.set(operation,{id,userId:uid,kind:'CASH',currency:'ORANGE',cost:amount,usd:amount,network,wallet,binanceEmail,fee,totalCost,timestamp,createdAt:serverTimestamp(),receipt});
      tx.update(profile,{orangeEssence:account.orangeEssence-totalCost,lastEconomyOperation:id});
      tx.set(doc(store,`cash_redemptions/${id}`),{id,userId:uid,email:`${uid}@test.invalid`,userName:account.name||'',amount,usd:amount,paymentCurrency:'USDT',network,wallet,binanceEmail,fee,totalDeducted:totalCost,status:'PENDING',requestedAt:serverTimestamp(),requestedAtMillis:timestamp,...override,network,wallet,binanceEmail,fee,totalDeducted:totalCost});
      const reportId=`payment_${id}`;
      const conversation=initial(reportId).map(entry=>entry.senderRole==='USER'?{...entry,senderUid:uid}:entry);
      const payment={id:reportId,reportId,redemptionId:id,userId:uid,userEmail:`${uid}@test.invalid`,userName:account.name||'Usuario',title:'Solicitud de pago USDT',description:'Solicitud de pago USDT',content:'Solicitud de pago USDT',tag:'PAGO',type:'PAGO',panel:'HISTORY',staffVisible:false,status:'PENDING',staffRead:false,isRead:true,userRead:true,userCanReply:false,timestamp,createdAt:serverTimestamp(),conversation};
      tx.set(doc(store,`support_reports/${reportId}`),payment);
      tx.set(doc(store,`users/${uid}/messages/${reportId}`),payment);
      tx.set(doc(store,`users/${uid}/subscription_history/${id}`),receipt);
    });
  }
  await test('cash requests debit atomically and keep owner-only receipts',async()=>{
    await assertSucceeds(redeem('cash-ten',10));
    const before=(await getDoc(doc(economy,'users/economy'))).data();
    await assertSucceeds(redeem('cash-ten',10));
    assert.equal((await getDoc(doc(economy,'users/economy'))).data().orangeEssence,before.orangeEssence);
    await assertSucceeds(getDoc(doc(admin,'cash_redemptions/cash-ten')));
    assert.equal((await getDoc(doc(admin,'support_reports/payment_cash-ten'))).data().redemptionId,'cash-ten');
    assert.equal((await getDoc(doc(economy,'users/economy/messages/payment_cash-ten'))).data().tag,'PAGO');
    await assertFails(getDoc(doc(moderator,'support_reports/payment_cash-ten')));
    await assertFails(getDoc(doc(other,'users/economy/messages/payment_cash-ten')));
    await assertFails(getDoc(doc(other,'cash_redemptions/cash-ten')));
    await assertFails(getDocs(collection(other,'cash_redemptions')));
    await assertSucceeds(getDocs(query(collection(economy,'cash_redemptions'),where('userId','==','economy'))));
  });
  await test('pending payouts load for the administrator and remain private from moderators',async()=>{
    await assertSucceeds(getDocs(query(collection(admin,'cash_redemptions'),where('status','==','PENDING'))));
    await assertFails(getDocs(query(collection(moderator,'cash_redemptions'),where('status','==','PENDING'))));
    await assertFails(getDocs(query(collection(other,'cash_redemptions'),where('status','==','PENDING'))));
  });
  await test('administrator redemption has the same atomic debit and idempotent receipt',async()=>{
    await updateDoc(doc(admin,'users/admin'),{orangeEssence:400});
    await assertSucceeds(redeem('admin-fifty',50,{},admin,'admin'));
    assert.equal((await getDoc(doc(admin,'users/admin'))).data().orangeEssence,345);
    await assertSucceeds(redeem('admin-fifty',50,{},admin,'admin'));
    assert.equal((await getDoc(doc(admin,'users/admin'))).data().orangeEssence,345);
    assert.equal((await getDoc(doc(admin,'cash_redemptions/admin-fifty'))).data().usd,50);
    await assertFails(getDoc(doc(moderator,'support_reports/payment_admin-fifty')));
  });
  await test('cash amount, paid status and refunds cannot be forged by a user',async()=>{
    await assertFails(redeem('wrong-cash-amount',3));
    await assertFails(redeem('fake-paid',10,{status:'PAID'}));
    await assertFails(redeem('fake-usd',10,{usd:1000}));
    await assertFails(redeem('fake-wallet',10,{wallet:'wrong-wallet'}));
    await assertFails(redeem('fake-network',10,{network:'UNKNOWN'}));
    await assertFails(updateDoc(doc(economy,'cash_redemptions/cash-ten'),{status:'PAID'}));
    await assertFails(deleteDoc(doc(economy,'cash_redemptions/cash-ten')));
    await assertFails(redeem('insufficient',50));
    await updateDoc(doc(admin,'users/economy'),{orangeEssence:50});
    await assertSucceeds(redeem('binance-email',10,{binanceEmail:'payout@example.com'}));
    const emailPayout=(await getDoc(doc(economy,'cash_redemptions/binance-email'))).data();
    assert.equal(emailPayout.binanceEmail,'payout@example.com');
    assert.equal(emailPayout.network,'');
    assert.equal(emailPayout.wallet,'');
    await assertFails(redeem('binance-email-invalid',10,{binanceEmail:'correo-invalido'}));
    const binanceRef=doc(admin,'cash_redemptions/binance-email');
    await assertFails(deleteDoc(binanceRef));
    await assertSucceeds(updateDoc(binanceRef,{status:'PAID',resolvedAtMillis:Date.now()-1209601000,historyDeleteAtMillis:Date.now()-1000}));
    await assertSucceeds(deleteDoc(binanceRef));
  });
  await test('staff can reject a cash request and refund once in one transaction',async()=>{
    const before=(await getDoc(doc(admin,'users/economy'))).data().orangeEssence;
    await assertSucceeds(runTransaction(admin,async tx=>{
      const request=doc(admin,'cash_redemptions/cash-ten'),profile=doc(admin,'users/economy');
      const r=await tx.get(request),p=await tx.get(profile);assert.equal(r.data().status,'PENDING');
      tx.update(profile,{orangeEssence:p.data().orangeEssence+r.data().totalDeducted});
      tx.update(request,{status:'REJECTED',resolvedAt:serverTimestamp()});
      tx.update(doc(admin,'users/economy/subscription_history/cash-ten'),{status:'Rechazado y reembolsado'});
    }));
    assert.equal((await getDoc(doc(economy,'users/economy'))).data().orangeEssence,before+15);
  });
  await test('payment conversation is private to its owner and the administrator',async()=>{
    const data={...ticket('payment','PAGO'),staffVisible:false};
    await assertSucceeds(setDoc(doc(user,'support_reports/payment'),data));
    await assertSucceeds(getDoc(doc(admin,'support_reports/payment')));
    await assertFails(getDoc(doc(moderator,'support_reports/payment')));
    await assertFails(getDoc(doc(other,'support_reports/payment')));
    await assertFails(setDoc(doc(user,'support_reports/payment-exposed'),{...data,staffVisible:true}));
  });
  await test('all cash request amounts debit the payout plus the exact network fee',async()=>{
    await updateDoc(doc(admin,'users/economy'),{orangeEssence:100});
    for(const amount of [25,50]) {
      const before=(await getDoc(doc(economy,'users/economy'))).data().orangeEssence;
      await assertSucceeds(redeem(`cash-${amount}`,amount));
      assert.equal((await getDoc(doc(economy,'users/economy'))).data().orangeEssence,before-amount-5);
      assert.equal((await getDoc(doc(economy,`cash_redemptions/cash-${amount}`))).data().usd,amount);
    }
  });
  await test('read acknowledgements synchronize without changing pending sponsorships',async()=>{
    await assertSucceeds(setDoc(doc(user,'users/user/panel_reads/sponsor-read'),{event:'notice:sponsor',revision:'',readAt:serverTimestamp()}));
    await assertSucceeds(getDoc(doc(db('user'),'users/user/panel_reads/sponsor-read')));
    await assertFails(getDoc(doc(other,'users/user/panel_reads/sponsor-read')));
    await assertFails(setDoc(doc(other,'users/user/panel_reads/forged'),{event:'notice:sponsor',revision:'',readAt:serverTimestamp()}));
  });
  await test('ticket and inbox mirror are created atomically with one system greeting', async () => {
    const batch = writeBatch(user); batch.set(doc(user, 'support_reports', 'ticket'), ticket('ticket')); batch.set(doc(user, 'users/user/messages/ticket'), ticket('ticket')); await assertSucceeds(batch.commit());
  });
  await test('owner and moderator can read support but another user cannot', async () => {
    await assertSucceeds(getDoc(doc(user, 'support_reports', 'ticket'))); await assertSucceeds(getDoc(doc(moderator, 'support_reports', 'ticket'))); await assertFails(getDoc(doc(other, 'support_reports', 'ticket')));
    await assertSucceeds(getDocs(query(collection(moderator, 'support_reports'), where('staffVisible', '==', true)))); await assertFails(getDocs(collection(moderator, 'support_reports')));
  });
  await test('sponsor stays private to its owner and administrator', async () => {
    await assertSucceeds(setDoc(doc(user, 'support_reports', 'sponsor'), ticket('sponsor','PATROCINADOR')));
    await assertSucceeds(getDoc(doc(admin, 'support_reports', 'sponsor'))); await assertFails(getDoc(doc(moderator, 'support_reports', 'sponsor')));
    await assertFails(updateDoc(doc(user, 'support_reports', 'sponsor'), { staffVisible: true }));
    await assertFails(setDoc(doc(user, 'support_reports', 'fake'), { ...ticket('fake','PATROCINADOR'), staffVisible: true }));
  });
  await test('role and secondary-role self escalation are forbidden', async () => {
    await assertFails(updateDoc(doc(user, 'users', 'user'), { role: 'admin' })); await assertFails(updateDoc(doc(user, 'users', 'user'), { secondaryRole: 'moderador' }));
  });
  await test('staff appends repeated answers and syncs recipient mirror', async () => {
    for (let i = 0; i < 2; i++) await assertSucceeds(runTransaction(moderator, async transaction => {
      const ref = doc(moderator, 'support_reports', 'ticket'), snap = await transaction.get(ref);
      const history = [...snap.data().conversation, { id: `reply${i}`, senderRole: 'SUPPORT', senderUid: 'mod', text: `Respuesta ${i}`, timestampMillis: 200+i }];
      const data = { conversation: history, status: 'READ', userCanReply:true, userRead: false, isRead: false, hasNewAdminReply: true, staffRead: true };
      transaction.update(ref, data); transaction.set(doc(moderator, 'users/user/messages/ticket'), data, { merge: true });
    }));
    assert.equal((await getDoc(doc(user, 'support_reports', 'ticket'))).data().conversation.length, 4);
  });
  await test('moderators cannot replace history or send another system greeting', async () => {
    const ref = doc(moderator, 'support_reports', 'ticket'), history = (await getDoc(doc(admin, 'support_reports', 'ticket'))).data().conversation;
    await assertFails(updateDoc(ref, { conversation: [...history, { id:'fake-system',senderRole:'SYSTEM',senderUid:'mod',text:greeting }] }));
    await assertFails(updateDoc(ref, { conversation: [...history, { id:'fake-greeting',senderRole:'SUPPORT',senderUid:'mod',text:greeting }] }));
    await assertFails(updateDoc(ref, { conversation: [{ id:'replace',senderRole:'SUPPORT',senderUid:'mod',text:'Sobrescribir' }] }));
  });
  await test('read status propagates to another device and ticket cannot be spoofed by user', async () => {
    await assertSucceeds(updateDoc(doc(user, 'support_reports', 'ticket'), { userRead: true, isRead: true, hasNewAdminReply: false, hasNewReply: false }));
    const secondDevice = db('user'); assert.equal((await getDoc(doc(secondDevice, 'support_reports', 'ticket'))).data().userRead, true);
    await assertFails(updateDoc(doc(user, 'support_reports', 'ticket'), { adminReply: 'Respuesta falsa' })); await assertFails(updateDoc(doc(user, 'support_reports', 'ticket'), { status: 'SOLVED' }));
  });
  await test('user must wait for real staff reply and cannot bypass the waiting state', async () => {
    await assertSucceeds(setDoc(doc(user, 'support_reports', 'new'), ticket('new')));
    const history = [...initial('new'), { id:'followup', senderRole:'USER', senderUid:'user', text:'Más detalles', timestampMillis:300 }];
    await assertFails(updateDoc(doc(user, 'support_reports', 'new'), { conversation:history, status:'PENDING', isCompleted:false, staffRead:false, lastUserMessage:'Más detalles' }));
    await assertFails(updateDoc(doc(user, 'support_reports', 'new'), { userCanReply:true }));
    await assertFails(updateDoc(doc(moderator, 'support_reports', 'new'), { userCanReply:true }));
    await assertFails(setDoc(doc(user, 'support_reports', 'fake-unlock'), { ...ticket('fake-unlock'), userCanReply:true }));
    const staffHistory = [...initial('new'), {id:'staff-first', senderRole:'SUPPORT', senderUid:'mod', text:'Respuesta real', timestampMillis:250}];
    await assertSucceeds(updateDoc(doc(moderator, 'support_reports', 'new'), { conversation:staffHistory, userCanReply:true, status:'READ' }));
    await assertSucceeds(updateDoc(doc(user, 'support_reports', 'new'), { conversation:[...staffHistory,history.at(-1)], status:'PENDING', isCompleted:false, staffRead:false, lastUserMessage:'Más detalles' }));
    await assertFails(updateDoc(doc(user, 'support_reports', 'new'), { conversation:[...history.slice(1),{ id:'evil',senderRole:'SUPPORT',senderUid:'user',text:'Falso' }] }));
  });
  await test('repeated user and staff turns remain open across devices until staff closes', async () => {
    for (let turn = 0; turn < 3; turn++) {
      for (const [store, senderRole, senderUid, status] of [[moderator, 'SUPPORT', 'mod', 'READ'], [user, 'USER', 'user', 'PENDING']]) {
        const ref = doc(store, 'support_reports', 'new');
        await assertSucceeds(runTransaction(store, async transaction => {
          const snap = await transaction.get(ref);
          const entry = { id: `turn-${turn}-${senderRole}`, senderRole, senderUid, text: `Mensagem ${turn}`, timestampMillis: 1000 + turn, isGreeting: false };
          const data = { conversation: [...snap.data().conversation, entry], status, isCompleted: false, staffRead: senderRole === 'SUPPORT', ...(senderRole === 'SUPPORT' ? { userCanReply: true } : { lastUserMessage: entry.text }) };
          transaction.update(ref, data);
          transaction.set(doc(store, 'users/user/messages/new'), data, { merge: true });
        }));
      }
    }
    assert.equal((await getDoc(doc(db('user'), 'users/user/messages/new'))).data().conversation.length, 10);
  });
  await test('conflicting legacy sponsor tags cannot expose a sponsorship to moderators', async () => {
    const data = { ...ticket('legacy-sponsor', 'SOPORTE'), type: 'PATROCINADOR', staffVisible: false };
    await assertSucceeds(setDoc(doc(user, 'support_reports', 'legacy-sponsor'), data));
    await assertSucceeds(getDoc(doc(admin, 'support_reports', 'legacy-sponsor')));
    await assertFails(getDoc(doc(moderator, 'support_reports', 'legacy-sponsor')));
    await assertFails(setDoc(doc(user, 'support_reports', 'exposed-sponsor'), { ...data, staffVisible: true }));
  });
  await test('only administrator deletes tickets and inbox messages', async () => {
    await assertFails(deleteDoc(doc(user,'support_reports','ticket')));
    await assertFails(deleteDoc(doc(moderator,'support_reports','ticket')));
    await assertFails(deleteDoc(doc(user,'users/user/messages/ticket')));
    await assertFails(deleteDoc(doc(moderator,'users/user/messages/ticket')));
    await assertSucceeds(deleteDoc(doc(admin,'support_reports','ticket')));
    await assertSucceeds(deleteDoc(doc(admin,'users/user/messages/ticket')));
  });
  await test('resolved status is shared and prevents user replies', async () => {
    const batch = writeBatch(moderator);
    const close = { status:'SOLVED', isCompleted:true, staffRead:true };
    batch.update(doc(moderator, 'support_reports', 'new'), close);
    batch.set(doc(moderator, 'users/user/messages/new'), close, { merge:true });
    await assertSucceeds(batch.commit());
    assert.equal((await getDoc(doc(user,'support_reports','new'))).data().status,'SOLVED');
    assert.equal((await getDoc(doc(db('user'),'users/user/messages/new'))).data().status,'SOLVED');
    const history = (await getDoc(doc(user,'support_reports','new'))).data().conversation;
    await assertFails(updateDoc(doc(moderator,'support_reports','new'), { conversation:[...history,{id:'closed-staff',senderRole:'SUPPORT',senderUid:'mod',text:'Fechado',isGreeting:false}], status:'READ' }));
    await assertFails(updateDoc(doc(user,'support_reports','new'), { conversation:[...history,{ id:'closedreply', senderRole:'USER',senderUid:'user',text:'No',timestampMillis:400 }], status:'PENDING',isCompleted:false,staffRead:false }));
  });
  const request = uid => ({ userId:uid,userName:uid,channelName:'Canal Coach',channelUrl:'https://twitch.tv/coach_test',platform:'Twitch',status:'PENDING',usingCoachAcknowledged:true,submittedAtMillis:Date.now(), submittedAt:serverTimestamp(), publicationId:`publication-${uid}-${Date.now()}` });
  await test('Google test URL is reserved for administrators and cannot be forged by streamers', async () => {
    const google = uid => ({ ...request(uid), channelUrl: 'https://www.google.com', platform: 'Google' });
    await assertSucceeds(setDoc(doc(admin, 'streamer_requests', 'admin'), { ...google('admin'), adminTest: true }));
    await assertFails(setDoc(doc(user, 'streamer_requests', 'user'), google('user')));
    await assertFails(setDoc(doc(streamer, 'streamer_requests', 's1'), google('s1')));
    await assertFails(setDoc(doc(streamer, 'streamer_requests', 's1'), { ...google('s1'), adminTest: true }));
    await assertFails(setDoc(doc(streamer, 'streamer_requests', 's1'), { ...request('s1'), adminTest: true }));
    const secondary = db('secondary');
    await env.withSecurityRulesDisabled(async context => {
      await setDoc(doc(context.firestore(), 'users', 'secondary'), { role: 'free', secondaryRole: 'streamer' });
    });
    await assertFails(setDoc(doc(secondary, 'streamer_requests', 'secondary'), google('secondary')));
    await assertSucceeds(setDoc(doc(secondary, 'streamer_requests', 'secondary'), request('secondary')));
  });
  await test('only streamers request and only admin reviews; bad hosts are rejected', async () => {
    await assertSucceeds(setDoc(doc(streamer,'streamer_requests','s1'),request('s1')));
    await assertFails(setDoc(doc(user,'streamer_requests','user'),request('user')));
    await assertFails(updateDoc(doc(streamer,'streamer_requests','s1'),{status:'APPROVED'}));
    await assertFails(getDocs(collection(moderator,'streamer_requests')));
    await assertFails(setDoc(doc(db('s2'),'streamer_requests','s2'),{...request('s2'),channelUrl:'https://twitch.tv.evil.com/coach_test'}));
    await assertSucceeds(getDocs(query(collection(admin,'streamer_requests'),where('status','==','PENDING'))));
  });
  await test('pending streamer requests survive reopening on a second device before review', async () => {
    const ownerSecondDevice=db('s1'), reviewerSecondDevice=db('admin');
    const fromFirst=(await getDoc(doc(streamer,'streamer_requests/s1'))).data();
    const fromSecond=(await assertSucceeds(getDoc(doc(ownerSecondDevice,'streamer_requests/s1')))).data();
    assert.equal(fromSecond.publicationId,fromFirst.publicationId);
    assert.equal(fromSecond.status,'PENDING');
    for (const device of [admin,reviewerSecondDevice]) {
      const queue=await assertSucceeds(getDocs(query(collection(device,'streamer_requests'),where('status','==','PENDING'))));
      assert.ok(queue.docs.some(row=>row.id==='s1'&&row.data().publicationId===fromFirst.publicationId));
    }
    await assertFails(updateDoc(doc(ownerSecondDevice,'streamer_requests/s1'),{status:'APPROVED'}));
    assert.equal((await getDoc(doc(reviewerSecondDevice,'streamer_requests/s1'))).data().status,'PENDING');
  });
  await test('concurrent approvals never exceed five; requests stop at capacity', async () => {
    await setDoc(doc(admin,'system_config','streamer_live'), { entries:[1,2,3,4].map(i=>({userId:`live${i}`,channelName:`Canal ${i}`,channelUrl:'https://kick.com/coach_test'})) });
    const publish = uid => runTransaction(admin, async transaction => {
      const ref = doc(admin,'system_config','streamer_live'), snapshot = await transaction.get(ref), entries = snapshot.data().entries;
      if (entries.length >= 5) throw new Error('capacity');
      transaction.update(ref,{entries:[...entries,{userId:uid,channelName:uid,channelUrl:'https://twitch.tv/coach_test'}]});
    });
    const results = await Promise.allSettled([publish('s1'),publish('s2')]);
    assert.equal(results.filter(r=>r.status==='fulfilled').length,1); assert.equal((await getDoc(doc(admin,'system_config','streamer_live'))).data().entries.length,5);
    await assertFails(setDoc(doc(db('s2'),'streamer_requests','s2'),request('s2')));
    await assertFails(updateDoc(doc(admin,'system_config','streamer_live'),{entries:Array.from({length:6},(_,i)=>({userId:`u${i}`}))}));
    await assertFails(updateDoc(doc(streamer,'system_config','streamer_live'),{entries:[]}));
  });
  await test('streamer removes only own publication and releases one slot', async () => {
    await setDoc(doc(admin,'system_config','streamer_live'),{entries:[{userId:'s1',channelName:'Uno',channelUrl:'https://kick.com/coach_test'},{userId:'s2',channelName:'Dos',channelUrl:'https://kick.com/coach_two'}]});
    await assertFails(updateDoc(doc(streamer,'system_config','streamer_live'),{entries:[]}));
    await assertSucceeds(updateDoc(doc(streamer,'system_config','streamer_live'),{entries:[{userId:'s2',channelName:'Dos',channelUrl:'https://kick.com/coach_two'}]}));
  });
  await test('publication history is shared by devices and owners cannot forge decisions', async () => {
    await setDoc(doc(admin,'system_config','streamer_live'), { entries:[] });
    await assertSucceeds(getDocs(query(collection(db('s2'),'streamer_requests/s2/history'))));
    const data = request('s2');
    const secondStreamer = db('s2');
    const batch = writeBatch(secondStreamer);
    batch.set(doc(secondStreamer,'streamer_requests','s2'),data);
    batch.set(doc(secondStreamer,`streamer_requests/s2/history/${data.publicationId}`),data);
    await assertSucceeds(batch.commit());
    const historyRef = doc(db('s2'),`streamer_requests/s2/history/${data.publicationId}`);
    assert.equal((await getDoc(historyRef)).data().status,'PENDING');
    await assertFails(updateDoc(historyRef,{status:'APPROVED'}));
    await assertFails(updateDoc(historyRef,{channelName:'Otro canal'}));
    await assertFails(getDocs(collection(other,'streamer_requests/s2/history')));
    await assertSucceeds(updateDoc(doc(admin,'streamer_requests','s2'),{status:'APPROVED',reviewedAtMillis:Date.now()}));
    const approved = (await getDoc(doc(admin,'streamer_requests','s2'))).data();
    await assertSucceeds(setDoc(doc(admin,`streamer_requests/s2/history/${data.publicationId}`),approved));
    assert.equal((await getDoc(doc(db('s2'),`streamer_requests/s2/history/${data.publicationId}`))).data().status,'APPROVED');
    const reused = { ...request('s2'), publicationId:data.publicationId };
    const forged = writeBatch(secondStreamer);
    forged.set(doc(secondStreamer,'streamer_requests','s2'),reused);
    forged.set(doc(secondStreamer,`streamer_requests/s2/history/${data.publicationId}`),reused);
    await assertFails(forged.commit());
  });
  await test('expired requests leave the queue atomically and remain rejected in history', async () => {
    const expiredAt = Date.now()-10800000-1000;
    const expired = { ...request('s1'),submittedAtMillis:expiredAt,submittedAt:Timestamp.fromMillis(expiredAt),publicationId:'publication-expired-test' };
    await env.withSecurityRulesDisabled(async context => {
      const store = context.firestore();
      await setDoc(doc(store,'streamer_requests','s1'),expired);
      await setDoc(doc(store,'streamer_requests/s1/history/publication-expired-test'),expired);
    });
    await assertFails(updateDoc(doc(admin,'streamer_requests','s1'),{status:'APPROVED'}));
    await assertFails(deleteDoc(doc(streamer,'streamer_requests','s1')));
    const batch = writeBatch(streamer);
    batch.set(doc(streamer,'streamer_requests/s1/history/publication-expired-test'),{...expired,status:'REJECTED',rejectionReason:'TIMEOUT',reviewedAtMillis:expiredAt+10800000});
    batch.delete(doc(streamer,'streamer_requests','s1'));
    await assertSucceeds(batch.commit());
    assert.equal((await getDoc(doc(db('s1'),'streamer_requests','s1'))).exists(),false);
    assert.equal((await getDoc(doc(db('s1'),'streamer_requests/s1/history/publication-expired-test'))).data().status,'REJECTED');
    await assertSucceeds(setDoc(doc(streamer,'streamer_requests','s1'),request('s1')));
  });
  await test('owners cannot expire or replace a request early or fake its submission date', async () => {
    const current = (await getDoc(doc(streamer,'streamer_requests','s1'))).data();
    const batch = writeBatch(streamer);
    batch.set(doc(streamer,`streamer_requests/s1/history/${current.publicationId}`),{...current,status:'REJECTED',rejectionReason:'TIMEOUT',reviewedAtMillis:current.submittedAtMillis+10800000});
    batch.delete(doc(streamer,'streamer_requests','s1'));
    await assertFails(batch.commit());
    await assertFails(setDoc(doc(streamer,'streamer_requests','s1'),request('s1')));
    await assertFails(setDoc(doc(db('s2'),'streamer_requests','s2'),{...request('s2'),submittedAtMillis:Date.now()+99999999}));
    await assertFails(deleteDoc(doc(streamer,`streamer_requests/s1/history/${current.publicationId}`)));
  });
  await test('role edits preserve the stored premium deadline and members cannot edit it', async () => {
    const until = Date.now()+7*86400000;
    await assertSucceeds(updateDoc(doc(admin,'users','s1'),{premiumUntil:until,role:'premium'}));
    await assertSucceeds(updateDoc(doc(admin,'users','s1'),{role:'streamer'}));
    assert.equal((await getDoc(doc(streamer,'users','s1'))).data().premiumUntil,until);
    await assertFails(updateDoc(doc(streamer,'users','s1'),{premiumUntil:until+86400000}));
    await assertSucceeds(updateDoc(doc(admin,'users','s1'),{premiumUntil:until+86400000}));
  });
  await test('guests read published channels but cannot read private configuration or history', async () => {
    const guest = env.unauthenticatedContext().firestore();
    await assertSucceeds(getDoc(doc(guest,'system_config','streamer_live')));
    await assertFails(getDoc(doc(guest,'system_config','app_notice_analytics')));
    await assertFails(getDocs(collection(guest,'system_config')));
    await assertFails(getDocs(collection(guest,'streamer_requests/s2/history')));
    await assertFails(updateDoc(doc(guest,'system_config','streamer_live'),{entries:[]}));
  });
  const counterId = 'publication-click-count-test';
  await test('channel opens from guests and two devices increment one shared private count', async () => {
    const owner = db('s2'), guest = env.unauthenticatedContext().firestore();
    await assertSucceeds(setDoc(doc(admin,'system_config','streamer_live'),{entries:[{userId:'s2',publicationId:counterId,channelName:'Coach',channelUrl:'https://twitch.tv/coach_test'}]}));
    await assertSucceeds(setDoc(doc(admin,'streamer_click_metrics',counterId),{userId:'s2',publicationId:counterId,submittedAtMillis:Date.now(),clickCount:0}));
    await assertSucceeds(updateDoc(doc(guest,'streamer_click_metrics',counterId),{clickCount:increment(1),lastClickedAt:serverTimestamp()}));
    await Promise.all([user,other,guest].map(viewer => assertSucceeds(updateDoc(doc(viewer,'streamer_click_metrics',counterId),{clickCount:increment(1),lastClickedAt:serverTimestamp()}))));
    assert.equal((await getDoc(doc(owner,'streamer_click_metrics',counterId))).data().clickCount,4);
    assert.equal((await getDoc(doc(db('s2'),'streamer_click_metrics',counterId))).data().clickCount,4);
    await assertSucceeds(getDocs(query(collection(owner,'streamer_click_metrics'),where('userId','==','s2'))));
    await assertFails(getDoc(doc(guest,'streamer_click_metrics',counterId)));
    await assertFails(getDoc(doc(other,'streamer_click_metrics',counterId)));
  });
  await test('durable guest click retries and concurrent devices count every event once',async()=>{
    const guest=env.unauthenticatedContext().firestore(), owner=db('s2');
    async function click(store,eventId) {
      const event=doc(store,`streamer_click_metrics/${counterId}/click_events/${eventId}`);
      if ((await getDoc(event)).exists()) return;
      const batch=writeBatch(store);
      batch.set(event,{publicationId:counterId,clickedAt:serverTimestamp(),deleteAt:Timestamp.fromMillis(Date.now()+8*86400000)});
      batch.update(doc(store,'streamer_click_metrics',counterId),{clickCount:increment(1),lastClickedAt:serverTimestamp(),lastClickId:eventId});
      try { await batch.commit(); } catch(error) { if (!(await getDoc(event)).exists()) throw error; }
    }
    await assertSucceeds(click(guest,'durable-first'));
    await assertSucceeds(click(guest,'durable-first'));
    await Promise.all([click(user,'concurrent-a'),click(guest,'concurrent-a'),click(other,'concurrent-b')]);
    assert.equal((await getDoc(doc(owner,'streamer_click_metrics',counterId))).data().clickCount,7);
    await assertFails(getDocs(collection(guest,`streamer_click_metrics/${counterId}/click_events`)));
    await assertFails(setDoc(doc(guest,`streamer_click_metrics/${counterId}/click_events/without-increment`),{publicationId:counterId,clickedAt:serverTimestamp(),deleteAt:Timestamp.fromMillis(Date.now()+86400000)}));
  });
  await test('redemption role removal blocks a request without deducting the balance',async()=>{
    const profile=doc(admin,'users/economy'), before=(await getDoc(profile)).data().orangeEssence;
    await assertSucceeds(updateDoc(profile,{role:'free',secondaryRole:''}));
    await assertFails(redeem('role-removed-cash',10));
    assert.equal((await getDoc(profile)).data().orangeEssence,before);
    await assertSucceeds(updateDoc(profile,{role:'creador',secondaryRole:'streamer'}));
  });
  await test('visitors cannot forge counters reset counts alter owners or count an inactive publication', async () => {
    const guest = env.unauthenticatedContext().firestore();
    await assertFails(setDoc(doc(guest,'streamer_click_metrics','forged-counter'),{userId:'s2',publicationId:'forged-counter',submittedAtMillis:Date.now(),clickCount:0}));
    for (const clickCount of [0,-1,100]) await assertFails(updateDoc(doc(guest,'streamer_click_metrics',counterId),{clickCount,lastClickedAt:serverTimestamp()}));
    await assertFails(updateDoc(doc(guest,'streamer_click_metrics',counterId),{clickCount:increment(1),lastClickedAt:serverTimestamp(),userId:'other'}));
    await assertSucceeds(updateDoc(doc(admin,'system_config','streamer_live'),{entries:[]}));
    await assertFails(updateDoc(doc(guest,'streamer_click_metrics',counterId),{clickCount:increment(1),lastClickedAt:serverTimestamp()}));
  });
  await test('approval and durable guest clicks reach the owner live without resetting counters on retry', async () => {
    const uid='atomic-streamer', id='atomic-publication';
    await env.withSecurityRulesDisabled(async context => {
      const store=context.firestore();
      await setDoc(doc(store,`users/${uid}`),{role:'streamer'});
      await setDoc(doc(store,'system_config/streamer_live'),{entries:[]});
    });
    const owner=db(uid), guest=env.unauthenticatedContext().firestore();
    const pending={...request(uid),publicationId:id};
    const submit=writeBatch(owner);
    submit.set(doc(owner,`streamer_requests/${uid}`),pending);
    submit.set(doc(owner,`streamer_requests/${uid}/history/${id}`),pending);
    await assertSucceeds(submit.commit());
    const seen=[];
    const unsubscribe=onSnapshot(query(collection(owner,'streamer_click_metrics'),where('userId','==',uid)),snap=>snap.docs.forEach(row=>seen.push(row.data().clickCount)));
    async function approve() {
      await runTransaction(admin,async tx=>{
        const ref=doc(admin,`streamer_requests/${uid}`), registry=doc(admin,'system_config/streamer_live'), metric=doc(admin,`streamer_click_metrics/${id}`);
        const data=(await tx.get(ref)).data(), live=(await tx.get(registry)).data().entries, counter=await tx.get(metric);
        if(data.status==='APPROVED'&&live.some(row=>row.publicationId===id)&&counter.exists()) return;
        assert.equal(data.status,'PENDING');
        const reviewed=Date.now();
        tx.set(registry,{entries:[...live,{userId:uid,publicationId:id,channelName:data.channelName,channelUrl:data.channelUrl,platform:data.platform,approvedAtMillis:reviewed}]},{merge:true});
        if(!counter.exists()) tx.set(metric,{userId:uid,publicationId:id,submittedAtMillis:data.submittedAtMillis,clickCount:0,status:'APPROVED'});
        tx.update(ref,{status:'APPROVED',verifiedUsingCoach:true,reviewedAtMillis:reviewed});
        tx.set(doc(admin,`streamer_requests/${uid}/history/${id}`),{...data,status:'APPROVED',verifiedUsingCoach:true,reviewedAtMillis:reviewed});
        tx.set(doc(admin,`users/${uid}/messages/streamer_review_${id}`),{id:`streamer_review_${id}`,title:'Publicación de streamer',content:'Tu publicación fue aceptada y el canal ya está visible.',tag:'GENERAL',panel:'STREAMER',timestamp:reviewed,isRead:false});
      });
    }
    async function click(eventId) {
      const event=doc(guest,`streamer_click_metrics/${id}/click_events/${eventId}`);
      if((await getDoc(event)).exists()) return;
      const batch=writeBatch(guest);
      batch.set(event,{publicationId:id,clickedAt:serverTimestamp(),deleteAt:Timestamp.fromMillis(Date.now()+8*86400000)});
      batch.update(doc(guest,`streamer_click_metrics/${id}`),{clickCount:increment(1),lastClickId:eventId,lastClickedAt:serverTimestamp()});
      await batch.commit();
    }
    try {
      await assertSucceeds(approve());
      assert.equal((await getDoc(doc(owner,`streamer_requests/${uid}/history/${id}`))).data().status,'APPROVED');
      assert.equal((await getDoc(doc(owner,`users/${uid}/messages/streamer_review_${id}`))).data().isRead,false);
      assert.equal((await getDoc(doc(guest,'system_config/streamer_live'))).data().entries[0].publicationId,id);
      await assertSucceeds(click('first-open'));
      await assertSucceeds(click('first-open'));
      await assertSucceeds(click('second-open'));
      await assertSucceeds(approve());
      assert.equal((await getDoc(doc(owner,`streamer_click_metrics/${id}`))).data().clickCount,2);
      await new Promise((resolve,reject)=>{
        const timer=setTimeout(()=>{clearInterval(poll);reject(Error('Owner listener missed the live count'));},5000);
        const poll=setInterval(()=>{if(seen.includes(2)){clearInterval(poll);clearTimeout(timer);resolve();}},20);
      });
      assert.ok(seen.includes(0));
    } finally {unsubscribe();}
  });
  await test('72 hour retention permits only expired history and counter deletion by owner or staff', async () => {
    const oldId = 'publication-72-hours-old', oldDate = Date.now()-259200000-1000;
    const data = { ...request('s2'),status:'ENDED',endedAtMillis:oldDate,submittedAtMillis:oldDate,submittedAt:Timestamp.fromMillis(oldDate),publicationId:oldId };
    await env.withSecurityRulesDisabled(async context => {
      const store = context.firestore();
      await setDoc(doc(store,`streamer_requests/s2/history/${oldId}`),data);
      await setDoc(doc(store,'streamer_click_metrics',oldId),{userId:'s2',publicationId:oldId,submittedAtMillis:oldDate,endedAtMillis:oldDate,status:'ENDED',clickCount:8});
    });
    await assertFails(deleteDoc(doc(db('s2'),'streamer_click_metrics',counterId)));
    await assertFails(deleteDoc(doc(other,`streamer_requests/s2/history/${oldId}`)));
    await assertFails(deleteDoc(doc(other,'streamer_click_metrics',oldId)));
    const owner = db('s2');
    const batch = writeBatch(owner);
    batch.delete(doc(owner,`streamer_requests/s2/history/${oldId}`));
    batch.delete(doc(owner,'streamer_click_metrics',oldId));
    await assertSucceeds(batch.commit());
    assert.equal((await getDoc(doc(db('s2'),`streamer_requests/s2/history/${oldId}`))).exists(),false);
    await assertFails(setDoc(doc(admin,`streamer_requests/s2/history/${oldId}`),data));
    await assertFails(setDoc(doc(admin,'streamer_click_metrics',oldId),{userId:'s2',publicationId:oldId,submittedAtMillis:oldDate,clickCount:0}));
  });
  await test('retention metadata cannot extend the 24 hour pending deadline', async () => {
    const owner = db('s2'), data = request('s2');
    await assertFails(setDoc(doc(owner,'streamer_requests','s2'),{...data,streamerHistoryDeleteAt:Timestamp.fromMillis(data.submittedAtMillis+8*86400000)}));
    await assertSucceeds(setDoc(doc(owner,'streamer_requests','s2'),{...data,streamerHistoryDeleteAt:Timestamp.fromMillis(data.submittedAtMillis+86400000)}));
    await assertFails(setDoc(doc(admin,'streamer_click_metrics','publication-wrong-deadline'),{userId:'s2',publicationId:'publication-wrong-deadline',submittedAtMillis:data.submittedAtMillis,clickCount:0,streamerHistoryDeleteAt:Timestamp.fromMillis(data.submittedAtMillis+8*86400000)}));
  });
  await test('an active publication older than 72 hours still accepts guest clicks and cannot be pruned', async () => {
    const id='publication-still-active', submitted=Date.now()-9*86400000;
    await assertSucceeds(setDoc(doc(admin,'system_config','streamer_live'),{entries:[{userId:'s2',publicationId:id,channelName:'Coach',channelUrl:'https://twitch.tv/coach_test'}]}));
    await assertSucceeds(setDoc(doc(admin,'streamer_click_metrics',id),{userId:'s2',publicationId:id,submittedAtMillis:submitted,status:'APPROVED',clickCount:0}));
    await assertSucceeds(updateDoc(doc(env.unauthenticatedContext().firestore(),'streamer_click_metrics',id),{clickCount:increment(1),lastClickedAt:serverTimestamp()}));
    await assertFails(deleteDoc(doc(db('s2'),'streamer_click_metrics',id)));
    const endedAt=Date.now(), deadline=Timestamp.fromMillis(endedAt+72*60*60*1000);
    await assertSucceeds(setDoc(doc(admin,'streamer_requests','s2'),{userId:'s2',publicationId:id,submittedAtMillis:submitted,status:'APPROVED'}));
    const owner=db('s2'), batch=writeBatch(owner);
    batch.update(doc(owner,'streamer_requests','s2'),{status:'ENDED',endedAtMillis:endedAt,streamerHistoryDeleteAt:deadline});
    batch.update(doc(owner,'streamer_click_metrics',id),{status:'ENDED',endedAtMillis:endedAt,streamerHistoryDeleteAt:deadline});
    batch.update(doc(owner,'system_config','streamer_live'),{entries:[]});
    await assertSucceeds(batch.commit());
    assert.equal((await getDoc(doc(owner,'streamer_click_metrics',id))).data().clickCount,1);
    await assertFails(deleteDoc(doc(owner,'streamer_click_metrics',id)));
  });
  await test('pending history can be pruned only at 24 hours', async () => {
    const owner=db('s2'), submitted=Date.now()-86400000-1000, id='publication-pending-24h';
    await env.withSecurityRulesDisabled(async context => {
      await setDoc(doc(context.firestore(),`streamer_requests/s2/history/${id}`),{userId:'s2',publicationId:id,submittedAtMillis:submitted,status:'PENDING'});
    });
    await assertSucceeds(deleteDoc(doc(owner,`streamer_requests/s2/history/${id}`)));
  });
  await test('administrator grants Premium with access deadline and history atomically', async () => {
    await env.withSecurityRulesDisabled(async context => setDoc(doc(context.firestore(),'users','gift-user'),{role:'free', registeredDevices:[]}));
    const ref=doc(admin,'users','gift-user'), timestamp=Date.now();
    const gift={id:'gift-one',timestamp,durationMillis:86400000,source:'ADMIN_GIFT',amount:'Regalo'};
    await assertSucceeds(runTransaction(admin, async transaction => {
      const account=await transaction.get(ref);
      transaction.update(ref,{premiumUntil:timestamp+86400000,subscriptionPlan:'ADMIN_GIFT',lastModifiedByAdmin:timestamp,subscriptionHistory:[...(account.data().subscriptionHistory||[]),gift]});
    }));
    const own=db('gift-user'), data=(await assertSucceeds(getDoc(doc(own,'users','gift-user')))).data();
    assert.equal(data.role,'free'); assert.equal(data.subscriptionHistory.length,1);
    await assertFails(updateDoc(doc(own,'users','gift-user'),{subscriptionHistory:[]}));
    await assertFails(updateDoc(doc(own,'users','gift-user'),{subscriptionPlan:'Admin Grant (100 days)',premiumUntil:timestamp+8640000000}));
    await assertFails(updateDoc(doc(moderator,'users','gift-user'),{subscriptionHistory:[]}));
    await assertSucceeds(updateDoc(ref,{subscriptionHistory:[gift,{...gift,id:'gift-two'}]}));
    assert.equal((await getDoc(doc(own,'users','gift-user'))).data().subscriptionHistory.length,2);
  });
  await test('timed Premium extensions preserve two occupied roles and owners cannot grant themselves time', async () => {
    const inherited=Date.parse('2030-11-18T19:27:00Z'), ref=doc(admin,'users','occupied-gift-user');
    await env.withSecurityRulesDisabled(async context => setDoc(doc(context.firestore(),'users','occupied-gift-user'),
      {role:'creador',secondaryRole:'streamer',premiumUntil:inherited,registeredDevices:[]}));
    let expected=inherited;
    for (const days of [1,7,30,90,365]) {
      const timestamp=Date.now(), gift={id:`duration-${days}`,timestamp,durationMillis:days*86400000,source:'ADMIN_GIFT'};
      await assertSucceeds(runTransaction(admin, async transaction => {
        const data=(await transaction.get(ref)).data();
        transaction.update(ref,{premiumUntil:data.premiumUntil+days*86400000,subscriptionPlan:'ADMIN_GIFT',
          lastModifiedByAdmin:timestamp,subscriptionHistory:[...(data.subscriptionHistory||[]),gift]});
      }));
      expected+=days*86400000;
      const data=(await getDoc(ref)).data();
      assert.equal(data.premiumUntil,expected); assert.equal(data.role,'creador'); assert.equal(data.secondaryRole,'streamer');
    }
    assert.equal((await getDoc(ref)).data().subscriptionHistory.length,5);
    await assertFails(updateDoc(doc(db('occupied-gift-user'),'users','occupied-gift-user'),{premiumUntil:expected+86400000}));
    await assertFails(updateDoc(doc(moderator,'users','occupied-gift-user'),{premiumUntil:expected+86400000}));
  });

  await test('own panel reads persist without granting roles or acknowledging another user', async () => {
    await assertSucceeds(updateDoc(doc(user,'users/user'),{panelReadKeys:['sponsor-seen']}));
    assert.deepEqual((await getDoc(doc(user,'users/user'))).data().panelReadKeys,['sponsor-seen']);
    await assertFails(updateDoc(doc(other,'users/user'),{panelReadKeys:['stolen']}));
    await assertFails(updateDoc(doc(user,'users/user'),{panelReadKeys:[],role:'admin'}));
  });
  await test('essence gifts save balances and receipts together and cannot be self awarded', async () => {
    const ref=doc(admin,'users/user'), receipt={id:'actual-gift',timestamp:Date.now(),durationMillis:0,planName:'Regalo de Esencias',amount:'+25 EN',source:'ADMIN_ESSENCE_ADJUSTMENT'};
    await assertSucceeds(runTransaction(admin,async tx=>{await tx.get(ref);tx.update(ref,{orangeEssence:25,subscriptionHistory:[receipt]});}));
    const own=(await getDoc(doc(user,'users/user'))).data(); assert.equal(own.orangeEssence,25);assert.equal(own.subscriptionHistory[0].amount,'+25 EN');
    await assertSucceeds(setDoc(doc(admin,'users/user/subscription_history/admin-gift'),receipt));
    await assertFails(setDoc(doc(user,'users/user/subscription_history/self-gift'),receipt));
    await assertFails(updateDoc(doc(user,'users/user'),{orangeEssence:50,subscriptionHistory:[receipt]}));
  });
  await test('streamer approval commits publication counter history and notification atomically',async()=>{
    const uid='review-atomic',id='publication-review-atomic';
    await env.withSecurityRulesDisabled(async context=>{
      const store=context.firestore();
      await setDoc(doc(store,`users/${uid}`),{role:'streamer',registeredDevices:[]});
      await setDoc(doc(store,`streamer_requests/${uid}`),{userId:uid,publicationId:id,channelName:'Coach Test',channelUrl:'https://twitch.tv/coach_test',platform:'Twitch',status:'PENDING',usingCoachAcknowledged:true,submittedAtMillis:Date.now()});
      await setDoc(doc(store,'system_config/streamer_live'),{entries:[]});
    });
    async function approve(badCounter=false){
      return runTransaction(admin,async tx=>{
        const req=doc(admin,`streamer_requests/${uid}`), registry=doc(admin,'system_config/streamer_live'),metric=doc(admin,`streamer_click_metrics/${id}`);
        const request=await tx.get(req),live=await tx.get(registry),counter=await tx.get(metric);
        if(request.data().status==='APPROVED' && live.data().entries.some(entry=>entry.publicationId===id))return;
        const entry={userId:uid,publicationId:id,channelName:request.data().channelName,channelUrl:request.data().channelUrl,platform:'Twitch',approvedAtMillis:Date.now()};
        if(!counter.exists())tx.set(metric,{userId:uid,publicationId:id,submittedAtMillis:request.data().submittedAtMillis,clickCount:badCounter?-1:0,status:'APPROVED'});
        tx.set(registry,{entries:[entry]});
        const reviewed={...request.data(),status:'APPROVED',verifiedUsingCoach:true,reviewedAtMillis:Date.now()};
        tx.set(req,reviewed);
        tx.set(doc(admin,`streamer_requests/${uid}/history/${id}`),reviewed);
        tx.set(doc(admin,`users/${uid}/messages/streamer_review_${id}`),{panel:'STREAMER',isRead:false,timestamp:Date.now()});
      });
    }
    await assertFails(approve(true));
    assert.equal((await getDoc(doc(admin,`streamer_requests/${uid}`))).data().status,'PENDING');
    assert.equal((await getDoc(doc(admin,'system_config/streamer_live'))).data().entries.length,0);
    await assertSucceeds(approve());
    assert.equal((await getDoc(doc(admin,`streamer_click_metrics/${id}`))).data().clickCount,0);
    const guest=env.unauthenticatedContext().firestore();
    await assertSucceeds(updateDoc(doc(guest,`streamer_click_metrics/${id}`),{clickCount:increment(1),lastClickedAt:serverTimestamp()}));
    await assertSucceeds(approve());
    assert.equal((await getDoc(doc(db(uid),`streamer_click_metrics/${id}`))).data().clickCount,1);
    assert.equal((await getDoc(doc(db(uid),`streamer_requests/${uid}/history/${id}`))).data().status,'APPROVED');
    assert.equal((await getDoc(doc(db(uid),`users/${uid}/messages/streamer_review_${id}`))).data().isRead,false);
  });
  await test('authorized direct-parent statistics include inbox history and publication records', async()=>{
    await env.withSecurityRulesDisabled(async context=>{
      const store=context.firestore();
      await setDoc(doc(store,'users/stats-target'),{role:'free',blueEssence:0,orangeEssence:100});
      await setDoc(doc(store,'users/stats-target/messages/inbox-one'),{title:'Mensaje'});
      await setDoc(doc(store,'users/stats-target/subscription_history/gift-one'),{amount:'+100 EN',source:'ADMIN_ESSENCE_ADJUSTMENT'});
      await setDoc(doc(store,'users/stats-target/subscription_history/removed-one'),{source:'ADMIN_REVOCATION',durationMillis:0});
      await setDoc(doc(store,'streamer_requests/stats-target'),{userId:'stats-target',status:'PENDING'});
      await setDoc(doc(store,'streamer_requests/stats-target/history/publication-one'),{status:'ENDED'});
    });
    for(const [path,expected] of [['users/stats-target/messages',1],['users/stats-target/subscription_history',2],['streamer_requests/stats-target/history',1]]) {
      const rows=await assertSucceeds(getDocs(collection(admin,path)));
      assert.equal(rows.size,expected);
      const aggregate=await assertSucceeds(getCountFromServer(collection(admin,path)));
      assert.equal(aggregate.data().count,expected);
      await assertFails(getDocs(collection(moderator,path)));
    }
    const target=(await assertSucceeds(getDoc(doc(admin,'users/stats-target')))).data();
    assert.equal(target.blueEssence,0);assert.equal(target.orangeEssence,100);
  });
  console.log(`${count} rule scenarios passed`);
} finally { await env.cleanup(); }
