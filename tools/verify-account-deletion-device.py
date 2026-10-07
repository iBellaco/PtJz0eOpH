"""Exercise unchanged deletion assertions on Android rather than a simulated dialog window."""
from pathlib import Path
import json
import subprocess

report = Path('app/build/reports/spanish-device/account-deletion')
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
assert result.returncode == 0 and 'OK (5 tests)' in result.stdout, result.stdout[-7000:]
assert 'FATAL EXCEPTION' not in crashes, crashes[-5000:]
subprocess.run(['adb', 'pull', '/sdcard/Android/data/com.Coach/files/coach-account-deletion-audit/.',
    str(report)], check=True, timeout=30)
for language in ('es', 'pt'):
    for stage in ('first', 'final'):
        assert (report / f'account-delete-{stage}-{language}.png').is_file()
(report / 'summary.json').write_text(json.dumps({'build_type':'release', 'tests':5,
    'languages':['es-419','pt'], 'confirmations':2, 'password_required':True,
    'duplicate_submission_blocked':True, 'error_does_not_claim_success':True,
    'real_account_mutations':False, 'crashes':0}, indent=2))
print('ACCOUNT_DELETION_DEVICE_AUDIT: five original behavioral cases passed on the installed release in Spanish and Portuguese', flush=True)
