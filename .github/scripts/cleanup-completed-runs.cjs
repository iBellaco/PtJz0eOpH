/** Keep at least ten completed/current runs for every workflow independently. */
module.exports = async ({ github, context, core }, workflowMinimum = 10, releaseLimit = 30) => {
  const repo = { owner: context.repo.owner, repo: context.repo.repo };
  const runs = await github.paginate(github.rest.actions.listWorkflowRunsForRepo,
    { ...repo, status: 'completed', per_page: 100 });
  const current = Number(context.runId);
  const completed = runs.filter(run => run.id !== current)
    .sort((a, b) => new Date(b.created_at) - new Date(a.created_at) || b.id - a.id);
  const byWorkflow = new Map();
  for (const run of completed) {
    const key = String(run.workflow_id ?? `name:${run.name}`);
    if (!byWorkflow.has(key)) byWorkflow.set(key, []);
    byWorkflow.get(key).push(run);
  }
  let removed = 0;
  for (const workflowRuns of byWorkflow.values()) {
    // The current run is not completed yet. Keep one fewer historical run for
    // this workflow so the completed run brings the visible total to ten.
    const reserveCurrent = workflowRuns.some(run => run.name === context.workflow) ? 1 : 0;
    const keepCompleted = Math.max(0, workflowMinimum - reserveCurrent);
    for (const run of workflowRuns.slice(keepCompleted)) {
      try {
        await github.rest.actions.deleteWorkflowRun({ ...repo, run_id: run.id });
        removed++;
      } catch (error) {
        if (error.status !== 404) throw error;
      }
    }
  }
  core.info(`Removed ${removed} old runs; every workflow retains at least ${workflowMinimum} runs.`);
  const releases = await github.paginate(github.rest.repos.listReleases, { ...repo, per_page: 100 });
  const signingReleaseId = Number(require('../coach-signing.json').release_id);
  const published = releases.filter(release => !release.draft && !release.prerelease)
    .sort((a, b) => new Date(b.published_at || b.created_at) - new Date(a.published_at || a.created_at));
  const keep = new Set(published.slice(0, releaseLimit - 1).map(release => release.id));
  keep.add(signingReleaseId);
  // gh release create uploads assets to a draft before publishing it. A parallel
  // service cleanup must not remove that draft while its upload is in progress.
  const uploadGraceMs = 60 * 60 * 1000;
  for (const release of releases) {
    if (release.draft && Date.now() - new Date(release.created_at).getTime() < uploadGraceMs) {
      keep.add(release.id);
    }
  }
  let removedReleases = 0;
  for (const release of releases.filter(release => !keep.has(release.id))) {
    try {
      await github.rest.repos.deleteRelease({ ...repo, release_id: release.id });
      removedReleases++;
    } catch (error) {
      if (error.status !== 404) throw error;
    }
  }
  core.info(`Removed ${removedReleases} old releases; up to ${releaseLimit - 1} final releases and the protected signing draft are retained.`);
};
