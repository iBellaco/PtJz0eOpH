"""Run real app regressions, then reject empty or failed JUnit reports."""
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET
import re
import os

ROOT = Path(__file__).resolve().parents[1]


def report_failures(suites=None):
    directory = ROOT / 'app/build/test-results/testDebugUnitTest'
    reports = [directory / f'TEST-{suite}.xml' for suite in suites] if suites else sorted(directory.glob('TEST-*.xml'))
    for path in reports:
        if not path.is_file():
            continue
        suite = path.stem.removeprefix('TEST-')
        try:
            cases = ET.parse(path).getroot().iter('testcase')
        except ET.ParseError:
            print('JUNIT_REPORT_UNREADABLE', flush=True)
            continue
        for case in cases:
            for failure in list(case.findall('failure')) + list(case.findall('error')):
                message = failure.get('message', '').replace('\r', '')[:5000]
                message = re.sub(r'[\w.+-]+@[\w.-]+', '[redacted-email]', message)
                message = re.sub(r'https?://\S+', '[redacted-url]', message)
                message = re.sub(r'(?i)\b(bearer|token|password|secret|authorization)\s*[:= ]\s*\S+', r'\1=[redacted-secret]', message)
                message = re.sub(r'\b(?:gh[pousr]_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|AIza[A-Za-z0-9_-]{20,})', '[redacted-secret]', message)
                print(f"REGRESSION_FAILURE {suite}.{case.get('name')}: {message}", flush=True)
                if os.environ.get('GITHUB_ACTIONS') == 'true':
                    safe_title = re.sub(r'[^A-Za-z0-9_. -]', '_', f'{suite}.{case.get("name")}')[:180]
                    safe_message = message.replace('%', '%25').replace('\r', '%0D').replace('\n', '%0A')
                    print(f'::error title={safe_title}::{safe_message}', flush=True)


def verify(suites):
    # Android puede montar el repositorio sobre almacenamiento compartido con
    # la bandera noexec; invocar el script mediante bash evita depender del
    # permiso de ejecución del archivo gradlew.
    command = ['bash', str(ROOT / 'gradlew'), ':app:testDebugUnitTest']
    for suite in suites:
        report = ROOT / f'app/build/test-results/testDebugUnitTest/TEST-{suite}.xml'
        report.unlink(missing_ok=True)
        command.extend(['--tests', suite])
    command.extend(['--stacktrace', '--no-daemon', '--max-workers=1'])
    result = subprocess.run(command, cwd=ROOT)
    if result.returncode:
        report_failures(suites)
        result.check_returncode()
    total = 0
    for suite in suites:
        report = ET.parse(ROOT / f'app/build/test-results/testDebugUnitTest/TEST-{suite}.xml').getroot()
        assert int(report.attrib['tests']) > 0 and int(report.attrib.get('failures', 0)) == int(report.attrib.get('errors', 0)) == 0, suite
        total += int(report.attrib['tests'])
    print(f'PASS: {total} real application regression cases')
