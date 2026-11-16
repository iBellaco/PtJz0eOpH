"""Reuse only a fully validated, same-repository PR APK with unchanged APK sources and build inputs."""
import json
import os
from pathlib import Path
import re
import subprocess
import time
from urllib.request import urlopen
from urllib.error import URLError

VALIDATED_BRANCH = 'coach-validacion'
API_ATTEMPTS = 3

# These files cannot change the APK: delivery housekeeping and its own tests.
DELIVERY_ONLY_FILES = {
    '.github/scripts/cleanup-completed-runs.cjs',
    '.github/scripts/cleanup-completed-runs.test.cjs',
    '.github/scripts/find-validated-apk.py',
    '.github/scripts/test_validated_apk.py',
    '.github/scripts/package-delivery.py',
    '.github/scripts/verify-apk-signing.py',
    '.github/scripts/test_apk_signing.py',
    'docs/coach-audit-1.1.10.257.md',
    'docs/coach-audit-1.1.10.336.md',
    'docs/coach-audit-1.1.10.351.md',
}
REQUIRED_JOBS = {'catalog-validation', 'security-auth-tests', 'scanner-tests',
                 'build-apk', 'core-tests', 'rendered-audit', 'installed-audit', 'installed-spanish',
                 'scanner-sequence-audit', 'build-and-release'}
REQUIRED_JOBS.add('native-title-preflight')


def api(path):
    # The publication verification step has read access to this public repo.
    # Use the public API when the step intentionally has no write token.
    last_error = None
    for attempt in range(API_ATTEMPTS):
        try:
            if not (os.environ.get('GH_TOKEN') or os.environ.get('GITHUB_TOKEN')):
                with urlopen('https://api.github.com/' + path, timeout=30) as response:
                    return json.load(response)
            result = subprocess.run(['gh', 'api', path], stdout=subprocess.PIPE,
                                    stderr=subprocess.PIPE, text=True, timeout=45)
            if result.returncode:
                raise RuntimeError(result.stderr.strip() or 'gh api falló')
            return json.loads(result.stdout)
        except (URLError, TimeoutError, subprocess.TimeoutExpired, RuntimeError, ValueError) as error:
            last_error = error
            if attempt + 1 < API_ATTEMPTS:
                # GitHub's Actions/artifact API is eventually consistent just
                # after a validation run completes. Give it a short retry
                # window before deciding that the APK cannot be reused.
                time.sleep(2 * (attempt + 1))
    raise RuntimeError('No se pudo comprobar la fuente del APK validado.') from last_error


def eligible(run, repository):
    return (run.get('status') == 'completed' and run.get('conclusion') == 'success'
            and run.get('event') in {'pull_request', 'push', 'workflow_dispatch'} and run.get('head_branch') == VALIDATED_BRANCH
            and (run.get('head_repository') or {}).get('full_name') == repository
            and re.fullmatch(r'[0-9a-f]{40}', run.get('head_sha', '')) is not None)


def same_apk_inputs(repository, source_tree, target_tree, request=api):
    if source_tree == target_tree:
        return True
    prefix = f'repos/{repository}'
    previous = request(f"{prefix}/git/trees/{source_tree}?recursive=1")
    current = request(f"{prefix}/git/trees/{target_tree}?recursive=1")
    if previous.get('truncated', True) or current.get('truncated', True):
        return False
    def apk_inputs(tree):
        ignored = DELIVERY_ONLY_FILES
        return {entry['path']: (entry['type'], entry['sha'])
                for entry in tree['tree'] if entry['type'] != 'tree'
                and entry['path'] not in ignored}
    return apk_inputs(previous) == apk_inputs(current)


def find(repository, target_sha, request=api):
    prefix = f'repos/{repository}'
    target_tree = request(f'{prefix}/git/commits/{target_sha}')['tree']['sha']
    # Scope the search to the validation branch. The previous all-branches
    # query could omit the correct run from the first page when several fix or
    # main runs were created close together, causing main to rebuild every
    # heavy suite even though a matching validation artifact existed.
    runs = request(
        f'{prefix}/actions/workflows/build-apk.yml/runs?branch={VALIDATED_BRANCH}&status=completed&per_page=100'
    )['workflow_runs']
    for run in runs:
        if not eligible(run, repository):
            continue
        source_tree = request(f"{prefix}/git/commits/{run['head_sha']}")['tree']['sha']
        if source_tree != target_tree:
            if not same_apk_inputs(repository, source_tree, target_tree, request):
                continue
        jobs = request(f"{prefix}/actions/runs/{run['id']}/jobs?per_page=100")['jobs']
        passed = {job['name'] for job in jobs if job.get('conclusion') == 'success'}
        if not REQUIRED_JOBS.issubset(passed):
            continue
        artifacts = request(f"{prefix}/actions/runs/{run['id']}/artifacts?per_page=100")['artifacts']
        matches = [a for a in artifacts if a.get('name') == 'app-release.apk' and a.get('expired') is False]
        if len(matches) != 1:
            continue
        return str(run['id']), run['head_sha']
    return '', ''


if __name__ == '__main__':
    run_id, source_sha = '', ''
    subprocess.run(['node', '.github/scripts/cleanup-completed-runs.test.cjs'], check=True)
    if (os.environ.get('GITHUB_REF') == 'refs/heads/main'
            or os.environ.get('GITHUB_HEAD_REF') == 'coach-validacion'):
        try:
            run_id, source_sha = find(os.environ['GITHUB_REPOSITORY'], os.environ['GITHUB_SHA'])
        except (RuntimeError, KeyError, ValueError):
            print('No hay una validación previa utilizable; se realizarán todas las comprobaciones.')
    with Path(os.environ['GITHUB_OUTPUT']).open('a') as output:
        output.write(f'run_id={run_id}\nsource_sha={source_sha}\n')
    print('APK validado reutilizable:', run_id or 'ninguno')
