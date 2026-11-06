import importlib.util
import unittest
from pathlib import Path


MODULE_PATH = Path(__file__).with_name("detect-validation-scope.py")
SPEC = importlib.util.spec_from_file_location("detect_validation_scope", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


class ValidationScopeTest(unittest.TestCase):
    def test_shared_build_files_force_full_scope(self):
        self.assertEqual(MODULE.classify_paths(["app/build.gradle.kts"]), "full")
        self.assertEqual(MODULE.classify_paths([".github/workflows/fix-error.yml"]), "full")

    def test_scanner_changes_are_targeted(self):
        self.assertEqual(MODULE.classify_paths(["app/src/main/java/com/example/service/screen/DraftVisionScanner.kt"]), "scanner")

    def test_sponsor_changes_are_targeted(self):
        self.assertEqual(MODULE.classify_paths(["server/economy/index.mjs"]), "sponsors")

    def test_ui_changes_are_targeted(self):
        self.assertEqual(MODULE.classify_paths(["app/src/main/res/values/strings.xml"]), "ui")

    def test_unknown_changes_are_full(self):
        self.assertEqual(MODULE.classify_paths(["app/src/main/java/com/example/data/SharedState.kt"]), "full")

    def test_docs_do_not_start_expensive_suites(self):
        outputs = MODULE.scope_outputs("docs")
        self.assertEqual(outputs["run_draft"], "false")
        self.assertEqual(outputs["run_sponsor"], "false")
        self.assertEqual(outputs["run_release"], "false")
        self.assertEqual(outputs["run_native"], "false")


if __name__ == "__main__":
    unittest.main()
