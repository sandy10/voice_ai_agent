# MoodLens 🔮

**MoodLens** is a voice-first mood journaling application and emotional companion built on top of the Agora Conversational AI platform. Instead of typing out how you feel, simply talk about your day with **Luna**, your empathetic AI companion. As you speak, the app listens, analyzes your emotions, and visualizes your mood in real-time.

---

## ✨ Features

### 🎙️ Conversational AI Companion (Luna)
- **Natural Voice Interaction:** Have fluid, uninterrupted, low-latency conversations using Agora's Real-Time Communication (RTC) framework.
- **Empathetic Persona:** The backend is powered by a customized LLM system prompt designed specifically for reflective journaling and active listening.
- **Context Awareness:** Luna remembers your emotional state from the start of the session to help guide the conversation constructively.

### 🧠 Real-Time Mood Analysis Engine
- **Live Sentiment Tracking:** The app locally analyzes the ongoing transcript, mapping your spoken words to five core emotional dimensions: 
  - 🟡 **Joy**
  - 🔵 **Calm**
  - 🟢 **Energy**
  - 🔴 **Stress**
  - 🟣 **Sadness**
- **Dynamic Scoring:** Uses Exponential Moving Average (EMA) and sigmoid normalization to create smooth, flowing transitions in your mood scores as your mood shifts during the conversation.

### 🎨 Immersive UI Visualizations (Jetpack Compose)
- **Animated Mood Ring:** A custom Compose Canvas component featuring concentric rings that pulse and glow. The sizes and colors of the rings shift in real-time to reflect your dominant emotions.
- **Dimension Bars:** Live progress bars that break down the exact percentages of your emotional landscape during the session.
- **Live Transcript:** A beautifully styled chat interface that displays exactly what you and Luna are saying in real-time.

### 📅 Local Journaling & History Tracker
- **Automatic Saving:** At the end of every session, your dominant mood and a summary of your transcript are securely saved to your local device.
- **7-Day Timeline:** A visually rich, horizontally scrollable timeline showing your mini mood-rings from the past week at a glance.
- **No Cloud Database Required:** All user history is strictly saved locally on the Android device using encrypted SharedPreferences.

---

## 🛠️ Tech Stack

**Client (Android):**
- 100% Kotlin
- UI: Jetpack Compose (Material 3)
- Real-time Audio/Video: Agora RTC & RTM SDKs
- State Management: StateFlow / ViewModel

**Server (Backend):**
- Python 3 / FastAPI
- Agora TEN Framework (Token generation & Agent Orchestration)
- Speech-to-Text: Nova-3
- LLM: GPT-4o-mini
- Text-to-Speech: Speech 2.6 Turbo

---

## 🚀 How to Run Locally

### 1. Start the Python Backend
1. Go to the `server/` directory.
2. Activate your virtual environment and install dependencies (`pip install -r requirements.txt`).
3. Set your Agora credentials in the `.env` file.
4. Run the server: 
   ```bash
   python -m uvicorn app.main:app --reload --host 127.0.0.1
   ```

### 2. Connect Your Device
If using a physical Android device plugged in via USB, use ADB reverse to securely pipe the server to your phone without exposing it to the internet:
```bash
adb reverse tcp:8000 tcp:8000
```

### 3. Build the Android App
Ensure your `local.properties` file in the Android root directory contains:
```properties
QUICKSTART_SERVER_URL=http://localhost:8000
```
Then build and install the app to your device:
```bash
./gradlew installDebug
```
