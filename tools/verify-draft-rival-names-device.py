"""Check real name-band OCR on the installed release, without accepting an OCR fallback."""
from pathlib import Path
import subprocess

report = Path('app/build/reports/spanish-device')
report.mkdir(parents=True, exist_ok=True)
result = subprocess.run(['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
    'com.example.DraftRivalNameInstalledTest', 'com.Coach.test/androidx.test.runner.AndroidJUnitRunner'],
    capture_output=True, text=True, timeout=150)
(report / 'draft-rival-name-instrumentation.txt').write_text(result.stdout + result.stderr)
assert result.returncode == 0 and 'OK (2 tests)' in result.stdout, result.stdout[-5000:]
print('DRAFT_RIVAL_NAME_AUDIT: current Milio and Caitlyn name bands recognized on installed release', flush=True)
