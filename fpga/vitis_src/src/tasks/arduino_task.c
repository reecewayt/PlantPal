/**
 * @file arduino_task.c
 * @brief Arduino UART communication task
 *
 * This task handles bidirectional UART communication with the Arduino ESP32
 * module using the protocol defined in plant_pal_uart_protocol.h. It operates
 * in polling mode, continuously checking for incoming commands and sending
 * appropriate responses.
 */

#include "FreeRTOS.h"
#include "task.h"

#include "config.h"
#include "xuartlite_l.h"
#include "xil_printf.h"
#include <string.h>

#define LOG_TAG "ARDUINO_TASK"
#include "logging.h"

// Include the shared protocol header
#include "plant_pal_uart_protocol.h"
#include "led_animation_task.h"

/************************** Local Definitions ********************************/
#define RX_BUFFER_SIZE          64
#define FRAME_TIMEOUT_MS        100     // Timeout waiting for complete frame

/************************** Local Variables **********************************/
static TaskHandle_t xArduinoTaskHandle = NULL;
static u8 RxBuffer[RX_BUFFER_SIZE];

/************************** Function Prototypes ******************************/
static void prvArduinoTask(void *pvParameters);
static BaseType_t prvReceiveFrame(u8 *command, u8 *length, u8 *payload);
static void prvSendFrame(u8 command, u8 length, const u8 *payload);
static void prvProcessCommand(u8 command, u8 length, const u8 *payload);
static void prvHandleReadMoisture(void);
static void prvHandleWaterOn(const u8 *payload, u8 length);
static void prvHandleStatusRequest(void);

/*****************************************************************************/
/**
 * @brief Receive a complete frame from the Arduino UART
 *
 * Polls the UART receive FIFO to read a complete frame consisting of:
 * [COMMAND] [LENGTH] [PAYLOAD...]
 *
 * @param command Pointer to store the received command byte
 * @param length Pointer to store the payload length
 * @param payload Pointer to buffer for storing payload data
 * @return pdTRUE if a complete frame was received, pdFALSE otherwise
 */
static BaseType_t prvReceiveFrame(u8 *command, u8 *length, u8 *payload) {
    TickType_t startTime = xTaskGetTickCount();
    u32 bytesRead = 0;
    
    // Wait for at least 2 bytes (command + length)
    while (bytesRead < 2) {
        if (!XUartLite_IsReceiveEmpty(ARDUINO_UART_BASEADDR)) {
            RxBuffer[bytesRead++] = XUartLite_RecvByte(ARDUINO_UART_BASEADDR);
        } else {
            // Check for timeout
            if ((xTaskGetTickCount() - startTime) > pdMS_TO_TICKS(FRAME_TIMEOUT_MS)) {
                if (bytesRead > 0) {
                    DEBUG_PRINT("Frame timeout: incomplete header\r\n");
                }
                return pdFALSE;
            }
            vTaskDelay(pdMS_TO_TICKS(1));
        }
    }
    
    // Parse command and length
    *command = RxBuffer[0];
    *length = RxBuffer[1];
    
    // Validate length
    if (*length > UART_MAX_PAYLOAD_SIZE) {
        DEBUG_PRINT("Invalid payload length: %d\r\n", *length);
        return pdFALSE;
    }
    
    // Read payload bytes if length > 0
    if (*length > 0) {
        bytesRead = 0;
        startTime = xTaskGetTickCount();
        
        while (bytesRead < *length) {
            if (!XUartLite_IsReceiveEmpty(ARDUINO_UART_BASEADDR)) {
                payload[bytesRead++] = XUartLite_RecvByte(ARDUINO_UART_BASEADDR);
            } else {
                // Check for timeout
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
 *
 * Sends a complete frame consisting of:
 * [COMMAND] [LENGTH] [PAYLOAD...]
 *
 * @param command Command/response byte to send
 * @param length Payload length (0 if no payload)
 * @param payload Pointer to payload data (can be NULL if length is 0)
 */
static void prvSendFrame(u8 command, u8 length, const u8 *payload) {
    // Send command byte
    XUartLite_SendByte(ARDUINO_UART_BASEADDR, command);
    
    // Send length byte
    XUartLite_SendByte(ARDUINO_UART_BASEADDR, length);
    
    // Send payload if present
    if (length > 0 && payload != NULL) {
        for (u8 i = 0; i < length; i++) {
            XUartLite_SendByte(ARDUINO_UART_BASEADDR, payload[i]);
        }
    }
}

/*****************************************************************************/
/**
 * @brief Handle CMD_READ_MOISTURE command
 *
 * Reads the current moisture sensor value and sends back a RESP_MOISTURE_DATA
 * response with the moisture percentage.
 */
static void prvHandleReadMoisture(void) {
    DEBUG_PRINT("Received CMD_READ_MOISTURE\r\n");
    
    // TODO: Read actual moisture sensor value
    // For now, return a placeholder value
    MoistureDataPayload response;
    response.moisture_percent = 50; // Placeholder: 50%
    
    DEBUG_PRINT("Sending RESP_MOISTURE_DATA: %d%%\r\n", response.moisture_percent);
    prvSendFrame(RESP_MOISTURE_DATA, sizeof(MoistureDataPayload), (u8*)&response);
}

/*****************************************************************************/
/**
 * @brief Handle CMD_WATER_ON command
 *
 * Processes a water pump control command. The payload contains the duration
 * in seconds to run the water pump.
 *
 * @param payload Pointer to WaterCommandPayload structure
 * @param length Length of the payload (should be 2 bytes)
 */
static void prvHandleWaterOn(const u8 *payload, u8 length) {
    DEBUG_PRINT("Received CMD_WATER_ON\r\n");
    
    if (length != sizeof(WaterCommandPayload)) {
        DEBUG_PRINT("Invalid CMD_WATER_ON payload length: %d\r\n", length);
        return;
    }
    
    WaterCommandPayload *waterCmd = (WaterCommandPayload*)payload;
    u16 duration = waterCmd->duration_seconds;
    
    DEBUG_PRINT("Water pump ON for %d seconds\r\n", duration);
    
    // TODO: Turn on water pump for specified duration
    // This would typically:
    // 1. Turn on pump GPIO
    // 2. Set a timer for duration_seconds
    // 3. Turn off pump when timer expires
    BaseType_t result = xSendLedAnimationCommand(duration);
    
    if (result != pdPASS) {
        DEBUG_PRINT("Failed to send LED animation command\r\n");
    }
    
    // Send acknowledgment
    DEBUG_PRINT("Sending RESP_WATER_ACK\r\n");
    prvSendFrame(RESP_WATER_ACK, 0, NULL);
}

/*****************************************************************************/
/**
 * @brief Handle CMD_STATUS_REQUEST command
 *
 * Responds to a status/heartbeat request with RESP_STATUS_OK to indicate
 * the FPGA is online and operational.
 */
static void prvHandleStatusRequest(void) {
    DEBUG_PRINT("Received CMD_STATUS_REQUEST\r\n");
    
    // TODO: Optionally check system health status here
    
    DEBUG_PRINT("Sending RESP_STATUS_OK\r\n");
    prvSendFrame(RESP_STATUS_OK, 0, NULL);
}

/*****************************************************************************/
/**
 * @brief Process a received command from Arduino
 *
 * Dispatches the command to the appropriate handler function based on the
 * command code.
 *
 * @param command Command code received
 * @param length Payload length
 * @param payload Pointer to payload data
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
 * @brief Arduino communication task
 *
 * Main task that continuously polls the Arduino UART for incoming commands,
 * processes them, and sends appropriate responses. This task operates in
 * polling mode using low-level UART driver functions.
 *
 * @param pvParameters Task parameters (unused)
 */
static void prvArduinoTask(void *pvParameters) {
    (void)pvParameters;
    
    u8 command;
    u8 length;
    u8 payload[UART_MAX_PAYLOAD_SIZE];
    
    DEBUG_PRINT("Arduino UART task started\r\n");
    DEBUG_PRINT("Listening on UART base address: 0x%08X\r\n", ARDUINO_UART_BASEADDR);
    
    while (1) {
        // Check if there's data available in the UART receive FIFO
        if (!XUartLite_IsReceiveEmpty(ARDUINO_UART_BASEADDR)) {
            
            // Attempt to receive a complete frame
            if (prvReceiveFrame(&command, &length, payload) == pdTRUE) {
                DEBUG_PRINT("Frame received - Command: 0x%02X, Length: %d\r\n", 
                           command, length);
                
                // Process the command
                prvProcessCommand(command, length, payload);
            }
        }
        
        // Small delay to prevent excessive CPU usage while polling
        vTaskDelay(pdMS_TO_TICKS(10));
    }
}

/*****************************************************************************/
/**
 * @brief Initialize the Arduino UART communication task
 *
 * Creates and starts the Arduino task with appropriate priority and stack size.
 *
 * @return pdPASS if task was created successfully, pdFAIL otherwise
 */
BaseType_t xArduinoTaskInit(void) {
    BaseType_t xStatus;
    
    DEBUG_PRINT("Initializing Arduino UART task...\r\n");
    
    // Create the Arduino communication task
    xStatus = xTaskCreate(
        prvArduinoTask,                     // Task function
        "ArduinoTask",                      // Task name
        configMINIMAL_STACK_SIZE * 2,      // Stack size
        NULL,                               // Task parameters
        tskIDLE_PRIORITY + 2,               // Task priority (higher than polling example)
        &xArduinoTaskHandle                 // Task handle
    );
    
    if (xStatus == pdPASS) {
        DEBUG_PRINT("Arduino UART task created successfully\r\n");
    } else {
        DEBUG_PRINT("ERROR: Failed to create Arduino UART task\r\n");
    }
    
    return xStatus;
}
