/**
 * @file arduino_task.h
 * @brief Arduino UART communication task interface
 *
 * Public interface for the Arduino UART communication task that handles
 * bidirectional communication with the Arduino ESP32 module.
 */

#pragma once

#include "FreeRTOS.h"

/**
 * @brief Initialize the Arduino UART communication task
 *
 * Creates and starts the Arduino task which handles UART communication
 * with the Arduino ESP32 module according to the PlantPal UART protocol.
 *
 * The task operates in polling mode, continuously checking for incoming
 * commands and sending appropriate responses.
 *
 * @return pdPASS if task was created successfully, pdFAIL otherwise
 */
BaseType_t xArduinoTaskInit(UBaseType_t priority);

/**
 * @brief Post a moisture message to the Arduino task
 */

BaseType_t xArduinoPostMoistureMessage(u8 percent);
