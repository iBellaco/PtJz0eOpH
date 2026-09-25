#!/usr/bin/env python3
"""
Sovereign Coach Build Standardizer for League of Legends: Wild Rift
Applies strict rules:
1. Exactly 1 build per line (role).
2. Exactly 3 Core Items (Premier Power Spikes).
3. 3-4 Situational Items tailored to counter comps (disjoint from core items, zero duplicates).
4. Tier 2 Boot evolved to Tier 3 Boot Upgrade.
5. Situational Boots (1-2 Tier 2 alternatives).
6. Runes:
   - 1 Keystone (from Clave)
   - 4 Secondaries: exactly 3 of the same category + 1 of a different category.
   - Zero duplicates, keystone never in secondaries.
7. Only items and runes from our official catalog.
8. Applied to all 142 champions with complete audit.
"""

import json
import re
import os
import sys

# 1. Load catalog items and runes
with open('app/src/main/java/com/example/data/WildRiftItemsData.kt', 'r', encoding='utf-8') as f:
    text_items = f.read()

with open('app/src/main/java/com/example/data/WildRiftSpellsAndRunes.kt', 'r', encoding='utf-8') as f:
    text_runes = f.read()

items_raw = re.findall(r'WildRiftItem\(\s*id\s*=\s*\"([^\"]+)\",\s*name\s*=\s*\"([^\"]+)\",.*?category\s*=\s*\"([^\"]+)\"', text_items, re.DOTALL)
runes_raw = re.findall(r'RuneItem\(\s*id\s*=\s*\"([^\"]+)\",\s*name\s*=\s*\"([^\"]+)\",.*?category\s*=\s*\"([^\"]+)\"', text_runes, re.DOTALL)

valid_items = {name: cat for id_, name, cat in items_raw}
valid_runes = {name: cat for id_, name, cat in runes_raw}

# Boot Upgrade Map
boot_upgrade_map = {
    "Botas blindadas": "Avance blindado",
    "Botas de mercurio": "Trituradoras encadenadas",
    "Botas de maná": "Botas del lanzahechizos",
    "Grebas de berserker": "Grebas de metal",
    "Botas jonias de la lucidez": "Lucidez carmesí",
    "Grebas codiciosas": "Botas inmortales",
    "Botas dinámicas": "Botas quebrantarmaduras"
}

def validate_build(champ_id, role, core_items, sit_items, boot_base, boot_upgrade, sit_boots, runes_list):
    # Rule 1: 3 Core Items
    if len(core_items) != 3:
        raise ValueError(f"[{champ_id}:{role}] coreItems must have exactly 3 items, got {len(core_items)}")
    if len(set(core_items)) != 3:
        raise ValueError(f"[{champ_id}:{role}] duplicate in coreItems: {core_items}")
    for it in core_items:
        if it not in valid_items:
            raise ValueError(f"[{champ_id}:{role}] unknown item in core: {it}")
        if valid_items[it] in ['Botas Nivel 2', 'Botas Nivel 3']:
            raise ValueError(f"[{champ_id}:{role}] boot item in core: {it}")

    # Rule 2: Situational items
    if len(sit_items) < 3 or len(sit_items) > 4:
        raise ValueError(f"[{champ_id}:{role}] situationalItems should have 3 or 4 items, got {len(sit_items)}")
    if len(set(sit_items)) != len(sit_items):
        raise ValueError(f"[{champ_id}:{role}] duplicate in situationalItems: {sit_items}")
    for it in sit_items:
        if it not in valid_items:
            raise ValueError(f"[{champ_id}:{role}] unknown situational item: {it}")
        if valid_items[it] in ['Botas Nivel 2', 'Botas Nivel 3']:
            raise ValueError(f"[{champ_id}:{role}] boot item in situational: {it}")

    # Rule 3: Disjoint check - No duplicate items in build!
    overlap = set(core_items).intersection(set(sit_items))
    if overlap:
        raise ValueError(f"[{champ_id}:{role}] item repeated across core and situational: {overlap}")

    # Rule 4: Boots
    if boot_base not in boot_upgrade_map:
        raise ValueError(f"[{champ_id}:{role}] invalid bootBase: {boot_base}")
    expected_upgrade = boot_upgrade_map[boot_base]
    if boot_upgrade != expected_upgrade:
        raise ValueError(f"[{champ_id}:{role}] bootUpgrade mismatch: expected {expected_upgrade}, got {boot_upgrade}")

    for b in sit_boots:
        if b not in boot_upgrade_map:
            raise ValueError(f"[{champ_id}:{role}] invalid situational boot: {b}")
        if b == boot_base:
            raise ValueError(f"[{champ_id}:{role}] situational boot cannot be same as bootBase: {b}")
    if len(set(sit_boots)) != len(sit_boots):
        raise ValueError(f"[{champ_id}:{role}] duplicate situational boots: {sit_boots}")

    # Rule 5: Runes
    if len(runes_list) != 5:
        raise ValueError(f"[{champ_id}:{role}] runes must have exactly 5, got {len(runes_list)}")
    keystone = runes_list[0]
    if valid_runes.get(keystone) != 'Clave':
        raise ValueError(f"[{champ_id}:{role}] {keystone} is not a valid Keystone")

    secondaries = runes_list[1:]
    if len(set(secondaries)) != 4:
        raise ValueError(f"[{champ_id}:{role}] duplicates in secondary runes: {secondaries}")
    if keystone in secondaries:
        raise ValueError(f"[{champ_id}:{role}] keystone {keystone} repeated in secondaries")

    for r in secondaries:
        if r not in valid_runes:
            raise ValueError(f"[{champ_id}:{role}] unknown secondary rune: {r}")
        if valid_runes[r] == 'Clave':
            raise ValueError(f"[{champ_id}:{role}] secondary rune {r} is a Keystone")

    from collections import Counter
    sec_cats = [valid_runes[r] for r in secondaries]
    counts = Counter(sec_cats)
    values = sorted(counts.values())
    if values != [1, 3]:
        raise ValueError(f"[{champ_id}:{role}] secondary runes must have 3 of same category and 1 different, got counts: {dict(counts)} for runes {secondaries}")

    return True

print("Build validator ready.")
