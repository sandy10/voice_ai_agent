# Production Migration Plan: Android Quickstart → Backend-Orchestrated Conversational AI

## Status
The baseline migration is implemented. This document remains as a reference for the architecture and outstanding production hardening. The quickstart intentionally has no application-user authentication; products must add their own authentication before public deployment.

## 1) Target architecture
1. The Android app never builds or uses `AGORA_APP_CERTIFICATE`.
2. The new Python server owns:
1. Agora App ID/Certificate validation and token generation.
2. Conversational AI orchestration through the `agora-agents` Python SDK (`start`, `interrupt`, `stop`).
3. Secure session state (`channel`, `requester_rtc_uid`, `agent_id`, token expiry).
1. The Android app communicates only with the Python server for bootstrap, join, interrupt, leave, and token refresh.
1. The server is exposed at a public HTTPS URL on a fixed port and configured in Android `local.properties` after start.

## 2) Repo structure to add
1. Add backend source at `server/` (or `backend/`, choose one and stay consistent).
1. Include:
1. `server/pyproject.toml` or `server/requirements.txt`.
1. `server/app/main.py` (FastAPI entrypoint).
1. `server/app/routes.py` (API routes).
1. `server/app/schemas.py` (Pydantic request/response types).
1. `server/app/agora_client.py` (server-side token generation and Agora SDK sessions).
1. `server/app/session_store.py` (in-memory first, Redis optional later).
1. `server/app/security.py` (development rate limiting).
1. `server/app/config.py` (env parsing, defaults).
1. `server/run.sh` plus `server/README.md`.

## 3) Use Agora CLI + Agora skills to bring in the Python server
1. Check available skills first (if needed in your environment).
1. Use the skill to scaffold or install a Python server template into the repo.
1. If CLI generation is unavailable, create a minimal FastAPI skeleton manually using the same file set above.
1. Keep app binding and credentials flow through environment variables only.

## 4) Backend contract (public API)
1. `POST /v1/conversation/bootstrap`
1. Request:
1. JSON with optional `requester_rtc_uid` and `requester_rtm_user_id`.
1. Response:
1. `channel_name`, `rtc_token`, `rtm_token`, `requester_rtc_uid`, `requester_rtm_user_id`.
1. `POST /v1/conversation/join`
1. Request:
1. `channel_name`, `requester_rtc_uid`, `agent_profile` (optional), `system_prompt` (optional).
1. Response:
1. `agent_id`, `created_at_unix`, `status`.
1. `POST /v1/conversation/interrupt`
1. Request:
1. `agent_id`, `channel_name`.
1. Response:
1. success boolean and message.
1. `POST /v1/conversation/leave`
1. Request:
1. `agent_id`, `channel_name`.
1. Response:
1. success boolean and message.
1. `POST /v1/conversation/refresh`
1. Request:
1. `channel_name`, `requester_rtc_uid`, `requester_rtm_user_id`.
1. Response:
1. new `rtc_token`, `rtm_token`.
1. `GET /health`
1. Health and build metadata endpoint for smoke checks.

## 5) Request/response security model
1. The quickstart client sends no custom bearer token. Product authentication is a separate production concern and must not be confused with Agora RTC/RTM or REST credentials.
1. Validate request origin and required fields.
1. Enforce per-request rate limiting and basic replay protection where practical.
1. Return explicit, structured errors for:
1. bad request (`400`), rate limit (`429`), upstream timeout (`504`), and Agora failures (`502`).

## 6) Backend implementation steps
1. Create configuration module with:
1. `AGORA_APP_ID`, `AGORA_APP_CERTIFICATE`.
1. `ASR_MODEL`, `LLM_MODEL`, `TTS_MODEL`, `TTS_VOICE_ID`.
1. `ALLOWED_ORIGINS` for CORS.
1. `PORT` and `HOST` for the loopback HTTP listener.
1. Implement token service:
1. generate RTC token for requester uid/channel.
1. generate RTM token for string uid.
1. let `agora-agents` generate agent REST and RTC credentials from the server-side App ID and Certificate.
1. Build/return bootstrap and refresh payloads.
1. Implement Agora SDK client with `AsyncAgora`, `Agent`, and async agent sessions.
1. Session store:
1. map `channel_name` -> `{agent_id, requester_rtc_uid, requester_rtm_user_id, created_at, agent_state, token_exp`.
1. cleanup old sessions on TTL.
1. Handle duplicate/join races idempotently.
1. Add logging:
1. request IDs.
1. agent id, channel, timings.
1. anonymized error details only.

## 7) HTTPS and public URL requirement
1. Backend runs loopback HTTP; the selected tunnel provider terminates public HTTPS.
1. Start command example:  
`uvicorn app.main:app --host 127.0.0.1 --port 8000`
1. Document local public exposure options:
1. reverse tunnel: `ngrok http http://127.0.0.1:8000` with a public HTTPS URL.
1. cloud tunnel: Cloudflared or equivalent.
1. deploy behind HTTPS load balancer.
1. Ensure Android points to:
1. `BACKEND_BASE_URL=https://<public-host>`.
1. `local.properties` key to add after server is started:
1. `QUICKSTART_SERVER_URL=https://...`.
1. Confirm CORS allows your Android debug/prod origins.

## 8) Android migration tasks
1. Remove local token creation from app runtime:
1. stop using `AgoraLocalTokenFactory` in production path.
1. stop requiring `AGORA_APP_CERTIFICATE` in config checks.
1. Add server config in `app/build.gradle.kts`:
1. read `QUICKSTART_SERVER_URL`.
1. Add the backend base URL to `QuickstartConfig`.
1. Add backend client API layer:
1. replace Agora REST calls in `ConversationAgoraApi` with backend calls.
1. Keep `ConversationRepository` API unchanged where possible for minimal surface change.
1. Update `startConversation()` in `ConversationViewModel`:
1. call `POST /bootstrap`.
1. use returned tokens for RTC/RTM join.
1. call `POST /join` after channel join.
1. Update `endConversation()`:
1. call `POST /leave` (and fallback local cleanup if call fails).
1. Update interrupt flow in `AgoraConversationSessionManager`:
1. on user interrupt event call `POST /interrupt`.
1. keep debounce/pacing behavior unchanged.
1. Replace token refresh logic:
1. on token expiry callback, call `POST /refresh`.
1. update RTC and RTM tokens from backend response.
1. Keep existing token renew error surfacing in session issues.
1. Remove/retire local token unit tests and add backend integration contract tests.

## 9) UI and diagnostics updates
1. Update startup/config diagnostics to show:
1. missing `QUICKSTART_SERVER_URL`.
1. server health check status.
1. Update warning/error copy from:
1. certificate/token-generation language
1. to:
1. server connectivity and auth messages.
1. Add debug panel row for backend latency and last server response.

## 10) Deployment and run flow
1. Start Android project setup:
1. set `AGORA_APP_ID` in server environment only, not in Android secrets.
1. keep Android config to the public server URL.
1. Start the Python server on loopback HTTP.
1. expose public URL via tunnel or deployment host.
1. write the URL to Android `local.properties`:
1. `QUICKSTART_SERVER_URL=...`
1. run Android app and execute start/interrupt/stop scenario.

## 11) Testing tasks
1. Backend unit tests:
1. bootstrap generates valid non-empty tokens (mocked token builder).
1. join/interrupt/leave call flow with mocked Agora responses.
1. rate-limited requests return `429`.
1. token refresh rotates tokens and updates session.
1. expiry cleanup purges stale sessions.
1. Android unit/integration tests:
1. replace token tests with backend contract tests.
1. verify state machine transitions still work.
1. verify fallback behavior when `/join` fails after RTC connect.
1. verify `/refresh` failure shows session issue and preserves user control.
1. manual smoke checklist:
1. start conversation successfully.
1. spoken interruption triggers backend interrupt.
1. end conversation triggers backend leave.
1. token expiry path renews tokens without crash.

## 12) Files to update
1. Android:
1. `app/build.gradle.kts`
1. `app/src/main/java/com/androidengineers/agent_quickstart_android/config/QuickstartConfig.kt`
1. `app/src/main/java/com/androidengineers/agent_quickstart_android/data/ConversationAgoraApi.kt`
1. `app/src/main/java/com/androidengineers/agent_quickstart_android/data/ConversationRepository.kt`
1. `app/src/main/java/com/androidengineers/agent_quickstart_android/rtc/AgoraConversationSessionManager.kt`
1. `app/src/main/java/com/androidengineers/agent_quickstart_android/ui/ConversationViewModel.kt`
1. `app/src/main/java/com/androidengineers/agent_quickstart_android/data/AgoraLocalTokenFactory.kt` (demote to optional/demo-only or remove from production path)
1. Docs:
1. `README.md`
1. `docs/setup.md`
1. `docs/architecture.md`
1. new doc for backend runbook.
1. Add:
1. `docs/production-migration-plan-agora-cli-python-backend.md` (this file).
1. New backend code as listed in section 2.

## 13) Rollout order (recommended)
1. Implement backend scaffold and endpoint contract.
1. Add loopback HTTP and public HTTPS tunnel exposure.
1. Update Android config + API transport.
1. Replace start/join/interrupt/leave flow.
1. Add token refresh path.
1. Add integration tests.
1. Hardening pass, docs updates, production smoke test.

## 14) Definition of done
1. Android app starts only with `AGORA_APP_ID` on the server side and `QUICKSTART_SERVER_URL` on the client.
1. No certificate generation exists in Android runtime.
1. `join/interrupt/leave/refresh` all go through backend and succeed in staging with public HTTPS URL.
1. Token expiry renewals handled via backend endpoint and logged.
1. Setup docs allow another developer to run the full flow end-to-end in under one hour.
