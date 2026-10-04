"""The same release signing identity must give the same device ID after uninstall."""
import json
from pathlib import Path
import re
import subprocess

apk = Path('app/build/outputs/apk/release/app-release.apk')
test_apk = Path('app/build/outputs/apk/androidTest/release/app-release-androidTest.apk')
report = Path('app/build/reports/portuguese-device')
report.mkdir(parents=True, exist_ok=True)

def identity(label):
    result = subprocess.run(['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
        'com.example.DeviceIdentityInstalledTest', 'com.Coach.test/androidx.test.runner.AndroidJUnitRunner'],
        capture_output=True, text=True, timeout=60)
    (report / f'device-identity-{label}.txt').write_text(result.stdout + result.stderr)
    assert result.returncode == 0 and 'OK (1 test)' in result.stdout, result.stdout[-3000:]
    values = re.findall(r'coach_device_identity=(WRD_DEVICE_[a-z0-9]+)', result.stdout)
    assert values, 'The installed application did not provide its hardware ID'
    return values[-1]

before = identity('before')
subprocess.run(['adb', 'uninstall', 'com.Coach'], check=True, timeout=60)
subprocess.run(['adb', 'install', str(apk)], check=True, timeout=60)
subprocess.run(['adb', 'install', '-r', str(test_apk)], check=True, timeout=60)
after = identity('after')
assert before == after, 'Uninstalling changed the hardware slot identity'
(report / 'device-reinstall-summary.json').write_text(json.dumps({
    'same_hardware_identity': True, 'fresh_installation': True, 'build_type': 'release'}, indent=2))
print('DEVICE_REINSTALL_AUDIT: the obfuscated APK retains hardware identity after uninstall', flush=True)
