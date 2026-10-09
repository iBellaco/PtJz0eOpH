"""Reuse only a fully validated, same-repository PR APK with an identical tree."""
import json
import os
from pathlib import Path
import re
import subprocess

REQUIRED_JOBS = {'security-and-data-tests', 'build-apk', 'core-and-rendered', 'installed-audit', 'installed-spanish', 'build-and-release'}


def api(path):
    result = subprocess.run(['gh', 'api', path], stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE, text=True)
    if result.returncode:
        raise RuntimeError('No se pudo consultar la validación previa; se compilará de nuevo.')
    return json.loads(result.stdout)


def eligible(run, repository):
    return (run.get('status') == 'completed' and run.get('conclusion') == 'success'
            and run.get('event') == 'pull_request' and run.get('head_branch') == 'coach-validacion'
            and (run.get('head_repository') or {}).get('full_name') == repository
            and re.fullmatch(r'[0-9a-f]{40}', run.get('head_sha', '')) is not None)


def find(repository, target_sha, request=api):
    prefix = f'repos/{repository}'
    target_tree = request(f'{prefix}/git/commits/{target_sha}')['tree']['sha']
    runs = request(f'{prefix}/actions/workflows/build-apk.yml/runs?status=success&per_page=30')['workflow_runs']
    for run in runs:
        if not eligible(run, repository):
            continue
        source_tree = request(f"{prefix}/git/commits/{run['head_sha']}")['tree']['sha']
        if source_tree != target_tree:
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
    if os.environ.get('GITHUB_REF') == 'refs/heads/main':
        try:
            run_id, source_sha = find(os.environ['GITHUB_REPOSITORY'], os.environ['GITHUB_SHA'])
        except (RuntimeError, KeyError, ValueError):
            print('No hay una validación previa utilizable; se realizarán todas las comprobaciones.')
    with Path(os.environ['GITHUB_OUTPUT']).open('a') as output:
        output.write(f'run_id={run_id}\nsource_sha={source_sha}\n')
    print('APK validado reutilizable:', run_id or 'ninguno')
