"""Build the native OCR plan for documented Android phone and tablet displays."""
import argparse
import json
import os
from pathlib import Path

SOURCE_SHA256 = '42661aefae3df52b4f72047c039120f72f4ea351139d7bdb5107351fe8d3342a'
SHARD_COUNT = 16


def make_plan(summary):
    if summary.get('status') != 'PASS' or summary.get('sourceSha256') != SOURCE_SHA256:
        raise ValueError('La matriz de origen debe estar completa y aprobada')
    profiles = [row for row in summary['profiles'] if row['variant'] == 'original']
    if len(profiles) != summary['resolutionProfiles'] or len({row['profile'] for row in profiles}) != len(profiles):
        raise ValueError('Faltan perfiles o existen duplicados')
    grouped = {}
    selected = 0
    for row in profiles:
        if not row.get('gameplayEvidence') or row.get('manufacturer', '').lower() in {'', 'generic', 'apple'}:
            continue
        prior = 'poco' if row['profile'] == 'poco-x7-pro' else 'default'
        calibration = row.get('calibration')
        if not calibration:
            raise ValueError('Falta la calibración medida de ' + row['profile'])
        insets = row.get('safeInsets') or [0,0,0,0]
        key = (*row['capture'], row['layout'], prior, json.dumps(calibration, sort_keys=True), tuple(insets))
        case = grouped.setdefault(key, dict(capture=row['capture'], layout=row['layout'],
                                           prior=prior, calibration=calibration, safeInsets=insets, profiles=[]))
        case['profiles'].append({key: row[key] for key in ('profile', 'model', 'display', 'specification')})
        selected += 1
    cases = []
    for case in grouped.values():
        name = min(row['profile'] for row in case['profiles'])
        for order in (True, False):
            cases.append(dict(case, id=f'sequence-{name}-{"ally" if order else "enemy"}-first',
                              firstPick=order, language='es' if order else 'pt', preflight=False))
    for index, case in enumerate(cases):
        case['shard'] = index % SHARD_COUNT
    if not cases:
        raise ValueError('No hay dispositivos Android documentados para OCR nativo')
    # The short gate covers both draft orders and the two ends of the measured aspect ratios.
    def aspect(case):
        return case['capture'][0] / case['capture'][1]
    min((c for c in cases if c['firstPick']), key=aspect)['preflight'] = True
    max((c for c in cases if not c['firstPick']), key=aspect)['preflight'] = True
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
    if os.environ.get('GITHUB_OUTPUT'):
        with open(os.environ['GITHUB_OUTPUT'], 'a') as outputs:
            shards = sorted({case['shard'] for case in plan['cases']})
            print('sequence_shards=' + json.dumps(shards), file=outputs)
    print('NATIVE_SEQUENCE_PLAN:', plan['modelProfiles'], 'perfiles;', len(plan['cases']), 'secuencias reales OCR simuladas por resolución')
