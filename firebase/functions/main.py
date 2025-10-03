# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`

from firebase_functions import https_fn
from firebase_functions.options import set_global_options
from firebase_admin import initialize_app

# For cost control, you can set the maximum number of containers that can be
# running at the same time. This helps mitigate the impact of unexpected
# traffic spikes by instead downgrading performance. This limit is a per-function
# limit. You can override the limit for each function using the max_instances
# parameter in the decorator, e.g. @https_fn.on_request(max_instances=5).
set_global_options(max_instances=10)

# initialize_app()
#
#
# @https_fn.on_request()
# def on_request_example(req: https_fn.Request) -> https_fn.Response:
#     return https_fn.Response("Hello world!")

initialize_app()  # Uncomment this line! It's crucial if your functions need to interact with other Firebase services.


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
