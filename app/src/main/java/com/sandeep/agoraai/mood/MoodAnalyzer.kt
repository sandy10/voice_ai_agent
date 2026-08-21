package com.sandeep.agoraai.mood

import kotlin.math.min

class MoodAnalyzer {
    private val joyKeywords = setOf("happy", "great", "wonderful", "amazing", "love", "excited", "fantastic", "grateful", "thankful", "blessed", "awesome", "enjoyed", "fun", "laugh", "celebrate", "delighted", "thrilled", "joyful", "cheerful", "smile", "pleased", "good", "nice", "beautiful", "brilliant", "yay", "hooray", "glad", "super", "perfect")
    private val calmKeywords = setOf("peaceful", "relaxed", "calm", "serene", "gentle", "quiet", "still", "content", "comfortable", "ease", "meditate", "breathe", "rest", "tranquil", "soothing", "mindful", "zen", "balanced", "harmonious", "grounded", "steady", "settled", "chill", "unwind", "mellow", "placid")
    private val energyKeywords = setOf("energetic", "motivated", "pumped", "active", "enthusiastic", "driven", "productive", "accomplished", "busy", "working", "exercise", "run", "gym", "exciting", "ambitious", "dynamic", "powerful", "fired", "inspired", "determined", "focused", "strong", "ready", "unstoppable", "charged", "vibrant")
    private val stressKeywords = setOf("stressed", "anxious", "worried", "overwhelmed", "pressure", "deadline", "rushing", "panic", "nervous", "tense", "frustrated", "irritated", "annoyed", "exhausted", "tired", "struggling", "difficult", "hard", "challenging", "demanding", "hectic", "chaotic", "crazy", "upset", "mad", "angry", "fear", "scared")
    private val sadnessKeywords = setOf("sad", "lonely", "disappointed", "lost", "missing", "cry", "hurt", "pain", "grief", "depressed", "down", "blue", "gloomy", "heartbroken", "empty", "hopeless", "melancholy", "somber", "unhappy", "regret", "sorry", "terrible", "awful", "miserable", "sorrow", "tear", "alone", "isolated")

    fun analyzeTranscript(turns: List<String>): MoodSnapshot {
        var joyCount = 0
        var calmCount = 0
        var energyCount = 0
        var stressCount = 0
        var sadnessCount = 0

        for (turn in turns) {
            val words = turn.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }
            for (word in words) {
                if (joyKeywords.contains(word)) joyCount++
                if (calmKeywords.contains(word)) calmCount++
                if (energyKeywords.contains(word)) energyCount++
                if (stressKeywords.contains(word)) stressCount++
                if (sadnessKeywords.contains(word)) sadnessCount++
            }
        }

        val totalMatches = joyCount + calmCount + energyCount + stressCount + sadnessCount
        if (totalMatches == 0) {
            return MoodSnapshot(calm = 0.3f, dominantMood = MoodDimension.CALM)
        }

        val joyScore = normalizeScore(joyCount)
        val calmScore = normalizeScore(calmCount)
        val energyScore = normalizeScore(energyCount)
        val stressScore = normalizeScore(stressCount)
        val sadnessScore = normalizeScore(sadnessCount)

        val tempSnapshot = MoodSnapshot(
            joy = joyScore,
            calm = calmScore,
            energy = energyScore,
            stress = stressScore,
            sadness = sadnessScore
        )

        return tempSnapshot.copy(dominantMood = tempSnapshot.dominantDimension())
    }

    private fun normalizeScore(matchCount: Int): Float {
        return min(1f, matchCount.toFloat() / (matchCount.toFloat() + 3f))
    }

    fun analyzeIncremental(previousSnapshot: MoodSnapshot, newTurnText: String): MoodSnapshot {
        val newSnapshot = analyzeTranscript(listOf(newTurnText))
        val alpha = 0.4f
        val oneMinusAlpha = 1f - alpha

        val joy = (previousSnapshot.joy * oneMinusAlpha) + (newSnapshot.joy * alpha)
        val calm = (previousSnapshot.calm * oneMinusAlpha) + (newSnapshot.calm * alpha)
        val energy = (previousSnapshot.energy * oneMinusAlpha) + (newSnapshot.energy * alpha)
        val stress = (previousSnapshot.stress * oneMinusAlpha) + (newSnapshot.stress * alpha)
        val sadness = (previousSnapshot.sadness * oneMinusAlpha) + (newSnapshot.sadness * alpha)

        val blendedSnapshot = MoodSnapshot(
            joy = joy,
            calm = calm,
            energy = energy,
            stress = stress,
            sadness = sadness
        )

        return blendedSnapshot.copy(dominantMood = blendedSnapshot.dominantDimension())
    }
}
