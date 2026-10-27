"""Run installed Red Magic and adaptive POCO image regressions (not physical tests)."""
import subprocess


def main():
    for apk in (
        'app/build/outputs/apk/release/app-release.apk',
        'app/build/outputs/apk/androidTest/release/app-release-androidTest.apk',
    ):
        subprocess.run(['adb', 'install', '-r', apk], check=True, timeout=120)
    methods = (
        'liveRedMagicCapturesWithMasteryBadgesAndViewerMasksFillTheActualHub',
        'reportedSlotsDoNotLoseSennaOrMalphiteAndFinishWithVisualRivalPick',
        'pocoLegendaryReadsCurrentTitlesAfterAStaleSessionAndClearRemovesCrops',
    )
    for method in methods:
        result = subprocess.run(
            ['adb', 'shell', 'am', 'instrument', '-w', '-r', '-e', 'class',
             'com.example.DraftRivalNameInstalledTest#' + method,
             'com.Coach.test/androidx.test.runner.AndroidJUnitRunner'],
            capture_output=True, text=True, timeout=900)
        print(result.stdout + result.stderr, flush=True)
        if result.returncode != 0 or 'OK (1 test)' not in result.stdout:
            raise SystemExit(f'Falló la regresión instalada: {method}')


if __name__ == '__main__':
    main()
