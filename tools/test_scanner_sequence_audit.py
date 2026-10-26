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
    def test_known_full_release_failures_are_in_the_isolated_plan(self):
        import json
        root = Path(__file__).resolve().parents[1]
        plan = json.loads((root / 'app/src/test/resources/scanner/targeted-sequence-plan.json').read_text())
        cases = {case['id']: case for case in plan['cases']}
        for name, dimensions in [('sequence-0-enemy-first', [1920, 869]),
                                 ('sequence-42-ally-first', [1920, 863])]:
            self.assertEqual(dimensions, cases[name]['capture'])
            self.assertEqual(0, cases[name]['shard'])
        workflow = (root / '.github/workflows/fix-error.yml').read_text()
        for name in ['sequence-0-enemy-first', 'sequence-42-ally-first']:
            self.assertIn("preflight: 'false'\n            targets: '" + name + "'", workflow)

    def fixture(self):
        profiles = [dict(profile=name, model=name, manufacturer='Xiaomi', display=[2400, 1080], capture=[1920, 864],
                         layout='uniform-fit-reference', calibration={'nameBandScale': 1}, specification=None, variant='original')
                    for name in ['red-magic-reference', 'poco-x7-pro', 'redmi', 'xiaomi']]
        return dict(status='PASS', sourceSha256=planner.SOURCE_SHA256, profiles=profiles, resolutionProfiles=4)

    def reports(self, fixture):
        cases = []
        for case in planner.make_plan(fixture)['cases']:
            steps = [dict(step=n, frames=2,
                          recognized=reporter.expected_names(case['firstPick'], n),
                          expected=reporter.expected_names(case['firstPick'], n), roles=dict(reporter.ROLES)) for n in range(10)]
            last_id = 'teemo'
            cases.append(dict(case, status='PASS', failures=[], steps=steps,validationMode='full',
                              realCaptures=[dict(file=file,sha256=ground['sha256'],expectedTitles=dict(ground['titles']),
                                  recognizedTitles=dict(ground['titles']),frames=2,visualCheck=ground['visual'],
                                  tenthExpected=ground['tenth'],tenthRecognized=ground['tenth'])
                                  for file,ground in reporter.REAL_CAPTURES.items()],
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

    def test_generic_geometry_does_not_gate_native_phone_ocr(self):
        fixture = self.fixture()
        fixture['profiles'].append(dict(fixture['profiles'][2], profile='generic-640-360',
                                        manufacturer='generic', model='resolution-simulation',
                                        display=[640, 360], capture=[640, 360]))
        fixture['resolutionProfiles'] += 1
        plan = planner.make_plan(fixture)
        self.assertEqual(4, plan['modelProfiles'])
        self.assertEqual(1, plan['geometryOnlyProfiles'])
        self.assertNotIn('generic-640-360', {row['profile'] for case in plan['cases']
                                            for row in case['profiles']})
        self.assertEqual('PASS', reporter.summarize(fixture, self.reports(fixture))['status'])

    def test_non_android_and_tablet_profiles_do_not_gate_the_overlay_release(self):
        fixture = self.fixture()
        template = fixture['profiles'][2]
        fixture['profiles'] += [
            dict(template, profile='iphone-14', manufacturer='Apple', model='iPhone 14'),
            dict(template, profile='xiaomi-pad-6', manufacturer='Xiaomi', model='Xiaomi Pad 6'),
            dict(template, profile='android-tablet', manufacturer='Samsung', model='Android Tablet'),
        ]
        fixture['resolutionProfiles'] += 3
        plan = planner.make_plan(fixture)
        selected = {row['profile'] for case in plan['cases'] for row in case['profiles']}
        self.assertEqual({'red-magic-reference', 'poco-x7-pro', 'redmi', 'xiaomi'}, selected)
        self.assertEqual(4, plan['modelProfiles'])
        self.assertEqual(3, plan['geometryOnlyProfiles'])

    def test_native_phone_matrix_is_split_into_bounded_shards(self):
        fixture = self.fixture()
        template = fixture['profiles'][2]
        fixture['profiles'] = [dict(template, profile=f'phone-{index}', capture=[1920, 700 + index],
                                    display=[2400, 900 + index])
                               for index in range(planner.SHARD_COUNT * 2)]
        fixture['resolutionProfiles'] = len(fixture['profiles'])
        plan = planner.make_plan(fixture)
        self.assertEqual(set(range(planner.SHARD_COUNT)), {case['shard'] for case in plan['cases']})
        self.assertLessEqual(max(sum(case['shard'] == shard for case in plan['cases'])
                                 for shard in range(planner.SHARD_COUNT)), 2)

    def test_missing_ocr_steps_wrong_names_tenth_duplicates_and_changes_cannot_pass(self):
        for mutation in ['missing', 'step', 'name', 'forged_expected', 'tenth', 'duplicate', 'geometry', 'source', 'roles', 'wrong_role', 'transport','preflight']:
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
                elif mutation == 'preflight':
                    first['validationMode']='preflight-not-release'
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

    def test_missing_forged_real_capture_or_early_visual_selection_cannot_pass(self):
        for mutation in ['missing','duplicate','hash','forged','visual']:
            fixture=self.fixture()
            reports=self.reports(fixture)
            real=reports[0]['cases'][0]['realCaptures']
            if mutation=='missing': real.pop()
            elif mutation=='duplicate': real.append(real[0])
            elif mutation=='hash': real[0]['sha256']='wrong'
            elif mutation=='visual': real[0]['tenthRecognized']='vi'
            else:
                real[0]['recognizedTitles']['0']='wrong'
                real[0]['expectedTitles']['0']='wrong'
            self.assertEqual('FAIL',reporter.summarize(fixture,reports)['status'])

    def test_cutout_margins_do_not_share_an_unshifted_native_sequence(self):
        fixture = self.fixture()
        fixture['profiles'][2]['safeInsets'] = [.05,0,0,.03]
        fixture['profiles'][3]['safeInsets'] = [0,0,.05,.03]
        plan = planner.make_plan(fixture)
        self.assertEqual(6,len(plan['cases']))
        self.assertEqual({(0,0,0,0),(.05,0,0,.03),(0,0,.05,.03)},
                         {tuple(case['safeInsets']) for case in plan['cases']})

    def test_new_documented_dimensions_also_get_the_non_release_preflight(self):
        fixture=self.fixture()
        dimensions=[(1560,720),(1544,720),(1570,720),(1640,720),(2270,1080),(2244,1080),
                    (2240,1080),(2388,1080),(2400,1176),(2616,1212),(2720,1260)]
        template=fixture['profiles'][2]
        fixture['profiles']=[dict(template,profile=f'new-{w}-{h}',display=[w,h],capture=[w,h]) for w,h in dimensions]
        fixture['resolutionProfiles']=len(dimensions)
        plan=planner.make_plan(fixture)
        self.assertEqual(len(dimensions),len(plan['cases']))
        self.assertTrue(all(case['preflight'] for case in plan['cases']))
