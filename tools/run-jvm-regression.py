"""Run real app regressions, then reject empty or failed JUnit reports."""
from pathlib import Path
import subprocess
import xml.etree.ElementTree as ET
import re

ROOT = Path(__file__).resolve().parents[1]


def verify(suites):
    command = [str(ROOT / 'gradlew'), ':app:testDebugUnitTest']
    for suite in suites:
        report = ROOT / f'app/build/test-results/testDebugUnitTest/TEST-{suite}.xml'
        report.unlink(missing_ok=True)
        command.extend(['--tests', suite])
    command.extend(['--stacktrace', '--no-daemon', '--max-workers=1'])
    result = subprocess.run(command, cwd=ROOT)
    if result.returncode:
        for suite in suites:
            path = ROOT / f'app/build/test-results/testDebugUnitTest/TEST-{suite}.xml'
            if not path.is_file():
                continue
            for case in ET.parse(path).getroot().iter('testcase'):
                for failure in list(case.findall('failure')) + list(case.findall('error')):
                    message = failure.get('message', '').replace('\r', '')[:5000]
                    message = re.sub(r'[\w.+-]+@[\w.-]+', '[redacted-email]', message)
                    message = re.sub(r'https?://\S+', '[redacted-url]', message)
                    print(f"REGRESSION_FAILURE {suite}.{case.get('name')}: {message}", flush=True)
        result.check_returncode()
    total = 0
    for suite in suites:
        report = ET.parse(ROOT / f'app/build/test-results/testDebugUnitTest/TEST-{suite}.xml').getroot()
        assert int(report.attrib['tests']) > 0 and int(report.attrib.get('failures', 0)) == int(report.attrib.get('errors', 0)) == 0, suite
        total += int(report.attrib['tests'])
    print(f'PASS: {total} real application regression cases')
