#!/usr/bin/env python3
"""Select the smallest safe fix-error validation scope for the changed files.

The full production gate remains unchanged. This classifier is intentionally
conservative: files with shared build, workflow, model, or test infrastructure
impact request the full fix-error scope instead of silently skipping coverage.
"""

from __future__ import annotations

import os
import subprocess
from pathlib import Path
from typing import Iterable


SCOPES = {"auto", "full", "scanner", "ui", "sponsors", "docs"}


def _changed_files() -> list[str]:
    before = os.environ.get("COACH_BEFORE_SHA", "").strip()
    current = os.environ.get("GITHUB_SHA", "HEAD").strip() or "HEAD"
    if not before or set(before) == {"0"}:
        return []
    result = subprocess.run(
        ["git", "diff", "--name-only", f"{before}..{current}"],
        check=True,
        capture_output=True,
        text=True,
    )
    return [line.strip() for line in result.stdout.splitlines() if line.strip()]


def _matches(path: str, prefixes: Iterable[str], tokens: Iterable[str] = ()) -> bool:
    return any(path.startswith(prefix) for prefix in prefixes) or any(
        token.lower() in path.lower() for token in tokens
    )


def classify_paths(paths: Iterable[str]) -> str:
    files = [Path(path).as_posix() for path in paths]
    if not files:
        return "full"

    full_prefixes = (
        ".github/",
        "gradle/",
        "build.gradle",
        "settings.gradle",
        "gradle.properties",
        "app/build.gradle",
        "app/proguard-rules",
        "tools/",
        "firestore.rules",
        "package.json",
        "package-lock.json",
    )
    scanner_prefixes = (
        "app/src/main/java/com/example/service/screen/",
        "app/src/main/java/com/example/vision/",
        "app/src/main/java/com/example/model/",
        "app/src/androidTest/java/com/example/Draft",
        "app/src/test/java/com/example/Draft",
        "app/src/test/resources/scanner/",
    )
    scanner_tokens = ("scanner", "capture", "ocr", "portrait", "multidevice")
    sponsor_prefixes = (
        "server/economy/",
        "server/account-deletion/",
        "tests/firestore/",
    )
    sponsor_tokens = ("sponsor", "economy", "essence", "firestore")
    ui_prefixes = (
        "app/src/main/java/com/example/ui/",
        "app/src/main/res/",
        "app/src/androidTest/java/com/example/",
        "app/src/test/java/com/example/",
    )
    ui_tokens = ("rendered", "overlay", "navigation", "tier", "premium")

    if any(_matches(path, full_prefixes) for path in files):
        return "full"
    if any(_matches(path, scanner_prefixes, scanner_tokens) for path in files):
        return "scanner"
    if any(_matches(path, sponsor_prefixes, sponsor_tokens) for path in files):
        return "sponsors"
    if any(_matches(path, ui_prefixes, ui_tokens) for path in files):
        return "ui"
    if all(path.startswith("docs/") or path in {"AGENTS.md", "INSTRUCCIONES_AGENTES.md"} for path in files):
        return "docs"
    return "full"


def selected_scope() -> str:
    requested = os.environ.get("COACH_VALIDATION_SCOPE", "auto").strip().lower() or "auto"
    if requested not in SCOPES:
        raise SystemExit(f"Alcance no reconocido: {requested}")
    if os.environ.get("COACH_SPANISH_ONLY", "false").lower() == "true":
        return "scanner"
    if os.environ.get("COACH_SCANNER_REGRESSIONS", "false").lower() == "true":
        return "scanner"
    if requested != "auto":
        return requested
    if os.environ.get("GITHUB_EVENT_NAME") != "push":
        return "full"
    return classify_paths(_changed_files())


def scope_outputs(scope: str) -> dict[str, str]:
    return {
        "scope": scope,
        "run_draft": str(scope in {"full", "scanner", "ui"}).lower(),
        "run_sponsor": str(scope in {"full", "sponsors"}).lower(),
        "run_release": str(scope in {"full", "scanner", "ui"}).lower(),
        "run_native": str(scope in {"full", "scanner"}).lower(),
    }


def main() -> None:
    outputs = scope_outputs(selected_scope())
    target = os.environ.get("GITHUB_OUTPUT")
    text = "\n".join(f"{key}={value}" for key, value in outputs.items()) + "\n"
    if target:
        Path(target).write_text(text, encoding="utf-8")
    print(text, end="")


if __name__ == "__main__":
    main()
