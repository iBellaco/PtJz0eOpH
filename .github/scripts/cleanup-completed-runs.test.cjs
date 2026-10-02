const assert = require('node:assert/strict');
const cleanup = require('./cleanup-completed-runs.cjs');
(async () => {
  const runs = Array.from({ length: 120 }, (_, i) => ({ id: i + 1, created_at: new Date(i * 1000).toISOString() })).reverse();
  const deleted = [];
  const context = { repo: { owner: 'owner', repo: 'repo' }, runId: 999 };
  const api = { listWorkflowRunsForRepo() {}, async deleteWorkflowRun({ run_id }) { deleted.push(run_id); } };
  const github = { rest: { actions: api }, async paginate(fn, params) {
    assert.equal(fn, api.listWorkflowRunsForRepo);
    assert.equal(params.status, 'completed');
    assert.equal(params.per_page, 100);
    return runs;
  } };
  await cleanup({ github, context, core: { info() {} } });
  assert.equal(deleted.length, 106);
  assert.deepEqual(deleted, Array.from({ length: 106 }, (_, i) => 106 - i));
  deleted.length = 0;
  await cleanup({ github: { ...github, paginate: async () => runs.slice(0, 10) }, context, core: { info() {} } });
  assert.equal(deleted.length, 0);
  console.log('PASS repository-wide retention and short histories');
})().catch(error => { console.error(error); process.exitCode = 1; });
