import importlib.util
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location('report', Path(__file__).with_name('workflow-failure-report.py'))
report = importlib.util.module_from_spec(spec)
spec.loader.exec_module(report)


class FailureReportTest(unittest.TestCase):
    def test_classification_does_not_echo_private_log(self):
        for text in ('Unresolved reference: secret', 'AssertionError: user@example.org',
                     'PERMISSION_DENIED bearer private-token', 'keystore /private/key', 'UnknownHost private-host'):
            value = report.diagnose(text)
            self.assertNotIn('example.org', value)
            self.assertNotIn('private', value)
            self.assertNotIn('secret', value)
            self.assertNotIn('No hay una causa', value)

    def test_unknown_error_is_not_fabricated(self):
        self.assertIn('No hay una causa', report.diagnose('unknown failure'))

    def test_safe_case_identifiers_are_actionable_without_echoing_failure_data(self):
        value = report.diagnose('REGRESSION_FAILURE com.example.DraftReferenceFrameTest.changedPortraitOrder: AssertionError user@example.org private-token')
        self.assertIn('com.example.DraftReferenceFrameTest.changedPortraitOrder', value)
        self.assertNotIn('example.org', value)
        self.assertNotIn('private-token', value)

    def test_timeout_and_cancellation_do_not_require_log_access(self):
        self.assertIn('cancelada', report.job_diagnosis('unused', {'conclusion': 'cancelled'}))
        self.assertIn('tiempo', report.job_diagnosis('unused', {'conclusion': 'timed_out'}))
