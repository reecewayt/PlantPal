#include "FreeRTOS.h"
#include "task.h"

#include "config.h"
#include "xil_printf.h"

#define LOG_TAG "MAIN"
#include "logging.h"
#include "arduino_task.h"
#include "sensor_task.h" 
#include "led_animation_task.h"
#include "nexys4io.h"


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
    xTaskStatus = xArduinoTaskInit(tskIDLE_PRIORITY + 1);
    if (xTaskStatus != pdPASS) {
        DEBUG_PRINT("ERROR: Failed to initialize Arduino task\r\n");
    }

    // Initialize LED Animation task
    xTaskStatus = xLedAnimationTaskInit(tskIDLE_PRIORITY + 1);
    if (xTaskStatus != pdPASS) {
        DEBUG_PRINT("ERROR: Failed to initialize LED Animation task\r\n");
    }
    
    // Initialize Sensor task
    xTaskStatus = xSensorTaskInit(tskIDLE_PRIORITY + 2);
    if (xTaskStatus != pdPASS) {
        DEBUG_PRINT("ERROR: Failed to initialize Sensor task\r\n");
    }
    
    DEBUG_PRINT("Post-startup initialization complete\r\n");
    
    // Delete this task as it's no longer needed
    vTaskDelete(NULL);
}


/*****************************************************************************/
/**
 * @brief Initialize platform hardware
 *
 * This function initializes hardware components needed for the application.
 * (i.e. GPIO, peripherals, etc.)
 *
 * @return XST_SUCCESS if successful, XST_FAILURE otherwise
 */
static int PlatformInit(void) {
    int status = XST_SUCCESS;
    
    status = NX4IO_initialize(XPAR_NEXYS4IO_0_BASEADDR);
    if (status != XST_SUCCESS)
    {
        DEBUG_PRINT("ERROR: Failed to initialize NEXYS4IO driver\r\n");
        return XST_FAILURE;
    }
    // clear 7-seg display 
    NX410_SSEG_setAllDigits(SSEGLO, CC_BLANK, CC_BLANK, CC_BLANK, CC_BLANK, DP_NONE);
    NX410_SSEG_setAllDigits(SSEGHI, CC_BLANK, CC_BLANK, CC_BLANK, CC_BLANK, DP_NONE);

    DEBUG_PRINT("NEXYS4IO driver initialized successfully\r\n");
    
    
    return status;
}