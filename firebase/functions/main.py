# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`

import asyncio
from firebase_functions import https_fn
from firebase_functions.options import set_global_options
from firebase_admin import initialize_app

# Import our agent
from agent import get_agent

# For cost control, you can set the maximum number of containers that can be
# running at the same time. This helps mitigate the impact of unexpected
# traffic spikes by instead downgrading performance. This limit is a per-function
# limit. You can override the limit for each function using the max_instances
# parameter in the decorator, e.g. @https_fn.on_request(max_instances=5).
set_global_options(max_instances=10)

initialize_app()  # Crucial for Firebase services integration


@https_fn.on_call()
def hello_world_firebase(req: https_fn.CallableRequest) -> any:
    # --- ADD PRINT STATEMENTS FOR DEBUGGING ---
    print("--- Function Invoked ---")
    print(f"Request data: {req.data}")
    print(f"Authentication context: {req.auth}")
    # ------------------------------------------

    client_msg = req.data.get("text")

    if client_msg is None:
        raise https_fn.HttpsError(
            code=https_fn.FunctionsErrorCode.INVALID_ARGUMENT,
            message="No text provided in the data payload."
        )

    return {
        "message": f"Hello from Firebase Functions (Python)! You sent: {client_msg}"
    }


@https_fn.on_call()
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
