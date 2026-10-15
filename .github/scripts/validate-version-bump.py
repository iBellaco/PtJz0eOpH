"""Ensure a production build cannot reuse the version from an existing release."""

from pathlib import Path
import re
import subprocess
import sys


VERSION_CODE_RE = re.compile(r"^\s*versionCode\s*=\s*(\d+)\s*$", re.MULTILINE)
RELEASE_TAG_RE = re.compile(r"-b(\d+)$")


def version_code_from_gradle(path: Path) -> int:
    match = VERSION_CODE_RE.search(path.read_text(encoding="utf-8"))
    if not match:
        raise ValueError(f"No se encontró versionCode en {path}")
    return int(match.group(1))


def released_version_codes(tags: list[str]) -> list[int]:
    return [int(match.group(1)) for tag in tags
            if (match := RELEASE_TAG_RE.search(tag))]


def production_version_is_newer(current: int, tags: list[str]) -> tuple[bool, int | None]:
    released = released_version_codes(tags)
    latest = max(released) if released else None
    return latest is None or current > latest, latest


def main() -> int:
    try:
        current = version_code_from_gradle(Path("app/build.gradle.kts"))
        raw_tags = subprocess.check_output(
            ["git", "tag", "--list", "v*-b*"], text=True
        ).splitlines()
        valid, latest = production_version_is_newer(current, raw_tags)
    except (OSError, subprocess.CalledProcessError, ValueError) as error:
        print(f"ERROR: no se pudo validar la versión de producción: {error}", file=sys.stderr)
        return 1

    if not valid:
        print(
            f"ERROR: versionCode {current} no es mayor que el último release "
            f"({latest}). Incrementa versionCode y versionName antes de publicar.",
            file=sys.stderr,
        )
        return 1

    print(f"Versión de producción válida: versionCode {current} (último release: {latest or 'ninguno'})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
