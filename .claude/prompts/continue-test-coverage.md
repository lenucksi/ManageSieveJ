# Start Prompt: ManageSieveJ Test Coverage to 80%

## Context

ManageSieveJ is a Java protocol library for ManageSieve (RFC 5804). Recent work:

- Migrated from TestNG to JUnit 5.11.4
- Added Jazzer 0.26.0 for fuzz testing (9 fuzz tests)
- Created quick-win tests (ParseException, ResponseAndPayload, NoisyReader, SieveScript)
- Coverage improved from ~5% to ~23%

## Current State

**Test files exist:**

- `ManageSieveClientTest.java` - Unicode string parsing tests
- `ManageSieveClientStringEncodingTest.java` - Literal string encoding tests
- `ManageSieveResponseTest.java` - Response type/code tests
- `ServerCapabilitiesTest.java` - Capability parsing tests
- `SieveScriptTest.java` - Script model tests (equals/hashCode)
- `ParseExceptionTest.java` - Exception tests
- `ResponseAndPayloadTest.java` - Value object tests
- `NoisyReaderTest.java` - Debug reader tests
- `ManageSieveFuzzTest.java` - Jazzer fuzz tests

**Coverage by class (from JaCoCo):**

| Class | Instructions Covered | Status |
|-------|---------------------|--------|
| NoisyReader | 100% | ✅ Complete |
| ParseException | 100% | ✅ Complete |
| ResponseAndPayload | 100% | ✅ Complete |
| SieveScript | 100% | ✅ Complete |
| ManageSieveResponse | 100% | ✅ Complete |
| ManageSieveResponse.Type | 100% | ✅ Complete |
| ManageSieveResponse.Code | 100% | ✅ Complete |
| ServerCapabilities | 100% | ✅ Complete |
| ManageSieveClient | ~17% | ⚠️ Needs work |
| XML utilities | 0% | ❌ Untested |
| Examples | 0% | ⏭️ Skip (not library code) |

## Task

1. **Update test plan documentation** in `/home/jo/kit/sieve/ManageSieveJ/docs/` or create if not exists
2. **Use the agent infrastructure** (test-generator, test-guardian agents) to generate remaining tests
3. **Target 80% overall coverage** focusing on:
   - `ManageSieveClient.java` - Core protocol operations (most critical)
   - Consider if XML utilities warrant testing or should be excluded

## ManageSieveClient Testing Strategy

The client has a `setupForTesting(Reader, Writer)` method allowing tests without network:

```java
@Test
void shouldParseServerResponse() throws Exception {
    ManageSieveClient client = new ManageSieveClient();
    String serverResponse = "OK \"Success\"\r\n";
    client.setupForTesting(new StringReader(serverResponse), new StringWriter());
    // Test protocol parsing
}
```

**Areas to test:**

- `parseString()` - Literal and quoted string parsing
- Response parsing - OK/NO/BYE with codes
- Capability negotiation
- STARTTLS handling (mock SSL context)
- SASL authentication flows
- Script operations (putscript, getscript, listscripts, etc.)

## Research Task

Research and report on:

1. **Mock frameworks** for testing SSL/SASL in Java (Mockito capabilities)
2. **Test fixture patterns** for protocol testing
3. **Any JUnit 5 extensions** that facilitate socket/stream testing
4. **Mutation testing** tools (PIT) for test quality assessment

## Deliverables

1. Updated test plan document with prioritized test cases
2. New test files achieving 80% coverage
3. Research report on test facilitation technology
4. Coverage report showing improvement

## Commands

```bash
# Run tests with coverage
cd /home/jo/kit/sieve/ManageSieveJ
mvn test jacoco:report

# View coverage report
xdg-open target/site/jacoco/index.html

# Run specific test class
mvn test -Dtest=ManageSieveClientTest
```
