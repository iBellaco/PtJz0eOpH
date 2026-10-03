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
    snapshot("draft-before-field")
    tap("Selecionar Campeão")
    snapshot("draft-picker-open")
    # The first available champion is sufficient to exercise assigning a field.
    nodes = app_nodes(window())
    names = [n.get("text") for n in nodes if n.get("text") in ("Garen", "Ahri", "Aatrox", "Darius", "Lux", "Yasuo")]
    if not names:
        raise AssertionError("No champion visible in draft picker")
    tap(names[0])
    snapshot("draft-after-selection")
finally:
    out = Path("app/build/reports/portuguese-device")
    out.mkdir(parents=True, exist_ok=True)
    logs = subprocess.run(["adb", "logcat", "-d", "-b", "crash"], capture_output=True, text=True, timeout=40)
    (out / "draft-crash-logcat.txt").write_text(logs.stdout)
    print(logs.stdout[-12000:], flush=True)
