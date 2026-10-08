"""Exercise the production catalog, role ranking and access-dependent three-column rows."""
import runpy
from pathlib import Path

if __name__ == '__main__':
    runpy.run_path(str(Path(__file__).parent / 'tools/run-jvm-regression.py'))['verify']([
        'com.example.CoachMatchupCoverageTest', 'com.example.BuildChoiceRulesTest',
        'com.example.ChampionBuildsCatalogValidationTest'])
