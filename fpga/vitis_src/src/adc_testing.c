#include "adc_testing.h"
#include "ads1115.h"     // ADC driver
#include "nexys4IO.h"    // Nexys4IO driver
#include "xparameters.h" // Needed for XPAR_*_BASEADDR

// Configuration for logging and task
#define LOG_TAG "ADC_TEST"
#include "logging.h"

#define ADCTEST_TASK_STACK_SIZE configMINIMAL_STACK_SIZE * 3
#define ADCTEST_TASK_PRIORITY tskIDLE_PRIORITY + 1

//Fallback for Nexys4IO base address if not defined in xparameters.h
#ifndef XPAR_NEXYS4IO_0_BASEADDR
#define XPAR_NEXYS4IO_0_BASEADDR 0x44A00000 
#endif

/**
 * @brief Helper function to display a 0-100 percentage on the 7-segment display.
 * Displays the value right-justified on digits 0, 1, and 2.
 * @param percent The moisture percentage (0-100).
 */
static void DisplayMoisture(u8 percent) {
    u8 digit0, digit1, digit2;

    // 1: Extract digits
    // Example: 85 -> H=0, T=8, U=5
    // Example: 100 -> H=1, T=0, U=0
    digit2 = percent % 10;          // Units digit (Right-most)
    digit1 = (percent / 10) % 10;   // Tens digit
    digit0 = percent / 100;         // Hundreds digit

    // 2: Clear all 8 digits for "cleared out" display
    // Clear Lower Bank (Digits 0-3)
    for (int i = 0; i < 4; i++) {
        NX4IO_SSEG_setDigit(SSEGLO, (enum _NX4IO_ssegdigits)i, CC_BLANK);
    }
    // Clear Upper Bank (Digits 4-7)
    for (int i = 0; i < 4; i++) {
        NX4IO_SSEG_setDigit(SSEGHI, (enum _NX4IO_ssegdigits)i, CC_BLANK);
    }
    
    // 3: Display the units and tens digits on the right-most two displays (SSEGLO)
    NX4IO_SSEG_setDigit(SSEGLO, DIGIT0, (enum _NX4IO_charcodes)(CC_0 + digit2));
    NX4IO_SSEG_setDigit(SSEGLO, DIGIT1, (enum _NX4IO_charcodes)(CC_0 + digit1));

    // 4: Display  hundreds digit if it's non-zero
    if (digit0 > 0) {
        NX4IO_SSEG_setDigit(SSEGLO, DIGIT2, (enum _NX4IO_charcodes)(CC_0 + digit0));
    }
    
    // 5: Decimal segment OFF
    NX4IO_SSEG_setDecPt(SSEGLO, DIGIT0, false);
}


/**
 * @brief Main task for reading the ADC and displaying the value.
 * @param pvParameters Unused.
 */
static void vAdcTestTask(void *pvParameters) {
    (void)pvParameters;
    int Status;
    u8 moisture_percent;

    // Initialize Nexys4IO peripheral
    DEBUG_PRINT("Initializing Nexys4IO peripheral...\r\n");
    Status = NX4IO_initialize(XPAR_NEXYS4IO_0_BASEADDR);
    if (Status != XST_SUCCESS) {
        DEBUG_PRINT("ERROR: Nexys4IO initialization failed.\r\n");
        vTaskDelete(NULL);
        return;
    }
    DEBUG_PRINT("Nexys4IO initialized.\r\n");

    // Initialize the ADS1115 I2C peripheral
    DEBUG_PRINT("Initializing ADS1115 I2C peripheral...\r\n");
    Status = ADS1115_Init();
    if (Status != XST_SUCCESS) {
        // DEBUG_PRINT("ERROR: ADS1115 I2C initialization failed. Possible wiring fault\r\n");
        //turned out to be a wiring fault, commenting out but leaving (working after soldering ADS1115 board)
        DEBUG_PRINT("ERROR: ADS1115 I2C initialization failed. Check ADS_I2C_BASE in ads1115.c.\r\n");
        vTaskDelete(NULL);
        return;
    }
    DEBUG_PRINT("ADS1115 I2C initialized. Starting moisture read loop.\r\n");
    
    // Main sensor read loop
    while (1) {
        // Read the moisture percentage (0-100)
        moisture_percent = ADS1115_GetMoisturePercent();

        //serial monitor print
        DEBUG_PRINT("Moisture: %d %%\r\n", moisture_percent);
        
        //7-Segment display output
        DisplayMoisture(moisture_percent);

        // Delay for 1 second before the next reading
        vTaskDelay(pdMS_TO_TICKS(1000));
    }
}


/**
 * @brief Creates the ADC testing FreeRTOS task.
 */
BaseType_t xAdcTestTaskInit(void) {
    BaseType_t xTaskStatus;
    
    xTaskStatus = xTaskCreate(
        vAdcTestTask, 
        "AdcTest", 
        ADCTEST_TASK_STACK_SIZE, 
        NULL, 
        ADCTEST_TASK_PRIORITY, 
        NULL
    );

    return xTaskStatus;
}