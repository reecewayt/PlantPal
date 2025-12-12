# Plant-Pal FPGA Project Setup

This guide provides instructions for setting up the hardware project for both software (Vitis) and hardware (Vivado) development.

## 🚀 Quick Start (Software Only - Vitis)

If you are only working on the C/C++ software, you **do not** need to build the Vivado project. You can use one of the pre-built hardware platforms.

1.  Launch Vitis.
2.  Create a new Application Project.
3.  When asked for a platform, choose **"Create a new platform from hardware (XSA)"**.
4.  Browse to the `.xsa` file that matches your board:
      * **Nexys 4:** `fpga/hw_platform/hw_platform_nexys4.xsa`
      * **Nexys 7:** `fpga/hw_platform/hw_platform_nexys7.xsa`
5.  Make sure to select `FreeRTOS` as the OS
6.  You can now build your software project on top of this platform.

> Our group had both a Nexys 4 and NexysA7 so either should work with this project

For more details on the software flow see section below: "Software Details" 

-----

## 🏗️ Getting started or Rebuilding the Vivado Project (Hardware)

If you need to modify the FPGA hardware (e.g., change the block design, add IP) or are just getting started and want to setup the vivado hardware platform, you must rebuild the project using the included Tcl script.

### 1\. Create the Project

1.  Open the Vivado Tcl Console (e.g., from the start menu or by opening Vivado and clicking `Window > Tcl Console`).
2.  In the Tcl Console, change the directory to the `fpga/` folder in this repository.
    ```tcl
    # Example:
    cd C:/git/plant-pal/fpga
    ```
3.  Source the build script to create the project.
    ```tcl
    source create_project.tcl
    ```
4.  The script will run and create a full Vivado project in the `build/plant_pal_fpga` directory.

### 2\. Open and Modify the Project

1.  In Vivado, click **"Open Project"**.
2.  Navigate to `build/plant_pal_fpga` and open the `plant_pal_fpga.xpr` file.
3.  You can now make any changes to the block design (`design_1.bd`) or HDL files.

### 3\. Switching Target Boards

This project supports both the Nexys 4 and Nexys 7. The build script adds constraint files for both.

  * **By default, `nexys4.xdc` is active.**
  * To switch to the Nexys 7, open the project in the GUI, go to the **"Sources"** window, right-click on `nexys7.xdc`, select **"Set as Active"** (or uncheck "IS\_ENABLED" on the Nexys 4 file and check it on the Nexys 7 file).

### 4\. Exporting Your Changes (Important\!)

If you modify the hardware, you **must** export a new `.xsa` file so it can be used in vitis.

1.  After running Implementation, select **File \> Export \> Export Hardware...**.
2.  Choose **"Include bitstream"**.
3.  Save the file, overwriting the old `.xsa` in the `fpga/hw_platform/` directory.
4.  **Commit the new `.xsa` file to Git.**

---

## Manage Project with Git

### Core Philosophy

This repository **does not** track the Vivado project file (`.xpr`) or any generated `build/` files. Instead, we use a "source-based" approach.

We only track the essential *source files* required to rebuild the entire project from scratch using the `fpga/scripts/create_project.tcl` and `fpga/scripts/embsys.tcl` script. This keeps the repository lightweight, avoids "untrackable" binary changes, and ensures all developers have a consistent setup.

### What We Track

The only files that are tracked by Git are the project "sources":

1.  **`fpga/create_project.tcl`**: The main script used to build the Vivado project.
2.  **`fpga/vivado_src/`**: This directory contains all the human-written source code:
    * `hdl/`: All top-level Verilog/SystemVerilog files (`.sv`).
    * `constraints/`: The board-specific pin-out files (`.xdc`).
    * `bd/`: The Vivado Block Design (`design_1.bd`) and its metadata (`.bda`).
3.  **`fpga/hw_platform/`**: The generated hardware definitions (`.xsa` files) that Vitis needs to build the software.

Any other file (like the entire `build/` directory) is ignored by Git.

### Team Workflow & Committing Changes

**All changes should be submitted as a Pull Request (PR)**. This is crucial for keeping the hardware and software definitions in sync for the whole team.

#### If You Change the Hardware (e.g., update `design_1.bd` or HDL):

This is the most critical workflow. You **must** commit both the source *and* the exported result.

1.  Open the Vivado project (or rebuild it using the Tcl script).
2.  Make your changes to the Block Design or HDL.
3.  Run Synthesis and Implementation to ensure your changes are valid.
4.  Export the new hardware platform: **File > Export > Export Hardware...**
5.  Save the new `.xsa` file to the `fpga/hw_platform/` directory, overwriting the old one for your board.
6.  **Commit your changes.** Your commit **must** include:
    * The updated source file (e.g., `fpga/vivado_src/bd/design_1.bd`).
    * The newly exported hardware file (e.g., `fpga/hw_platform/hw_platform_nexys4.xsa`).
7.  Open a Pull Request with a clear description of your hardware changes.

#### If You Only Change Software (e.g., update `.c` files in Vitis):

1.  Make your changes to the software source.
2.  Commit the updated software files.
3.  Open a Pull Request.

This workflow ensures that any developer who pulls the `main` branch will always have the correct `.xsa` file to match the latest hardware sources.

--- 
## Software Details

Our software was built on the FreeRTOS port for the Microblaze Risc-V IP (soft core).

### Architecture Overview

The application uses a **FreeRTOS-based multi-task architecture** running on a MicroBlaze soft processor. The system coordinates sensor data acquisition, UART communication with an Arduino ESP32 module, and LED animations through independent FreeRTOS tasks with proper priority scheduling.

**Note:** The `vivado_src/` directory contains the hardware design sources (HDL, block designs, constraints), while `vitis_src/` contains all embedded software sources.

### Key Components

#### Main Application (`src/main.c`)
- Initializes FreeRTOS scheduler
- Creates a post-startup hook task for hardware and task initialization
- Initializes the Nexys4IO peripheral driver for 7-segment display and LEDs

#### FreeRTOS Tasks (`src/tasks/`)

1. **Arduino UART Task** (`arduino_task.c`)
   - Handles bidirectional UART communication with Arduino ESP32 module
   - Implements the Plant-Pal UART protocol (defined in `common/plant_pal_uart_protocol.h`)
   - Processes commands: `CMD_READ_MOISTURE`, `CMD_WATER_ON`, `CMD_STATUS_REQUEST`
   - Operates in polling mode with frame-based communication
   - Priority: `tskIDLE_PRIORITY + 1`

2. **Sensor Task** (`sensor_task.c`)
   - Reads soil moisture data from ADS1115 ADC via I2C
   - Converts raw ADC values to 0-100% moisture readings
   - Displays moisture percentage on 7-segment display
   - Responds to external read requests from Arduino task
   - Runs periodically (1-second interval)
   - Priority: `tskIDLE_PRIORITY + 2` (highest priority)

3. **LED Animation Task** (`led_animation_task.c`)
   - Controls LED animations on the Nexys board
   - Receives animation commands via FreeRTOS queue
   - Triggered when watering operations are performed
   - Priority: `tskIDLE_PRIORITY + 1`

#### Hardware Drivers (`src/drivers/`)

- **ADS1115 ADC Driver** (`ads1115.c`): I2C-based 16-bit ADC driver for capacitive soil moisture sensor
  - Configurable gain, data rate
  - Includes calibration values for Capacitive Soil Sensor v1.2
  - Uses low-level Xilinx I2C functions (`xiic_l.h`)

- **Nexys4IO Driver** (`nexys4io.c`): Custom peripheral driver for Nexys4/7 board features
  - Controls 7-segment displays (8 digits)
  - GPIO/LED control
  - Hardware interface to custom FPGA IP

#### Communication Protocol (`common/plant_pal_uart_protocol.h`)

Shared header between FPGA and Arduino ESP32 defining:
- UART configuration (115200 baud, pins 16/17)
- Command codes (Arduino → FPGA): `CMD_READ_MOISTURE`, `CMD_WATER_ON`, `CMD_STATUS_REQUEST`
- Response codes (FPGA → Arduino): `RESP_MOISTURE_DATA`, `RESP_ACK`, `RESP_ERROR`, `RESP_STATUS`
- Frame structure: `[COMMAND] [LENGTH] [PAYLOAD...]`
- Maximum payload size: 32 bytes

### Configuration (`src/include/config.h`)

- **USB_UART_BASEADDR**: Debug/console UART (XUARTLITE_0)
- **ARDUINO_UART_BASEADDR**: Arduino communication UART (XUARTLITE_1)
- Debug logging enable/disable flag

### Build System

- **CMake-based build** configured through `app_component/src/CMakeLists.txt`
- Dependencies: `xilstandalone`, `xiltimer`, `freertos`, `gloss`
- Application YAML configuration: `app_component/src/app.yaml`
- Platform: Generated from `hw_platform/*.xsa` files

### Development Workflow

1. Source code modifications should be made in `vitis_src/src/`
2. Build using Vitis IDE or command-line CMake
3. Hardware platform must match the exported `.xsa` from Vivado
4. Debug output available via USB UART (115200 baud)

See `vitis_src/src` for all driver code, FreeRTOS tasks, and application logic. 

#### Note
Vitis components are not tracked by Git, as these components often included machine specific configurations. Only the pure application sources are tracked and are therefore out of any `app_component` build tree managed by vitis. This means you will need to manually create a vitis `app_component` and `platform` component, then add your sources in the respecitve UserConfig.cmake

- Example of my UserConfig.cmake stored locally on my machine as `vitis_src/app_component/src/UserConfig.cmake`
```cmake
# Add these lines to your UserConfig.cmake
set(USER_INCLUDE_DIRECTORIES
"../../src/include"
"../../../../common"
)
set(USER_COMPILE_SOURCES
"../../src/main.c"
"../../src/tasks/arduino_task.c"
"../../src/tasks/led_animation_task.c"
"../../src/tasks/sensor_task.c"
"../../src/drivers/nexys4io.c"
"../../src/drivers/nexys4io_selftest.c"
"../../src/drivers/ads1115.c"
)
```

