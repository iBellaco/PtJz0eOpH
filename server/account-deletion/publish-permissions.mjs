import {readFile} from 'node:fs/promises';
import {createHash} from 'node:crypto';
import {initializeApp, applicationDefault} from 'firebase-admin/app';
import {getSecurityRules} from 'firebase-admin/security-rules';
import {buckets} from './media.mjs';

if (process.env.FIRESTORE_EMULATOR_HOST || process.env.FIREBASE_STORAGE_EMULATOR_HOST) {
  throw new Error('Permission publication requires the production connection, not an emulator');
}
initializeApp({credential: applicationDefault(), projectId: 'wild-rift-drafting'});
const rules = getSecurityRules();
const firestoreSource = await readFile(new URL('../../firestore.rules', import.meta.url), 'utf8');
const storageSource = await readFile(new URL('../../storage.rules', import.meta.url), 'utf8');

function verified(active, filename, expected) {
  if (active.source.length !== 1 || active.source[0].name !== filename || active.source[0].content !== expected) {
    throw new Error(`The active ${filename} does not match the verified repository source`);
  }
  return {ruleset: active.name, sha256: createHash('sha256').update(expected).digest('hex')};
}

// Publish directly through the supported Rules API. This does not enable services,
// create buckets, alter IAM, or require unrelated Service Usage discovery permissions.
await rules.releaseFirestoreRulesetFromSource(firestoreSource);
console.log('ACTIVE_ACCOUNT_PERMISSIONS:', JSON.stringify(verified(await rules.getFirestoreRuleset(), 'firestore.rules', firestoreSource)));
for (const bucket of buckets) {
  await rules.releaseStorageRulesetFromSource(storageSource, bucket);
  console.log('ACTIVE_MEDIA_PERMISSIONS:', JSON.stringify({bucket, ...verified(await rules.getStorageRuleset(bucket), 'storage.rules', storageSource)}));
}
