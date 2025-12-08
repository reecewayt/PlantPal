#include "FreeRTOS.h"
#include "task.h"
#include "nexys4io.h"
#include "queue.h"
#include "xparameters.h"


#define LOG_TAG "LED_ANIMATION_TASK"
#include "logging.h"

#define NX4IO_BASEADDR   XPAR_NEXYS4IO_0_BASEADDR 



static xQueueHandle prvLedCommandQueue = NULL;

// Forward Declaration
static void prvLedAnimationTask(void *pvParameters);




BaseType_t xSendLedAnimationCommand(int duration_seconds) {
    if (prvLedCommandQueue == NULL) {
        DEBUG_PRINT("LED Animation command queue not initialized\r\n");
        return pdFAIL;
    }
    return xQueueSend(prvLedCommandQueue, &duration_seconds, portMAX_DELAY);
}

BaseType_t xLedAnimationTaskInit(UBaseType_t priority) {
    BaseType_t xStatus;
    
    DEBUG_PRINT("Initializing LED Animation task...\r\n");

    // create queue, can only handle 1 command at a time
    prvLedCommandQueue = xQueueCreate(1, sizeof(int));

    // initialize nexys4IO 
    int status = NX4IO_initialize(NX4IO_BASEADDR);
    if (status != XST_SUCCESS) {
        DEBUG_PRINT("ERROR: Failed to initialize NEXYS4IO driver\r\n");
        return pdFAIL;
    }
    
    // Create the LED Animation task
    xStatus = xTaskCreate(
        prvLedAnimationTask,               // Task function
        "LedAnimationTask",                // Task name
        configMINIMAL_STACK_SIZE * 2,      // Stack size
        NULL,                              // Task parameters
        priority,                          // Task priority
        NULL                               // Task handle
    );
    
    if (xStatus == pdPASS) {
        DEBUG_PRINT("LED Animation task created successfully\r\n");
    } else {
        DEBUG_PRINT("ERROR: Failed to create LED Animation task\r\n");
    }
    
    return xStatus;
}

//LED Animation Task
static void prvLedAnimationTask(void *pvParameters) {
    (void)pvParameters;
    const int NUM_LEDS = 16; 
    int duration_seconds;
    u32 led_value = 0;

    
    DEBUG_PRINT("LED Animation task started\r\n");
    
    while (1) {
        // Wait for a command
        if (xQueueReceive(prvLedCommandQueue, &duration_seconds, portMAX_DELAY) == pdTRUE) {
            DEBUG_PRINT("Starting LED animation for %d seconds\r\n", duration_seconds);
            
            int delay_step_ms = duration_seconds * 1000 / (NUM_LEDS);
            led_value = 0; // Start with all LEDs off
            
            for (int i = 0; i < NUM_LEDS; i++) {
                // Light up LEDs progressively: start with LED 0, then add LED 1, etc.
                led_value |= (1 << i); // Set bit i to turn on LED i
                NX4IO_setLEDs(led_value);
                
                vTaskDelay(pdMS_TO_TICKS(delay_step_ms));
            }

            // Turn off LEDs after animation
            led_value = 0;
            NX4IO_setLEDs(led_value);
            DEBUG_PRINT("LED animation completed\r\n");
        }
    }
}