import test from 'node:test';
import assert from 'node:assert/strict';
import {conversationCleanup} from '../conversations.mjs';
const row=(senderUid,senderRole,text)=>({senderUid,senderRole,text,senderName:senderUid});
test('erasure removes the former staff answer and stale caches while preserving the recipient text',()=>{
  const initial=row('recipient','USER','private question');
  const result=conversationCleanup({conversation:[initial,row('erased','SUPPORT','old answer')],adminReply:'old answer'},'erased','');
  assert.deepEqual(result.conversation,[initial]);
  assert.equal(result.adminReply,'');assert.equal(result.repliedBy,'');assert.equal(result.userCanReply,false);
});
test('another staff member and their cached answer remain intact',()=>{
  const recipient=row('recipient','USER','question'),answer=row('other-staff','SUPPORT','keep answer');
  const result=conversationCleanup({conversation:[recipient,row('erased','SUPPORT','erase'),answer]},'erased','');
  assert.deepEqual(result,{conversation:[recipient,answer]});
  assert.equal(conversationCleanup({conversation:[recipient,answer]},'erased',''),null);
});
test('a verifiable legacy email is removed without matching a merely identical name',()=>{
  const legacy={senderEmail:'owner@test.invalid',senderName:'same name',senderRole:'SUPPORT',text:'erase'};
  const other={senderUid:'other',senderName:'same name',senderRole:'USER',text:'keep'};
  assert.deepEqual(conversationCleanup({conversation:[legacy,other]},'owner','owner@test.invalid').conversation,[other]);
});
