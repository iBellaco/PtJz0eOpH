/** Keep 30 runs total while reserving delivery evidence from frequent service runs. */
module.exports = async ({ github, context, core }, limit = 30) => {
  const repo = { owner: context.repo.owner, repo: context.repo.repo };
  const runs = await github.paginate(github.rest.actions.listWorkflowRunsForRepo,
    { ...repo, status: 'completed', per_page: 100 });
  const current = Number(context.runId);
  const completed = runs.filter(run => run.id !== current)
    .sort((a, b) => new Date(b.created_at) - new Date(a.created_at) || b.id - a.id);
  const deliveries = completed.filter(run => run.name === 'Build and Release APK');
  const services = completed.filter(run => run.name !== 'Build and Release APK');
  // Reserve the current run's place because it is not completed yet.
  const reserveDelivery = context.workflow === 'Build and Release APK' ? 1 : 0;
  const reserveService = reserveDelivery ? 0 : 1;
  let removed = 0;
  const deliveryQuota = Math.floor(limit / 2);
  const serviceQuota = limit - deliveryQuota;
  for (const run of [...deliveries.slice(deliveryQuota - reserveDelivery), ...services.slice(serviceQuota - reserveService)]) {
    try {
      await github.rest.actions.deleteWorkflowRun({ ...repo, run_id: run.id });
      removed++;
    } catch (error) {
      if (error.status !== 404) throw error;
    }
  }
  core.info(`Removed ${removed} completed runs; up to ${limit} runs total are retained.`);
  const releases = await github.paginate(github.rest.repos.listReleases, { ...repo, per_page: 100 });
  const signingReleaseId = Number(require('../coach-signing.json').release_id);
  const published = releases.filter(release => !release.draft && !release.prerelease)
    .sort((a, b) => new Date(b.published_at || b.created_at) - new Date(a.published_at || a.created_at));
  const keep = new Set(published.slice(0, limit - 1).map(release => release.id));
  keep.add(signingReleaseId);
  let removedReleases = 0;
  for (const release of releases.filter(release => !keep.has(release.id))) {
    try {
      await github.rest.repos.deleteRelease({ ...repo, release_id: release.id });
      removedReleases++;
    } catch (error) {
      if (error.status !== 404) throw error;
    }
  }
  core.info(`Removed ${removedReleases} old releases; up to ${limit - 1} final releases and the protected signing draft are retained.`);
};
