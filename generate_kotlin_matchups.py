import json

# Load all champions
with open('app/src/main/res/raw/champions_part1.json', 'r', encoding='utf-8') as f:
    c1 = json.load(f)
with open('app/src/main/res/raw/champions_part2.json', 'r', encoding='utf-8') as f:
    c2 = json.load(f)

all_champs = c1 + c2
by_id = {c['id']: c for c in all_champs}
by_name = {c['name'].lower(): c for c in all_champs}

# Role pools
role_pools = {'TOP': [], 'JUNGLE': [], 'MID': [], 'ADC': [], 'SUPPORT': []}
for c in all_champs:
    roles = [c['primaryRole']] + c.get('secondaryRoles', [])
    for r in roles:
        if r in role_pools and c['name'] not in role_pools[r]:
            role_pools[r].append(c['name'])

# Complete curated database for all champions in Wild Rift
# For each champion id, a dict of role -> (advantages, counters, synergies)
# We will verify every single name is in the proper role pool!
