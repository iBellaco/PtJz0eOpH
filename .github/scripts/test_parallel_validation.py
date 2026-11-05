"""Guard the real Actions graph, commands, evidence and publication gate."""
from pathlib import Path
import re
import json
import shlex
import subprocess
import sys
import unittest

import yaml


WORKFLOWS = Path(__file__).resolve().parents[1] / 'workflows'
EXPECTED_RENDERED_CLASSES = {
    'PortugueseRenderedAuditTest', 'PortugueseGeneratedSurfaceTest',
    'HubChampionNavigationTest', 'HistoryConsistencyRenderedTest',
    'BuildCoachingRenderedTest', 'SpellCatalogRenderedTest',
    'SupportReportDecoderTest', 'OfflinePortugueseAuditTest',
    'RuntimeBehaviorTest', 'RuntimeVisibilityTest', 'DraftChampionSelectionTest',
    'RequiredItemCorrectionsTest', 'ComponentItemsCatalogTest',
    'AccountDeletionRenderedTest',
}


class ParallelValidationTest(unittest.TestCase):
    def test_spanish_recheck_builds_current_apks_and_keeps_the_full_default_matrix(self):
        workflow = yaml.safe_load((WORKFLOWS / 'fix-error.yml').read_text())
        jobs = workflow['jobs']
        recheck = jobs['targeted-red-magic-release']
        matrix = recheck['strategy']['matrix']['include']
        self.assertEqual({'espanol-instalado', 'titulos-cortos', 'redmagic-secuencia-completa',
                          'xiaomi-secuencia-completa'}, {entry['suite'] for entry in matrix})
        self.assertIn("!inputs.spanish_only", recheck['if'])
        spanish = jobs['targeted-spanish-recheck']
        self.assertIn('inputs.spanish_only', spanish['if'])
        self.assertIn('shared-release-apks', spanish['needs'])
        self.assertIn('verify-draft-rival-names-device.py',
                      next(step for step in spanish['steps'] if 'script' in step.get('with', {}))['with']['script'])
        self.assertIn('shared-release-apks', recheck['needs'])
        self.assertNotIn('if', jobs['shared-release-apks'])
        self.assertNotIn('if', jobs['targeted-last-pick'])
        self.assertFalse(recheck.get('continue-on-error', False))

    def test_fix_sequence_target_belongs_to_the_selected_shard(self):
        workflow = yaml.safe_load((WORKFLOWS / 'fix-error.yml').read_text())
        steps = workflow['jobs']['targeted-installed-vi']['steps']
        plan = json.loads((WORKFLOWS.parents[1] /
            'app/src/test/resources/scanner/targeted-sequence-plan.json').read_text())
        step = next(s for s in steps if 'COACH_SEQUENCE_TARGETS' in s.get('env', {}))
        env = step['env']
        targets = set(env['COACH_SEQUENCE_TARGETS'].split(','))
        self.assertTrue(targets)
        self.assertEqual('false', env['COACH_SEQUENCE_PREFLIGHT'])
        selected = {c['id'] for c in plan['cases']
                    if c['shard'] == int(env['COACH_SEQUENCE_SHARD']) and c['id'] in targets}
        self.assertEqual(targets, selected,
            'Todos los casos solicitados deben pertenecer a la partición del plan reducido')

    @classmethod
    def setUpClass(cls):
        cls.workflow = yaml.safe_load((WORKFLOWS / 'build-apk.yml').read_text())
        cls.jobs = cls.workflow['jobs']

    def step(self, job, name):
        return next(step for step in self.jobs[job]['steps'] if step.get('name') == name)

    def test_both_jobs_are_parallel_mandatory_and_have_independent_budgets(self):
        for name, budget in [('core-tests', 75), ('rendered-audit', 60)]:
            job = self.jobs[name]
            self.assertEqual(['find-validated-apk', 'security-auth-tests'], job['needs'])
            self.assertEqual("needs.find-validated-apk.outputs.run_id == ''", job['if'])
            self.assertEqual(budget, job['timeout-minutes'])
            self.assertEqual('ubuntu-latest', job['runs-on'])
            self.assertFalse(job.get('continue-on-error', False))
            for step in job['steps']:
                self.assertFalse(step.get('continue-on-error', False))
        self.assertNotIn('core-and-rendered', self.jobs)

    def test_new_runner_has_the_same_complete_setup(self):
        core = self.jobs['core-tests']['steps'][:5]
        self.assertEqual(core, self.jobs['rendered-audit']['steps'][:5])
        self.assertEqual([
            'Checkout repository', 'Set up Java 21', 'Prepare environment files',
            'Set up Gradle 9.3.1', 'Restore Android client configuration from Actions Secrets',
        ], [step['name'] for step in core])

    def test_lint_and_unfiltered_unit_suite_remain_in_core(self):
        lint = self.step('core-tests', 'Check Android lint')
        self.assertIn('./gradlew :app:lintDebug --max-workers=1 --stacktrace --no-daemon', lint['run'])
        suite = self.step('core-tests', 'Run the complete Android unit test suite')
        self.assertEqual('./gradlew :app:testDebugUnitTest --fail-fast --max-workers=1 --stacktrace --no-daemon', suite['run'])
        self.assertEqual({'CI': 'true'}, suite['env'])
        self.assertNotIn('if', suite)
        self.assertFalse(any('Inspect rendered' in step.get('name', '')
                             for step in self.jobs['core-tests']['steps']))

    def test_all_rendered_classes_and_fresh_picture_recording_are_preserved(self):
        step = self.step('rendered-audit', 'Inspect rendered Portuguese screens')
        tokens = shlex.split(step['run'])
        classes = [tokens[index + 1] for index, token in enumerate(tokens) if token == '--tests']
        self.assertEqual({'com.example.' + name for name in EXPECTED_RENDERED_CLASSES}, set(classes))
        self.assertEqual(len(EXPECTED_RENDERED_CLASSES), len(classes))
        self.assertIn('--rerun', tokens)
        self.assertIn('-Proborazzi.test.record=true', tokens)
        self.assertEqual({'CI': 'true'}, step['env'])
        self.assertNotIn('if', step)
        self.assertFalse(any('complete Android unit' in s.get('name', '')
                             for s in self.jobs['rendered-audit']['steps']))

    def test_reports_keep_unique_names_and_are_retained_even_on_failure(self):
        expected = {
            'core-tests': {'android-lint-audit', 'core-regression-audit'},
            'rendered-audit': {'portuguese-rendered-audit', 'build-coaching-audit', 'portuguese-focused-audit'},
        }
        for job, names in expected.items():
            uploads = [step for step in self.jobs[job]['steps']
                       if step.get('uses', '').startswith('actions/upload-artifact@')]
            self.assertEqual(names, {step['with']['name'] for step in uploads})
            self.assertEqual(len(names), len(uploads))
            for step in uploads:
                self.assertEqual('always()', step['if'])
                self.assertEqual(14, step['with']['retention-days'])
            diagnostic = self.step(job, 'Publish failing Android cases without private data')
            self.assertEqual('failure()', diagnostic['if'])
            self.assertIn('report_failures', diagnostic['run'])

    def test_long_installed_audits_use_stable_software_rendering_and_clean_order(self):
        portuguese = self.step('installed-audit', 'Revisit installed APK in Portuguese')
        spanish = self.step('installed-spanish',
                            'Revisit installed APK in Spanish and exercise overlay history')
        for step in (portuguese, spanish):
            options = step['with']['emulator-options']
            self.assertIn('-gpu swiftshader', options)
            self.assertNotIn('swiftshader_indirect', options)

        portuguese_script = portuguese['with']['script']
        self.assertLess(portuguese_script.index('COACH_AUDIT_LANGUAGE=pt'),
                        portuguese_script.index('verify-native-ocr-device.py'))
        spanish_script = spanish['with']['script']
        self.assertLess(spanish_script.index('verify-draft-rival-names-device.py'),
                        spanish_script.index('verify-account-deletion-device.py'))

    def test_release_summary_and_cleanup_wait_for_both_jobs(self):
        for job in ['build-and-release', 'workflow-report']:
            self.assertTrue({'core-tests', 'rendered-audit'}.issubset(self.jobs[job]['needs']))
            self.assertEqual("always() && (inputs.target_portuguese_run == '' || inputs.test_build)", self.jobs[job]['if'])
        env = self.step('build-and-release', 'Require all validations to pass')['env']
        self.assertEqual('${{ needs.core-tests.result }}', env['CORE_RESULT'])
        self.assertEqual('${{ needs.rendered-audit.result }}', env['RENDERED_RESULT'])

    def test_targeted_portuguese_run_reuses_apk_and_skips_full_validation(self):
        targeted = self.jobs['portuguese-audit-only']
        self.assertIn("inputs.target_portuguese_run != '' && !inputs.test_build", targeted['if'])
        download = next(step for step in targeted['steps'] if step.get('name') == 'Reuse APKs from the completed validation')
        self.assertEqual('${{ inputs.target_portuguese_run }}', download['with']['run-id'])
        self.assertEqual('installed-audit-apks', download['with']['name'])
        self.assertEqual("inputs.target_portuguese_run == '' || inputs.test_build", self.jobs['find-validated-apk']['if'])
        script = next(step for step in targeted['steps'] if step.get('name') == 'Revisit only the installed APK in Portuguese')['with']['script']
        self.assertIn('COACH_AUDIT_LANGUAGE=pt python3 tools/audit-portuguese-device.py', script)

    def test_main_test_build_runs_full_gate_without_official_release(self):
        inputs = self.workflow['on']['workflow_dispatch']['inputs']
        self.assertEqual(False, inputs['test_build']['default'])
        find = self.step('find-validated-apk', 'Find a fully validated APK with identical source files')
        self.assertIn('!inputs.test_build', find['if'])
        self.assertIn("github.ref != 'refs/heads/main'", find['if'])
        build = self.step('build-apk', 'Build obfuscated release APK')
        self.assertIn('TEST_VERSION_ARG=(-PcoachTestIteration=1)', build['run'])
        self.assertEqual("${{ inputs.test_build && 'true' || 'false' }}", build['env']['COACH_TEST_BUILD'])
        self.assertIn('!inputs.test_build', self.step('find-validated-apk',
            'Validate production version is newer than the last release')['if'])
        self.assertNotIn('!inputs.test_build', self.step('find-validated-apk',
            'Check access to the persistent signing identity')['if'])
        restore = self.step('build-apk', 'Restore persistent debug keystore')['env']
        self.assertIn("github.ref == 'refs/heads/main'", restore['COACH_REQUIRE_PERSISTENT_SIGNING'])
        self.assertIn("github.ref == 'refs/heads/coach-validacion'", restore['COACH_REQUIRE_PERSISTENT_SIGNING'])
        self.assertNotIn('inputs.test_build', restore['COACH_ALLOW_EPHEMERAL_TEST_SIGNING'])
        self.assertNotIn('inputs.test_build', build['env']['COACH_ALLOW_EPHEMERAL_TEST_SIGNING'])
        for name in ('Verify the validated APK still carries the persistent signature',
                     'Create or Update GitHub Release'):
            self.assertIn('!inputs.test_build', self.step('build-and-release', name)['if'])
        self.assertEqual('inputs.test_build', self.step('build-and-release', 'Upload test APK')['if'])
        verify = self.step('build-and-release', 'Verify persistent signature of test APK')
        self.assertIn("github.ref == 'refs/heads/main'", verify['if'])
        self.assertIn('verify-apk-signing.py', verify['run'])
        self.assertEqual('true', verify['env']['COACH_REQUIRE_APK_PROVENANCE'])
        publish = self.step('build-and-release', 'Publish direct test downloads')
        self.assertIn("github.ref == 'refs/heads/main'", publish['if'])
        self.assertIn('--prerelease --latest=false', publish['run'])
        self.assertIn('steps.test_delivery.outputs.project_zip', publish['run'])
        self.assertIn('Coach-${TEST_VERSION}.apk', publish['run'])
        download = self.step('build-and-release', 'Download test APK')['with']
        self.assertEqual('${{ needs.find-validated-apk.outputs.run_id || github.run_id }}', download['run-id'])
        self.assertEqual('${{ secrets.GITHUB_TOKEN }}', download['github-token'])

    def gate(self, overrides=None):
        step = self.step('build-and-release', 'Require all validations to pass')
        script = step['run'].split("python3 - <<'PYRESULT'\n", 1)[1].rsplit('PYRESULT', 1)[0]
        env = {key: 'success' for key in step['env']}
        env['REUSE_RUN'] = ''
        env.update(overrides or {})
        return subprocess.run([sys.executable, '-c', script], env=env,
                              capture_output=True, text=True).returncode

    def test_real_publication_gate_rejects_each_non_green_result(self):
        self.assertEqual(0, self.gate())
        env = self.step('build-and-release', 'Require all validations to pass')['env']
        for key in env:
            if key.startswith('REUSE_'):
                continue
            for result in ['failure', 'cancelled', 'timed_out', 'skipped', '']:
                with self.subTest(key=key, result=result):
                    self.assertNotEqual(0, self.gate({key: result}))

    def test_reuse_path_requires_successful_lookup_and_all_audits_skipped(self):
        env = self.step('build-and-release', 'Require all validations to pass')['env']
        reused = {key: 'skipped' for key in env if not key.startswith('REUSE_')}
        reused.update(REUSE_RUN='42', REUSE_RESULT='success')
        self.assertEqual(0, self.gate(reused))
        for result in ['failure', 'cancelled', 'skipped', '']:
            self.assertNotEqual(0, self.gate({**reused, 'REUSE_RESULT': result}))
        for key in ['CORE_RESULT', 'RENDERED_RESULT']:
            self.assertNotEqual(0, self.gate({**reused, key: 'failure'}))

    def test_graph_has_no_missing_dependencies_or_cycles(self):
        def visit(name, ancestors):
            self.assertIn(name, self.jobs)
            self.assertNotIn(name, ancestors)
            dependencies = self.jobs[name].get('needs', [])
            if isinstance(dependencies, str):
                dependencies = [dependencies]
            for dependency in dependencies:
                visit(dependency, ancestors | {name})
        for job in self.jobs:
            visit(job, set())
            # All needs expressions must be declared, including the new gate entries.
            references = set(re.findall(r'needs\.([a-z-]+)\.', str(self.jobs[job])))
            declared = self.jobs[job].get('needs', [])
            self.assertTrue(references.issubset({declared} if isinstance(declared, str) else declared))

    def test_guard_runs_in_fix_and_full_validation(self):
        for filename, job in [('fix-error.yml', 'catalog-preflight'),
                              ('build-apk.yml', 'catalog-validation')]:
            workflow = yaml.safe_load((WORKFLOWS / filename).read_text())
            commands = '\n'.join(s.get('run', '') for s in workflow['jobs'][job]['steps'])
            self.assertIn('pip install --quiet PyYAML==6.0.3', commands)
            self.assertIn('unittest discover -s .github/scripts -p test_parallel_validation.py', commands)
            self.assertIn('unittest discover -s .github/scripts -p test_validated_apk.py', commands)


if __name__ == '__main__':
    unittest.main()
