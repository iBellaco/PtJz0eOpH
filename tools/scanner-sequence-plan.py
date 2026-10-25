"""Build a native OCR plan for phone profiles from the complete geometric matrix."""
import argparse
import json
from pathlib import Path

SOURCE_SHA256 = '42661aefae3df52b4f72047c039120f72f4ea351139d7bdb5107351fe8d3342a'
SHARD_COUNT = 16


def supports_android_phone_overlay(row):
    """Release OCR claims cover Android phones where the app overlay can run."""
    manufacturer = str(row.get('manufacturer', '')).strip().lower()
    identity = (str(row.get('profile', '')) + ' ' + str(row.get('model', ''))).lower()
    words = set(identity.replace('-', ' ').replace('_', ' ').split())
    return (manufacturer not in {'generic', 'apple'}
            and words.isdisjoint({'tablet', 'pad', 'ipad', 'tab'}))


def make_plan(summary):
    if summary.get('status') != 'PASS' or summary.get('sourceSha256') != SOURCE_SHA256:
        raise ValueError('La matriz de origen debe estar completa y aprobada')
    profiles = [row for row in summary['profiles'] if row['variant'] == 'original']
    if len(profiles) != summary['resolutionProfiles'] or len({row['profile'] for row in profiles}) != len(profiles):
        raise ValueError('Faltan perfiles o existen duplicados')
    grouped = {}
    selected = 0
    for row in profiles:
        # Preserve the original group indexes so a targeted failure keeps its
        # stable case ID. Generic geometry experiments are not phone models and
        # must not become release-gating native OCR claims.
        prior = 'poco' if row['profile'] == 'poco-x7-pro' else 'default'
        calibration = row.get('calibration')
        if not calibration:
            raise ValueError('Falta la calibración medida de ' + row['profile'])
        insets = row.get('safeInsets') or [0,0,0,0]
        key = (*row['capture'], row['layout'], prior, json.dumps(calibration, sort_keys=True), tuple(insets))
        case = grouped.setdefault(key, dict(capture=row['capture'], layout=row['layout'],
                                           prior=prior, calibration=calibration, safeInsets=insets, profiles=[]))
        if not supports_android_phone_overlay(row):
            continue
        case['profiles'].append({key: row[key] for key in ('profile', 'model', 'display', 'specification')})
        selected += 1
    cases = []
    for index, case in enumerate(grouped.values()):
        if not case['profiles']:
            continue
        # Alternate both real draft orders across the matrix. Both known devices exercise both orders.
        orders = [True, False] if any(row['profile'] in ('red-magic-reference', 'poco-x7-pro')
                                     for row in case['profiles']) else [index % 2 == 0]
        for order in orders:
            cases.append(dict(case, id=f'sequence-{index}-{"ally" if order else "enemy"}-first',
                              firstPick=order, language='es' if order else 'pt'))
    for index, case in enumerate(cases):
        case['shard'] = index % SHARD_COUNT
        case['preflight'] = (tuple(case['capture']) in {(640,360),(800,450),(1024,768),(1600,1000),(854,480),(1520,720)}
                             or any(tuple(row['display']) in {(1560,720),(1544,720),(1570,720),(1640,720),(2270,1080),
                                    (2244,1080),(2240,1080),(2388,1080),(2400,1176),(2616,1212),(2720,1260)}
                                    for row in case['profiles'])
                             or any(row['profile'] in ('red-magic-reference','poco-x7-pro') for row in case['profiles']))
    if not cases:
        raise ValueError('No hay perfiles de teléfono para OCR nativo')
    return dict(sourceSha256=SOURCE_SHA256, modelProfiles=selected,
                geometryOnlyProfiles=len(profiles)-selected,
                physicalDevicesTested=[], cases=cases)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('summary', type=Path)
    parser.add_argument('output', type=Path)
    args = parser.parse_args()
    plan = make_plan(json.loads(args.summary.read_text()))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(plan, ensure_ascii=False, indent=2))
    print('NATIVE_SEQUENCE_PLAN:', plan['modelProfiles'], 'perfiles;', len(plan['cases']), 'secuencias reales OCR simuladas por resolución')
