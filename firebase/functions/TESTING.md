# Quick Test Reference

## Run All Tests
```bash
cd firebase/functions
python run_tests.py
```

## Run Specific Test Class
```bash
# Memory management tests only
python run_tests.py --test TestAgentMemoryManagement

# Thread isolation tests only
python run_tests.py --test TestAgentThreadIsolation

# Tool integration tests only
python run_tests.py --test TestAgentToolIntegration
```

## Run Single Test Method
```bash
python run_tests.py --test TestAgentMemoryManagement.test_multi_turn_conversation_with_context
```

## Run with Less Output
```bash
python run_tests.py --quiet
```

## Alternative: Direct Python Module
```bash
# Run all tests
python -m tests.integration.test_agent_integration

# Run specific class
python -m unittest tests.integration.test_agent_integration.TestAgentMemoryManagement

# Run single test
python -m unittest tests.integration.test_agent_integration.TestAgentMemoryManagement.test_single_turn_conversation
```

## Before Running Tests

1. **Set API Keys** (choose one method):

   **Option A: Environment variables**
   ```bash
   export OPENAI_API_KEY="sk-..."
   export TAVILY_API_KEY="tvly-..."
   export LANGSMITH_API_KEY="ls-..."  # Optional
   ```

   **Option B: Create `.env` file**
   ```bash
   cd firebase/functions
   cp .env.template .env
   # Edit .env and add your keys
   ```

2. **Install dependencies** (if not already installed):
   ```bash
   pip install -r requirements.txt
   ```

## What Tests Cover

✅ **Memory Management**
- Single and multi-turn conversations
- Context retention across turns
- Memory trimming (after 3+ messages)
- Conversation history access

✅ **Thread Isolation**
- Different threads stay separate
- Same thread maintains context
- Thread persistence across agent instances

✅ **Tool Usage**
- Tavily web search
- IoT moisture sensor data
- Irrigation control

✅ **Error Handling**
- Empty messages
- Off-topic questions

✅ **Configuration**
- Different token limits

## Expected Runtime
- Full test suite: **2-5 minutes**
- Single test: **5-30 seconds**

## Expected Cost
- Full test suite: **~$0.10-0.50** (real LLM calls)
- Consider using mocked tests for frequent runs
