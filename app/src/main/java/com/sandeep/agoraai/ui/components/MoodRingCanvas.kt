package com.sandeep.agoraai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.agoraai.mood.MoodSnapshot

@Composable
fun MoodRingCanvas(
    modifier: Modifier = Modifier,
    joy: Float = 0f,
    calm: Float = 0f,
    energy: Float = 0f,
    stress: Float = 0f,
    sadness: Float = 0f,
    dominantEmoji: String = "😌",
    isActive: Boolean = false,
) {
    val joyAnim by animateFloatAsState(
        targetValue = joy,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "joyAnim"
    )
    val calmAnim by animateFloatAsState(
        targetValue = calm,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "calmAnim"
    )
    val energyAnim by animateFloatAsState(
        targetValue = energy,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "energyAnim"
    )
    val stressAnim by animateFloatAsState(
        targetValue = stress,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "stressAnim"
    )
    val sadnessAnim by animateFloatAsState(
        targetValue = sadness,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "sadnessAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minDim = size.minDimension
            val center = Offset(size.width / 2, size.height / 2)
            
            if (isActive) {
                val radius = minDim / 2
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = pulseAlpha), Color.Transparent),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }

            val strokeWidth = minDim / 12f
            val spacing = strokeWidth * 0.5f
            val startAngle = -90f // Start from top (12 o'clock)

            val rings = listOf(
                Pair(Color(0xFFFFC107), joyAnim), // Joy
                Pair(Color(0xFF64B5F6), calmAnim), // Calm
                Pair(Color(0xFF66BB6A), energyAnim), // Energy
                Pair(Color(0xFFEF5350), stressAnim), // Stress
                Pair(Color(0xFFAB47BC), sadnessAnim) // Sadness
            )

            var currentRadius = (minDim / 2f) - (strokeWidth / 2f)

            for ((color, value) in rings) {
                if (currentRadius > 0) {
                    // Draw subtle background ring
                    drawArc(
                        color = color.copy(alpha = 0.2f),
                        startAngle = startAngle,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(center.x - currentRadius, center.y - currentRadius),
                        size = Size(currentRadius * 2, currentRadius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Draw actual value ring
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = value * 360f,
                        useCenter = false,
                        topLeft = Offset(center.x - currentRadius, center.y - currentRadius),
                        size = Size(currentRadius * 2, currentRadius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                currentRadius -= (strokeWidth + spacing)
            }
        }
        
        Text(
            text = dominantEmoji,
            fontSize = 48.sp,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun MoodRingCanvas(
    modifier: Modifier = Modifier,
    mood: MoodSnapshot,
    isActive: Boolean = false,
) {
    MoodRingCanvas(
        modifier = modifier,
        joy = mood.joy,
        calm = mood.calm,
        energy = mood.energy,
        stress = mood.stress,
        sadness = mood.sadness,
        dominantEmoji = mood.dominantMood.emoji,
        isActive = isActive
    )
}

@Preview(showBackground = true)
@Composable
fun MoodRingCanvasPreview() {
    MaterialTheme {
        MoodRingCanvas(
            modifier = Modifier.size(200.dp).padding(16.dp),
            joy = 0.8f,
            calm = 0.6f,
            energy = 0.7f,
            stress = 0.3f,
            sadness = 0.1f,
            dominantEmoji = "😊",
            isActive = true
        )
    }
}
