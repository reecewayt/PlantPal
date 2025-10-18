/*
 * PlantPal ESP32 Configuration
 *
 * Update these values for your local setup, and save in config.h. Some of these configs
 * contain sensitive information (e.g. WiFi password) so do not commit config.h to version control.
 * To get started, copy config_example.h to config.h and edit the values as needed.
 */

#ifndef CONFIG_H
#define CONFIG_H

// ============================================
// WiFi Configuration
// ============================================
#define WIFI_SSID       "YOUR_WIFI_SSID"
#define WIFI_PASSWORD   "YOUR_WIFI_PASSWORD"

// ============================================
// MQTT Broker Configuration
// ============================================
// IMPORTANT: Use your computer's local IP address
// Find it with:
//   Mac/Linux: ifconfig | grep "inet "
//   Windows:   ipconfig
//
// Example: "192.168.1.123" or "10.0.0.45"
#define MQTT_BROKER_HOST    "192.168.1.XXX"
#define MQTT_BROKER_PORT    1883

// MQTT Client ID (should be unique per device)
#define MQTT_CLIENT_ID      "PlantPalESP32"

// ============================================
// MQTT Topics
// ============================================
#define TOPIC_PUBLISH       "plantpal/hello"
#define TOPIC_SUBSCRIBE     "plantpal/command"

// Future topics for full implementation:
// #define TOPIC_SENSORS    "plantpal/sensors"
// #define TOPIC_STATUS     "plantpal/status"
// #define TOPIC_ERRORS     "plantpal/errors"



// Future UART pins for FPGA communication:
// #define UART_RX_PIN      16
// #define UART_TX_PIN      17
// #define UART_BAUD_RATE   115200

// ============================================
// Timing Configuration
// ============================================
#define PUBLISH_INTERVAL_MS     5000    // Publish every 5 seconds
#define MQTT_KEEPALIVE_SEC      10      // MQTT keep-alive interval
#define RECONNECT_DELAY_MS      5000    // Wait between reconnection attempts

// ============================================
// Debug Settings
// ============================================
#define SERIAL_BAUD_RATE        115200
#define ENABLE_DEBUG_OUTPUT     true

#endif // CONFIG_H
