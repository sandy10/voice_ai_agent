from __future__ import annotations

import logging
import time
import uuid
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware

from .agora_client import AgoraClient
from .config import Settings
from .routes import create_router
from .session_store import SessionStore


logger = logging.getLogger("uvicorn.error")


def create_app(
    settings: Settings | None = None,
    agora_client: AgoraClient | None = None,
) -> FastAPI:
    resolved_settings = settings or Settings.from_env()
    store = SessionStore(resolved_settings.session_ttl_seconds)
    agora = agora_client or AgoraClient(resolved_settings)

    @asynccontextmanager
    async def lifespan(_: FastAPI):
        resolved_settings.validate()
        yield
        await agora.close()

    application = FastAPI(
        title="Agora Android Quickstart Server",
        version=resolved_settings.build_version,
        lifespan=lifespan,
    )
    application.state.settings = resolved_settings
    application.state.session_store = store
    application.add_middleware(
        CORSMiddleware,
        allow_origins=list(resolved_settings.allowed_origins),
        allow_credentials=resolved_settings.allowed_origins != ("*",),
        allow_methods=["GET", "POST"],
        allow_headers=["Content-Type", "X-Request-ID"],
    )

    @application.middleware("http")
    async def request_context(request: Request, call_next):
        request_id = request.headers.get("X-Request-ID") or str(uuid.uuid4())
        started = time.monotonic()
        response = await call_next(request)
        elapsed_ms = int((time.monotonic() - started) * 1000)
        response.headers["X-Request-ID"] = request_id
        response.headers["Server-Timing"] = f"app;dur={elapsed_ms}"
        logger.info(
            "request_id=%s method=%s path=%s status=%s duration_ms=%s",
            request_id,
            request.method,
            request.url.path,
            response.status_code,
            elapsed_ms,
        )
        return response

    application.include_router(create_router(resolved_settings, store, agora))
    return application


app = create_app()
