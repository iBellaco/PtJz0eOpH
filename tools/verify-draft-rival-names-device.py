"""Check real name-band OCR on the installed release, without accepting an OCR fallback."""
from pathlib import Path
import subprocess
import re

TEST_CLASS = 'com.example.DraftRivalNameInstalledTest'
SOURCE = Path(__file__).resolve().parents[1] / 'app/src/androidTest/java/com/example/DraftRivalNameInstalledTest.kt'


def expected_methods(source):
    methods = re.findall(r'^\s*@Test\s+fun\s+(\w+)\s*\(', source, re.MULTILINE)
    if not methods or len(methods) != len(set(methods)):
        raise ValueError('No se pudo determinar una lista única de pruebas instaladas')
    return set(methods)


def validate_result(output, returncode, expected):
    """Require every source test to finish successfully, including new tests."""
    if returncode != 0 or not expected:
        raise ValueError(f'Instrumentación incompleta: salida={returncode}, esperadas={len(expected)}')
    if re.search(r'FAILURES!!!|INSTRUMENTATION_FAILED|INSTRUMENTATION_RESULT: shortMsg=', output):
        raise ValueError('La instrumentación informó fallos o terminó por un error')
    totals = re.findall(r'^OK \((\d+) tests?\)\s*$', output, re.MULTILINE)
    if totals != [str(len(expected))]:
        raise ValueError(f'Resumen incompleto: esperadas={len(expected)}, aprobadas={totals}')
    if not re.search(r'^INSTRUMENTATION_CODE: -1\s*$', output, re.MULTILINE):
        raise ValueError('Falta el cierre normal de AndroidJUnitRunner')
    completed = []
    record = {}
    for line in output.splitlines():
        field = re.fullmatch(r'INSTRUMENTATION_STATUS: (\w+)=(.*)', line)
        if field:
            record[field[1]] = field[2]
        status = re.fullmatch(r'INSTRUMENTATION_STATUS_CODE: (-?\d+)', line)
        if not status:
            continue
        code = int(status[1])
        if code < 0:
            raise ValueError(f'Prueba fallida u omitida: {record.get("test")}, estado={code}')
        if code == 0:
            if record.get('class') != TEST_CLASS or record.get('numtests') != str(len(expected)):
                raise ValueError(f'Registro de otra clase o ejecución parcial: {record}')
            completed.append(record.get('test'))
        record = {}
    if len(completed) != len(expected) or set(completed) != expected:
        missing = sorted(expected - set(completed))
        raise ValueError(f'Pruebas incompletas o duplicadas: faltan={missing}, terminadas={completed}')


def main():
    expected = expected_methods(SOURCE.read_text())
    report = Path('app/build/reports/spanish-device')
    report.mkdir(parents=True, exist_ok=True)
    command = ['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
        TEST_CLASS, 'com.Coach.test/androidx.test.runner.AndroidJUnitRunner']
    try:
        result = subprocess.run(command, capture_output=True, text=True, timeout=1200)
    except subprocess.TimeoutExpired as failure:
        def decode(value):
            return value.decode(errors='replace') if isinstance(value, bytes) else value or ''
        partial = decode(failure.stdout) + decode(failure.stderr)
        (report / 'draft-rival-name-instrumentation.txt').write_text(partial)
        print(partial[-8000:], flush=True)
        raise
    output = result.stdout + result.stderr
    (report / 'draft-rival-name-instrumentation.txt').write_text(output)
    try:
        validate_result(output, result.returncode, expected)
    except ValueError as error:
        raise AssertionError(f'{error}\n{output[-24000:]}') from error
    print(f'DRAFT_RIVAL_NAME_AUDIT: {len(expected)} installed tests passed; exact allied names and observed lanes survive selection, OCR gaps and champion trades in ES/PT; both Vi portraits fill jungle', flush=True)
    for evidence in re.findall(r'(?:DRAFT_FAST_NAME_AUDIT|DRAFT_BAN_8067_AUDIT): [^\r\n]+', output):
        print(evidence, flush=True)


if __name__ == '__main__':
    main()
