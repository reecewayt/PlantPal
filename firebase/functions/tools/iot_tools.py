# Collection of IoT-related tools for PlantPal
# Simple placeholder tools that will be developed once IoT infrastructure is set up

from langchain.tools import tool
from typing import Dict, Any

# TODO: Integrate this tool with a simple database retrieval so that it can get the sensor
# associated with the user, rather than requiring the user to input sensor IDs directly.

@tool
def get_moisture_data(sensor_id: str) -> Dict[str, Any]:
    """
    Get soil moisture data from a sensor.

    Args:
        sensor_id: The ID of the moisture sensor

    Returns:
        Mock moisture data for testing
    """
    print(f"📊 Getting moisture data for sensor: {sensor_id}")

    # TODO: Replace with actual IoT sensor reading
    return {
        "sensor_id": sensor_id,
        "moisture_percentage": 65.0,
        "temperature": 22.5,
        "timestamp": "2025-10-04T10:30:00Z",
        "status": "active"
    }

@tool
def get_sensor_id() -> str:
    """
    Agent call this before requesting sensor data. Retrieve the sensor ID associated
    with the current user.

    Returns:
        A mock sensor ID for testing purposes.
    """
    return "plantpal_location_sensor-type_sensor-id"


@tool
def control_irrigation(action: str, duration_minutes: int = 5) -> str:
    """
    Control the irrigation system.

    Args:
        action: Either 'start' or 'stop'
        duration_minutes: How long to run irrigation (for 'start' action)

    Returns:
        Status message
    """
    print(f"💧 Irrigation {action} for {duration_minutes} minutes")

    # TODO: Replace with actual irrigation control
    if action == "start":
        return f"✅ Started irrigation for {duration_minutes} minutes"
    elif action == "stop":
        return "✅ Stopped irrigation"
    else:
        return f"❌ Unknown action: {action}. Use 'start' or 'stop'"
