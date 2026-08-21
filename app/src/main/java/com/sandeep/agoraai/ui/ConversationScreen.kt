package com.sandeep.agoraai.ui

import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.Locale
import com.sandeep.agoraai.audio.TurnState
import com.sandeep.agoraai.model.AgentVisualState
import com.sandeep.agoraai.model.ConversationUiState
import com.sandeep.agoraai.model.SessionIssue
import com.sandeep.agoraai.model.TranscriptSpeaker
import com.sandeep.agoraai.model.TranscriptTurn
import com.sandeep.agoraai.model.TranscriptTurnStatus
import com.sandeep.agoraai.mood.MoodDimension
import com.sandeep.agoraai.mood.MoodEntry
import com.sandeep.agoraai.mood.MoodSnapshot
import com.sandeep.agoraai.ui.components.AgentAvatarBadge
import com.sandeep.agoraai.ui.components.AgentButton
import com.sandeep.agoraai.ui.components.AgentButtonVariant
import com.sandeep.agoraai.ui.components.AgentCard
import com.sandeep.agoraai.ui.components.AgentIconControlButton
import com.sandeep.agoraai.ui.components.InfoField
import com.sandeep.agoraai.ui.components.LabeledIconText
import com.sandeep.agoraai.ui.components.MoodDimensionBars
import com.sandeep.agoraai.ui.components.MoodRingCanvas
import com.sandeep.agoraai.ui.components.StatusChip
import com.sandeep.agoraai.ui.theme.AgentquickstartandroidTheme

private object VoiceAiLayout {
    val ScreenPadding = 20.dp
    val SectionSpacing = 18.dp
    val CardSpacing = 16.dp
    val ContentMaxWidth = 980.dp
    val BottomBarHeight = 112.dp
    val TranscriptMinHeight = 280.dp
    val TranscriptMaxHeight = 460.dp
}

private data class StatusChipModel(
    val label: String,
    val highlighted: Boolean,
    val accent: Color,
)

private data class InfoItemModel(
    val label: String,
    val value: String,
)

@Composable
fun ConversationScreen(
    uiState: ConversationUiState,
    onStartRequested: () -> Unit,
    onEndConversation: () -> Unit,
    onToggleMicrophone: () -> Unit,
    onToggleTheme: () -> Unit,
    onDismissMessages: () -> Unit,
) {
    VoiceAiAppScreen(
        uiState = uiState,
        onStartRequested = onStartRequested,
        onEndConversation = onEndConversation,
        onToggleMicrophone = onToggleMicrophone,
        onToggleTheme = onToggleTheme,
        onDismissMessages = onDismissMessages,
    )
}

@Composable
fun VoiceAiAppScreen(
    uiState: ConversationUiState,
    onStartRequested: () -> Unit,
    onEndConversation: () -> Unit,
    onToggleMicrophone: () -> Unit,
    onToggleTheme: () -> Unit,
    onDismissMessages: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            MoodLensTopBar(
                isDarkTheme = uiState.isDarkTheme,
                onToggleTheme = onToggleTheme,
            )
        },
        bottomBar = {
            if (uiState.inConversation) {
                BottomCallControls(
                    micEnabled = uiState.micRequestedEnabled,
                    isStopping = uiState.isStopping,
                    onToggleMicrophone = onToggleMicrophone,
                    onEndConversation = onEndConversation,
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
            ) {
                val bottomPadding = if (uiState.inConversation) {
                    VoiceAiLayout.BottomBarHeight
                } else {
                    24.dp
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = VoiceAiLayout.ScreenPadding)
                        .widthIn(max = VoiceAiLayout.ContentMaxWidth)
                        .align(Alignment.TopCenter),
                    color = Color.Transparent,
                ) {
                    if (uiState.inConversation) {
                        MoodJournalSessionScreen(
                            uiState = uiState,
                            bottomPadding = bottomPadding,
                            onDismissMessages = onDismissMessages,
                        )
                    } else {
                        MoodJournalPreScreen(
                            uiState = uiState,
                            onStartRequested = onStartRequested,
                            onDismissMessages = onDismissMessages,
                        )
                    }
                }
            }
        }
    }
}

// ── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun MoodLensTopBar(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    Surface(
        modifier = Modifier.statusBarsPadding(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VoiceAiLayout.ScreenPadding, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusChip(
                    text = "MoodLens",
                    highlighted = true,
                    accentColor = MaterialTheme.colorScheme.primary,
                )
                AgentIconControlButton(
                    icon = if (isDarkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    contentDescription = if (isDarkTheme) "Switch to light theme" else "Switch to dark theme",
                    active = isDarkTheme,
                    onClick = onToggleTheme,
                )
            }
            Text(
                text = "Voice Mood Journal",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Talk to Luna, your AI companion. She'll help you reflect on your day while tracking your emotional landscape.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            )
        }
    }
}

// ── Pre-Session Screen (Mood Journal Home) ──────────────────────────────────

@Composable
private fun MoodJournalPreScreen(
    uiState: ConversationUiState,
    onStartRequested: () -> Unit,
    onDismissMessages: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(VoiceAiLayout.SectionSpacing),
    ) {
        // Today's mood ring or greeting
        item {
            TodayMoodCard(
                journaledToday = uiState.journaledToday,
                currentMood = uiState.currentMood,
                moodHistory = uiState.moodHistory,
            )
        }

        // 7-day mood timeline (if there's history)
        if (uiState.moodHistory.isNotEmpty() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            item {
                com.sandeep.agoraai.ui.components.MoodTimelineRow(
                    entries = uiState.moodHistory,
                )
            }
        }

        transientMessages(
            errorMessage = uiState.errorMessage,
            warningMessage = uiState.warningMessage,
            onDismissMessages = onDismissMessages,
        )

        item {
            JournalStartCard(
                uiState = uiState,
                onStartRequested = onStartRequested,
            )
        }
    }
}

@Composable
private fun TodayMoodCard(
    journaledToday: Boolean,
    currentMood: MoodSnapshot,
    moodHistory: List<MoodEntry>,
) {
    AgentCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (journaledToday && moodHistory.isNotEmpty()) {
                val todayEntry = moodHistory.lastOrNull()
                val todayMood = todayEntry?.mood ?: currentMood

                Text(
                    text = "Today's mood",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                MoodRingCanvas(
                    modifier = Modifier.size(140.dp),
                    mood = todayMood,
                    isActive = false,
                )

                Text(
                    text = "${todayMood.dominantMood.emoji} ${todayMood.dominantMood.label}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )

                if (todayEntry?.transcriptSummary != null) {
                    Text(
                        text = todayEntry.transcriptSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                    )
                }
            } else {
                Text(
                    text = "How are you feeling today?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                MoodRingCanvas(
                    modifier = Modifier.size(120.dp),
                    mood = MoodSnapshot(calm = 0.15f),
                    isActive = false,
                )

                Text(
                    text = "Start a voice journal session with Luna to explore your emotions and build your mood timeline.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun JournalStartCard(
    uiState: ConversationUiState,
    onStartRequested: () -> Unit,
) {
    AgentCard(
        title = "Ready to journal",
        subtitle = "Voice-first mood journaling powered by Agora Conversational AI.",
    ) {
        LabeledIconText(
            icon = Icons.Outlined.Link,
            label = "Your companion: Luna",
            value = "An empathetic AI that listens, reflects, and helps you process your day through natural conversation.",
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            preSessionStatusChips(uiState).forEach { chip ->
                StatusChip(
                    text = chip.label,
                    highlighted = chip.highlighted,
                    accentColor = chip.accent,
                )
            }
        }

        if (!uiState.isConfigured && uiState.configMessage != null) {
            InlineNoticeCard(
                title = "Configuration needed",
                message = uiState.configMessage,
                accentColor = MaterialTheme.colorScheme.error,
                icon = Icons.Outlined.ErrorOutline,
            )
        }

        AgentButton(
            text = if (uiState.isStarting) {
                "Connecting to Luna..."
            } else if (uiState.journaledToday) {
                "Journal again today"
            } else {
                "Start journaling"
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isConfigured && !uiState.isStarting,
            onClick = onStartRequested,
        )
    }
}

// ── In-Session Screen (Active Mood Journal) ─────────────────────────────────

@Composable
fun MoodJournalSessionScreen(
    uiState: ConversationUiState,
    bottomPadding: Dp,
    onDismissMessages: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 24.dp, bottom = bottomPadding),
        verticalArrangement = Arrangement.spacedBy(VoiceAiLayout.SectionSpacing),
    ) {
        transientMessages(
            errorMessage = uiState.errorMessage,
            warningMessage = uiState.warningMessage,
            onDismissMessages = onDismissMessages,
        )

        // Live mood ring with companion status
        item {
            LiveMoodPresenceCard(
                visualState = uiState.agentVisualState,
                label = uiState.agentStateLabel,
                turnState = uiState.turnState,
                currentMood = uiState.currentMood,
            )
        }

        // Real-time mood dimension bars
        item {
            MoodDimensionBars(
                mood = uiState.currentMood,
            )
        }

        // Transcript
        item {
            TranscriptPanel(
                history = uiState.transcriptHistory,
                liveTranscript = uiState.liveTranscript,
            )
        }

        if (uiState.issues.isNotEmpty()) {
            item {
                IssuesPanel(issues = uiState.issues)
            }
        }

        item {
            LiveSessionCard(uiState = uiState)
        }
    }
}

@Composable
private fun LiveMoodPresenceCard(
    modifier: Modifier = Modifier,
    visualState: AgentVisualState,
    label: String,
    turnState: TurnState,
    currentMood: MoodSnapshot,
) {
    AgentCard(
        modifier = modifier,
        title = "Luna",
        subtitle = "Your mood journal companion",
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Live animated mood ring
            MoodRingCanvas(
                modifier = Modifier.size(140.dp),
                mood = currentMood,
                isActive = true,
            )

            Text(
                text = label.replace("cloud agent", "Luna")
                    .replace("Agora agent", "Luna"),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusChip(
                    text = turnState.toReadableLabel(),
                    highlighted = true,
                    accentColor = visualState.accentColor(),
                )
                if (currentMood.dominantMood != MoodDimension.CALM || currentMood.calm > 0.1f) {
                    StatusChip(
                        text = "${currentMood.dominantMood.emoji} ${currentMood.dominantMood.label}",
                        highlighted = true,
                        accentColor = currentMood.dominantMood.toColor(),
                    )
                }
            }
        }
    }
}

// ── Transcript Panel ────────────────────────────────────────────────────────

@Composable
fun TranscriptPanel(
    modifier: Modifier = Modifier,
    history: List<TranscriptTurn>,
    liveTranscript: TranscriptTurn?,
) {
    val listState = rememberLazyListState()
    val visibleTurns = buildList {
        addAll(history)
        if (liveTranscript != null) {
            add(liveTranscript)
        }
    }

    LaunchedEffect(visibleTurns.size, liveTranscript?.text) {
        if (visibleTurns.isNotEmpty()) {
            listState.animateScrollToItem(visibleTurns.lastIndex)
        }
    }

    AgentCard(
        modifier = modifier,
        title = "Conversation",
        subtitle = "Your voice journal session with Luna.",
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
            ),
        ) {
            if (visibleTurns.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(VoiceAiLayout.TranscriptMinHeight)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        MoodRingCanvas(
                            modifier = Modifier.size(64.dp),
                            mood = MoodSnapshot(calm = 0.2f),
                            isActive = true,
                        )
                        Text(
                            text = "Luna is getting ready to listen...",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Start talking about your day and your conversation will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = VoiceAiLayout.TranscriptMinHeight,
                            max = VoiceAiLayout.TranscriptMaxHeight,
                        )
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    state = listState,
                ) {
                    items(items = visibleTurns, key = { it.key }) { turn ->
                        TranscriptBubble(turn = turn)
                    }
                }
            }
        }
    }
}

@Composable
private fun TranscriptBubble(
    turn: TranscriptTurn,
) {
    val isUser = turn.speaker == TranscriptSpeaker.USER
    val containerColor = when {
        isUser -> MaterialTheme.colorScheme.primaryContainer
        turn.status == TranscriptTurnStatus.INTERRUPTED -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isUser -> MaterialTheme.colorScheme.onPrimaryContainer
        turn.status == TranscriptTurnStatus.INTERRUPTED -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = containerColor,
            tonalElevation = 1.dp,
            shadowElevation = 0.dp,
            modifier = Modifier.widthIn(max = 360.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (isUser) "You" else "Luna",
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.76f),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = turn.text.ifBlank { "..." },
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                )
                if (turn.status != TranscriptTurnStatus.END) {
                    StatusChip(
                        text = if (turn.status == TranscriptTurnStatus.IN_PROGRESS) {
                            "Streaming"
                        } else {
                            "Interrupted"
                        },
                        highlighted = true,
                        accentColor = if (turn.status == TranscriptTurnStatus.IN_PROGRESS) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                    )
                }
            }
        }
    }
}

// ── Issues Panel ────────────────────────────────────────────────────────────

@Composable
private fun IssuesPanel(
    issues: List<SessionIssue>,
) {
    AgentCard(
        title = "Session diagnostics",
        subtitle = "Recent warnings and runtime signals surfaced by the realtime layer.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            issues.take(4).forEachIndexed { index, issue ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AgentAvatarBadge(
                            name = issue.source.uppercase(Locale.ROOT),
                            modifier = Modifier.size(42.dp),
                            highlightColor = MaterialTheme.colorScheme.error,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = issue.source.uppercase(Locale.ROOT),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = issue.code,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = issue.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (index != issues.take(4).lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    }
                }
            }
        }
    }
}

// ── Bottom Controls ─────────────────────────────────────────────────────────

@Composable
fun BottomCallControls(
    micEnabled: Boolean,
    isStopping: Boolean,
    onToggleMicrophone: () -> Unit,
    onEndConversation: () -> Unit,
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        tonalElevation = 6.dp,
        shadowElevation = 12.dp,
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VoiceAiLayout.ScreenPadding, vertical = 16.dp)
                .heightIn(min = 72.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            AgentIconControlButton(
                icon = if (micEnabled) Icons.Outlined.Mic else Icons.Outlined.MicOff,
                contentDescription = if (micEnabled) "Mute microphone" else "Unmute microphone",
                active = micEnabled,
                onClick = onToggleMicrophone,
            )
            AgentButton(
                text = if (micEnabled) "Mute mic" else "Unmute mic",
                modifier = Modifier.weight(1f),
                variant = AgentButtonVariant.Secondary,
                onClick = onToggleMicrophone,
            )
            AgentButton(
                text = if (isStopping) "Saving mood..." else "End & save",
                modifier = Modifier.weight(1f),
                variant = AgentButtonVariant.Destructive,
                enabled = !isStopping,
                onClick = onEndConversation,
            )
        }
    }
}

// ── Live Session Info ───────────────────────────────────────────────────────

@Composable
private fun LiveSessionCard(
    modifier: Modifier = Modifier,
    uiState: ConversationUiState,
) {
    AgentCard(
        modifier = modifier,
        title = "Session details",
        subtitle = "Channel, transport health, and microphone state.",
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            connectedStatusChips(uiState).forEach { item ->
                StatusChip(
                    text = item.label,
                    highlighted = item.highlighted,
                    accentColor = item.accent,
                )
            }
        }

        ResponsiveInfoGrid(
            items = connectedInfoItems(uiState),
        )
    }
}

@Composable
private fun ResponsiveInfoGrid(
    items: List<InfoItemModel>,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= 640.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowItems.forEach { item ->
                            InfoField(
                                label = item.label,
                                value = item.value,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { item ->
                    InfoField(
                        label = item.label,
                        value = item.value,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// ── Shared Utility Components ───────────────────────────────────────────────

@Composable
private fun InlineNoticeCard(
    title: String,
    message: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.1f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.transientMessages(
    errorMessage: String?,
    warningMessage: String?,
    onDismissMessages: () -> Unit,
) {
    if (errorMessage != null) {
        item {
            DismissibleMessageCard(
                title = "Action needed",
                message = errorMessage,
                accentColor = MaterialTheme.colorScheme.error,
                icon = Icons.Outlined.ErrorOutline,
                onDismiss = onDismissMessages,
            )
        }
    }

    if (warningMessage != null) {
        item {
            DismissibleMessageCard(
                title = "Heads up",
                message = warningMessage,
                accentColor = MaterialTheme.colorScheme.tertiary,
                icon = Icons.Outlined.WarningAmber,
                onDismiss = onDismissMessages,
            )
        }
    }
}

@Composable
private fun DismissibleMessageCard(
    title: String,
    message: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onDismiss: () -> Unit,
) {
    AgentCard {
        InlineNoticeCard(
            title = title,
            message = message,
            accentColor = accentColor,
            icon = icon,
        )
        AgentButton(
            text = "Dismiss",
            modifier = Modifier.fillMaxWidth(),
            variant = AgentButtonVariant.Secondary,
            onClick = onDismiss,
        )
    }
}

// ── Status Chips & Info Items ───────────────────────────────────────────────

@Composable
private fun preSessionStatusChips(uiState: ConversationUiState): List<StatusChipModel> {
    return listOf(
        StatusChipModel(
            label = if (uiState.isConfigured) "Backend ready" else "Server config needed",
            highlighted = uiState.isConfigured,
            accent = MaterialTheme.colorScheme.primary,
        ),
        StatusChipModel(
            label = if (uiState.microphonePermissionGranted) "Microphone ready" else "Microphone needed",
            highlighted = uiState.microphonePermissionGranted,
            accent = MaterialTheme.colorScheme.secondary,
        ),
        StatusChipModel(
            label = if (uiState.journaledToday) "Journaled today ✓" else "Not journaled yet",
            highlighted = uiState.journaledToday,
            accent = MaterialTheme.colorScheme.tertiary,
        ),
    )
}

@Composable
private fun connectedStatusChips(uiState: ConversationUiState): List<StatusChipModel> {
    val agentJoined = uiState.agentVisualState != AgentVisualState.WAITING &&
        uiState.agentVisualState != AgentVisualState.DISCONNECTED

    return listOf(
        StatusChipModel(
            label = "Luna active",
            highlighted = true,
            accent = MaterialTheme.colorScheme.primary,
        ),
        StatusChipModel(
            label = if (uiState.micRequestedEnabled) "Microphone ready" else "Microphone muted",
            highlighted = uiState.micRequestedEnabled,
            accent = MaterialTheme.colorScheme.secondary,
        ),
        StatusChipModel(
            label = uiState.rtcConnectionLabel,
            highlighted = uiState.rtcConnectionLabel.contains("connected", ignoreCase = true),
            accent = MaterialTheme.colorScheme.primary,
        ),
        StatusChipModel(
            label = if (agentJoined) "Luna joined" else "Waiting for Luna",
            highlighted = agentJoined,
            accent = uiState.agentVisualState.accentColor(),
        ),
    )
}

private fun connectedInfoItems(uiState: ConversationUiState): List<InfoItemModel> {
    return listOf(
        InfoItemModel("Channel", uiState.channelName ?: "Joining..."),
        InfoItemModel("Local UID", uiState.localUid ?: "Pending"),
        InfoItemModel("RTM status", uiState.rtmConnectionLabel),
        InfoItemModel("Backend latency", uiState.backendLatencyMs?.let { "$it ms" } ?: "Pending"),
        InfoItemModel("Dominant mood", "${uiState.currentMood.dominantMood.emoji} ${uiState.currentMood.dominantMood.label}"),
    )
}

@Composable
private fun AgentVisualState.accentColor(): Color {
    return when (this) {
        AgentVisualState.WAITING -> MaterialTheme.colorScheme.outline
        AgentVisualState.LISTENING -> MaterialTheme.colorScheme.secondary
        AgentVisualState.THINKING -> MaterialTheme.colorScheme.tertiary
        AgentVisualState.SPEAKING -> MaterialTheme.colorScheme.primary
        AgentVisualState.IDLE -> MaterialTheme.colorScheme.primary
        AgentVisualState.DISCONNECTED -> MaterialTheme.colorScheme.error
    }
}

private fun TurnState.toReadableLabel(): String {
    return when (this) {
        TurnState.IDLE -> "Standing by"
        TurnState.USER_SPEAKING -> "Listening to you"
        TurnState.USER_TURN_FINALIZING -> "Processing..."
        TurnState.AGENT_THINKING -> "Luna is thinking"
        TurnState.AGENT_SPEAKING -> "Luna is speaking"
        TurnState.BARGE_IN_DETECTED -> "Interrupted"
    }
}

@Composable
private fun MoodDimension.toColor(): Color {
    return when (this) {
        MoodDimension.JOY -> Color(0xFFFFC107)
        MoodDimension.CALM -> Color(0xFF64B5F6)
        MoodDimension.ENERGY -> Color(0xFF66BB6A)
        MoodDimension.STRESS -> Color(0xFFEF5350)
        MoodDimension.SADNESS -> Color(0xFFAB47BC)
    }
}

// ── Previews ────────────────────────────────────────────────────────────────

@Preview(
    name = "MoodLens Pre-session",
    showBackground = true,
    widthDp = 420,
    heightDp = 900,
)
@Composable
private fun PreSessionPreview() {
    AgentquickstartandroidTheme {
        ConversationScreen(
            uiState = previewPreSessionState(),
            onStartRequested = {},
            onEndConversation = {},
            onToggleMicrophone = {},
            onToggleTheme = {},
            onDismissMessages = {},
        )
    }
}

@Preview(
    name = "MoodLens Active Session",
    showBackground = true,
    widthDp = 420,
    heightDp = 900,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ConnectedSessionPreview() {
    AgentquickstartandroidTheme(darkTheme = true) {
        ConversationScreen(
            uiState = previewConnectedState(),
            onStartRequested = {},
            onEndConversation = {},
            onToggleMicrophone = {},
            onToggleTheme = {},
            onDismissMessages = {},
        )
    }
}

private fun previewPreSessionState(): ConversationUiState {
    return ConversationUiState(
        isConfigured = true,
        microphonePermissionGranted = true,
        configMessage = null,
        warningMessage = null,
        errorMessage = null,
        moodHistory = listOf(
            MoodEntry(
                id = 1,
                date = "2026-08-20",
                mood = MoodSnapshot(joy = 0.7f, calm = 0.5f, energy = 0.6f, stress = 0.2f, sadness = 0.1f, dominantMood = MoodDimension.JOY),
                transcriptSummary = "Had a great day at work, finished the project!",
                durationSeconds = 180,
            ),
            MoodEntry(
                id = 2,
                date = "2026-08-19",
                mood = MoodSnapshot(joy = 0.3f, calm = 0.8f, energy = 0.4f, stress = 0.1f, sadness = 0.2f, dominantMood = MoodDimension.CALM),
                transcriptSummary = "Peaceful Sunday, read a book and went for a walk.",
                durationSeconds = 240,
            ),
        ),
    )
}

private fun previewConnectedState(): ConversationUiState {
    return ConversationUiState(
        isConfigured = true,
        microphonePermissionGranted = true,
        inConversation = true,
        channelName = "mood-session-123",
        localUid = "1045",
        rtcConnectionLabel = "RTC connected",
        rtmConnectionLabel = "Connected",
        agentVisualState = AgentVisualState.SPEAKING,
        agentStateLabel = "Luna is speaking",
        turnState = TurnState.AGENT_SPEAKING,
        micEnabled = true,
        micRequestedEnabled = true,
        currentMood = MoodSnapshot(
            joy = 0.65f,
            calm = 0.4f,
            energy = 0.55f,
            stress = 0.15f,
            sadness = 0.1f,
            dominantMood = MoodDimension.JOY,
        ),
        transcriptHistory = listOf(
            TranscriptTurn(
                key = "1",
                turnId = 1L,
                streamId = 1L,
                speaker = TranscriptSpeaker.AGENT,
                text = "Hey there! I'd love to hear about your day. What's been on your mind?",
                status = TranscriptTurnStatus.END,
                createdAtMillis = 0L,
            ),
            TranscriptTurn(
                key = "2",
                turnId = 2L,
                streamId = 1L,
                speaker = TranscriptSpeaker.USER,
                text = "I had a really great day actually! We launched the new feature and everyone was excited about it.",
                status = TranscriptTurnStatus.END,
                createdAtMillis = 1L,
            ),
        ),
        liveTranscript = TranscriptTurn(
            key = "3",
            turnId = 3L,
            streamId = 2L,
            speaker = TranscriptSpeaker.AGENT,
            text = "That sounds wonderful! It must feel amazing to see your work come together like that. What part of the launch felt most rewarding?",
            status = TranscriptTurnStatus.IN_PROGRESS,
            createdAtMillis = 2L,
        ),
    )
}
