"""Check the actual APK certificate against the restored signing identity."""
import os
from pathlib import Path
import re
import subprocess

def certificate_matches(output, expected):
    # Android 37 labels certificates by scheme ("V3.0 Signer:"), whereas
    # older tools use "Signer #1". Ignore public-key digests and source stamps.
    fingerprints = re.findall(
        r'^(?:Signer #\d+|V\d+(?:\.\d+)* Signer):? certificate SHA-256 digest:\s*([0-9a-fA-F]{64})\s*$',
        output, re.MULTILINE)
    counts = re.findall(r'^Number of signers:\s*(\d+)\s*$', output, re.MULTILINE)
    return (counts == ['1'] and bool(fingerprints)
            and {fingerprint.lower() for fingerprint in fingerprints} == {expected})


def main():
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
    if result.returncode or not certificate_matches(result.stdout, expected):
        print('Verificador:', tools[-1].parent.name, 'Código:', result.returncode)
        print('Salida pública del verificador de APK:', result.stdout[:3500], result.stderr[:2000])
        raise SystemExit('La firma del APK no coincide con la identidad persistente restaurada.')
    print('APK verificado con firma persistente: ' + expected)


if __name__ == '__main__':
    main()
