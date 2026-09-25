import re

with open('app/src/main/java/com/example/data/WildRiftItemsData.kt', 'r', encoding='utf-8') as f:
    text = f.read()

updates = {
    'serpent_s_fang': {
        'goldCost': 2800,
        'stats': '+50 Daño de ataque • +15 Penetración de armadura • +10 Velocidad de habilidades',
        'statsEn': '+50 Attack Damage • +15 Armor Penetration • +10 Ability Haste',
        'passive': 'Siegaescudos: Infligir daño a un campeón enemigo reduce los escudos que recibe durante 3 s. Los campeones cuerpo a cuerpo aplican un (10% del daño de ataque adicional + 40)% de reducción de escudos, con un máximo del 60%. Por su parte, los campeones a distancia aplican un (10% del daño de ataque adicional + 25)% de reducción de escudos, con un máximo del 45%. Cuando infliges daño a un enemigo que no está afectado por Siegaescudos, reduces todos sus escudos en la misma cantidad.',
        'passiveEn': 'Shieldbreaker: Dealing damage to an enemy champion reduces any shields they receive for 3s. Melee champions apply (10% bonus Attack Damage + 40)% shield reduction, up to 60%. Ranged champions apply (10% bonus Attack Damage + 25)% shield reduction, up to 45%. When you deal damage to an enemy who is not affected by Shieldbreaker, reduce all their shields by the same amount.',
        'coachTip': 'Objeto esencial contra composiciones enemigas con escudos pesados (Lulu, Karma, Lux, Shen, Sterak). Reduces drásticamente los escudos recibidos y existentes.',
        'coachTipEn': 'Essential anti-shield item against enemy comps with heavy shields (Lulu, Karma, Lux, Shen, Sterak). Drastically reduces new and existing shields.'
    },
    'youmuu_s_ghostblade': {
        'goldCost': 3000,
        'stats': '+55 Daño de ataque • +15 Penetración de armadura • +15 Velocidad de habilidades • +4% Velocidad de movimiento',
        'statsEn': '+55 Attack Damage • +15 Armor Penetration • +15 Ability Haste • +4% Movement Speed',
        'passive': 'Impulso: 3 s tras abandonar el combate contra campeones, obtiene 30 de velocidad de movimiento (20 para campeones a distancia).',
        'passiveEn': 'Momentum: 3s after exiting combat with champions, gain 30 Movement Speed (20 for ranged champions).',
        'coachTip': 'Excelente primer objeto para asesinos AD. Proporciona gran movilidad fuera de combate para rotar rápidamente por el mapa y emboscar líneas secundarias.',
        'coachTipEn': 'Excellent first item for AD assassins. Provides great out-of-combat mobility to roam quickly across the map and gank side lanes.'
    },
    'duskblade_of_draktharr': {
        'goldCost': 3000,
        'stats': '+55 Daño de ataque • +18 Penetración de armadura • +10 Velocidad de habilidades',
        'statsEn': '+55 Attack Damage • +18 Armor Penetration • +10 Ability Haste',
        'passive': 'Acechador nocturno: El primer ataque a un campeón inflige de 60 a 160 de daño físico adicional y lo ralentiza un 99% durante 0,35 s (10 s de enfriamiento). Las asistencias o asesinatos de campeones reinician el enfriamiento.',
        'passiveEn': 'Nightstalker: The first attack against a champion deals 60 to 160 bonus physical damage and slows them by 99% for 0.35s (10s Cooldown). Champion takedowns reset the cooldown.',
        'coachTip': 'Objeto clave de ráfaga para asesinos AD. Asesta un primer golpe devastador con ralentización masiva para asegurar combos y se reinicia al conseguir eliminaciones en peleas de equipo.',
        'coachTipEn': 'Key burst item for AD assassins. Delivers a devastating first strike with a massive slow to lock in combos, resetting on champion takedowns in teamfights.'
    },
    'edge_of_night': {
        'goldCost': 3000,
        'stats': '+250 Vida máxima • +50 Daño de ataque • +12 Penetración de armadura',
        'statsEn': '+250 Max Health • +50 Attack Damage • +12 Armor Penetration',
        'passive': 'Anular: Otorga un escudo de hechizos que bloquea la siguiente habilidad hostil (35 s de enfriamiento).',
        'passiveEn': 'Annul: Grants a spell shield that blocks the next hostile ability (35s Cooldown).',
        'coachTip': 'Objeto ofensivo-defensivo indispensable contra campeones con control de masas directo o iniciaciones de largo alcance.',
        'coachTipEn': 'Indispensable offensive-defensive item against champions with targeted CC or long-range initiates.'
    },
    'the_collector': {
        'goldCost': 3000,
        'stats': '+50 Daño de ataque • +10 Penetración de armadura • +25% Probabilidad de crítico',
        'statsEn': '+50 Attack Damage • +10 Armor Penetration • +25% Critical Rate',
        'passive': 'Muerte e impuestos: Si infliges daño a un campeón enemigo y lo dejas con menos del 5% de su vida máxima, lo ejecutas, el umbral de ejecución según la vida máxima aumenta un 0,1% permanentemente y obtienes 25 de oro adicional.',
        'passiveEn': 'Death and Taxes: If you deal damage that leaves an enemy champion below 5% max Health, execute them. The execution threshold increases by 0.1% permanently per kill, and grants 25 bonus gold.',
        'coachTip': 'Objeto híbrido de penetración de armadura y crítico que asegura eliminaciones instantáneas a objetivos con poca vida, acelerando la bola de nieve de oro.',
        'coachTipEn': 'Hybrid armor pen and crit item that guarantees instant executes on low-health targets, accelerating your gold snowball.'
    },
    'mercurial_scimitar': {
        'goldCost': 3100,
        'stats': '+45 Daño de ataque • +40 Resistencia mágica • +12% Robo de vida',
        'statsEn': '+45 Attack Damage • +40 Magic Resist • +12% Physical Vamp',
        'passive': 'Fajín de mercurio (activa): Elimina todas las debilitaciones de control de adversario que te hayan aplicado y te otorga inmunidad contra el control de adversario durante 0,25 s.\\nPerseverancia (pasiva): Cuando el efecto de Fajín termina, otorga un 30% de tenacidad y un 30% de resistencia a las ralentizaciones durante 1,5 s (60 s de enfriamiento). No se puede utilizar mientras te lanzan por los aires o te empujan.',
        'passiveEn': 'Quicksilver (Active): Removes all crowd control debuffs and grants crowd control immunity for 0.25s.\\nPerseverance (Passive): When Quicksilver ends, grants 30% Tenacity and 30% Slow Resist for 1.5s (60s Cooldown). Cannot be cast while Knocked Up or Knocked Back.',
        'coachTip': 'Objeto situacional crucial contra composiciones enemigas con cadenas duras de CC y daño mágico alto. Su activa elimina efectos inhabilitantes e inmuniza brevemente.',
        'coachTipEn': 'Crucial situational item against heavy enemy CC chains and high magic damage. Its active clears disabling effects and grants brief immunity.'
    }
}

def update_item_block(match):
    block = match.group(0)
    id_match = re.search(r'id = \"([^\"]+)\"', block)
    if not id_match or id_match.group(1) not in updates:
        return block
    
    item_id = id_match.group(1)
    data = updates[item_id]
    
    block = re.sub(r'goldCost = \d+', f'goldCost = {data["goldCost"]}', block)
    block = re.sub(r'stats = \"[^\"]*\"', f'stats = \"{data["stats"]}\"', block)
    block = re.sub(r'statsEn = \"[^\"]*\"', f'statsEn = \"{data["statsEn"]}\"', block)
    block = re.sub(r'passive = \"[^\"]*\"', f'passive = \"{data["passive"]}\"', block)
    block = re.sub(r'passiveEn = \"[^\"]*\"', f'passiveEn = \"{data["passiveEn"]}\"', block)
    block = re.sub(r'coachTip = \"[^\"]*\"', f'coachTip = \"{data["coachTip"]}\"', block)
    block = re.sub(r'coachTipEn = \"[^\"]*\"', f'coachTipEn = \"{data["coachTipEn"]}\"', block)
    
    return block

new_text = re.sub(r'WildRiftItem\(\s*id = \"[^\"]+\",.*?\n\s*\)', update_item_block, text, flags=re.DOTALL)

with open('app/src/main/java/com/example/data/WildRiftItemsData.kt', 'w', encoding='utf-8') as f:
    f.write(new_text)

print('SUCCESSFULLY_UPDATED_ITEMS')
