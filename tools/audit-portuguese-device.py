"""Navigate the installed, signed-out APK and retain actual Android UI evidence."""
import json
import os
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

MODE = os.environ.get("COACH_AUDIT_LANGUAGE", "both")
if MODE not in ("both", "pt", "es"):
    raise ValueError("COACH_AUDIT_LANGUAGE must be both, pt or es")
CURRENT_LANGUAGE = "pt"
OUT = Path("app/build/reports/spanish-device" if MODE == "es" else "app/build/reports/portuguese-device")
OUT.mkdir(parents=True, exist_ok=True)
APP = "com.Coach"
source = Path("app/src/test/java/com/example/SpanishUiResidue.kt").read_text()
literal = re.search(r'val pattern = Regex\(\s*("(?:[^"\\]|\\.)*")', source).group(1)
pattern = json.loads(literal).replace(r"\p{L}\p{N}_", r"\w").replace(r"\p{L}", r"[^\W\d_]")
SPANISH = re.compile(pattern, re.IGNORECASE)
findings = []
authored_texts = []
screens = []


def adb(*args, binary=False):
    return subprocess.check_output(["adb", *args], text=not binary, timeout=40)


def window():
    last_error = "UI not ready"
    for attempt in range(10):
        try:
            # Never read a previous screen when Android cannot dump the new hierarchy.
            adb("shell", "rm", "-f", "/sdcard/coach-window.xml")
            adb("shell", "uiautomator", "dump", "/sdcard/coach-window.xml")
            xml = adb("exec-out", "cat", "/sdcard/coach-window.xml")
            root = ET.fromstring(xml)
            if root.tag == "hierarchy" and list(root.iter("node")):
                return xml
            last_error = "Empty Android UI hierarchy"
        except (subprocess.SubprocessError, ET.ParseError) as error:
            last_error = str(error)
        print("PORTUGUESE_DEVICE_WAIT:", attempt + 1, last_error, flush=True)
        time.sleep(1)
    (OUT / "hierarchy-error.txt").write_text(last_error)
    (OUT / "logcat.txt").write_text(adb("logcat", "-d", "-t", "500"))
    (OUT / "hierarchy-error.png").write_bytes(adb("exec-out", "screencap", "-p", binary=True))
    raise AssertionError("Android UI hierarchy did not become ready: " + last_error)


def app_nodes(xml):
    return [n for n in ET.fromstring(xml).iter("node") if n.get("package") == APP]


def is_channel_name(node):
    # Channel brands are authored content; their exact names must survive localization.
    return node.get("resource-id", "").rsplit("/", 1)[-1] == "streamer_channel_name"


def ui_text_candidates(nodes):
    result = []
    for node in nodes:
        if not is_channel_name(node) and node.get("text"):
            result.append(node.get("text"))
        # Accessibility labels are still application UI even on an authored name.
        if node.get("content-desc"):
            result.append(node.get("content-desc"))
    return list(dict.fromkeys(result))


def snapshot(name):
    xml = window()
    (OUT / (name + ".xml")).write_text(xml)
    (OUT / (name + ".png")).write_bytes(adb("exec-out", "screencap", "-p", binary=True))
    strings = list(dict.fromkeys(s for n in app_nodes(xml) for s in [n.get("text", ""), n.get("content-desc", "")] if s))
    if not strings:
        raise AssertionError("No application texts rendered: " + name)
    (OUT / (name + ".json")).write_text(json.dumps(strings, ensure_ascii=False, indent=2))
    nodes = app_nodes(xml)
    authored_texts.extend({"screen": name, "text": n.get("text"), "kind": "channel_name"} for n in nodes if is_channel_name(n) and n.get("text"))
    findings.extend({"screen": name, "text": s} for s in ui_text_candidates(nodes) if len(s) > 2 and SPANISH.search(s.replace("Lee Sin", "LeeSin")))
    screens.append(name)
    print("PORTUGUESE_DEVICE_SCREEN:", name, len(strings), "texts", flush=True)


size = re.search(r"(\d+)x(\d+)", adb("shell", "wm", "size"))
WIDTH, HEIGHT = map(int, size.groups())


def scroll(direction="down"):
    start, end = HEIGHT * 3 // 4, HEIGHT // 3
    if direction == "up":
        start, end = end, start
    adb("shell", "input", "swipe", str(WIDTH // 2), str(start), str(WIDTH // 2), str(end), "450")
    time.sleep(0.6)


def tap(label, scrolling=0, scroll_direction="down"):
    labels = (label,) if isinstance(label, str) else tuple(label)
    for attempt in range(max(10, scrolling + 1)):
        nodes = app_nodes(window())
        # A real remote announcement can arrive at any step, including onboarding.
        # Retain its Portuguese evidence before closing the blocking window.
        if any(n.get("text") == "COMUNICADO OFICIAL" for n in nodes):
            snapshot("official-notice-" + str(len(screens)))
            acknowledgment = next((n for n in nodes if n.get("text") == "Entendido"), None)
            if acknowledgment is not None:
                points = list(map(int, re.findall(r"\d+", acknowledgment.get("bounds", ""))))
                if len(points) == 4 and points[2] > points[0] and points[3] > points[1]:
                    adb("shell", "input", "tap", str((points[0] + points[2]) // 2), str((points[1] + points[3]) // 2))
                    time.sleep(0.8)
                    continue
        for node in nodes:
            if any(candidate in [node.get("text"), node.get("content-desc")] for candidate in labels):
                points = list(map(int, re.findall(r"\d+", node.get("bounds", ""))))
                if len(points) == 4 and points[2] > points[0] and points[3] > points[1]:
                    adb("shell", "input", "tap", str((points[0] + points[2]) // 2), str((points[1] + points[3]) // 2))
                    time.sleep(0.8)
                    return
        if attempt < scrolling:
            scroll(scroll_direction)
        else:
            time.sleep(1)
    snapshot("missing-" + str(len(screens)))
    raise AssertionError("Control not found: " + str(label))


def back():
    adb("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(0.8)


def enter_search_text(value):
    # Android key injection can outrun Compose/IME commits. Pace it and verify the actual value.
    for attempt in range(3):
        if attempt:
            tap('Fechar' if CURRENT_LANGUAGE == 'pt' else 'Cerrar')
            tap('Buscar item por nome ou estatísticas...' if CURRENT_LANGUAGE == 'pt' else 'Buscar objeto por nombre o estadísticas...')
        for letter in value:
            adb('shell', 'input', 'text', letter)
            time.sleep(0.08)
        back()
        actual = next((n.get('text', '') for n in app_nodes(window()) if n.get('class') == 'android.widget.EditText'), None)
        if actual == value:
            return
        print('SEARCH_INPUT_RETRY:', value, actual, flush=True)
    snapshot('search-input-failure')
    raise AssertionError('Android did not commit the complete search input: ' + value)


components = json.loads(Path('app/src/main/assets/wild_rift_component_items.json').read_text())
component_by_id = {entry['id']: entry for groups in components['secciones'].values() for entries in groups.values() for entry in entries}
adb("install", "-r", "app/build/outputs/apk/release/app-release.apk")
if MODE in ("pt", "both"):
    adb("shell", "pm", "clear", APP)
    adb("shell", "pm", "grant", APP, "android.permission.POST_NOTIFICATIONS")
    adb("shell", "input", "keyevent", "KEYCODE_WAKEUP")
    adb("shell", "wm", "dismiss-keyguard")
    adb("shell", "am", "start", "-W", "-n", APP + "/com.example.MainActivity")
    tap(("Português", "Portugués"))
    snapshot("language-portuguese")
    tap("Continuar em Português")
    snapshot("privacy")
    for tab in ["Termos", "Terceiros", "Privacidade"]:
        tap(tab)
        snapshot("legal-" + tab)
    tap("Aceitar e Entrar")
    for page in range(4):
        snapshot("onboarding-" + str(page + 1))
        tap("Seguinte" if page < 3 else "Começar agora!")
    snapshot("home")
    tap("🎨")
    snapshot("themes-portuguese")
    tap("Águas de Sentina", scrolling=6)
    snapshot("theme-preview-portuguese")
    tap("Aplicar", scrolling=6, scroll_direction="up")
    snapshot("theme-premium-portuguese")
    tap("Entendido")
    back()
    tap("Informação")
    snapshot("information")
    for page in range(3):
        scroll()
        snapshot("information-scroll-" + str(page + 1))
    tap("Perguntas Frequentes (FAQ)", scrolling=6)
    snapshot("faq")
    tap("Como funciona o overlay flutuante durante a partida?")
    snapshot("faq-answer")
    tap("Entendido")
    # FAQ returns directly to the dashboard; a second Back opens the exit dialog.
    back()
    for tab in ["Seleção", "Classificação", "Catálogo", "Usuário"]:
        tap(tab)
        snapshot("dashboard-" + tab)
        if tab == "Seleção":
            for page in range(3):
                scroll()
                snapshot("draft-empty-scroll-" + str(page + 1))
            texts = [n.get("text", "") for n in app_nodes(window())]
            if any("Melhor Opção segundo" in text for text in texts):
                raise AssertionError("Empty draft must not show team recommendations")
        if tab == "Classificação":
            tap("Entrar ou cadastrar-se", scrolling=3)
            snapshot("tier-login")
            tap("Cadastre-se", scrolling=3)
            snapshot("tier-registration")
            back()
            for page in range(2):
                scroll()
                snapshot("tier-guest-scroll-" + str(page + 1))
        if tab == "Catálogo":
            for catalog in ["Itens", "Runas", "Feitiços"]:
                tap(catalog)
                snapshot("catalog-" + catalog)
                scroll()
                snapshot("catalog-scroll-" + catalog)
        if tab == "Usuário":
            scroll()
            snapshot("user-scroll")
    # Validate new components in the real installed APK while Portuguese remains selected.
    tap("Catálogo")
    tap("Itens")
    tap("Recolher filtros")
    for item_id in ['tear_of_the_goddess', 'quicksilver_sash_mid_tier']:
        expected = component_by_id[item_id]
        tap('Buscar item por nome ou estatísticas...')
        enter_search_text(item_id)
        tap(expected['nombre_pt'], scrolling=3)
        snapshot('component-item-' + item_id)
        actual = [n.get('text', '') for n in app_nodes(window())]
        for stat in expected['estadisticas_pt']:
            if stat not in actual:
                raise AssertionError('Component Portuguese stat missing: ' + item_id + ': ' + stat)
        scroll()
        snapshot('component-item-' + item_id + '-passive')
        back()
        tap('Fechar')
    adb("shell", "am", "force-stop", APP)
    adb("shell", "am", "start", "-W", "-n", APP + "/com.example.MainActivity")
    tap("Início")
    tap("Informação")
    snapshot("restart-retains-portuguese")
    (OUT / "summary.json").write_text(json.dumps({"screens": screens, "findings": findings, "authored_texts": authored_texts}, ensure_ascii=False, indent=2))
    if findings:
        for finding in findings:
            print("PORTUGUESE_DEVICE_RESIDUE:", finding, flush=True)
        raise AssertionError("Spanish text remains in installed APK")
    print("PORTUGUESE_DEVICE_AUDIT:", len(screens), "screens, zero Spanish findings", flush=True)

# Reinstalling is unnecessary: inspect the same APK with Spanish selected and
# compare its actual item prices/stat rows with the user's requested corrections.
if MODE in ("es", "both"):
    CURRENT_LANGUAGE = 'es'
    OUT = OUT / 'spanish'
    OUT.mkdir(parents=True, exist_ok=True)
    # Shared words such as habilidades, recarga and concede are valid Spanish too.
    SPANISH = re.compile(r'\b(?:você|não|habilidade|dano|campeões|velocidade|adicionais|inimigos|acertos|assinatura|notificação|essências|usuário)\b', re.IGNORECASE)
    findings, authored_texts, screens = [], [], []
    adb('shell', 'pm', 'clear', APP)
    adb('shell', 'pm', 'grant', APP, 'android.permission.POST_NOTIFICATIONS')
    adb('shell', 'am', 'start', '-W', '-n', APP + '/com.example.MainActivity')
    tap('Español')
    snapshot('language-spanish')
    tap('Continuar en Español')
    snapshot('privacy-spanish')
    for tab in ['Términos', 'Terceros', 'Privacidad']:
        tap(tab)
        snapshot('legal-' + tab)
    tap('Aceptar y Entrar')
    for page in range(4):
        snapshot('onboarding-' + str(page + 1))
        tap('Siguiente' if page < 3 else '¡Comenzar ahora!')
    snapshot('home-spanish')
    tap('🎨')
    snapshot('themes-spanish')
    tap('Aguas Estancadas', scrolling=6)
    snapshot('theme-preview-spanish')
    tap('Aplicar', scrolling=6, scroll_direction='up')
    snapshot('theme-premium-spanish')
    tap('Entendido')
    back()
    for tab in ['Selección', 'Clasificación', 'Catálogo', 'Usuario']:
        tap(tab)
        snapshot('dashboard-' + tab)
    tap('Catálogo')
    tap('Objetos')
    tap('Minimizar filtros')  # Leave results visible even on the small 320dp emulator.
    snapshot('catalog-spanish')
    expectations = json.loads(Path('app/src/test/resources/item-corrections-158.json').read_text())
    items_source = Path('app/src/main/java/com/example/data/WildRiftItemsData.kt').read_text()
    for item_id in ['mercurial_scimitar', 'fiendhunter_bolts', 'kraken_slayer', 'nashor_s_tooth',
                    'imperial_mandate', 'terminus', 'yordle_trap', 'rabadon_s_deathcap']:
        tap('Buscar objeto por nombre o estadísticas...')
        enter_search_text(item_id)  # Dismiss the keyboard, retaining the search result.
        name = re.search(r'id = "' + re.escape(item_id) + r'",\s*name = "([^"]+)"', items_source).group(1)
        tap(name, scrolling=3)
        snapshot('required-item-' + item_id)
        actual = [n.get('text', '') for n in app_nodes(window())]
        expected = expectations[item_id]
        if 'goldCost' in expected and not any(str(expected['goldCost']) in s for s in actual):
            raise AssertionError('Updated item price missing in installed APK: ' + item_id)
        for stat in expected.get('stats', '').split(' • '):
            if stat and stat not in actual:
                raise AssertionError('Updated item stat missing in installed APK: ' + item_id + ': ' + stat)
        scroll()
        snapshot('required-item-' + item_id + '-passive')
        back()
        tap('Cerrar')  # Clear only the catalog search field after closing the dialog.
    for item_id in ['tear_of_the_goddess', 'quicksilver_sash_mid_tier']:
        expected = component_by_id[item_id]
        tap('Buscar objeto por nombre o estadísticas...')
        enter_search_text(item_id)
        tap(expected['nombre'], scrolling=3)
        snapshot('component-item-' + item_id)
        actual = [n.get('text', '') for n in app_nodes(window())]
        for stat in expected['estadisticas']:
            if stat not in actual:
                raise AssertionError('Component Spanish stat missing: ' + item_id + ': ' + stat)
        scroll()
        snapshot('component-item-' + item_id + '-passive')
        back()
        tap('Cerrar')
    adb('shell', 'am', 'force-stop', APP)
    adb('shell', 'am', 'start', '-W', '-n', APP + '/com.example.MainActivity')
    tap('Inicio')
    tap('Información')
    snapshot('restart-retains-spanish')
    (OUT / 'summary.json').write_text(json.dumps({'language': 'es-419', 'screens': screens,
        'findings': findings, 'authored_texts': authored_texts}, ensure_ascii=False, indent=2))
    if findings:
        for finding in findings:
            print('SPANISH_DEVICE_RESIDUE:', finding, flush=True)
        raise AssertionError('Portuguese text remains in Spanish screens of installed APK')
    print('SPANISH_DEVICE_AUDIT:', len(screens), 'screens, zero Portuguese findings', flush=True)
