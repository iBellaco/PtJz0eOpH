import json

tips = [
    (
        "💡 Consejo del Coach:",
        "💡 Coach's Tip:",
        "💡 Dica do Coach:"
    ),
    (
        "💡 Recomendación de Invocador:",
        "💡 Summoner Recommendation:",
        "💡 Recomendação de Invocador:"
    ),
    (
        "💡 Ideal para combos cortos de asesinos o magos que buscan estallar a un rival rápido.",
        "💡 Ideal for short burst combos on assassins or mages looking to burst a target down quickly.",
        "💡 Ideal para combos curtos de assassinos ou magos que buscam explodir um rival rapidamente."
    ),
    (
        "💡 Perfecto para campeones que escalan y aseguran asesinatos en peleas largas (ej. Katarina, Khazix).",
        "💡 Perfect for scaling champions who secure takedowns in extended teamfights (e.g. Katarina, Kha'Zix).",
        "💡 Perfeito para campeões que escalam e garantem eliminações em lutas longas (ex: Katarina, Kha'Zix)."
    ),
    (
        "💡 Excelente para tiradores o luchadores que dependen de ataques básicos rápidos.",
        "💡 Excellent for marksmen or fighters who rely on rapid basic attacks.",
        "💡 Excelente para atiradores ou lutadores que dependem de ataques básicos rápidos."
    ),
    (
        "💡 Fundamental en hypercarries como Jinx o Vayne para dominar las peleas largas.",
        "💡 Essential on hypercarries like Jinx or Vayne to dominate extended teamfights.",
        "💡 Fundamental em hypercarries como Jinx ou Vayne para dominar lutas longas."
    ),
    (
        "💡 Útil para sobrevivir líneas difíciles gracias a su curación y movilidad al kitear.",
        "💡 Useful for surviving tough lanes thanks to its healing and kiting mobility.",
        "💡 Útil para sobreviver a rotas difíceis graças à sua cura e mobilidade ao caitar."
    ),
    (
        "💡 La mejor opción para luchadores y duelistas que buscan intercambios prolongados (ej. Darius, Riven).",
        "💡 Best choice for fighters and duelists seeking extended trades (e.g. Darius, Riven).",
        "💡 A melhor opção para lutadores e duelistas que buscam trocas prolongadas (ex: Darius, Riven)."
    ),
    (
        "💡 Indispensable en tanques y colosos para tener sustain y escalar vida máxima.",
        "💡 Essential on tanks and juggernauts for sustain and scaling maximum health.",
        "💡 Indispensável em tanques e colossos para ter sustentação e escalar vida máxima."
    ),
    (
        "💡 Selecciona esta runa en soportes protectores (ej. Braum, Lulu) para mitigar burst enemigo.",
        "💡 Pick this rune on peeling supports (e.g. Braum, Lulu) to mitigate enemy burst.",
        "💡 Escolha esta runa em suportes protetores (ex: Braum, Lulu) para mitigar o burst inimigo."
    ),
    (
        "💡 Muy versátil para soportes encantadores o magos de pokeo constante (ej. Karma, Orianna).",
        "💡 Highly versatile for enchanters or constant poke mages (e.g. Karma, Orianna).",
        "💡 Muito versátil para suportes encantadores ou magos de poke constante (ex: Karma, Orianna)."
    ),
    (
        "💡 Ideal para magos de artillería que pokean a distancia (ej. Ziggs, Lux).",
        "💡 Ideal for artillery mages poking from long range (e.g. Ziggs, Lux).",
        "💡 Ideal para magos de artilharia que dão poke à distância (ex: Ziggs, Lux)."
    ),
    (
        "💡 Perfecta para magos de combo que necesitan reposicionarse rápido (ej. Orianna, Vladimir).",
        "💡 Perfect for combo mages needing fast repositioning (e.g. Orianna, Vladimir).",
        "💡 Perfeita para magos de combo que precisam se reposicionar rápido (ex: Orianna, Vladimir)."
    ),
    (
        "💡 Útil en asesinos o magos de ráfaga para escalar en oro rápidamente y explotar objetivos.",
        "💡 Useful on assassins or burst mages to snowball gold quickly and burst targets.",
        "💡 Útil em assassinos ou magos de dano explosivo para acumular ouro rápido e explodir alvos."
    ),
    (
        "💡 Excelente para soportes de iniciación (ej. Leona, Nautilus) para potenciar su CC.",
        "💡 Excellent for engage supports (e.g. Leona, Nautilus) to amplify their crowd control.",
        "💡 Excelente para suportes de iniciação (ex: Leona, Nautilus) para potencializar seu controle de grupo."
    ),
    (
        "💡 Runa perfecta para tanques de iniciación masiva (ej. Amumu, Alistar) que necesitan resistir el focus enemigo post-combo.",
        "💡 Perfect rune for heavy engage tanks (e.g. Amumu, Alistar) needing durability after initiating.",
        "💡 Runa perfeita para tanques de iniciação massiva (ex: Amumu, Alistar) que precisam resistir ao foco inimigo pós-combo."
    ),
    (
        "💡 Ideal en peleas de equipo cerradas. Te recompensa con vida vital tras cada eliminación o asistencia.",
        "💡 Ideal in close teamfights. Rewards you with vital health after each takedown or assist.",
        "💡 Ideal em lutas de equipe acirradas. Recompensa você com vida vital após cada eliminação ou assistência."
    ),
    (
        "💡 Útil en intercambios sostenidos cortos, incrementa tu daño para asegurar duelos tempranos.",
        "💡 Useful in short sustained trades, ramps up your damage to secure early duels.",
        "💡 Útil em trocas sustentadas curtas, aumenta seu dano para garantir duelos no início do jogo."
    ),
    (
        "💡 Obligatorio si el equipo enemigo tiene muchos tanques y campeones con mucha vida extra.",
        "💡 Mandatory if the enemy team has multiple tanks and high bonus-health champions.",
        "💡 Obrigatório se o time inimigo tiver muitos tanques e campeões com muita vida adicional."
    ),
    (
        "💡 Para asesinos o ADC que buscan asegurar la baja (ejecutar) a enemigos que intenten escapar a baja vida.",
        "💡 For assassins or marksmen looking to execute low-health fleeing enemies.",
        "💡 Para assassinos ou atiradores que buscam executar inimigos em fuga com pouca vida."
    ),
    (
        "💡 Escoge esta runa si priorizas maximizar tu DPS (daño por segundo) a través de ataques básicos rápidos.",
        "💡 Choose this rune to maximize sustained DPS through faster basic attacks.",
        "💡 Escolha esta runa se priorizar maximizar seu DPS através de ataques básicos rápidos."
    ),
    (
        "💡 Vital si el equipo enemigo está lleno de control de masas (Stun, Inmovilización, etc). Evitará que te eliminen encadenado.",
        "💡 Vital if the enemy team is packed with CC (Stuns, Roots, etc.). Prevents CC-chain deaths.",
        "💡 Vital se o time inimigo estiver repleto de controle de grupo (Atordoamento, Enraizamento, etc.). Evita ser eliminado em cadeia."
    ),
    (
        "💡 Si tu campeón no armará Robo de Vida temprano pero necesita sustento para sobrevivir y farmear.",
        "💡 If your champion doesn't build early Life Steal but needs sustain to farm and stay on the map.",
        "💡 Se o seu campeão não comprar Roubo de Vida cedo mas precisar de sustentação para farmar."
    ),
    (
        "💡 Excelente en duelistas como Olaf o Tryndamere que se vuelven más letales cuando se acercan a la muerte.",
        "💡 Excellent on duelists like Olaf or Tryndamere who become deadliest at low health.",
        "💡 Excelente em duelistas como Olaf ou Tryndamere que se tornam mais letais próximos da morte."
    ),
    (
        "💡 Runa situacional: Úsala para complementar el estilo de juego de tu campeón frente a esta composición específica.",
        "💡 Situational rune: Use it to complement your champion's playstyle against this specific matchup.",
        "💡 Runa situacional: Use para complementar o estilo de jogo do seu campeão contra esta composição específica."
    ),
    (
        "Imprescindible en el 99% de las partidas para reposicionarse, iniciar peleas de equipo o escapar por encima de muros.",
        "Essential in 99% of matches for repositioning, initiating teamfights, or flashing over walls to escape.",
        "Essencial em 99% das partidas para se reposicionar, iniciar lutas de equipe ou atravessar paredes para fugir."
    ),
    (
        "Clave para asesinos y soportes agresivos para asegurar asesinatos en juego temprano y anular curaciones de campeones como Aatrox, Soraka o Dr. Mundo.",
        "Key for assassins and aggressive supports to secure early kills and counter heavy healing champions like Aatrox, Soraka, or Dr. Mundo.",
        "Chave para assassinos e suportes agressivos para garantir abates no início de jogo e anular curas de campeões como Aatrox, Soraka ou Dr. Mundo."
    ),
    (
        "Obligatorio para el rol de Jungla para asegurar monstruos épicos (Dragones, Heraldo, Barón) y farmear eficientemente.",
        "Mandatory for the Jungle role to secure epic monsters (Dragons, Rift Herald, Baron) and clear camps efficiently.",
        "Obrigatório para a Selva para garantir monstros épicos (Dragões, Arauto, Barão) e farmar eficientemente."
    ),
    (
        "Vital para neutralizar a hipercarries o asesinos rivales en peleas grupales reduciendo su daño y movilidad drásticamente.",
        "Vital for shutting down enemy hypercarries or assassins in teamfights by drastically reducing their damage and mobility.",
        "Vital para neutralizar hipercarries ou assassinos rivais em lutas de equipe, reduzindo seu dano e mobilidade drasticamente."
    ),
    (
        "Excelente para tiradores o magos de ráfaga para resistir emboscadas o burst sorpresa en línea.",
        "Excellent for marksmen or burst mages to survive surprise ganks and burst in lane.",
        "Excelente para atiradores ou magos de burst para resistir a emboscadas ou dano surpresa na rota."
    ),
    (
        "Ideal para campeones con movilidad continua como Darius, Olaf, Singed o Gwen para evitar que los enemigos escapen.",
        "Ideal for sustained mobility champions like Darius, Olaf, Singed, or Gwen to run down fleeing enemies.",
        "Ideal para campeões com mobilidade contínua como Darius, Olaf, Singed ou Gwen para evitar que os inimigos fujam."
    ),
    (
        "Potente para campeones de carril de Barón para mantener presión dividida y unirse inmediatamente a peleas de objetivos.",
        "Powerful for Baron Laners to maintain split push pressure and instantly join objective teamfights.",
        "Poderoso para a Rota do Barão para manter split push e se juntar instantaneamente às lutas por objetivos."
    ),
    (
        "Uso situacional según la composición y mapa.",
        "Situational pick based on matchup and game mode.",
        "Uso situacional conforme a composição e o mapa."
    )
]

with open('app/src/main/assets/translations_en.json', 'r', encoding='utf-8') as f:
    en_json = json.load(f)

with open('app/src/main/assets/translations_pt.json', 'r', encoding='utf-8') as f:
    pt_json = json.load(f)

for es, en, pt in tips:
    en_json[es] = en
    pt_json[es] = pt

with open('app/src/main/assets/translations_en.json', 'w', encoding='utf-8') as f:
    json.dump(en_json, f, ensure_ascii=False, indent=2)

with open('app/src/main/assets/translations_pt.json', 'w', encoding='utf-8') as f:
    json.dump(pt_json, f, ensure_ascii=False, indent=2)

print("Added coach tips translations successfully!")
