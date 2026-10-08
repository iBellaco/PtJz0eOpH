"""Restore Android client configuration from Actions Secrets without logging its contents."""
import json
import os
from pathlib import Path
import sys


def restore(root, environment):
    value = environment.get('COACH_GOOGLE_SERVICES_JSON', '')
    if not value.strip():
        raise ValueError('Falta el Secret COACH_GOOGLE_SERVICES_JSON en GitHub Actions.')
    try:
        document = json.loads(value)
        clients = document['client']
        project = document['project_info']
        valid = isinstance(project['project_id'], str) and bool(project['project_id'].strip()) and any(
            client['client_info']['android_client_info']['package_name'] == 'com.Coach'
            and any(isinstance(key.get('current_key'), str) and key['current_key'].strip()
                    for key in client.get('api_key', []))
            for client in clients)
        if not valid:
            raise ValueError()
    except (ValueError, TypeError, KeyError, AttributeError):
        raise ValueError('El Secret debe contener un google-services.json válido para com.Coach.') from None
    target = root / 'app' / 'google-services.json'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(document, indent=2) + '\n')
    target.chmod(0o600)
    return target


if __name__ == '__main__':
    try:
        restore(Path.cwd(), os.environ)
        print('Configuración Android restaurada desde el Secret de Actions.')
    except ValueError as error:
        print(str(error), file=sys.stderr)
        raise SystemExit(1)
