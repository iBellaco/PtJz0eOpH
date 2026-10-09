import {readFileSync} from 'node:fs';
import {initializeTestEnvironment,assertSucceeds,assertFails} from '@firebase/rules-unit-testing';
import {ref,uploadBytes,updateMetadata,deleteObject,getMetadata} from 'firebase/storage';
import {doc,setDoc,updateDoc} from 'firebase/firestore';
if(!process.env.FIREBASE_STORAGE_EMULATOR_HOST)throw new Error('Storage emulator is required; production access is prohibited.');
const env=await initializeTestEnvironment({projectId:'demo-coach-tests',
  firestore:{host:'127.0.0.1',port:8088,rules:readFileSync(new URL('../../firestore.rules',import.meta.url),'utf8')},
  storage:{host:'127.0.0.1',port:9199,rules:readFileSync(new URL('../../storage.rules',import.meta.url),'utf8')}});
try {
  await env.withSecurityRulesDisabled(async context=>{
    const store=context.firestore();
    await setDoc(doc(store,'users/video-owner'),{role:'patrocinador',banned:false});
    await setDoc(doc(store,'users/video-other'),{role:'patrocinador',banned:false});
  });
  const owner=env.authenticatedContext('video-owner',{email:'video-owner@test.invalid'}).storage();
  const other=env.authenticatedContext('video-other',{email:'video-other@test.invalid'}).storage();
  const guest=env.authenticatedContext('video-guest',{email:'coach.guest.reader@gmail.com'}).storage();
  const data=new Uint8Array([1,2,3]),metadata={contentType:'video/mp4',customMetadata:{uploaderUid:'video-owner'}};
  const file=ref(owner,'notice_videos/video-owner/test.mp4');
  await assertSucceeds(uploadBytes(file,data,metadata));
  await assertFails(uploadBytes(ref(owner,'notice_videos/video-owner/too-large.mp4'),new Uint8Array(20*1024*1024+1),metadata));
  await env.withSecurityRulesDisabled(async context=>updateDoc(doc(context.firestore(),'users/video-owner'),{banned:true}));
  await assertFails(uploadBytes(ref(owner,'notice_videos/video-owner/suspended.mp4'),data,metadata));
  await env.withSecurityRulesDisabled(async context=>updateDoc(doc(context.firestore(),'users/video-owner'),{banned:false}));
  await assertSucceeds(getMetadata(file));
  console.log('PASS media upload binds account, path and owner');
  await assertFails(uploadBytes(ref(other,'notice_videos/video-owner/forged.mp4'),data,metadata));
  await assertFails(uploadBytes(ref(owner,'notice_videos/video-owner/mismatched.mp4'),data,{contentType:'video/mp4',customMetadata:{uploaderUid:'video-other'}}));
  console.log('PASS another account cannot forge media ownership');
  await assertFails(updateMetadata(file,{customMetadata:{uploaderUid:'video-other'}}));
  await assertFails(uploadBytes(file,data,{contentType:'video/mp4',customMetadata:{uploaderUid:'video-other'}}));
  await assertSucceeds(uploadBytes(file,data,metadata));
  await assertFails(deleteObject(file));
  console.log('PASS uploaded owner is immutable and erasure is privileged');
  await assertFails(uploadBytes(ref(guest,'notice_videos/video-guest/guest.mp4'),data,{contentType:'video/mp4',customMetadata:{uploaderUid:'video-guest'}}));
  await assertFails(uploadBytes(ref(owner,'other-private-folder/file.mp4'),data,metadata));
  console.log('PASS shared guest and unrelated media paths cannot be written');
} finally {await env.cleanup();}
process.exit(0);
