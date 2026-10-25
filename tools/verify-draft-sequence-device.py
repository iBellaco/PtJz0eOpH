"""Run the production name reader and final visual selection on the installed release; never mock OCR."""
import json
import os
from pathlib import Path
import re
import subprocess
import threading


def run_instrumentation(command, log_path, timeout):
    """Flush native progress as it arrives; retain actual partial output on a failure."""
    with log_path.open('w') as log:
        with subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                              text=True, bufsize=1) as process:
            timer = threading.Timer(timeout, process.kill)
            timer.daemon = True
            timer.start()
            try:
                for line in process.stdout:
                    log.write(line)
                    log.flush()
                    print(line, end='', flush=True)
                code = process.wait()
            finally:
                timer.cancel()
    return code, log_path.read_text()


def main():
    shard = int(os.environ['COACH_SEQUENCE_SHARD'])
    preflight = os.environ.get('COACH_SEQUENCE_PREFLIGHT')=='true'
    targets = {value.strip() for value in os.environ.get('COACH_SEQUENCE_TARGETS','').split(',') if value.strip()}
    label = 'preflight' if preflight else str(shard)
    directory = Path('app/build/reports/scanner-sequence-preflight' if preflight else 'app/build/reports/scanner-sequence')
    directory.mkdir(parents=True, exist_ok=True)
    plan = json.loads(Path('app/build/generated/scanner-sequence-assets/scanner-sequence-plan.json').read_text())
    expected = {case['id'] for case in plan['cases']
                if (case['preflight'] if preflight else case['shard']==shard)
                and (not targets or case['id'] in targets)}
    assert expected, 'La partición no puede estar vacía'
    build_type = os.environ.get('COACH_SEQUENCE_BUILD_TYPE', 'release').lower()
    for apk in [f'app/build/outputs/apk/{build_type}/app-{build_type}.apk',
                f'app/build/outputs/apk/androidTest/{build_type}/app-{build_type}-androidTest.apk']:
        subprocess.run(['adb', 'install', '-r', apk], check=True, timeout=120)
    command = ['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
               'com.example.DraftSequenceMultiDeviceInstalledTest', '-e', 'sequenceShard', str(shard),
               '-e', 'sequencePreflight', 'true' if preflight else 'false']
    if targets:
        command += ['-e', 'sequenceTargets', ','.join(sorted(targets))]
    command += ['com.Coach.test/androidx.test.runner.AndroidJUnitRunner']
    code, output = run_instrumentation(command, directory / f'instrumentation-{label}.txt',
                                       1800 if preflight else 3300)
    cases = [json.loads(line) for line in re.findall(r'COACH_SEQUENCE_CASE: (\{[^\r\n]+\})', output)]
    report = dict(shard=shard, sourceSha256=plan['sourceSha256'], cases=cases,
                  expectedCases=sorted(expected), physicalDevicesTested=[],
                  instrumentationPassed=code == 0 and 'OK (1 test)' in output and 'FAILURES' not in output)
    (directory / f'sequence-{label}.json').write_text(json.dumps(report, ensure_ascii=False, indent=2))
    remote = 'scanner-sequence-preflight' if preflight else 'scanner-sequence-audit'
    subprocess.run(['adb', 'pull', f'/sdcard/Android/data/com.Coach/files/{remote}/',
                    str(directory / 'images')], capture_output=True, timeout=120)
    assert {case['id'] for case in cases} == expected and len(cases) == len(expected), 'No se completaron todos los casos'
    assert code == 0 and 'OK (1 test)' in output and 'FAILURES' not in output, output[-12000:]
    assert all(case['status'] == 'PASS' for case in cases), 'Hay resoluciones que no completan la secuencia'
    assert all(case.get('validationMode')==('preflight-not-release' if preflight else 'full') for case in cases)
    required_files = {'draft-swain-eight.jpg', 'draft-kaisa-nine.jpg', 'draft-mel-nine.jpg'}
    for case in cases:
        if not preflight or targets or any(p['profile'] in {'red-magic-reference','poco-x7-pro'} for p in case['profiles']):
            real = case.get('realCaptures', [])
            assert len(real) == 3 and {r['file'] for r in real} == required_files, 'Faltan capturas reales'
            assert all(r['expectedTitles'] == r['recognizedTitles'] and r['frames'] == 2 for r in real), 'Falló un título real'
            for r in real:
                if r['visualCheck'] == 'blocked-before-nine':
                    assert r['tenthRecognized'] is None, 'No activar el décimo antes de nueve títulos'
                if r['visualCheck'] == 'required':
                    assert r['tenthRecognized'] == r['tenthExpected'], 'Falló el décimo real'
    print('NATIVE_TITLE_PREFLIGHT' if preflight else 'NATIVE_SEQUENCE_AUDIT', ': PASS', len(cases),
          'casos; OCR nativo sin sustituciones; 0 teléfonos físicos; prueba preliminar no publicable' if preflight else 'secuencias completas; 0 teléfonos físicos')


if __name__ == '__main__':
    main()
