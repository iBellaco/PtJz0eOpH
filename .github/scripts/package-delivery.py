"""Package the project ZIP only after the signed release APK is validated."""
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
from zipfile import ZipFile


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
    for key, path in [('project_zip', project)]:
        print(f'{key}: {path.name} ({path.stat().st_size} bytes)')
        if os.environ.get('GITHUB_OUTPUT'):
            with open(os.environ['GITHUB_OUTPUT'], 'a') as stream:
                stream.write(f'{key}={path}\n')


if __name__ == '__main__':
    main()
