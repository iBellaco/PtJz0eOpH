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
        for group, devices in {'device-profiles': module.expected_devices()}.items():
            profiles = []
            for name, device in devices.items():
                width, height = int(device['width']), int(device['height'])
                scale = min(1.0, 1920 / width, 1080 / height)
                for variant in sorted(module.VARIANTS):
                    checks = [dict(slot=i, expected='champion', recognized='champion', confirmed='champion',
                                   crop=[0, (i % 5) * 70, 60, (i % 5) * 70 + 60],
                                   accepted=True, stableFramesExercised=2, score=.99, centerErrorReferencePixels=1)
                              for i in range(10)]
                    checks += [dict(ban=i, expected='banned', recognized='banned', state='CONFIRMED',
                                    scanFramesExercised=2, centerErrorReferencePixels=1) for i in range(10)]
                    profiles.append(dict(profile=name, variant=variant, model=device['model'],
                                         manufacturer=device['manufacturer'], specification=device['evidence'],
                                         display=[width, height], capture=[int(width * scale + .5), int(height * scale + .5)],
                                         checks=checks, status='PASS'))
            (directory / (group + '.json')).write_text(json.dumps(dict(
                sourceSha256=module.SOURCE_SHA256, profiles=profiles, failures=[])))
        (directory / 's26-screenshot-alignment.json').write_text(json.dumps(dict(
            measurements=[dict(captureWidth=1280, captureHeight=591, status='PASS')])))
        (directory / 'geometry-transformations.json').write_text(json.dumps(dict(status='PASS', cases=5133)))

    def test_complete_report_is_explicitly_simulated_and_exports_all_rows(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            self.fixtures(directory)
            summary = module.summarize(directory)
            self.assertEqual('PASS', summary['status'])
            devices = module.expected_devices()
            self.assertIn('galaxy-tab-s6-lite-2020', devices)
            self.assertIn('xiaomi-pad-6', devices)
            self.assertEqual(len(devices), summary['resolutionProfiles'])
            self.assertEqual(3 * len(devices), summary['simulatedCases'])
            self.assertEqual(60 * len(devices), summary['identityChecks'])
            self.assertEqual(len({(d['width'], d['height']) for d in devices.values()}), summary['uniqueDisplaySizes'])
            self.assertEqual([], summary['physicalDevicesTested'])
            module.write_reports(directory, summary)
            self.assertEqual(1 + summary['simulatedCases'], len((directory / 'audit-measurements.csv').read_text().splitlines()))
            self.assertIn('no significa error geométrico cero', (directory / 'audit-report.md').read_text())

    def test_missing_variants_wrong_identity_and_duplicate_positions_cannot_pass(self):
        for mutation in ['variant', 'identity', 'duplicates', 'confirmation', 'center', 'source', 's26', 'geometry',
                         'repeated_device', 'unknown_device', 'wrong_display', 'overlap']:
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
                elif mutation == 'repeated_device':
                    data['profiles'][3:6] = data['profiles'][:3]
                elif mutation == 'unknown_device':
                    for row in data['profiles'][:3]:
                        row['profile'] = 'unverified-screen'
                elif mutation == 'wrong_display':
                    first['display'] = [640, 360]
                elif mutation == 'overlap':
                    first['checks'][1]['crop'][1] = 50
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
