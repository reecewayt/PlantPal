"""
Simple unit tests for PlantPal IoT tools
Tests basic hardcoded return values using LangChain .invoke() method
"""

import unittest
import sys
import os
from unittest.mock import patch

# Add parent directories to path for imports (PlantPal pattern)
parent_dir = os.path.dirname(os.path.dirname(
    os.path.dirname(os.path.abspath(__file__))))
sys.path.append(parent_dir)

from tools.iot_tools import (
    get_moisture_data,
    get_sensor_id,
    control_irrigation
)


class TestIoTTools(unittest.TestCase):
    """Simple tests for PlantPal IoT tools. TODO: Expand with real tests once IoT infrastructure is set up."""

    def test_get_moisture_data(self):
        """Test moisture data returns expected hardcoded structure."""
        result = get_moisture_data.invoke({"sensor_id": "test_sensor"})

        # Verify basic structure
        self.assertIsInstance(result, dict)
        self.assertIn("sensor_id", result)
        self.assertIn("moisture_percentage", result)
        self.assertIn("temperature", result)

        print("✅ Moisture data test passed")

    def test_get_sensor_id(self):
        """Test sensor ID prints expected hardcoded value."""
        with patch('builtins.print') as mock_print:
            get_sensor_id.invoke({})
            # Just verify print was called
            mock_print.assert_called_once()

        print("✅ Sensor ID test passed")

    def test_control_irrigation(self):
        """Test irrigation control returns expected hardcoded message."""
        result = control_irrigation.invoke({"action": "start"})

        # Verify basic return type and content
        self.assertIsInstance(result, str)
        self.assertIn("irrigation", result.lower())

        print("✅ Irrigation control test passed")


if __name__ == '__main__':
    unittest.main(verbosity=2)
