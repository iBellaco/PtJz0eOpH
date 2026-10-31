"""Retry only transient Android UI startup failures; retain both attempts."""
import json
from pathlib import Path
import shutil
import subprocess
import sys

TRANSIENT = ('Android UI hierarchy did not become ready:', 'No application texts rendered:')


def should_retry(output):
    return any(marker in output for marker in TRANSIENT) and not any(
        marker in output for marker in ('FATAL EXCEPTION', 'Fatal signal', 'Application process died'))


def run_audit(run, preserve):
    for attempt in (1, 2):
        result = run()
        output = result.stdout + result.stderr
        preserve(attempt, output)
        print(output, end='', flush=True)
        if result.returncode == 0:
            return 0, attempt
        if attempt == 2 or not should_retry(output):
            return result.returncode or 1, attempt
        print('DRAFT_AUDIT: transient UI startup failure; retrying once', flush=True)
    raise AssertionError('Unreachable')


def main():
    evidence = Path('app/build/reports/draft-attempts')
    source = Path('app/build/reports/portuguese-device')
    evidence.mkdir(parents=True, exist_ok=True)

    def run():
        return subprocess.run([sys.executable, 'tools/check-draft-selection-device.py'],
                              capture_output=True, text=True)

    def preserve(attempt, output):
        target = evidence / str(attempt)
        target.mkdir(parents=True, exist_ok=True)
        (target / 'output.txt').write_text(output)
        if source.exists():
            shutil.copytree(source, target / 'device', dirs_exist_ok=True)

    code, attempts = run_audit(run, preserve)
    (evidence / 'summary.json').write_text(json.dumps({'attempts': attempts, 'exitCode': code}))
    return code


if __name__ == '__main__':
    sys.exit(main())
