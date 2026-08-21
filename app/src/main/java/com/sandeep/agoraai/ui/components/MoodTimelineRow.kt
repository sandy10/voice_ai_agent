package com.sandeep.agoraai.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.agoraai.mood.MoodDimension
import com.sandeep.agoraai.mood.MoodEntry
import com.sandeep.agoraai.mood.MoodSnapshot
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A horizontally scrollable row of mini mood rings showing the last 7 days.
 * Each day shows a mini MoodRingCanvas if there's an entry, or a dashed empty
 * circle if not. The current day gets a subtle highlight border.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MoodTimelineRow(
    modifier: Modifier = Modifier,
    entries: List<MoodEntry>,
) {
    AgentCard(
        modifier = modifier,
        title = "Your week",
        subtitle = "Mood patterns from the last 7 days",
    ) {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("MMM d")
        val dayFormatter = DateTimeFormatter.ofPattern("EEE")

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val dates = (6 downTo 0).map { today.minusDays(it.toLong()) }

            items(dates) { date ->
                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val entryForDate = entries.find { it.date == dateString }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isToday = date == today

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .run {
                                if (isToday) {
                                    border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                                } else {
                                    this
                                }
                            }
                            .padding(4.dp)
                    ) {
                        if (entryForDate != null) {
                            MoodRingCanvas(
                                mood = entryForDate.mood,
                                modifier = Modifier.size(48.dp)
                            )
                        } else {
                            Canvas(modifier = Modifier.size(48.dp)) {
                                drawCircle(
                                    color = Color.Gray,
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                )
                            }
                            Text("—", color = Color.Gray, fontSize = 16.sp)
                        }
                    }

                    Text(
                        text = date.format(dayFormatter),
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = date.format(dateFormatter),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun MoodTimelineRowPreview() {
    MaterialTheme {
        val today = LocalDate.now()
        val sampleEntries = listOf(
            MoodEntry(
                id = 1L,
                date = today.format(DateTimeFormatter.ISO_LOCAL_DATE),
                mood = MoodSnapshot(0.8f, 0.6f, 0.7f, 0.3f, 0.1f, MoodDimension.JOY),
                transcriptSummary = "Had a great day!",
                durationSeconds = 180,
            ),
            MoodEntry(
                id = 2L,
                date = today.minusDays(2).format(DateTimeFormatter.ISO_LOCAL_DATE),
                mood = MoodSnapshot(0.3f, 0.8f, 0.4f, 0.1f, 0.2f, MoodDimension.CALM),
                transcriptSummary = "Peaceful day",
                durationSeconds = 200,
            ),
            MoodEntry(
                id = 3L,
                date = today.minusDays(5).format(DateTimeFormatter.ISO_LOCAL_DATE),
                mood = MoodSnapshot(0.1f, 0.2f, 0.9f, 0.8f, 0.1f, MoodDimension.ENERGY),
                transcriptSummary = "Very busy day",
                durationSeconds = 150,
            ),
        )
        MoodTimelineRow(
            entries = sampleEntries,
            modifier = Modifier.padding(16.dp)
        )
    }
}
