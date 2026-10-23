import importlib.util
import json
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('scanner_summary', Path(__file__).with_name('scanner-audit-summary.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class ScannerAuditSummaryTest(unittest.TestCase):
    def fixtures(self, directory):
        for group, count in module.GROUPS.items():
            profiles = []
            for index in range(count):
                for variant in sorted(module.VARIANTS):
                    checks = [dict(slot=i, expected='champion', recognized='champion', confirmed='champion',
                                   accepted=True, stableFramesExercised=2, score=.99, centerErrorReferencePixels=1)
                              for i in range(10)]
                    checks += [dict(ban=i, expected='banned', recognized='banned', state='CONFIRMED',
                                    scanFramesExercised=2, centerErrorReferencePixels=1) for i in range(10)]
                    profiles.append(dict(profile=f'{group}-{index}', variant=variant, model='synthetic-unit-fixture',
                                         display=[2400, 1080], capture=[1920, 864], checks=checks, status='PASS'))
            (directory / (group + '.json')).write_text(json.dumps(dict(
                sourceSha256=module.SOURCE_SHA256, profiles=profiles, failures=[])))
        (directory / 's26-screenshot-alignment.json').write_text(json.dumps(dict(
            measurements=[dict(status='PASS') for _ in range(3)])))
        (directory / 'geometry-transformations.json').write_text(json.dumps(dict(status='PASS', cases=5133)))

    def test_complete_report_is_explicitly_simulated_and_exports_all_rows(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            self.fixtures(directory)
            summary = module.summarize(directory)
            self.assertEqual('PASS', summary['status'])
            self.assertEqual(118, summary['resolutionProfiles'])
            self.assertEqual(354, summary['simulatedCases'])
            self.assertEqual(7080, summary['identityChecks'])
            self.assertEqual(1, summary['uniqueDisplaySizes'])
            self.assertEqual([], summary['physicalDevicesTested'])
            module.write_reports(directory, summary)
            self.assertEqual(355, len((directory / 'audit-measurements.csv').read_text().splitlines()))
            self.assertIn('no significa error geométrico cero', (directory / 'audit-report.md').read_text())

    def test_missing_variants_wrong_identity_and_duplicate_positions_cannot_pass(self):
        for mutation in ['variant', 'identity', 'duplicates', 'confirmation', 'center', 'source', 's26', 'geometry']:
            with self.subTest(mutation=mutation), tempfile.TemporaryDirectory() as temporary:
                directory = Path(temporary)
                self.fixtures(directory)
                path = directory / 'device-profiles.json'
                data = json.loads(path.read_text())
                first = data['profiles'][0]
                if mutation == 'variant':
                    data['profiles'].pop(0)
                elif mutation == 'identity':
                    first['checks'][0]['recognized'] = 'incorrect'
                elif mutation == 'duplicates':
                    first['checks'][0]['slot'] = 1
                elif mutation == 'confirmation':
                    first['checks'][0]['confirmed'] = None
                elif mutation == 'center':
                    first['checks'][0]['centerErrorReferencePixels'] = 7
                elif mutation == 'source':
                    data['sourceSha256'] = 'wrong'
                elif mutation == 's26':
                    (directory / 's26-screenshot-alignment.json').write_text(json.dumps(dict(
                        measurements=[dict(status='FAIL') for _ in range(3)])))
                else:
                    (directory / 'geometry-transformations.json').unlink()
                path.write_text(json.dumps(data))
                summary = module.summarize(directory)
                self.assertEqual('FAIL', summary['status'])
                if mutation in ['identity', 'duplicates', 'confirmation', 'center']:
                    self.assertEqual('FAIL', summary['profiles'][0]['status'])
