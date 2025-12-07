#pragma once

#include "FreeRTOS.h"
#include "task.h"
#include "xstatus.h"

/**
 * @brief Creates and starts the ADC testing FreeRTOS task.
 * * This task initializes the Nexys4IO and ADS1115, then continuously 
 * reads the moisture level and updates the serial monitor and 7-segment display.
 * * @return pdPASS if the task was created successfully, otherwise pdFAIL.
 */
BaseType_t xAdcTestTaskInit(void);