import contextlib
import importlib.util
import io
from pathlib import Path
import sys
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('sequence_device', Path(__file__).with_name('verify-draft-sequence-device.py'))
device = importlib.util.module_from_spec(spec)
spec.loader.exec_module(device)


class SequenceStreamTest(unittest.TestCase):
    def test_targeted_native_reports_do_not_overwrite_each_other(self):
        labels = [device.evidence_label(True, 0, {case}) for case in
                  ['sequence-49-enemy-first', 'sequence-56-ally-first', 'sequence-63-enemy-first']]
        self.assertEqual(3, len(set(labels)))
        self.assertEqual('preflight', device.evidence_label(True, 0, set()))
        self.assertEqual('15', device.evidence_label(False, 15, set()))
        self.assertNotIn('/', device.evidence_label(False, 0, {'../invalid'}))

    def test_real_stdout_and_stderr_remain_available_when_instrumentation_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'native.txt'
            with contextlib.redirect_stdout(io.StringIO()) as visible:
                code, text = device.run_instrumentation([sys.executable, '-u', '-c',
                    "import sys; print('actual partial case',flush=True); print('actual error',file=sys.stderr,flush=True); sys.exit(1)"], path, 10)
            self.assertEqual(1, code)
            self.assertIn('actual partial case', text)
            self.assertIn('actual error', text)
            self.assertEqual(text, visible.getvalue())
            self.assertNotIn('OK (1 test)', text)

    def test_timeout_keeps_partial_output_without_marking_it_as_a_pass(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'native.txt'
            with contextlib.redirect_stdout(io.StringIO()):
                code, text = device.run_instrumentation([sys.executable, '-u', '-c',
                    "import time; print('unfinished native case',flush=True); time.sleep(20)"], path, 2)
            self.assertNotEqual(0, code)
            self.assertIn('unfinished native case', text)
            self.assertNotIn('OK (1 test)', text)
