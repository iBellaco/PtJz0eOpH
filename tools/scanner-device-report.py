"""Collect non-identifying device metadata and evaluate physical scanner evidence."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import math
from pathlib import Path
import subprocess


def evaluate(report, base):
    observations = report.get('observations', [])
    if not observations:
        return {'status': 'PENDING', 'reason': 'Faltan observaciones físicas'}
    errors, latencies = [], []
    device = report.get('device', {})
    if device.get('emulator') is not False:
        errors.append('No se ha confirmado un dispositivo físico')
    for field in ('model', 'size', 'density', 'android'):
        if not device.get(field):
            errors.append('Falta metadato: ' + field)
    if not report.get('version') or not report.get('calibrationProfile'):
        errors.append('Falta versión o perfil de calibración')
    seen_evidence, pick_slots, picks = set(), [], []
    for observation in observations:
        expected, detected = observation.get('expected'), observation.get('detected')
        if not isinstance(expected, list) or not expected or not all(isinstance(x, str) and x.strip() for x in expected):
            errors.append('Referencia de campeones vacía o inválida')
        elif expected != detected:
            errors.append('Selección incorrecta: ' + str(observation.get('kind')))
        if observation.get('kind') == 'pick':
            pick_slots.append(observation.get('slot'))
            if isinstance(expected, list) and len(expected) == 1:
                picks.extend(expected)
            else:
                errors.append('Cada selección debe identificar un campeón')
        elif observation.get('kind') == 'bans':
            if not isinstance(expected, list) or len(expected) != 10:
                errors.append('Cada captura de baneos debe identificar diez slots')
        else:
            errors.append('Tipo de observación desconocido')
        latency = observation.get('latencyMs')
        if isinstance(latency, bool) or not isinstance(latency, (int, float)) or not math.isfinite(latency) or latency < 0:
            errors.append('Latencia inválida')
        else:
            latencies.append(latency)
        evidence = observation.get('evidence', '')
        instant = observation.get('atMs', 0)
        if isinstance(instant, bool) or not isinstance(instant, int) or instant < 0:
            errors.append('Instante de evidencia inválido')
            instant = -1
        identity = (str(evidence), instant)
        if identity in seen_evidence:
            errors.append('Evidencia duplicada sin un instante diferente')
        seen_evidence.add(identity)
        path = (base / evidence).resolve()
        if not evidence or not path.is_relative_to(base.resolve()) or not path.is_file():
            errors.append('Falta evidencia local verificable')
        elif hashlib.sha256(path.read_bytes()).hexdigest() != observation.get('sha256'):
            errors.append('La evidencia no coincide con su huella')
    kinds = [entry.get('kind') for entry in observations]
    if kinds.count('bans') < 3 or kinds.count('pick') < 10:
        errors.append('Se requieren tres capturas de baneos y diez selecciones')
    if len(pick_slots) != 10 or any(isinstance(slot, bool) or not isinstance(slot, int) for slot in pick_slots) or set(pick_slots) != set(range(1, 11)):
        errors.append('Faltan slots únicos del uno al diez')
    if len(picks) != 10 or len(set(picks)) != 10:
        errors.append('Las diez selecciones deben corresponder a campeones distintos')
    for check in ('orientation', 'profilePersistence', 'finalBuildRedirect'):
        if report.get('checks', {}).get(check) is not True:
            errors.append('Comprobación pendiente: ' + check)
    ordered = sorted(latencies)
    p95 = ordered[max(0, math.ceil(len(ordered) * .95) - 1)] if ordered else None
    if p95 is not None and p95 > 500:
        errors.append('Demora p95 superior a 500 ms')
    return {'status': 'FAIL' if errors else 'PASS', 'observations': len(observations), 'p95Ms': p95, 'errors': errors}


def collect(serial, version, profile):
    def adb(*args):
        return subprocess.check_output(['adb', '-s', serial, *args], text=True, timeout=30).strip()
    return {
        'schema': 1, 'recordedAt': datetime.now(timezone.utc).isoformat(), 'version': version,
        'calibrationProfile': profile,
        'device': {'model': adb('shell', 'getprop', 'ro.product.model'),
                   'android': adb('shell', 'getprop', 'ro.build.version.release'),
                   'size': adb('shell', 'wm', 'size'), 'density': adb('shell', 'wm', 'density'),
                   'emulator': adb('shell', 'getprop', 'ro.kernel.qemu') == '1'},
        'checks': {'orientation': None, 'profilePersistence': None, 'finalBuildRedirect': None},
        'observations': [], 'result': {'status': 'PENDING'},
    }


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest='command', required=True)
    device = sub.add_parser('collect')
    device.add_argument('--serial', required=True)
    device.add_argument('--version', required=True)
    device.add_argument('--profile', required=True)
    device.add_argument('--output', type=Path, required=True)
    check = sub.add_parser('evaluate')
    check.add_argument('report', type=Path)
    args = parser.parse_args()
    if args.command == 'collect':
        report = collect(args.serial, args.version, args.profile)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        # Refuse to overwrite existing physical evidence.
        with args.output.open('x') as stream:
            json.dump(report, stream, ensure_ascii=False, indent=2)
        print('SCANNER_DEVICE: PENDING; metadatos guardados, faltan mediciones reales')
        return 0
    report = json.loads(args.report.read_text())
    result = evaluate(report, args.report.parent)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if result['status'] == 'PASS' else 1


if __name__ == '__main__':
    raise SystemExit(main())
