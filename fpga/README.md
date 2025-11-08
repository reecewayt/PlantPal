# Plant-Pal Project

Welcome to the Plant-Pal project\! This is a hardware/software co-design project that integrates an FPGA design (Vivado/Vitis) with an Android application and Firebase backend.

This README provides the setup instructions and development workflow, with a special focus on the `fpga/` directory.

## 📦 Project Structure

```
plant-pal/
├── android/            # Android application source code
├── firebase/           # Firebase cloud functions and rules
├── fpga/
│   ├── build/            # IGNORED: All generated Vivado/Vitis project files
│   ├── platform/         # TRACKED: The exported .xsa hardware platform
│   │   └── plant_pal_platform.xsa
│   ├── scripts/          # TRACKED: Tcl script to rebuild the Vivado project
│   │   └── create_project.tcl
│   ├── src/              # TRACKED: All hardware source files
│   │   ├── bd/           # Block Designs (.bd)
│   │   ├── constraints/  # Constraint files (.xdc)
│   │   └── hdl/          # Verilog/VHDL source files (.v, .vhd)
│   └── vitis_src/        # TRACKED: Vitis (C/C++) application sources
├── .gitignore
├── LICENSE
└── README.md
```

## 🛠 FPGA Development Workflow

To avoid version-control conflicts and keep our repository clean, we follow a script-based workflow.

**Our Philosophy:** We **DO NOT** commit the generated Vivado project (`.xpr` file) or any build artifacts. We only track the *source files* (`src/`) and a *Tcl script* (`scripts/`) that rebuilds the project from those sources.

### Prerequisites

  * Vivado & Vitis (202x.x)
  * Git

-----

### 🚀 First-Time Project Setup (Walkthrough)

This is a step-by-step guide to building the Vivado project for the first time.

1.  **Clone the Repository:**

    ```bash
    git clone [your-repo-url]
    cd plant-pal
    ```

2.  **Open Vivado:** Launch the main Vivado IDE.

3.  **Open the Tcl Console:** At the bottom of the Vivado window, click the `Tcl Console` tab.

4.  **Navigate to the Scripts Directory:** Use the `cd` command in the Tcl console to navigate to the `fpga/scripts` directory.

    > **Note:** Tcl uses forward slashes `/` for paths, even on Windows.

    ```tcl
    # Example:
    cd C:/path/to/plant-pal/fpga/scripts

    # Or, if you're at the project root already:
    cd ./fpga/scripts
    ```

5.  **Run the Create Script:**

    ```tcl
    source ./create_project.tcl
    ```

6.  **What Happens Next:** The script will run for 30-60 seconds. It will create a new Vivado project inside the `fpga/build/plant_pal_fpga` directory, adding all the correct sources and constraint files from the `src/` folder.

7.  **Open the Project:** Now, you can open the project in the GUI.

      * Go to **File \> Open Project...**
      * Navigate to and select `fpga/build/plant_pal_fpga/plant_pal_fpga.xpr`.

You now have a fully functional project.

-----

### BOARD-SPECIFIC SETUP: Activating Your Constraint File

The project has two constraint files, but only one can be active. The script enables the `nexys4.xdc` by default.

#### For Nexys 4 Users (Default)

You are all set\! The `nexys4.xdc` is already active. You can run synthesis and implementation.

#### For Nexys 7 Users

You must manually swap the active constraint file. **You only need to do this once.**

1.  In the **"Sources"** pane, expand **"Constraints"** \> **"constrs\_1"**.
2.  You will see:
      * `nexys4.xdc (Enabled)`
      * `nexys7.xdc (Disabled)`
3.  Right-click on `nexys4.xdc` and select **"Disable File..."**.
4.  Right-click on `nexys7.xdc` and select **"Enable File..."**.

You are now ready to run synthesis and implementation on your Nexys 7 board. Vivado will remember this setting for this project (since it's in the `build/` folder, which is ignored).

-----

## 🔁 How to Modify the Project (Adding Files)

To keep the project in sync, follow these rules.

### 1\. Adding New HDL Files (`.v` / `.vhd`)

1.  **DO NOT** use the "Add Sources" button in the GUI.
2.  **Instead:** Save your new file (e.g., `my_module.v`) directly into the `fpga/src/hdl/` directory.
3.  **That's it\!** The `create_project.tcl` script automatically finds and adds *all* files in that directory.
4.  Commit your new file to Git:
    ```bash
    git add fpga/src/hdl/my_module.v
    git commit -m "Added new HDL module"
    ```

### 2\. Modifying the Block Design (`.bd`)

This is the one exception where you will use the GUI.

1.  Open the project (`fpga/build/plant_pal_fpga.xpr`).
2.  Open the Block Design (`design_1.bd`) from the "Sources" pane.
3.  Make your changes and **Save the Block Design**.
4.  The script links directly to the file in `fpga/src/bd/`. When you save, you are modifying the *tracked* file.
5.  Commit your changes:
    ```bash
    git add fpga/src/bd/design_1.bd
    git commit -m "Updated block design with new AXI timer"
    ```

### 3\. Updating the Hardware Platform for Vitis (`.xsa`)

After you run synthesis and implementation, you must export the hardware so Vitis can use it.

1.  In Vivado, go to **File \> Export \> Export Hardware...**
2.  A wizard will pop up.
3.  On the "Output" page, select **"Include Bitstream"**.
4.  For "File name," **CRITICAL:** Save the file *directly* over the old one:
    `fpga/platform/plant_pal_platform.xsa`
5.  Click "Finish".
6.  Commit the updated platform file:
    ```bash
    git add fpga/platform/plant_pal_platform.xsa
    git commit -m "Updated hardware platform (XSA)"
    ```

### 4\. Adding/Modifying Vitis Source Code (`.c` / `.h`)

1.  All Vitis source code lives in `fpga/vitis_src/`.
2.  Add, edit, and delete files in this folder as you normally would.
3.  Commit any changes to this folder.

## Git & Version Control Summary

### ✔️ What We Track (Commit This)

  * `fpga/scripts/` (The "recipe")
  * `fpga/src/` (The "ingredients": HDL, BD, constraints)
  * `fpga/vitis_src/` (The software source)
  * `fpga/platform/plant_pal_platform.xsa` (The hardware/software handoff)
  * `android/`
  * `firebase/`
  * `README.md`, `LICENSE`, `.gitignore`

### ❌ What We Ignore (Do NOT Commit)

  * `fpga/build/` (The entire generated project)
  * Any `.log`, `.jou`, or `.cache` files. (The `.gitignore` handles this)