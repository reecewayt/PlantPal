/*
 * PlantPal UART Protocol
 * Shared header for Arduino ESP32 <-> FPGA communication
 */

#pragma once

#include <stdint.h>

// ============================================
// UART Configuration
// ============================================
#define UART_BAUD_RATE          115200
#define UART_RX_PIN             16  // ESP32 RX (FPGA TX)
#define UART_TX_PIN             17  // ESP32 TX (FPGA RX)

// ============================================
// Protocol Constants
// ============================================
#define UART_MAX_PAYLOAD_SIZE   32

// ============================================
// Command Codes (Arduino -> FPGA)
// ============================================
#define CMD_READ_MOISTURE       0x10    // Request moisture reading
#define CMD_WATER_ON            0x20    // Turn water pump on for N seconds
#define CMD_STATUS_REQUEST      0x30    // Request status/heartbeat

// ============================================
// Response Codes (FPGA -> Arduino)
// ============================================
#define RESP_MOISTURE_DATA      0x11    // Moisture reading response
#define RESP_WATER_ACK          0x21    // Water command acknowledged
#define RESP_STATUS_OK          0x31    // Status OK / heartbeat response

// ============================================
// Frame Structure
// ============================================
/*
 * Simple UART Frame Format:
 *
 * [COMMAND] [LENGTH] [PAYLOAD...]
 *
 * COMMAND:  1 byte  - Command or Response code
 * LENGTH:   1 byte  - Payload length (0-32)
 * PAYLOAD:  0-32 bytes - Command/response specific data
 *
 * Minimum frame size: 2 bytes (no payload)
 * Maximum frame size: 34 bytes (32-byte payload)
 */

// ============================================
// Payload Structures
// ============================================

// CMD_READ_MOISTURE - No payload needed (length = 0)

// RESP_MOISTURE_DATA payload (1 byte)
typedef struct {
    uint8_t moisture_percent;   // 0-100%
} MoistureDataPayload;

// CMD_WATER_ON payload (2 bytes)
typedef struct {
    uint16_t duration_seconds;  // Duration in seconds (little-endian)
} WaterCommandPayload;

// RESP_WATER_ACK - No payload needed (length = 0)

// CMD_STATUS_REQUEST - No payload needed (length = 0)

// RESP_STATUS_OK - No payload needed (length = 0)

// ============================================
// Protocol Documentation
// ============================================

/*
 * MESSAGE FLOWS:
 *
 * 1. MOISTURE READING REQUEST
 *    Arduino -> FPGA: [0x10][0x00]
 *    FPGA -> Arduino: [0x11][0x01][65]  (65% moisture)
 *
 * 2. WATER PUMP CONTROL (30 seconds)
 *    Arduino -> FPGA: [0x20][0x02][30][0]  (30 = 0x1E in little-endian)
 *    FPGA -> Arduino: [0x21][0x00]  (acknowledged)
 *
 * 3. STATUS/HEARTBEAT CHECK
 *    Arduino -> FPGA: [0x30][0x00]
 *    FPGA -> Arduino: [0x31][0x00]  (online)
 */
