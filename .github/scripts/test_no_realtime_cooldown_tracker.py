"""Guards the removal of the real-time ability cooldown tracker UI."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[2]
SOURCE_ROOT = ROOT / "app/src/main"
REMOVED_FILE = SOURCE_ROOT / "java/com/example/ui/components/CooldownTrackerPanel.kt"


class NoRealtimeCooldownTrackerTest(unittest.TestCase):
    def test_tracker_file_and_state_holder_are_removed(self):
        self.assertFalse(REMOVED_FILE.exists())
        production = "\n".join(
            path.read_text(encoding="utf-8")
            for path in SOURCE_ROOT.rglob("*.kt")
            if path != REMOVED_FILE
        )
        self.assertNotIn("CooldownTrackerPanel", production)
        self.assertNotIn("CooldownTrackerStateHolder", production)

    def test_float_and_auxiliary_screens_have_no_tracker_navigation(self):
        production = "\n".join(
            path.read_text(encoding="utf-8")
            for path in SOURCE_ROOT.rglob("*.kt")
        ).lower()
        for marker in ("cd tracker", "cooldown tracker", "cooldowntrackerpanel", "cooldowntrackerstateholder"):
            self.assertNotIn(marker, production)


if __name__ == "__main__":
    unittest.main()
