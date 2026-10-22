import test from 'node:test';
import assert from 'node:assert/strict';
import {classify, inventory} from '../backup-status.mjs';

test('permission failure is unknown, never mistaken for absent or successful backups', () => {
  const denied = {status:403,body:{}};
  const report = classify(denied, denied, denied);
  assert.equal(report.backupStatus,'UNKNOWN'); assert.equal(report.scheduleStatus,'UNKNOWN');
});
test('only READY backups establish available recovery data', () => {
  const ok = body => ({status:200,body});
  const pending = ok({backups:[{state:'CREATING'}]});
  assert.equal(classify(ok({}),ok({}),pending).backupStatus,'NONE');
  const ready = classify(ok({pointInTimeRecoveryEnablement:'POINT_IN_TIME_RECOVERY_ENABLED'}),
    ok({backupSchedules:[{}]}),ok({backups:[{state:'READY',database:'projects/test/databases/(default)',snapshotTime:'2026-10-22T00:00:00Z'}]}));
  assert.equal(ready.backupStatus,'AVAILABLE'); assert.equal(ready.scheduleStatus,'CONFIGURED');
  assert.equal(classify(ok({}),ok({}),ok({backups:[{state:'READY',database:'projects/test/databases/other'}]})).backupStatus,'NONE');
});
test('inventory only issues read requests and reports transport failures', async () => {
  const calls=[];
  const report=await inventory('test',async url=>{calls.push(url);throw new Error('offline');});
  assert.equal(calls.length,3); assert.equal(report.backupStatus,'UNKNOWN');
});
