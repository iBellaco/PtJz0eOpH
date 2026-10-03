"""Exercise actual draft fields and the champion picker in the installed APK."""
from pathlib import Path
import subprocess

# Share the installed audit's language setup and hierarchy helpers, stopping at home.
source = Path("tools/audit-portuguese-device.py").read_text()
prefix = source.split('tap("Informação")')[0]
try:
    exec(compile(prefix, "tools/audit-portuguese-device.py", "exec"), globals())
    adb("logcat", "-c")
    tap("Seleção")
    tap("Todas as rotas")
    tap("Rota do Meio")
    snapshot("draft-before-field")

    def champion(name):
        for node in app_nodes(window()):
            if node.get("class") != "android.widget.EditText" and name in (node.get("text"), node.get("content-desc")):
                bounds = list(map(int, re.findall(r"\d+", node.get("bounds", ""))))
                adb("shell", "input", "tap", str((bounds[0]+bounds[2])//2), str((bounds[1]+bounds[3])//2))
                time.sleep(0.8)
                return
        raise AssertionError("Champion grid entry not visible: " + name)

    def field(team, role):
        title = "EQUIPE ALIADA" if team == "ally" else "EQUIPE RIVAL"
        for attempt in range(7):
            root = ET.fromstring(window())
            candidates = []
            for parent in root.iter("node"):
                children = list(parent.iter("node"))
                if any(n.get("text") == title for n in children):
                    matches = [n for n in children if n.get("content-desc") == role]
                    if matches:
                        candidates.append((len(children), matches[0]))
            if candidates:
                node = min(candidates, key=lambda value: value[0])[1]
                bounds = list(map(int, re.findall(r"\d+", node.get("bounds", ""))))
                adb("shell", "input", "tap", str((bounds[0]+bounds[2])//2), str((bounds[1]+bounds[3])//2))
                time.sleep(0.8)
                return
            if team == "ally":
                adb("shell", "input", "swipe", str(WIDTH//2), str(int(HEIGHT*.25)), str(WIDTH//2), str(int(HEIGHT*.8)), "400")
                time.sleep(0.6)
            else:
                scroll()
        raise AssertionError("Draft field not visible: " + team + "/" + role)

    teams = {
        "ally": ["Garen", "Xin Zhao", "Ahri", "Jhin", "Lulu"],
        "enemy": ["Darius", "Lee Sin", "Yasuo", "Caitlyn", "Nami"],
    }
    for team, names in teams.items():
        for role, name in zip(["TOP", "SELVA", "MID", "DUO", "SUPORTE"], names):
            field(team, role)
            snapshot("draft-picker-" + team + "-" + role)
            tap("Buscar campeão...")
            for letter in name:
                adb("shell", "input", "text", "%s" if letter == " " else letter)
                time.sleep(0.08)
            back()
            snapshot("draft-search-" + name.replace(" ", "-"))
            champion(name)
            snapshot("draft-assigned-" + team + "-" + role)
            if not adb("shell", "pidof", APP).strip():
                raise AssertionError("Application process died after selecting " + name)
    field("ally", "TOP")
    tap("Buscar campeão...")
    for letter in "Teemo":
        adb("shell", "input", "text", letter)
        time.sleep(0.08)
    back()
    champion("Teemo")
    snapshot("draft-replaced-top")

finally:
    out = Path("app/build/reports/portuguese-device")
    out.mkdir(parents=True, exist_ok=True)
    logs = subprocess.run(["adb", "logcat", "-d", "-b", "crash"], capture_output=True, text=True, timeout=40)
    (out / "draft-crash-logcat.txt").write_text(logs.stdout)
    print(logs.stdout[-12000:], flush=True)
