"""
PlantPal AI Agent Configuration
Sets up LangChain agent with conversation memory and tools
"""
import enum
import os
from typing import List, Optional, Any, Dict

from langchain.tools import BaseTool
from langchain_tavily import TavilySearch
from langchain.agents import create_agent
from langchain.agents.middleware import SummarizationMiddleware
from langchain.chat_models import init_chat_model, BaseChatModel
from langgraph.checkpoint.memory import InMemorySaver
from langchain_core.messages import BaseMessage, HumanMessage, AIMessage
from langchain_core.runnables import RunnableConfig
from langsmith import Client, traceable
import langsmith as ls


# For tracking of Agent calls and langsmith client
os.environ["LANGSMITH_TRACING"] = "true"
langsmith_client = Client()

_system_prompt = """You are PlantPal, an expert AI assistant specialized in plant care and gardening.

## Your Role
You help users care for their plants by providing accurate information, actionable advice, and leveraging your available tools to access real-time data.

## Core Capabilities
- Plant identification and care recommendations
- Diagnosing plant health issues (yellowing leaves, pests, diseases, etc.)
- Watering schedules and soil moisture monitoring
- Seasonal care guidance and climate considerations
- IoT sensor data analysis and irrigation control

## Tool Usage Guidelines
1. **Tavily Search**: Always use this FIRST for plant-specific questions requiring current information (care guides, pest identification, disease treatment, etc.)
2. **Sensor Tools**: Use `get_moisture_data` and `get_sensor_id` to check real-time soil conditions
3. **Irrigation Control**: Use `control_irrigation` when users need to water their plants or adjust watering

## Interaction Style
- Be warm, friendly, and enthusiastic about plants
- Use conversational language as if talking to a fellow plant lover
- Ask clarifying questions when needed (e.g., "What type of plant?", "How much sunlight does it get?")
- Proactively suggest using your tools when relevant (e.g., "Would you like me to check your sensor data?")

## Important Behaviors
- If you don't know something, admit it honestly and offer to search for the answer
- Handle non-plant questions politely but remind users you specialize in plant care
- When giving advice, consider the user's specific situation (location, experience level, plant type)
- Cite sources when using web search results

## Response Format
- Keep responses concise but informative
- Use bullet points for care instructions or multiple steps
- Include relevant warnings (toxicity, overwatering risks, etc.) when applicable

Remember: Your goal is to help users become confident, successful plant parents!
"""


class MaxOutputTokens(enum.Enum):
    """Enum for maximum token settings which will
    be used in constructing BaseChatModel instance if
    not properly set long outputs will be cut off
    """
    SMALL = 4096
    MEDIUM = 8192
    LARGE = 16384   # max for gpt-4o


def get_thread_history(thread_id: str, project_name: str) -> List[BaseMessage]:
    """
    Retrieve conversation history for a thread from LangSmith.
    Reconstructs conversation from LLM runs.

    Args:
        thread_id: Unique identifier for the conversation thread
        project_name: LangSmith project name

    Returns:
        List of BaseMessage objects representing conversation history
        (HumanMessage and AIMessage only, system messages excluded as will be add by
        agent create method)
    """

    # Filter runs by thread_id metadata
    filter_string = (
        f'and(in(metadata_key, ["session_id","conversation_id",'
        f'"thread_id"]), eq(metadata_value, "{thread_id}"))'
    )

    # Get all runs for this thread (llm type for model calls)
    runs = [
        r for r in langsmith_client.list_runs(
            project_name=project_name,
            filter=filter_string,
            run_type="llm"
        )
    ]

    if not runs:
        print(f"No history found for thread: {thread_id}")
        return []

    # Sort by start time (oldest first to maintain conversation order)
    runs = sorted(runs, key=lambda run: run.start_time)

    print(f"Found {len(runs)} runs for thread: {thread_id}")

    # Build message history from runs
    messages: List[BaseMessage] = []

    for run in runs:
        if not run.inputs or not run.outputs:
            continue

        # Extract input messages (skip system message)
        input_messages = run.inputs.get('messages', [[]])[0]
        for msg in input_messages:
            msg_type = msg['kwargs'].get('type')
            content = msg['kwargs'].get('content', '')

            # Only add human messages (system is handled by agent prompt)
            if msg_type == 'human':
                messages.append(HumanMessage(content=content))

        # Extract AI response from outputs
        generations = run.outputs.get('generations', [[]])
        if generations and generations[0]:
            ai_response = generations[0][0].get('message', {})
            ai_content = ai_response.get('kwargs', {}).get('content', '')
            if ai_content:
                messages.append(AIMessage(content=ai_content))

    # Limit to most recent 25 messages to prevent excessive context
    if len(messages) > 25:
        messages = messages[-25:]
        print("Truncated history to most recent 25 messages")

    print(f"Reconstructed {len(messages)} messages from history")
    return messages


# Middleware configuration
# SummarizationMiddleware will automatically summarize old messages
# when token count exceeds the trigger threshold
def create_summarization_middleware(
    summary_model: str = "gpt-4o-mini",
    token_trigger: int = 4000,
    keep_messages: int = 20
) -> SummarizationMiddleware:
    """
    Create middleware that summarizes conversation history
    when it gets too long, keeping recent messages intact.

    Args:
        summary_model: Model to use for summarization (cheaper/faster)
        token_trigger: Token count that triggers summarization
        keep_messages: Number of recent messages to keep unsummarized

    Returns:
        Configured SummarizationMiddleware instance
    """
    return SummarizationMiddleware(
        model=summary_model,
        max_tokens_before_summary=token_trigger,
        messages_to_keep={"messages": keep_messages},
    )


class PlantPalAgent:
    """
    PlantPal AI Agent with conversation memory, IoT tools, and Tavily Search
    """
    def __init__(
            self,
            thread_id: str,
            max_tokens: MaxOutputTokens = MaxOutputTokens.LARGE,
            model: str = "gpt-4o",
            existing_thread: bool = False,
            ):

        """Initialize the agent with tools and memory

        Args:
            thread_id: Unique identifier for conversation thread
            max_tokens: Maximum tokens for model output
            model: LLM model to use
            use_langsmith_history: If True, load conversation history
                                  from LangSmith traces
            langsmith_project: LangSmith project name for history retrieval
        """

        print("Initializing PlantPal Agent...")

        self.thread_id = thread_id
        self.existing_thread = existing_thread

        self.tools = self._setup_tools()
        self.llm_model: BaseChatModel = init_chat_model(
                            model=model,
                            max_tokens=max_tokens.value
                            )

        self.memory = InMemorySaver()
        self.config: RunnableConfig = {
            "configurable": {
                "thread_id": thread_id,
                "metadata": {
                    "session_id": thread_id,
                    "thread_id": thread_id
                }
            }
        }

        # Load existing conversation history from LangSmith if enabled
        if self.existing_thread:
            print(f"Loading conversation history for thread: {thread_id}")
            self.chat_history = get_thread_history(
                thread_id=thread_id,
                project_name=os.getenv("LANGSMITH_PROJECT")
            )
        else:
            self.chat_history = []
        # Create summarization middleware to manage long conversations
        summarization_middleware = create_summarization_middleware(
            summary_model="gpt-4o-mini",
            token_trigger=4000,
            keep_messages=20
        )

        self.agent = create_agent(
                            model=self.llm_model,
                            system_prompt=_system_prompt,
                            tools=self.tools,
                            middleware=[summarization_middleware],
                            checkpointer=self.memory
                            )

    def _setup_tools(self) -> List[BaseTool]:
        """Setup and return list of available tools"""
        tools: List[BaseTool] = []

        # 3rd party tools
        # Add Tavily Search tool for web search capabilities
        tavily_tool = TavilySearch(max_results=3)
        tools.append(tavily_tool)

        iot_tools_module = __import__(
            'tools.iot_tools',
            fromlist=['get_moisture_data', 'get_sensor_id',
                      'control_irrigation']
        )
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
            # On first call with existing thread, prepend chat history
            if self.chat_history:
                messages = self.chat_history + [HumanMessage(content=message)]
                # Clear history so we don't prepend it again
                self.chat_history = []
            else:
                messages = [HumanMessage(content=message)]

            response = self.agent.invoke({
                "messages": messages
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
_current_thread_id: Optional[str] = None


def get_agent(
    thread_id: Optional[str] = None,
    existing_thread: bool = False
) -> PlantPalAgent:
    """
    Get or create the global agent instance
    Recreates agent if thread_id changes to ensure proper isolation

    Args:
        thread_id: Unique identifier for conversation thread
        existing_thread: If True, load conversation history from
                        LangSmith traces for long-running chats
    """
    global _agent_instance, _current_thread_id

    # Recreate agent if thread_id changed or doesn't exist
    if _agent_instance is None or _current_thread_id != thread_id:
        print(f"Creating new agent for thread: {thread_id}")
        _agent_instance = PlantPalAgent(
            thread_id=thread_id,
            existing_thread=existing_thread
        )
        _current_thread_id = thread_id
    else:
        print(f"Reusing existing agent for thread: {thread_id}")

    return _agent_instance


def reset_agent():
    """Reset the global agent instance and thread tracking"""
    global _agent_instance, _current_thread_id
    _agent_instance = None
    _current_thread_id = None
    print("Agent instance reset")


def delete_all_messages(state: Dict[str, Any]) -> Dict[str, Any]:
    """Clear all messages from conversation state"""
    return {"messages": []}
