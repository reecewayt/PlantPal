//Copyright 1986-2022 Xilinx, Inc. All Rights Reserved.
//Copyright 2022-2024 Advanced Micro Devices, Inc. All Rights Reserved.
//--------------------------------------------------------------------------------
//Tool Version: Vivado v.2024.2 (win64) Build 5239630 Fri Nov 08 22:35:27 MST 2024
//Date        : Tue Nov 11 17:57:16 2025
//Host        : DESKTOP-EUTEA2Q running 64-bit major release  (build 9200)
//Command     : generate_target design_1_wrapper.bd
//Design      : design_1_wrapper
//Purpose     : IP block netlist
//--------------------------------------------------------------------------------
`timescale 1 ps / 1 ps

module design_1_wrapper
   (arduino_uart_rxd,
    arduino_uart_txd,
    clk_100MHz,
    iic_adc_scl_io,
    iic_adc_sda_io,
    resetn,
    usb_uart_rxd,
    usb_uart_txd);
  input arduino_uart_rxd;
  output arduino_uart_txd;
  input clk_100MHz;
  inout iic_adc_scl_io;
  inout iic_adc_sda_io;
  input resetn;
  input usb_uart_rxd;
  output usb_uart_txd;

  wire arduino_uart_rxd;
  wire arduino_uart_txd;
  wire clk_100MHz;
  wire iic_adc_scl_i;
  wire iic_adc_scl_io;
  wire iic_adc_scl_o;
  wire iic_adc_scl_t;
  wire iic_adc_sda_i;
  wire iic_adc_sda_io;
  wire iic_adc_sda_o;
  wire iic_adc_sda_t;
  wire resetn;
  wire usb_uart_rxd;
  wire usb_uart_txd;

  design_1 design_1_i
       (.arduino_uart_rxd(arduino_uart_rxd),
        .arduino_uart_txd(arduino_uart_txd),
        .clk_100MHz(clk_100MHz),
        .iic_adc_scl_i(iic_adc_scl_i),
        .iic_adc_scl_o(iic_adc_scl_o),
        .iic_adc_scl_t(iic_adc_scl_t),
        .iic_adc_sda_i(iic_adc_sda_i),
        .iic_adc_sda_o(iic_adc_sda_o),
        .iic_adc_sda_t(iic_adc_sda_t),
        .resetn(resetn),
        .usb_uart_rxd(usb_uart_rxd),
        .usb_uart_txd(usb_uart_txd));
  IOBUF iic_adc_scl_iobuf
       (.I(iic_adc_scl_o),
        .IO(iic_adc_scl_io),
        .O(iic_adc_scl_i),
        .T(iic_adc_scl_t));
  IOBUF iic_adc_sda_iobuf
       (.I(iic_adc_sda_o),
        .IO(iic_adc_sda_io),
        .O(iic_adc_sda_i),
        .T(iic_adc_sda_t));
endmodule
