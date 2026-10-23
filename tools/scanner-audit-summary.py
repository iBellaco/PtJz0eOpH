"""Summarize measured scanner fixtures without claiming physical phone certification."""
import argparse
import json
from pathlib import Path


def summarize(directory):
    groups = ['device-profiles', 'phone-resolutions', 'tablet-resolutions',
              'foldable-resolutions', 'edge-anchored-resolutions']
    profiles, failures = [], []
    for group in groups:
        source = directory / (group + '.json')
        if not source.is_file():
            failures.append('Falta grupo: ' + group)
            continue
        data = json.loads(source.read_text())
        failures.extend(data.get('failures', []))
        for profile in data.get('profiles', []):
            if profile.get('status') != 'PASS' or len(profile.get('checks', [])) != 20:
                failures.append('Caso incompleto: ' + profile.get('profile', group))
            profiles.append({key: profile.get(key) for key in
                             ['profile', 'model', 'display', 'capture', 'evidenceType', 'status']})
    s26_file = directory / 's26-screenshot-alignment.json'
    s26 = json.loads(s26_file.read_text()) if s26_file.is_file() else None
    if not s26 or len(s26.get('measurements', [])) != 3:
        failures.append('Falta alineación de la captura S26 en tres resoluciones')
    return {'status': 'FAIL' if failures else 'PASS', 'profiles': profiles,
            'simulatedCases': len(profiles), 's26Screenshot': s26,
            'physicalDevicesTested': [], 'physicalCertification': 'PENDING', 'failures': failures}


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('directory', type=Path)
    args = parser.parse_args()
    summary = summarize(args.directory)
    (args.directory / 'audit-summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2))
    print('SCANNER_AUDIT:', summary['status'], summary['simulatedCases'],
          'casos simulados; dispositivos físicos probados en esta ejecución: 0')
    raise SystemExit(0 if summary['status'] == 'PASS' else 1)
