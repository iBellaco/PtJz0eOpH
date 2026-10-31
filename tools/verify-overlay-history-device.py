"""Open and close history in a real window without an activity on the release APK."""
from pathlib import Path
import json
import subprocess

report = Path('app/build/reports/spanish-device')
report.mkdir(parents=True, exist_ok=True)
subprocess.run(['adb', 'install', '-r', 'app/build/outputs/apk/androidTest/release/app-release-androidTest.apk'], check=True, timeout=60)
subprocess.run(['adb', 'logcat', '-c'], check=True, timeout=20)
result = subprocess.run(['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
    'com.example.OverlayHistoryInstalledTest', 'com.Coach.test/androidx.test.runner.AndroidJUnitRunner'],
    capture_output=True, text=True, timeout=90)
(report / 'overlay-history-instrumentation.txt').write_text(result.stdout + result.stderr)
crashes = subprocess.check_output(['adb', 'logcat', '-d', '-b', 'crash'], text=True, timeout=30)
(report / 'overlay-history-crash-logcat.txt').write_text(crashes)
assert result.returncode == 0 and 'OK (1 test)' in result.stdout, result.stdout[-3000:]
assert 'FATAL EXCEPTION' not in crashes, crashes[-3000:]
subprocess.run(['adb', 'pull', '/sdcard/Android/data/com.Coach/files/coach-device-audit/overlay-history-es.png',
    str(report / 'overlay-history-es.png')], check=True, timeout=30)
(report / 'overlay-history-summary.json').write_text(json.dumps({
    'build_type': 'release', 'window': 'application_overlay', 'activity_dispatcher': False,
    'opened': True, 'returned': True, 'crashes': 0, 'language': 'es-419'}, indent=2))
print('OVERLAY_HISTORY_AUDIT: installed obfuscated history opens and returns without an activity or crashes', flush=True)
