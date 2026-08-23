# MoodLens - AI Voice Mood Journal

**MoodLens** is a voice-first mood journaling application and emotional companion built on top of the Agora Conversational AI platform. Instead of typing out how you feel, users simply talk about their day with an empathetic AI companion. As they speak, the app listens, analyzes their emotions, and visualizes their mood in real-time.

This recipe demonstrates how to combine **Agora RTC**, **Jetpack Compose Custom Graphics**, and **On-device sentiment analysis** into a seamless, interactive Android app.

---

## 🎯 Use Case

Traditional journaling apps suffer from high friction—users have to type out their thoughts after a long day. Voice assistants are typically transactional ("Set a timer"). This project bridges the gap by creating an AI companion that:
1. Actively listens via low-latency voice AI.
2. Infers emotional state in real-time directly from the streaming transcript.
3. Provides immediate visual and conversational feedback.

## 🏗️ Architecture & Key Components

This project is split into a **Python Backend** and a **Kotlin Android Client**.

### Backend (Agora TEN Framework)
The backend acts as the orchestrator. It connects to the Agora channel and bridges the gap between:
- **Speech-to-Text (Nova-3)**
- **LLM (GPT-4o-mini)**
- **Text-to-Speech (MiniMax)**

The backend is completely stateless and can be hosted on platforms like Render. It handles the heavy lifting of the conversational AI so the Android device is free to focus on UI and local sentiment analysis.

### Android Client (Jetpack Compose)
The Android app is responsible for the user interface, joining the Agora channel, and locally analyzing the conversation.

#### 1. Real-Time Transcript & Sentiment Analysis
As the user speaks, the Agora SDK receives real-time transcription events (`user.transcription` and `assistant.transcription`) via the RTM (Real-Time Messaging) channel.

The app takes these transcripts and feeds them into the `MoodAnalyzer`, which uses keyword matching and an Exponential Moving Average (EMA) algorithm to smoothly transition the user's emotional state over time.

```kotlin
// MoodAnalyzer.kt
fun analyzeIncremental(previousSnapshot: MoodSnapshot, newTurnText: String): MoodSnapshot {
    val newSnapshot = analyzeTranscript(listOf(newTurnText)) // Keyword matching
    
    // EMA Smoothing for fluid UI transitions
    val alpha = 0.4f
    val oneMinusAlpha = 1f - alpha

    val joy = (previousSnapshot.joy * oneMinusAlpha) + (newSnapshot.joy * alpha)
    // ... calculate for calm, energy, stress, sadness
    
    val blendedSnapshot = MoodSnapshot(joy = joy, calm = calm, ...)
    return blendedSnapshot.copy(dominantMood = blendedSnapshot.dominantDimension())
}
```

#### 2. Dynamic UI Visualizations
The `MoodRingCanvas.kt` component takes the `MoodSnapshot` flow and dynamically animates concentric rings. Each emotion corresponds to a specific color and ring size. As the EMA-smoothed scores update, the rings breathe and pulse, giving the user an intuitive sense of their emotional landscape.

#### 3. Local Data Persistence
For privacy, all journal entries are stored strictly on the user's device. When a session ends, the app packages the final `MoodSnapshot` and a summary of the transcript into a `MoodEntry` and saves it using `Gson` and encrypted `SharedPreferences`.

## 🚀 How to Run

### 1. Prerequisites
- Android Studio Ladybug or newer.
- An Agora Developer Account (App ID and App Certificate).
- (Optional) A Render account if you wish to deploy the backend yourself, otherwise you can use the provided quickstart server.

### 2. Configure the Android App
The Android app is pre-configured to connect to a live quickstart server. Ensure your `local.properties` file in the Android root directory points to the live backend (or your own deployed backend):
```properties
QUICKSTART_SERVER_URL=https://agora-voice-agent.onrender.com
```

### 3. Build & Run
Open the `voice_ai_agent` folder in Android Studio, sync Gradle, and run the app on an emulator or physical device.

### 4. Running your own Backend
If you wish to modify the AI's behavior, prompt, or services, you can run the Python backend locally:
1. Navigate to the `server/` directory.
2. Set up your virtual environment and run `pip install -r requirements.txt`.
3. Add your Agora App ID, App Certificate, and API keys to the `.env` file.
4. Run the server using uvicorn:
   ```bash
   python -m uvicorn app.main:app --reload --host 127.0.0.1
   ```
5. Point your Android app's `QUICKSTART_SERVER_URL` to `http://localhost:8000` (Use `adb reverse tcp:8000 tcp:8000` to expose localhost to your emulator/device).
