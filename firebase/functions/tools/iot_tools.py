from langchain.tools import tool
from typing import Dict, Any, Optional
from google.cloud import pubsub_v1
from firebase_admin import firestore
import json
import time
from config import TOPICS

# Initialize Pub/Sub publisher
publisher = pubsub_v1.PublisherClient()

@tool
def get_moisture_data() -> str:
    """
    Get soil moisture data from the sensor. This tool requests fresh data from
    the IoT device and waits for the response.

    Returns:
        A string containing the moisture percentage and timestamp
    """
    print(f"📊 Getting moisture data from sensor")

    try:
        # Get Firestore client
        db = firestore.client()
        doc_ref = db.collection('sensor_data').document('esp32')

        # Delete any existing data for fresh request
        doc_ref.delete()

        # Publish request to request-soil topic
        topic_path = TOPICS["request-soil"]
        request_payload = json.dumps({
            'timestamp': int(time.time() * 1000)
        }).encode('utf-8')

        # Publish request
        future = publisher.publish(
            topic_path,
            request_payload
        )
        future.result()  # Wait for publish to complete
        print(f"✅ Published request for moisture data")

        # Wait for response (poll Firestore with timeout)
        max_wait_seconds = 10
        poll_interval = 0.5
        elapsed = 0

        while elapsed < max_wait_seconds:
            # Check if data exists in Firestore
            doc = doc_ref.get()

            if doc.exists:
                data = doc.to_dict()
                if data:
                    percentage = data.get('percentage')
                    timestamp = data.get('timestamp')

                    # Convert timestamp to readable format
                    if timestamp:
                        timestamp_readable = time.strftime(
                            '%Y-%m-%d %H:%M:%S',
                            time.localtime(timestamp / 1000)
                        )
                        return (f"Soil moisture: "
                               f"{percentage:.1f}% (measured at {timestamp_readable})")
                    else:
                        return (f"Soil moisture: "
                               f"{percentage:.1f}% (timestamp unavailable)")

            time.sleep(poll_interval)
            elapsed += poll_interval

        # Timeout - no response received
        return (f"⏱️ Timeout: No response from sensor after {max_wait_seconds}s. "
               f"The sensor may be offline or out of range.")

    except Exception as e:
        print(f"❌ Error getting moisture data: {e}")
        return f"❌ Error: Unable to retrieve moisture data - {str(e)}"




@tool
def control_irrigation(duration_seconds: int = 5) -> str:
    """
    Control the irrigation system. Should be used when user requests you
    to water their plants. The duration default is 5 seconds. Depending on how dry the plant is
    water for 1, 5, or 10 seconds.

    Args:
        duration_seconds: How long to run irrigation in seconds

    Returns:
        Status message
    """
    print(f"💧 Irrigating for {duration_seconds} seconds")

    try:
        # Publish request to request-water topic
        topic_path = TOPICS["request-water"]
        request_payload = json.dumps({
            'duration_seconds': duration_seconds
        }).encode('utf-8')

        # Publish request
        future = publisher.publish(
            topic_path,
            request_payload
        )
        future.result()  # Wait for publish to complete
        print(f"✅ Published irrigation request for {duration_seconds} seconds")

        return f"✅ Started irrigation for {duration_seconds} seconds"

    except Exception as e:
        print(f"❌ Error controlling irrigation: {e}")
        return f"❌ Error: Unable to start irrigation - {str(e)}"


@tool
def get_system_status() -> str:
    """
    Get the current system status to check if the IoT device is online and operational.
    Use this tool when you need to verify device connectivity before requesting data or actions.

    Returns:
        A string containing the system status information
    """
    print(f"🔧 Getting system status")

    try:
        # Get Firestore client
        db = firestore.client()
        doc_ref = db.collection('system_status').document('esp32')

        # Check if status exists
        doc = doc_ref.get()

        if doc.exists:
            data = doc.to_dict()
            if data:
                received_at = data.get('received_at')

                # Calculate how long ago the status was received
                if received_at:
                    time_diff = int(time.time() * 1000) - received_at
                    seconds_ago = time_diff / 1000

                    # Consider device online if status was received within last 60 seconds
                    if seconds_ago < 60:
                        status_info = f"✅ Device is ONLINE (last update {seconds_ago:.0f}s ago)"
                    else:
                        status_info = f"⚠️ Device may be OFFLINE (last update {seconds_ago:.0f}s ago)"

                    # Include any additional status fields
                    other_fields = {k: v for k, v in data.items() if k != 'received_at'}
                    if other_fields:
                        status_info += f"\nStatus details: {json.dumps(other_fields, indent=2)}"

                    return status_info
                else:
                    return "⚠️ Device status unknown (no timestamp available)"

        return "❌ No system status available. Device has not reported status yet."

    except Exception as e:
        print(f"❌ Error getting system status: {e}")
        return f"❌ Error: Unable to retrieve system status - {str(e)}"
