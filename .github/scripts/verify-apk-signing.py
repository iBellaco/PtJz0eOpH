"""Check the actual APK certificate against the restored signing identity."""
import os
from pathlib import Path
import re
import subprocess

expected = os.environ.get('COACH_EXPECTED_APK_CERT', '').lower()
if not re.fullmatch(r'[0-9a-f]{64}', expected):
    raise SystemExit('No hay una huella de firma válida para comprobar el APK.')
sdk = Path(os.environ['ANDROID_HOME'])
tools = sorted((sdk / 'build-tools').glob('*/apksigner'))
if not tools:
    raise SystemExit('No está disponible el verificador de firmas de Android.')
result = subprocess.run([str(tools[-1]), 'verify', '--verbose', '--print-certs',
                         'app/build/outputs/apk/debug/app-debug.apk'],
                        stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
fingerprints = re.findall(r'Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F]{64})', result.stdout)
if result.returncode or fingerprints != [expected]:
    raise SystemExit('La firma del APK no coincide con la identidad persistente restaurada.')
print('APK verificado con firma persistente: ' + expected)
