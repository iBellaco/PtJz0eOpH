"""Restore Coach's signing key from secrets or a strictly private draft.

Only public metadata belongs in coach-signing.json. Never print the draft body,
include it in an exception, or upload a keystore as an Actions artifact.
"""
import argparse
import importlib.util
import json
import os
from pathlib import Path
import re
import subprocess


SCRIPT_DIR = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('coach_signing', SCRIPT_DIR / 'prepare-debug-signing.py')
signing = importlib.util.module_from_spec(spec)
spec.loader.exec_module(signing)


def private_environment(document, manifest):
    if document.get('id') != manifest['release_id'] or document.get('draft') is not True:
        raise RuntimeError('La firma debe permanecer en el borrador privado previsto. No se permite una publicación pública.')
    try:
        body = json.loads(document.get('body', ''))
    except (TypeError, ValueError):
        raise RuntimeError('El almacenamiento privado de firma no tiene un formato válido.') from None
    expected = manifest['certificate_sha256']
    if not re.fullmatch(r'[0-9a-f]{64}', expected):
        raise RuntimeError('La huella pública de firma no es válida.')
    if body.get('schema') != 1 or body.get('certificate_sha256') != expected:
        raise RuntimeError('El almacenamiento privado no corresponde a la identidad de firma prevista.')
    encoded = body.get('keystore_base64')
    if not isinstance(encoded, str) or not encoded.strip():
        raise RuntimeError('No está disponible la clave privada de firma.')
    return {'COACH_REQUIRE_PERSISTENT_SIGNING': 'true',
            'COACH_DEBUG_KEYSTORE_BASE64': encoded,
            'COACH_SIGNING_CERT_SHA256': expected}


def read_private_document(manifest):
    result = subprocess.run(['gh', 'api',
                             f"repos/{manifest['repository']}/releases/{manifest['release_id']}"],
                            stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if result.returncode:
        raise RuntimeError('No se pudo acceder al almacenamiento privado de firma con la conexión de esta ejecución.')
    try:
        return json.loads(result.stdout)
    except ValueError:
        raise RuntimeError('La conexión no devolvió el almacenamiento privado de firma esperado.') from None


def resolve_environment(environment, manifest, reader=read_private_document):
    if environment.get('COACH_DEBUG_KEYSTORE_BASE64', '').strip():
        expected = (environment.get('COACH_SIGNING_CERT_SHA256', '') or manifest.get('certificate_sha256', '')).strip().lower()
        if not re.fullmatch(r'[0-9a-f]{64}', expected):
            raise RuntimeError('La clave de firma configurada necesita su huella de certificado.')
        return {**environment, 'COACH_SIGNING_CERT_SHA256': expected, 'COACH_REQUIRE_PERSISTENT_SIGNING': 'true'}
    if (environment.get('COACH_ALLOW_EPHEMERAL_TEST_SIGNING') == 'true'
            and environment.get('COACH_REQUIRE_PERSISTENT_SIGNING') != 'true'):
        return {'COACH_REQUIRE_PERSISTENT_SIGNING': 'false'}
    return private_environment(reader(manifest), manifest)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--check-access', action='store_true')
    args = parser.parse_args()
    try:
        manifest = json.loads((SCRIPT_DIR.parent / 'coach-signing.json').read_text())
        environment = resolve_environment(os.environ, manifest)
        if args.check_access:
            print('Configuración privada de firma persistente disponible.')
        else:
            fingerprint = signing.prepare(Path.cwd(), Path.home() / '.android', environment)
            print('SIGNING_CERT_SHA256=' + fingerprint)
            if os.environ.get('GITHUB_OUTPUT'):
                with Path(os.environ['GITHUB_OUTPUT']).open('a') as output:
                    output.write('certificate_sha256=' + fingerprint + '\n')
    except (RuntimeError, subprocess.CalledProcessError):
        # Suppress response bodies and keytool output, including private material.
        print('No se pudo restaurar la firma persistente. Comprueba el acceso privado, su estado de borrador y el certificado previsto.')
        raise SystemExit(1)
