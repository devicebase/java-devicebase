# Contributing to DeviceBase Java SDK

Thank you for your interest in contributing to the DeviceBase Java SDK!

## Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/java-devicebase.git`
3. Navigate to the project directory: `cd java-devicebase`

## Development Setup

### Prerequisites

- Java 11 or higher
- Gradle 8.x (or use the included Gradle Wrapper)

### Building

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run checkstyle
./gradlew checkstyleMain
```

## Code Style

This project follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with some modifications:

- 4-space indentation
- No wildcard imports
- Maximum line length: 100 characters
- Javadoc required for all public classes and methods

## Pull Request Process

1. Ensure all tests pass: `./gradlew test`
2. Ensure code passes checkstyle: `./gradlew checkstyleMain`
3. Update documentation if needed
4. Request a review from a project maintainer

## Reporting Issues

Please report issues on the [GitHub Issue Tracker](https://github.com/uusense/java-devicebase/issues).

Include the following information:

- SDK version
- Java version
- Operating system
- Steps to reproduce
- Expected vs actual behavior

## License

By contributing, you agree that your contributions will be licensed under the MIT License.