"""Check real name-band OCR on the installed release, without accepting an OCR fallback."""
from pathlib import Path
import subprocess
import re

report = Path('app/build/reports/spanish-device')
report.mkdir(parents=True, exist_ok=True)
command = ['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
    'com.example.DraftRivalNameInstalledTest', 'com.Coach.test/androidx.test.runner.AndroidJUnitRunner']
try:
    # Stable title confirmation requires two independent frames for picks/trades.
    # Include both screen geometries and the reported ten-slot portrait case.
    result = subprocess.run(command, capture_output=True, text=True, timeout=1200)
except subprocess.TimeoutExpired as failure:
    def decode(value):
        return value.decode(errors='replace') if isinstance(value, bytes) else value or ''
    partial = decode(failure.stdout) + decode(failure.stderr)
    (report / 'draft-rival-name-instrumentation.txt').write_text(partial)
    print(partial[-8000:], flush=True)
    raise
(report / 'draft-rival-name-instrumentation.txt').write_text(result.stdout + result.stderr)
assert result.returncode == 0 and 'OK (12 tests)' in result.stdout, result.stdout[-24000:]
print('DRAFT_RIVAL_NAME_AUDIT: exact allied names and observed lanes survive selection, OCR gaps and champion trades in ES/PT; both Vi portraits fill jungle', flush=True)

for evidence in re.findall(r"(?:DRAFT_FAST_NAME_AUDIT|DRAFT_BAN_8067_AUDIT): [^\r\n]+", result.stdout):
    print(evidence, flush=True)
