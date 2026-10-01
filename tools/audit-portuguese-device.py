"""Navigate the installed, signed-out APK and retain actual Android UI evidence."""
import json
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

OUT = Path("app/build/reports/portuguese-device")
OUT.mkdir(parents=True, exist_ok=True)
APP = "com.Coach"
source = Path("app/src/test/java/com/example/PortugueseRenderedAuditTest.kt").read_text()
literal = re.search(r'val pattern = Regex\(\s*("(?:[^"\\]|\\.)*")', source).group(1)
pattern = json.loads(literal).replace(r"\p{L}\p{N}_", r"\w")
SPANISH = re.compile(pattern, re.IGNORECASE)
findings = []
screens = []


def adb(*args, binary=False):
    return subprocess.check_output(["adb", *args], text=not binary, timeout=40)


def window():
    adb("shell", "uiautomator", "dump", "/sdcard/coach-window.xml")
    return adb("exec-out", "cat", "/sdcard/coach-window.xml")


def app_nodes(xml):
    return [n for n in ET.fromstring(xml).iter("node") if n.get("package") == APP]


def snapshot(name):
    xml = window()
    (OUT / (name + ".xml")).write_text(xml)
    (OUT / (name + ".png")).write_bytes(adb("exec-out", "screencap", "-p", binary=True))
    strings = list(dict.fromkeys(s for n in app_nodes(xml) for s in [n.get("text", ""), n.get("content-desc", "")] if s))
    if not strings:
        raise AssertionError("No application texts rendered: " + name)
    (OUT / (name + ".json")).write_text(json.dumps(strings, ensure_ascii=False, indent=2))
    findings.extend({"screen": name, "text": s} for s in strings if len(s) > 2 and SPANISH.search(s.replace("Lee Sin", "LeeSin")))
    screens.append(name)
    print("PORTUGUESE_DEVICE_SCREEN:", name, len(strings), "texts", flush=True)


size = re.search(r"(\d+)x(\d+)", adb("shell", "wm", "size"))
WIDTH, HEIGHT = map(int, size.groups())


def scroll():
    adb("shell", "input", "swipe", str(WIDTH // 2), str(HEIGHT * 3 // 4), str(WIDTH // 2), str(HEIGHT // 3), "450")
    time.sleep(0.6)


def tap(label, scrolling=0):
    for attempt in range(max(10, scrolling + 1)):
        nodes = app_nodes(window())
        for node in nodes:
            if label in [node.get("text"), node.get("content-desc")]:
                points = list(map(int, re.findall(r"\d+", node.get("bounds", ""))))
                if len(points) == 4 and points[2] > points[0] and points[3] > points[1]:
                    adb("shell", "input", "tap", str((points[0] + points[2]) // 2), str((points[1] + points[3]) // 2))
                    time.sleep(0.8)
                    return
        if attempt < scrolling:
            scroll()
        else:
            time.sleep(1)
    snapshot("missing-" + str(len(screens)))
    raise AssertionError("Control not found: " + label)


def back():
    adb("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(0.8)


adb("install", "-r", "app/build/outputs/apk/debug/app-debug.apk")
adb("shell", "pm", "clear", APP)
adb("shell", "pm", "grant", APP, "android.permission.POST_NOTIFICATIONS")
adb("shell", "am", "start", "-n", APP + "/com.example.MainActivity")
tap("Português")
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
back()
back()
for tab in ["Seleção", "Tier List", "Catálogo", "Usuário"]:
    tap(tab)
    snapshot("dashboard-" + tab)
    if tab == "Catálogo":
        for catalog in ["Itens", "Runas", "Feitiços"]:
            tap(catalog)
            snapshot("catalog-" + catalog)
adb("shell", "am", "force-stop", APP)
adb("shell", "am", "start", "-n", APP + "/com.example.MainActivity")
tap("Informação")
snapshot("restart-retains-portuguese")
(OUT / "summary.json").write_text(json.dumps({"screens": screens, "findings": findings}, ensure_ascii=False, indent=2))
if findings:
    for finding in findings:
        print("PORTUGUESE_DEVICE_RESIDUE:", finding, flush=True)
    raise AssertionError("Spanish text remains in installed APK")
print("PORTUGUESE_DEVICE_AUDIT:", len(screens), "screens, zero Spanish findings", flush=True)
