#!/usr/bin/env bash
# Only known infrastructure permission restrictions may use the existing private worker.
# Compilation, authentication and unknown deployment failures must still fail the workflow.
set -euo pipefail
log_path=${1:?deployment log required}
if grep -Fq 'HTTP Error: 403, Permission denied to get service' "$log_path"; then
  exit 0
fi
if grep -Fq 'Cloud Functions deployment requires the Cloud Build API to be enabled.' "$log_path" &&
   grep -Fq 'current credentials do not have permission to enable APIs' "$log_path"; then
  exit 0
fi
exit 1
