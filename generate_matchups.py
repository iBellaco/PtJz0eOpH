import json

with open('app/src/main/res/raw/champions_part1.json') as f:
    c1 = json.load(f)
with open('app/src/main/res/raw/champions_part2.json') as f:
    c2 = json.load(f)

all_champs = c1 + c2
by_id = {c['id']: c for c in all_champs}
by_name = {c['name'].lower(): c for c in all_champs}

print(f"Loaded {len(all_champs)} champions")
