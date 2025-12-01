# Note: This script is intended to be ran once when first setting up the project. 
# after project setup it is up to the user to ensure any added or deleted files
# adhere to the design flow and are checked into git appropriately. 
#
# IMPORTANT: Before running this script, ensure the vivado_src/bd/embsys/ directory
# only contains .bd and .bda files (if using fallback) or is empty (recommended).
# The embsys.tcl script will regenerate the block design from scratch.

# 1. Set paths and project info
set proj_name "plant_pal_fpga"
set proj_dir "../build/${proj_name}"
set src_dir "../vivado_src"

puts "=================================="
puts "Starting project creation script"
puts "=================================="
puts "Project name: ${proj_name}"
puts "Project directory: ${proj_dir}"
puts "Source directory: ${src_dir}"
puts ""

# Check if the project directory already exists and delete it
if { [file exists $proj_dir] } {
    puts "Deleting existing project directory: ${proj_dir}"
    file delete -force $proj_dir
}

# Clean up any existing generated BD files (but keep .bd/.bda if they exist)
set bd_gen_dir "${src_dir}/bd/embsys"
if { [file exists ${bd_gen_dir}] } {
    puts "Cleaning up generated BD files in: ${bd_gen_dir}"
    foreach dir {ip ipshared hdl hw_handoff ui sim synth} {
        set dir_path "${bd_gen_dir}/${dir}"
        if { [file exists ${dir_path}] } {
            puts "  Removing: ${dir_path}"
            file delete -force ${dir_path}
        }
    }
    # Also remove generated files
    foreach pattern {*.xdc *.bmj *.bmm *.bxml *_bmstub.v} {
        foreach f [glob -nocomplain "${bd_gen_dir}/${pattern}"] {
            puts "  Removing: ${f}"
            file delete -force ${f}
        }
    }
}

# Part number for BOTH boards
set part_num "xc7a100tcsg324-1"

# 2. Create the project
puts "Creating project with part: ${part_num}"
create_project ${proj_name} ${proj_dir} -part ${part_num}
puts "Project created successfully"
puts ""

# 3. Add source files (HDL and Block Design)
#    This method checks if files exist before trying to add them.

puts "Adding HDL source files..."
set hdl_files_v [glob -nocomplain ${src_dir}/hdl/*.v]
if { [llength $hdl_files_v] > 0 } {
    puts "  Found [llength $hdl_files_v] Verilog file(s): $hdl_files_v"
    add_files -norecurse $hdl_files_v
} else {
    puts "  No Verilog (.v) files found"
}

set hdl_files_sv [glob -nocomplain ${src_dir}/hdl/*.sv]
if { [llength $hdl_files_sv] > 0 } {
    puts "  Found [llength $hdl_files_sv] SystemVerilog file(s): $hdl_files_sv"
    add_files -norecurse $hdl_files_sv
} else {
    puts "  No SystemVerilog (.sv) files found"
}

puts ""

# Add custom IP repository FIRST (before BD creation)
puts "Adding custom IP repository..."
set ip_repo_path [file normalize ${src_dir}/ip]
if { [file exists $ip_repo_path] } {
    puts "  IP repository path: ${ip_repo_path}"
    set_property ip_repo_paths $ip_repo_path [current_project]
    update_ip_catalog
    puts "  IP repository added successfully"
} else {
    puts "  WARNING: IP repository path does not exist: ${ip_repo_path}"
}
puts ""

# Create Block Design from TCL script
puts "Creating Block Design from TCL script..."
set bd_tcl_script "embsys.tcl"
if { ![file exists ${bd_tcl_script}] } {
    puts "  ERROR: Block design TCL script not found: ${bd_tcl_script}"
    puts "  Please ensure embsys.tcl exists in the scripts directory."
    return -code error "Missing required file: ${bd_tcl_script}"
}

puts "  Sourcing block design script: ${bd_tcl_script}"
if { [catch {source ${bd_tcl_script}} errmsg] } {
    puts "  ERROR: Failed to source block design script: ${bd_tcl_script}"
    puts "  Error message: ${errmsg}"
    return -code error "Block design creation failed"
}
puts "  Block design created successfully"

# Generate the HDL wrapper for the block design
puts "  Generating HDL wrapper for block design..."
set bd_files [get_files *.bd]
if { [llength $bd_files] == 0 } {
    puts "  ERROR: No block design files found after sourcing ${bd_tcl_script}"
    return -code error "Block design generation failed"
}

set bd_file [lindex $bd_files 0]
set wrapper_file [make_wrapper -files $bd_file -top]
add_files -norecurse $wrapper_file
puts "  HDL wrapper generated: $wrapper_file"

# Set top_module.sv as the top-level module
puts "  Setting top_module as the top-level design..."
set_property top top_module [current_fileset]
update_compile_order -fileset sources_1
puts "  Top module set successfully"
puts ""

# 4. Add BOTH constraint files
puts "Adding constraint files..."
set constr_nexys4 [glob -nocomplain ${src_dir}/constraints/nexys4.xdc]
if { [llength $constr_nexys4] > 0 } {
    puts "  Found Nexys 4 constraint file: $constr_nexys4"
    add_files -fileset constrs_1 $constr_nexys4
    set_property USED_IN {synthesis implementation} [get_files $constr_nexys4]
    puts "  Nexys 4 constraints added and ENABLED"
} else {
    puts "  WARNING: Nexys 4 constraint file not found"
}

set constr_nexys7 [glob -nocomplain ${src_dir}/constraints/nexys7.xdc]
if { [llength $constr_nexys7] > 0 } {
    puts "  Found Nexys 7 constraint file: $constr_nexys7"
    add_files -fileset constrs_1 $constr_nexys7
    set_property USED_IN {synthesis implementation} [get_files $constr_nexys7]
    
    # By default, disable the nexys7 file.
    set_property IS_ENABLED false [get_files $constr_nexys7]
    puts "  Nexys 7 constraints added and DISABLED (by default)"
} else {
    puts "  WARNING: Nexys 7 constraint file not found"
}

puts ""
puts "=================================="
puts "Project creation completed!"
puts "=================================="
puts "Project created. By default, 'nexys4.xdc' is active."
puts "To use the Nexys 7, open the project in the GUI and enable 'nexys7.xdc' and disable 'nexys4.xdc'."