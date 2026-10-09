/** Preserve delivery evidence separately from the high-frequency service runs. */
module.exports = async ({ github, context, core }, limit = 15) => {
  const repo = { owner: context.repo.owner, repo: context.repo.repo };
  const runs = await github.paginate(github.rest.actions.listWorkflowRunsForRepo,
    { ...repo, status: 'completed', per_page: 100 });
  const current = Number(context.runId);
  const completed = runs.filter(run => run.id !== current)
    .sort((a, b) => new Date(b.created_at) - new Date(a.created_at) || b.id - a.id);
  const deliveries = completed.filter(run => run.name === 'Build and Release APK');
  const services = completed.filter(run => run.name !== 'Build and Release APK');
  // Preserve the current run's category when it completes.
  const reserveDelivery = context.workflow === 'Build and Release APK' ? 1 : 0;
  const reserveService = reserveDelivery ? 0 : 1;
  let removed = 0;
  for (const run of [...deliveries.slice(limit - reserveDelivery), ...services.slice(limit - reserveService)]) {
    try {
      await github.rest.actions.deleteWorkflowRun({ ...repo, run_id: run.id });
      removed++;
    } catch (error) {
      if (error.status !== 404) throw error;
    }
  }
  core.info(`Removed ${removed} completed runs; up to ${limit} deliveries and ${limit} service runs are retained.`);
};
