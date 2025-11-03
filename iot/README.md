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

*(TODO This section is a placeholder. We will add FPGA details once complete.)*


### ESP32 (WiFi Bridge)

The ESP32's sole responsibility is to act as a bridge. It listens for data from the FPGA, connects to WiFi, and forwards the data to the MQTT broker.

**Requirements:**

  * **Framework:** Arduino
  * **Libraries:**
    - `pubsubclient` for MQTT
    - `HardwareSerial` for FPGA communication

**Configuration:**
Your ESP32 code will need to be configured with:

  * **Hardware Interface:** UART/SPI/I2C pins for communicating with the FPGA.
  * **Wi-Fi Credentials:** SSID & Password.
  * **HiveMQ Broker:** Address (e.g., `your-hivemq-broker.com` or an IP) and Port (`1883`).
  * **MQTT Topic:** (e.g., `plantpal/telemetry/soil`).

-----

## Manual Testing Flow (CLI)

Before integrating the hardware, you **must** test the data pipeline. This flow lets you simulate the ESP32 to ensure the broker and cloud bridge are working perfectly.

### Prerequisite: Avoid Port Conflicts

If you have a local `mosquitto` broker running as a service (common on macOS with Homebrew), stop it to free up port `1883` for HiveMQ.

```bash
brew services stop mosquitto
```

### Part 1: Test the Local MQTT Broker

This test confirms that your HiveMQ broker is running and can handle messages. You will simulate both the ESP32 (publisher) and your backend (subscriber).

**Step 1. Open two terminal windows.**

  * **Terminal 1** will be your **Subscriber** (listening for messages).
  * **Terminal 2** will be your **Publisher** (simulating the ESP32).

**Step 2. (In Terminal 1) Start the Subscriber.**
This command subscribes to `test/topic` and will print any messages it receives. The `-v` (verbose) flag prints the topic and the payload.

Using `mosquitto_sub`:

```bash
mosquitto_sub -h localhost -p 1883 -t 'test/topic' -v
```

*Or using `mqtt-cli`:*

```bash
mqtt sub -h localhost -p 1883 -t 'test/topic' -v
```

> You will see no output yet. It is now waiting for a message.

**Step 3. (In Terminal 2) Publish a Test Message.**
This command sends the message `hello-hivemq` to the `test/topic`. This is what your ESP32 will do.

Using `mosquitto_pub`:

```bash
mosquitto_pub -h localhost -p 1883 -t 'test/topic' -m 'hello-hivemq'
```

*Or using `mqtt-cli`:*

```bash
mqtt pub -h localhost -p 1883 -t 'test/topic' -m 'hello-hivemq'
```

**Step 4. Verify the Result.**
Instantly, in **Terminal 1**, you should see the message appear:

```
test/topic hello-hivemq
```

If you see this, your local broker is working\!

### Part 2: Test the Full Cloud Bridge

Now you'll test the entire flow, from MQTT all the way to Google Pub/Sub.

**Step 1. Start your Subscriber (Terminal 1).**
This time, **subscribe to the topic defined in your `config.xml`**, the one that HiveMQ is configured to bridge to the cloud.

```bash
# Use the *actual* topic your ESP32 will publish to
mosquitto_sub -h localhost -p 1883 -t 'plantpal/telemetry/soil' -v
```

**Step 2. Publish a Valid JSON Message (Terminal 2).**
Simulate the ESP32 by sending a message that matches your Avro schema.

```bash
mosquitto_pub -h localhost -p 1883 -t 'plantpal/telemetry/soil' -m '{"percentage": 42.5, "timestamp": 1697666103000}'
```

**Step 3. Check Google Cloud Pub/Sub.**

  * Go to the Google Cloud Console -\> Pub/Sub -\> Subscriptions.
  * Find the subscription for your topic.
  * Click "View Messages" and "Pull".
  * You should see the JSON payload you just published.

If you see the message in Google Cloud, your entire data pipeline is confirmed to be working. You can now confidently flash your ESP32.
