import importlib.util
import json
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('coverage',Path(__file__).with_name('scanner-request-coverage.py'))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class CoverageTest(unittest.TestCase):
    def test_ios_families_and_unknown_sizes_never_become_pass(self):
        request = dict(brands=[dict(brand='Samsung',models=['Galaxy A13','Galaxy A13 5G']),
                              dict(brand='Apple',platform='iOS-no-Android-APK',models=['iPhone X'])])
        summary = dict(profiles=[dict(profile='a13-5g',model='Galaxy A13 5G',variant='original',status='PASS')])
        result = module.report(summary,request)
        self.assertEqual(['PENDIENTE_MEDIDA_Y_VARIANTE_CONFIRMADAS','IMAGEN_PASS','NO_COMPATIBLE_APK_ANDROID'],
                         [row['status'] for row in result['models']])
        self.assertEqual([],result['physicalDevicesTested'])

    def test_requested_profiles_have_unique_ids_dimensions_and_explicit_sources(self):
        import csv
        path = module.ROOT/'app/src/test/resources/scanner/requested-displays.csv'
        with path.open() as source:
            rows = list(csv.DictReader(source))
        self.assertEqual(43,len(rows))
        self.assertEqual(len(rows),len({row['id'] for row in rows}))
        self.assertTrue(all(int(row['width'])>=int(row['height'])>0 and row['specification'].startswith('https://') for row in rows))
        requested = json.loads((module.ROOT/'docs/scanner-requested-models.json').read_text())
        self.assertEqual(32,len(requested['brands']))
        self.assertEqual(32,len({row['brand'] for row in requested['brands']}))
