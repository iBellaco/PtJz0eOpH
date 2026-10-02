import { readFileSync } from 'node:fs';
import assert from 'node:assert/strict';
import { initializeTestEnvironment, assertSucceeds, assertFails } from '@firebase/rules-unit-testing';
import { doc, setDoc, getDoc, updateDoc, deleteDoc, getDocs, collection, query, where, runTransaction, writeBatch, serverTimestamp, Timestamp } from 'firebase/firestore';
const env = await initializeTestEnvironment({ projectId: 'demo-coach-tests', firestore: { host: '127.0.0.1', port: 8080, rules: readFileSync(new URL('../../firestore.rules', import.meta.url), 'utf8') } });
const db = uid => env.authenticatedContext(uid, { email: `${uid}@test.invalid` }).firestore();
const user = db('user'), moderator = db('mod'), admin = db('admin'), other = db('other'), streamer = db('s1');
const greeting = 'Hola. El sistema ha recibido tu mensaje. El equipo de Coach te responderá aquí.';
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
  console.log(`${count} rule scenarios passed`);
} finally { await env.cleanup(); }
