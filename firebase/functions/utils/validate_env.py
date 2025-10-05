
import os

try:
    from dotenv import load_dotenv
    load_dotenv()
except ImportError:
    print("python-dotenv not installed, using system environment variables")
except Exception as e:
    print(f"Error loading .env file: {e}")


def validate_environment():
    """
    TODO: Validate required environment variables following PlantPal Firebase Functions pattern.
    Returns True if all required keys are present.
    """
    required_vars = ["OPENAI_API_KEY", "OPENWEATHERMAP_API_KEY"]
    missing_vars = []

    for var in required_vars:
        if not os.getenv(var):
            missing_vars.append(var)

    if missing_vars:
        print(f"❌ Missing required environment variables: {missing_vars}")
        print("Please check your .env file or environment configuration")
        return False

    print("✅ All required environment variables found")
    return True
