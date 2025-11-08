////////////
// Top-level module for ECE 544 Project #1
// May have to be modified for your specific embedded system implementation
///////////
`timescale 1 ps / 1 ps

module top_module
   (
	input logic			clk,
	input logic			btnCpuReset,
	//output logic [6:0]	seg,    // todo: Implement SEG to display last read soil moisture value 0% - 99%
    input logic         arduino_uart_rxd,
    output logic        arduino_uart_txd,
    input logic         usb_uart_rxd,
    output logic        usb_uart_txd,
    inout              i2c_adc_scl,
    inout              i2c_adc_sda        
);
    
  // instantiate the embedded system
  design_1_wrapper design_1_wrapper_i
  ( 
    // UART
    .arduino_uart_rxd(arduino_uart_rxd),
    .arduino_uart_txd(arduino_uart_txd),
    .usb_uart_rxd(usb_uart_rxd),
    .usb_uart_txd(usb_uart_txd),   
    // Clk & Reset 
    .resetn(btnCpuReset),
    .clk_100MHz(clk),
    // I2C 
    .iic_adc_scl_io(i2c_adc_scl),
    .iic_adc_sda_io(i2c_adc_sda)   
   );
        
endmodule
