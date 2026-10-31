#!/usr/bin/env python3
"""Refine bundled alternatives without replacing each champion's core build.

The conditions mirror BuildChoiceRules. No alternative is required for every build.
Run from the repository root after regenerating the bundled build catalog.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app/src/main/assets"
PAIRS = {
    "Botas de mercurio": "Trituradoras encadenadas",
    "Botas blindadas": "Avance blindado",
    "Botas jonias de la lucidez": "Lucidez carmesí",
}
REASONS = {
    "Botas de mercurio": "Contra daño mágico y controles de masas que te impiden mantenerte en la primera línea.",
    "Botas blindadas": "Contra tiradores y duelistas cuyo daño depende de ataques básicos.",
    "Botas jonias de la lucidez": "Si necesitas lanzar escudos, curaciones y controles con más frecuencia.",
}


def alternative(champion, role, primary):
    frontline = champion.get("isFrontline", False)
    ranged = champion.get("isRanged", False)
    physical = champion.get("damageType") == "PHYSICAL"
    if frontline and champion["id"] not in {"olaf", "dr_mundo", "drmundo"}:
        boot = "Botas de mercurio"
    elif ranged and physical:
        boot = "Botas blindadas"
    elif role.startswith("Soporte") and not frontline:
        boot = "Botas jonias de la lucidez"
    elif role.startswith("Línea de Barón") and not ranged:
        boot = "Botas blindadas"
    elif role.startswith("Jungla") and physical:
        boot = "Botas jonias de la lucidez"
    else:
        return None
    if boot.casefold() == primary.casefold():
        return None
    reason = REASONS[boot]
    if boot == "Botas blindadas" and not ranged:
        reason = "Si el rival de línea depende de ataques básicos y necesitas sobrevivir a sus intercambios."
    elif boot == "Botas jonias de la lucidez" and role.startswith("Jungla"):
        reason = "Si priorizas más rotaciones de habilidades y hechizos sobre el daño de una sola ráfaga."
    return {"itemName": boot, "description": reason}


def refine():
    champions = {}
    for part in (1, 2):
        for champion in json.loads((ROOT / f"app/src/main/res/raw/champions_part{part}.json").read_text()):
            champions[champion["id"]] = champion
    path = ASSETS / "champions_creator_builds.json"
    builds = json.loads(path.read_text())
    for build in builds:
        choice = alternative(champions[build["championId"]], build["role"], build["bootsT2Item"]["itemName"])
        build["situationalBootsT2Item"] = choice
        build["situationalBootsT3Item"] = (
            {"itemName": PAIRS[choice["itemName"]], "description": choice["description"]} if choice else None
        )
    path.write_text(json.dumps(builds, ensure_ascii=False, indent=2) + "\n")
    return builds


if __name__ == "__main__":
    print(f"Updated {len(refine())} bundled builds")
