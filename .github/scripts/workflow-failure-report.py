#!/usr/bin/env python3
"""Write the failed jobs and steps for this GitHub Actions run to the summary."""

import json
import os
import subprocess
import sys


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
        failed.append((job.get("name", "unknown job"), conclusion, steps))

    summary_path = os.environ["GITHUB_STEP_SUMMARY"]
    with open(summary_path, "a", encoding="utf-8") as summary:
        summary.write("\n## Resultado del workflow\n\n")
        if not failed:
            summary.write("✅ Todas las validaciones finalizaron correctamente.\n")
            return 0
        summary.write("❌ El workflow no se publicó porque fallaron estas comprobaciones:\n\n")
        for job_name, conclusion, steps in failed:
            summary.write(f"- **{job_name}** — `{conclusion}`\n")
            for step in steps:
                summary.write(f"  - Paso: `{step.get('name', 'unknown step')}` — `{step.get('conclusion')}`\n")
        summary.write("\nRevisa los logs del paso indicado para conocer el error exacto.\n")

    return 1


if __name__ == "__main__":
    sys.exit(main())
