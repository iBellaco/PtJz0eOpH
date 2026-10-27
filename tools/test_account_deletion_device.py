"""Runner failure diagnostics, using mocked adb (not Android/device tests)."""
import os
import contextlib
import io
from pathlib import Path
import runpy
import subprocess
import tempfile
import unittest
from unittest.mock import patch

SCRIPT = Path(__file__).with_name('verify-account-deletion-device.py').resolve()


class AccountDeletionDeviceTest(unittest.TestCase):
    def exercise(self, native_output, pull_code=0, attempt=''):
        calls = []

        def run(command, **kwargs):
            calls.append(command)
            if 'instrument' in command:
                return subprocess.CompletedProcess(command, 0, native_output, '')
            if 'pull' in command:
                if pull_code == 0:
                    report = Path(command[-1])
                    for language in ('es', 'pt'):
                        for stage in ('first', 'final'):
                            (report / f'account-delete-{stage}-{language}.png').touch()
                return subprocess.CompletedProcess(command, pull_code, 'evidence collected', '')
            return subprocess.CompletedProcess(command, 0, '', '')

        original = Path.cwd()
        with tempfile.TemporaryDirectory() as directory:
            try:
                os.chdir(directory)
                with patch.dict(os.environ, {'COACH_ACCOUNT_DELETION_ATTEMPT': attempt}), \
                        patch('subprocess.run', side_effect=run), \
                        patch('subprocess.check_output', return_value=''):
                    failure = None
                    try:
                        with contextlib.redirect_stdout(io.StringIO()):
                            runpy.run_path(str(SCRIPT), run_name='__main__')
                    except AssertionError as error:
                        failure = str(error)
                reports = list(Path('app/build/reports').rglob('instrumentation.txt'))
                self.assertEqual(1, len(reports))
                report = reports[0].parent
                self.assertEqual(native_output, (report / 'instrumentation.txt').read_text())
                self.assertTrue((report / 'evidence-pull.txt').exists())
                self.assertTrue(any('pull' in call for call in calls))
                self.assertEqual(failure is None, (report / 'summary.json').exists())
                if attempt:
                    self.assertEqual(f'attempt-{attempt}', report.name)
                return failure
            finally:
                os.chdir(original)

    def test_failed_native_test_still_pulls_diagnostics_and_remains_failed(self):
        output = 'ComposeTimeoutException: missing control\nFAILURES!!!\nTests run: 5, Failures: 1'
        self.assertIn('ComposeTimeoutException', self.exercise(output))

    def test_failed_evidence_download_does_not_hide_original_assertion(self):
        self.assertIn('original UI error', self.exercise('original UI error', pull_code=1))

    def test_successful_attempts_keep_separate_evidence(self):
        for attempt in ('1', '2', '3'):
            with self.subTest(attempt=attempt):
                self.assertIsNone(self.exercise('OK (5 tests)', attempt=attempt))


if __name__ == '__main__':
    unittest.main()
