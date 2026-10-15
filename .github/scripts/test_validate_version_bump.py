import importlib.util
from pathlib import Path
import unittest


SCRIPT = Path(__file__).with_name("validate-version-bump.py")
spec = importlib.util.spec_from_file_location("validate_version_bump", SCRIPT)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class ValidateVersionBumpTest(unittest.TestCase):
    def test_reads_gradle_version_code(self):
        self.assertEqual(
            module.VERSION_CODE_RE.search("versionCode = 993").group(1), "993"
        )

    def test_ignores_non_release_tags(self):
        self.assertEqual(
            module.released_version_codes(["v1.1.10.276-b992", "draft", "v1.0-b7"]),
            [992, 7],
        )

    def test_requires_a_newer_code(self):
        self.assertEqual(module.production_version_is_newer(993, ["v1.1-b992"]), (True, 992))
        self.assertEqual(module.production_version_is_newer(992, ["v1.1-b992"]), (False, 992))

    def test_first_release_has_no_floor(self):
        self.assertEqual(module.production_version_is_newer(1, ["preview"]), (True, None))


if __name__ == "__main__":
    unittest.main()
