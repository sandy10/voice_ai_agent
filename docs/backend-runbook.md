# Python backend runbook

## Architecture

The Android app calls this FastAPI service for session bootstrap, token refresh, and agent lifecycle operations. Only the Python process has `AGORA_APP_CERTIFICATE`. The bootstrap response includes the Agora App ID because the RTC and RTM SDKs need it to initialize; it is a public identifier, not a credential.

## Configure

```bash
python3 -m venv server/.venv
source server/.venv/bin/activate
pip install -r server/requirements-dev.txt
cp server/.env.example server/.env.local
agora project env write server/.env.local
```

Add any optional model settings to `server/.env.local`. `AGORA_APP_ID` and `AGORA_APP_CERTIFICATE` are the only required server credentials.

## Run locally and create a public HTTPS URL

```bash
./server/run.sh
```

`run.sh` listens on `http://127.0.0.1:8000`. It intentionally uses loopback HTTP because the tunnel process runs on the same machine and terminates public TLS.

In a second terminal:

```bash
./server/tunnel.sh --provider ngrok
```

Choose `cloudflare`, `ngrok`, `tailscale`, or `localtunnel` with `--provider`. Each publishes a trusted public HTTPS URL while forwarding to the loopback HTTP port. Configure Android after the URL appears:

```bash
./server/configure-android.sh https://generated-public-host
```

See [Local HTTPS tunnels](local-tunnels.md) for manual provider commands, public health verification, URL rotation, and troubleshooting. Rebuild or reinstall the app whenever the public URL changes.

For a stable production URL, deploy the same ASGI app behind a managed HTTPS load balancer instead of using a development tunnel.

## API

- `GET /health` is public and reports service version and active-session count.
- `POST /v1/conversation/bootstrap` creates a channel and short-lived RTC/RTM token.
- `POST /v1/conversation/join` starts an agent and is idempotent by channel.
- `POST /v1/conversation/interrupt` interrupts the active agent.
- `POST /v1/conversation/leave` stops the agent and removes server session state.
- `POST /v1/conversation/refresh` rotates the user token after validating the session identity.

The Android app does not send Agora credentials or a custom bearer token. The backend generates the user's RTC/RTM token, starts and controls the agent through `agora-agents`, and returns only the values required by the RTC and RTM SDKs. The in-memory session and rate-limit stores are suitable for one process. Use Redis or another shared store and add product-level user authentication before scaling or deploying publicly.

## Smoke check

```bash
curl http://127.0.0.1:8000/health
curl -X POST http://127.0.0.1:8000/v1/conversation/bootstrap \
  -H "Content-Type: application/json" \
  -d '{}'
```
