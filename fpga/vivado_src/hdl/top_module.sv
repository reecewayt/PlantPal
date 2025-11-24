////////////
// Top-level module for ECE 544 Project #1
// May have to be modified for your specific embedded system implementation
///////////
`timescale 1 ps / 1 ps

module top_module
   (
	input logic			clk,
	input logic [15:0]  sw,
	input logic		    btnU,
	input logic		    btnR,
	input logic			btnL,
	input logic			btnD,
	input logic			btnC,
	input logic			btnCpuReset,
	output logic [15:0]	led,
    output logic [7:0]	an,
	output logic [6:0]	seg,
    output logic		dp,   
    input logic         arduino_uart_rxd,
    output logic        arduino_uart_txd,
    input logic         usb_uart_rxd,
    output logic        usb_uart_txd,
    inout               sclk_io,
    inout               sda_io        
);
    
  // instantiate the embedded system
   embsys embsys_i
   (
        // Nexys4 IO Connections
        .an_0(an),
        .btnC_0(btnC),
        .btnD_0(btnD),
	    .btnL_0(btnL),
	    .btnR_0(btnR),
	    .btnU_0(btnU),
	    .dp_0(dp),
	    .led_0(led),
	    .sw_0(sw),
	    .seg_0(seg),
        // UART
        .arduino_uart_rxd(arduino_uart_rxd),
        .arduino_uart_txd(arduino_uart_txd),
        .usb_uart_rxd(usb_uart_rxd),
        .usb_uart_txd(usb_uart_txd), 
        // I2C
        .scl_io(sclk_io),
        .sda_io(sda_io),
        // CPU 
        .clk_100MHz(clk),
        .resetn(btnCpuReset)
   );        
endmodule
