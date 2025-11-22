# PlantPal Agent Integration Tests

Real LLM integration tests that validate agent behavior, memory management, and thread isolation.

## What These Tests Cover

### 1. **Memory Management Tests** (`TestAgentMemoryManagement`)
- ✅ Single-turn conversations
- ✅ Multi-turn conversations with context retention
- ✅ Memory trimming after 3+ messages
- ✅ Conversation history persistence

### 2. **Thread Isolation Tests** (`TestAgentThreadIsolation`)
- ✅ Different threads maintain separate conversations
- ✅ Same thread_id maintains context across agent instances
- ✅ LangSmith history retrieval across agent restarts
- ✅ Firebase function pattern with history loading

### 3. **Tool Integration Tests** (`TestAgentToolIntegration`)
- ✅ Tavily web search tool usage
- ✅ IoT tools (moisture sensor, irrigation control)

### 4. **Error Handling Tests** (`TestAgentErrorHandling`)
- ✅ Empty message handling
- ✅ Off-topic question handling

### 5. **Configuration Tests** (`TestAgentConfiguration`)
- ✅ Different token limit configurations

## Prerequisites

1. **Required API Keys** (set as environment variables):
   ```bash
   export OPENAI_API_KEY="your-key-here"
   export TAVILY_API_KEY="your-key-here"
   export LANGSMITH_API_KEY="your-key-here"  # Optional for tracing
   ```

2. **Or use `.env` file** in `firebase/functions/`:
   ```
   OPENAI_API_KEY=your-key-here
   TAVILY_API_KEY=your-key-here
   LANGSMITH_API_KEY=your-key-here
   ```

## Running the Tests

### Run All Integration Tests
```bash
cd firebase/functions
python -m tests.integration.test_agent_integration
```

### Run Specific Test Class
```bash
python -m unittest tests.integration.test_agent_integration.TestAgentMemoryManagement
```

### Run Single Test
```bash
python -m unittest tests.integration.test_agent_integration.TestAgentMemoryManagement.test_multi_turn_conversation_with_context
```

### Run with Different Verbosity
```python
# In Python shell
from tests.integration.test_agent_integration import run_integration_tests
run_integration_tests(verbosity=1)  # Less verbose
run_integration_tests(verbosity=2)  # Default - detailed
```

## Expected Output

```
==============================================================
🌱 PlantPal Agent Integration Tests
==============================================================
Testing: Memory Management, Thread Isolation, Tool Usage

test_multi_turn_conversation_with_context (tests.integration.test_agent_integration.TestAgentMemoryManagement)
🧪 Testing multi-turn conversation with context...
Turn 1 response: Succulents are...
Turn 2 response: Water them infrequently...
✅ Multi-turn context test passed ... ok

...

==============================================================
📊 Test Summary
==============================================================
Tests run: 12
Successes: 12
Failures: 0
Errors: 0
==============================================================
```

## Important Notes

### API Costs
⚠️ **These tests make real LLM API calls and will incur costs**
- Each test makes 1-5 API calls to OpenAI
- Estimated cost: ~$0.10-0.50 per full test run
- Use sparingly during development

### Test Duration
⏱️ Tests take **2-5 minutes** to complete due to:
- Real LLM API calls
- Rate limiting delays between requests
- Tool execution (web search, etc.)

### Rate Limiting
If you encounter rate limit errors:
1. Add delays between tests (already included)
2. Reduce test scope temporarily
3. Use a higher-tier API key

## Debugging Failed Tests

### Test Fails with "Context not maintained"
- Check that `trim_messages` middleware is working correctly
- Verify `InMemorySaver` checkpointer is configured
- Ensure `thread_id` is properly passed to config

### Test Fails with "Tool not invoked"
- Verify tools are properly loaded in `agent.py`
- Check API keys for Tavily are set
- Review agent prompt to ensure tool usage is encouraged

### Test Fails with API Errors
- Verify API keys are valid and have sufficient quota
- Check network connectivity
- Review LangSmith traces for detailed error info

## Integration with CI/CD

To run these tests in CI/CD:

```yaml
# .github/workflows/test.yml
- name: Run Integration Tests
  env:
    OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
    TAVILY_API_KEY: ${{ secrets.TAVILY_API_KEY }}
  run: |
    cd firebase/functions
    python -m tests.integration.test_agent_integration
```

⚠️ **Consider**: Integration tests may be too expensive/slow for every commit.
Run them on:
- Pull requests to main
- Scheduled nightly builds
- Manual triggers only

## Next Steps

After validating basic behavior, consider:
1. **Mock LLM tests** - Faster, cheaper tests with mocked responses
2. **Performance benchmarks** - Track response times
3. **Load testing** - Test concurrent thread handling
4. **End-to-end tests** - Test Firebase Functions deployment
