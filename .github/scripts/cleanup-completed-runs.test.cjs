const assert = require('node:assert/strict');
const cleanup = require('./cleanup-completed-runs.cjs');
(async () => {
  const runs = Array.from({ length: 120 }, (_, i) => ({ id: i + 1, name: i % 4 === 0 ? 'Build and Release APK' : 'Private economy service', created_at: new Date(i * 1000).toISOString() })).reverse();
  const deleted = [];
  const api = { listWorkflowRunsForRepo() {}, async deleteWorkflowRun({ run_id }) { deleted.push(run_id); } };
  const context = { repo: { owner: 'owner', repo: 'repo' }, runId: 999, workflow: 'Private economy service' };
  const releaseDeleted = [];
  const releaseApi = { listReleases() {}, async deleteRelease({ release_id }) { releaseDeleted.push(release_id); } };
  const github = { rest: { actions: api, repos: releaseApi }, async paginate(fn, params) {
    assert.equal(params.per_page, 100);
    if (fn === releaseApi.listReleases) return [
      ...Array.from({ length: 40 }, (_, i) => ({ id: i + 1, draft: false, prerelease: false, published_at: new Date(i * 1000).toISOString() })),
      { id: require('../coach-signing.json').release_id, draft: true },
      { id: 1000, draft: false, prerelease: true, published_at: new Date(41000).toISOString() }
    ];
    assert.equal(fn, api.listWorkflowRunsForRepo);
    assert.equal(params.status, 'completed');
    return runs;
  } };
  await cleanup({ github, context, core: { info() {} } });
  assert.equal(deleted.length, 91);
  assert.equal(releaseDeleted.length, 12);
  assert.ok(!releaseDeleted.includes(require('../coach-signing.json').release_id));
  assert.ok(releaseDeleted.includes(1000));
  assert.ok(deleted.includes(2));
  assert.ok(!deleted.includes(117));
  assert.ok(!deleted.includes(120));
  deleted.length = 0;
  await cleanup({ github: { ...github, paginate: async fn => fn === releaseApi.listReleases ? [] : runs.slice(0, 10) }, context, core: { info() {} } });
  assert.equal(deleted.length, 0);
  console.log('PASS repository-wide retention and short histories');
})().catch(error => { console.error(error); process.exitCode = 1; });
