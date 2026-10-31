#!/usr/bin/env bash
set -euo pipefail
project_id="${1:?Indica el proyecto de Coach autorizado para el despliegue}"
# Uses the configured deployment identity. Never starts interactive authentication.
firebase deploy --non-interactive --only firestore:rules --project "$project_id"
gcloud firestore fields ttls update streamerHistoryDeleteAt --collection-group=history --enable-ttl --project="$project_id" --quiet
gcloud firestore fields ttls update streamerHistoryDeleteAt --collection-group=streamer_click_metrics --enable-ttl --project="$project_id" --quiet

gcloud firestore fields ttls update deleteAt --collection-group=click_events --enable-ttl --project="$project_id" --quiet
