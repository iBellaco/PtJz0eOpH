"""Verify compression, every supported ABI, extraction, and native OCR in the installed APK."""
import json
from pathlib import Path
import subprocess
import zipfile
import hashlib

apk = Path('app/build/outputs/apk/release/app-release.apk')
report = Path('app/build/reports/portuguese-device')
report.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(apk) as archive:
    libraries = {}
    for abi in ['armeabi-v7a', 'arm64-v8a', 'x86_64']:
        name = f'lib/{abi}/libmlkit_google_ocr_pipeline.so'
        entry = archive.getinfo(name)
        assert entry.compress_type == zipfile.ZIP_DEFLATED, f'Native library is not compressed: {abi}'
        libraries[abi] = {'bytes': entry.file_size, 'download_bytes': entry.compress_size,
                          'sha256': hashlib.sha256(archive.read(name)).hexdigest()}
subprocess.run(['adb', 'install', '-r', str(apk)], check=True, timeout=60)
subprocess.run(['adb', 'install', '-r', 'app/build/outputs/apk/androidTest/release/app-release-androidTest.apk'], check=True, timeout=60)
result = subprocess.run(['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
                         'com.example.NativeOcrPackagingTest',
                         'com.Coach.test/androidx.test.runner.AndroidJUnitRunner'],
                        text=True, capture_output=True, timeout=90)
(report / 'native-ocr-instrumentation.txt').write_text(result.stdout + result.stderr)
(report / 'native-ocr-crash-logcat.txt').write_text(subprocess.check_output(['adb', 'logcat', '-d', '-b', 'crash'], text=True, timeout=30))
assert result.returncode == 0 and 'OK (1 test)' in result.stdout and 'FAILURES' not in result.stdout, result.stdout[-3000:]
(report / 'native-packaging.json').write_text(json.dumps({'apk_bytes': apk.stat().st_size, 'libraries': libraries, 'native_ocr': 'passed'}, indent=2))
print('NATIVE_OCR_DEVICE: all three ABIs retained; native recognition passed', flush=True)
