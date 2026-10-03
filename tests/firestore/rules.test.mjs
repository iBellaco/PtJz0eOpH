import { readFileSync } from 'node:fs';
import assert from 'node:assert/strict';
import { initializeTestEnvironment, assertSucceeds, assertFails } from '@firebase/rules-unit-testing';
import { doc, setDoc, getDoc, updateDoc, deleteDoc, getDocs, collection, query, where, runTransaction, writeBatch, serverTimestamp, Timestamp, increment } from 'firebase/firestore';
const env = await initializeTestEnvironment({ projectId: 'demo-coach-tests', firestore: { host: '127.0.0.1', port: 8080, rules: readFileSync(new URL('../../firestore.rules', import.meta.url), 'utf8') } });
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
  await test('visitors cannot forge counters reset counts alter owners or count an inactive publication', async () => {
    const guest = env.unauthenticatedContext().firestore();
    await assertFails(setDoc(doc(guest,'streamer_click_metrics','forged-counter'),{userId:'s2',publicationId:'forged-counter',submittedAtMillis:Date.now(),clickCount:0}));
    for (const clickCount of [0,-1,100]) await assertFails(updateDoc(doc(guest,'streamer_click_metrics',counterId),{clickCount,lastClickedAt:serverTimestamp()}));
    await assertFails(updateDoc(doc(guest,'streamer_click_metrics',counterId),{clickCount:increment(1),lastClickedAt:serverTimestamp(),userId:'other'}));
    await assertSucceeds(updateDoc(doc(admin,'system_config','streamer_live'),{entries:[]}));
    await assertFails(updateDoc(doc(guest,'streamer_click_metrics',counterId),{clickCount:increment(1),lastClickedAt:serverTimestamp()}));
  });
  await test('seven day retention permits only expired history and counter deletion by owner or staff', async () => {
    const oldId = 'publication-seven-days-old', oldDate = Date.now()-604800000-1000;
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
  await test('an active publication older than seven days still accepts guest clicks and cannot be pruned', async () => {
    const id='publication-still-active', submitted=Date.now()-9*86400000;
    await assertSucceeds(setDoc(doc(admin,'system_config','streamer_live'),{entries:[{userId:'s2',publicationId:id,channelName:'Coach',channelUrl:'https://twitch.tv/coach_test'}]}));
    await assertSucceeds(setDoc(doc(admin,'streamer_click_metrics',id),{userId:'s2',publicationId:id,submittedAtMillis:submitted,status:'APPROVED',clickCount:0}));
    await assertSucceeds(updateDoc(doc(env.unauthenticatedContext().firestore(),'streamer_click_metrics',id),{clickCount:increment(1),lastClickedAt:serverTimestamp()}));
    await assertFails(deleteDoc(doc(db('s2'),'streamer_click_metrics',id)));
    const endedAt=Date.now(), deadline=Timestamp.fromMillis(endedAt+7*86400000);
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
  console.log(`${count} rule scenarios passed`);
} finally { await env.cleanup(); }
