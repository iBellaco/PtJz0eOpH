/** Synthetic account attributes and attachment recovery, strictly on all three local emulators. */
import assert from 'node:assert/strict';
import {mkdtemp, writeFile, readFile, mkdir} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {createHash} from 'node:crypto';
for (const key of ['FIRESTORE_EMULATOR_HOST','FIREBASE_AUTH_EMULATOR_HOST','FIREBASE_STORAGE_EMULATOR_HOST']) {
  if (!/^127\.0\.0\.1:\d+$/.test(process.env[key] || '')) throw new Error('Loopback emulators required');
}
const {auth, db, PROJECT} = await import('../store.mjs');
const {getStorage} = await import('firebase-admin/storage');
assert.equal(PROJECT, 'demo-coach-tests');
const uid = 'recovery-media-fixture-owner';
const root = `recovery_drill_fixture/${uid}`;
const attachment = getStorage().bucket('demo-coach-tests.appspot.com').file(`${root}/avatar.bin`);
const profile = {uid, email:'recovery-fixture@example.invalid', displayName:'Coach ensayo', emailVerified:true, disabled:false};
const claims = {coachFixture:true};
const bytes = Buffer.from([0,1,2,3,255,254]);
const digest = value => createHash('sha256').update(value).digest('hex');
async function cleanup() {
  await attachment.delete({ignoreNotFound:true});
  await db.recursiveDelete(db.doc(root));
  try { await auth.deleteUser(uid); } catch(error) { if (error.code !== 'auth/user-not-found') throw error; }
}
// The fixed fixture scope is intentionally the only state this drill can replace.
try {
  await auth.createUser(profile);
  await auth.setCustomUserClaims(uid, claims);
  await db.doc(root).set({owner:uid, attachment:attachment.name});
  await attachment.save(bytes, {resumable:false, metadata:{contentType:'application/octet-stream'}});
  const account = await auth.getUser(uid);
  const snapshot = {schema:1, project:PROJECT, identity:{uid:account.uid,email:account.email,
    displayName:account.displayName,emailVerified:account.emailVerified,disabled:account.disabled},
    claims:account.customClaims, document:(await db.doc(root).get()).data(),
    attachment:{path:attachment.name,base64:(await attachment.download())[0].toString('base64'),sha256:digest(bytes)}};
  const directory = await mkdtemp(join(tmpdir(),'coach-recovery-media-'));
  const file = join(directory,'snapshot.json');
  const serialized = JSON.stringify(snapshot);
  await writeFile(file, serialized, {mode:0o600});
  const restored = await readFile(file, 'utf8');
  assert.equal(digest(restored), digest(serialized));
  const copy = JSON.parse(restored);
  assert.equal(copy.project, PROJECT);
  assert.equal(copy.identity.uid, uid);
  assert.equal(copy.attachment.path, attachment.name);
  await cleanup();
  await assert.rejects(auth.getUser(uid), {code:'auth/user-not-found'});
  assert.equal((await attachment.exists())[0], false);
  await auth.createUser(copy.identity);
  await auth.setCustomUserClaims(uid, copy.claims);
  await db.doc(root).set(copy.document);
  await attachment.save(Buffer.from(copy.attachment.base64,'base64'), {resumable:false, metadata:{contentType:'application/octet-stream'}});
  const result = await auth.getUser(uid);
  assert.equal(result.email, profile.email);
  assert.equal(result.emailVerified, true);
  assert.equal(result.disabled, false);
  assert.deepEqual(result.customClaims, claims);
  assert.deepEqual((await db.doc(root).get()).data(), snapshot.document);
  assert.equal(digest((await attachment.download())[0]), snapshot.attachment.sha256);
  const output = new URL('../../../app/build/reports/recovery/', import.meta.url);
  await mkdir(output, {recursive:true});
  await writeFile(new URL('emulator-media-drill.json', output), JSON.stringify({
    status:'PASS', evidenceType:'isolated-emulators-synthetic-fixtures',
    verified:['document-links','account-attributes','verified-email-attribute','custom-claims','attachment-bytes-sha256'],
    productionRestoreVerified:false, passwordHashRestoreVerified:false, billingActivated:false,
  },null,2));
  console.log('RECOVERY_MEDIA_DRILL: synthetic identity attributes, document links and attachment bytes restored; passwords and production recovery remain unverified.');
} finally { await cleanup(); }
process.exit(0);
