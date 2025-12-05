#include "FreeRTOS.h"
#include "task.h"

#include "config.h"
#include "xil_printf.h"

#define LOG_TAG "MAIN"
#include "logging.h"
#include "arduino_task.h"

// Hardware Drivers
#include "nexys4io.h"
#include "ads1115.h"

/************************** Global Definitions *******************************/

// Check xparameters.h for the exact name of your Nexys4IO IP base address
// It usually follows the format: XPAR_<IP_NAME>_S00_AXI_BASEADDR
#ifndef NEXYS4IO_BASEADDR
#define NEXYS4IO_BASEADDR XPAR_NEXYS4IO_0_BASEADDR
#endif

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
 */
static void prvPostStartupHook(void *pvParameters) {
    (void)pvParameters;
    int Status;
    BaseType_t xTaskStatus;
    
    DEBUG_PRINT("Post-startup initialization starting...\r\n");
    
    // Initialize platform hardware
    Status = PlatformInit();
    if (Status != XST_SUCCESS) {
        DEBUG_PRINT("CRITICAL ERROR: Platform initialization failed\r\n");
        // We might want to stall here or continue with degraded functionality
    } else {
        DEBUG_PRINT("Platform initialized successfully\r\n");
    }
    
    // Initialize Arduino UART communication and Watering tasks
    xTaskStatus = xArduinoTaskInit();
    if (xTaskStatus != pdPASS) {
        DEBUG_PRINT("ERROR: Failed to initialize Arduino task\r\n");
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
 */
static int PlatformInit(void) {
    int Status;
    
    DEBUG_PRINT("Initializing Hardware Peripherals...\r\n");
    
    // 1. Initialize Nexys4IO (LEDs and Switches)
    //
    Status = NX4IO_initialize(NEXYS4IO_BASEADDR);
    if (Status != XST_SUCCESS) {
        DEBUG_PRINT("Failed to initialize Nexys4IO\r\n");
        return XST_FAILURE;
    }
    
    // Clear all LEDs on startup
    NX4IO_setLEDs(0x0000);
    
    // 2. Initialize ADS1115 (Soil Moisture Sensor)
    //
    Status = ADS1115_Init();
    if (Status != XST_SUCCESS) {
        DEBUG_PRINT("Failed to initialize ADS1115\r\n");
        return XST_FAILURE;
    }
    
    return XST_SUCCESS;
}