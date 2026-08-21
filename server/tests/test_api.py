import httpx
import pytest
from fastapi.testclient import TestClient

from app.agora_client import AgoraClient, AgoraUpstreamError
from app.config import Settings
from app.main import create_app


class FakeAgoraClient:
    def __init__(self) -> None:
        self.join_calls = 0

    def create_user_tokens(self, channel_name: str, rtc_uid: int):
        return f"rtc-{channel_name}-{rtc_uid}", f"rtm-{rtc_uid}", 2_000_000_000

    async def join_agent(self, **kwargs):
        self.join_calls += 1
        return {"agent_id": "agent-1", "create_ts": 1_700_000_000, "status": "started"}

    async def interrupt_agent(self, agent_id: str, channel_name: str):
        return None

    async def leave_agent(self, agent_id: str, channel_name: str):
        return None

    async def close(self):
        return None


def settings() -> Settings:
    return Settings(
        agora_app_id="app-id",
        agora_app_certificate="certificate",
    )


class FailingStopSession:
    async def stop(self):
        raise RuntimeError("temporary stop failure")


@pytest.mark.asyncio
async def test_sdk_agent_configuration_creates_an_async_session():
    sdk_settings = Settings(
        agora_app_id="0" * 32,
        agora_app_certificate="1" * 32,
    )
    async with httpx.AsyncClient() as http_client:
        agora = AgoraClient(sdk_settings, http_client=http_client)
        session = agora._build_agent().create_async_session(
            channel="room-a",
            agent_uid="123456",
            remote_uids=["42"],
        )
        assert session.status == "idle"


@pytest.mark.asyncio
async def test_sdk_client_rejects_an_unknown_area():
    sdk_settings = Settings(
        agora_app_id="0" * 32,
        agora_app_certificate="1" * 32,
        agora_area="somewhere",
    )
    async with httpx.AsyncClient() as http_client:
        with pytest.raises(ValueError, match="Unsupported AGORA_AREA"):
            AgoraClient(sdk_settings, http_client=http_client)


@pytest.mark.asyncio
async def test_failed_stop_keeps_session_available_for_retry():
    agora = object.__new__(AgoraClient)
    agora._sessions = {"agent-1": ("room-a", FailingStopSession())}

    with pytest.raises(AgoraUpstreamError, match="temporary stop failure"):
        await agora.leave_agent("agent-1", "room-a")

    assert "agent-1" in agora._sessions


def test_health_and_bootstrap_are_available_without_client_auth():
    with TestClient(create_app(settings(), FakeAgoraClient())) as client:
        assert client.get("/health").status_code == 200
        assert client.post("/v1/conversation/bootstrap", json={}).status_code == 200


def test_full_conversation_contract_and_idempotent_join():
    fake = FakeAgoraClient()
    with TestClient(create_app(settings(), fake)) as client:
        bootstrap = client.post(
            "/v1/conversation/bootstrap",
            json={"requester_rtc_uid": 42, "requester_rtm_user_id": "42"},
        )
        assert bootstrap.status_code == 200
        session = bootstrap.json()
        assert session["app_id"] == "app-id"
        assert session["rtc_token"].startswith("rtc-")

        join_body = {"channel_name": session["channel_name"], "requester_rtc_uid": 42}
        first_join = client.post("/v1/conversation/join", json=join_body)
        second_join = client.post("/v1/conversation/join", json=join_body)
        assert first_join.json()["agent_id"] == "agent-1"
        assert second_join.json()["agent_id"] == "agent-1"
        assert fake.join_calls == 1

        refresh = client.post(
            "/v1/conversation/refresh",
            json={
                "channel_name": session["channel_name"],
                "requester_rtc_uid": 42,
                "requester_rtm_user_id": "42",
            },
        )
        assert refresh.status_code == 200
        assert refresh.json()["rtm_token"] == "rtm-42"

        action = {"channel_name": session["channel_name"], "agent_id": "agent-1"}
        assert client.post("/v1/conversation/interrupt", json=action).status_code == 200
        assert client.post("/v1/conversation/leave", json=action).status_code == 200
        assert client.post("/v1/conversation/refresh", json={
            "channel_name": session["channel_name"],
            "requester_rtc_uid": 42,
            "requester_rtm_user_id": "42",
        }).status_code == 404
