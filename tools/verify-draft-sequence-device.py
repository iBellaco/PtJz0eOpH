"""Run the production name reader and final visual selection on the installed release; never mock OCR."""
import json
import os
from pathlib import Path
import re
import subprocess


def main():
    shard = int(os.environ['COACH_SEQUENCE_SHARD'])
    directory = Path('app/build/reports/scanner-sequence')
    directory.mkdir(parents=True, exist_ok=True)
    plan = json.loads(Path('app/build/generated/scanner-sequence-assets/scanner-sequence-plan.json').read_text())
    expected = {case['id'] for case in plan['cases'] if case['shard'] == shard}
    assert expected, 'La partición no puede estar vacía'
    for apk in ['app/build/outputs/apk/release/app-release.apk',
                'app/build/outputs/apk/androidTest/release/app-release-androidTest.apk']:
        subprocess.run(['adb', 'install', '-r', apk], check=True, timeout=120)
    command = ['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
               'com.example.DraftSequenceMultiDeviceInstalledTest', '-e', 'sequenceShard', str(shard),
               'com.Coach.test/androidx.test.runner.AndroidJUnitRunner']
    completed = subprocess.run(command, capture_output=True, text=True, timeout=3300)
    output = completed.stdout + completed.stderr
    (directory / f'instrumentation-{shard}.txt').write_text(output)
    print(output, flush=True)
    cases = [json.loads(line) for line in re.findall(r'COACH_SEQUENCE_CASE: (\{[^\r\n]+\})', output)]
    report = dict(shard=shard, sourceSha256=plan['sourceSha256'], cases=cases,
                  expectedCases=sorted(expected), physicalDevicesTested=[])
    (directory / f'sequence-{shard}.json').write_text(json.dumps(report, ensure_ascii=False, indent=2))
    subprocess.run(['adb', 'pull', '/sdcard/Android/data/com.Coach/files/scanner-sequence-audit/',
                    str(directory / 'images')], capture_output=True, timeout=120)
    assert {case['id'] for case in cases} == expected and len(cases) == len(expected), 'No se completaron todos los casos'
    assert completed.returncode == 0 and 'OK (1 test)' in output and 'FAILURES' not in output, output[-12000:]
    assert all(case['status'] == 'PASS' for case in cases), 'Hay resoluciones que no completan la secuencia'
    print('NATIVE_SEQUENCE_AUDIT: PASS', len(cases), 'secuencias; OCR nativo sin sustituciones; 0 teléfonos físicos')


if __name__ == '__main__':
    main()
