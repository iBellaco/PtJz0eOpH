import importlib.util
from pathlib import Path
from subprocess import CompletedProcess
import unittest

spec = importlib.util.spec_from_file_location('retry', Path(__file__).with_name('run-draft-device-audit.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class RetryPolicyTest(unittest.TestCase):
    def exercise(self, outputs):
        attempts = iter(outputs)
        kept = []
        def run():
            code, output = next(attempts)
            return CompletedProcess([], code, output, '')
        return module.run_audit(run, lambda n, output: kept.append((n, output))), kept

    def test_transient_failure_retains_first_attempt_and_can_recover(self):
        result, kept = self.exercise([(1, 'No application texts rendered: missing-0'), (0, 'passed')])
        self.assertEqual((0, 2), result)
        self.assertEqual(2, len(kept))

    def test_product_assertions_and_crashes_are_never_retried(self):
        for error in ['Control not found: Buscar', 'Chosen champion did not appear',
                      'FATAL EXCEPTION No application texts rendered: missing-0']:
            result, kept = self.exercise([(1, error)])
            self.assertEqual((1, 1), result)
            self.assertEqual(1, len(kept))

    def test_second_failure_still_fails_workflow(self):
        error = 'Android UI hierarchy did not become ready: empty'
        self.assertEqual((1, 2), self.exercise([(1, error), (1, error)])[0])
