/**
 * @file arduino_task.c
 * @brief Arduino UART communication task
 *
 * This task handles bidirectional UART communication with the Arduino ESP32
 * module. It integrates the ADS1115 sensor for moisture readings and the
 * Nexys4IO LEDs for simulated watering.
 */

#include "FreeRTOS.h"
#include "task.h"
#include "semphr.h" 

#include "config.h"
#include "xuartlite_l.h"
#include "xil_printf.h"
#include <string.h>

#define LOG_TAG "ARDUINO_TASK"
#include "logging.h"

// Include protocol and hardware drivers
#include "plant_pal_uart_protocol.h"
#include "ads1115.h"
#include "nexys4io.h"

/************************** Local Definitions ********************************/
#define RX_BUFFER_SIZE          64
#define FRAME_TIMEOUT_MS        100     // Timeout waiting for complete frame

/************************** Local Variables **********************************/
static TaskHandle_t xArduinoTaskHandle = NULL;
static TaskHandle_t xWateringTaskHandle = NULL;
static SemaphoreHandle_t xWateringSemaphore = NULL;

static u8 RxBuffer[RX_BUFFER_SIZE];
static volatile u16 uWateringDuration = 0; // Shared variable for duration

/************************** Function Prototypes ******************************/
static void prvArduinoTask(void *pvParameters);
static void prvWateringTask(void *pvParameters);
static BaseType_t prvReceiveFrame(u8 *command, u8 *length, u8 *payload);
static void prvSendFrame(u8 command, u8 length, const u8 *payload);
static void prvProcessCommand(u8 command, u8 length, const u8 *payload);
static void prvHandleReadMoisture(void);
static void prvHandleWaterOn(const u8 *payload, u8 length);
static void prvHandleStatusRequest(void);

/*****************************************************************************/
/**
 * @brief Receive a complete frame from the Arduino UART
 */
static BaseType_t prvReceiveFrame(u8 *command, u8 *length, u8 *payload) {
    TickType_t startTime = xTaskGetTickCount();
    u32 bytesRead = 0;
    
    // Wait for at least 2 bytes (command + length)
    while (bytesRead < 2) {
        if (!XUartLite_IsReceiveEmpty(ARDUINO_UART_BASEADDR)) {
            RxBuffer[bytesRead++] = XUartLite_RecvByte(ARDUINO_UART_BASEADDR);
        } else {
            if ((xTaskGetTickCount() - startTime) > pdMS_TO_TICKS(FRAME_TIMEOUT_MS)) {
                return pdFALSE;
            }
            vTaskDelay(pdMS_TO_TICKS(1));
        }
    }
    
    *command = RxBuffer[0];
    *length = RxBuffer[1];
    
    if (*length > UART_MAX_PAYLOAD_SIZE) {
        DEBUG_PRINT("Invalid payload length: %d\r\n", *length);
        return pdFALSE;
    }
    
    if (*length > 0) {
        bytesRead = 0;
        startTime = xTaskGetTickCount();
        
        while (bytesRead < *length) {
            if (!XUartLite_IsReceiveEmpty(ARDUINO_UART_BASEADDR)) {
                payload[bytesRead++] = XUartLite_RecvByte(ARDUINO_UART_BASEADDR);
            } else {
                if ((xTaskGetTickCount() - startTime) > pdMS_TO_TICKS(FRAME_TIMEOUT_MS)) {
                    DEBUG_PRINT("Frame timeout: incomplete payload\r\n");
                    return pdFALSE;
                }
                vTaskDelay(pdMS_TO_TICKS(1));
            }
        }
    }
    
    return pdTRUE;
}

/*****************************************************************************/
/**
 * @brief Send a frame to the Arduino UART
 */
static void prvSendFrame(u8 command, u8 length, const u8 *payload) {
    XUartLite_SendByte(ARDUINO_UART_BASEADDR, command);
    XUartLite_SendByte(ARDUINO_UART_BASEADDR, length);
    
    if (length > 0 && payload != NULL) {
        for (u8 i = 0; i < length; i++) {
            XUartLite_SendByte(ARDUINO_UART_BASEADDR, payload[i]);
        }
    }
}

/*****************************************************************************/
/**
 * @brief Handle CMD_READ_MOISTURE command
 * Reads real data from ADS1115
 */
static void prvHandleReadMoisture(void) {
    DEBUG_PRINT("Received CMD_READ_MOISTURE\r\n");
    
    MoistureDataPayload response;
    
    // Read actual moisture sensor value from ADS1115 driver
    response.moisture_percent = ADS1115_GetMoisturePercent();
    
    DEBUG_PRINT("Sending RESP_MOISTURE_DATA: %d%%\r\n", response.moisture_percent);
    prvSendFrame(RESP_MOISTURE_DATA, sizeof(MoistureDataPayload), (u8*)&response);
}

/*****************************************************************************/
/**
 * @brief Handle CMD_WATER_ON command
 * Triggers the watering task via semaphore
 */
static void prvHandleWaterOn(const u8 *payload, u8 length) {
    DEBUG_PRINT("Received CMD_WATER_ON\r\n");
    
    if (length != sizeof(WaterCommandPayload)) {
        DEBUG_PRINT("Invalid CMD_WATER_ON payload length: %d\r\n", length);
        return;
    }
    
    WaterCommandPayload *waterCmd = (WaterCommandPayload*)payload;
    
    // Set global duration and signal the watering task
    uWateringDuration = waterCmd->duration_seconds;
    
    if (xWateringSemaphore != NULL) {
        xSemaphoreGive(xWateringSemaphore);
        DEBUG_PRINT("Water pump sequence triggered for %d seconds\r\n", uWateringDuration);
    } else {
        DEBUG_PRINT("ERROR: Watering semaphore not initialized\r\n");
    }
    
    // Send acknowledgment immediately
    DEBUG_PRINT("Sending RESP_WATER_ACK\r\n");
    prvSendFrame(RESP_WATER_ACK, 0, NULL);
}

/*****************************************************************************/
/**
 * @brief Handle CMD_STATUS_REQUEST command
 */
static void prvHandleStatusRequest(void) {
    DEBUG_PRINT("Received CMD_STATUS_REQUEST\r\n");
    DEBUG_PRINT("Sending RESP_STATUS_OK\r\n");
    prvSendFrame(RESP_STATUS_OK, 0, NULL);
}

/*****************************************************************************/
/**
 * @brief Process a received command
 */
static void prvProcessCommand(u8 command, u8 length, const u8 *payload) {
    switch (command) {
        case CMD_READ_MOISTURE:
            prvHandleReadMoisture();
            break;
            
        case CMD_WATER_ON:
            prvHandleWaterOn(payload, length);
            break;
            
        case CMD_STATUS_REQUEST:
            prvHandleStatusRequest();
            break;
            
        default:
            DEBUG_PRINT("Unknown command received: 0x%02X\r\n", command);
            break;
    }
}

/*****************************************************************************/
/**
 * @brief Dedicated task to handle high-power watering (LEDs)
 * This runs independently so the UART polling loop doesn't freeze.
 */
static void prvWateringTask(void *pvParameters) {
    (void) pvParameters;
    
    DEBUG_PRINT("Watering Control Task Started\r\n");

    while(1) {
        // Wait indefinitely for the signal to start watering
        if (xSemaphoreTake(xWateringSemaphore, portMAX_DELAY) == pdTRUE) {
            
            DEBUG_PRINT("Watering... (LEDs ON)\r\n");
            
            // Turn ON all 16 LEDs on Nexys A7 to simulate pump
            NX4IO_setLEDs(0xFFFF);
            
            // Wait for the specified duration (convert Seconds to Ticks)
            vTaskDelay(pdMS_TO_TICKS(uWateringDuration * 1000));
            
            // Turn OFF LEDs
            NX4IO_setLEDs(0x0000);
            
            DEBUG_PRINT("Watering Complete. (LEDs OFF)\r\n");
        }
    }
}

/*****************************************************************************/
/**
 * @brief Arduino communication task
 */
static void prvArduinoTask(void *pvParameters) {
    (void)pvParameters;
    
    u8 command;
    u8 length;
    u8 payload[UART_MAX_PAYLOAD_SIZE];
    
    DEBUG_PRINT("Arduino UART task started\r\n");
    
    while (1) {
        if (!XUartLite_IsReceiveEmpty(ARDUINO_UART_BASEADDR)) {
            if (prvReceiveFrame(&command, &length, payload) == pdTRUE) {
                DEBUG_PRINT("Frame received - Command: 0x%02X\r\n", command);
                prvProcessCommand(command, length, payload);
            }
        }
        vTaskDelay(pdMS_TO_TICKS(10));
    }
}

/*****************************************************************************/
/**
 * @brief Initialize the Arduino UART communication task
 */
BaseType_t xArduinoTaskInit(void) {
    BaseType_t xStatus;
    
    DEBUG_PRINT("Initializing Arduino UART task...\r\n");
    
    // Create Synchronization Semaphore
    xWateringSemaphore = xSemaphoreCreateBinary();
    if (xWateringSemaphore == NULL) {
        DEBUG_PRINT("ERROR: Failed to create watering semaphore\r\n");
        return pdFAIL;
    }

    // Create the Arduino communication task
    xStatus = xTaskCreate(
        prvArduinoTask,
        "ArduinoTask",
        configMINIMAL_STACK_SIZE * 2,
        NULL,
        tskIDLE_PRIORITY + 2,
        &xArduinoTaskHandle
    );
    
    // Create the Watering Control task
    if (xStatus == pdPASS) {
        xStatus = xTaskCreate(
            prvWateringTask,
            "WateringTask",
            configMINIMAL_STACK_SIZE,
            NULL,
            tskIDLE_PRIORITY + 1,
            &xWateringTaskHandle
        );
    }
    
    if (xStatus == pdPASS) {
        DEBUG_PRINT("Arduino tasks created successfully\r\n");
    } else {
        DEBUG_PRINT("ERROR: Failed to create Arduino tasks\r\n");
    }
    
    return xStatus;
}