/** Disk round-trip of typed documents and nested records, isolated to emulators. */
import assert from 'node:assert/strict';
import {mkdtemp, writeFile, readFile} from 'node:fs/promises';
import {tmpdir} from 'node:os';
import {join} from 'node:path';
import {createHash} from 'node:crypto';

const host = process.env.FIRESTORE_EMULATOR_HOST;
if (!/^127\.0\.0\.1:\d+$/.test(host || '')) throw new Error('Loopback emulator required');
const {db, PROJECT} = await import('../store.mjs');
assert.equal(PROJECT, 'demo-coach-tests');
const root = 'recovery_drill_fixture/owner';
const paths = [root, `${root}/history/receipt`];
const base = `http://${host}/v1/projects/demo-coach-tests/databases/(default)/documents/`;
const fields = {
  label: {stringValue:'Coach ensayo'}, count: {integerValue:'9007199254740991'},
  createdAt: {timestampValue:'2026-10-22T00:00:00.123456Z'}, enabled:{booleanValue:true},
  payload:{bytesValue:'AAEC'}, location:{geoPointValue:{latitude:12.3,longitude:-4.5}},
  nested:{mapValue:{fields:{values:{arrayValue:{values:[{nullValue:null},{doubleValue:1.25}]}}}}},
  reference:{referenceValue:'projects/demo-coach-tests/databases/(default)/documents/recovery_drill_fixture/other'},
};
async function request(path, options) {
  if (!paths.includes(path)) throw new Error('Fixture scope required');
  const response = await fetch(base + path, options);
  if (!response.ok) throw new Error(`Recovery fixture HTTP ${response.status}`);
  return response.json();
}
for (const path of paths) await request(path, {method:'PATCH', headers:{'Content-Type':'application/json'},body:JSON.stringify({fields})});
const documents = [];
for (const path of paths) documents.push({path, fields:(await request(path)).fields});
const directory = await mkdtemp(join(tmpdir(),'coach-recovery-'));
const file = join(directory,'snapshot.json');
const payload = JSON.stringify({schema:1,project:PROJECT,documents});
const digest = value => createHash('sha256').update(value).digest('hex');
await writeFile(file,payload,{mode:0o600});
await writeFile(join(directory,'snapshot.sha256'),digest(payload),{mode:0o600});
const stored = await readFile(file,'utf8');
assert.equal(digest(stored),await readFile(join(directory,'snapshot.sha256'),'utf8'));
const snapshot = JSON.parse(stored);
assert.equal(snapshot.project,'demo-coach-tests');
await db.recursiveDelete(db.doc(root));
for (const path of paths) assert.equal((await db.doc(path).get()).exists,false);
for (const document of snapshot.documents) await request(document.path, {
  method:'PATCH',headers:{'Content-Type':'application/json'},body:JSON.stringify({fields:document.fields}),
});
for (const document of snapshot.documents) assert.deepEqual((await request(document.path)).fields,document.fields);
await db.recursiveDelete(db.doc(root));
console.log('RECOVERY_DRILL: disk integrity and typed nested document restoration verified in isolated emulator; no production data restored.');
