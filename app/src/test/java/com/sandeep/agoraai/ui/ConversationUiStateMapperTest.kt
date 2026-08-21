package com.sandeep.agoraai.ui

import com.sandeep.agoraai.audio.TurnState
import com.sandeep.agoraai.model.AgentConversationState
import com.sandeep.agoraai.model.AgentVisualState
import com.sandeep.agoraai.model.ConversationUiState
import com.sandeep.agoraai.model.SessionSnapshot
import com.sandeep.agoraai.model.TranscriptSpeaker
import com.sandeep.agoraai.model.TranscriptTurn
import com.sandeep.agoraai.model.TranscriptTurnStatus
import io.agora.rtc2.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationUiStateMapperTest {
    @Test
    fun freshUiStateCarriesTheDefaultFlags() {
        val state = ConversationUiStateMapper.freshUiState(
            permissionGranted = true,
            warningMessage = "warning",
            errorMessage = "error",
        )

        assertTrue(state.microphonePermissionGranted)
        assertEquals("warning", state.warningMessage)
        assertEquals("error", state.errorMessage)
        assertEquals(AgentVisualState.WAITING, state.agentVisualState)
    }

    @Test
    fun mergeSessionMapsLiveTranscriptHistoryAndLabels() {
        val currentState = ConversationUiState(
            inConversation = true,
            microphonePermissionGranted = true,
        )
        val snapshot = SessionSnapshot(
            channelName = "demo-channel",
            rtcConnectionState = Constants.CONNECTION_STATE_CONNECTED,
            rtmConnectionState = "CONNECTED",
            localRtcUid = 42,
            isAgentRtcConnected = true,
            agentState = AgentConversationState.SPEAKING,
            turnState = TurnState.AGENT_SPEAKING,
            transcriptTurns = listOf(
                TranscriptTurn(
                    key = "user:1:-1",
                    turnId = 1,
                    streamId = null,
                    speaker = TranscriptSpeaker.USER,
                    text = "Hello",
                    status = TranscriptTurnStatus.END,
                    createdAtMillis = 1_000L,
                ),
                TranscriptTurn(
                    key = "agent:2:-1",
                    turnId = 2,
                    streamId = null,
                    speaker = TranscriptSpeaker.AGENT,
                    text = "I am listening",
                    status = TranscriptTurnStatus.IN_PROGRESS,
                    createdAtMillis = 2_000L,
                ),
            ),
            micEnabled = false,
            micRequestedEnabled = false,
            micAutoMuted = true,
        )

        val merged = ConversationUiStateMapper.mergeSession(currentState, snapshot)

        assertEquals("demo-channel", merged.channelName)
        assertEquals("42", merged.localUid)
        assertEquals("RTC connected", merged.rtcConnectionLabel)
        assertEquals("Connected", merged.rtmConnectionLabel)
        assertEquals(AgentVisualState.SPEAKING, merged.agentVisualState)
        assertEquals("Speaking back in real time · barge-in ready", merged.agentStateLabel)
        assertEquals(TurnState.AGENT_SPEAKING, merged.turnState)
        assertFalse(merged.micEnabled)
        assertFalse(merged.micRequestedEnabled)
        assertTrue(merged.micAutoMuted)
        assertEquals(1, merged.transcriptHistory.size)
        assertEquals("Hello", merged.transcriptHistory.first().text)
        assertEquals("I am listening", merged.liveTranscript?.text)
        assertTrue(merged.inConversation)
        assertNull(merged.errorMessage)
    }

    @Test
    fun mergeSessionMapsWaitingWhenRtcIsConnecting() {
        val merged = ConversationUiStateMapper.mergeSession(
            currentState = ConversationUiState(),
            snapshot = SessionSnapshot(
                rtcConnectionState = Constants.CONNECTION_STATE_CONNECTING,
                rtmConnectionState = "DISCONNECTED",
                agentState = AgentConversationState.IDLE,
            ),
        )

        assertEquals(AgentVisualState.WAITING, merged.agentVisualState)
        assertEquals("Waiting for the cloud agent", merged.agentStateLabel)
        assertEquals("RTC connecting", merged.rtcConnectionLabel)
    }

    @Test
    fun mergeSessionMapsDisconnectedWhenRtcFails() {
        val merged = ConversationUiStateMapper.mergeSession(
            currentState = ConversationUiState(inConversation = true),
            snapshot = SessionSnapshot(
                rtcConnectionState = Constants.CONNECTION_STATE_FAILED,
                rtmConnectionState = "FAILED",
                agentState = AgentConversationState.IDLE,
            ),
        )

        assertEquals(AgentVisualState.DISCONNECTED, merged.agentVisualState)
        assertEquals("Connection interrupted", merged.agentStateLabel)
        assertEquals("RTC failed", merged.rtcConnectionLabel)
        assertTrue(merged.inConversation)
    }
}
