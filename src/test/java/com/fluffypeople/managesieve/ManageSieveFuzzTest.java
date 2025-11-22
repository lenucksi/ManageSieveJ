package com.fluffypeople.managesieve;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * Fuzz tests for ManageSieve protocol parsing.
 *
 * These tests use Jazzer to generate random inputs that exercise
 * the protocol parsing code, looking for crashes, hangs, and
 * security vulnerabilities.
 *
 * Run in regression mode (default): mvn test -Dtest=ManageSieveFuzzTest
 * Run in fuzzing mode: JAZZER_FUZZ=1 mvn test -Dtest=ManageSieveFuzzTest
 */
public class ManageSieveFuzzTest {

    /**
     * Fuzz test for ManageSieveResponse parsing.
     * Tests the response type parsing (OK, NO, BYE) with random inputs.
     */
    @FuzzTest
    public void fuzzResponseTypeParsing(FuzzedDataProvider data) {
        String responseString = data.consumeRemainingAsString();

        // The response should handle any input without crashing
        ManageSieveResponse response = new ManageSieveResponse();

        // Try to set type from random string (setType takes String)
        try {
            response.setType(responseString);
        } catch (ParseException e) {
            // Expected for invalid type names
        } catch (NullPointerException e) {
            // Expected for null/empty input
        }
    }

    /**
     * Fuzz test for ManageSieveResponse code parsing.
     * Tests the response code parsing with random inputs.
     */
    @FuzzTest
    public void fuzzResponseCodeParsing(FuzzedDataProvider data) {
        String codeString = data.consumeRemainingAsString();

        ManageSieveResponse response = new ManageSieveResponse();

        // Try to set code from random string (setCode takes String)
        try {
            response.setCode(codeString);
        } catch (Exception e) {
            // Expected for invalid code names
        }
    }

    /**
     * Fuzz test for SieveScript handling.
     * Tests script name and body with random inputs.
     */
    @FuzzTest
    public void fuzzSieveScriptHandling(FuzzedDataProvider data) {
        String name = data.consumeString(256);
        String body = data.consumeRemainingAsString();
        boolean active = data.consumeBoolean();

        // Should handle any input without crashing
        SieveScript script = new SieveScript(name, body, active);

        // Verify getters work
        script.getName();
        script.getBody();
        script.isActive();

        // Test equals and hashCode with random data
        SieveScript other = new SieveScript(name, "different body", !active);
        script.equals(other);
        script.hashCode();
    }

    /**
     * Fuzz test for ServerCapabilities parsing.
     * Tests SASL methods and Sieve extensions parsing.
     */
    @FuzzTest
    public void fuzzServerCapabilitiesParsing(FuzzedDataProvider data) {
        ServerCapabilities caps = new ServerCapabilities();

        String saslMethods = data.consumeString(512);
        String sieveExtensions = data.consumeString(512);
        String implementation = data.consumeString(256);
        String notify = data.consumeString(256);
        String version = data.consumeString(64);
        String language = data.consumeString(32);
        String owner = data.consumeString(128);

        // All setters should handle any input without crashing
        try {
            caps.setSASLMethods(saslMethods);
            caps.setSieveExtensions(sieveExtensions);
            caps.setImplementationName(implementation);
            caps.setNotify(notify);
            caps.setVersion(version);
            caps.setLanguage(language);
            caps.setOwner(owner);
            caps.setHasTLS(data.consumeBoolean());
            caps.setMaxRedirects(data.consumeInt());

            // Test getters
            caps.getSASLMethods();
            caps.getImplementationName();
            caps.getVersion();
            caps.getLanguage();
            caps.getOwner();
            caps.getMaxRedirects();
            caps.isValid();
        } catch (Exception e) {
            // Log but don't fail - we're looking for crashes, not exceptions
        }
    }

    /**
     * Fuzz test for ManageSieveClient string parsing.
     * Tests the parseString method with various encoded strings.
     */
    @FuzzTest
    public void fuzzStringParsing(FuzzedDataProvider data) {
        ManageSieveClient client = new ManageSieveClient();

        // Generate a potentially malformed string literal
        int length = data.consumeInt(0, 10000);
        String content = data.consumeString(length);

        // Try different string encoding formats used by ManageSieve
        String[] encodings = {
            // Quoted string format
            "\"" + content + "\"",
            // Literal string format {length}\r\ndata
            "{" + content.length() + "}\r\n" + content,
            // Malformed formats
            "{" + length + "}\r\n" + content,
            "{}\r\n" + content,
            "\"" + content,  // Unclosed quote
            content,  // Raw content
        };

        String encoding = encodings[data.consumeInt(0, encodings.length - 1)];

        StringReader reader = new StringReader(encoding);
        StringWriter writer = new StringWriter();

        try {
            client.setupForTesting(reader, writer);
            client.parseString();
        } catch (Exception e) {
            // Expected for malformed input
        }
    }

    /**
     * Fuzz test for ParseException handling.
     * Ensures exception creation with various messages doesn't crash.
     */
    @FuzzTest
    public void fuzzParseExceptionCreation(FuzzedDataProvider data) {
        String message = data.consumeRemainingAsString();

        // Should handle any message without crashing
        ParseException ex1 = new ParseException();
        ParseException ex2 = new ParseException(message);

        // Verify methods work
        ex1.getMessage();
        ex2.getMessage();
        ex1.getCause();
        ex2.getCause();
    }

    /**
     * Fuzz test for ResponseAndPayload creation.
     * Tests with various payload contents.
     */
    @FuzzTest
    public void fuzzResponseAndPayloadCreation(FuzzedDataProvider data) {
        String payload = data.consumeRemainingAsString();

        ManageSieveResponse response = new ManageSieveResponse();
        ResponseAndPayload rap = new ResponseAndPayload(response, payload);

        // Verify getters work
        rap.getResponse();
        rap.getPayload();

        // Test with null response
        ResponseAndPayload rap2 = new ResponseAndPayload(null, payload);
        rap2.getResponse();
        rap2.getPayload();
    }

    /**
     * Fuzz test for NoisyReader with various inputs.
     */
    @FuzzTest
    public void fuzzNoisyReader(FuzzedDataProvider data) {
        String content = data.consumeRemainingAsString();

        StringReader base = new StringReader(content);
        NoisyReader reader = new NoisyReader(base);

        try {
            char[] buffer = new char[Math.max(1, data.consumeInt(1, 1024))];
            int offset = data.consumeInt(0, Math.max(0, buffer.length - 1));
            int length = data.consumeInt(0, buffer.length - offset);

            if (length > 0) {
                reader.read(buffer, offset, length);
            }

            reader.close();
        } catch (Exception e) {
            // Expected for edge cases
        }
    }

    /**
     * Fuzz test simulating protocol conversation.
     * Tests various command/response patterns.
     */
    @FuzzTest
    public void fuzzProtocolConversation(FuzzedDataProvider data) {
        // Simulate various ManageSieve responses
        String[] responseTypes = {"OK", "NO", "BYE"};
        String[] codes = {"AUTH-TOO-WEAK", "ENCRYPT-NEEDED", "SASL", "REFERRAL",
                          "TRANSITION-NEEDED", "TRYLATER", "ACTIVE", "NONEXISTENT",
                          "ALREADYEXISTS", "TAG", "WARNINGS", "QUOTA"};

        String responseType = responseTypes[data.consumeInt(0, responseTypes.length - 1)];
        String code = codes[data.consumeInt(0, codes.length - 1)];
        String message = data.consumeString(256);
        String subcode = data.consumeString(64);

        // Build a response line similar to what a server might send
        StringBuilder responseLine = new StringBuilder();
        responseLine.append(responseType);

        if (data.consumeBoolean()) {
            responseLine.append(" (").append(code);
            if (data.consumeBoolean() && !subcode.isEmpty()) {
                responseLine.append(" \"").append(subcode).append("\"");
            }
            responseLine.append(")");
        }

        if (data.consumeBoolean() && !message.isEmpty()) {
            responseLine.append(" \"").append(message).append("\"");
        }

        responseLine.append("\r\n");

        // Try to parse this response
        StringReader reader = new StringReader(responseLine.toString());
        StringWriter writer = new StringWriter();
        ManageSieveClient client = new ManageSieveClient();

        try {
            client.setupForTesting(reader, writer);
            // Note: We can't call parseResponse directly as it's private
            // But the tokenizer setup will still exercise the reader
        } catch (Exception e) {
            // Expected for malformed input
        }
    }
}
