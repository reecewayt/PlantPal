// Copyright 1986-2022 Xilinx, Inc. All Rights Reserved.
// Copyright 2022-2025 Advanced Micro Devices, Inc. All Rights Reserved.
// -------------------------------------------------------------------------------

`timescale 1 ps / 1 ps

(* BLOCK_STUB = "true" *)
module design_1 (
  usb_uart_rxd,
  usb_uart_txd,
  arduino_uart_rxd,
  arduino_uart_txd,
  iic_adc_scl_i,
  iic_adc_scl_o,
  iic_adc_scl_t,
  iic_adc_sda_i,
  iic_adc_sda_o,
  iic_adc_sda_t,
  resetn,
  clk_100MHz
);

  (* X_INTERFACE_INFO = "xilinx.com:interface:uart:1.0 usb_uart RxD" *)
  (* X_INTERFACE_MODE = "master usb_uart" *)
  input usb_uart_rxd;
  (* X_INTERFACE_INFO = "xilinx.com:interface:uart:1.0 usb_uart TxD" *)
  output usb_uart_txd;
  (* X_INTERFACE_INFO = "xilinx.com:interface:uart:1.0 arduino_uart RxD" *)
  (* X_INTERFACE_MODE = "master arduino_uart" *)
  input arduino_uart_rxd;
  (* X_INTERFACE_INFO = "xilinx.com:interface:uart:1.0 arduino_uart TxD" *)
  output arduino_uart_txd;
  (* X_INTERFACE_INFO = "xilinx.com:interface:iic:1.0 iic_adc SCL_I" *)
  (* X_INTERFACE_MODE = "master iic_adc" *)
  input iic_adc_scl_i;
  (* X_INTERFACE_INFO = "xilinx.com:interface:iic:1.0 iic_adc SCL_O" *)
  output iic_adc_scl_o;
  (* X_INTERFACE_INFO = "xilinx.com:interface:iic:1.0 iic_adc SCL_T" *)
  output iic_adc_scl_t;
  (* X_INTERFACE_INFO = "xilinx.com:interface:iic:1.0 iic_adc SDA_I" *)
  input iic_adc_sda_i;
  (* X_INTERFACE_INFO = "xilinx.com:interface:iic:1.0 iic_adc SDA_O" *)
  output iic_adc_sda_o;
  (* X_INTERFACE_INFO = "xilinx.com:interface:iic:1.0 iic_adc SDA_T" *)
  output iic_adc_sda_t;
  (* X_INTERFACE_INFO = "xilinx.com:signal:reset:1.0 RST.RESETN RST" *)
  (* X_INTERFACE_MODE = "slave RST.RESETN" *)
  (* X_INTERFACE_PARAMETER = "XIL_INTERFACENAME RST.RESETN, POLARITY ACTIVE_LOW, INSERT_VIP 0" *)
  input resetn;
  (* X_INTERFACE_INFO = "xilinx.com:signal:clock:1.0 CLK.CLK_100MHZ CLK" *)
  (* X_INTERFACE_MODE = "slave CLK.CLK_100MHZ" *)
  (* X_INTERFACE_PARAMETER = "XIL_INTERFACENAME CLK.CLK_100MHZ, FREQ_HZ 100000000, FREQ_TOLERANCE_HZ 0, PHASE 0.0, CLK_DOMAIN design_1_clk_100MHz, INSERT_VIP 0" *)
  input clk_100MHz;

  // stub module has no contents

endmodule
