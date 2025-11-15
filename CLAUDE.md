# ManageSieveJ Development Guide for Claude

## Project Overview

ManageSieveJ is a Java implementation of the ManageSieve protocol (RFC 5804) for managing Sieve mail filtering scripts on mail servers. This fork is maintained by lenucksi specifically as a dependency for the SieveEditor project.

## Key Information

- **Language**: Java 21 LTS (builds with Java 17 and 11 for compatibility testing)
- **Build System**: Maven
- **Testing Framework**: TestNG
- **License**: MIT
- **Distribution**: GitHub Packages (Maven)

## Project Structure

```
ManageSieveJ/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── module-info.java
│   │   │   └── com/fluffypeople/managesieve/
│   │   │       ├── ManageSieveClient.java       # Core client (869 LOC)
│   │   │       ├── ManageSieveResponse.java     # Response handling (221 LOC)
│   │   │       ├── ServerCapabilities.java      # Capability parsing (155 LOC)
│   │   │       ├── SieveScript.java             # Data model (92 LOC)
│   │   │       ├── examples/                    # Example code
│   │   │       └── xml/                         # XML utilities (792 LOC)
│   │   └── resources/
│   │       ├── SieveXML.xsd
│   │       └── XMLtoSieve.xsl
│   └── test/
│       └── java/
│           └── com/fluffypeople/managesieve/
│               └── ManageSieveClientTest.java
├── pom.xml
├── .github/
│   └── workflows/
└── CLAUDE.md (this file)
```

## Core Functionality

### ManageSieve Protocol Implementation
- **Connection Management**: Connect, STARTTLS, logout
- **Authentication**: SASL (PLAIN, CRAM-MD5, etc.)
- **Script Management**: List, get, put, delete, set active, rename, check
- **SSL/TLS**: Certificate validation, secure connections
- **Capability Negotiation**: Server capability parsing

### Key Classes
1. **ManageSieveClient** - Main API entry point for protocol operations
2. **ManageSieveResponse** - Structured server response (OK/NO/BYE with codes)
3. **ServerCapabilities** - Holds server capabilities (SASL, Sieve extensions, etc.)
4. **SieveScript** - Model object for scripts (name, body, active status)
5. **XML utilities** - Convert Sieve scripts to/from XML representation

## Development Workflow

### Building
```bash
mvn clean compile
```

### Testing
```bash
mvn test
```

### Packaging
```bash
mvn package
```

### Publishing to GitHub Packages
```bash
mvn deploy
```

## Testing Guidelines

### Current Test Coverage
- **Coverage**: ~3% (minimal)
- **Tested**: Unicode string parsing, basic connection state
- **Untested**: Protocol commands, SSL/TLS, authentication, XML utilities

### Testing Priorities (in order)
1. **Protocol Operations** - Command formatting, response parsing, error handling
2. **Response/Capability Parsing** - All response codes, capability string parsing
3. **Model Classes** - equals()/hashCode() contracts
4. **SSL/TLS Validation** - Certificate hostname matching (security-critical)
5. **SASL Authentication** - Multi-step auth flows
6. **XML Utilities** - Sieve ↔ XML conversion
7. **Integration Tests** - Full protocol flows with mock servers

### Test Infrastructure
- **TestNG** - Primary testing framework
- **Mockito** - Mocking for network/SSL/SASL components
- **AssertJ** - Fluent assertions
- **JaCoCo** - Code coverage reporting

## Common Tasks

### Adding New Tests
1. Create test class in `src/test/java/com/fluffypeople/managesieve/`
2. Use TestNG annotations: `@Test`, `@DataProvider`, `@BeforeMethod`, etc.
3. Mock external dependencies (sockets, SSL, SASL) using Mockito
4. Run `mvn test` to verify

### Adding New Protocol Features
1. Update `ManageSieveClient.java` with new command method
2. Update `ManageSieveResponse.java` if new response codes are needed
3. Add comprehensive tests for the new feature
4. Update documentation

### Debugging Protocol Issues
- Use `NoisyReader` for detailed stream debugging
- Check `ManageSieveClient.setupForTesting()` for test harness support
- Protocol traces are logged via java.util.logging at FINEST level

## CI/CD

### GitHub Actions Workflows
- **CI** (`ci.yml`) - Build and test on every push/PR
- **Release** (`release.yml`) - Automated releases with semantic versioning
- **Package** (`package.yml`) - Publish to GitHub Packages

### Publishing Releases
Releases are automated via GitHub Actions. When ready to release:
1. Create and push a version tag (e.g., `v0.3.2`)
2. GitHub Actions will build, test, and publish to GitHub Packages
3. Update SieveEditor's pom.xml to use the new version

## Dependencies for SieveEditor

To use this library in SieveEditor, add to pom.xml:

```xml
<dependency>
    <groupId>com.fluffypeople</groupId>
    <artifactId>managesievej</artifactId>
    <version>0.3.2-SNAPSHOT</version>
</dependency>

<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/lenucksi/ManageSieveJ</url>
    </repository>
</repositories>
```

## Architecture Notes

### String Encoding
- All protocol communication uses UTF-8
- Two string formats supported:
  - **Quoted strings**: `"escaped \"string\""`
  - **Literal strings**: `{length}CRLF<data>` (for binary-safe data)

### Response Parsing
- Uses `StreamTokenizer` for protocol parsing
- Supports both quoted and literal string formats
- Handle multi-line responses with capabilities

### SSL/TLS
- STARTTLS command upgrades plain connection to TLS
- Certificate hostname validation against CN and SubjectAlternativeName
- TODO: Wildcard hostname support (see line 237, 248)

### SASL Authentication
- Pluggable via javax.security.sasl framework
- Supports challenge-response sequences
- Server advertises supported mechanisms via CAPABILITY

## Important Implementation Details

### Thread Safety
- ManageSieveClient methods are `synchronized`
- One client instance = one connection
- Not designed for concurrent command execution on same client

### Error Handling
- `ParseException` for protocol parsing errors
- `IOException` for network/stream errors
- `SaslException` for authentication failures
- Server responses include human-readable messages (should be shown to users)

### Encoding Constraints
- Escaped strings limited to 1024 bytes (MAX_ESCAPED_STRING_LENGTH)
- Larger data must use literal string format
- UTF-8 byte length used, not character count

## Known Issues / TODOs

1. **Wildcard hostname support** - Lines 237, 248 in ManageSieveClient.java
2. **Minimal test coverage** - ~3% coverage, needs comprehensive testing
3. **No integration tests** - No mock server for full protocol testing
4. **Module system** - Uses Java modules but not widely tested

## Contact & Maintenance

This fork is maintained by lenucksi for SieveEditor compatibility.

Original author: Osric Wilkinson <osric@fluffypeople.com>

## Resources

- **ManageSieve RFC**: http://tools.ietf.org/html/rfc5804
- **Sieve RFC**: http://tools.ietf.org/html/rfc5228
- **Original Repository**: https://github.com/Moosemorals/ManageSieveJ
- **This Fork**: https://github.com/lenucksi/ManageSieveJ
- **SieveEditor**: https://github.com/lenucksi/SieveEditor
