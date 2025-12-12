#pragma once

#include "FreeRTOS.h"
#include "stdbool.h"


/**
 * @brief Creates and starts the sensor task for moisture monitoring.
 * 
 * This task initializes the Nexys4IO and ADS1115, then waits for notifications
 * to read moisture data. When signaled, it reads the moisture sensor, updates
 * the 7-segment display, and posts the data to the arduino task queue.
 * 
 * @return pdPASS if the task was created successfully, otherwise pdFAIL.
 */
BaseType_t xSensorTaskInit(UBaseType_t priority);

/**
 * @brief Signal the sensor task to perform a moisture reading.
 * 
 * Sends a task notification to wake up the sensor task and trigger a
 * moisture reading cycle.
 * 
 * @return pdPASS if notification was sent successfully, pdFAIL otherwise.
 */
BaseType_t xSensorTaskSignalRead(void);

/**
 * @brief Get the latest moisture reading.
 * 
 * Retrieves the most recent moisture percentage reading.
 * 
 * @param latestReading Pointer to a variable to store the latest reading.
 * @return true if a valid reading is available, false otherwise.
 */

bool SensorGetLatestReading(u8* latestReading);


