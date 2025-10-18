/*
 * PlantPal ESP32 - MQTT Subscription Test
 *
 * Simple test to verify ESP32 can receive MQTT messages
 * Subscribes to "test/topic" and prints received messages to Serial Monitor
 *
 * Hardware: ESP32-DevKitC or similar
 * MQTT Broker: HiveMQ running on 10.0.0.25:1883
 *
 * SETUP:
 * 1. Update config.h with your WiFi credentials and MQTT broker IP
 * 2. Install "MQTT" library by Joel Gaehwiler (256dpi) via Library Manager
 * 3. Upload to ESP32 and open Serial Monitor (115200 baud)
 * 4. Send test message: mosquitto_pub -h 10.0.0.25 -p 1883 -t "test/topic" -m "Hello"
 */

#include <WiFi.h>
#include <MQTT.h>
#include "config.h"

// MQTT topic to subscribe to
const char TEST_TOPIC_SUB[] = "test/topic";

WiFiClient net;
MQTTClient client;

void connect() {
  Serial.print("Checking WiFi...");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(1000);
  }

  Serial.print("\nConnecting to MQTT broker at ");
  Serial.print(MQTT_BROKER_HOST);
  Serial.print(":");
  Serial.print(MQTT_BROKER_PORT);
  Serial.print("...");

  // Connect to HiveMQ broker
  // client.connect(clientID)
  while (!client.connect(MQTT_CLIENT_ID)) {
    Serial.print(".");
    delay(1000);
  }

  Serial.println("\nMQTT connected!");

  // Subscribe to test topic
  client.subscribe(TEST_TOPIC_SUB);
  Serial.print("Subscribed to: ");
  Serial.println(TEST_TOPIC_SUB);
}

void messageReceived(String &topic, String &payload) {
  Serial.println("\n=== MESSAGE RECEIVED ===");
  Serial.print("Topic: ");
  Serial.println(topic);
  Serial.print("Payload: ");
  Serial.println(payload);
  Serial.println("========================\n");

  // Blink LED to indicate message received
  digitalWrite(LED_BUILTIN, HIGH);
  delay(100);
  digitalWrite(LED_BUILTIN, LOW);
}

void setup() {
  Serial.begin(SERIAL_BAUD_RATE);

  // Wait for serial monitor
  delay(2000);

  Serial.println("\n=== PlantPal ESP32 - MQTT Subscribe Test ===");
  Serial.println("Config:");
  Serial.print("  WiFi SSID: ");
  Serial.println(WIFI_SSID);
  Serial.print("  MQTT Broker: ");
  Serial.print(MQTT_BROKER_HOST);
  Serial.print(":");
  Serial.println(MQTT_BROKER_PORT);
  Serial.print("  Subscribe Topic: ");
  Serial.println(TEST_TOPIC_SUB);
  Serial.println();

  // Setup LED
  pinMode(LED_BUILTIN, OUTPUT);
  digitalWrite(LED_BUILTIN, LOW);

  // Connect to WiFi
  Serial.print("Connecting to WiFi: ");
  Serial.println(WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  // Initialize MQTT client
  client.begin(MQTT_BROKER_HOST, MQTT_BROKER_PORT, net);
  client.onMessage(messageReceived);

  connect();

  Serial.println("\n=== Setup Complete ===");
  Serial.println("Listening for messages on: " + String(TEST_TOPIC_SUB));
  Serial.println("\nTest with:");
  Serial.print("  mosquitto_pub -h ");
  Serial.print(MQTT_BROKER_HOST);
  Serial.print(" -p ");
  Serial.print(MQTT_BROKER_PORT);
  Serial.print(" -t \"");
  Serial.print(TEST_TOPIC_SUB);
  Serial.println("\" -m \"Your message\"");
  Serial.println();
}

void loop() {
  client.loop();

  // Reconnect if connection lost
  if (!client.connected()) {
    Serial.println("\nConnection lost! Reconnecting...");
    connect();
  }
}
