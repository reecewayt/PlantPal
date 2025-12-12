#include "sensor_task.h"
#include "arduino_task.h"
#include "ads1115.h"     
#include "nexys4IO.h"    
#include "xparameters.h" 
#include "FreeRTOS.h"
#include "task.h"

// Configuration for logging and task
#define LOG_TAG "SENSOR_TASK"
#include "logging.h"

// Task handle for external signaling
static TaskHandle_t xSensorTaskHandle = NULL;

// Global reading value, holds the latest moisture percentage (0-100)
typedef struct {
    volatile u8 moisture_percent;
    volatile bool valid; 
} MoistureReading_t;

static MoistureReading_t global_moisture_reading = {0, false};

/**
 * @brief Helper function to display a 0-100 percentage on the 7-segment display.
 * Displays the value right-justified on digits 0, 1, and 2.
 * @param percent The moisture percentage (0-100).
 */
static void DisplayMoisture(u8 percent) {
    u8 hundreds, tens, ones; 
    u8 blank = CC_BLANK; // Character code for blank

    if (percent > 99) {
        hundreds = 1; // Clamp to max 100%
        tens = 0;
        ones = 0;
    } else {
        hundreds = 0;
        tens = (percent % 100) / 10;
        ones = percent % 10;
    }
    
    NX410_SSEG_setAllDigits(SSEGLO, blank, hundreds, tens, ones, DP_NONE);
}


/**
 * @brief Main sensor task for reading moisture and responding to signals.
 * 
 * This task runs every 1 second to read moisture data.
 * It reads the ADC, updates the display, and updates the global reading value.
 * 
 * @param pvParameters Unused.
 */
static void prvSensorTask(void *pvParameters) {
    (void)pvParameters;
    int Status;
    u8 moisture_percent;

    
    // Initialize the ADS1115 I2C peripheral
    DEBUG_PRINT("Initializing ADS1115 I2C peripheral...\r\n");
    Status = ADS1115_Init();
    if (Status != XST_SUCCESS) {
        DEBUG_PRINT("ERROR: ADS1115 I2C initialization failed. Check ADS_I2C_BASE in ads1115.c.\r\n");
        global_moisture_reading.valid = false;
        vTaskDelete(NULL);
        return;
    }
    DEBUG_PRINT("ADS1115 I2C initialized. Sensor task running every 1 second.\r\n");
    
    // Main sensor read loop - runs every 1 second
    while (1) {
        // Read the moisture percentage (0-100)
        moisture_percent = ADS1115_GetMoisturePercent();

        // Update global reading with valid flag
        global_moisture_reading.moisture_percent = moisture_percent;
        global_moisture_reading.valid = true;

        // Serial monitor print
        DEBUG_PRINT("Moisture: %d %%\r\n", moisture_percent);
        
        // Update 7-segment display
        DisplayMoisture(moisture_percent);
        
        // Wait 1 second before next reading
        vTaskDelay(pdMS_TO_TICKS(1000));
    }
}


/**
 * @brief Creates the sensor FreeRTOS task.
 */
BaseType_t xSensorTaskInit(UBaseType_t priority) {
    BaseType_t xTaskStatus;
    
    DEBUG_PRINT("Initializing Sensor task...\r\n");
    
    xTaskStatus = xTaskCreate(
        prvSensorTask, 
        "SensorTask", 
        configMINIMAL_STACK_SIZE * 2, 
        NULL, 
        priority, 
        &xSensorTaskHandle
    );
    
    if (xTaskStatus == pdPASS) {
        DEBUG_PRINT("Sensor task created successfully\r\n");
    } else {
        DEBUG_PRINT("ERROR: Failed to create sensor task\r\n");
    }

    return xTaskStatus;
}

/**
 * @brief Signal the sensor task to perform a moisture reading.
 * 
 * Sends a task notification to wake up the sensor task.
 * 
 * @return pdPASS if notification was sent successfully, pdFAIL otherwise.
 */
BaseType_t xSensorTaskSignalRead(void) {
    if (xSensorTaskHandle != NULL) {
        xTaskNotifyGive(xSensorTaskHandle);
        return pdPASS;
    }
    return pdFAIL;
}

/**
 * @brief Get the latest moisture reading.
 * 
 * @param latestReading Pointer to store the latest moisture percentage.
 * @return true if a valid reading is available, false otherwise.
 */
bool SensorGetLatestReading(u8* latestReading) {
    if (latestReading != NULL && global_moisture_reading.valid) {
        *latestReading = global_moisture_reading.moisture_percent;
        return true;
    }
    return false;
}