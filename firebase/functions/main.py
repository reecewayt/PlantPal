import asyncio
from firebase_functions import https_fn
from firebase_functions import options
from firebase_functions.options import set_global_options
from firebase_admin import initialize_app, firestore
from firebase_functions import pubsub_fn
import json
import time

# Import our agent and config
from agent import get_agent
from config import TOPICS

# For cost control, you can set the maximum number of containers that can be
# running at the same time. This helps mitigate the impact of unexpected
# traffic spikes by instead downgrading performance. This limit is a per-function
# limit. You can override the limit for each function using the max_instances
# parameter in the decorator, e.g. @https_fn.on_request(max_instances=5).
set_global_options(max_instances=10)

initialize_app()  # Crucial for Firebase services integration


@https_fn.on_call(memory=options.MemoryOption.MB_512)
def plantpal_chat(req: https_fn.CallableRequest) -> any:
    """
    PlantPal AI Chat Function
    Handles conversation with the LangChain agent

    Request data:
        - message: User's chat message (required)
        - thread_id: Conversation thread identifier (required for persistence)
        - use_history: Boolean, if True loads conversation history from
                      LangSmith (default: False)
    """
    print("--- PlantPal Chat Function Invoked ---")
    print(f"Request data: {req.data}")

    # Extract message from request
    message = req.data.get("message")
    thread_id = req.data.get("thread_id")  # User/conversation identifier
    existing_thread = req.data.get("existing_thread", False)  # Load history

    if not message:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message="No message provided in the data payload."
        )

    if not thread_id:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message="No thread_id provided. Required for conversation tracking."
        )

    try:
        # Get the agent instance
        # If existing_thread=True, agent loads past conversation from LangSmith
        # This enables long-running chats across Firebase function invocations
        agent = get_agent(
            thread_id=thread_id,
            existing_thread=existing_thread
        )
        response = agent.chat(message)

        return {
            "response": response,
            "success": True,
            "thread_id": thread_id  # Echo back for client tracking
        }

    except Exception as e:
        print(f"Error in plantpal_chat: {e}")
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INTERNAL,
            message=f"Internal error processing your request: {str(e)}"
        )


@pubsub_fn.on_message_published(topic=TOPICS["data-moisture"])
def handle_moisture_data(event: pubsub_fn.CloudEvent[pubsub_fn.MessagePublishedData]) -> None:
    """Handle incoming moisture data from IoT devices and store in Firestore."""
    try:
        # Decode the message data
        data = event.data.message.json
    except Exception as e:
        #Message was not json decodable
        print(f"❌ Error decoding message data: {e}")
        return

    payload = data if data else {"percentage": None, "timestamp": None} # Fallback if error

    try:
        print(f"📊 Received moisture data: {payload}")


        # Store the data in Firestore
        db = firestore.client()
        doc_ref = db.collection('sensor_data').document("esp32") # Default to unknown for now

        doc_ref.set({
            'percentage': payload.get('percentage'),
            'timestamp': payload.get('timestamp'),
            'received_at': int(time.time() * 1000),
        })

        print(f"✅ Stored moisture data for sensor esp32 in Firestore")

    except Exception as e:
        print(f"❌ Error storing moisture data: {e}")


@pubsub_fn.on_message_published(topic=TOPICS["system-status"])
def handle_system_status(event: pubsub_fn.CloudEvent[pubsub_fn.MessagePublishedData]) -> None:
    """Handle incoming system status updates from IoT devices and store in Firestore."""
    try:
        # Decode the message data
        data = event.data.message.json
    except Exception as e:
        # Message was not json decodable
        print(f"❌ Error decoding system status message: {e}")
        return

    payload = data if data else {}

    try:
        print(f"🔧 Received system status: {payload}")

        # Store the data in Firestore (overwrites previous status)
        db = firestore.client()
        doc_ref = db.collection('system_status').document('esp32')

        doc_ref.set({
            **payload,
            'received_at': int(time.time() * 1000)
        })

        print(f"✅ Stored system status in Firestore")

    except Exception as e:
        print(f"❌ Error storing system status: {e}")
