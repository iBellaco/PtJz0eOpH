"""Summarize measured scanner fixtures without claiming physical phone certification."""
import argparse
import csv
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / 'app/src/test/resources/scanner/overlay-displays.csv'
VARIANTS = {'original', 'changed-background', 'changed-portraits-and-bans'}
SOURCE_SHA256 = '42661aefae3df52b4f72047c039120f72f4ea351139d7bdb5107351fe8d3342a'

def expected_devices():
    with MANIFEST.open(newline='') as source:
        rows = list(csv.DictReader(source))
    devices = {row['profile']: row for row in rows}
    if not rows or len(devices) != len(rows) or any(not (ROOT / row['evidence']).is_file() for row in rows):
        raise ValueError('La lista de dispositivos debe tener identificadores únicos y evidencia de juego')
    return devices


def summarize(directory):
    profiles, failures = [], []
    seen = set()
    devices = expected_devices()
    source = directory / 'device-profiles.json'
    if source.is_file():
        data = json.loads(source.read_text())
    else:
        failures.append('Falta el informe de dispositivos')
        data = {}
    failures.extend(data.get('failures', []))
    if data.get('sourceSha256') != SOURCE_SHA256:
        failures.append('Referencia incorrecta: ' + source.name)
    variants_by_profile = {}
    for profile in data.get('profiles', []):
        previous_failures = len(failures)
        name = profile.get('profile', source.name)
        device = devices.get(name)
        if device:
            width, height = int(device['width']), int(device['height'])
            scale = min(1.0, 1920 / width, 1080 / height)
            if profile.get('capture') != [int(width * scale + .5), int(height * scale + .5)]:
                failures.append('Captura distinta de la superficie de producción: ' + name)
        if (not device or profile.get('display') != [int(device['width']), int(device['height'])]
                or profile.get('manufacturer') != device['manufacturer'] or profile.get('model') != device['model']
                or profile.get('specification') != device['evidence']):
            failures.append('Dispositivo o pantalla sin evidencia de partida: ' + name)
        variant = profile.get('variant')
        key = (name, variant)
        checks = profile.get('checks', [])
        picks = [check for check in checks if 'slot' in check]
        bans = [check for check in checks if 'ban' in check]
        if key in seen:
            failures.append('Caso duplicado: ' + name)
        seen.add(key)
        variants_by_profile.setdefault(name, set()).add(variant)
        complete = (profile.get('status') == 'PASS' and len(checks) == 20
                    and sorted(check['slot'] for check in picks) == list(range(10))
                    and sorted(check['ban'] for check in bans) == list(range(10)))
        if not complete:
            failures.append('Caso incompleto: ' + name)
        slots = {check['slot']: check.get('crop') for check in picks}
        previous_bottom = 0
        for i in range(10):
            if i % 5 == 0:
                previous_bottom = 0
            rect = slots.get(i)
            if not isinstance(rect, list) or len(rect) != 4 or rect[0] >= rect[2] or rect[1] >= rect[3]:
                failures.append(f'Recorte inválido: {name} puesto {i}')
                continue
            if previous_bottom > rect[1]:
                failures.append(f'Recortes superpuestos: {name} puestos {i-1} y {i}')
            previous_bottom = rect[3]
        for check in picks:
            if (not check.get('accepted') or check.get('expected') != check.get('recognized')
                    or check.get('expected') != check.get('confirmed')
                    or check.get('stableFramesExercised') != 2):
                failures.append('Retrato no confirmado: ' + name)
        for check in bans:
            if (check.get('expected') != check.get('recognized') or check.get('state') != 'CONFIRMED'
                    or check.get('scanFramesExercised') != 2):
                failures.append('Baneo no confirmado: ' + name)
        errors = [check.get('centerErrorReferencePixels', float('inf')) for check in checks]
        if any(not 0 <= error <= 6 for error in errors):
            failures.append('Centro fuera de tolerancia: ' + name)
        row = {key: profile.get(key) for key in ['profile', 'case', 'variant', 'manufacturer',
               'model', 'specification', 'display', 'capture', 'viewport', 'layout', 'safeInsets', 'calibration', 'evidenceType', 'status']}
        row.update(checks=len(checks), pickChecks=len(picks), banChecks=len(bans),
                   gameplayEvidence=device['evidence'] if device else None,
                   maxCenterErrorReferencePixels=max(errors, default=None),
                   minimumPortraitScore=min((check.get('score', 0) for check in picks), default=None))
        if len(failures) != previous_failures:
            row['status'] = 'FAIL'
        profiles.append(row)
    if set(variants_by_profile) != set(devices) or any(values != VARIANTS for values in variants_by_profile.values()):
        failures.append('Matriz o variantes incompletas: ' + source.name)
    s26_file = directory / 's26-screenshot-alignment.json'
    s26 = json.loads(s26_file.read_text()) if s26_file.is_file() else None
    if not s26 or [(m.get('captureWidth'), m.get('captureHeight'), m.get('status'))
                   for m in s26.get('measurements', [])] != [(1280, 591, 'PASS')]:
        failures.append('Falta alineación de la captura S26 original de 1280 × 591')
    geometry_file = directory / 'geometry-transformations.json'
    geometry = json.loads(geometry_file.read_text()) if geometry_file.is_file() else None
    if not geometry or geometry.get('status') != 'PASS' or geometry.get('cases') != 5133:
        failures.append('Falta evidencia de las 5133 transformaciones geométricas')
    return {'status': 'FAIL' if failures else 'PASS', 'profiles': profiles,
            'simulatedCases': len(profiles), 'resolutionProfiles': len({row['profile'] for row in profiles}),
            'uniqueDisplaySizes': len({tuple(row['display']) for row in profiles}), 'geometryOnly': geometry,
            'identityChecks': sum(row['checks'] for row in profiles), 'sourceSha256': SOURCE_SHA256,
            's26Screenshot': s26,
            'physicalDevicesTested': [], 'physicalCertification': 'PENDING', 'failures': failures}


def write_reports(directory, summary):
    (directory / 'audit-summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2))
    columns = ['profile', 'model', 'display', 'capture', 'viewport', 'safeInsets', 'variant', 'layout',
               'pickChecks', 'banChecks', 'minimumPortraitScore', 'maxCenterErrorReferencePixels', 'status', 'specification']
    with (directory / 'audit-measurements.csv').open('w', newline='') as target:
        writer = csv.DictWriter(target, fieldnames=columns, extrasaction='ignore')
        writer.writeheader()
        writer.writerows(summary['profiles'])
    def size(values):
        return ' × '.join(str(value) for value in values or [])
    lines = ['# Auditoría del escáner de Coach', '',
             f"Resultado: {summary['status']}. {summary['resolutionProfiles']} perfiles, "
             f"{summary['uniqueDisplaySizes']} medidas únicas, {summary['simulatedCases']} casos "
             f"y {summary['identityChecks']} comprobaciones de identidad.", '',
             'Cada perfil verifica original, centro modificado y retratos/baneos reordenados. '
             'Se ejercitan dos frames de confirmación por slot y dos escaneos por baneo. '
             'No mide latencia real ni certifica teléfonos físicos, densidades, versiones de Android o todas las skins.', '',
             'Las pantallas corresponden a teléfonos reportados y a tablets Android con partidas documentadas. '
             'La comprobación de Coach en estas tablets se ejecuta con capturas reproducidas. '
             'La prueba S26 usa la columna rival visible en la captura original de 1280 × 591.', '',
             'Adicionalmente se verifican 5133 transformaciones geométricas: anchos 640–3840 '
             'en pasos de 37 y altos 360–2160 en pasos de 31. No son 5133 imágenes reconocidas.', '',
             '| Perfil / modelo | Pantalla horizontal | Captura procesada | Variantes | Máximo error (px de referencia) | Resultado |',
             '| --- | --- | --- | --- | --- | --- |']
    for name in dict.fromkeys(row['profile'] for row in summary['profiles']):
        rows = [row for row in summary['profiles'] if row['profile'] == name]
        row = rows[0]
        maximum = max((item['maxCenterErrorReferencePixels'] or 0 for item in rows))
        status = 'PASS' if len(rows) == 3 and all(item['status'] == 'PASS' for item in rows) else 'FAIL'
        lines.append(f"| {name} / {row['model']} | {size(row['display'])} | {size(row['capture'])} | {len(rows)} | {maximum:.3f} | {status} |")
    lines += ['', 'Tolerancia: máximo 6 píxeles medidos sobre la referencia de 1280 × 579. '
              'Un PASS significa que las 20 posiciones se reconocieron y confirmaron en las tres variantes; '
              'no significa error geométrico cero ni compatibilidad universal.', '', '## Fuentes de medidas de los modelos', '']
    lines += ['- ' + source for source in sorted({row['specification'] for row in summary['profiles'] if row['specification']})]
    if summary['failures']:
        lines += ['', '## Fallos', ''] + ['- ' + failure for failure in summary['failures']]
    (directory / 'audit-report.md').write_text('\n'.join(lines) + '\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('directory', type=Path)
    args = parser.parse_args()
    summary = summarize(args.directory)
    write_reports(args.directory, summary)
    print('SCANNER_AUDIT:', summary['status'], summary['simulatedCases'],
          'casos simulados; dispositivos físicos probados en esta ejecución: 0')
    for failure in dict.fromkeys(summary['failures']):
        print(failure)
    raise SystemExit(0 if summary['status'] == 'PASS' else 1)
