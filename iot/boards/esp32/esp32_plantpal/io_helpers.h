#pragma once

#include "config.h"
#include <Arduino.h>
#include <ArduinoJson.h>
#include <MQTT.h>
#include "../../../../common/plant_pal_uart_protocol.h"

void messageReceived(String &topic, String &payload, Stream &uart, MQTTClient &mqttClient);
String formatMoistureTopicPayload(double moisturePercent, unsigned long timestamp);

// UART helper functions
bool sendUartCommand(Stream &uart, uint8_t command, const uint8_t* payload, uint8_t length);
bool receiveUartResponse(Stream &uart, uint8_t &command, uint8_t* payload, uint8_t &length, unsigned long timeout_ms);
