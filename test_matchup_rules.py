import json

with open('app/src/main/res/raw/champions_part1.json') as f:
    c1 = json.load(f)
with open('app/src/main/res/raw/champions_part2.json') as f:
    c2 = json.load(f)

all_champs = c1 + c2
champs_by_id = {c['id']: c for c in all_champs}
name_to_champ = {c['name'].lower(): c for c in all_champs}
id_to_name = {c['id']: c['name'] for c in all_champs}

role_pools = {'TOP': set(), 'JUNGLE': set(), 'MID': set(), 'ADC': set(), 'SUPPORT': set()}
for c in all_champs:
    roles = [c['primaryRole']] + c.get('secondaryRoles', [])
    for r in roles:
        if r in role_pools:
            role_pools[r].add(c['name'])

print("Role pool sizes:")
for r, p in role_pools.items():
    print(r, len(p))
