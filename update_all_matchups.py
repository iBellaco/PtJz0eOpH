import json
import os

with open('app/src/main/res/raw/champions_part1.json', 'r', encoding='utf-8') as f:
    c1 = json.load(f)
with open('app/src/main/res/raw/champions_part2.json', 'r', encoding='utf-8') as f:
    c2 = json.load(f)

all_champs = c1 + c2
print(f"Total champions: {len(all_champs)}")
