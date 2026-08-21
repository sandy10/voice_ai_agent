package com.sandeep.agoraai.ui

import android.app.Application
import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sandeep.agoraai.config.QuickstartConfig
import com.sandeep.agoraai.data.ConversationRepository
import com.sandeep.agoraai.model.ConversationUiState
import com.sandeep.agoraai.model.TranscriptSpeaker
import com.sandeep.agoraai.model.TranscriptTurnStatus
import com.sandeep.agoraai.mood.MoodAnalyzer
import com.sandeep.agoraai.mood.MoodEntry
import com.sandeep.agoraai.mood.MoodSnapshot
import com.sandeep.agoraai.mood.MoodStorage
import com.sandeep.agoraai.rtc.AgoraConversationSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ConversationViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository = ConversationRepository()
    private val sessionManager = AgoraConversationSessionManager(application)
    private val moodAnalyzer = MoodAnalyzer()
    private val moodStorage = MoodStorage(application)
    private val _uiState = MutableStateFlow(ConversationUiStateMapper.freshUiState())

    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var activeAgentId: String? = null
    private var themeInitialized: Boolean = false
    private var sessionStartTimeMs: Long = 0L
    private var lastAnalyzedTurnCount: Int = 0

    init {
        loadMoodHistory()

        viewModelScope.launch {
            sessionManager.snapshot.collectLatest { snapshot ->
                // Run mood analysis on new completed transcript turns
                val allTurns = snapshot.transcriptTurns
                if (allTurns.size > lastAnalyzedTurnCount) {
                    val newTurns = allTurns.subList(lastAnalyzedTurnCount, allTurns.size)
                    var currentMood = _uiState.value.currentMood
                    for (turn in newTurns) {
                        if (turn.text.isNotBlank() && turn.status != TranscriptTurnStatus.IN_PROGRESS) {
                            currentMood = moodAnalyzer.analyzeIncremental(currentMood, turn.text)
                        }
                    }
                    lastAnalyzedTurnCount = allTurns.size
                    _uiState.update { current ->
                        current.copy(currentMood = currentMood)
                    }
                }

                // Also do live analysis on the in-progress transcript for visual feedback
                val liveTurn = allTurns.lastOrNull { it.status == TranscriptTurnStatus.IN_PROGRESS }
                if (liveTurn != null && liveTurn.text.length > 10) {
                    val liveMood = moodAnalyzer.analyzeIncremental(_uiState.value.currentMood, liveTurn.text)
                    _uiState.update { current ->
                        ConversationUiStateMapper.mergeSession(
                            current.copy(currentMood = liveMood),
                            snapshot,
                        )
                    }
                } else {
                    _uiState.update { current ->
                        ConversationUiStateMapper.mergeSession(current, snapshot)
                    }
                }
            }
        }
    }

    private fun loadMoodHistory() {
        val history = moodStorage.getLast7DaysEntries()
        val todayEntry = moodStorage.getTodayEntry()
        _uiState.update {
            it.copy(
                moodHistory = history,
                journaledToday = todayEntry != null,
            )
        }
    }

    private fun getTodayDateString(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        } else {
            val cal = java.util.Calendar.getInstance()
            String.format(
                "%04d-%02d-%02d",
                cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1,
                cal.get(java.util.Calendar.DAY_OF_MONTH),
            )
        }
    }

    private fun buildMoodContextForAgent(): String? {
        val latest = moodStorage.getLatestEntry() ?: return null
        val mood = latest.mood
        return "Last session (${latest.date}): " +
            "dominant mood was ${mood.dominantMood.label} " +
            "(joy=${(mood.joy * 100).toInt()}%, " +
            "calm=${(mood.calm * 100).toInt()}%, " +
            "energy=${(mood.energy * 100).toInt()}%, " +
            "stress=${(mood.stress * 100).toInt()}%, " +
            "sadness=${(mood.sadness * 100).toInt()}%). " +
            "Summary: ${latest.transcriptSummary}"
    }

    fun updateMicrophonePermission(granted: Boolean) {
        _uiState.update { it.copy(microphonePermissionGranted = granted) }
    }

    fun initializeTheme(systemDarkTheme: Boolean) {
        if (themeInitialized) {
            return
        }
        themeInitialized = true
        _uiState.update { it.copy(isDarkTheme = systemDarkTheme) }
    }

    fun toggleTheme() {
        themeInitialized = true
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun togglePersona() {
        _uiState.update { 
            val newPersona = if (it.selectedPersona == "luna") "sol" else "luna"
            it.copy(selectedPersona = newPersona)
        }
    }

    fun startConversation() {
        val currentState = _uiState.value
        if (currentState.isStarting || currentState.isStopping) {
            return
        }
        if (!QuickstartConfig.isConfigured) {
            _uiState.update {
                it.copy(
                    errorMessage = QuickstartConfig.startupHelpMessage(),
                    warningMessage = null,
                )
            }
            return
        }
        if (!currentState.microphonePermissionGranted) {
            _uiState.update {
                it.copy(
                    errorMessage = "Microphone access is required to publish your voice to the Agora channel.",
                    warningMessage = null,
                )
            }
            return
        }

        // Reset mood analysis state for new session
        lastAnalyzedTurnCount = 0
        sessionStartTimeMs = SystemClock.elapsedRealtime()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isStarting = true,
                    errorMessage = null,
                    warningMessage = null,
                    currentMood = MoodSnapshot(),
                )
            }

            runCatching {
                val healthStartedAt = SystemClock.elapsedRealtime()
                val health = repository.checkHealth()
                _uiState.update {
                    it.copy(
                        backendLatencyMs = SystemClock.elapsedRealtime() - healthStartedAt,
                        lastServerResponse = "${health.status} (${health.version})",
                    )
                }
                val bootstrap = repository.requestSessionBootstrap()
                sessionManager.connect(bootstrap) { channel, rtcUid, rtmUserId ->
                    repository.renewTokens(
                        channel = channel,
                        rtcUid = rtcUid,
                        rtmUserId = rtmUserId,
                    )
                }

                val requesterRtcUid = sessionManager.snapshot.value.localRtcUid
                    .takeIf { it > 0 }
                    ?.toString()
                    ?: bootstrap.uid
                val inviteAttempt = runCatching {
                    repository.inviteAgent(
                        channelName = bootstrap.channel,
                        requesterRtcUid = requesterRtcUid,
                        agentMode = "mood_journal",
                        moodContext = buildMoodContextForAgent(),
                        agentProfile = currentState.selectedPersona,
                    )
                }
                val inviteResult = inviteAttempt.getOrNull()
                activeAgentId = inviteResult?.agentId
                sessionManager.setActiveAgentId(activeAgentId)

                val warning = inviteAttempt.exceptionOrNull()?.message?.let { message ->
                    "The Android client joined the channel, but the server could not start the Agora agent: $message"
                } ?: if (inviteResult?.agentId == null) {
                    "The Android client joined the channel, but the server returned no Agora agent ID."
                } else null

                _uiState.update { current ->
                    ConversationUiStateMapper.mergeSession(
                        current.copy(
                            isStarting = false,
                            inConversation = true,
                            warningMessage = warning,
                        ),
                        sessionManager.snapshot.value,
                    )
                }
            }.onFailure { error ->
                sessionManager.disconnect(resetSnapshot = true)
                activeAgentId = null
                _uiState.value = ConversationUiStateMapper.freshUiState(
                    permissionGranted = _uiState.value.microphonePermissionGranted,
                    errorMessage = error.message ?: "Unable to start the conversation through the quickstart server.",
                    isDarkTheme = _uiState.value.isDarkTheme,
                    moodHistory = _uiState.value.moodHistory,
                    journaledToday = _uiState.value.journaledToday,
                ).copy(lastServerResponse = "Request failed")
            }
        }
    }

    fun endConversation() {
        val currentState = _uiState.value
        if (currentState.isStopping || currentState.isStarting) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isStopping = true) }

            // Save mood entry before ending
            saveMoodEntry()

            val warning = activeAgentId?.let { agentId ->
                val channelName = sessionManager.snapshot.value.channelName
                runCatching {
                    if (channelName != null) {
                        repository.stopConversation(agentId, channelName)
                    }
                }.exceptionOrNull()?.message?.let { message ->
                    "The local session ended, but the server leave request failed: $message"
                }
            }

            activeAgentId = null
            sessionManager.setActiveAgentId(null)
            sessionManager.disconnect(resetSnapshot = true)
            lastAnalyzedTurnCount = 0

            // Reload mood history after saving
            val updatedHistory = moodStorage.getLast7DaysEntries()
            val todayEntry = moodStorage.getTodayEntry()

            _uiState.value = ConversationUiStateMapper.freshUiState(
                permissionGranted = _uiState.value.microphonePermissionGranted,
                warningMessage = warning,
                isDarkTheme = _uiState.value.isDarkTheme,
                moodHistory = updatedHistory,
                journaledToday = todayEntry != null,
            )
        }
    }

    private fun saveMoodEntry() {
        val currentMood = _uiState.value.currentMood
        val transcriptTurns = sessionManager.snapshot.value.transcriptTurns

        val summary = transcriptTurns
            .filter { it.speaker == TranscriptSpeaker.USER && it.status == TranscriptTurnStatus.END }
            .joinToString(" ") { it.text }
            .take(200)

        if (summary.isBlank() && currentMood.joy == 0f && currentMood.calm == 0f) {
            return
        }

        val durationSeconds = ((SystemClock.elapsedRealtime() - sessionStartTimeMs) / 1000).toInt()

        val entry = MoodEntry(
            date = getTodayDateString(),
            mood = currentMood,
            transcriptSummary = summary.ifBlank { "Brief conversation" },
            durationSeconds = durationSeconds,
        )

        moodStorage.saveMoodEntry(entry)
    }

    fun toggleMicrophone() {
        sessionManager.setMicrophoneEnabled(!_uiState.value.micRequestedEnabled)
    }

    fun clearTransientMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                warningMessage = null,
            )
        }
    }

    override fun onCleared() {
        sessionManager.release()
        super.onCleared()
    }
}
