#!/usr/bin/env bash
set -euo pipefail

PORT="${PORT:-8000}"
PROVIDER=""

usage() {
  cat <<'EOF'
Usage: ./server/tunnel.sh --provider <provider> [--port <port>]

Providers:
  cloudflare   Cloudflare Quick Tunnel (requires cloudflared)
  ngrok        ngrok HTTP tunnel (requires ngrok)
  tailscale    Tailscale Funnel (requires tailscale and Funnel access)
  localtunnel  LocalTunnel (requires lt or npx)

Examples:
  ./server/tunnel.sh --provider ngrok
  ./server/tunnel.sh --provider cloudflare --port 8000
EOF
}

missing_value() {
  echo "Missing value for $1." >&2
  usage >&2
  exit 2
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Provider '$PROVIDER' requires '$1' on PATH." >&2
    exit 1
  fi
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -p|--provider)
      [[ $# -ge 2 ]] || missing_value "$1"
      PROVIDER="$2"
      shift 2
      ;;
    --provider=*)
      PROVIDER="${1#*=}"
      shift
      ;;
    --port)
      [[ $# -ge 2 ]] || missing_value "$1"
      PORT="$2"
      shift 2
      ;;
    --port=*)
      PORT="${1#*=}"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if [[ -z "$PROVIDER" ]]; then
  echo "Choose a tunnel provider with --provider." >&2
  usage >&2
  exit 2
fi

if [[ ! "$PORT" =~ ^[0-9]+$ ]] || (( PORT < 1 || PORT > 65535 )); then
  echo "Port must be an integer between 1 and 65535." >&2
  exit 2
fi

case "$PROVIDER" in
  cloudflare|cloudflared)
    PROVIDER="cloudflare"
    require_command cloudflared
    exec cloudflared tunnel --url "http://127.0.0.1:${PORT}"
    ;;
  ngrok)
    require_command ngrok
    exec ngrok http "http://127.0.0.1:${PORT}"
    ;;
  tailscale)
    require_command tailscale
    exec tailscale funnel "http://127.0.0.1:${PORT}"
    ;;
  localtunnel|lt)
    PROVIDER="localtunnel"
    if command -v lt >/dev/null 2>&1; then
      exec lt --port "$PORT"
    fi
    require_command npx
    exec npx localtunnel --port "$PORT"
    ;;
  *)
    echo "Unsupported tunnel provider: $PROVIDER" >&2
    usage >&2
    exit 2
    ;;
esac
