#pragma once

#include "config.h"
#include <Arduino.h>
#include <ArduinoJson.h>
#include <MQTT.h>


void messageReceived(String &topic, String &payload);
String formatMoistureTopicPayload(double moisturePercent, unsigned long timestamp);
