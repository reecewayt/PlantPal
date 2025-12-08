#include "ads1115.h"
#include "xiic_l.h"
#include "xparameters.h"
#include "FreeRTOS.h"
#include "task.h"
#define LOG_TAG "adc"
#include "logging.h"


// Define your I2C Base Address here (Check xparameters.h)
#ifndef ADS_I2C_BASE
#define ADS_I2C_BASE XPAR_XIIC_0_BASEADDR
#endif

// Calibration values for Capacitive Soil Sensor v1.2
// You must calibrate these: Put sensor in air (Dry) and water (Wet) to find values
#define SOIL_DRY_RAW  22000 
#define SOIL_WET_RAW  10000 

int ADS1115_Init(void) {
    // Basic low-level check
    // In a full implementation, you might reset the I2C bus here
    return XST_SUCCESS;
}

u16 ADS1115_ReadRaw(void) {
    u8 writeBuffer[3];
    u8 readBuffer[2];
    // int Status;  <-- REMOVED UNUSED VARIABLE

    // 1. Write Config to Trigger Conversion
    writeBuffer[0] = ADS1115_REG_CONFIG;
    writeBuffer[1] = ADS1115_CONFIG_MSB;
    writeBuffer[2] = ADS1115_CONFIG_LSB;

    // Send configuration (3 bytes: Reg Ptr + Config MSB + Config LSB)
    unsigned int bytesSent = XIic_Send(ADS_I2C_BASE, ADS1115_ADDR, writeBuffer, 3, XIIC_STOP);
    if (bytesSent != 3) {
        DEBUG_PRINT("I2C Write Failed\r\n");
        return 0;
    }

    // 2. Wait for conversion (approx 8ms for 128SPS)
    vTaskDelay(pdMS_TO_TICKS(10)); 

    // 3. Set Register Pointer to Conversion Register
    writeBuffer[0] = ADS1115_REG_CONVERSION;
    XIic_Send(ADS_I2C_BASE, ADS1115_ADDR, writeBuffer, 1, XIIC_STOP);

    // 4. Read 2 Bytes
    unsigned int bytesRecv = XIic_Recv(ADS_I2C_BASE, ADS1115_ADDR, readBuffer, 2, XIIC_STOP);
    if (bytesRecv != 2) {
        DEBUG_PRINT("I2C Read Failed\r\n");
        return 0;
    }

    // Combine bytes (Big Endian)
    u16 raw = ((u16)readBuffer[0] << 8) | readBuffer[1];
    return raw;
}

u8 ADS1115_GetMoisturePercent(void) {
    u16 raw = ADS1115_ReadRaw();
    
    // Constrain logic
    if (raw > SOIL_DRY_RAW) raw = SOIL_DRY_RAW;
    if (raw < SOIL_WET_RAW) raw = SOIL_WET_RAW;

    // Map raw value to 0-100%
    // Note: Capacitive sensors usually have High Value = Dry, Low Value = Wet
    // So we inverse the mapping.
    u32 range = SOIL_DRY_RAW - SOIL_WET_RAW;
    u32 delta = SOIL_DRY_RAW - raw;
    
    return (u8)((delta * 100) / range);
}