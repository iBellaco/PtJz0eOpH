"""Publish two portable ZIPs only after the signed release APK is validated."""
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
from zipfile import ZipFile, ZIP_DEFLATED


def forbidden(name):
    parts = Path(name).parts
    return (any(part in {'.git', 'node_modules', '.env', 'secrets.properties', 'local.properties', 'google-services.json'} for part in parts)
            or name.lower().endswith(('.keystore', '.jks', '.base64', '.apk', '.aab'))
            or any(part.startswith('gha-creds-') for part in parts))


def main():
    apk_dir = Path('app/build/outputs/apk/release')
    apk = apk_dir / 'app-release.apk'
    provenance = json.loads((apk_dir / 'coach-apk-provenance.json').read_text())
    proof = json.loads((apk_dir / 'coach-obfuscation-proof.json').read_text())
    version = provenance['version_name']
    if not re.fullmatch(r'[0-9]+(?:\.[0-9]+){2,3}', version):
        raise SystemExit('Invalid delivery version')
    digest = hashlib.sha256(apk.read_bytes()).hexdigest()
    tree = subprocess.check_output(['git', 'rev-parse', 'HEAD^{tree}'], text=True).strip()
    if (digest != provenance['apk_sha256'] or digest != proof['apk_sha256']
            or tree != provenance['source_tree'] or proof['renamed_application_classes'] < 100):
        raise SystemExit('Delivery does not match the validated signed and obfuscated APK')
    output = Path(os.environ.get('RUNNER_TEMP', '/tmp')) / 'coach-delivery'
    output.mkdir(parents=True, exist_ok=True)
    project = output / f'Coach-{version}-proyecto.zip'
    subprocess.run(['git', 'archive', '--format=zip', '--prefix=Coach/', '-o', str(project), 'HEAD'], check=True)
    with ZipFile(project) as archive:
        prohibited = [name for name in archive.namelist() if forbidden(name)]
        if prohibited or archive.testzip():
            project.unlink()
            raise SystemExit('Source archive contains a private or invalid file')
        if 'Coach/gradle/wrapper/gradle-wrapper.jar' not in archive.namelist():
            raise SystemExit('Source archive is missing its reproducible Gradle wrapper')
    packaged = output / f'Coach-{version}-APK-ofuscado.zip'
    apk_name = f'Coach-{version}-ofuscado.apk'
    with ZipFile(packaged, 'w', ZIP_DEFLATED, compresslevel=6) as archive:
        archive.write(apk, apk_name)
        archive.writestr('SHA256.txt', f'{digest}  {apk_name}\n')
        archive.writestr('VERIFICACION.txt', f'Coach {version} — build {provenance["version_code"]}\nPaquete: com.Coach\nAPK release firmado y ofuscado con R8.\nCódigo comprobado: {tree}\nCertificado SHA256: {provenance["certificate_sha256"]}\nAPK SHA256: {digest}\nClases de aplicación renombradas: {proof["renamed_application_classes"]}\n')
        for name in ['coach-apk-provenance.json', 'coach-obfuscation-proof.json']:
            archive.write(apk_dir / name, name)
    with ZipFile(packaged) as archive:
        if archive.testzip() or hashlib.sha256(archive.read(apk_name)).hexdigest() != digest:
            raise SystemExit('APK ZIP failed integrity verification')
    checksums = output / 'SHA256SUMS.txt'
    checksums.write_text(''.join(f'{hashlib.sha256(file.read_bytes()).hexdigest()}  {file.name}\n' for file in [packaged, project]))
    for key, path in [('apk_zip', packaged), ('project_zip', project), ('zip_checksums', checksums)]:
        print(f'{key}: {path.name} ({path.stat().st_size} bytes)')
        if os.environ.get('GITHUB_OUTPUT'):
            with open(os.environ['GITHUB_OUTPUT'], 'a') as stream:
                stream.write(f'{key}={path}\n')


if __name__ == '__main__':
    main()
