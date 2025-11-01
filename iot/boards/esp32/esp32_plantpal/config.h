/*
 * PlantPal ESP32 Configuration
 *
 * Update these values for your local setup
 */

#pragma once

// ============================================
// MQTT Broker Configuration
// ============================================
// IMPORTANT: Use your computer's local IP address
// Find it with:
//   Mac/Linux: ifconfig | grep "inet "
//   Windows:   ipconfig
//
// Example: "192.168.1.123" or "10.0.0.45"
#define MQTT_BROKER_HOST    "10.0.0.25"
#define MQTT_BROKER_PORT    1883

// MQTT Client ID (should be unique per device)
#define MQTT_CLIENT_ID      "PlantPalESP32"

// ============================================
// MQTT Topics
// IMPORTANT: Update to these topics will need to be updated in
// your HiveMQ Google Pub/Sub extension configuration as well.
// ============================================
#define SUB_MOISTURE_TOPIC      "plantpal/request_soil"
#define SUB_WATER_TOPIC         "plantpal/request_water"
#define PUB_TELEMETRY_TOPIC     "plantpal/data/moisture"
#define PUB_STATUS_TOPIC        "plantpal/status"

// ============================================
// Hardware Pin Definitions
// ============================================

// Future UART pins for FPGA communication:
// #define UART_RX_PIN      16
// #define UART_TX_PIN      17
// #define UART_BAUD_RATE   115200

// ============================================
// Timing Configuration
// ============================================
//#define PUBLISH_INTERVAL_MS     5000    // Publish every 5 seconds
//#define MQTT_KEEPALIVE_SEC      10      // MQTT keep-alive interval
//#define RECONNECT_DELAY_MS      5000    // Wait between reconnection attempts

// ============================================
// Debug Settings
// ============================================
#define SERIAL_BAUD_RATE        115200
#define ENABLE_DEBUG_OUTPUT // Comment out to disable debug logs


#ifdef ENABLE_DEBUG_OUTPUT
    #define DEBUG_LOG(tag, msg) Serial.print("["); Serial.print(tag); Serial.print("] "); Serial.println(msg);
#else
    #define DEBUG_LOG(tag, msg) // No-op when debug output is disabled
#endif
