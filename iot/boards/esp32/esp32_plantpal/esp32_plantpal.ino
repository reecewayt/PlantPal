/*
 * PlantPal ESP32 - MQTT to UART Bridge
 * Handles MQTT messages and communicates with FPGA over UART
 */

#include <WiFi.h>
#include <MQTT.h>
#include "config.h"
#include "secrets.h"
#include "mqtt_service.h"
#include "../include/uart_protocol.h"


const String TAG = "MAIN";

WiFiClient net;
MQTTClient client;

// UART Serial (Serial2 on ESP32)
HardwareSerial uartSerial(2);

// Status check interval (60 seconds)
const unsigned long STATUS_CHECK_INTERVAL_MS = 60000UL;
unsigned long _lastStatusCheckMs = 0;

void connect() {
  DEBUG_LOG(TAG, "Checking WiFi...");
  while (WiFi.status() != WL_CONNECTED) {
    DEBUG_LOG(TAG, ".");
    delay(1000);
  }

  DEBUG_LOG(TAG, String("\nConnecting to MQTT broker at ") + MQTT_BROKER_HOST + ":" + String(MQTT_BROKER_PORT) + "...");

  while (!client.connect(MQTT_CLIENT_ID)) {
    DEBUG_LOG(TAG, ".");
    delay(1000);
  }

  DEBUG_LOG(TAG, "\nMQTT connected!");

  client.subscribe(SUB_MOISTURE_TOPIC);
  DEBUG_LOG(TAG, String("Subscribed to: ") + SUB_MOISTURE_TOPIC);
  client.subscribe(SUB_WATER_TOPIC);
  DEBUG_LOG(TAG, String("Subscribed to: ") + SUB_WATER_TOPIC);

}

void setup() {
  Serial.begin(SERIAL_BAUD_RATE);
  delay(2000);

  DEBUG_LOG(TAG, "\n=== PlantPal ESP32 MQTT-UART Bridge ===");
  DEBUG_LOG(TAG, "Config:");
  DEBUG_LOG(TAG, String("  WiFi SSID: ") + WIFI_SSID);
  DEBUG_LOG(TAG, String("  MQTT Broker: ") + MQTT_BROKER_HOST + ":" + String(MQTT_BROKER_PORT));
  DEBUG_LOG(TAG, String("  UART: RX=") + String(UART_RX_PIN) + " TX=" + String(UART_TX_PIN) + " Baud=" + String(UART_BAUD_RATE));
  DEBUG_LOG(TAG, "");

  // Initialize UART for FPGA communication
  uartSerial.begin(UART_BAUD_RATE, SERIAL_8N1, UART_RX_PIN, UART_TX_PIN);
  DEBUG_LOG(TAG, "UART initialized");

  // Connect to WiFi
  DEBUG_LOG(TAG, String("Connecting to WiFi: ") + WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  // Initialize MQTT client
  client.begin(MQTT_BROKER_HOST, MQTT_BROKER_PORT, net);
  client.onMessage([](String &topic, String &payload) {
    messageReceived(topic, payload, uartSerial, client);
  });

  connect();

  DEBUG_LOG(TAG, "\n=== Setup Complete ===");
}

void loop() {
  client.loop();

  // Reconnect if connection lost
  if (!client.connected()) {
    DEBUG_LOG(TAG, "\nConnection lost! Reconnecting...");
    connect();
  }

  // Periodic status check with FPGA (every 60 seconds)
  if (client.connected()) {
    unsigned long now = millis();
    if (now - _lastStatusCheckMs >= STATUS_CHECK_INTERVAL_MS) {
      _lastStatusCheckMs = now;

      DEBUG_LOG(TAG, "Requesting FPGA status...");

      // Send status request to FPGA
      sendUartCommand(uartSerial, CMD_STATUS_REQUEST, nullptr, 0);

      // Wait for response
      uint8_t responseCmd;
      uint8_t responsePayload[UART_MAX_PAYLOAD_SIZE];
      uint8_t responseLen;

      if (receiveUartResponse(uartSerial, responseCmd, responsePayload, responseLen, 2000)) {
        if (responseCmd == RESP_STATUS_OK) {
          DEBUG_LOG(TAG, "FPGA status: OK");

          // Publish status to MQTT
          String statusPayload = "{\"status\":\"online\",\"timestamp\":" + String(millis()) + "}";
          client.publish(PUB_STATUS_TOPIC, statusPayload.c_str());
        } else {
          DEBUG_LOG(TAG, "FPGA status: Unexpected response");
        }
      } else {
        DEBUG_LOG(TAG, "FPGA status: No response (timeout)");
        String statusPayload = "{\"status\":\"offline\",\"timestamp\":" + String(millis()) + "}";
        client.publish(PUB_STATUS_TOPIC, statusPayload.c_str());
      }
    }
  }
}
