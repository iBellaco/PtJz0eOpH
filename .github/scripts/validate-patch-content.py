#!/usr/bin/env python3
"""Reject production catalog content that still references an old patch."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]
EXPECTED = "7.3c"
OLD = re.compile(r"\b7\.3b\b", re.IGNORECASE)
FILES = [
    *sorted((ROOT / "app/src/main/assets").glob("*.json")),
    *sorted((ROOT / "app/src/main/res/raw").glob("*.json")),
    *sorted((ROOT / "app/src/main/java").rglob("*.kt")),
]


def main() -> int:
    errors = []
    for path in FILES:
        text = path.read_text(encoding="utf-8")
        if OLD.search(text):
            errors.append(f"{path.relative_to(ROOT)} contiene una referencia a 7.3b")
    if errors:
        print("PATCH_CONTENT_VALIDATION: FAILED")
        print("\n".join(errors))
        return 1
    print(f"PATCH_CONTENT_VALIDATION: OK (contenido de producción alineado con {EXPECTED})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
