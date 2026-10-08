import json, re, os

# 1. Runes
runes_translations = [
    {
        "id": "dark_harvest",
        "name_es": "Cosecha Oscura",
        "name_en": "Dark Harvest",
        "name_pt": "Colheita Sombria",
        "desc_es": "Daño adicional, amplificación de acumulaciones\n\nAl infligir daño a un campeón con menos del 50% de vida, se inflige daño adaptable y se cosecha su alma, lo que aumenta permanentemente el daño de Cosecha Oscura.\nEnfriamiento: 45 s (se reinicia a 1,5 s con eliminaciones de campeones).",
        "desc_en": "Bonus damage, stack amplification\n\nDamaging a champion below 50% health deals adaptive damage and harvests their soul, permanently increasing Dark Harvest's damage.\nCooldown: 45s (resets to 1.5s on champion takedowns).",
        "desc_pt": "Dano adicional, amplificação de acúmulos\n\nCausar dano a um campeão com menos de 50% de vida causa dano adaptativo e colhe sua alma, aumentando permanentemente o dano de Colheita Sombria.\nTempo de Recarga: 45s (reinicia para 1,5s com eliminações de campeões)."
    },
    {
        "id": "electrocute",
        "name_es": "Electrocutar",
        "name_en": "Electrocute",
        "name_pt": "Eletrocutar",
        "desc_es": "Daño explosivo\n\nEn 3 s, golpea a un mismo campeón enemigo con 3 ataques o habilidades individuales para infligir daño adaptable adicional.\nDaño: 40-194 + 40% AD adicional + 25% AP.\nEnfriamiento: 20 s.",
        "desc_en": "Burst damage\n\nHit an enemy champion with 3 unique attacks or abilities within 3s to deal bonus adaptive damage.\nDamage: 40-194 + 40% bonus AD + 25% AP.\nCooldown: 20s.",
        "desc_pt": "Dano explosivo\n\nAcerte um mesmo campeão inimigo com 3 ataques ou habilidades individuais em até 3s para causar dano adaptativo adicional.\nDano: 40-194 + 40% AD adicional + 25% AP.\nTempo de Recarga: 20s."
    },
    {
        "id": "lethal_tempo",
        "name_es": "Compás Letal",
        "name_en": "Lethal Tempo",
        "name_pt": "Ritmo Fatal",
        "desc_es": "Velocidad de ataque\n\nObtienes velocidad de ataque acumulable al atacar a campeones enemigos. Al llegar al máximo de 6 acumulaciones, obtienes alcance de ataque adicional y velocidad de ataque adicional.",
        "desc_en": "Attack speed\n\nGain stacking attack speed when attacking enemy champions. At maximum 6 stacks, gain bonus attack range and additional attack speed.",
        "desc_pt": "Velocidade de ataque\n\nReceba velocidade de ataque acumulável ao atacar campeões inimigos. Ao atingir o máximo de 6 acúmulos, ganhe alcance de ataque adicional e velocidade de ataque extra."
    },
    {
        "id": "fleet_footwork",
        "name_es": "Pies Veloces",
        "name_en": "Fleet Footwork",
        "name_pt": "Agilidade nos Pés",
        "desc_es": "Movilidad, curación\n\nMoverse, atacar y utilizar habilidades genera energía. A las 100 acumulaciones, tu siguiente ataque restaura vida y otorga un 20% de velocidad de movimiento adicional durante 1 s.",
        "desc_en": "Mobility, healing\n\nMoving, attacking, and casting abilities builds energy. At 100 stacks, your next attack heals you and grants 20% bonus movement speed for 1s.",
        "desc_pt": "Mobilidade, cura\n\nMover-se, atacar e conjurar habilidades acumula energia. Com 100 acúmulos, seu próximo ataque restaura vida e concede 20% de velocidade de movimento adicional por 1s."
    },
    {
        "id": "conqueror",
        "name_es": "Conquistador",
        "name_en": "Conqueror",
        "name_pt": "Conquistador",
        "desc_es": "Acumula daño y succión\n\nGolpear a un campeón con ataques o habilidades otorga acumulaciones de fuerza adaptable (hasta 6). Con el máximo de acumulaciones, obtienes omnivampirismo físico y mágico adicional.",
        "desc_en": "Stacking damage & vamp\n\nDamaging a champion with attacks or abilities grants stacks of adaptive force (up to 6). At max stacks, gain bonus physical and magic omnivamp.",
        "desc_pt": "Acúmulo de dano e vampirismo\n\nAtingir um campeão com ataques ou habilidades concede acúmulos de força adaptativa (até 6). Com o máximo de acúmulos, ganhe vampirismo físico e mágico adicional."
    },
    {
        "id": "grasp_undying",
        "name_es": "Garras del Inmortal",
        "name_en": "Grasp of the Undying",
        "name_pt": "Aperto dos Mortos-Vivos",
        "desc_es": "Tanque, curación\n\nCada 3 s que pases en combate, se potencia tu siguiente ataque contra campeones para infligir daño mágico adicional según tu vida máxima, curarte y aumentar tu vida permanentemente.",
        "desc_en": "Tank, sustain\n\nEvery 3s in combat, empower your next basic attack against champions to deal bonus magic damage based on your max health, heal you, and permanently increase your max health.",
        "desc_pt": "Tanque, sustentação\n\nA cada 3s em combate, fortaleça seu próximo ataque contra campeões para causar dano mágico adicional baseado na sua vida máxima, curar você e aumentar permanentemente sua vida."
    },
    {
        "id": "guardian",
        "name_es": "Guardián",
        "name_en": "Guardian",
        "name_pt": "Guardião",
        "desc_es": "Protección, escudo\n\nProtege a los aliados que se encuentren a 3,5 de distancia y al aliado que selecciones con una habilidad. Si tú o el aliado recibís daño, ambos obtenéis un escudo y velocidad de movimiento adicional.",
        "desc_en": "Protection, shield\n\nGuard allies within 3.5 range and the ally you target with abilities. If you or the guarded ally take damage, both gain a shield and bonus movement speed.",
        "desc_pt": "Proteção, escudo\n\nProteja aliados a até 3,5 de distância e o aliado alvo de suas habilidades. Se você ou o aliado protegido sofrerem dano, ambos recebem um escudo e velocidade de movimento adicional."
    },
    {
        "id": "first_strike",
        "name_es": "Primer Golpe",
        "name_en": "First Strike",
        "name_pt": "Primeiro Ataque",
        "desc_es": "Iniciación, amplificación de daño, oro adicional\n\nIniciar combate contra un campeón enemigo otorga 5 de oro y Primer Golpe durante 3 s, lo que te hace infligir un 9% de daño verdadero adicional y otorga oro según el daño adicional infligido.\nEnfriamiento: 19-14 s.",
        "desc_en": "Initiation, damage amp, bonus gold\n\nInitiating combat against an enemy champion grants 5 gold and First Strike for 3s, dealing 9% bonus true damage and granting bonus gold based on damage dealt.\nCooldown: 19-14s.",
        "desc_pt": "Iniciação, amplificação de dano, ouro bônus\n\nIniciar combate contra um campeão inimigo concede 5 de ouro e Primeiro Ataque por 3s, causando 9% de dano verdadeiro adicional e concedendo ouro baseado no dano causado.\nTempo de Recarga: 19-14s."
    },
    {
        "id": "aery",
        "name_es": "Aery",
        "name_en": "Aery",
        "name_pt": "Invocar Aery",
        "desc_es": "Desgaste, protección\n\nTus ataques y habilidades envían a Aery hasta el objetivo para infligir daño adicional a enemigos o proteger a aliados con un escudo.",
        "desc_en": "Poke, shielding\n\nYour attacks and abilities send Aery to your target, damaging enemies or shielding allies.",
        "desc_pt": "Desgaste, escudo\n\nSeus ataques e habilidades enviam Aery até o alvo, causando dano a inimigos ou concedendo um escudo a aliados."
    },
    {
        "id": "arcane_comet",
        "name_es": "Cometa Arcano",
        "name_en": "Arcane Comet",
        "name_pt": "Cometa Arcano",
        "desc_es": "Hostigar desde lejos, amplificación de acumulaciones\n\nInfligir daño a un campeón con una habilidad lanza un cometa a su posición que inflige daño adaptable tras un breve retardo.\nEnfriamiento: 20-8 s (se reduce al golpear con habilidades).",
        "desc_en": "Long-range poke, stack scaling\n\nDamaging a champion with an ability hurls a comet at their location, dealing adaptive damage after a brief delay.\nCooldown: 20-8s (reduced by landing abilities).",
        "desc_pt": "Poke de longo alcance, escalamento\n\nCausar dano a um campeão com uma habilidade lança um cometa em sua posição, causando dano adaptativo após um breve intervalo.\nTempo de Recarga: 20-8s (reduzido ao acertar habilidades)."
    },
    {
        "id": "phase_rush",
        "name_es": "Irrupción de Fase",
        "name_en": "Phase Rush",
        "name_pt": "Ímpeto Gradual",
        "desc_es": "Movilidad, velocidad de habilidades\n\nGolpear a un campeón con 3 ataques o habilidades individuales en un plazo de 3 s otorga una gran bonificación de velocidad de movimiento y aceleración de habilidad durante 3 s.",
        "desc_en": "Mobility, ability haste\n\nHit a champion with 3 unique attacks or abilities within 3s to gain a surge of movement speed and ability haste for 3s.",
        "desc_pt": "Mobilidade, aceleração de habilidade\n\nAtingir um campeão com 3 ataques ou habilidades únicas em até 3s concede um grande aumento de velocidade de movimento e aceleração de habilidade por 3s."
    },
    {
        "id": "glacial_augment",
        "name_es": "Soberano Gélido",
        "name_en": "Glacial Augment",
        "name_pt": "Aprimoramento Glacial",
        "desc_es": "Control, ralentización\n\nAl inmovilizar a un campeón enemigo, se proyectan 3 rayos gélidos que ralentizan a los enemigos y reducen el daño que infligen a tus aliados.",
        "desc_en": "Crowd control, slow\n\nImmobilizing an enemy champion emits 3 glacial rays that create frozen zones, slowing enemies and reducing damage dealt to your allies.",
        "desc_pt": "Controle de grupo, lentidão\n\nImobilizar um campeão inimigo emite 3 raios glaciais que criam zonas congeladas, reduzindo a velocidade dos inimigos e o dano causado a seus aliados."
    },
    {
        "id": "cheap_shot",
        "name_es": "Golpe Bajo",
        "name_en": "Cheap Shot",
        "name_pt": "Golpe Desleal",
        "desc_es": "Inflige de 10 a 45 de daño verdadero adicional a los enemigos con movimiento o acciones ralentizadas o mermadas (4 s de enfriamiento).",
        "desc_en": "Deal 10-45 bonus true damage to enemies with impaired movement or actions (4s cooldown).",
        "desc_pt": "Causa de 10 a 45 de dano verdadeiro adicional a inimigos com movimento ou ações debilitadas (4s de tempo de recarga)."
    },
    {
        "id": "sudden_impact",
        "name_es": "Impacto Repentino",
        "name_en": "Sudden Impact",
        "name_pt": "Impacto Repentino",
        "desc_es": "Tras un deslizamiento, salto, teleportación instantánea, teletransporte o tras salir del sigilo, infligir daño a un campeón enemigo otorga 11-16 de penetración de armadura o mágica durante 4 s (4 s de enfriamiento).",
        "desc_en": "After using a dash, leap, blink, teleport, or leaving stealth, damaging an enemy champion grants 11-16 armor penetration or magic penetration for 4s (4s cooldown).",
        "desc_pt": "Após usar um avanço, salto, teleporte instantâneo ou sair de furtividade, causar dano a um campeão inimigo concede 11-16 de penetração de armadura ou mágica por 4s (4s de recarga)."
    },
    {
        "id": "empowered_attack",
        "name_es": "Ataque Potenciado",
        "name_en": "Empowered Attack",
        "name_pt": "Ataque Fortalecido",
        "desc_es": "Cada 8 s, potencia tu siguiente ataque, que infligirá 20-60 de daño adaptable adicional.",
        "desc_en": "Every 8s, empower your next basic attack to deal 20-60 bonus adaptive damage.",
        "desc_pt": "A cada 8s, fortalece seu próximo ataque básico para causar 20-60 de dano adaptativo adicional."
    },
    {
        "id": "chain_assault",
        "name_es": "Asalto Encadenado",
        "name_en": "Chain Assault",
        "name_pt": "Assalto em Cadeia",
        "desc_es": "Infligir daño a un campeón enemigo con una habilidad activa otorga acumulaciones que aumentan tu velocidad de movimiento y aceleración de habilidad.",
        "desc_en": "Damaging an enemy champion with an active ability grants stacks increasing movement speed and ability haste.",
        "desc_pt": "Causar dano a um campeão inimigo com uma habilidade ativa concede acúmulos que aumentam sua velocidade de movimento e aceleração de habilidade."
    },
    {
        "id": "tyrant",
        "name_es": "Tirano",
        "name_en": "Tyrant",
        "name_pt": "Tirano",
        "desc_es": "Al infligir daño a un campeón con menos de un 50% de vida, infliges un 5% de daño adaptable adicional.",
        "desc_en": "Damaging an enemy champion below 50% health deals 5% bonus adaptive damage.",
        "desc_pt": "Causar dano a um campeão inimigo com menos de 50% de vida causa 5% de dano adaptativo adicional."
    },
    {
        "id": "hubris",
        "name_es": "Soberbia",
        "name_en": "Hubris",
        "name_pt": "Soberba",
        "desc_es": "Las eliminaciones de campeones enemigos en 3 s tras infligirles daño otorgan una estatua y poder adaptable temporal.",
        "desc_en": "Takedowns on enemy champions within 3s of damaging them grant a statue and temporary adaptive force.",
        "desc_pt": "Eliminações de campeões inimigos em até 3s após causar-lhes dano concedem uma estátua e força adaptativa temporária."
    },
    {
        "id": "eyeball_collection",
        "name_es": "Colección de Globos Oculares",
        "name_en": "Eyeball Collection",
        "name_pt": "Globos Oculares",
        "desc_es": "Otorga 1,5 de daño de ataque o 3 de poder de habilidad al participar en eliminaciones de campeones enemigos (hasta 10 acumulaciones). Con el máximo de acumulaciones, otorga daño o poder adicional.",
        "desc_en": "Grants 1.5 Attack Damage or 3 Ability Power per champion takedown (up to 10 stacks). At max stacks, gain additional bonus AD or AP.",
        "desc_pt": "Concede 1,5 de Dano de Ataque ou 3 de Poder de Habilidade por eliminação de campeão inimigo (até 10 acúmulos). Com acúmulos máximos, concede AD ou AP bônus adicional."
    },
    {
        "id": "ingenious_hunter",
        "name_es": "Cazador Ingenioso",
        "name_en": "Ingenious Hunter",
        "name_pt": "Caça Ardilosa",
        "desc_es": "Otorga 20 de velocidad de objetos. Por cada eliminación de un campeón único, obtienes 5 de velocidad de objetos adicional.",
        "desc_en": "Grants 20 item haste. Gain an additional 5 item haste for each unique enemy champion takedown.",
        "desc_pt": "Concede 20 de aceleração de itens. Ganhe 5 de aceleração de itens adicional para cada eliminação de campeão único."
    },
    {
        "id": "relentless_hunter",
        "name_es": "Cazador Incesante",
        "name_en": "Relentless Hunter",
        "name_pt": "Caça Incansável",
        "desc_es": "Otorga 10 de velocidad de movimiento fuera de combate. Por cada eliminación de un campeón único, obtienes 6 de velocidad de movimiento fuera de combate adicional.",
        "desc_en": "Grants 10 out-of-combat movement speed. Gain an additional 6 out-of-combat movement speed for each unique enemy champion takedown.",
        "desc_pt": "Concede 10 de velocidade de movimento fora de combate. Ganhe 6 de velocidade de movimento fora de combate adicional para cada eliminação única de campeão."
    },
    {
        "id": "zombie_ward",
        "name_es": "Guardián Zombi",
        "name_en": "Zombie Ward",
        "name_pt": "Sentinela Zumbi",
        "desc_es": "Las eliminaciones de guardianes enemigos crean un guardián zombi aliado en su lugar. Otorga daño de ataque o poder de habilidad adaptable por cada guardián zombi generado.",
        "desc_en": "Killing enemy wards summons an allied Zombie Ward in their place. Grants adaptive Attack Damage or Ability Power for each Zombie Ward spawned.",
        "desc_pt": "Destruir sentinelas inimigas gera uma Sentinela Zumbi aliada no local. Concede Dano de Ataque ou Poder de Habilidade adaptativo para cada Sentinela Zumbi gerada."
    },
    {
        "id": "brutal",
        "name_es": "Brutal",
        "name_en": "Brutal",
        "name_pt": "Brutal",
        "desc_es": "Los ataques infligen (5 + 6% AD adicional + 3% AP) de daño adaptable adicional al impacto contra campeones enemigos.",
        "desc_en": "Basic attacks deal (5 + 6% bonus AD + 3% AP) bonus on-hit adaptive damage to enemy champions.",
        "desc_pt": "Ataques básicos causam (5 + 6% AD bônus + 3% AP) de dano adaptativo adicional ao contato contra campeões inimigos."
    },
    {
        "id": "triumph",
        "name_es": "Triunfo",
        "name_en": "Triumph",
        "name_pt": "Triunfo",
        "desc_es": "Las eliminaciones de campeones restauran un 10% de la vida perdida y un 10% del maná o energía máximos.",
        "desc_en": "Champion takedowns restore 10% missing health and 10% max mana or energy.",
        "desc_pt": "Eliminações de campeões restauram 10% da vida perdida e 10% da mana ou energia máxima."
    },
    {
        "id": "battle_fervor",
        "name_es": "Fervor de Batalla",
        "name_en": "Battle Fervor",
        "name_pt": "Fervor de Batalha",
        "desc_es": "Obtienes un 1,4% de amplificación de daño de las habilidades por cada ataque asestado a campeones (se acumula hasta 5 veces).",
        "desc_en": "Gain 1.4% ability damage amplification for each basic attack landed on champions (stacks up to 5 times).",
        "desc_pt": "Receba 1,4% de amplificação de dano de habilidades para cada ataque básico acertado em campeões (acumula até 5 vezes)."
    },
    {
        "id": "last_stand",
        "name_es": "Último Esfuerzo",
        "name_en": "Last Stand",
        "name_pt": "Até a Morte",
        "desc_es": "Cuando tu vida está por debajo del 60%, los ataques asestados a campeones enemigos infligen un 5%-11% de daño adaptable adicional (máximo con menos del 30% de vida).",
        "desc_en": "While below 60% health, attacks on enemy champions deal 5%-11% bonus adaptive damage (maximum at below 30% health).",
        "desc_pt": "Enquanto estiver com menos de 60% de vida, ataques a campeões inimigos causam 5%-11% de dano adaptativo adicional (máximo abaixo de 30% de vida)."
    },
    {
        "id": "cut_down",
        "name_es": "Derribado",
        "name_en": "Cut Down",
        "name_pt": "Dilacerar",
        "desc_es": "Tus ataques infligen un 6,5% de daño adicional a campeones enemigos que tengan más vida máxima adicional que tú.",
        "desc_en": "Deal up to 6.5% bonus damage to enemy champions who have more bonus max health than you.",
        "desc_pt": "Seus ataques causam até 6,5% de dano adicional a campeões inimigos que tenham mais vida máxima bônus do que você."
    },
    {
        "id": "coup_de_grace",
        "name_es": "Golpe de Gracia",
        "name_en": "Coup de Grace",
        "name_pt": "Golpe de Misericórdia",
        "desc_es": "Inflige un 8% de daño adicional a campeones enemigos con menos del 40% de vida.",
        "desc_en": "Deal 8% bonus damage to enemy champions below 40% health.",
        "desc_pt": "Causa 8% de dano adicional a campeões inimigos com menos de 40% de vida."
    },
    {
        "id": "legend_alacrity",
        "name_es": "Leyenda: Presteza",
        "name_en": "Legend: Alacrity",
        "name_pt": "Lenda: Espontaneidade",
        "desc_es": "Otorga un 3% de velocidad de ataque. Asesina a monstruos, súbditos y campeones para obtener hasta un 18% de velocidad de ataque adicional.",
        "desc_en": "Grants 3% attack speed. Takedowns on monsters, minions, and champions grant up to 18% additional attack speed.",
        "desc_pt": "Concede 3% de velocidade de ataque. Eliminações de monstros, tropas e campeões concedem até 18% de velocidade de ataque adicional."
    },
    {
        "id": "legend_tenacity",
        "name_es": "Leyenda: Tenacidad",
        "name_en": "Legend: Tenacity",
        "name_pt": "Lenda: Tenacidade",
        "desc_es": "Otorga un 3% de tenacidad y un 3% de resistencia a las ralentizaciones. Eliminar monstruos, súbditos y campeones aumenta la tenacidad y resistencia hasta un 15%.",
        "desc_en": "Grants 3% tenacity and 3% slow resistance. Takedowns on monsters, minions, and champions increase tenacity and slow resistance up to 15%.",
        "desc_pt": "Concede 3% de tenacidade e 3% de resistência a lentidão. Eliminações de monstros, tropas e campeões aumentam a tenacidade e resistência até 15%."
    },
    {
        "id": "legend_bloodline",
        "name_es": "Leyenda: Linaje",
        "name_en": "Legend: Bloodline",
        "name_pt": "Lenda: Linhagem",
        "desc_es": "Otorga un 1% de omnisucción. Asesina a monstruos, súbditos y campeones para obtener hasta un 7% de omnisucción adicional.",
        "desc_en": "Grants 1% omnivamp. Takedowns on monsters, minions, and champions grant up to 7% additional omnivamp.",
        "desc_pt": "Concede 1% de vampirismo universal. Eliminações de monstros, tropas e campeões concedem até 7% de vampirismo universal adicional."
    },
    {
        "id": "font_of_life",
        "name_es": "Fuente de Vida",
        "name_en": "Font of Life",
        "name_pt": "Fonte da Vida",
        "desc_es": "Cuando tus ataques o habilidades golpean a un campeón enemigo, lo marcas. Los aliados que ataquen a enemigos marcados se curan durante unos segundos.",
        "desc_en": "Hitting an enemy champion with attacks or abilities marks them. Allies who damage marked enemies restore health over a few seconds.",
        "desc_pt": "Atingir um campeão inimigo com ataques ou habilidades marca o alvo. Aliados que atacarem alvos marcados regeneram vida ao longo de alguns segundos."
    },
    {
        "id": "courage_of_the_colossus",
        "name_es": "Coraje del Coloso",
        "name_en": "Courage of the Colossus",
        "name_pt": "Coragem do Colosso",
        "desc_es": "Otorga un escudo que absorbe de 25 a 45 + 1% del daño por vida máxima tras inmovilizar a un campeón enemigo (10 s de enfriamiento).",
        "desc_en": "Gain a shield absorbing 25-45 + 1% max health upon immobilizing an enemy champion (10s cooldown).",
        "desc_pt": "Receba um escudo que absorve de 25 a 45 + 1% de vida máxima após imobilizar um campeão inimigo (10s de tempo de recarga)."
    },
    {
        "id": "nullifying_orb",
        "name_es": "Orbe Anulador",
        "name_en": "Nullifying Orb",
        "name_pt": "Orbe Anulador",
        "desc_es": "Si un campeón te inflige daño suficiente para hacerte bajar del 35% de vida máxima, obtienes un escudo que absorbe daño durante 4 s (60 s de enfriamiento).",
        "desc_en": "If damage from a champion would reduce you below 35% max health, gain a shield absorbing damage for 4s (60s cooldown).",
        "desc_pt": "Se sofrer dano de um campeão que reduza sua vida para menos de 35%, receba um escudo que absorve dano por 4s (60s de tempo de recarga)."
    },
    {
        "id": "bone_plating",
        "name_es": "Revestimiento de Huesos",
        "name_en": "Bone Plating",
        "name_pt": "Osso Revestido",
        "desc_es": "Al recibir daño de un campeón, los siguientes 3 ataques o habilidades de campeones que recibas infligen 30-60 menos de daño durante 1,5 s (35 s de enfriamiento).",
        "desc_en": "After taking damage from a champion, the next 3 attacks or abilities from champions deal 30-60 less damage for 1.5s (35s cooldown).",
        "desc_pt": "Ao sofrer dano de um campeão, os próximos 3 ataques ou habilidades de campeões causam 30-60 a menos de dano por 1,5s (35s de recarga)."
    },
    {
        "id": "second_wind",
        "name_es": "Fuerzas Renovadas",
        "name_en": "Second Wind",
        "name_pt": "Ventos Revigorantes",
        "desc_es": "Otorga 5 de vida cada 5 s.\nAl recibir daño de un campeón enemigo, regeneras un 3% de tu vida perdida durante los siguientes 5 s (se duplica para campeones cuerpo a cuerpo).",
        "desc_en": "Grants 5 health every 5s.\nAfter taking damage from an enemy champion, regenerate 3% missing health over 5s (doubled for melee champions).",
        "desc_pt": "Concede 5 de vida a cada 5s.\nApós sofrer dano de um campeão inimigo, regenera 3% da sua vida perdida ao longo de 5s (dobrado para campeões corpo a corpo)."
    },
    {
        "id": "unflinching",
        "name_es": "Inquebrantable",
        "name_en": "Unflinching",
        "name_pt": "Inabalável",
        "desc_es": "Obtienes un 3% de armadura y de resistencia mágica. Por cada campeón enemigo vivo cercano, obtienes un 3% adicional de armadura y resistencia mágica.",
        "desc_en": "Grants 3% armor and magic resist. For each nearby living enemy champion, gain an additional 3% armor and magic resist.",
        "desc_pt": "Concede 3% de armadura e resistência mágica. Para cada campeão inimigo vivo por perto, receba 3% adicional de armadura e resistência mágica."
    },
    {
        "id": "overgrowth",
        "name_es": "Sobrecrecimiento",
        "name_en": "Overgrowth",
        "name_pt": "Crescimento Excessivo",
        "desc_es": "Por cada 3 súbditos enemigos o monstruos asesinados cerca de ti, obtienes 2 de vida máxima permanentemente. Al alcanzar 30 acumulaciones, obtienes un 3,5% de vida máxima adicional.",
        "desc_en": "Gain 2 permanent max health for every 3 enemy minions or monsters dying near you. At 30 stacks, gain an additional 3.5% max health.",
        "desc_pt": "Ganhe 2 de vida máxima permanente a cada 3 tropas inimigas ou monstros abatidos perto de você. Ao atingir 30 acúmulos, ganhe 3,5% de vida máxima adicional."
    },
    {
        "id": "revitalize",
        "name_es": "Revitalizar",
        "name_en": "Revitalize",
        "name_pt": "Revitalizar",
        "desc_es": "Amplifica un 5% las curaciones y escudos. Si la vida del objetivo está por debajo del 40%, la amplificación aumenta un 10% adicional.",
        "desc_en": "Heals and shields you cast or receive are 5% stronger. Increased by an additional 10% on targets below 40% health.",
        "desc_pt": "Curas e escudos conjurados ou recebidos são 5% mais fortes. Aumenta em 10% adicional em alvos com menos de 40% de vida."
    },
    {
        "id": "perseverance",
        "name_es": "Perseverancia",
        "name_en": "Perseverance",
        "name_pt": "Perseverança",
        "desc_es": "Otorga 10% de tenacidad. Otorga de 10 a 15 de armadura y resistencia mágica adicionales durante 2 s tras quedar inmovilizado.",
        "desc_en": "Grants 10% tenacity. Gain 10-15 bonus armor and magic resist for 2s after being immobilized.",
        "desc_pt": "Concede 10% de tenacidade. Ganhe 10-15 de armadura e resistência mágica bônus por 2s após ser imobilizado."
    },
    {
        "id": "demolish",
        "name_es": "Demoler",
        "name_en": "Demolish",
        "name_pt": "Demolir",
        "desc_es": "Al estar a 550 de distancia de una torreta enemiga, cargas un ataque potente contra ella durante 3 s. El ataque cargado inflige 200 (+25% de vida máxima) de daño físico adicional a la torreta (35 s de enfriamiento).",
        "desc_en": "While within 550 range of an enemy turret, charge a powerful attack over 3s. The charged attack deals 200 (+25% max health) bonus physical damage to the turret (35s cooldown).",
        "desc_pt": "A até 550 de distância de uma torre inimiga, carrega um ataque poderoso ao longo de 3s. O ataque carregado causa 200 (+25% de vida máxima) de dano físico bônus à torre (35s de recarga)."
    },
    {
        "id": "manaflow_band",
        "name_es": "Banda de Maná",
        "name_en": "Manaflow Band",
        "name_pt": "Faixa de Fluxo de Mana",
        "desc_es": "Golpear a un campeón enemigo con una habilidad o ataque potenciado aumenta tu maná máximo en 30, hasta un máximo de 300 de maná.\nAl llegar al máximo de 300 de maná, restaura un 1,5% del maná restante cada 5 s.",
        "desc_en": "Hitting an enemy champion with an ability or empowered attack increases your maximum mana by 30, up to 300 mana.\nAt max 300 mana, restore 1.5% remaining mana every 5s.",
        "desc_pt": "Atingir um campeão inimigo com uma habilidade ou ataque fortalecido aumenta sua mana máxima em 30, até o máximo de 300 de mana.\nCom o máximo de 300 de mana, restaura 1,5% da mana restante a cada 5s."
    },
    {
        "id": "axiomatic_arcologist",
        "name_es": "Arcanólogo Axiomático",
        "name_en": "Axiomatic Arcanist",
        "name_pt": "Arcanista Axiomático",
        "desc_es": "Tu habilidad definitiva obtiene un 10% de daño, curación y escudo adicionales.\nLas eliminaciones de campeones reducen el enfriamiento restante de tu definitiva un 15%.",
        "desc_en": "Your ultimate ability gains 10% bonus damage, healing, and shield strength.\nChampion takedowns reduce the remaining cooldown of your ultimate by 15%.",
        "desc_pt": "Sua habilidade ultimate recebe 10% de dano, cura e escudo adicionais.\nEliminações de campeões reduzem o tempo de recarga restante da sua ultimate em 15%."
    },
    {
        "id": "transcendence",
        "name_es": "Trascendencia",
        "name_en": "Transcendence",
        "name_pt": "Transcendência",
        "desc_es": "Otorga una bonificación al alcanzar los siguientes niveles:\nNivel 1: +6 de aceleración de habilidad.\nNivel 6: +6 de aceleración de habilidad adicional.\nNivel 11: Tras una eliminación de un campeón, reduce el tiempo de enfriamiento restante de las habilidades básicas un 15%.",
        "desc_en": "Gain bonuses upon reaching key champion levels:\nLevel 1: +6 ability haste.\nLevel 6: +6 additional ability haste.\nLevel 11: On champion takedown, reduce remaining cooldowns of basic abilities by 15%.",
        "desc_pt": "Receba bônus ao atingir níveis chave do campeão:\nNível 1: +6 de aceleração de habilidade.\nNível 6: +6 de aceleração de habilidade adicional.\nNível 11: Em eliminações de campeões, reduz o tempo de recarga restante das habilidades básicas em 15%."
    },
    {
        "id": "celerity",
        "name_es": "Celeridad",
        "name_en": "Celerity",
        "name_pt": "Celeridade",
        "desc_es": "Obtiene un 2% de velocidad de movimiento. Aumentan un 7% todos los efectos de velocidad de movimiento adicionales que recibas.",
        "desc_en": "Grants 2% movement speed. Increases all bonus movement speed effects you receive by 7%.",
        "desc_pt": "Concede 2% de velocidade de movimento. Aumenta em 7% todos os efeitos de velocidade de movimento adicionais recebidos."
    },
    {
        "id": "absolute_focus",
        "name_es": "Concentración Absoluta",
        "name_en": "Absolute Focus",
        "name_pt": "Foco Absoluto",
        "desc_es": "Con más del 65% de la vida, obtienes 2-20 de daño de ataque o 4-40 de poder de habilidad (adaptable según el nivel).",
        "desc_en": "While above 65% health, gain 2-20 Attack Damage or 4-40 Ability Power (adaptive based on level).",
        "desc_pt": "Com mais de 65% de vida, ganhe 2-20 de Dano de Ataque ou 4-40 de Poder de Habilidade (adaptativo com base no nível)."
    },
    {
        "id": "nimbus_cloak",
        "name_es": "Capa del Nimbo",
        "name_en": "Nimbus Cloak",
        "name_pt": "Manto de Nimbus",
        "desc_es": "Tras usar un hechizo (Destello, Prender, etc.), obtienes un 10-40% de velocidad de movimiento adicional durante 3 s. La eficacia de esta mejora depende del enfriamiento del hechizo utilizado.",
        "desc_en": "After casting a summoner spell (Flash, Ignite, etc.), gain 10-40% bonus movement speed for 3s. Speed scale depends on spell cooldown.",
        "desc_pt": "Após usar um feitiço de invocador (Flash, Incendiar, etc.), ganhe 10-40% de velocidade de movimento adicional por 3s. A velocidade varia com o tempo de recarga do feitiço."
    },
    {
        "id": "scorch",
        "name_es": "Piroláser",
        "name_en": "Scorch",
        "name_pt": "Chamuscar",
        "desc_es": "Infligir daño a un campeón enemigo con una habilidad lo quema y le inflige entre 21 y 49 de daño mágico adicional (según el nivel) tras 1 s (8 s de enfriamiento).",
        "desc_en": "Damaging an enemy champion with an ability burns them for 21-49 bonus magic damage (based on level) after 1s (8s cooldown).",
        "desc_pt": "Causar dano a um campeão inimigo com uma habilidade queima o alvo causando 21-49 de dano mágico adicional (com base no nível) após 1s (8s de recarga)."
    },
    {
        "id": "gathering_storm",
        "name_es": "Se Avecina Tormenta",
        "name_en": "Gathering Storm",
        "name_pt": "Tempestade Crescente",
        "desc_es": "Tras 6 min de partida, otorga 2 de daño de ataque o 4 de poder de habilidad (adaptable), que aumentan cada 3 minutos a 5 o 10, 9 o 18, 14 o 28, etc.",
        "desc_en": "After 6 minutes, gain 2 Attack Damage or 4 Ability Power (adaptive), increasing every 3 minutes to 5/10, 9/18, 14/28, etc.",
        "desc_pt": "Após 6 min de jogo, concede 2 de Dano de Ataque ou 4 de Poder de Habilidade (adaptativo), aumentando a cada 3 min para 5/10, 9/18, 14/28, etc."
    },
    {
        "id": "botanist",
        "name_es": "Botanista",
        "name_en": "Botanist",
        "name_pt": "Botanista",
        "desc_es": "Cuando destruyes una planta, obtienes 10 de oro y efectos potenciados de la planta.\nFrutos de miel: Cuando se consumen, aumenta el efecto curativo un 20%.\nFlor del adivino: Cuando se destruye, la visión que otorga dura un 20% más.\nPiña explosiva: Tras el empujón, otorga un 40% de velocidad de movimiento durante 2,5 s.",
        "desc_en": "When destroying a jungle plant, gain 10 gold and enhanced plant effects.\nHoneyfruit: Increases healing by 20%.\nScryer's Bloom: Vision revealed lasts 20% longer.\nBlast Cone: Grants 40% movement speed for 2.5s after detonation.",
        "desc_pt": "Ao destruir uma planta da selva, ganhe 10 de ouro e efeitos aprimorados.\nFrutomel: Aumenta a cura em 20%.\nFlor do Vidente: Visão concedida dura 20% a mais.\nPitema Explosivo: Concede 40% de velocidade de movimento por 2,5s após o salto."
    },
    {
        "id": "hextech_flashtraption",
        "name_es": "Hextello",
        "name_en": "Hextech Flashtraption",
        "name_pt": "Flashtração Hextec",
        "desc_es": "Cuando Destello está en enfriamiento, se reemplaza por Hextello. Tras una canalización de hasta 2 s, te trasladas a una ubicación nueva. La distancia varía en función el tiempo de canalización (18 s de enfriamiento).\nPasa a 6 s de enfriamiento al entrar en combate con campeones.",
        "desc_en": "While Flash is on cooldown, it is replaced by Hexflash. Channel for up to 2s to blink to a target location (18s cooldown).\nCooldown is reduced to 6s after leaving combat with champions.",
        "desc_pt": "Enquanto Flash estiver em recarga, ele é substituído pelo Flashtração Hextec. Canalize por até 2s para piscar até o local alvo (18s de recarga).\nTempo de recarga vai para 6s ao sair de combate com campeões."
    },
    {
        "id": "ixtali_seedjar",
        "name_es": "Semillero Ixtalí",
        "name_en": "Ixtali Seedjar",
        "name_pt": "Sementeira Ixtali",
        "desc_es": "Al destruir una planta, obtienes una semilla al instante que reemplaza tu talismán durante 60 s. La semilla madura y se autodestruye poco después tras plantarla en la ubicación objetivo. (Cuando un aliado destruye una planta, también aparecerán semillas que puedes recoger).\nLas semillas están disponibles a partir del minuto 2 de la partida.\nCada planta tiene un enfriamiento de 30 s.\nLas piñas explosivas que plantes te lanzan más lejos al detonar.",
        "desc_en": "Destroying a plant grants a seed that replaces your trinket for 60s. Plant the seed to spawn a mature plant at target location.\nSeeds are available starting at minute 2:00.\nEach plant type has a 30s cooldown.\nPlanted Blast Cones knock you further.",
        "desc_pt": "Destruir uma planta concede uma semente que substitui sua sentinela por 60s. Plante a semente para fazer brotar uma planta madura no local alvo.\nSementes disponíveis a partir dos 2 min de jogo.\nCada tipo de planta tem 30s de recarga.\nPitemas Explosivos plantados lançam você mais longe."
    }
]

# 2. Spells
spells_translations = [
    {
        "id": "ghost",
        "name_es": "Fantasmal",
        "name_en": "Ghost",
        "name_pt": "Fantasma",
        "category_es": "Movilidad & Utilidad",
        "category_en": "Mobility & Utility",
        "category_pt": "Mobilidade e Utilidade",
        "desc_es": "Mapas aplicables: Wild Rift, Abismo de los Lamentos\n\nObtiene una gran mejora de velocidad de movimiento que decrece hasta un 25% de velocidad de movimiento adicional durante 8 s. La duración de Fantasmal aumenta en 6 s con cada asesinato o asistencia, lo que reinicia su efecto hasta la cifra inicial.",
        "desc_en": "Applicable maps: Wild Rift, Howling Abyss\n\nGain a large burst of movement speed decaying to 25% bonus movement speed for 8s. Ghost's duration increases by 6s with each champion takedown, resetting the boost to initial speed.",
        "desc_pt": "Mapas aplicáveis: Wild Rift, Howling Abyss\n\nReceba um grande bônus de velocidade de movimento que decai para 25% de velocidade de movimento adicional por 8s. A duração do Fantasma aumenta em 6s com cada eliminação ou assistência, reiniciando o bônus para o valor inicial."
    },
    {
        "id": "heal",
        "name_es": "Curar",
        "name_en": "Heal",
        "name_pt": "Curar",
        "category_es": "Movilidad & Utilidad",
        "category_en": "Mobility & Utility",
        "category_pt": "Mobilidade e Utilidade",
        "desc_es": "Mapas aplicables: Wild Rift, Abismo de los Lamentos\n\nRestaura entre 60 y 242 de vida y otorga un 30% de velocidad de movimiento adicional durante 1 s a ti y al campeón aliado más herido que se encuentre cerca.",
        "desc_en": "Applicable maps: Wild Rift, Howling Abyss\n\nRestores 60-242 health and grants 30% bonus movement speed for 1s to you and the most wounded nearby allied champion.",
        "desc_pt": "Mapas aplicáveis: Wild Rift, Howling Abyss\n\nRestaura entre 60 e 242 de vida e concede 30% de velocidade de movimento adicional por 1s para você e o campeão aliado próximo mais ferido."
    },
    {
        "id": "barrier",
        "name_es": "Barrera",
        "name_en": "Barrier",
        "name_pt": "Barreira",
        "category_es": "Movilidad & Utilidad",
        "category_en": "Mobility & Utility",
        "category_pt": "Mobilidade e Utilidade",
        "desc_es": "Mapas aplicables: Wild Rift, Abismo de los Lamentos\n\nObtienes un escudo que absorbe entre 100 y 450 de daño durante 2 s.",
        "desc_en": "Applicable maps: Wild Rift, Howling Abyss\n\nGain a shield that absorbs 100-450 damage for 2s.",
        "desc_pt": "Mapas aplicáveis: Wild Rift, Howling Abyss\n\nReceba um escudo que absorve entre 100 e 450 de dano por 2s."
    },
    {
        "id": "exhaust",
        "name_es": "Extenuación",
        "name_en": "Exhaust",
        "name_pt": "Exaustão",
        "category_es": "Combate & Daño",
        "category_en": "Combat & Damage",
        "category_pt": "Combate e Dano",
        "desc_es": "Mapas aplicables: Wild Rift, Abismo de los Lamentos\n\nExtenúa a un campeón enemigo objetivo, lo que reduce su velocidad de movimiento un 60% y el daño que inflige un 40% durante 2,5 s.",
        "desc_en": "Applicable maps: Wild Rift, Howling Abyss\n\nExhausts target enemy champion, reducing their movement speed by 60% and damage dealt by 40% for 2.5s.",
        "desc_pt": "Mapas aplicáveis: Wild Rift, Howling Abyss\n\nExaure um campeão inimigo alvo, reduzindo sua velocidade de movimento em 60% e o dano causado em 40% por 2,5s."
    },
    {
        "id": "cleanse",
        "name_es": "Limpiar",
        "name_en": "Cleanse",
        "name_pt": "Purificar",
        "category_es": "Movilidad & Utilidad",
        "category_en": "Mobility & Utility",
        "category_pt": "Mobilidade e Utilidade",
        "desc_es": "Mapas aplicables: Wild Rift\n\nElimina todas las debilitaciones e incapacitaciones que afecten a tu campeón y otorga inmunidad a las incapacitaciones durante 0,75 s.",
        "desc_en": "Applicable maps: Wild Rift\n\nRemoves all crowd control debuffs and disables affecting your champion and grants crowd control immunity for 0.75s.",
        "desc_pt": "Mapas aplicáveis: Wild Rift\n\nRemove todos os efeitos negativos e debuffs de controle de grupo que afetam seu campeão e concede imunidade a controle de grupo por 0,75s."
    },
    {
        "id": "flash",
        "name_es": "Destello",
        "name_en": "Flash",
        "name_pt": "Flash",
        "category_es": "Movilidad & Utilidad",
        "category_en": "Mobility & Utility",
        "category_pt": "Mobilidade e Utilidade",
        "desc_es": "Mapas aplicables: Wild Rift, Abismo de los Lamentos\n\nTeleporta a tu campeón una corta distancia hacia delante o hacia la dirección objetivo.",
        "desc_en": "Applicable maps: Wild Rift, Howling Abyss\n\nTeleports your champion a short distance toward your target cursor direction.",
        "desc_pt": "Mapas aplicáveis: Wild Rift, Howling Abyss\n\nTeletransporta seu campeão por uma curta distância na direção do cursor alvo."
    },
    {
        "id": "ignite",
        "name_es": "Prender",
        "name_en": "Ignite",
        "name_pt": "Incendiar",
        "category_es": "Combate & Daño",
        "category_en": "Combat & Damage",
        "category_pt": "Combate e Dano",
        "desc_es": "Mapas aplicables: Wild Rift, Abismo de los Lamentos\n\nPrende a un campeón enemigo objetivo, lo que inflige entre 60 y 410 de daño verdadero a lo largo de 5 s y le aplica un 60% de Heridas Graves.",
        "desc_en": "Applicable maps: Wild Rift, Howling Abyss\n\nIgnites target enemy champion, dealing 60-410 true damage over 5s and applying 60% Grievous Wounds.",
        "desc_pt": "Mapas aplicáveis: Wild Rift, Howling Abyss\n\nIncendeia o campeão inimigo alvo, causando entre 60 e 410 de dano verdadeiro ao longo de 5s e aplicando 60% de Feridas Dolorosas."
    },
    {
        "id": "smite",
        "name_es": "Castigo",
        "name_en": "Smite",
        "name_pt": "Golpear",
        "category_es": "Combate & Daño",
        "category_en": "Combat & Damage",
        "category_pt": "Combate e Dano",
        "desc_es": "Mapas aplicables: Wild Rift\n\nInflige 600-1000 de daño verdadero a monstruos de la jungla o súbditos. Tras 3 usos en monstruos de la jungla, evoluciona a Castigo Desafiante o Castigo Devastador.",
        "desc_en": "Applicable maps: Wild Rift\n\nDeals 600-1000 true damage to jungle monsters or minions. After 3 uses on jungle monsters, evolves into Chilling Smite or Challenging Smite.",
        "desc_pt": "Mapas aplicáveis: Wild Rift\n\nCausa 600-1000 de dano verdadeiro a monstros da selva ou tropas. Após 3 usos em monstros da selva, evolui para Golpe Desafiador ou Golpe Devastador."
    },
    {
        "id": "teleport",
        "name_es": "Teleportar",
        "name_en": "Teleport",
        "name_pt": "Teleporte",
        "category_es": "Movilidad & Utilidad",
        "category_en": "Mobility & Utility",
        "category_pt": "Mobilidade e Utilidade",
        "desc_es": "Mapas aplicables: Wild Rift\n\nTras canalizar durante 3 s, te teleportas hacia una estructura aliada, centinela o súbdito objetivo.",
        "desc_en": "Applicable maps: Wild Rift\n\nAfter channeling for 3s, teleport to a target allied turret, ward, or minion.",
        "desc_pt": "Mapas aplicáveis: Wild Rift\n\nApós canalizar por 3s, teletransporta seu campeão até uma estrutura aliada, sentinela ou tropa alvo."
    },
    {
        "id": "clarity",
        "name_es": "Claridad",
        "name_en": "Clarity",
        "name_pt": "Clareza",
        "category_es": "Movilidad & Utilidad",
        "category_en": "Mobility & Utility",
        "category_pt": "Mobilidade e Utilidade",
        "desc_es": "Mapas aplicables: Abismo de los Lamentos\n\nRestaura un 50% del maná máximo a tu campeón y un 25% del maná máximo a los aliados cercanos.",
        "desc_en": "Applicable maps: Howling Abyss\n\nRestores 50% max mana to your champion and 25% max mana to nearby allies.",
        "desc_pt": "Mapas aplicáveis: Howling Abyss\n\nRestaura 50% da mana máxima para seu campeão e 25% da mana máxima para aliados próximos."
    },
    {
        "id": "mark",
        "name_es": "Marca y Deslizamiento",
        "name_en": "Mark / Dash",
        "name_pt": "Marcar / Avançar",
        "category_es": "Movilidad & Utilidad",
        "category_en": "Mobility & Utility",
        "category_pt": "Mobilidade e Utilidade",
        "desc_es": "Mapas aplicables: Abismo de los Lamentos\n\nLanza una bola de nieve en línea recta. Si golpea a un enemigo, puedes reactivar la habilidad para desplazarte directamente hasta el objetivo marcado.",
        "desc_en": "Applicable maps: Howling Abyss\n\nThrow a snowball in a straight line. If it hits an enemy, reactivate to dash directly to the marked target.",
        "desc_pt": "Mapas aplicáveis: Howling Abyss\n\nLança uma bola de neve em linha reta. Se atingir um inimigo, reative para avançar diretamente até o alvo marcado."
    }
]

# Update translations_en.json and translations_pt.json
with open('app/src/main/assets/translations_en.json', 'r', encoding='utf-8') as f:
    en_json = json.load(f)

with open('app/src/main/assets/translations_pt.json', 'r', encoding='utf-8') as f:
    pt_json = json.load(f)

# Categories
cats = {
    "Clave": ("Keystones", "Essenciais"),
    "Dominación": ("Domination", "Dominação"),
    "Precisión": ("Precision", "Precisão"),
    "Valor": ("Resolve", "Determinação"),
    "Brujería": ("Sorcery", "Feitiçaria"),
    "Inspiración": ("Inspiration", "Inspiração"),
    "Movilidad & Utilidad": ("Mobility & Utility", "Mobilidade e Utilidade"),
    "Combate & Daño": ("Combat & Damage", "Combate e Dano"),
    "Defensivo": ("Defensive", "Defensivo"),
    "Ofensivo": ("Offensive", "Ofensivo"),
    "Jungla": ("Jungle", "Selva"),
    "Soporte & Supervivencia": ("Support & Survival", "Suporte e Sobrevivência"),
    "Control & Debilitación": ("Crowd Control & Debuff", "Controle e Debuff"),
    "Utilidad (ARAM)": ("Utility (ARAM)", "Utilidade (ARAM)"),
    "Básicos": ("Basic Items", "Itens Básicos"),
    "Nivel Medio": ("Mid Tier", "Nível Médio"),
    "Daño Físico": ("Physical Damage", "Dano Físico"),
    "Daño Mágico": ("Magic Damage", "Dano Mágico"),
    "Defensa": ("Defense", "Defesa"),
    "Soporte": ("Support", "Suporte"),
    "Botas N2": ("Tier 2 Boots", "Botas N2"),
    "Botas N3": ("Tier 3 Boots", "Botas N3"),
    "Encantamientos": ("Enchantments & Actives", "Encantamentos e Ativos"),
    "BASIC": ("Basic Items", "Itens Básicos"),
    "MID_TIER": ("Mid Tier", "Nível Médio"),
    "PHYSICAL": ("Physical Damage", "Dano Físico"),
    "MAGIC": ("Magic Damage", "Dano Mágico"),
    "DEFENSE": ("Defense", "Defesa"),
    "SUPPORT": ("Support", "Suporte"),
    "BOOTS_T2": ("Tier 2 Boots", "Botas N2"),
    "BOOTS_T3": ("Tier 3 Boots", "Botas N3"),
    "ACTIVE": ("Enchantments & Actives", "Encantamentos e Ativos"),
    "TODOS": ("All", "Todos"),
    "Todos": ("All", "Todos")
}

for k, (en_val, pt_val) in cats.items():
    en_json[k] = en_val
    pt_json[k] = pt_val

# Runes
for r in runes_translations:
    en_json[r["name_es"]] = r["name_en"]
    pt_json[r["name_es"]] = r["name_pt"]
    en_json[r["desc_es"]] = r["desc_en"]
    pt_json[r["desc_es"]] = r["desc_pt"]

# Spells
for s in spells_translations:
    en_json[s["name_es"]] = s["name_en"]
    pt_json[s["name_es"]] = s["name_pt"]
    en_json[s["desc_es"]] = s["desc_en"]
    pt_json[s["desc_es"]] = s["desc_pt"]
    en_json[s["category_es"]] = s["category_en"]
    pt_json[s["category_es"]] = s["category_pt"]

# Clean identity mappings in en_json and pt_json
cleaned_en = {}
for k, v in en_json.items():
    cleaned_en[k] = v

cleaned_pt = {}
for k, v in pt_json.items():
    cleaned_pt[k] = v

with open('app/src/main/assets/translations_en.json', 'w', encoding='utf-8') as f:
    json.dump(cleaned_en, f, ensure_ascii=False, indent=2)

with open('app/src/main/assets/translations_pt.json', 'w', encoding='utf-8') as f:
    json.dump(cleaned_pt, f, ensure_ascii=False, indent=2)

print("Updated translations_en.json and translations_pt.json successfully!")
