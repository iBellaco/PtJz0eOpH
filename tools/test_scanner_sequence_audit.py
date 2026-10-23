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
                         layout='uniform-fit-reference', calibration={'nameBandScale': 1}, specification=None, variant='original')
                    for name in ['red-magic-reference', 'poco-x7-pro', 'redmi', 'xiaomi']]
        return dict(status='PASS', sourceSha256=planner.SOURCE_SHA256, profiles=profiles, resolutionProfiles=4)

    def reports(self, fixture):
        cases = []
        for case in planner.make_plan(fixture)['cases']:
            steps = [dict(step=n, frames=1 if n == 0 else 2,
                          recognized=reporter.expected_names(case['firstPick'], n),
                          expected=reporter.expected_names(case['firstPick'], n), roles=dict(reporter.ROLES)) for n in range(10)]
            last_id = 'teemo'
            cases.append(dict(case, status='PASS', failures=[], steps=steps,
                              tenth=dict(expected=last_id, recognized=last_id, confirmed=True, nameHidden=True)))
        return [dict(sourceSha256=planner.SOURCE_SHA256, cases=cases, instrumentationPassed=True)]

    def test_equal_dimensions_share_pixels_without_losing_models_or_known_device_orders(self):
        plan = planner.make_plan(self.fixture())
        self.assertEqual(4, plan['modelProfiles'])
        self.assertEqual(4, len(plan['cases']))
        self.assertEqual([], plan['physicalDevicesTested'])
        self.assertEqual({'red-magic-reference', 'poco-x7-pro', 'redmi', 'xiaomi'},
                         {profile['profile'] for case in plan['cases'] for profile in case['profiles']})
        self.assertEqual('PASS', reporter.summarize(self.fixture(), self.reports(self.fixture()))['status'])

    def test_missing_ocr_steps_wrong_names_tenth_duplicates_and_changes_cannot_pass(self):
        for mutation in ['missing', 'step', 'name', 'forged_expected', 'tenth', 'duplicate', 'geometry', 'source', 'roles', 'wrong_role', 'transport']:
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
                elif mutation == 'forged_expected':
                    first['steps'][4]['recognized']['0'] = 'wrong'
                    first['steps'][4]['expected']['0'] = 'wrong'
                elif mutation == 'tenth':
                    first['tenth']['confirmed'] = False
                elif mutation == 'duplicate':
                    reports[0]['cases'].append(first)
                elif mutation == 'geometry':
                    first['capture'] = [1, 1]
                elif mutation == 'source':
                    reports[0]['sourceSha256'] = 'wrong'
                elif mutation == 'roles':
                    first['steps'][4]['roles'].pop('0')
                elif mutation == 'transport':
                    reports[0]['instrumentationPassed'] = False
                else:
                    first['steps'][4]['roles']['0'] = 'MID'
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

    def test_cutout_margins_do_not_share_an_unshifted_native_sequence(self):
        fixture = self.fixture()
        fixture['profiles'][2]['safeInsets'] = [.05,0,0,.03]
        fixture['profiles'][3]['safeInsets'] = [0,0,.05,.03]
        plan = planner.make_plan(fixture)
        self.assertEqual(6,len(plan['cases']))
        self.assertEqual({(0,0,0,0),(.05,0,0,.03),(0,0,.05,.03)},
                         {tuple(case['safeInsets']) for case in plan['cases']})
