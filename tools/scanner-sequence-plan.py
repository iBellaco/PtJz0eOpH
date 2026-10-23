"""Build a native OCR plan from the completed geometric/portrait matrix, not a hand-picked subset."""
import argparse
import json
from pathlib import Path

SOURCE_SHA256 = '42661aefae3df52b4f72047c039120f72f4ea351139d7bdb5107351fe8d3342a'


def make_plan(summary):
    if summary.get('status') != 'PASS' or summary.get('sourceSha256') != SOURCE_SHA256:
        raise ValueError('La matriz de origen debe estar completa y aprobada')
    profiles = [row for row in summary['profiles'] if row['variant'] == 'original']
    if len(profiles) != summary['resolutionProfiles'] or len({row['profile'] for row in profiles}) != len(profiles):
        raise ValueError('Faltan perfiles o existen duplicados')
    grouped = {}
    for row in profiles:
        # Equal resolutions with different model labels do not become different physical tests.
        # Keep both known priors and every distinct distribution/capture geometry.
        prior = 'poco' if row['profile'] == 'poco-x7-pro' else 'default'
        key = (*row['capture'], row['layout'], prior)
        case = grouped.setdefault(key, dict(capture=row['capture'], layout=row['layout'],
                                           prior=prior, profiles=[]))
        case['profiles'].append({key: row[key] for key in ('profile', 'model', 'display', 'specification')})
    cases = []
    for index, case in enumerate(grouped.values()):
        # Alternate both real draft orders across the matrix. Both known devices exercise both orders.
        orders = [True, False] if any(row['profile'] in ('red-magic-reference', 'poco-x7-pro')
                                     for row in case['profiles']) else [index % 2 == 0]
        for order in orders:
            cases.append(dict(case, id=f'sequence-{index}-{"ally" if order else "enemy"}-first',
                              firstPick=order, language='es' if order else 'pt'))
    for index, case in enumerate(cases):
        case['shard'] = index % 4
    return dict(sourceSha256=SOURCE_SHA256, modelProfiles=len(profiles),
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
