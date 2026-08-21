# MoodLens 🔮

**MoodLens** is a voice-first mood journaling application and emotional companion built on top of the Agora Conversational AI platform. Instead of typing out how you feel, simply talk about your day with your empathetic AI companion. As you speak, the app listens, analyzes your emotions, and visualizes your mood in real-time.

---

## ✨ Features

### 🎙️ Conversational AI Companions (Luna & Sol)
- **Natural Voice Interaction:** Have fluid, uninterrupted, low-latency conversations using Agora's Real-Time Communication (RTC) framework.
- **Dual Personas:** Choose between **Luna** (a warm, empathetic female voice) or **Sol** (a charming, supportive male voice). The backend dynamically updates the LLM prompt and Text-to-Speech voices based on your selection.
- **Context Awareness:** Your companion remembers your emotional state from the start of the session to help guide the conversation constructively.

### 🧠 Real-Time Mood Analysis Engine
- **Live Sentiment Tracking:** The app locally analyzes the ongoing transcript, mapping your spoken words to five core emotional dimensions: 
  - 🟨 **Joy**
  - 🟦 **Calm**
  - 🟩 **Energy**
  - 🟥 **Stress**
  - 🟪 **Sadness**
- **Dynamic Scoring:** Uses Exponential Moving Average (EMA) and sigmoid normalization to create smooth, flowing transitions in your mood scores as your mood shifts during the conversation.

### 🎨 Immersive UI Visualizations & Haptics (Jetpack Compose)
- **Animated Mood Ring:** A custom Compose Canvas component featuring concentric rings that pulse and glow. The sizes and colors of the rings shift in real-time to reflect your dominant emotions.
- **Live Transcript:** A beautifully styled chat interface that displays exactly what you and your companion are saying in real-time.
- **Haptic Feedback:** The app utilizes Android's native haptic feedback to deliver subtle, responsive vibrations when you mute the mic, end a session, or right as the AI begins speaking, making the companion feel more alive.

### 📅 Local Journaling & Sharing
- **Automatic Saving:** At the end of every session, your dominant mood and a summary of your transcript are securely saved to your local device.
- **7-Day Timeline:** A visually rich, horizontally scrollable timeline showing your mini mood-rings from the past week at a glance.
- **Share Entry:** Found a journal session particularly insightful? Tap the "Share entry" button to quickly export your dominant mood and transcript summary to your Notes, Messages, or social media!
- **No Cloud Database Required:** All user history is strictly saved locally on the Android device using encrypted SharedPreferences.

---

## 🛠️ Tech Stack

**Client (Android):**
- 100% Kotlin
- UI: Jetpack Compose (Material 3)
- Real-time Audio/Video: Agora RTC & RTM SDKs
- State Management: StateFlow / ViewModel

**Server (Backend Hosted on Render):**
- Python 3 / FastAPI
- Agora TEN Framework (Token generation & Agent Orchestration)
- Speech-to-Text: Nova-3
- LLM: GPT-4o-mini
- Text-to-Speech: MiniMax TTS

---

## 🚀 How to Run

### 1. Configure the Android App
The backend for this app is centrally deployed on Render, meaning you don't have to host it locally to test the Android application!

Ensure your `local.properties` file in the Android root directory points to the live backend:
```properties
QUICKSTART_SERVER_URL=https://agora-voice-agent.onrender.com
```

### 2. Build the Android App
Build and install the app to your device:
```bash
./gradlew installDebug
```

### 3. (Optional) Run the Backend Locally
If you wish to modify the Python backend and run it locally:
1. Go to the `server/` directory.
2. Activate your virtual environment and install dependencies (`pip install -r requirements.txt`).
3. Set your Agora credentials in the `.env` file.
4. Run the server: 
   ```bash
   python -m uvicorn app.main:app --reload --host 127.0.0.1
   ```
5. Update your Android `local.properties` to `http://localhost:8000` and use `adb reverse tcp:8000 tcp:8000` to connect your device.
