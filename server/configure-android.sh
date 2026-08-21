#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Usage: $0 https://public-server-url" >&2
  exit 1
fi

SERVER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROPERTIES_FILE="$SERVER_DIR/../local.properties"
PUBLIC_URL="${1%/}"

if [[ "$PUBLIC_URL" != https://* ]]; then
  echo "The Android app requires an https:// server URL." >&2
  exit 1
fi
touch "$PROPERTIES_FILE"
TEMP_FILE="$(mktemp)"
awk '!/^QUICKSTART_SERVER_URL=/ && !/^QUICKSTART_SERVER_TOKEN=/' "$PROPERTIES_FILE" > "$TEMP_FILE"
{
  cat "$TEMP_FILE"
  echo "QUICKSTART_SERVER_URL=$PUBLIC_URL"
} > "$PROPERTIES_FILE"
rm -f "$TEMP_FILE"

echo "Updated $PROPERTIES_FILE with the public server URL."
