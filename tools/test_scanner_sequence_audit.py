import importlib.util
from pathlib import Path
import unittest


def module(name):
    spec = importlib.util.spec_from_file_location(name, Path(__file__).with_name(name + '.py'))
    value = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(value)
    return value


planner = module('scanner-sequence-plan')
reporter = module('scanner-sequence-summary')


class SequenceAuditTest(unittest.TestCase):
    def fixture(self):
        profiles = [dict(profile=name, model=name, display=[2400, 1080], capture=[1920, 864],
                         layout='uniform-fit-reference', specification=None, variant='original')
                    for name in ['red-magic-reference', 'poco-x7-pro', 'redmi', 'xiaomi']]
        return dict(status='PASS', sourceSha256=planner.SOURCE_SHA256, profiles=profiles, resolutionProfiles=4)

    def reports(self, fixture):
        cases = []
        for case in planner.make_plan(fixture)['cases']:
            steps = [dict(step=n, frames=1 if n == 0 else 2,
                          recognized={str(i): 'name' for i in range(n)}, expected={str(i): 'name' for i in range(n)},
                          roles={str(i): 'MID' for i in range(5)}) for n in range(10)]
            cases.append(dict(case, status='PASS', failures=[], steps=steps,
                              tenth=dict(expected='vi', recognized='vi', confirmed=True, nameHidden=True)))
        return [dict(sourceSha256=planner.SOURCE_SHA256, cases=cases)]

    def test_equal_dimensions_share_pixels_without_losing_models_or_known_device_orders(self):
        plan = planner.make_plan(self.fixture())
        self.assertEqual(4, plan['modelProfiles'])
        self.assertEqual(4, len(plan['cases']))
        self.assertEqual([], plan['physicalDevicesTested'])
        self.assertEqual({'red-magic-reference', 'poco-x7-pro', 'redmi', 'xiaomi'},
                         {profile['profile'] for case in plan['cases'] for profile in case['profiles']})
        self.assertEqual('PASS', reporter.summarize(self.fixture(), self.reports(self.fixture()))['status'])

    def test_missing_ocr_steps_wrong_names_tenth_duplicates_and_changes_cannot_pass(self):
        for mutation in ['missing', 'step', 'name', 'tenth', 'duplicate', 'geometry', 'source', 'roles']:
            with self.subTest(mutation=mutation):
                fixture = self.fixture()
                reports = self.reports(fixture)
                first = reports[0]['cases'][0]
                if mutation == 'missing':
                    reports[0]['cases'].pop()
                elif mutation == 'step':
                    first['steps'].pop()
                elif mutation == 'name':
                    first['steps'][4]['recognized']['0'] = 'wrong'
                elif mutation == 'tenth':
                    first['tenth']['confirmed'] = False
                elif mutation == 'duplicate':
                    reports[0]['cases'].append(first)
                elif mutation == 'geometry':
                    first['capture'] = [1, 1]
                elif mutation == 'source':
                    reports[0]['sourceSha256'] = 'wrong'
                else:
                    first['steps'][4]['roles'].pop('0')
                self.assertEqual('FAIL', reporter.summarize(fixture, reports)['status'])

    def test_unapproved_or_incomplete_portrait_matrix_is_not_a_native_plan(self):
        for mutation in ['failed', 'source', 'missing']:
            fixture = self.fixture()
            if mutation == 'failed':
                fixture['status'] = 'FAIL'
            elif mutation == 'source':
                fixture['sourceSha256'] = 'wrong'
            else:
                fixture['profiles'].pop()
            with self.assertRaises(ValueError):
                planner.make_plan(fixture)
