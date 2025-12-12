# PlantPal: An FPGA & ESP32 IoT System

This project is an IoT solution for monitoring plant soil moisture. An **FPGA** is responsible for interfacing with and reading sensor data. It passes this data to an **ESP32**, which acts as a WiFi-to-MQTT bridge. The ESP32 formats the data as a JSON payload and publishes it to a HiveMQ broker. From there, a Google Cloud Pub/Sub extension forwards the data to a Firebase backend for storage and analysis.

## Architecture Overview

The flow of data in this project is as follows:

1.  **Sensors & FPGA:** A soil moisture sensor is connected to the FPGA, which handles the low-level signal processing and data acquisition.
2.  **FPGA-to-ESP32:** The FPGA sends the processed sensor data to the ESP32 via a serial connection (e.g., UART, SPI, or I2C).
3.  **ESP32 (WiFi Bridge):** The ESP32 receives the serial data, connects to the local WiFi, formats the data into the correct JSON schema, and publishes it to an MQTT topic.
4.  **HiveMQ Broker:** A local or cloud-hosted HiveMQ broker receives the MQTT message.
5.  **HiveMQ Pub/Sub Extension:** The "HiveMQ Enterprise Extension for Google Cloud Pub/Sub" forwards the message to a Google Cloud Pub/Sub topic.
6.  **Google Cloud Pub/Sub:** This acts as the scalable messaging bridge to the cloud.
7.  **Firebase Backend:** A service (like a Cloud Function) subscribes to the Pub/Sub topic, processes the data, and saves it to Firebase (e.g., Firestore).

-----

## Backend & Broker Setup

### 1\. HiveMQ & Google Cloud Pub/Sub

This project requires **HiveMQ Enterprise Edition** and the **Extension for Google Cloud Pub/Sub** to act as the bridge.

**Requirements:**

  * Follow the official guide: [**HiveMQ Extension for Google Cloud Pub/Sub**](https://docs.hivemq.com/hivemq-google-cloud-pubsub-extension/latest/index.html).
  * This guide will walk you through setting up HiveMQ, configuring Google Cloud credentials, and enabling the extension.

> **Configuration Note:**
> The guide will mention a `config.xml` file. This project includes a template to help you get started. You can find it in `/iot/hivemq/config-template.xml`. You will need to fill in your specific Google Cloud project details.

### 2\. Data Schema (Google Pub/Sub)

Google Cloud Pub/Sub can enforce a schema on topics to ensure data integrity. You must define the following Avro schema in your Pub/Sub topic configuration.

**Schema Definition (`PlantPalSoilTelemtry`):**

```json
{
  "type": "record",
  "name": "PlantPalSoilTelemtry",
  "fields": [
    {
      "name": "percentage",
      "type": "double"
    },
    {
      "name": "timestamp",
      "type": "long",
      "logicalType": "timestamp-millis"
    }
  ]
}
```

**Example Valid Message:**
This is the JSON format the ESP32 should publish.

```json
{
  "percentage": 42.5,
  "timestamp": 1697666103000
}
```

-----

## Embedded (FPGA & ESP32)

### FPGA

The FPGA is responsible for reading the soil moisture sensor and sending the data to the ESP32. The implementation details for our FPGA (Nexys 4 and Nexys A7) can be found in the `/fpga` directory.


### ESP32 (WiFi Bridge)

The ESP32's sole responsibility is to act as a bridge. It listens for data from the FPGA, connects to WiFi, and forwards the data to the MQTT broker. You can easily adapt the provided code for other microcontrollers with WiFi capabilities, change the MQTT broker, or modify the topics for your use case. 