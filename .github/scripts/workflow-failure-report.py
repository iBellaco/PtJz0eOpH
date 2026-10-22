#!/usr/bin/env python3
"""Write the failed jobs and steps for this GitHub Actions run to the summary."""

import json
import os
import subprocess
import sys
import re
from pathlib import Path


def diagnose(log: str) -> str:
    """Return fixed guidance, never log excerpts (logs can contain private identifiers)."""
    patterns = [
        (r'Unresolved reference|Compilation error|compile.*Kotlin.*FAILED', 'Compilación Kotlin: revisar símbolos y tipos en el paso de compilación.'),
        (r'AssertionError|ComparisonFailure|tests? failed|FAILED.*Test', 'Regresión: abrir el informe de pruebas del job; revisar el caso fallido antes de publicar.'),
        (r'SIGNING|keystore|signingConfig|certificate digest', 'Firma: revisar la configuración productiva y la huella esperada; no usar claves de depuración.'),
        (r'PERMISSION_DENIED|403|insufficient.*permission', 'Permisos: revisar el acceso de la conexión usada por este job, sin ampliar privilegios indiscriminadamente.'),
        (r'401|UNAUTHENTICATED|invalid_grant', 'Autenticación: revisar vigencia y configuración de la conexión de servicio.'),
        (r'OutOfMemoryError|Java heap space|No space left', 'Recursos insuficientes: revisar memoria y espacio del ejecutor.'),
        (r'timed? out|TimeoutException|ETIMEDOUT|ECONNRESET|UnknownHost|UNAVAILABLE', 'Conectividad o tiempo agotado: comprobar disponibilidad y límites de reintento.'),
    ]
    guidance = next((message for pattern, message in patterns if re.search(pattern, log, re.I)),
                'No hay una causa segura identificable: abrir el enlace del job y consultar su paso fallido. No repetir una publicación sin revisar el error.')
    cases = list(dict.fromkeys(re.findall(r'REGRESSION_FAILURE\s+(com[.]example[.][A-Za-z0-9_]+[.][A-Za-z0-9_]+)', log)))[:3]
    return guidance + (' Casos: ' + ', '.join(cases) + '.' if cases else '')


def job_diagnosis(repository: str, job: dict) -> str:
    if job.get('conclusion') == 'cancelled':
        return 'Ejecución cancelada; las validaciones no se consideran aprobadas.'
    if job.get('conclusion') == 'timed_out':
        return 'El job superó el tiempo permitido; revisar el paso que seguía ejecutándose.'
    try:
        response = subprocess.run(['gh', 'api', f"repos/{repository}/actions/jobs/{int(job['id'])}/logs"],
                                  capture_output=True, text=True, timeout=30, check=True)
        return diagnose(response.stdout)
    except (subprocess.SubprocessError, ValueError, KeyError):
        return diagnose('')


def main() -> int:
    repository = os.environ["GITHUB_REPOSITORY"]
    run_id = os.environ["GITHUB_RUN_ID"]
    response = subprocess.run(
        ["gh", "api", f"repos/{repository}/actions/runs/{run_id}/jobs?per_page=100"],
        check=True,
        capture_output=True,
        text=True,
    )
    jobs = json.loads(response.stdout).get("jobs", [])
    failed = []
    for job in jobs:
        conclusion = job.get("conclusion")
        if conclusion not in {"failure", "cancelled", "timed_out", "action_required"}:
            continue
        steps = [
            step
            for step in job.get("steps", [])
            if step.get("conclusion") in {"failure", "cancelled", "timed_out", "action_required"}
        ]
        failed.append((job.get("name", "unknown job"), conclusion, steps, job))

    evidence = {
        'schema': 1, 'runId': run_id, 'sourceSha': os.environ.get('GITHUB_SHA', ''),
        'scope': 'prerequisite jobs observed before final report; not physical device certification',
        'status': 'FAIL' if failed else 'PASS',
        'jobs': [{'name': j.get('name'), 'conclusion': j.get('conclusion'),
                  'url': j.get('html_url')} for j in jobs if j.get('conclusion') is not None],
        'failureGuidance': [{'job': entry[0], 'guidance': job_diagnosis(repository, entry[3])} for entry in failed],
    }
    version_text = Path('app/build.gradle.kts').read_text(encoding='utf-8')
    evidence['versionName'] = re.search(r'versionName\s*=\s*"([^"]+)"', version_text).group(1)
    evidence['versionCode'] = int(re.search(r'versionCode\s*=\s*(\d+)', version_text).group(1))
    report = Path('app/build/reports/delivery/workflow-result.json')
    report.parent.mkdir(parents=True, exist_ok=True)
    report.write_text(json.dumps(evidence, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

    summary_path = os.environ["GITHUB_STEP_SUMMARY"]
    with open(summary_path, "a", encoding="utf-8") as summary:
        summary.write("\n## Resultado del workflow\n\n")
        if not failed:
            summary.write("Las comprobaciones previas finalizaron sin fallos. Los jobs omitidos no se cuentan como pruebas ejecutadas.\n")
            summary.write(f"\nEvidencia: run `{run_id}`, commit `{evidence['sourceSha']}`; artefacto `delivery-evidence`.\n")
            return 0
        summary.write("❌ El workflow no se publicó porque fallaron estas comprobaciones:\n\n")
        for index, (job_name, conclusion, steps, job) in enumerate(failed):
            summary.write(f"- **{job_name}** — `{conclusion}`\n")
            for step in steps:
                summary.write(f"  - Paso: `{step.get('name', 'unknown step')}` — `{step.get('conclusion')}`\n")
            summary.write(f"  - Diagnóstico: {evidence['failureGuidance'][index]['guidance']}\n")
            summary.write(f"  - [Abrir job]({job.get('html_url', '')})\n")
        summary.write("\nEl diagnóstico clasifica la causa sin publicar fragmentos privados. El error exacto permanece en el paso enlazado.\n")

    return 1


if __name__ == "__main__":
    sys.exit(main())
