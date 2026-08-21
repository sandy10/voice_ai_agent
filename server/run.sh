#!/usr/bin/env bash
set -euo pipefail

SERVER_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SERVER_DIR"

set -a
if [[ -f .env ]]; then source .env; fi
if [[ -f .env.local ]]; then source .env.local; fi
set +a

HOST="${HOST:-127.0.0.1}"
PORT="${PORT:-8000}"

exec python -m uvicorn app.main:app \
  --host "$HOST" \
  --port "$PORT"
