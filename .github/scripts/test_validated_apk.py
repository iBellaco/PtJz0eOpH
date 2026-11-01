import importlib.util
from pathlib import Path
import unittest
import re

spec = importlib.util.spec_from_file_location('validated_apk', Path(__file__).with_name('find-validated-apk.py'))
validated = importlib.util.module_from_spec(spec)
spec.loader.exec_module(validated)


class ValidatedApkTest(unittest.TestCase):
    repository = 'example/coach'
    fixture_run = {'id': 42, 'status': 'completed', 'conclusion': 'success', 'event': 'pull_request',
           'head_branch': 'coach-validacion', 'head_repository': {'full_name': repository}, 'head_sha': 'a' * 40}

    workflow = (Path(__file__).parents[1] / 'workflows/build-apk.yml').read_text()
    workflow_jobs = set(re.findall(r'^  ([a-z][a-z-]+):$', workflow, re.MULTILINE))

    def invoke(self, changed_tree=False, failed_job=None, expired=False, run=None, files=None, missing_job=None,
               failed_conclusion='failure'):
        def request(path):
            if '/git/commits/' in path:
                return {'tree': {'sha': 'different' if changed_tree and path.endswith('a' * 40) else 'same'}}
            if '/git/trees/' in path:
                changed = files if files is not None else [{'filename': 'app/src/main/Example.kt'}]
                entries = [{'path': 'app/build.gradle.kts', 'type': 'blob', 'sha': 'base'}]
                for f in changed:
                    entries.append({'path': f.get('previous_filename', f['filename'])
                                    if '/different?' in path else f['filename'],
                                    'type': 'blob', 'sha': 'old' if '/different?' in path else 'new'})
                return {'truncated': len(changed) >= 300, 'tree': entries}
            if '/workflows/' in path:
                return {'workflow_runs': [run or self.fixture_run]}
            if '/jobs?' in path:
                return {'jobs': [{'name': name, 'conclusion': failed_conclusion if failed_job == name else 'success'}
                                 for name in self.workflow_jobs if name != missing_job]}
            if '/artifacts?' in path:
                return {'artifacts': [{'name': 'app-release.apk', 'expired': expired}]}
            self.fail(path)
        return validated.find(self.repository, 'b' * 40, request)

    def test_identical_fully_validated_tree_is_reused(self):
        self.assertEqual(('42', 'a' * 40), self.invoke())

    def test_validation_push_and_manual_runs_require_the_same_checks(self):
        for event in ['push', 'workflow_dispatch']:
            run = dict(self.fixture_run, event=event)
            self.assertEqual(('42', 'a' * 40), self.invoke(run=run))
            self.assertEqual(('', ''), self.invoke(run=run, failed_job='installed-audit'))
            self.assertEqual(('', ''), self.invoke(run=run, expired=True))
            self.assertEqual(('', ''), self.invoke(run=run, changed_tree=True))

    def test_any_source_change_requires_new_validation(self):
        self.assertEqual(('', ''), self.invoke(changed_tree=True))

    def test_delivery_housekeeping_can_reuse_the_installed_apk(self):
        self.assertEqual(('42', 'a' * 40), self.invoke(changed_tree=True,
            files=[{'filename': p} for p in validated.DELIVERY_ONLY_FILES]))

    def test_build_inputs_and_renamed_source_cannot_be_excluded(self):
        for filename in ['app/build.gradle.kts', '.github/workflows/build-apk.yml',
                         'gradle/libs.versions.toml', 'AGENTS.md',
                         'docs/draft-recognition-contract.md',
                         'app/src/test/resources/scanner/requested-displays.csv',
                         'app/src/test/resources/portraits/draft-kaisa-nine.jpg']:
            self.assertEqual(('', ''), self.invoke(changed_tree=True, files=[{'filename': filename}]))
        self.assertEqual(('', ''), self.invoke(changed_tree=True, files=[{
            'filename': '.github/scripts/cleanup-completed-runs.cjs',
            'previous_filename': 'app/src/main/Example.kt'}]))
        self.assertEqual(('42', 'a' * 40), self.invoke(changed_tree=True, files=[]))
        self.assertEqual(('', ''), self.invoke(changed_tree=True,
            files=[{'filename': '.github/scripts/cleanup-completed-runs.cjs'}] * 300))

    def test_failed_installed_audit_prevents_reuse(self):
        self.assertEqual(('', ''), self.invoke(failed_job='installed-audit'))

    def test_both_parallel_jobs_must_pass_before_reuse(self):
        for name in ['core-tests', 'rendered-audit']:
            with self.subTest(job=name):
                self.assertEqual(('', ''), self.invoke(missing_job=name))
                for conclusion in ['failure', 'cancelled', 'timed_out', 'skipped', None]:
                    self.assertEqual(('', ''), self.invoke(failed_job=name, failed_conclusion=conclusion))

    def test_required_jobs_exist_in_the_actual_workflow(self):
        self.assertTrue(validated.REQUIRED_JOBS.issubset(self.workflow_jobs))

    def test_current_catalog_scanner_and_authentication_are_mandatory(self):
        for name in ['catalog-validation', 'scanner-tests', 'security-auth-tests']:
            self.assertEqual(('', ''), self.invoke(failed_job=name))
            self.assertEqual(('', ''), self.invoke(missing_job=name))

    def test_failed_spanish_overlay_audit_prevents_reuse(self):
        self.assertEqual(('', ''), self.invoke(failed_job='installed-spanish'))

    def test_missing_or_failed_native_sequence_matrix_prevents_reuse(self):
        self.assertEqual(('', ''), self.invoke(failed_job='scanner-sequence-audit'))
        self.assertEqual(('', ''), self.invoke(missing_job='scanner-sequence-audit'))
        self.assertEqual(('', ''), self.invoke(failed_job='native-title-preflight'))
        self.assertEqual(('', ''), self.invoke(missing_job='native-title-preflight'))

    def test_parallel_build_cannot_publish_before_full_and_native_regressions(self):
        release = self.workflow.split('  build-and-release:', 1)[1].split('  cleanup-history:', 1)[0]
        for dependency in ['core-tests', 'rendered-audit', 'installed-audit', 'installed-spanish', 'scanner-sequence-audit','native-title-preflight']:
            self.assertIn('    - ' + dependency + '\n', release)
        self.assertIn("'RENDERED_RESULT', 'DEVICE_RESULT', 'SPANISH_RESULT', 'SEQUENCE_RESULT'", release)
        self.assertIn('if any(value != expected for value in results.values())', release)

    def test_picture_evidence_is_regenerated_even_when_test_results_are_cached(self):
        for marker in ['com.example.PortugueseRenderedAuditTest',
                       'com.example.DraftMultiDeviceAuditTest.formerlyFailingCentersStayAlignedWhenPortraitsChange',
                       'com.example.DraftMultiDeviceAuditTest --tests']:
            command = next(line for line in self.workflow.splitlines() if 'run: ./gradlew' in line and marker in line)
            self.assertIn(':app:testDebugUnitTest --rerun ', command)

    def test_expired_artifact_prevents_reuse(self):
        self.assertEqual(('', ''), self.invoke(expired=True))

    def test_foreign_or_unfinished_runs_prevent_reuse(self):
        for change in [{'status': 'in_progress'}, {'conclusion': 'failure'}, {'event': 'pull_request_target'},
                       {'head_branch': 'other'}, {'head_repository': {'full_name': 'other/coach'}},
                       {'head_sha': 'invalid'}]:
            self.assertEqual(('', ''), self.invoke(run={**self.fixture_run, **change}))


if __name__ == '__main__':
    unittest.main()
