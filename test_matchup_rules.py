"""Exercise the production catalog, role ranking and access-dependent three-column rows."""
import runpy
from pathlib import Path

if __name__ == '__main__':
    runpy.run_path(str(Path(__file__).parent / 'tools/run-jvm-regression.py'))['verify']([
        'com.example.CoachMatchupCoverageTest', 'com.example.BuildChoiceRulesTest',
        'com.example.ChampionBuildsCatalogValidationTest',
        'com.example.DraftAutoStopTest', 'com.example.DraftSlotLifecycleTest', 'com.example.DraftSlotVisibilityTest', 'com.example.DraftStableTitlesTest', 'com.example.BannedPortraitScannerTest',
        'com.example.DeviceDraftProfileTest', 'com.example.CaptureCoordinateSpaceTest', 'com.example.DraftSessionLifecycleTest', 'com.example.PocoDebugCaptionLayoutTest', 'com.example.LiteRTViewerNavigationTest'])
