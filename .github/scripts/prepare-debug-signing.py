"""Use a configured signing identity; ephemeral keys are permitted only for tests."""
import base64
import hashlib
import os
from pathlib import Path
import re
import shutil
import subprocess


def prepare(root, android_dir, environment, run=subprocess.run):
    required = environment.get('COACH_REQUIRE_PERSISTENT_SIGNING') == 'true'
    encoded = environment.get('COACH_DEBUG_KEYSTORE_BASE64', '').strip()
    expected = environment.get('COACH_SIGNING_CERT_SHA256', '').strip().lower()
    if required and (not encoded or not re.fullmatch(r'[0-9a-f]{64}', expected)):
        raise RuntimeError('Publicación bloqueada: configura la clave persistente y su huella de certificado. No se generará una firma nueva para publicar.')
    keystore = root / 'debug.keystore'
    if encoded:
        try:
            content = base64.b64decode(''.join(encoded.split()), validate=True)
        except ValueError:
            raise RuntimeError('El secreto de firma no contiene un archivo base64 válido.') from None
        if not content:
            raise RuntimeError('El archivo de firma está vacío.')
        keystore.write_bytes(content)
        keystore.chmod(0o600)
    else:
        run(['keytool', '-genkeypair', '-keystore', str(keystore), '-storepass', 'android',
             '-alias', 'androiddebugkey', '-keypass', 'android', '-dname', 'CN=Coach Test,O=Coach,C=US',
             '-keyalg', 'RSA', '-keysize', '2048', '-validity', '10000'], check=True,
            stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    certificate = run(['keytool', '-exportcert', '-keystore', str(keystore), '-storepass', 'android',
                       '-alias', 'androiddebugkey'], check=True, stdout=subprocess.PIPE,
                      stderr=subprocess.PIPE).stdout
    fingerprint = hashlib.sha256(certificate).hexdigest()
    if expected and fingerprint != expected:
        raise RuntimeError('Publicación bloqueada: el certificado no coincide con la identidad de firma configurada.')
    android_dir.mkdir(parents=True, exist_ok=True)
    for target in (root / 'github.keystore', android_dir / 'debug.keystore'):
        shutil.copyfile(keystore, target)
        target.chmod(0o600)
    keystore.chmod(0o600)
    return fingerprint


if __name__ == '__main__':
    try:
        fingerprint = prepare(Path.cwd(), Path.home() / '.android', os.environ)
        print('SIGNING_CERT_SHA256=' + fingerprint)
    except (RuntimeError, subprocess.CalledProcessError) as error:
        # Never include the encoded key, file contents or keytool stderr in CI output.
        print(str(error) if isinstance(error, RuntimeError) else 'No se pudo validar la clave de firma configurada.')
        raise SystemExit(1)
