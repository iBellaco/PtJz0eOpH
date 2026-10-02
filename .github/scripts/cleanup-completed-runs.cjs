/** Keep a repository-wide history, reserving one slot for the run executing this job. */
module.exports = async ({ github, context, core }, limit = 15) => {
  const repo = { owner: context.repo.owner, repo: context.repo.repo };
  const runs = await github.paginate(github.rest.actions.listWorkflowRunsForRepo,
    { ...repo, status: 'completed', per_page: 100 });
  const current = Number(context.runId);
  const completed = runs.filter(run => run.id !== current)
    .sort((a, b) => new Date(b.created_at) - new Date(a.created_at) || b.id - a.id);
  let removed = 0;
  for (const run of completed.slice(limit - 1)) {
    try {
      await github.rest.actions.deleteWorkflowRun({ ...repo, run_id: run.id });
      removed++;
    } catch (error) {
      if (error.status !== 404) throw error;
    }
  }
  core.info(`Removed ${removed} completed runs; history limit: ${limit}.`);
};
