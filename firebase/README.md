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
- The android code comes with a couple of lines that can be commented in or out depending on if you're using the local emulator for testing.
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


# Testing
Testing the functionality of firebase was achieve mostly through manual tests by running the emulator and check appropriate responses. Where appropriate, integration and unit tests can be found in `firebase/functions/tests/` directory.


## Important
This app uses several api keys. Be sure you have properly set them up in your personal `.env` file.
A template has been provided as reference.

   **Environment variables**
   ```bash
   export OPENAI_API_KEY="sk-..."
   export TAVILY_API_KEY="tvly-..."
   export LANGSMITH_API_KEY="ls-..."  # Optional
   ```

## Note on testing Pub/Sub locally
When running local emulator suite, Pub/Sub functions can be trigger with commands using a curl POST from the terminal to the port it has been started on. For example:
```bash
curl -X POST "http://localhost:8085/v1/projects/plantpal-f1bfa/topics/data-moisture:publish" \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [{
      "data": "eyJwZXJjZW50YWdlIjogNDUuNSwgInRpbWVzdGFtcCI6IDE3MzIzOTY4MDAwMDB9"
    }]
  }'
```
