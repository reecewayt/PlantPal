#pragma once

#include "xparameters.h"

// flag used by logging utility
// comment out to disable DEBUG
#define DEBUG_1 

// USB to UART bridge, used for serial communication and debugging
#define USB_UART_BASEADDR XPAR_XUARTLITE_0_BASEADDR

// Arduino UART interface, used to received commands from arduino mqtt node
#define ARDUINO_UART_BASEADDR XPAR_XUARTLITE_1_BASEADDR