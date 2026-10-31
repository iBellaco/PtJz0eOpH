import hashlib
import importlib.util
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('report', Path(__file__).with_name('scanner-device-report.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class PhysicalEvidenceTest(unittest.TestCase):
    def test_missing_observations_are_pending_not_passed(self):
        self.assertEqual('PENDING', module.evaluate({}, Path('.'))['status'])

    def test_requires_real_device_matching_evidence_and_measured_latency(self):
        with tempfile.TemporaryDirectory() as directory:
            base = Path(directory)
            evidence = base / 'capture.txt'
            evidence.write_text('synthetic test evidence; never a physical-device result')
            entry = {'expected': ['ahri'], 'detected': ['ahri'], 'latencyMs': 250,
                     'evidence': evidence.name, 'sha256': hashlib.sha256(evidence.read_bytes()).hexdigest()}
            report = {'version': 'test', 'calibrationProfile': 'test',
                      'device': {'model': 'fixture', 'size': '2400x1080', 'density': '420', 'android': '14', 'emulator': False},
                      'checks': dict.fromkeys(['orientation', 'profilePersistence', 'finalBuildRedirect'], True),
                      'observations': [dict(entry, kind='bans', atMs=i,
                                            expected=[f'ban{n}' for n in range(10)],
                                            detected=[f'ban{n}' for n in range(10)]) for i in range(3)] +
                                      [dict(entry, kind='pick', slot=i+1, atMs=i+3,
                                            expected=[f'champ{i}'], detected=[f'champ{i}']) for i in range(10)]}
            self.assertEqual('PASS', module.evaluate(report, base)['status'])
            for key, value in [('emulator', True)]:
                report['device'][key] = value
                self.assertEqual('FAIL', module.evaluate(report, base)['status'])
                report['device'][key] = False
            report['observations'][0]['detected'] = ['garen']
            self.assertEqual('FAIL', module.evaluate(report, base)['status'])
            report['observations'][0]['detected'] = report['observations'][0]['expected'][:]
            for entry in report['observations']:
                entry['latencyMs'] = 700
            self.assertEqual('FAIL', module.evaluate(report, base)['status'])
            evidence.write_text('changed')
            self.assertTrue(any('huella' in error for error in module.evaluate(report, base)['errors']))
