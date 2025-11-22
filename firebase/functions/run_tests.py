#!/usr/bin/env python3
"""
Quick test runner for PlantPal agent integration tests
Usage: python run_tests.py [options]
"""

import sys
import os
import argparse

# Add current directory to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

def main():
    parser = argparse.ArgumentParser(
        description='Run PlantPal agent integration tests'
    )
    parser.add_argument(
        '--verbose', '-v',
        action='count',
        default=2,
        help='Increase verbosity (can be used multiple times)'
    )
    parser.add_argument(
        '--quiet', '-q',
        action='store_true',
        help='Minimal output'
    )
    parser.add_argument(
        '--test', '-t',
        type=str,
        help='Run specific test class or method (e.g., TestAgentMemoryManagement)'
    )

    args = parser.parse_args()

    # Set verbosity
    verbosity = 0 if args.quiet else args.verbose

    # Load environment FIRST
    try:
        from dotenv import load_dotenv
        load_dotenv()
        print("✅ Loaded environment variables from .env\n")
    except ImportError:
        print("ℹ️  python-dotenv not found, using system environment\n")

    # Check for required API keys AFTER loading .env
    if not os.getenv("OPENAI_API_KEY"):
        print("❌ Error: OPENAI_API_KEY not found in environment")
        print("Please ensure your .env file exists and contains OPENAI_API_KEY")
        print("Or set it as an environment variable\n")
        sys.exit(1)

    # Run specific test or all tests
    if args.test:
        print(f"Running specific test: {args.test}\n")
        import unittest
        from tests.integration import test_agent_integration

        loader = unittest.TestLoader()
        suite = loader.loadTestsFromName(
            f'tests.integration.test_agent_integration.{args.test}'
        )
        runner = unittest.TextTestRunner(verbosity=verbosity)
        result = runner.run(suite)

        sys.exit(0 if result.wasSuccessful() else 1)
    else:
        # Run all integration tests
        from tests.integration.test_agent_integration import (
            run_integration_tests
        )
        result = run_integration_tests(verbosity=verbosity)
        sys.exit(0 if result.wasSuccessful() else 1)


if __name__ == '__main__':
    main()
