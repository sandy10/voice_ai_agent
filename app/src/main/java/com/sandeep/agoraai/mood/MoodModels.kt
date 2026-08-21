package com.sandeep.agoraai.mood

enum class MoodDimension(val label: String, val emoji: String) {
    JOY("Joy", "😊"),
    CALM("Calm", "😌"),
    ENERGY("Energy", "⚡"),
    STRESS("Stress", "😰"),
    SADNESS("Sadness", "😢")
}

data class MoodSnapshot(
    val joy: Float = 0f,      // 0.0-1.0
    val calm: Float = 0f,     // 0.0-1.0  
    val energy: Float = 0f,   // 0.0-1.0
    val stress: Float = 0f,   // 0.0-1.0
    val sadness: Float = 0f,  // 0.0-1.0
    val dominantMood: MoodDimension = MoodDimension.CALM,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun dominantDimension(): MoodDimension {
        var maxScore = joy
        var dominant = MoodDimension.JOY
        
        if (calm > maxScore) {
            maxScore = calm
            dominant = MoodDimension.CALM
        }
        if (energy > maxScore) {
            maxScore = energy
            dominant = MoodDimension.ENERGY
        }
        if (stress > maxScore) {
            maxScore = stress
            dominant = MoodDimension.STRESS
        }
        if (sadness > maxScore) {
            maxScore = sadness
            dominant = MoodDimension.SADNESS
        }
        
        return dominant
    }
    
    fun overallPositivity(): Float {
        val score = (joy + calm + energy - stress - sadness + 2) / 4
        return score.coerceIn(0f, 1f)
    }
}

data class MoodEntry(
    val id: Long = 0,
    val date: String,              // "2026-08-21" format
    val mood: MoodSnapshot,
    val transcriptSummary: String, // first 200 chars of conversation
    val durationSeconds: Int,
    val createdAt: Long = System.currentTimeMillis()
)
