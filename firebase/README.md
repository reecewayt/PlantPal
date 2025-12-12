# PlantPal — Firebase Functions (Python)

This repository the PlantPal backend: a simple chat-bot agent (see `agent.py`) with helper utilities (`iot_tools.py`) and a Python Cloud Function wrapper (`main.py`) so an Android app can talk to the agent through Firebase Callable Functions. The agent implementation is intentionally minimal — it exposes a chat API that the callable function `plantpal_chat` invokes and returns a JSON response { "response": ..., "success": true }.

## Prerequisites

Before working with this project, you should be familiar with the following technologies and services:

### Firebase Services
- **Firebase Functions** - Serverless functions for backend logic (Python runtime)
- **Cloud Firestore** - NoSQL database for storing sensor data
- **Pub/Sub** - Message queue for IoT device communication
- **Firebase Authentication** - User authentication (optional)
- **Firebase Emulator Suite** - Local testing environment

### LangChain & AI Tools
- **LangChain** - Framework for building LLM-powered applications
- **LangGraph** - State machine framework for agent workflows
- **LangSmith** - Observability and tracing for LangChain applications (optional)
- **Tool calling** - LLM function/tool invocation patterns

### Google Cloud Platform
- **Google Cloud Pub/Sub** - Message publishing and subscription service
- **Cloud Functions** - Serverless compute platform
- **Firestore** - Document database
- **IAM & Authentication** - Service account management

### Development Tools
- Python 3.13+
- Firebase CLI (`npm install -g firebase-tools`)
- Virtual environments (`venv`)
- Git for version control

For firebase functionality, its important to test locally. This documentation assumes you're familiar with firebase. Run the emulator locally before deployment.
```
firebase emulators:start
```

Connecting the Android app to the local emulator
- The android code comes with a couple of lines that need to be called from `MainActivity.kt` when using the local emulator.
- See `android/app/src/main/java/com/example/plantpal/MainActivity.kt`
```kotlin
private fun configFirebaseServices() {
        if(BuildConfig.DEBUG){
            Firebase.auth.useEmulator(LOCALHOST, AUTH_PORT)
            Firebase.functions.useEmulator(LOCALHOST, FIREBASE_FUNCTIONS_PORT)
        }
    }
```

- If you have connection issues, ensure the following
  - Add the network security config file at:
    `PlantPal/android/app/src/main/res/xml/network_security_config.xml`
    (whitelist local HTTP traffic so the emulator can reach your functions).



## Important
This app uses several api keys. Be sure you have properly set them up in your personal `.env` file.
A template has been provided as reference.

   **Environment variables**
   ```bash
   export OPENAI_API_KEY="sk-..."
   export TAVILY_API_KEY="tvly-..."
   export LANGSMITH_API_KEY="ls-..."  # Optional
   ```

