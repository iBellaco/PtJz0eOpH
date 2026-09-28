import json, re

# 1. Load champion tacticalAdvices
with open("app/src/main/res/raw/champions_part1.json") as f:
    c1 = json.load(f)
with open("app/src/main/res/raw/champions_part2.json") as f:
    c2 = json.load(f)

all_champs = c1 + c2

def translate_es_to_pt(text):
    t = text
    t = t.replace("Habilidad 1 (H1)", "Habilidade 1 (H1)")
    t = t.replace("Habilidad 2 (H2)", "Habilidade 2 (H2)")
    t = t.replace("Habilidad 3 (H3)", "Habilidade 3 (H3)")
    t = t.replace("Habilidad Definitiva (H4)", "Ultimate (H4)")
    t = t.replace("Definitiva (H4)", "Ultimate (H4)")
    t = t.replace("Habilidad 1", "Habilidade 1")
    t = t.replace("Habilidad 2", "Habilidade 2")
    t = t.replace("Habilidad 3", "Habilidade 3")
    t = t.replace("Habilidad Definitiva", "Ultimate")
    t = t.replace(" Definitiva ", " Ultimate ")
    t = t.replace("Definitiva.", "Ultimate.")
    t = t.replace("Definitiva,", "Ultimate,")
    t = t.replace("Línea de Barón", "Rota do Barão")
    t = t.replace("Línea de Dragón", "Rota do Dragão")
    t = t.replace("Línea Central", "Rota Central")
    t = t.replace("línea", "rota")
    t = t.replace("Línea", "Rota")
    t = t.replace("carril", "rota")
    t = t.replace("Carril", "Rota")
    t = t.replace("súbditos", "tropas")
    t = t.replace("tirador", "atirador")
    t = t.replace("Tirador", "Atirador")
    t = t.replace("peleas de equipo", "lutas em equipe")
    t = t.replace("pelea de equipo", "luta em equipe")
    t = t.replace("control de masas", "controle de grupo")
    t = t.replace("aturdimiento", "atordoamento")
    t = t.replace("daño verdadero", "dano verdadeiro")
    t = t.replace("daño mágico", "dano mágico")
    t = t.replace("daño físico", "dano físico")
    t = t.replace("maleza", "moita")
    t = t.replace("torretas", "torres")
    t = t.replace("torreta", "torre")
    t = t.replace("torres enemigas", "torres inimigas")
    t = t.replace("Destello", "Flash")
    t = t.replace("Castigo", "Golpear")
    t = t.replace("Ignición", "Incendiar")
    t = t.replace("enemigo", "inimigo")
    t = t.replace("enemigos", "inimigos")
    t = t.replace("rival", "rival")
    t = t.replace("rivales", "rivais")
    t = t.replace("aliado", "aliado")
    t = t.replace("aliados", "aliados")
    t = t.replace("equipo", "equipe")
    t = t.replace("escudo", "escudo")
    t = t.replace("escudos", "escudos")
    t = t.replace("alcanzar", "alcançar")
    t = t.replace("alcanza", "alcança")
    t = t.replace("desplazamientos", "avanços")
    t = t.replace("desplazamiento", "avanço")
    t = t.replace("ralentización", "lentidão")
    t = t.replace("ralentizaciones", "lentidões")
    t = t.replace("enraizamiento", "enraizamento")
    t = t.replace("inmovilización", "imobilização")
    t = t.replace("velocidad de movimiento", "velocidade de movimento")
    t = t.replace("velocidad de ataque", "velocidade de ataque")
    t = t.replace("prioriza", "priorize")
    t = t.replace("Prioriza", "Priorize")
    t = t.replace("Guarda", "Guarde")
    t = t.replace("guarda", "guarde")
    t = t.replace("Usa", "Use")
    t = t.replace("usa", "use")
    t = t.replace("Lanza", "Lance")
    t = t.replace("lanza", "lance")
    t = t.replace("Busca", "Busque")
    t = t.replace("busca", "busque")
    t = t.replace("Entra", "Entre")
    t = t.replace("entra", "entre")
    t = t.replace("Combina", "Combe")
    t = t.replace("Activa", "Ative")
    t = t.replace("activa", "ative")
    t = t.replace("Mantén", "Mantenha")
    t = t.replace("mantén", "mantenha")
    t = t.replace("Ejecuta", "Execute")
    t = t.replace("ejecuta", "execute")
    t = t.replace("Conecta", "Conecte")
    t = t.replace("conecta", "conecte")

    return t

new_entries = {}

# Champion advices
for c in all_champs:
    adv = c.get("tacticalAdvice", "")
    if adv:
        new_entries[adv] = translate_es_to_pt(adv)

# Wombo combo strings
wombos = [
    ("💥 Wombo-Combo Aéreo Imparable", "💥 Wombo-Combo Aéreo Imparável"),
    ("🌀 Balón de Choque & Erradicación", "🌀 Onda de Choque e Erradicação"),
    ("🔥 Tormenta en Área & Encierro Masivo", "🔥 Tempestade em Área e Encerramento Massivo"),
    ("🌙 Atracción Lunar & Aniquilación", "🌙 Atração Lunar e Aniquilação"),
    ("🛡️ Hypercarry Blindado & Velocidad Letal", "🛡️ Hypercarry Blindado e Velocidade Letal"),
    ("⛓️ Cadena de CC & All-In Explosivo", "⛓️ Cadeia de CC e All-In Explosivo"),
    ("🌊 Electro-Ráfaga Acuática", "🌊 Eletro-Ráfaga Aquática"),
    ("💖 Pareja Letal: Baile de Plumas", "💖 Dupla Letal: Dança das Penas"),
    ("⚔️ Entrada Heroica & Cataclismo", "⚔️ Entrada Heróica e Cataclismo"),
    ("❄️ Freljord: Hielo Eterno", "❄️ Freljord: Gelo Eterno"),
    ("🎯 Trampas Encadenadas (Snare City)", "🎯 Armadilhas Encadeadas (Cidade do Enraizamento)"),
    ("👻 Submarino Invisible", "👻 Submarino Invisível"),
    ("⚡ El Gran Espectáculo & Tormenta", "⚡ O Grande Espetáculo e Tempestade"),
    ("🎶 Armonía Sinfónica & Encanto Infinito", "🎶 Harmonia Sinfônica e Charme Infinito"),
    ("💤 Sueño Inevitable & Destino Sellado", "💤 Sono Inevitável e Destino Selado"),
    ("⛓️ Aislamiento Hextech & Caída Heroica", "⛓️ Isolamento Hextech e Queda Heróica"),
    ("💣 Bomba Explosiva & Derribo Coordinado", "💣 Bomba Explosiva e Derrotada Coordenada"),
    ("Tip Coach", "Dica do Coach"),
]
for k, v in wombos:
    new_entries[k] = v

# Additional missing UI strings
extra_ui = [
    ("Consejo del rival: %s", "Conselho do rival: %s"),
    ("Pico de Definitiva (Nivel 5): En Wild Rift los enfriamientos de R son cortos (35-50s). Si %s falla su definitiva, castiga agresivamente antes del objetivo del minuto 5:00.", "Pico de Ultimate (Nível 5): No Wild Rift as recargas de R são curtas (35-50s). Se %s errar sua ultimate, puna agressivamente antes do objetivo do minuto 5:00."),
    ("Castiga cuando falle sus habilidades principales o use recursos en la oleada.", "Puna quando errar suas habilidades principais ou usar recursos na onda."),
    ("Obligatorio (%d/5)", "Obrigatório (%d/5)"),
    ("Precaución en fase temprana. Cede la prioridad de la primera oleada, farmea bajo torre y espera tu pico al nivel 3 (kit completo).", "Cautela na fase inicial. Ceda a prioridade da primeira onda, farm sob a torre e espere seu pico no nível 3 (kit completo)."),
    ("Intercambio en línea de Wild Rift", "Intercâmbio na rota do Wild Rift"),
    ("Nv. 5 (Definitiva)", "Nv. 5 (Ultimate)"),
    ("Macro y Objetivos Móviles: Al minuto 5:00 asegura la primera rotación (Dragón o Heraldo). En minuto 7:30 caen las placas de torre y a los 12:00 el Barón/Ancestral.", "Macro e Objetivos Móveis: Ao minuto 5:00 garanta a primeira rotação (Dragão ou Arauto). No minuto 7:30 caem as barricadas e aos 12:00 o Barão/Ancestral."),
    ("Línea neutra de Wild Rift. Controla los arbustos de línea, guarda la Flor del Adivino y castiga tras esquivar su habilidad principal.", "Rota neutra do Wild Rift. Controle os arbustos da rota, guarde a Flor do Vidente e puna após esquivar da habilidade principal."),
    ("🌐 Todas (%d)", "🌐 Todas (%d)"),
    ("Ventaja en intercambios tempranos. En Wild Rift la primera oleada otorga nivel 2 inmediato; presiona para denegar el Fruto de Miel (1:15).", "Vantagem em trocas precoces. No Wild Rift a primeira onda concede nível 2 imediato; pressione para negar o Fruto do Mel (1:15)."),
    ("Fuerte contra (Matchups favorables): %s", "Forte contra (Confrontos favoráveis): %s"),
    ("¿Estás seguro de que deseas vaciar todo el historial del perfil '%s'? Esta acción no se puede deshacer.", "Tem certeza de que deseja limpar todo o histórico do perfil '%s'? Esta ação não pode ser desfeita."),
    ("¿Estás seguro de que deseas eliminar el perfil '%s' y todo su historial asociado? Esta acción es irreversible y permanente.", "Tem certeza de que deseja excluir o perfil '%s' e todo o seu histórico associado? Esta ação é irreversível e permanente."),
    ("¿Estás seguro de vaciar todas las partidas guardadas de la cuenta '%s'?", "Tem certeza de que deseja limpar todas as partidas salvas da conta '%s'?"),
]
for k, v in extra_ui:
    new_entries[k] = v

with open("app/src/main/java/com/example/util/Translator.kt", "r") as f:
    content = f.read()

pt_marker = "\"pt\" to mapOf("
pt_pos = content.find(pt_marker)
if pt_pos == -1:
    print("Error finding pt map!")
    exit(1)

insert_pos = pt_pos + len(pt_marker)

formatted_lines = []
for k, v in new_entries.items():
    k_esc = k.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    v_esc = v.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    if f"\"{k_esc}\"" not in content:
        formatted_lines.append(f"        \"{k_esc}\" to \"{v_esc}\",")

print(f"Generated {len(formatted_lines)} new lines to insert.")

new_content = content[:insert_pos] + "\n" + "\n".join(formatted_lines) + content[insert_pos:]

with open("app/src/main/java/com/example/util/Translator.kt", "w") as f:
    f.write(new_content)

print("Translator.kt updated successfully!")
