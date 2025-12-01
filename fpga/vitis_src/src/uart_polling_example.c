#include "FreeRTOS.h"
#include "task.h"

#include "config.h"
#include "xuartlite_l.h"
#include "xil_printf.h"
#include <string.h>

#define LOG_TAG "UART_POLL"
#include "logging.h"

/************************** Local Definitions ********************************/
#define ARDUINO_RX_BUFFER_SIZE 64

/************************** Local Variables **********************************/
static u8 ArduinoRxBuffer[ARDUINO_RX_BUFFER_SIZE];

/************************** Function Prototypes ******************************/
static void prvArduinoUartPollingTask(void *pvParameters);

/*****************************************************************************/
/**
 * @brief Arduino UART polling task
 *
 * This task continuously polls the UART receive FIFO using low-level driver
 * functions. When data is detected, it reads byte-by-byte until the FIFO is
 * empty, then echoes the received data.
 *
 * @param pvParameters Task parameters (unused)
 */
static void prvArduinoUartPollingTask(void *pvParameters) {
    (void)pvParameters;
    unsigned int ByteCount;
    
    DEBUG_PRINT("Arduino UART polling task started\r\n");
    
    while (1) {
        // Poll the receive FIFO using low-level function
        if (!XUartLite_IsReceiveEmpty(ARDUINO_UART_BASEADDR)) {
            
            // Clear buffer for new data
            memset(ArduinoRxBuffer, 0, ARDUINO_RX_BUFFER_SIZE);
            ByteCount = 0;
            
            // Read bytes one by one until FIFO is empty or buffer is full
            while (!XUartLite_IsReceiveEmpty(ARDUINO_UART_BASEADDR) && 
                   ByteCount < (ARDUINO_RX_BUFFER_SIZE - 1)) {
                
                ArduinoRxBuffer[ByteCount] = XUartLite_RecvByte(ARDUINO_UART_BASEADDR);
                ByteCount++;
            }
            
            // Echo received data byte by byte
            xil_printf("[UART_POLL] Received from Arduino (%d bytes): ", ByteCount);
            for (unsigned int i = 0; i < ByteCount; i++) {
                xil_printf("%c", ArduinoRxBuffer[i]);
            }
            xil_printf("\r\n");
        }
        
        // Small delay to prevent excessive CPU usage while polling
        vTaskDelay(pdMS_TO_TICKS(10));
    }
}

/*****************************************************************************/
/**
 * @brief Main function
 *
 * Entry point for the standalone UART polling application.
 */
int main(void) {
    DEBUG_PRINT("Plant-Pal UART Polling Application Starting...\r\n");
    
    // Create the UART polling task
    xTaskCreate(
        prvArduinoUartPollingTask,
        "ArduinoUartPoll",
        configMINIMAL_STACK_SIZE * 2,
        NULL,
        tskIDLE_PRIORITY + 1,
        NULL
    );
    
    DEBUG_PRINT("Starting FreeRTOS scheduler...\r\n");
    
    // Start the FreeRTOS scheduler
    vTaskStartScheduler();
    
    // Should never reach here
    while (1) {
        // Empty loop
    }
    
    return XST_SUCCESS;
}
