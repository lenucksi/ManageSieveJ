# Claude Code Harness for ManageSieveJ

This directory contains the Claude Code harness configuration for ManageSieveJ development.

## Structure

```text
.claude/
├── commands/          # Slash commands for common tasks
│   ├── build.md      # Build the project
│   ├── test.md       # Run tests
│   ├── clean.md      # Clean build artifacts
│   ├── package.md    # Create JAR package
│   ├── coverage.md   # Run tests with coverage
│   └── verify.md     # Full Maven verification
├── hooks/            # Session hooks
│   └── SessionStart  # Environment verification on session start
└── README.md         # This file
```

## Available Commands

### `/build`

Builds the ManageSieveJ project using Maven (`mvn clean compile`).

### `/test`

Runs the TestNG test suite (`mvn test`).

### `/clean`

Cleans all Maven build artifacts (`mvn clean`).

### `/package`

Compiles, tests, and packages the project into a JAR file (`mvn clean package`).

### `/coverage`

Runs tests with JaCoCo code coverage analysis (`mvn clean test jacoco:report`).

### `/verify`

Runs the full Maven verification lifecycle including all tests and packaging (`mvn clean verify`).

## Hooks

### SessionStart

Automatically runs when a Claude Code session starts. It:

- Verifies Java and Maven installations
- Checks for pom.xml
- Displays project information
- Lists available commands

## Development Workflow

1. Start a Claude Code session - SessionStart hook will verify your environment
2. Use `/build` to compile the code
3. Use `/test` to run tests
4. Use `/coverage` to check test coverage
5. Use `/package` to create the final JAR artifact

## Project Information

- **Language**: Java 21 LTS (compatible with Java 17 and 11)
- **Build System**: Maven
- **Testing Framework**: TestNG
- **Test Coverage**: JaCoCo
- **Distribution**: GitHub Packages

For more information, see CLAUDE.md in the project root.
