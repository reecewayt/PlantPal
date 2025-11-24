#include "mqtt_service.h"

const String TAG = "MQTT_SERVICE";

void messageReceived(String &topic, String &payload, HardwareSerial &uart, MQTTClient &mqttClient) {
    if (topic == SUB_MOISTURE_TOPIC) {
        DEBUG_LOG(TAG, "Received moisture sensor reading request");

        // Send moisture read command to FPGA
        sendUartCommand(uart, CMD_READ_MOISTURE, nullptr, 0);

        // Wait for response
        uint8_t responseCmd;
        uint8_t responsePayload[UART_MAX_PAYLOAD_SIZE];
        uint8_t responseLen;

        if (receiveUartResponse(uart, responseCmd, responsePayload, responseLen, 2000)) {
            if (responseCmd == RESP_MOISTURE_DATA && responseLen >= 1) {
                MoistureDataPayload* data = (MoistureDataPayload*)responsePayload;
                uint8_t moisturePercent = data->moisture_percent;

                DEBUG_LOG(TAG, String("Moisture reading: ") + String(moisturePercent) + "%");

                // Publish to MQTT
                String mqttPayload = formatMoistureTopicPayload(moisturePercent, millis());
                mqttClient.publish(PUB_TELEMETRY_TOPIC, mqttPayload.c_str());
                DEBUG_LOG(TAG, String("Published: ") + mqttPayload);
            } else {
                DEBUG_LOG(TAG, "Invalid moisture response from FPGA");
            }
        } else {
            DEBUG_LOG(TAG, "No response from FPGA (timeout)");
        }
    }
    else if (topic == SUB_WATER_TOPIC) {
        DEBUG_LOG(TAG, "Received water plant request");
        DEBUG_LOG(TAG, String("Payload: ") + payload);

        // Parse duration from JSON payload
        DynamicJsonDocument doc(128);
        DeserializationError error = deserializeJson(doc, payload);

        if (error) {
            DEBUG_LOG(TAG, String("JSON parse error: ") + error.c_str());
            return;
        }

        uint16_t duration = doc["duration"] | 10;  // Default 10 seconds if not specified
        DEBUG_LOG(TAG, String("Water duration: ") + String(duration) + "s");

        // Build water command payload
        WaterCommandPayload waterPayload;
        waterPayload.duration_seconds = duration;

        // Send water command to FPGA
        sendUartCommand(uart, CMD_WATER_ON, (uint8_t*)&waterPayload, sizeof(waterPayload));

        // Wait for acknowledgment
        uint8_t responseCmd;
        uint8_t responsePayload[UART_MAX_PAYLOAD_SIZE];
        uint8_t responseLen;

        if (receiveUartResponse(uart, responseCmd, responsePayload, responseLen, 2000)) {
            if (responseCmd == RESP_WATER_ACK) {
                DEBUG_LOG(TAG, "Water command acknowledged by FPGA");
            } else {
                DEBUG_LOG(TAG, "Unexpected response from FPGA");
            }
        } else {
            DEBUG_LOG(TAG, "No acknowledgment from FPGA (timeout)");
        }
    }
    else {
        DEBUG_LOG(TAG, String("Received message on unknown topic: ") + topic);
        DEBUG_LOG(TAG, payload);
    }
}

/**
 * @brief Moisture topic payload maps to a google pub/sub topic which has a
 * specific JSON structure. In this case, we need to format the payload accordingly.
 * See /schemas/moisture-data.json for the expected structure.
 *
 * @param moisturePercent
 * @param timestamp
 * @return Serialized JSON String
 */
String formatMoistureTopicPayload(double moisturePercent, unsigned long timestamp) {
    DynamicJsonDocument doc(128);
    doc["percentage"] = moisturePercent;
    doc["timestamp"] = timestamp;
    String output;
    serializeJson(doc, output);
    return output;
}

/**
 * @brief Send UART command to FPGA
 * Frame format: [COMMAND][LENGTH][PAYLOAD...]
 */
bool sendUartCommand(HardwareSerial &uart, uint8_t command, const uint8_t* payload, uint8_t length) {
    if (length > UART_MAX_PAYLOAD_SIZE) {
        return false;
    }

    uart.write(command);
    uart.write(length);

    if (payload != nullptr && length > 0) {
        uart.write(payload, length);
    }

    return true;
}

/**
 * @brief Receive UART response from FPGA
 * Frame format: [COMMAND][LENGTH][PAYLOAD...]
 */
bool receiveUartResponse(HardwareSerial &uart, uint8_t &command, uint8_t* payload, uint8_t &length, unsigned long timeout_ms) {
    unsigned long startTime = millis();

    // Wait for command byte
    while (!uart.available()) {
        if (millis() - startTime > timeout_ms) {
            return false;
        }
        delay(1);
    }
    command = uart.read();

    // Wait for length byte
    startTime = millis();
    while (!uart.available()) {
        if (millis() - startTime > timeout_ms) {
            return false;
        }
        delay(1);
    }
    length = uart.read();

    // Read payload if length > 0
    if (length > 0) {
        if (length > UART_MAX_PAYLOAD_SIZE) {
            return false;
        }

        uint8_t bytesRead = 0;
        startTime = millis();

        while (bytesRead < length) {
            if (uart.available()) {
                payload[bytesRead++] = uart.read();
                startTime = millis();  // Reset timeout on each byte
            }

            if (millis() - startTime > timeout_ms) {
                return false;
            }
            delay(1);
        }
    }

    return true;
}
