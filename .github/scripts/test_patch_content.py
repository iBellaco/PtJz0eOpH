import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location("patch_content", Path(__file__).with_name("validate-patch-content.py"))
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class PatchContentTest(unittest.TestCase):
    def test_source_evidence_keeps_its_real_historical_version(self):
        self.assertTrue(module.valid_evidence_patches(['7.3c'], '7.3d'))
        self.assertTrue(module.valid_evidence_patches(['7.3d'], '7.3d'))
        for patches in [[], ['8.1'], ['7.3e'], ['unknown'], [None], '7.3c']:
            self.assertFalse(module.valid_evidence_patches(patches, '7.3d'), patches)

    def test_rejects_any_incorrect_current_patch(self):
        for value in ['Parche 7.3a', 'Patch 7.4', '{"patch":"8.1"}', '{"gamePatch":"7.3b"}']:
            self.assertTrue(module.validate_text(value, '7.3c'), value)

    def test_allows_current_patch_and_historical_evidence(self):
        for value in ['Parche 7.3c', 'Patch 7.3c', '{"patch":"7.3c"}',
                      '{"sources":{"historical":{"patches":["7.2b"]}}}', 'val scale = 0.7f']:
            self.assertEqual([], module.validate_text(value, '7.3c'), value)
