#!/usr/bin/env bash
# Usage: TARGET_DATABASE_URL=postgresql://... R2_ACCOUNT_ID=... R2_ACCESS_KEY_ID=... \
#        R2_SECRET_ACCESS_KEY=... R2_BUCKET=... ./scripts/restore-backup.sh [object-key]
# Without an object key, the most recent backup under db/ is restored.
# Restore into a fresh/empty database, never straight over production.
set -euo pipefail

: "${TARGET_DATABASE_URL:?}" "${R2_ACCOUNT_ID:?}" "${R2_ACCESS_KEY_ID:?}" "${R2_SECRET_ACCESS_KEY:?}" "${R2_BUCKET:?}"

export AWS_ACCESS_KEY_ID="$R2_ACCESS_KEY_ID"
export AWS_SECRET_ACCESS_KEY="$R2_SECRET_ACCESS_KEY"
export AWS_DEFAULT_REGION=auto
ENDPOINT="https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com"

KEY="${1:-}"
if [ -z "$KEY" ]; then
  KEY=$(aws s3api list-objects-v2 --bucket "$R2_BUCKET" --prefix db/ --endpoint-url "$ENDPOINT" \
    --query 'sort_by(Contents,&LastModified)[-1].Key' --output text)
fi
echo "Restoring $KEY"

aws s3 cp "s3://${R2_BUCKET}/${KEY}" restore.dump --endpoint-url "$ENDPOINT"
pg_restore --no-owner --no-privileges --clean --if-exists --dbname="$TARGET_DATABASE_URL" restore.dump
rm -f restore.dump
echo "Restore complete."
