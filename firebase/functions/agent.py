"""
PlantPal AI Agent Configuration
Sets up LangChain agent with conversation memory and tools
"""
import os
from typing import List, Optional
from langchain.agents import AgentExecutor, create_openai_functions_agent
from langchain.memory import ConversationBufferWindowMemory
from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain.schema import BaseMessage
from langchain.tools import BaseTool
from langchain_tavily import TavilySearch
from langgraph.prebuilt import create_react_agent
from langchain.chat_models import init_chat_model
from langgraph.checkpoint.memory import MemorySaver

# For track of Agent calls
os.environ["LANGSMITH_TRACING"] = "true"



class PlantPalAgent:
    """
    PlantPal AI Agent with conversation memory, with IoT tools, and Tavily Search
    """

    def __init__(self, thread_id: Optional[str] = None):
        """Initialize the agent with tools and memory"""
        print("Initializing PlantPal Agent...")

        # Lightweight agent model
        self.tools = self._setup_tools()
        self.llm_model = init_chat_model("gpt-4o", model_provider="openai")
        self.memory = MemorySaver()
        self.config = {"configurable" : {"thread_id": thread_id}}

        self.agent = create_react_agent(self.llm_model, self.tools, checkpointer=self.memory)

    def _setup_tools(self) -> List[BaseTool]:
        """Setup and return list of available tools"""
        tools: List[BaseTool] = []

        # 3rd party tools
        # Add Tavily Search tool for web search capabilities
        tavily_tool = TavilySearch(max_results=3)
        tools.append(tavily_tool)

        iot_tools_module = __import__('tools.iot_tools', fromlist=['get_moisture_data', 'get_sensor_id', 'control_irrigation'])
        tools.append(iot_tools_module.get_moisture_data)
        tools.append(iot_tools_module.get_sensor_id)
        tools.append(iot_tools_module.control_irrigation)

        return tools

    def chat(self, message: str) -> str:
        """
        Process a chat message and return response

        Args:
            message: User's input message

        Returns:
            Agent's response as string
        """
        try:
            response = self.agent.invoke({
                "messages": [("user", message)]
            }, config=self.config)

            return response["messages"][-1].content

        except Exception as e:
            print(f"Error in agent chat: {e}")
            return ("I'm sorry, I encountered an error processing your "
                    "request. Please try again.")

    def get_conversation_history(self) -> List[BaseMessage]:
        """Get the current conversation history"""
        return self.memory.chat_memory.messages

    def clear_memory(self):
        """Clear the conversation memory"""
        self.memory.clear()
        print("Conversation memory cleared")


# Global agent instance - will be initialized when needed
_agent_instance: Optional[PlantPalAgent] = None


def get_agent(thread_id: Optional[str] = None) -> PlantPalAgent:
    """
    Get or create the global agent instance
    This ensures we reuse the same agent across function calls
    """
    global _agent_instance

    if _agent_instance is None:
        _agent_instance = PlantPalAgent(thread_id=thread_id)

    return _agent_instance


def reset_agent():
    """Reset the global agent instance"""
    global _agent_instance
    _agent_instance = None
    print("Agent instance reset")


if __name__ == "__main__":
    print("🌱 PlantPal Agent Initialization Test")
    print("=" * 40)
    print("Testing agent setup and basic chat...")

    try:
        from dotenv import load_dotenv
        load_dotenv()
        agent = get_agent(thread_id="test_thread")
        test_message = "Hello, PlantPal! Can you search the web for the weather today in Portland Oregon?"
        response = agent.chat(test_message)
        print(f"Agent response: {response}")
        print("-" * 40)
        test_message2 = "Also, can you get the moisture data from my sensor?"
        response2 = agent.chat(test_message2)
        print(f"Agent response: {response2}")

    except Exception as e:
        print(f"Error during agent test: {e}")
