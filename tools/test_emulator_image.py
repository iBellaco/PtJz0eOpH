import importlib.util
from pathlib import Path
import subprocess
import unittest
from unittest.mock import Mock

spec = importlib.util.spec_from_file_location('image_setup', Path(__file__).with_name('prepare-emulator-image.py'))
setup = importlib.util.module_from_spec(spec)
spec.loader.exec_module(setup)


class ImageSetupTest(unittest.TestCase):
    def result(self, code, message=''):
        return subprocess.CompletedProcess([], code, '', message)

    def test_corrupt_zip_recovers_before_tests(self):
        run = Mock(side_effect=[self.result(1, 'Error reading Zip content from a SeekableByteChannel.'), self.result(0)])
        pause = Mock()
        self.assertEqual(0, setup.install(run, pause))
        self.assertEqual(2, run.call_count)
        pause.assert_called_once_with(5)
        for call in run.call_args_list:
            self.assertEqual(['sdkmanager', '--install', setup.PACKAGE, '--channel=0'], call.args[0])

    def test_persistent_corruption_fails_after_three_attempts(self):
        run = Mock(return_value=self.result(1, 'Error reading Zip content from a SeekableByteChannel'))
        self.assertEqual(1, setup.install(run, Mock()))
        self.assertEqual(3, run.call_count)

    def test_configuration_error_is_not_retried(self):
        run = Mock(return_value=self.result(1, 'Failed to find package'))
        pause = Mock()
        self.assertEqual(1, setup.install(run, pause))
        self.assertEqual(1, run.call_count)
        pause.assert_not_called()

    def test_download_timeout_is_bounded_and_retains_failure(self):
        run = Mock(side_effect=subprocess.TimeoutExpired('sdkmanager', 180, output=b'partial download'))
        self.assertEqual(124, setup.install(run, Mock()))
        self.assertEqual(3, run.call_count)

    def test_success_has_no_retry(self):
        run = Mock(return_value=self.result(0))
        pause = Mock()
        self.assertEqual(0, setup.install(run, pause))
        self.assertEqual(1, run.call_count)
        pause.assert_not_called()
