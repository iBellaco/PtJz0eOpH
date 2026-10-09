/** Preserve delivery evidence separately from the high-frequency service runs. */
module.exports = async ({ github, context, core }, limit = 30) => {
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
  const releases = await github.paginate(github.rest.repos.listReleases, { ...repo, per_page: 100 });
  const published = releases.filter(release => !release.draft)
    .sort((a, b) => new Date(b.published_at || b.created_at) - new Date(a.published_at || a.created_at));
  let removedReleases = 0;
  for (const release of published.slice(limit)) {
    try {
      await github.rest.repos.deleteRelease({ ...repo, release_id: release.id });
      removedReleases++;
    } catch (error) {
      if (error.status !== 404) throw error;
    }
  }
  core.info(`Removed ${removedReleases} old releases; up to ${limit} published releases are retained.`);
};
