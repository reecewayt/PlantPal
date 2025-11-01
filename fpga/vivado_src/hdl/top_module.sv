////////////
// Top-level module for ECE 544 Project #1
// May have to be modified for your specific embedded system implementation
///////////
`timescale 1 ps / 1 ps

module top_module
   (
	input logic			clk,
	input logic [15:0]	sw,
	input logic		    btnU,
	input logic		    btnR,
	input logic			btnL,
	input logic			btnD,
	input logic			btnC,			// not used in project 1
	input logic			btnCpuReset,
	output logic [15:0]	led,
    output logic RGB1_Blue, RGB1_Green, RGB1_Red,
	output logic RGB2_Blue,RGB2_Green, RGB2_Red,	
    output logic [7:0]	an,
	output logic [6:0]	seg,
    output logic		dp,
    input logic         usb_uart_rxd,
    output logic         usb_uart_txd
);
    
  wire [31:0] control_reg, gpio_rtl_tri_o;
  
  // wrap the gpio output to the rgbPWM control register
  assign control_reg = gpio_rtl_tri_o;

  // instantiate the embedded system
  embsys embsys_i
       (.gpio_rtl_0_tri_o(gpio_rtl_tri_o),
        .RGB2_Blue_0(RGB2_Blue),
        .RGB2_Green_0(RGB2_Green),
        .RGB2_Red_0(RGB2_Red),
        .an_0(an),
        .btnC_0(btnC),
        .btnD_0(btnD),
        .btnL_0(btnL),
        .btnR_0(btnR),
        .btnU_0(btnU),
        //.clkPWM_0(),
        .clk_100MHz(clk),
        .controlReg_0(control_reg),
        .dp_0(dp),
        .led_0(led),
        .resetn(btnCpuReset),
        .rgbBLUE_0(RGB1_Blue),
        .rgbGREEN_0(RGB1_Green),
        .rgbRED_0(RGB1_Red),
        .seg_0(seg),
        .sw_0(sw),
        .uart_rtl_0_rxd(usb_uart_rxd),
        .uart_rtl_0_txd(usb_uart_txd)
        );
        
endmodule
