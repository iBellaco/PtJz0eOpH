import test from 'node:test';
import assert from 'node:assert/strict';
import {mediaObject, mediaObjects, legacyExternalMedia} from '../media.mjs';
test('only uploaded notice files in Coach buckets can enter a deletion plan',()=>{
  const url='https://firebasestorage.googleapis.com/v0/b/wild-rift-drafting.firebasestorage.app/o/notice_videos%2Fowned.mp4?token=private';
  assert.deepEqual(mediaObject(url),{bucket:'wild-rift-drafting.firebasestorage.app',path:'notice_videos/owned.mp4'});
  for(const unsafe of [url.replace('wild-rift-drafting.firebasestorage.app','other.appspot.com'),url.replace('notice_videos%2Fowned.mp4','credentials.json'),url.replace('owned.mp4','..%2Fprivate'),url.replace('https:','http:'),'file:///tmp/anything']) assert.equal(mediaObject(unsafe),null);
  assert.equal(mediaObjects({videoUrl:url,externalUrl:'https://other.invalid'}).length,1);
});
test('legacy anonymous uploads require withdrawal rather than false completion',()=>{
  assert.deepEqual(legacyExternalMedia({videoUrl:'https://files.catbox.moe/old.mp4'}),['https://files.catbox.moe/old.mp4']);
  assert.deepEqual(legacyExternalMedia({videoUrl:'https://tmpfiles.org/dl/123/old.mp4'}),['https://tmpfiles.org/dl/123/old.mp4']);
  assert.deepEqual(legacyExternalMedia({videoUrl:'https://www.youtube.com/watch?v=external'}),[]);
});
