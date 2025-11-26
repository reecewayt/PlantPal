#include "FreeRTOS.h"
#include "task.h"

#include "config.h"
#include "xil_printf.h"

#define LOG_TAG "MAIN"
#include "logging.h"
#include "arduino_task.h"

/************************** Global Instances *********************************/


/************************** Function Prototypes ******************************/
static int PlatformInit(void);
static void prvPostStartupHook(void *pvParameters);


int main() {
    DEBUG_PRINT("Plant-Pal FPGA Application Starting...\r\n");

    // Create post-startup initialization task
    xTaskCreate(
        prvPostStartupHook, 
        "PostStartupHook", 
        configMINIMAL_STACK_SIZE * 2, 
        NULL, 
        tskIDLE_PRIORITY + 2, 
        NULL
    );

    // Start the FreeRTOS scheduler
    vTaskStartScheduler();

    // Should never reach here
    while (1) {
        // Empty loop
    }

    return XST_SUCCESS;
}

/*****************************************************************************/
/**
 * @brief Post-startup hook task
 *
 * Performs initialization after the FreeRTOS scheduler starts. This includes
 * platform hardware initialization and creating application tasks.
 *
 * @param pvParameters Task parameters (unused)
 */
static void prvPostStartupHook(void *pvParameters) {
    (void)pvParameters;
    int Status;
    BaseType_t xTaskStatus;
    
    DEBUG_PRINT("Post-startup initialization starting...\r\n");
    
    // Initialize platform hardware (if needed)
    Status = PlatformInit();
    if (Status != XST_SUCCESS) {
        DEBUG_PRINT("Platform initialization failed\r\n");
    } else {
        DEBUG_PRINT("Platform initialized successfully\r\n");
    }
    
    // Initialize Arduino UART communication task
    xTaskStatus = xArduinoTaskInit();
    if (xTaskStatus != pdPASS) {
        DEBUG_PRINT("ERROR: Failed to initialize Arduino task\r\n");
    }
    // TODO: Add additional application task initializations here
    
    DEBUG_PRINT("Post-startup initialization complete\r\n");
    
    // Delete this task as it's no longer needed
    vTaskDelete(NULL);
}


/*****************************************************************************/
/**
 * @brief Initialize platform hardware
 *
 * This function initializes hardware components needed for the application.
 * Currently a placeholder for future hardware initialization (sensors, GPIO, etc.)
 *
 * @return XST_SUCCESS if successful, XST_FAILURE otherwise
 */
static int PlatformInit(void) {
    
    // TODO: Add hardware initialization here
    // Examples:
    // - Initialize GPIO for water pump control
    // - Initialize I2C for moisture sensor
    // - Initialize timers
    // - Initialize other peripherals
    
    return XST_SUCCESS;
}



