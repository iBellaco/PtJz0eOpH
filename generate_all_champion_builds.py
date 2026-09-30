import json, re, os, uuid

# 1. Load catalogs
with open("app/src/main/java/com/example/data/WildRiftItemsData.kt") as f:
    items_kt = f.read()

item_blocks = re.findall(r"WildRiftItem\((.*?)\)(?:,|\s*\))", items_kt, re.DOTALL)
items = {}
for b in item_blocks:
    id_m = re.search(r'id\s*=\s*"([^"]+)"', b)
    name_m = re.search(r'name\s*=\s*"([^"]+)"', b)
    cat_m = re.search(r'category\s*=\s*"([^"]+)"', b)
    tip_m = re.search(r'coachTip\s*=\s*"([^"]+)"', b)
    icon_m = re.search(r'iconUrl\s*=\s*"([^"]+)"', b)
    if id_m and name_m:
        items[name_m.group(1)] = {
            "id": id_m.group(1),
            "name": name_m.group(1),
            "category": cat_m.group(1) if cat_m else "",
            "coachTip": tip_m.group(1) if tip_m else "",
            "iconUrl": icon_m.group(1) if icon_m else ""
        }

with open("app/src/main/java/com/example/data/WildRiftSpellsAndRunes.kt") as f:
    spells_runes_kt = f.read()

rune_blocks = re.findall(r"RuneItem\((.*?)\)(?:,|\s*\))", spells_runes_kt, re.DOTALL)
runes = {}
for b in rune_blocks:
    id_m = re.search(r'id\s*=\s*"([^"]+)"', b)
    name_m = re.search(r'name\s*=\s*"([^"]+)"', b)
    cat_m = re.search(r'category\s*=\s*"([^"]+)"', b)
    icon_m = re.search(r'iconUrl\s*=\s*"([^"]+)"', b)
    desc_m = re.search(r'description\s*=\s*"([^"]+)"', b)
    if id_m and name_m and cat_m:
        runes[name_m.group(1)] = {
            "id": id_m.group(1),
            "name": name_m.group(1),
            "category": cat_m.group(1),
            "iconUrl": icon_m.group(1) if icon_m else "",
            "description": desc_m.group(1) if desc_m else ""
        }

spell_blocks = re.findall(r"SummonerSpellItem\((.*?)\)(?:,|\s*\))", spells_runes_kt, re.DOTALL)
spells = {}
for b in spell_blocks:
    id_m = re.search(r'id\s*=\s*"([^"]+)"', b)
    name_m = re.search(r'name\s*=\s*"([^"]+)"', b)
    icon_m = re.search(r'iconUrl\s*=\s*"([^"]+)"', b)
    desc_m = re.search(r'description\s*=\s*"([^"]+)"', b)
    if id_m and name_m:
        spells[name_m.group(1)] = {
            "id": id_m.group(1),
            "name": name_m.group(1),
            "iconUrl": icon_m.group(1) if icon_m else "",
            "description": desc_m.group(1) if desc_m else ""
        }

# 2. Boot pairs mapping
BOOT_T2_TO_T3 = {
    "Botas blindadas": "Avance blindado",
    "Botas de mercurio": "Trituradoras encadenadas",
    "Botas de maná": "Botas del lanzahechizos",
    "Grebas de berserker": "Grebas de metal",
    "Grebas codiciosas": "Botas inmortales",
    "Botas jonias de la lucidez": "Lucidez carmesí",
    "Botas dinámicas": "Botas quebrantarmaduras",
}

BOOT_TIPS = {
    "Botas blindadas": "Reduce drásticamente el daño de ataques básicos directos de tiradores y duelistas AD.",
    "Avance blindado": "Otorga armadura reforzada de Nivel 3 y aceleración de movimiento defensiva al recibir ataques físicos.",
    "Botas de mercurio": "Otorga resistencia mágica y tenacidad fundamental para reducir aturdimientos y cadenas de control.",
    "Trituradoras encadenadas": "Mejora de Nivel 3 que maximiza la tenacidad y resistencia ante ráfagas mágicas continuas.",
    "Botas de maná": "Provee penetración mágica plana y restauración continua de maná para maximizar el daño de habilidades.",
    "Botas del lanzahechizos": "Mejora de Nivel 3 que amplifica el poder de habilidad y penetración en escaramuzas mágicas.",
    "Grebas de berserker": "Incrementa sustancialmente la velocidad de ataque y sustentación de vida por impacto básico.",
    "Grebas de metal": "Mejora de Nivel 3 que maximiza la cadencia de disparo y daño continuado en peleas prolongadas.",
    "Grebas codiciosas": "Aporta omnisucción y daño adaptable para campeones que requieren sustentación en duelos largos.",
    "Botas inmortales": "Mejora de Nivel 3 que potencia el omnisucción y escudo salvavidas en momentos críticos de vida baja.",
    "Botas jonias de la lucidez": "Acelera los enfriamientos de habilidades y hechizos de invocador para rotaciones rápidas.",
    "Lucidez carmesí": "Mejora de Nivel 3 que optimiza la rotación de combos y reduce el tiempo de recarga de definitivas.",
    "Botas dinámicas": "Otorga penetración física temprana y velocidad de movimiento fuera de combate para rotaciones y emboscadas.",
    "Botas quebrantarmaduras": "Mejora de Nivel 3 que destruye armaduras defensivas facilitando la eliminación instantánea de objetivos blandos."
}

# 3. Spell advice
SPELL_TIPS = {
    "Destello": "", # Vacio segun la regla del usuario
    "Prender": "Aplica daño verdadero y heridas graves tempranas para asegurar la ejecución del objetivo en escaramuzas clave.",
    "Aplastar": "Hechizo obligatorio de jungla para asegurar monstruos neutrales, Dragones y Barón con daño verdadero garantizado.",
    "Barrera": "Otorga un escudo de absorción inmediata para contrarrestar daño explosivo y sobrevivir a all-ins enemigos.",
    "Extenuación": "Reduce drásticamente la velocidad de movimiento y el daño saliente de amenazas asesinas o tiradores rivales.",
    "Fantasmal": "Incrementa la velocidad de movimiento durante persecuciones y reposicionamiento en peleas de equipo extensas.",
    "Curar": "Restaura salud inmediata y otorga aceleración de movimiento para ti y tu aliado más cercano en situaciones críticas.",
    "Limpiar": "Remueve efectos inhabilitantes y control de masas hostil permitiendo escapar de emboscadas con cadenas de CC."
}

# 4. Rune advice
RUNE_TIPS = {
    "Conquistador": "Acumula fuerza adaptable en combate prolongado otorgando vampirismo y daño demoledor al máximo de cargas.",
    "Electrocutar": "Provoca daño adaptable instantáneo al conectar tres ataques o habilidades consecutivas en ráfaga.",
    "Primer Golpe": "Genera oro adicional y un 9% de daño verdadero extra al golpear primero en cualquier enfrentamiento.",
    "Compás Letal": "Aumenta exponencialmente la velocidad de ataque con cada impacto básico superando el límite estándar.",
    "Pies Veloces": "Carga energía con el movimiento para curar y otorgar un estallido de velocidad al atacar.",
    "Garras del Inmortal": "Restaura vida, otorga daño mágico adicional y aumenta permanentemente tu vida máxima en intercambios en línea.",
    "Aery": "Envía un espíritu que daña al rival con habilidades o coloca escudos protectores sobre aliados.",
    "Cometa Arcano": "Lanza un cometa que inflige daño adaptable de área tras impactar habilidades de hostigamiento.",
    "Irrupción de Fase": "Otorga una gran aceleración de movimiento y resistencia a ralentizaciones tras golpear con tres habilidades o ataques.",
    "Guardián": "Despliega un escudo sobre ti y tu aliado cercano al recibir daño severo en combate.",
    "Fortalecimiento": "Potencia el daño de ataques tras asestar habilidades para maximizar el comercio de daño corto.",
    "Cosecha Oscura": "Cosecha almas de enemigos con baja salud infligiendo daño adaptable escalable infinito.",
    "Soberano Gélido": "Ralentiza en abanico y reduce el daño de los enemigos inmovilizados protegiendo a tus aliados.",
    
    # Dominacion
    "Impacto Repentino": "Aumenta la penetración física y mágica tras utilizar deslizamientos, saltos o teletransportes.",
    "Colección de Globos Oculares": "Otorga fuerza adaptable acumulable con cada derribo de campeón enemigo.",
    "Tirano": "Aumenta el daño infligido a campeones según la diferencia de vida o estados inhabilitados.",
    "Golpe Bajo": "Inflige daño verdadero adicional a objetivos con movimiento o acciones reducidas.",
    "Ataque Potenciado": "Aumenta el daño del siguiente ataque básico tras un periodo breve de preparación.",
    "Soberbia": "Concede daño adicional tras participar en asesinatos recientes.",
    "Cazador Ingenioso": "Reduce el enfriamiento de objetos activos y pasivos con enfriamiento.",
    "Cazador Incesante": "Aumenta la velocidad de movimiento fuera de combate facilitando rotaciones rápidas.",
    "Guardián Zombi": "Genera centinelas aliados al destruir guardianes enemigos aumentando la visión y daño adaptable.",
    "Asalto Encadenado": "Otorga tenacidad y aceleración de movimiento continua en combate.",
    
    # Precision
    "Triunfo": "Restaura un 10% de la vida faltante y otorga oro adicional tras cada eliminación de campeón.",
    "Leyenda: Velocidad": "Otorga aceleración de habilidad progresiva al eliminar campeones y monstruos.",
    "Leyenda: Presteza": "Aumenta permanentemente la velocidad de ataque con cada derribo o acumulación.",
    "Leyenda: Linaje": "Otorga omnisucción y robo de vida permanente acumulable durante la partida.",
    "Golpe de Gracia": "Aumenta un 7% el daño infligido a enemigos con menos del 40% de vida.",
    "Último Esfuerzo": "Inflige hasta un 11% de daño adicional cuando tu propia vida se encuentra por debajo del 60%.",
    "Brutal": "Los ataques básicos infligen daño físico adaptable adicional al impactar.",
    "Derribado": "Incrementa el daño contra enemigos que posean significativamente mayor vida máxima que tú.",
    "Fervor de Batalla": "Acumula daño constante en intercambios físicos prolongados.",
    
    # Valor
    "Revestimiento de Huesos": "Mitiga el daño de los tres ataques o habilidades consecutivas de un mismo enemigo.",
    "Fuerzas Renovadas": "Regenera vida continuamente tras recibir daño de campeones enemigos en fase de líneas.",
    "Sobrecrecimiento": "Aumenta permanentemente tu vida máxima cada vez que mueren súbditos o monstruos cerca de ti.",
    "Fuente de Vida": "Marca a los rivales inmovilizados para que tus aliados se curen al atacarlos.",
    "Inquebrantable": "Otorga armadura y resistencia mágica adicionales que se incrementan con vida baja.",
    "Demoler": "Carga un golpe demoledor contra torretas enemigas para conseguir placas de oro temprano.",
    "Revitalizar": "Potencia las curaciones y escudos otorgados o recibidos en un 5% (10% si el objetivo tiene baja vida).",
    "Coraje del Coloso": "Otorga un escudo inmediato tras asestar habilidades de inmovilización a campeones rivales.",
    "Orbe Anulador": "Despliega un escudo protector de emergencia al caer por debajo del 35% de vida.",
    "Perseverancia": "Otorga resistencia al daño de ráfaga y control de masas tras recibir impactos.",
    
    # Brujeria
    "Banda de Maná": "Incrementa tu reserva máxima de maná permanentemente al golpear campeones con habilidades.",
    "Trascendencia": "Concede aceleración de habilidad progresiva y reduce enfriamientos activos al conseguir derribos.",
    "Se Avecina Tormenta": "Otorga poder de habilidad o daño de ataque adaptable creciente cada pocos minutos.",
    "Piroláser": "Prende fuego al enemigo infligiendo daño mágico residual en el primer impacto de habilidad.",
    "Celeridad": "Aumenta la velocidad de movimiento pasiva y amplifica cualquier mejora de velocidad activa.",
    "Concentración Absoluta": "Otorga fuerza adaptable adicional mientras te mantengas por encima del 70% de vida.",
    "Capa del Nimbo": "Concede un estallido de velocidad de movimiento tras lanzar cualquier hechizo de invocador.",
    "Arcanólogo Axiomático": "Reduce el enfriamiento de la habilidad definitiva tras derribos de campeones.",
    "Botanista": "Potencia el uso de plantas del mapa y frutos de miel.",
    "Hextello": "Canaliza un teletransporte de corto alcance mientras Destello esté en enfriamiento.",
    "Semillero Ixtalí": "Permite recoger y plantar semillas de plantas tácticas del mapa."
}

print("Base data tables prepared.")
