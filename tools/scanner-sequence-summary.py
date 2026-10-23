"""Require every planned native sequence and preserve every modeled display in a readable audit."""
import argparse
import importlib.util
import json
from pathlib import Path

spec = importlib.util.spec_from_file_location('sequence_plan', Path(__file__).with_name('scanner-sequence-plan.py'))
planner = importlib.util.module_from_spec(spec)
spec.loader.exec_module(planner)


def summarize(portraits, reports):
    plan = planner.make_plan(portraits)
    expected = {case['id']: case for case in plan['cases']}
    seen, failures, results = set(), [], []
    for report in reports:
        if report.get('sourceSha256') != plan['sourceSha256']:
            failures.append('Referencia OCR distinta')
        for case in report.get('cases', []):
            key = case['id']
            planned = expected.get(key)
            if key in seen or planned is None:
                failures.append('Secuencia duplicada o no planificada: ' + key)
                continue
            seen.add(key)
            before = len(failures)
            if any(case.get(field) != planned[field] for field in ('capture', 'profiles', 'layout', 'prior', 'firstPick', 'language', 'shard')):
                failures.append('Geometría/secuencia distinta a la planificada: ' + key)
            steps = case.get('steps', [])
            if case.get('status') != 'PASS' or case.get('failures') or [step.get('step') for step in steps] != list(range(10)):
                failures.append('Secuencia incompleta: ' + key)
            for step in steps:
                if (len(step.get('recognized', {})) != step['step'] or step.get('recognized') != step.get('expected')
                        or step.get('frames') != (1 if step['step'] == 0 else 2)
                        or set(step.get('roles', {})) != {'0', '1', '2', '3', '4'}):
                    failures.append(f"Lectura de nombre/línea incorrecta: {key} paso {step['step']}")
            tenth = case.get('tenth', {})
            if (not tenth.get('confirmed') or not tenth.get('nameHidden') or not tenth.get('expected')
                    or tenth.get('expected') != tenth.get('recognized')):
                failures.append('Décima selección visual no confirmada: ' + key)
            result = dict(case)
            result['status'] = 'PASS' if before == len(failures) else 'FAIL'
            results.append(result)
    failures += ['Falta secuencia: ' + key for key in sorted(set(expected) - seen)]
    return dict(status='FAIL' if failures else 'PASS', sourceSha256=plan['sourceSha256'],
                modelProfiles=plan['modelProfiles'], sequences=len(results), expectedSequences=len(expected),
                nativeOcrFrames=sum(step.get('frames', 0) for case in results for step in case.get('steps', [])),
                physicalDevicesTested=[], cases=results, failures=failures)


def write_report(directory, summary):
    directory.mkdir(parents=True, exist_ok=True)
    (directory / 'sequence-summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2))
    lines = ['# Coach: secuencia OCR 1–9 y décima selección visual', '',
             f"Resultado: {summary['status']}; {summary['sequences']}/{summary['expectedSequences']} secuencias; "
             f"{summary['nativeOcrFrames']} lecturas OCR nativas; {summary['modelProfiles']} perfiles asociados.", '',
             'APK release instalado en emulador Android 34 x86_64. No hay teléfonos físicos conectados. '
             'Se redimensionan los píxeles de la captura del propietario y se simula la aparición sucesiva de los títulos. '
             'Los nombres de campeones seleccionados no se vuelven a dibujar ni se inyectan como resultados OCR. '
             'Los títulos de líneas previos son sintéticos en español/portugués; los rivales sin seleccionar no tienen título. '
             'Los retratos permanecen visibles antes de seleccionar, para impedir confirmaciones visuales prematuras.', '',
             'Cada secuencia prueba 0–9 nombres en orden, dos lecturas por nombre nuevo y décima imagen con nombre oculto. '
             'Ambos órdenes se distribuyen en la matriz; Red Magic y Poco ejercitan ambos. '
             'Los perfiles con idéntica captura/distribución comparten el mismo caso de píxeles: no son pruebas físicas diferentes. '
             'No certifica rendimiento, permisos de captura, densidad, skins o versiones Android de cada teléfono.', '',
             '| Modelo / escenario | Pantalla horizontal | Captura procesada | Distribución | Orden probado | Idioma | Resultado |',
             '| --- | --- | --- | --- | --- | --- | --- |']
    for case in summary['cases']:
        for profile in case['profiles']:
            size = lambda values: ' × '.join(map(str, values))
            lines.append(f"| {profile['model']} ({profile['profile']}) | {size(profile['display'])} | {size(case['capture'])} | "
                         f"{case['layout']} | {'Aliado primero' if case['firstPick'] else 'Rival primero'} | {case['language']} | {case['status']} |")
    if summary['failures']:
        lines += ['', '## Fallos', ''] + ['- ' + failure for failure in summary['failures']]
    (directory / 'sequence-report.md').write_text('\n'.join(lines) + '\n')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('portraits', type=Path)
    parser.add_argument('reports', type=Path)
    args = parser.parse_args()
    reports = [json.loads(path.read_text()) for path in sorted(args.reports.glob('**/sequence-[0-3].json'))]
    summary = summarize(json.loads(args.portraits.read_text()), reports)
    write_report(args.reports, summary)
    print('SCANNER_SEQUENCE:', summary['status'], summary['sequences'], '/', summary['expectedSequences'])
    raise SystemExit(0 if summary['status'] == 'PASS' else 1)
