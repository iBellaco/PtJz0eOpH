import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('rival_device', Path(__file__).with_name('verify-draft-rival-names-device.py'))
device = importlib.util.module_from_spec(spec)
spec.loader.exec_module(device)


def successful_output(methods):
    records = []
    for method in sorted(methods):
        fields = (f'INSTRUMENTATION_STATUS: class={device.TEST_CLASS}\n'
                  f'INSTRUMENTATION_STATUS: numtests={len(methods)}\n'
                  f'INSTRUMENTATION_STATUS: test={method}\n')
        records.append(fields + 'INSTRUMENTATION_STATUS_CODE: 1\n')
        records.append('INSTRUMENTATION_STATUS: stream=progress\nINSTRUMENTATION_STATUS_CODE: 2\n')
        records.append(fields + 'INSTRUMENTATION_STATUS_CODE: 0\n')
    return ''.join(records) + f'OK ({len(methods)} tests)\nINSTRUMENTATION_CODE: -1\n'


class DraftRivalDeviceTest(unittest.TestCase):
    def setUp(self):
        self.expected = device.expected_methods(device.SOURCE.read_text())

    def test_current_suite_and_future_additions_do_not_require_a_literal_count(self):
        self.assertIn('boundedLiveSpanishTitlesRememberLanesAndUpdateHub', self.expected)
        self.assertIn('boundedLivePortugueseTitlesRememberLanesAndUpdateHub', self.expected)
        for methods in (self.expected, self.expected | {'futureRegression'}):
            device.validate_result(successful_output(methods), 0, methods)

    def test_old_subset_cannot_pass(self):
        old = self.expected - {'boundedLiveSpanishTitlesRememberLanesAndUpdateHub',
                               'boundedLivePortugueseTitlesRememberLanesAndUpdateHub'}
        with self.assertRaises(ValueError):
            device.validate_result(successful_output(old), 0, self.expected)

    def test_matching_count_with_a_wrong_method_cannot_pass(self):
        wrong = (self.expected - {sorted(self.expected)[0]}) | {'unrelated'}
        with self.assertRaises(ValueError):
            device.validate_result(successful_output(wrong), 0, self.expected)

    def test_duplicate_method_cannot_hide_a_missing_method(self):
        output = successful_output(self.expected)
        first, second = sorted(self.expected)[:2]
        with self.assertRaises(ValueError):
            device.validate_result(output.replace(f'test={first}\n', f'test={second}\n'), 0, self.expected)

    def test_success_banner_cannot_hide_failed_or_skipped_status(self):
        output = successful_output(self.expected)
        for status in (-1, -2, -3, -4):
            with self.subTest(status=status), self.assertRaises(ValueError):
                device.validate_result(output.replace('INSTRUMENTATION_STATUS_CODE: 0',
                    f'INSTRUMENTATION_STATUS_CODE: {status}', 1), 0, self.expected)

    def test_crash_or_incomplete_runner_cannot_pass(self):
        output = successful_output(self.expected)
        for invalid in (output + 'INSTRUMENTATION_FAILED: crashed',
                        output + 'INSTRUMENTATION_RESULT: shortMsg=crashed',
                        output + 'FAILURES!!!', output.replace('INSTRUMENTATION_CODE: -1', ''),
                        f'OK ({len(self.expected)} tests)\nINSTRUMENTATION_CODE: -1\n'):
            with self.subTest(invalid=invalid[-100:]), self.assertRaises(ValueError):
                device.validate_result(invalid, 0, self.expected)

    def test_process_error_cannot_pass_even_with_successful_test_output(self):
        with self.assertRaises(ValueError):
            device.validate_result(successful_output(self.expected), 1, self.expected)

    def test_empty_source_is_not_an_empty_successful_suite(self):
        with self.assertRaises(ValueError):
            device.expected_methods('class Empty {}')


if __name__ == '__main__':
    unittest.main()
