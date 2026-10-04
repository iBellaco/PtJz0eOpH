"""Require R8-renamed application classes in the actual, non-debuggable APK."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import zipfile

APK = Path('app/build/outputs/apk/release/app-release.apk')
PROOF = APK.with_name('coach-obfuscation-proof.json')


def renamed_classes(mapping):
    return [target for original, target in re.findall(
        r'^(com\.example\.[^ :]+) -> ([^ :]+):$', mapping, re.MULTILINE)
        if original != target and target.startswith('com.example.wrdftx.o.')]


def verify_apk():
    sdk = Path(os.environ['ANDROID_HOME'])
    aapt = sorted((sdk / 'build-tools').glob('*/aapt'))[-1]
    badging = subprocess.check_output([str(aapt), 'dump', 'badging', str(APK)], text=True)
    if 'application-debuggable' in badging:
        raise SystemExit('El APK de entrega permite depuración; no se publicará.')
    with zipfile.ZipFile(APK) as archive:
        dex = b''.join(archive.read(name) for name in archive.namelist()
                       if re.fullmatch(r'classes\d*\.dex', name))
    return dex


def check_proof(dex):
    proof = json.loads(PROOF.read_text())
    if (proof.get('schema') != 1 or proof.get('build_type') != 'release'
            or proof.get('apk_sha256') != hashlib.sha256(APK.read_bytes()).hexdigest()
            or proof.get('renamed_application_classes', 0) < 100
            or len(proof.get('verified_descriptors', [])) < 5):
        raise SystemExit('No existe una comprobación válida de ofuscación para este APK.')
    if not all(descriptor.encode() + b'\0' in dex for descriptor in proof['verified_descriptors']):
        raise SystemExit('Las clases ofuscadas verificadas no están en el APK de entrega.')
    return proof


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--build', action='store_true')
    args = parser.parse_args()
    dex = verify_apk()
    if args.build:
        mapping = Path('app/build/outputs/mapping/release/mapping.txt').read_text()
        renamed = renamed_classes(mapping)
        descriptors = list(dict.fromkeys('L' + name.replace('.', '/') + ';' for name in renamed))
        present = [descriptor for descriptor in descriptors if descriptor.encode() + b'\0' in dex]
        if len(renamed) < 100 or len(present) < 5:
            raise SystemExit('R8 no ha ofuscado suficientes clases de la aplicación.')
        PROOF.write_text(json.dumps({
            'schema': 1, 'build_type': 'release',
            'apk_sha256': hashlib.sha256(APK.read_bytes()).hexdigest(),
            'mapping_sha256': hashlib.sha256(mapping.encode()).hexdigest(),
            'renamed_application_classes': len(renamed),
            'verified_descriptors': present[:5],
        }, indent=2) + '\n')
    proof = check_proof(dex)
    print('APK release sin depuración; clases de la aplicación ofuscadas:',
          proof['renamed_application_classes'])


if __name__ == '__main__':
    main()
