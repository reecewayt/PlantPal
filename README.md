# PlantPal

**An AI-Augmented IoT Plant Watering System**

PlantPal is an intelligent plant care system that combines FPGA-based sensor hardware, ESP32 IoT connectivity, and conversational AI to automate soil monitoring and watering. Users interact with the system through an Android app featuring a chatbot powered by LangChain agents hosted on Firebase.

---

## 🌱 Motivation

Traditional IoT plant monitoring systems provide raw sensor data but lack intelligent interaction. PlantPal addresses this by integrating **agentic AI capabilities** into a physical plant watering system:

- **Conversational Interface**: Users ask natural language questions like "How's my plant doing?" or "Water my plant for 5 seconds" instead of navigating complex dashboards
- **LangChain-Powered Agent**: The AI agent uses tool calling to query real-time sensor data from Firestore and send watering commands via Google Cloud Pub/Sub
- **Firebase Cloud Functions**: Serverless backend hosts the LangGraph agent, providing scalable AI-driven automation
- **End-to-End IoT Pipeline**: FPGA hardware reads soil moisture sensors, ESP32 bridges to MQTT/HiveMQ, data flows through Google Cloud Pub/Sub to Firebase, and the AI agent orchestrates it all

By combining embedded systems, cloud infrastructure, and modern AI frameworks, PlantPal demonstrates how agentic AI can augment IoT capabilities to create more intuitive and intelligent user experiences.

---

## 🚀 Getting Started

### Prerequisites

Before setting up PlantPal, you'll need:

1. **Google Cloud Platform Account**: [Sign up here](https://cloud.google.com/)
2. **Firebase Project**: [Create a Firebase project](https://console.firebase.google.com/)
3. **Android Studio**: For building and running the Android app

### Quick Start Guide

The easiest way to understand the system is through the **Android app UI**, which provides the conversational interface to interact with your plant.

#### 1. Set Up Firebase for Android

Follow the official Firebase Android setup guide to integrate Firebase with the app:  
👉 **[Add Firebase to your Android project](https://firebase.google.com/docs/android/setup)**

**Critical Step**: Download your `google-services.json` file from the Firebase Console and place it in:
```
android/app/google-services.json
```

Without this file, the Android app cannot communicate with Firebase services (Authentication, Cloud Functions, Firestore).

#### 2. Install Dependencies

- **Android**: Open `android/` in Android Studio and sync Gradle dependencies
- **Firebase Functions**: See [`firebase/README.md`](#firebase-backend) for Python environment setup
- **FPGA/IoT**: See [`fpga/README.md`](#fpga--embedded-hardware) and [`iot/README.md`](#iot-connectivity) for hardware requirements

#### 3. Run the App

1. Build and run the Android app on a device or emulator
2. Start the Firebase Emulator Suite for local testing (see [`firebase/README.md`](#firebase-backend))
3. Interact with the chatbot to monitor soil moisture and control watering

---

## 📂 Project Structure

This repository is organized into four main components:

### 🤖 [Firebase Backend](firebase/README.md)

The brain of PlantPal — a LangGraph-based AI agent deployed as Firebase Cloud Functions.

- **Agent Implementation**: LangChain/LangGraph agent with tool calling for Firestore queries and IoT commands
- **Cloud Functions**: Python-based serverless functions (`plantpal_chat`, Pub/Sub triggers)
- **Firestore Integration**: Stores historical sensor data for agent context
- **Testing**: Local emulator support and unit/integration tests

📖 **[Read the Firebase README →](firebase/README.md)**

---

### 🔌 [IoT Connectivity](iot/README.md)

The data pipeline connecting physical sensors to the cloud.

- **ESP32 WiFi Bridge**: Receives UART data from FPGA, publishes JSON to MQTT
- **HiveMQ Broker**: MQTT broker with Google Cloud Pub/Sub extension
- **Data Schema**: Avro schema for `PlantPalSoilTelemetry` messages
- **Architecture**: Detailed flow from sensor → FPGA → ESP32 → MQTT → Pub/Sub → Firebase

📖 **[Read the IoT README →](iot/README.md)**

---

### 🖥️ [FPGA & Embedded Hardware](fpga/README.md)

The hardware foundation — FPGA-based sensor interface with FreeRTOS software.

- **Vivado Project**: Hardware design for Nexys 4/7 boards (block design, HDL, constraints)
- **Vitis Software**: FreeRTOS application with sensor tasks, UART protocol, I2C drivers
- **Soil Moisture Sensing**: ADS1115 ADC driver for capacitive sensor
- **UART Protocol**: Communication with ESP32 for commands and data transfer
- **Hardware Platform**: Pre-built `.xsa` files for software-only development

📖 **[Read the FPGA README →](fpga/README.md)**

---

### 📱 Android App

The user-facing application providing a conversational UI for plant care.

- **Chat Interface**: Natural language interaction with the LangChain agent
- **Firebase SDK**: Authentication, Cloud Functions, and Firestore integration
- **Real-Time Data**: Displays current soil moisture and watering status
- **Local Testing**: Configurable emulator support for development

📖 **See `android/` directory for source code**
> Note: It is recommended to use Android Studio for building and running the app. When selecting the project, choose the `android/` folder in Android Studio.
---

## 🛠️ Development Workflow

1. **Hardware Setup**: Follow [`fpga/README.md`](fpga/README.md) and [`iot/README.md`](iot/README.md) to set up sensor hardware and ESP32
2. **Backend Development**: Use Firebase Emulator Suite (see [`firebase/README.md`](firebase/README.md)) for local testing
3. **Android Development**: Configure `google-services.json` and test against local or deployed Firebase
4. **Integration Testing**: Verify end-to-end flow from sensor reading → AI response

---

## 📜 License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

## 🤝 Contributing

This is an academic project developed for ECE 544. It is not actively maintained. However, all code has been tested and works at the time of writing. Feel free to fork and modify for personal use or learning!
