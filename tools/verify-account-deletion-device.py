"""Exercise unchanged deletion assertions on Android rather than a simulated dialog window."""
from pathlib import Path
import json
import os
import subprocess

report = Path('app/build/reports/spanish-device/account-deletion')
attempt = os.environ.get('COACH_ACCOUNT_DELETION_ATTEMPT', '')
if attempt:
    if not attempt.isascii() or not attempt.isdecimal() or int(attempt) < 1:
        raise ValueError('COACH_ACCOUNT_DELETION_ATTEMPT debe ser un entero positivo')
    report = report / f'attempt-{attempt}'
report.mkdir(parents=True, exist_ok=True)
for apk in ('app/build/outputs/apk/release/app-release.apk', 'app/build/outputs/apk/androidTest/release/app-release-androidTest.apk'):
    subprocess.run(['adb', 'install', '-r', apk], check=True, timeout=60)
subprocess.run(['adb', 'shell', 'pm', 'grant', 'com.Coach', 'android.permission.POST_NOTIFICATIONS'], check=True, timeout=20)
subprocess.run(['adb', 'logcat', '-c'], check=True, timeout=20)
result = subprocess.run(['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
    'com.example.AccountDeletionInstalledTest', 'com.Coach.test/androidx.test.runner.AndroidJUnitRunner'],
    capture_output=True, text=True, timeout=240)
(report / 'instrumentation.txt').write_text(result.stdout + result.stderr)
crashes = subprocess.check_output(['adb', 'logcat', '-d', '-b', 'crash'], text=True, timeout=30)
(report / 'crash-logcat.txt').write_text(crashes)
# Pull partial screenshots and semantic diagnostics before raising an assertion.
# A failed test must not discard the evidence needed to identify its control.
evidence = subprocess.run(['adb', 'pull', '/sdcard/Android/data/com.Coach/files/coach-account-deletion-audit/.',
    str(report)], capture_output=True, text=True, timeout=30)
(report / 'evidence-pull.txt').write_text(evidence.stdout + evidence.stderr)
assert result.returncode == 0 and 'OK (5 tests)' in result.stdout, result.stdout[-7000:]
assert 'FATAL EXCEPTION' not in crashes, crashes[-5000:]
evidence.check_returncode()
for language in ('es', 'pt'):
    for stage in ('first', 'final'):
        assert (report / f'account-delete-{stage}-{language}.png').is_file()
(report / 'summary.json').write_text(json.dumps({'build_type':'release', 'tests':5,
    'languages':['es-419','pt'], 'confirmations':2, 'password_required':True,
    'duplicate_submission_blocked':True, 'error_does_not_claim_success':True,
    'real_account_mutations':False, 'crashes':0}, indent=2))
print('ACCOUNT_DELETION_DEVICE_AUDIT: five original behavioral cases passed on the installed release in Spanish and Portuguese', flush=True)
