# Note: This script is intended to be ran once when first setting up the project. 
# after project setup it is up to the user to ensure any added or deleted files
# adhere to the design flow and are checked into git appropriately. 

# 1. Set paths and project info
set proj_name "plant_pal_fpga"
set proj_dir "../build/${proj_name}"
set src_dir "../vivado_src"

# Check if the project directory already exists and delete it
if { [file exists $proj_dir] } {
    file delete -force $proj_dir
}

# Part number for BOTH boards
set part_num "xc7a100tcsg324-1"

# 2. Create the project
create_project ${proj_name} ${proj_dir} -part ${part_num}

# 3. Add source files (HDL and Block Design)
#    This method checks if files exist before trying to add them.

set hdl_files_v [glob -nocomplain ${src_dir}/hdl/*.v]
if { [llength $hdl_files_v] > 0 } {
    add_files -norecurse $hdl_files_v
}

set hdl_files_sv [glob -nocomplain ${src_dir}/hdl/*.sv]
if { [llength $hdl_files_sv] > 0 } {
    add_files -norecurse $hdl_files_sv
}

set bd_files [glob -nocomplain ${src_dir}/bd/design_1/*.bd]
if { [llength $bd_files] > 0 } {
    add_files -norecurse $bd_files
}

# 4. Add BOTH constraint files
set constr_nexys4 [glob -nocomplain ${src_dir}/constraints/nexys4.xdc]
if { [llength $constr_nexys4] > 0 } {
    add_files -fileset constrs_1 $constr_nexys4
    set_property USED_IN {synthesis implementation} [get_files $constr_nexys4]
}

# Generate the HDL wrapper for the block design
make_wrapper -files [get_files *.bd] -top -import_files

set constr_nexys7 [glob -nocomplain ${src_dir}/constraints/nexys7.xdc]
if { [llength $constr_nexys7] > 0 } {
    add_files -fileset constrs_1 $constr_nexys7
    set_property USED_IN {synthesis implementation} [get_files $constr_nexys7]
    
    # By default, disable the nexys7 file.
    set_property IS_ENABLED false [get_files $constr_nexys7]
}

puts "Project created. By default, 'nexys4.xdc' is active."
puts "To use the Nexys 7, open the project in the GUI and enable 'nexys7.xdc' and disable 'nexys4.xdc'."