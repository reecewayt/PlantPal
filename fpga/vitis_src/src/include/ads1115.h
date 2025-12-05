#pragma once

#include "xil_types.h"

// I2C Address (ADDR pin connected to GND)
#define ADS1115_ADDR            0x48

// Configuration Registers
#define ADS1115_REG_CONVERSION  0x00
#define ADS1115_REG_CONFIG      0x01

// Configuration Bits (Default for Soil Sensor: Single Shot, +/- 4.096V, Channel 0)
// OS=1 (Start), MUX=100 (AIN0-GND), PGA=001 (4.096V), MODE=1 (Single)
// DR=100 (128SPS), COMP=11 (Disable)
#define ADS1115_CONFIG_MSB      0xC3 
#define ADS1115_CONFIG_LSB      0x83

/**
 * @brief Initialize the I2C controller for the ADS1115
 * @return XST_SUCCESS or XST_FAILURE
 */
int ADS1115_Init(void);

/**
 * @brief Reads the raw value from the ADC (Channel 0)
 * @return 16-bit raw value from sensor
 */
u16 ADS1115_ReadRaw(void);

/**
 * @brief Reads the moisture and converts to percentage
 * @return Moisture percentage (0-100)
 */
u8 ADS1115_GetMoisturePercent(void);