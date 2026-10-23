"""Report requested models honestly: exact named pixels tested are not family-wide certification."""
import argparse
import csv
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def canonical(value):
    value = re.sub(r'\([^)]*\)', '', value.lower()).strip()
    return re.sub(r'[^a-z0-9]', '', value)


def report(summary, requested):
    # Match named models only. Never promote a 4G/5G family or shared resolution into a tested model.
    originals = [row for row in summary['profiles'] if row['variant'] == 'original']
    rows = []
    for brand in requested['brands']:
        for name in brand['models']:
            matched = [row for row in originals if canonical(row['model']) == canonical(name)]
            if brand.get('platform') == 'iOS-no-Android-APK':
                status = 'NO_COMPATIBLE_APK_ANDROID'
            elif matched:
                status = 'IMAGEN_PASS' if all(row['status'] == 'PASS' for row in matched) else 'IMAGEN_FAIL'
            else:
                status = 'PENDIENTE_MEDIDA_Y_VARIANTE_CONFIRMADAS'
            rows.append(dict(brand=brand['brand'],model=name,status=status,
                             profiles=[row['profile'] for row in matched]))
    return dict(physicalDevicesTested=[],brandsRequested=len(requested['brands']),models=rows,
                scope='resized-reference-images-not-hardware-certification')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('directory',type=Path)
    args = parser.parse_args()
    summary = json.loads((args.directory/'audit-summary.json').read_text())
    requested = json.loads((ROOT/'docs/scanner-requested-models.json').read_text())
    coverage = report(summary, requested)
    (args.directory/'requested-model-coverage.json').write_text(json.dumps(coverage,ensure_ascii=False,indent=2))
    lines = ['# Cobertura de la lista solicitada','',
        'Solo se comprueban imágenes redimensionadas. No se certifican teléfonos físicos ni todos los modelos de una marca. '
        'IMAGEN_PASS se refiere a retratos/baneos; el informe de secuencias nativas es obligatorio por separado. '
        'No se asignan medidas inventadas a modelos cuya variante o especificación no se ha confirmado. '
        'El APK Android no funciona en iPhone. El tipo de notch de la lista no se toma como especificación verificada.','',
        '| Marca | Modelo solicitado | Estado de imágenes | Perfil exacto |','| --- | --- | --- | --- |']
    lines += [f"| {row['brand']} | {row['model']} | {row['status']} | {', '.join(row['profiles']) or '—'} |" for row in coverage['models']]
    lines += ['', '## Marcas sin un modelo concreto solicitado', '',
              ', '.join(brand['brand'] for brand in requested['brands'] if not brand['models']), '',
              'Las fichas de referencia y medidas de los perfiles incorporados están en audit-report.md. '
              'Un perfil de referencia no cubre automáticamente todos los equipos ni las variantes de esa marca.']
    (args.directory/'requested-model-coverage.md').write_text('\n'.join(lines)+'\n')
    print('REQUESTED_COVERAGE:',len(coverage['models']),'modelos registrados; sin certificación universal')
