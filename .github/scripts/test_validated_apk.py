import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('validated_apk', Path(__file__).with_name('find-validated-apk.py'))
validated = importlib.util.module_from_spec(spec)
spec.loader.exec_module(validated)


class ValidatedApkTest(unittest.TestCase):
    repository = 'example/coach'
    fixture_run = {'id': 42, 'status': 'completed', 'conclusion': 'success', 'event': 'pull_request',
           'head_branch': 'pruebas', 'head_repository': {'full_name': repository}, 'head_sha': 'a' * 40}

    def invoke(self, changed_tree=False, failed_job=False, expired=False, run=None):
        def request(path):
            if '/git/commits/' in path:
                return {'tree': {'sha': 'different' if changed_tree and path.endswith('a' * 40) else 'same'}}
            if '/workflows/' in path:
                return {'workflow_runs': [run or self.fixture_run]}
            if '/jobs?' in path:
                return {'jobs': [{'name': name, 'conclusion': 'failure' if failed_job and name == 'installed-audit' else 'success'} for name in validated.REQUIRED_JOBS]}
            if '/artifacts?' in path:
                return {'artifacts': [{'name': 'app-debug.apk', 'expired': expired}]}
            self.fail(path)
        return validated.find(self.repository, 'b' * 40, request)

    def test_identical_fully_validated_tree_is_reused(self):
        self.assertEqual(('42', 'a' * 40), self.invoke())

    def test_any_source_change_requires_new_validation(self):
        self.assertEqual(('', ''), self.invoke(changed_tree=True))

    def test_failed_installed_audit_prevents_reuse(self):
        self.assertEqual(('', ''), self.invoke(failed_job=True))

    def test_expired_artifact_prevents_reuse(self):
        self.assertEqual(('', ''), self.invoke(expired=True))

    def test_foreign_or_unfinished_runs_prevent_reuse(self):
        for change in [{'status': 'in_progress'}, {'conclusion': 'failure'}, {'event': 'push'},
                       {'head_branch': 'other'}, {'head_repository': {'full_name': 'other/coach'}},
                       {'head_sha': 'invalid'}]:
            self.assertEqual(('', ''), self.invoke(run={**self.fixture_run, **change}))


if __name__ == '__main__':
    unittest.main()
