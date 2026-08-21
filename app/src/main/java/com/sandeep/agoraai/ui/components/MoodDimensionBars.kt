package com.sandeep.agoraai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.agoraai.mood.MoodDimension
import com.sandeep.agoraai.mood.MoodSnapshot

@Composable
fun MoodDimensionBars(
    modifier: Modifier = Modifier,
    mood: MoodSnapshot,
) {
    AgentCard(
        title = "Mood dimensions",
        subtitle = "Real-time emotional analysis from your conversation",
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MoodBarRow("😊 Joy", mood.joy, Color(0xFFFFC107))
            MoodBarRow("😌 Calm", mood.calm, Color(0xFF64B5F6))
            MoodBarRow("⚡ Energy", mood.energy, Color(0xFF66BB6A))
            MoodBarRow("😰 Stress", mood.stress, Color(0xFFEF5350))
            MoodBarRow("😢 Sadness", mood.sadness, Color(0xFFAB47BC))
        }
    }
}

@Composable
private fun MoodBarRow(
    label: String,
    score: Float,
    color: Color,
) {
    val animatedScore by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "scoreAnim"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(90.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedScore)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }

        Text(
            text = "${(animatedScore * 100).toInt()}%",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MoodDimensionBarsPreview() {
    MaterialTheme {
        MoodDimensionBars(
            mood = MoodSnapshot(
                joy = 0.72f,
                calm = 0.4f,
                energy = 0.6f,
                stress = 0.2f,
                sadness = 0.1f,
                dominantMood = MoodDimension.JOY
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
