#include "mqtt_service.h"

const String TAG = "MQTT_SERVICE";

void messageReceived(String &topic, String &payload) {

    // `topic` is an Arduino String, which can't be used in a switch statement.
    // Use if/else string comparisons instead.
    if (topic == SUB_MOISTURE_TOPIC) {
        DEBUG_LOG(TAG, "Received moisture sensor reading request");
        DEBUG_LOG(TAG, payload);
        // TODO: Handle moisture sensor request
    }
    else if (topic == SUB_WATER_TOPIC) {
        DEBUG_LOG(TAG, "Received water plant request");
        DEBUG_LOG(TAG, payload);
        // TODO: Handle water level request
    }
    else {
        // Unknown topic
        // TODO: Handle unknown topic
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
    // Use a DynamicJsonDocument with a small buffer to avoid template issues
    DynamicJsonDocument doc(128);
    doc["percentage"] = moisturePercent;
    doc["timestamp"] = timestamp;
    String output;
    serializeJson(doc, output);
    return output;
}
