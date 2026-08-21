from __future__ import annotations

import time
from collections import defaultdict, deque
from threading import Lock

from fastapi import HTTPException, Request, status
from .config import Settings


class RateLimiter:
    def __init__(self, requests_per_minute: int) -> None:
        self._limit = requests_per_minute
        self._events: dict[str, deque[float]] = defaultdict(deque)
        self._lock = Lock()

    def check(self, key: str) -> None:
        now = time.monotonic()
        with self._lock:
            events = self._events[key]
            while events and now - events[0] >= 60:
                events.popleft()
            if len(events) >= self._limit:
                raise HTTPException(
                    status_code=status.HTTP_429_TOO_MANY_REQUESTS,
                    detail="Request rate limit exceeded.",
                )
            events.append(now)


def build_rate_limiter(settings: Settings):
    limiter = RateLimiter(settings.requests_per_minute)

    async def limit(
        request: Request,
    ) -> None:
        client_host = request.client.host if request.client else "unknown"
        limiter.check(client_host)

    return limit
