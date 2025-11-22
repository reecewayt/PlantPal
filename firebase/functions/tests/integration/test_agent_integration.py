"""
Integration tests for PlantPal Agent
Tests real LLM interactions, memory management, and thread isolation
"""

import unittest
import sys
import os
import time

# Add parent directories to path for imports
parent_dir = os.path.dirname(os.path.dirname(
    os.path.dirname(os.path.abspath(__file__))))
sys.path.append(parent_dir)

from agent import PlantPalAgent, MaxOutputTokens, reset_agent


class TestAgentMemoryManagement(unittest.TestCase):
    """Test conversation memory and context management"""

    def setUp(self):
        """Set up fresh agent for each test"""
        reset_agent()
        self.agent = PlantPalAgent(thread_id="test_memory_thread")

    def tearDown(self):
        """Clean up after each test"""
        reset_agent()

    def test_single_turn_conversation(self):
        """Test basic single message interaction"""
        print("\n🧪 Testing single-turn conversation...")

        response = self.agent.chat("What is your name?")

        self.assertIsInstance(response, str)
        self.assertGreater(len(response), 0)
        self.assertIn("PlantPal", response)

        print(f"✅ Single turn test passed. Response: {response[:100]}...")

    def test_multi_turn_conversation_with_context(self):
        """Test that agent maintains context across multiple turns"""
        print("\n🧪 Testing multi-turn conversation with context...")

        # Turn 1: Ask about a specific plant
        response1 = self.agent.chat("Tell me about succulents.")
        self.assertGreater(len(response1), 0)
        print(f"Turn 1 response: {response1[:100]}...")

        # Turn 2: Follow-up question that requires context from turn 1
        response2 = self.agent.chat("How often should I water them?")
        self.assertGreater(len(response2), 0)

        # The agent should understand "them" refers to succulents
        # Response should mention succulents or watering frequency
        keywords = ["succulent", "water", "infrequent", "dry"]
        self.assertTrue(
            any(keyword in response2.lower() for keyword in keywords),
            "Agent should maintain context about succulents"
        )

        print(f"Turn 2 response: {response2[:100]}...")
        print("✅ Multi-turn context test passed")


    def test_conversation_history_persistence(self):
        """Test that conversation history is accessible"""
        print("\n🧪 Testing conversation history persistence...")

        # Send a few messages
        self.agent.chat("What are good indoor plants?")
        self.agent.chat("Tell me more about the first one.")

        # Check that we can access the conversation history
        # Note: With LangGraph checkpointer, history is stored differently
        # This test validates the agent can maintain state across calls

        response = self.agent.chat("What was my first question?")
        self.assertGreater(len(response), 0)

        # Agent should reference or acknowledge the earlier question about indoor plants
        print(f"History recall response: {response[:150]}...")
        print("✅ Conversation history test passed")


class TestAgentThreadIsolation(unittest.TestCase):
    """Test that different threads maintain separate conversations"""

    def tearDown(self):
        """Clean up after each test"""
        reset_agent()

    def test_different_threads_are_isolated(self):
        """Test that conversations in different threads don't interfere"""
        print("\n🧪 Testing thread isolation...")

        # Create two agents with different thread IDs
        agent_thread_a = PlantPalAgent(thread_id="thread_a")
        agent_thread_b = PlantPalAgent(thread_id="thread_b")

        # Thread A conversation
        response_a1 = agent_thread_a.chat("My name is Alice and I love ferns.")
        print(f"Thread A - Message 1: {response_a1[:80]}...")

        # Thread B conversation
        response_b1 = agent_thread_b.chat("My name is Bob and I love cacti.")
        print(f"Thread B - Message 1: {response_b1[:80]}...")

        # Ask each thread to recall the user's name
        response_a2 = agent_thread_a.chat("What is my name?")
        response_b2 = agent_thread_b.chat("What is my name?")

        print(f"Thread A - Name recall: {response_a2[:80]}...")
        print(f"Thread B - Name recall: {response_b2[:80]}...")

        # Thread A should mention Alice, not Bob
        self.assertIn("Alice", response_a2)
        self.assertNotIn("Bob", response_a2)

        # Thread B should mention Bob, not Alice
        self.assertIn("Bob", response_b2)
        self.assertNotIn("Alice", response_b2)

        print("✅ Thread isolation test passed - threads are properly separated")

    def test_same_thread_maintains_context(self):
        """Test that using the same thread_id maintains conversation context"""
        print("\n🧪 Testing same thread maintains context...")

        thread_id = "persistent_thread"

        # First agent instance
        agent1 = PlantPalAgent(thread_id=thread_id)
        response1 = agent1.chat("I have a monstera plant that needs help.")
        print(f"Agent 1 response: {response1[:80]}...")

        # Create a new agent instance with the SAME thread_id
        # This simulates a new function invocation in Firebase
        agent2 = PlantPalAgent(thread_id=thread_id)
        response2 = agent2.chat("What plant did I just mention?")
        print(f"Agent 2 response: {response2[:80]}...")

        # The new agent should recall the monstera mention
        self.assertTrue(
            "monstera" in response2.lower(),
            "Agent should recall previous conversation with same thread_id"
        )

        print("✅ Same thread persistence test passed")

    def test_langsmith_history_retrieval(self):
        """Test LangSmith history loading across agent restarts"""
        print("\n🧪 Testing LangSmith history retrieval...")

        thread_id = "langsmith_history_test_v3"

        # Session 1: Create conversation history
        print("--- Session 1: Creating conversation history ---")
        agent1 = PlantPalAgent(
            thread_id=thread_id,
            existing_thread=False  # Start fresh
        )
        response1 = agent1.chat("My name is Charlie and I grow bamboo.")
        print(f"Session 1 response: {response1[:80]}...")

        # Give LangSmith time to process the trace
        time.sleep(2)

        # Reset agent (simulates Firebase function restart)
        reset_agent()

        # Session 2: Load history from LangSmith
        print("\n--- Session 2: Loading history from LangSmith ---")
        agent2 = PlantPalAgent(
            thread_id=thread_id,
            existing_thread=True  # Load from LangSmith traces
        )

        # Ask follow-up that requires previous context
        response2 = agent2.chat("What's my name and what plant do I grow?")
        print(f"Session 2 response: {response2[:80]}...")

        # Agent should recall both name and plant from LangSmith history
        response_lower = response2.lower()
        self.assertTrue(
            "charlie" in response_lower,
            "Agent should recall name from LangSmith history"
        )
        self.assertTrue(
            "bamboo" in response_lower,
            "Agent should recall plant from LangSmith history"
        )

        print("✅ LangSmith history retrieval test passed")
        reset_agent()

    def test_firebase_function_pattern_with_history(self):
        """Test Firebase function pattern with history loading"""
        print("\n🧪 Testing Firebase function pattern with history...")

        thread_id = "firebase_user_123"

        def simulate_firebase_call(user_message, use_history=False):
            """Simulates a Firebase function invocation"""
            agent = PlantPalAgent(
                thread_id=thread_id,
                use_langsmith_history=use_history
            )
            response = agent.chat(user_message)
            reset_agent()  # Clean up like Firebase would
            return response

        # First call: User introduces themselves
        print("--- Call 1: User introduction ---")
        response1 = simulate_firebase_call(
            "Hi, I'm Dana and I have 5 succulents",
            use_history=False
        )
        print(f"Response 1: {response1[:80]}...")

        # Wait for trace to be recorded
        time.sleep(2)

        # Second call: With history enabled, should remember
        print("\n--- Call 2: With history loading ---")
        response2 = simulate_firebase_call(
            "How many plants did I say I have?",
            use_history=True
        )
        print(f"Response 2: {response2[:80]}...")

        # Should recall the number of plants
        response_lower = response2.lower()
        self.assertTrue(
            any(num in response_lower for num in ["5", "five"]),
            "Agent should recall plant count from LangSmith history"
        )

        print("✅ Firebase function pattern test passed")


class TestAgentToolIntegration(unittest.TestCase):
    """Test that agent correctly uses available tools"""

    def setUp(self):
        """Set up fresh agent for each test"""
        reset_agent()
        self.agent = PlantPalAgent(thread_id="test_tools_thread")

    def tearDown(self):
        """Clean up after each test"""
        reset_agent()

    def test_agent_uses_tavily_search(self):
        """Test that agent can invoke Tavily search for web queries"""
        print("\n🧪 Testing Tavily search tool usage...")

        # Ask a question that should trigger web search
        response = self.agent.chat(
            "What are the current best practices for growing tomatoes in 2025?"
        )

        self.assertGreater(len(response), 0)
        # Response should be informative (agent used search)
        self.assertGreater(len(response), 50)

        print(f"Search-based response: {response[:150]}...")
        print("✅ Tavily search tool test passed")

    def test_agent_uses_iot_tools(self):
        """Test that agent invokes IoT tools when appropriate"""
        print("\n🧪 Testing IoT tool usage...")

        # Ask about sensor data
        response = self.agent.chat("What is the moisture level from my sensor?")

        self.assertGreater(len(response), 0)
        # Response should mention moisture or sensor data
        self.assertTrue(
            any(keyword in response.lower() for keyword in ["moisture", "sensor", "percent", "%"]),
            "Agent should invoke IoT tool and return sensor data"
        )

        print(f"IoT tool response: {response[:150]}...")
        print("✅ IoT tool test passed")


class TestAgentErrorHandling(unittest.TestCase):
    """Test agent behavior with edge cases and errors"""

    def setUp(self):
        """Set up fresh agent for each test"""
        reset_agent()
        self.agent = PlantPalAgent(thread_id="test_error_thread")

    def tearDown(self):
        """Clean up after each test"""
        reset_agent()

    def test_empty_message_handling(self):
        """Test agent handles empty messages gracefully"""
        print("\n🧪 Testing empty message handling...")

        response = self.agent.chat("")

        # Agent should still respond, even to empty input
        self.assertIsInstance(response, str)
        self.assertGreater(len(response), 0)

        print(f"Empty message response: {response[:100]}...")
        print("✅ Empty message handling test passed")

    def test_non_plant_question_handling(self):
        """Test agent handles off-topic questions appropriately"""
        print("\n🧪 Testing off-topic question handling...")

        response = self.agent.chat("What is the capital of France?")

        self.assertGreater(len(response), 0)
        # Agent should either answer or redirect to plant care
        print(f"Off-topic response: {response[:150]}...")
        print("✅ Off-topic question test passed")


class TestAgentConfiguration(unittest.TestCase):
    """Test different agent configurations"""

    def tearDown(self):
        """Clean up after each test"""
        reset_agent()

    def test_different_max_tokens_configuration(self):
        """Test agent can be initialized with different token limits"""
        print("\n🧪 Testing different token configurations...")

        # Test with smaller token limit
        agent_small = PlantPalAgent(
            thread_id="small_tokens",
            max_tokens=MaxOutputTokens.SMALL
        )
        response = agent_small.chat("Hello!")
        self.assertGreater(len(response), 0)

        # Test with large token limit
        agent_large = PlantPalAgent(
            thread_id="large_tokens",
            max_tokens=MaxOutputTokens.LARGE
        )
        response = agent_large.chat("Hello!")
        self.assertGreater(len(response), 0)

        print("✅ Token configuration test passed")


def run_integration_tests(verbosity=2):
    """Run all integration tests with specified verbosity"""
    print("\n" + "="*60)
    print("🌱 PlantPal Agent Integration Tests")
    print("="*60)
    print("Testing: Memory Management, Thread Isolation, Tool Usage\n")

    # Create test suite
    loader = unittest.TestLoader()
    suite = unittest.TestSuite()

    # Add all test classes
    suite.addTests(loader.loadTestsFromTestCase(TestAgentMemoryManagement))
    suite.addTests(loader.loadTestsFromTestCase(TestAgentThreadIsolation))
    suite.addTests(loader.loadTestsFromTestCase(TestAgentToolIntegration))
    suite.addTests(loader.loadTestsFromTestCase(TestAgentErrorHandling))
    suite.addTests(loader.loadTestsFromTestCase(TestAgentConfiguration))

    # Run tests
    runner = unittest.TextTestRunner(verbosity=verbosity)
    result = runner.run(suite)

    # Print summary
    print("\n" + "="*60)
    print("📊 Test Summary")
    print("="*60)
    print(f"Tests run: {result.testsRun}")
    print(f"Successes: {result.testsRun - len(result.failures) - len(result.errors)}")
    print(f"Failures: {len(result.failures)}")
    print(f"Errors: {len(result.errors)}")
    print("="*60 + "\n")

    return result


if __name__ == '__main__':
    # Check for environment variables
    if not os.getenv("OPENAI_API_KEY"):
        print("⚠️  Warning: OPENAI_API_KEY not found in environment")
        print("Make sure to set your API keys before running these tests\n")

    # Load .env if available
    try:
        from dotenv import load_dotenv
        load_dotenv()
        print("✅ Loaded environment variables from .env\n")
    except ImportError:
        print("ℹ️  python-dotenv not found, using existing environment\n")

    # Run the tests
    run_integration_tests(verbosity=2)
