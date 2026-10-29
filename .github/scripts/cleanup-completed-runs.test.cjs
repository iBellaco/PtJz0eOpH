const assert = require('node:assert/strict');
const cleanup = require('./cleanup-completed-runs.cjs');
(async () => {
  const runs = [
    ...Array.from({ length: 11 }, (_, i) => ({ id: i + 1, workflow_id: 101, name: 'Build and Release APK', created_at: new Date((i + 1) * 1000).toISOString() })),
    ...Array.from({ length: 11 }, (_, i) => ({ id: i + 21, workflow_id: 202, name: 'Private economy service', created_at: new Date((i + 21) * 1000).toISOString() }))
  ].reverse();
  const deleted = [];
  const api = { listWorkflowRunsForRepo() {}, async deleteWorkflowRun({ run_id }) { deleted.push(run_id); } };
  const context = { repo: { owner: 'owner', repo: 'repo' }, runId: 999, workflow: 'Build and Release APK' };
  const releaseDeleted = [];
  const releaseApi = { listReleases() {}, async deleteRelease({ release_id }) { releaseDeleted.push(release_id); } };
  const github = { rest: { actions: api, repos: releaseApi }, async paginate(fn, params) {
    assert.equal(params.per_page, 100);
    if (fn === releaseApi.listReleases) return [
      ...Array.from({ length: 40 }, (_, i) => ({ id: i + 1, draft: false, prerelease: false, published_at: new Date(i * 1000).toISOString() })),
      { id: require('../coach-signing.json').release_id, draft: true },
      { id: 1001, draft: true, created_at: new Date().toISOString() },
      { id: 1002, draft: true, created_at: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString() },
      { id: 1000, draft: false, prerelease: true, published_at: new Date(41000).toISOString() }
    ];
    assert.equal(fn, api.listWorkflowRunsForRepo);
    assert.equal(params.status, 'completed');
    return runs;
  } };
  await cleanup({ github, context, core: { info() {} } });
  assert.deepEqual(deleted.sort((a, b) => a - b), [1, 2, 21]);
  assert.equal(releaseDeleted.length, 13);
  assert.ok(!releaseDeleted.includes(require('../coach-signing.json').release_id));
  assert.ok(releaseDeleted.includes(1000));
  assert.ok(!releaseDeleted.includes(1001), 'uploading release remains intact');
  assert.ok(releaseDeleted.includes(1002), 'abandoned old draft is removed');
  assert.ok(deleted.includes(2));
  assert.ok(!deleted.includes(11));
  assert.ok(!deleted.includes(31));
  deleted.length = 0;
  await cleanup({ github: { ...github, paginate: async fn => fn === releaseApi.listReleases ? [] : runs.slice(0, 10) }, context, core: { info() {} } });
  assert.equal(deleted.length, 0);
  console.log('PASS per-workflow retention and short histories');
})().catch(error => { console.error(error); process.exitCode = 1; });
