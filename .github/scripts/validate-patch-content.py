#!/usr/bin/env python3
"""Validate current patch declarations, preserving historical source evidence."""
import json
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[2]
CANONICAL = ROOT / "app/src/main/java/com/example/data/GamePatch.kt"
EXPECTED = re.search(r'const val VERSION = "(\d+\.\d+[a-z]?)"', CANONICAL.read_text()).group(1)
MENTION = re.compile(r'\b(?:Parche|Patch)\s+(\d+\.\d+[a-z]?)(?![\w.])', re.IGNORECASE)
FILES = [
    *sorted((ROOT / "app/src/main/assets").glob("*.json")),
    *sorted((ROOT / "app/src/main/res/raw").glob("*.json")),
    *sorted((ROOT / "app/src/main/java").rglob("*.kt")),
]
FILES.append(ROOT / "tools/build_coaching.py")


def valid_source_patch_bindings(scraper):
    sources = re.findall(r'GlobalScrapingSource\s*\(([^()]*)\)', scraper, re.DOTALL)
    ids = [re.match(r'\s*"([^"]+)"', source) for source in sources]
    return (len(sources) == 2 and all(ids)
            and {match.group(1) for match in ids} == {'wildriftfire', 'wildriftcore'}
            and all(re.search(r',\s*com\.example\.data\.GamePatch\.VERSION\s*,?\s*$', source)
                    for source in sources))


def validate_text(text, expected):
    family = re.sub(r'[a-z]$', '', expected)
    errors = [f"referencia de parche incompatible: {match.group(1)}"
              for match in MENTION.finditer(text) if match.group(1).lower() not in {expected, family}]
    # Validate explicit machine-readable current-patch fields too. Other
    # publishers' historical `patches` arrays are evidence, not active versions.
    try:
        document = json.loads(text)
    except (ValueError, TypeError):
        return errors
    def walk(value):
        if isinstance(value, dict):
            for key, child in value.items():
                if key in {"patch", "gamePatch", "currentPatch", "patchVersion"}:
                    if child != expected:
                        errors.append(f"{key}: se esperaba {expected}")
                else:
                    walk(child)
        elif isinstance(value, list):
            for child in value:
                walk(child)
    walk(document)
    return errors


def valid_evidence_patches(patches, expected):
    def key(value):
        match = re.fullmatch(r'(\d+)\.(\d+)([a-z]?)', value) if isinstance(value, str) else None
        return (int(match[1]), int(match[2]), match[3]) if match else None
    return isinstance(patches, list) and bool(patches) and all(
        key(patch) is not None and key(patch) <= key(expected) for patch in patches)


def main() -> int:
    errors = []
    # Translation dictionaries also preserve old, no-longer-referenced keys.
    # Only validate entries whose source strings occur in current production.
    active_text = '\n'.join(path.read_text(encoding='utf-8') for path in FILES
                            if not path.name.startswith('translations') and '/ui/' not in str(path))
    active_strings = set()
    for literal in re.findall(r'"(?:\\.|[^"\\])*"', active_text):
        try:
            active_strings.add(json.loads(literal))
        except ValueError:
            pass
    for path in FILES:
        text = path.read_text(encoding="utf-8")
        if path.name.startswith('translations'):
            entries = json.loads(text)
            text = json.dumps({key: value for key, value in entries.items() if key in active_strings}, ensure_ascii=False)
        elif path.suffix == '.kt' and '/ui/' in str(path):
            # UI examples (e.g. composing a notice) are not patch declarations.
            continue
        for error in validate_text(text, EXPECTED):
            errors.append(f"{path.relative_to(ROOT)}: {error}")
    references = json.loads((ROOT / "app/src/main/assets/matchup_references.json").read_text())
    for source in ("wildriftfire", "wildriftcore"):
        # These arrays record the version actually scraped from each publisher,
        # not the locally updated item catalog. Never relabel old evidence as new.
        patches = references["sources"][source]["patches"]
        if not valid_evidence_patches(patches, EXPECTED):
            errors.append(f"Fuente {source}: versión de evidencia ausente, inválida o futura")
    scraper = (ROOT / "app/src/main/java/com/example/data/sync/BestBuildWrScraper.kt").read_text()
    if not valid_source_patch_bindings(scraper):
        errors.append("Las dos fuentes activas deben usar GamePatch.VERSION")
    if errors:
        print("PATCH_CONTENT_VALIDATION: FAILED")
        print("\n".join(errors))
        return 1
    print(f"PATCH_CONTENT_VALIDATION: OK (contenido de producción alineado con {EXPECTED})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
