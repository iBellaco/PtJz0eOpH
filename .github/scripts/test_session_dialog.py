"""Fast source regression for the aliased buttons that blocked Android compilation.

This guard supplements, and never replaces, the Android compiler and UI tests.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[2]


class SessionDialogButtonsTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.source = (ROOT / 'app/src/main/java/com/example/ui/components/AdminDashboardDialog.kt').read_text()
        cls.dialog = cls.source.split('if (!isAdmin) {', 1)[1].split('DisposableEffect(context)', 1)[0]

    def test_calls_use_the_existing_import_alias(self):
        self.assertIn('import com.example.ui.components.CoachTextButton as TextButton', self.source)
        self.assertNotRegex(self.dialog, r'(?<![\w.])CoachTextButton\s*\(')
        self.assertEqual(2, len(re.findall(r'\bTextButton\s*\(', self.dialog)))

    def test_retry_keeps_the_refresh_action_and_loading_guard(self):
        self.assertIn('confirmButton = { TextButton(onClick = { AuthManager.refreshClaims() }, enabled = !checkingPermissions,', self.dialog)
        self.assertIn('Modifier.heightIn(min = 48.dp).testTag("permissions_retry")', self.dialog)

    def test_close_remains_available_and_accessible(self):
        self.assertIn('dismissButton = { TextButton(onClick = onDismiss,', self.dialog)
        self.assertIn('Modifier.heightIn(min = 48.dp).testTag("permissions_close")', self.dialog)


if __name__ == '__main__':
    unittest.main()
