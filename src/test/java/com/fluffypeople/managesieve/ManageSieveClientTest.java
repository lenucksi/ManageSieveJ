package com.fluffypeople.managesieve;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class ManageSieveClientTest {

    private ManageSieveClient client;
    private StringWriter out;

    @BeforeEach
    void setUp() {
        client = new ManageSieveClient();
        out = new StringWriter();
    }

    void setupClientWithInput(String input) {
        StringReader in = new StringReader(input);
        client.setupForTesting(in, out);
    }

    @Nested
    @DisplayName("Connection State")
    class ConnectionStateTests {

        @Test
        void shouldReportNotConnectedByDefault() {
            assertThat(client.isConnected()).isFalse();
        }

        @Test
        void shouldReturnNullCapabilitiesByDefault() {
            assertThat(client.getCapabilities()).isNull();
        }

        @Test
        void shouldGetAndSetSocketTimeout() throws Exception {
            client.setSocketTimeout(5000);
            int result = client.getSocketTimeout();

            assertThat(result).isEqualTo(5000);
        }

        @Test
        void shouldReturnDefaultSocketTimeout() throws Exception {
            int result = client.getSocketTimeout();

            assertThat(result).isZero();
        }

        @Test
        void shouldSetTimeoutMultipleTimes() throws Exception {
            client.setSocketTimeout(1000);
            assertThat(client.getSocketTimeout()).isEqualTo(1000);

            client.setSocketTimeout(30000);
            assertThat(client.getSocketTimeout()).isEqualTo(30000);

            client.setSocketTimeout(0);
            assertThat(client.getSocketTimeout()).isZero();
        }

        @Test
        void shouldReturnCapabilitiesAfterParsing() throws Exception {
            setupClientWithInput(
                    "\"IMPLEMENTATION\" \"TestSieve\"\r\n" +
                    "\"VERSION\" \"1.0\"\r\n" +
                    "\"SIEVE\" \"fileinto\"\r\n" +
                    "OK\r\n" +
                    "OK\r\n"
            );
            client.capability();

            ServerCapabilities caps = client.getCapabilities();
            assertThat(caps).isNotNull();
            assertThat(caps.getImplementationName()).isEqualTo("TestSieve");
            assertThat(caps.getVersion()).isEqualTo("1.0");
            assertThat(caps.hasSieveExtension("fileinto")).isTrue();
        }
    }

    @Nested
    @DisplayName("String Parsing")
    class StringParsingTests {

        @Test
        void shouldParseSimpleQuotedString() throws Exception {
            setupClientWithInput("\"hello\"\r\n");

            String result = client.parseString();

            assertThat(result).isEqualTo("hello");
        }

        @Test
        void shouldParseEmptyQuotedString() throws Exception {
            setupClientWithInput("\"\"\r\n");

            String result = client.parseString();

            assertThat(result).isEmpty();
        }

        @Test
        void shouldParseQuotedStringWithEscapedQuotes() throws Exception {
            setupClientWithInput("\"foo\\\"bar\"\r\n");

            String result = client.parseString();

            assertThat(result).isEqualTo("foo\"bar");
        }

        @Test
        void shouldParseQuotedStringWithEscapedBackslash() throws Exception {
            setupClientWithInput("\"path\\\\to\\\\file\"\r\n");

            String result = client.parseString();

            assertThat(result).isEqualTo("path\\to\\file");
        }

        @Test
        void shouldParseLiteralString() throws Exception {
            setupClientWithInput("{5}\r\nhello\r\n");

            String result = client.parseString();

            assertThat(result).isEqualTo("hello");
        }

        @Test
        void shouldParseEmptyLiteralString() throws Exception {
            setupClientWithInput("{0}\r\n");

            String result = client.parseString();

            assertThat(result).isEmpty();
        }

        @Test
        void shouldParseLiteralStringWithUnicode() throws Exception {
            setupClientWithInput("{9}\r\n日本語");

            String result = client.parseString();

            assertThat(result).isEqualTo("日本語");
        }

        @Test
        void shouldParseLiteralStringWithMixedContent() throws Exception {
            setupClientWithInput("{12}\r\nhello 世界");

            String result = client.parseString();

            assertThat(result).isEqualTo("hello 世界");
        }

        @Test
        void shouldThrowOnMissingQuoteOrBrace() throws Exception {
            setupClientWithInput("hello\r\n");

            assertThatThrownBy(() -> client.parseString())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting DQUOTE or {");
        }

        @Test
        void shouldThrowOnNumberInsteadOfString() throws Exception {
            setupClientWithInput("123\r\n");

            assertThatThrownBy(() -> client.parseString())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting DQUOTE or {");
        }

        @Test
        void shouldThrowOnLiteralWithoutNumber() throws Exception {
            setupClientWithInput("{}");

            assertThatThrownBy(() -> client.parseString())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting NUMBER");
        }

        @Test
        void shouldThrowOnNegativeLiteralLength() throws Exception {
            setupClientWithInput("{-5}\r\n");

            assertThatThrownBy(() -> client.parseString())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Literal string length cannot be negative");
        }

        @Test
        void shouldThrowOnLiteralMissingClosingBrace() throws Exception {
            setupClientWithInput("{5");

            assertThatThrownBy(() -> client.parseString())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting }");
        }

        @Test
        void shouldThrowOnLiteralMissingEOLAfterBrace() throws Exception {
            setupClientWithInput("{5}xyz");

            assertThatThrownBy(() -> client.parseString())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting EOL");
        }

        @Test
        void shouldThrowOnUnexpectedEndOfInput() throws Exception {
            setupClientWithInput("{5}");

            assertThatThrownBy(() -> client.parseString())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Unexpected end of input");
        }

        @Test
        void shouldParseQuotedStringAfterWhitespace() throws Exception {
            setupClientWithInput("  \"hello\"\r\n");

            String result = client.parseString();

            assertThat(result).isEqualTo("hello");
        }

        @Test
        void shouldParseMultipleStringsSequentially() throws Exception {
            setupClientWithInput("\"first\" \"second\"\r\n");

            String first = client.parseString();
            String second = client.parseString();

            assertThat(first).isEqualTo("first");
            assertThat(second).isEqualTo("second");
        }

        @Test
        void shouldParseLiteralStringLongerThanBuffer() throws Exception {
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < 1500; i++) {
                content.append('a');
            }
            String input = "{" + content.length() + "}\r\n" + content + "\r\n";
            setupClientWithInput(input);

            String result = client.parseString();

            assertThat(result).hasSize(1500);
            assertThat(result).doesNotContainAnyWhitespaces();
        }

        @Test
        void shouldParseLiteralFollowedByQuoted() throws Exception {
            setupClientWithInput("{3}\r\nabc \"def\"\r\n");

            String literal = client.parseString();
            String quoted = client.parseString();

            assertThat(literal).isEqualTo("abc");
            assertThat(quoted).isEqualTo("def");
        }
    }

    @Nested
    @DisplayName("Response Parsing")
    class ResponseParsingTests {

        @Test
        void shouldParseSimpleOKResponse() throws Exception {
            setupClientWithInput("OK\r\n");
            client.noop(null);

            assertThat(out.toString()).startsWith("NOOP");
        }

        @Test
        void shouldParseSimpleNOResponse() throws Exception {
            setupClientWithInput("NO \"Script not found\"\r\n");
            ManageSieveResponse resp = client.deletescript("test.sieve");

            assertThat(resp.isNo()).isTrue();
            assertThat(resp.getMessage()).isEqualTo("Script not found");
        }

        @Test
        void shouldParseBYEResponse() throws Exception {
            setupClientWithInput("BYE \"Server shutting down\"\r\n");
            ManageSieveResponse resp = client.logout();

            assertThat(resp.isBye()).isTrue();
            assertThat(resp.getMessage()).isEqualTo("Server shutting down");
        }

        @Test
        void shouldParseOKResponseWithMessage() throws Exception {
            setupClientWithInput("OK \"done\"\r\n");
            ManageSieveResponse resp = client.noop("tag");

            assertThat(resp.isOk()).isTrue();
            assertThat(resp.getMessage()).isEqualTo("done");
        }

        @Test
        void shouldParseResponseWithCode() throws Exception {
            setupClientWithInput("OK (WARNINGS) \"script has warnings\"\r\n");
            ManageSieveResponse resp = client.checkscript("require [\"fileinto\"]");

            assertThat(resp.isOk()).isTrue();
            assertThat(resp.getCode()).isEqualTo(ManageSieveResponse.Code.WARNINGS);
            assertThat(resp.getMessage()).isEqualTo("script has warnings");
        }

        @Test
        void shouldParseResponseWithCodeAndParam() throws Exception {
            setupClientWithInput("OK (SASL \"PLAIN\")\r\n");

            ManageSieveResponse resp = client.noop(null);

            assertThat(resp.isOk()).isTrue();
            assertThat(resp.getCode()).isEqualTo(ManageSieveResponse.Code.SASL);
            assertThat(resp.getParam()).isEqualTo("PLAIN");
        }

        @Test
        void shouldParseResponseWithCodeNoParamNoMessage() throws Exception {
            setupClientWithInput("NO (QUOTA)\r\n");
            ManageSieveResponse resp = client.havespace("test.sieve", 5000);

            assertThat(resp.isNo()).isTrue();
            assertThat(resp.getCode()).isEqualTo(ManageSieveResponse.Code.QUOTA);
            assertThat(resp.getCode().hasParam()).isFalse();
        }

        @Test
        void shouldParseResponseWithCodeAndMessage() throws Exception {
            setupClientWithInput("NO (NONEXISTENT) \"Script not found\"\r\n");
            ManageSieveResponse resp = client.deletescript("ghost.sieve");

            assertThat(resp.isNo()).isTrue();
            assertThat(resp.getCode()).isEqualTo(ManageSieveResponse.Code.NONEXISTENT);
            assertThat(resp.getMessage()).isEqualTo("Script not found");
        }

        @Test
        void shouldThrowOnNonWordResponse() throws Exception {
            setupClientWithInput("123\r\n");

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting WORD");
        }

        @Test
        void shouldThrowOnResponseWithInvalidType() throws Exception {
            setupClientWithInput("INVALID\r\n");

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Invalid response type");
        }

        @Test
        void shouldThrowOnResponseWithUnclosedBracket() throws Exception {
            setupClientWithInput("OK (WARNINGS\r\n");

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting RIGHT_BRACKET");
        }

        @Test
        void shouldThrowOnResponseWithMissingCodeWord() throws Exception {
            setupClientWithInput("OK (123)\r\n");

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting LEFT_BRACKET");
        }

        @Test
        void shouldThrowOnResponseWithMissingEOL() throws Exception {
            setupClientWithInput("OK \"message\" extra\r\n");

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting EOL");
        }
    }

    @Nested
    @DisplayName("Capability Parsing")
    class CapabilityParsingTests {

        @Test
        void shouldParseAllCapabilities() throws Exception {
            setupClientWithInput(
                    "\"IMPLEMENTATION\" \"ManageSieve\"\r\n" +
                    "\"SASL\" \"PLAIN LOGIN\"\r\n" +
                    "\"SIEVE\" \"fileinto reject vacation\"\r\n" +
                    "\"STARTTLS\"\r\n" +
                    "\"MAXREDIRECTS\" 5\r\n" +
                    "\"NOTIFY\" \"mailto\"\r\n" +
                    "\"LANGUAGE\" \"en\"\r\n" +
                    "\"VERSION\" \"1.0\"\r\n" +
                    "\"OWNER\" \"admin\"\r\n" +
                    "OK\r\n" +
                    "OK\r\n"
            );
            client.capability();

            ServerCapabilities caps = client.getCapabilities();
            assertThat(caps.getImplementationName()).isEqualTo("ManageSieve");
            assertThat(caps.hasSASLMethod("PLAIN")).isTrue();
            assertThat(caps.hasSASLMethod("LOGIN")).isTrue();
            assertThat(caps.hasSieveExtension("fileinto")).isTrue();
            assertThat(caps.hasSieveExtension("reject")).isTrue();
            assertThat(caps.hasSieveExtension("vacation")).isTrue();
            assertThat(caps.hasTLS()).isTrue();
            assertThat(caps.getMaxRedirects()).isEqualTo(5);
            assertThat(caps.hasNotify("mailto")).isTrue();
            assertThat(caps.getLanguage()).isEqualTo("en");
            assertThat(caps.getVersion()).isEqualTo("1.0");
            assertThat(caps.getOwner()).isEqualTo("admin");
        }

        @Test
        void shouldParseUnknownCapability() throws Exception {
            setupClientWithInput(
                    "\"IMPLEMENTATION\" \"Test\"\r\n" +
                    "\"X-EXTENSION\" \"somevalue\"\r\n" +
                    "\"SIEVE\" \"fileinto\"\r\n" +
                    "OK\r\n" +
                    "OK\r\n"
            );
            client.capability();

            ServerCapabilities caps = client.getCapabilities();
            assertThat(caps.getImplementationName()).isEqualTo("Test");
            assertThat(caps.hasSieveExtension("fileinto")).isTrue();
        }

        @Test
        void shouldThrowOnUnexpectedTokenInCapabilities() throws Exception {
            setupClientWithInput("!\r\n");

            assertThatThrownBy(() -> client.capability())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Unexpected token");
        }

        @Test
        void shouldThrowOnNonNumberAfterMaxRedirects() throws Exception {
            setupClientWithInput(
                    "\"IMPLEMENTATION\" \"X\"\r\n" +
                    "\"MAXREDIRECTS\" \"invalid\"\r\n" +
                    "\"SIEVE\" \"fileinto\"\r\n" +
                    "OK\r\n" +
                    "OK\r\n"
            );

            assertThatThrownBy(() -> client.capability())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting NUMBER");
        }

        @Test
        void shouldThrowOnNonEOLAfterCapability() throws Exception {
            setupClientWithInput(
                    "\"IMPLEMENTATION\" \"X\" \"EXTRA\"\r\n" +
                    "\"SIEVE\" \"fileinto\"\r\n" +
                    "OK\r\n" +
                    "OK\r\n"
            );

            assertThatThrownBy(() -> client.capability())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting EOL");
        }

        @Test
        void shouldHandleCapabilityWithLiteralString() throws Exception {
            String implValue = "ManageSieve";
            String input = "{13}\r\n" + implValue + "\r\n" +
                           "\"IMPLEMENTATION\" " + "{13}\r\n" + implValue + "\r\n" +
                           "\"SIEVE\" \"fileinto\"\r\n" +
                           "OK\r\n" +
                           "OK\r\n";

            setupClientWithInput(input);

            assertThatThrownBy(() -> client.capability())
                    .isInstanceOf(ParseException.class);
        }
    }

    @Nested
    @DisplayName("Command Methods")
    class CommandMethodTests {

        @Test
        void shouldSendHavespaceCommand() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.havespace("myscript.sieve", 1024);

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).contains("HAVESPACE \"myscript.sieve\" 1024\r\n");
        }

        @Test
        void shouldSendHavespaceAndGetNoWhenNoSpace() throws Exception {
            setupClientWithInput("NO (QUOTA) \"Insufficient space\"\r\n");
            ManageSieveResponse resp = client.havespace("large.sieve", 999999);

            assertThat(resp.isNo()).isTrue();
            assertThat(resp.getCode()).isEqualTo(ManageSieveResponse.Code.QUOTA);
            assertThat(resp.getMessage()).isEqualTo("Insufficient space");
        }

        @Test
        void shouldSendPutscriptCommand() throws Exception {
            setupClientWithInput("OK (WARNINGS) \"script stored with warnings\"\r\n");
            ManageSieveResponse resp = client.putscript("test.sieve", "require [\"fileinto\"];");

            assertThat(resp.isOk()).isTrue();
            assertThat(resp.getCode()).isEqualTo(ManageSieveResponse.Code.WARNINGS);
            assertThat(resp.getMessage()).isEqualTo("script stored with warnings");
            assertThat(out.toString()).contains("PUTSCRIPT");
            assertThat(out.toString()).contains("\"test.sieve\"");
        }

        @Test
        void shouldSendPutscriptWithEmptyBody() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.putscript("empty.sieve", "");

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).contains("PUTSCRIPT");
        }

        @Test
        void shouldSendDeletescriptCommand() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.deletescript("old.sieve");

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).isEqualTo("DELETESCRIPT \"old.sieve\"\r\n");
        }

        @Test
        void shouldSendSetactiveCommand() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.setactive("myfilter.sieve");

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).isEqualTo("SETACTIVE \"myfilter.sieve\"\r\n");
        }

        @Test
        void shouldSendSetactiveWithEmptyName() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.setactive("");

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).isEqualTo("SETACTIVE \"\"\r\n");
        }

        @Test
        void shouldSendLogoutCommand() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.logout();

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).isEqualTo("LOGOUT\r\n");
        }

        @Test
        void shouldSendRenamescriptCommand() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.renamescript("old.sieve", "new.sieve");

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).contains("RENAMESCRIPT");
        }

        @Test
        void shouldSendCheckscriptCommand() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.checkscript("require [\"fileinto\"];");

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).contains("CHECKSCRIPT");
        }

        @Test
        void shouldSendCheckscriptAndGetParseError() throws Exception {
            setupClientWithInput("NO \"parse error at line 5\"\r\n");
            ManageSieveResponse resp = client.checkscript("invalid script garbage");

            assertThat(resp.isNo()).isTrue();
            assertThat(resp.getMessage()).isEqualTo("parse error at line 5");
        }

        @Test
        void shouldSendNoopWithoutTag() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.noop(null);

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).isEqualTo("NOOP\r\n");
        }

        @Test
        void shouldSendNoopWithTag() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.noop("ping");

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).contains("NOOP");
        }

        @Test
        void shouldSendCapabilityCommand() throws Exception {
            setupClientWithInput(
                    "\"IMPLEMENTATION\" \"TestSieve\"\r\n" +
                    "\"SIEVE\" \"fileinto\"\r\n" +
                    "OK\r\n" +
                    "OK\r\n"
            );
            ManageSieveResponse resp = client.capability();

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).startsWith("CAPABILITY\r\n");
        }
    }

    @Nested
    @DisplayName("Script Operations")
    class ScriptOperationTests {

        @Test
        void shouldListScripts() throws Exception {
            setupClientWithInput(
                    "\"script1\"\r\n" +
                    "\"script2\"\r\n" +
                    "OK\r\n"
            );
            ArrayList<SieveScript> scripts = new ArrayList<>();
            ManageSieveResponse resp = client.listscripts(scripts);

            assertThat(resp.isOk()).isTrue();
            assertThat(scripts).hasSize(2);
            assertThat(scripts.get(0).getName()).isEqualTo("script1");
            assertThat(scripts.get(0).isActive()).isFalse();
            assertThat(scripts.get(1).getName()).isEqualTo("script2");
            assertThat(scripts.get(1).isActive()).isFalse();
        }

        @Test
        void shouldListScriptsWithActiveMarker() throws Exception {
            setupClientWithInput(
                    "\"inactive.sieve\"\r\n" +
                    "\"active.sieve\" ACTIVE\r\n" +
                    "OK\r\n"
            );
            ArrayList<SieveScript> scripts = new ArrayList<>();
            ManageSieveResponse resp = client.listscripts(scripts);

            assertThat(resp.isOk()).isTrue();
            assertThat(scripts).hasSize(2);
            assertThat(scripts.get(0).getName()).isEqualTo("inactive.sieve");
            assertThat(scripts.get(0).isActive()).isFalse();
            assertThat(scripts.get(1).getName()).isEqualTo("active.sieve");
            assertThat(scripts.get(1).isActive()).isTrue();
        }

        @Test
        void shouldListScriptsWithLiteralName() throws Exception {
            setupClientWithInput(
                    "{11}\r\nscript name\r\n" +
                    "OK\r\n"
            );
            ArrayList<SieveScript> scripts = new ArrayList<>();
            ManageSieveResponse resp = client.listscripts(scripts);

            assertThat(resp.isOk()).isTrue();
            assertThat(scripts).hasSize(1);
            assertThat(scripts.get(0).getName()).isEqualTo("script name");
        }

        @Test
        void shouldListEmptyScripts() throws Exception {
            setupClientWithInput("OK\r\n");
            ArrayList<SieveScript> scripts = new ArrayList<>();
            ManageSieveResponse resp = client.listscripts(scripts);

            assertThat(resp.isOk()).isTrue();
            assertThat(scripts).isEmpty();
        }

        @Test
        void shouldClearExistingScriptsList() throws Exception {
            setupClientWithInput(
                    "\"newscript\"\r\n" +
                    "OK\r\n"
            );
            ArrayList<SieveScript> scripts = new ArrayList<>();
            scripts.add(new SieveScript("oldscript", null, false));
            assertThat(scripts).hasSize(1);

            ManageSieveResponse resp = client.listscripts(scripts);

            assertThat(resp.isOk()).isTrue();
            assertThat(scripts).hasSize(1);
            assertThat(scripts.get(0).getName()).isEqualTo("newscript");
        }

        @Test
        void shouldGetScriptWithPayload() throws Exception {
            setupClientWithInput("{5}\r\nworld\r\nOK\r\n");
            SieveScript script = new SieveScript("world.sieve", null, false);
            ManageSieveResponse resp = client.getScript(script);

            assertThat(resp.isOk()).isTrue();
            assertThat(script.getBody()).isEqualTo("world");
        }

        @Test
        void shouldGetScriptWhenNotFound() throws Exception {
            setupClientWithInput("NO (NONEXISTENT) \"Script not found\"\r\n");
            SieveScript script = new SieveScript("ghost.sieve", null, false);
            ManageSieveResponse resp = client.getScript(script);

            assertThat(resp.isNo()).isTrue();
            assertThat(resp.getCode()).isEqualTo(ManageSieveResponse.Code.NONEXISTENT);
            assertThat(resp.getMessage()).isEqualTo("Script not found");
        }

        @Test
        void shouldGetScriptWithLiteralPayload() throws Exception {
            setupClientWithInput("{5}\r\nhello\r\nOK\r\n");
            SieveScript script = new SieveScript("test.sieve", null, false);
            ManageSieveResponse resp = client.getScript(script);

            assertThat(resp.isOk()).isTrue();
            assertThat(script.getBody()).isEqualTo("hello");
        }

        @Test
        void shouldThrowOnUnexpectedWordInListscripts() throws Exception {
            setupClientWithInput(
                    "\"script1\" BLAH\r\n"
            );

            ArrayList<SieveScript> scripts = new ArrayList<>();
            assertThatThrownBy(() -> client.listscripts(scripts))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Unexpected word")
                    .hasMessageContaining("BLAH");
        }

        @Test
        void shouldThrowOnUnexpectedTokenInListscripts() throws Exception {
            setupClientWithInput("123\r\n");

            ArrayList<SieveScript> scripts = new ArrayList<>();
            assertThatThrownBy(() -> client.listscripts(scripts))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Unexpected token");
        }

        @Test
        void shouldThrowOnMissingEOLInListscripts() throws Exception {
            setupClientWithInput("\"script1\" ACTIVE extra\r\n");

            ArrayList<SieveScript> scripts = new ArrayList<>();
            assertThatThrownBy(() -> client.listscripts(scripts))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expected EOL");
        }
    }

    @Nested
    @DisplayName("Payload Parsing")
    class PayloadParsingTests {

        @Test
        void shouldThrowOnMissingEOLAfterPayload() throws Exception {
            setupClientWithInput("{3}\r\nabcXYZ");

            assertThatThrownBy(() -> {
                SieveScript script = new SieveScript("test.sieve", null, false);
                client.getScript(script);
            }).isInstanceOf(ParseException.class)
              .hasMessageContaining("Expecting EOL");
        }
    }

    @Nested
    @DisplayName("Resource Management")
    class ResourceManagementTests {

        @Test
        void shouldDisconnectCleanly() throws Exception {
            setupClientWithInput("OK\r\n");
            client.noop(null);

            client.disconnect();

            assertThat(out.toString()).isNotEmpty();
        }

        @Test
        void shouldDisconnectWhenAlreadyDisconnected() throws Exception {
            setupClientWithInput("");

            client.disconnect();
            client.disconnect();

        }

        @Test
        void shouldHandleDisconnectWithInputReaderNotSet() throws Exception {
            setupClientWithInput("OK\r\n");

            client.disconnect();

        }

        @Test
        void shouldDisconnectWithOnlyInSet() throws Exception {
            client.setupForTesting(new StringReader(""), new StringWriter());

            client.disconnect();

        }

        @Test
        void shouldCloseResourcesOnDisconnect() throws Exception {
            StringWriter customOut = new StringWriter();
            client.setupForTesting(new StringReader("OK\r\n"), customOut);
            client.noop(null);

            client.disconnect();

            assertThat(customOut.toString()).contains("NOOP");
        }
    }

    @Nested
    @DisplayName("Error Handling Edge Cases")
    class ErrorHandlingEdgeCasesTests {

        @Test
        void shouldThrowWhenScriptNameExceedsMaxLength() {
            String longName = "a".repeat(1026);
            setupClientWithInput("OK\r\n");

            assertThatThrownBy(() -> client.deletescript(longName))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("maximum size");
        }

        @Test
        void shouldThrowIOExceptionOnWriteError() {
            Writer badWriter = new Writer() {
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    throw new IOException("write failed");
                }
                @Override
                public void flush() {}
                @Override
                public void close() {}
            };
            client.setupForTesting(new StringReader("OK\r\n"), badWriter);

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Unknown error writing to server");
        }

        @Test
        void shouldDisconnectWithoutErrorWhenOutNotSet() throws Exception {
            Reader input = new StringReader("OK\r\n");
            client.setupForTesting(input, out);
            client.noop(null);

            client.disconnect();

            assertThat(client.isConnected()).isFalse();
        }
    }

    @Nested
    @DisplayName("Command Output")
    class CommandOutputTests {

        @Test
        void shouldEscapeSpecialCharsInScriptName() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.setactive("quote\"name");

            assertThat(resp.isOk()).isTrue();
            String sent = out.toString();
            assertThat(sent).isEqualTo("SETACTIVE \"quote\\\"name\"\r\n");
        }

        @Test
        void shouldEscapeBackslashInScriptName() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.deletescript("back\\slash");

            assertThat(resp.isOk()).isTrue();
            String sent = out.toString();
            assertThat(sent).isEqualTo("DELETESCRIPT \"back\\\\slash\"\r\n");
        }

        @Test
        void shouldEncodeEmptyNameForSetactive() throws Exception {
            setupClientWithInput("OK\r\n");
            client.setactive("");

            assertThat(out.toString()).isEqualTo("SETACTIVE \"\"\r\n");
        }

        @Test
        void shouldHandleNameWithSpacesForHavespace() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.havespace("my script.sieve", 100);

            assertThat(resp.isOk()).isTrue();
            String sent = out.toString();
            assertThat(sent).contains("\"my script.sieve\"");
        }

        @Test
        void shouldSendCommandWithSpecialNameCharacters() throws Exception {
            setupClientWithInput("OK\r\n");
            client.deletescript("test-script_v2.1");

            assertThat(out.toString()).isEqualTo("DELETESCRIPT \"test-script_v2.1\"\r\n");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        void shouldThrowOnMissingEOLAfterResponseCode() throws Exception {
            setupClientWithInput("OK (WARNINGS\r\n");

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting RIGHT_BRACKET");
        }

        @Test
        void shouldThrowOnNonWordAfterLeftBracket() throws Exception {
            setupClientWithInput("OK (\"string\")\r\n");

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting LEFT_BRACKET");
        }

        @Test
        void shouldThrowWhenParseStringGetsBareWord() throws Exception {
            setupClientWithInput("bareword\r\n");

            assertThatThrownBy(() -> client.parseString())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting DQUOTE or {");
        }

        @Test
        void shouldThrowWhenParseStringGetsSpecialChar() throws Exception {
            setupClientWithInput("{\r\n");

            assertThatThrownBy(() -> client.parseString())
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting NUMBER");
        }

        @Test
        void shouldReadPartialLiteralOnEOF() throws Exception {
            setupClientWithInput("{5}\r\nab");

            String result = client.parseString();

            assertThat(result).isEqualTo("ab");
        }

        @Test
        void shouldThrowOnGarbageAfterResponse() throws Exception {
            setupClientWithInput("OK\r\ntrash\r\n");

            ManageSieveResponse resp = client.noop(null);
            assertThat(resp.isOk()).isTrue();

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Invalid response type");
        }

        @Test
        void shouldThrowOnNoEOLAfterResponseMessage() throws Exception {
            setupClientWithInput("OK \"message\" extra\r\n");

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting EOL");
        }

        @Test
        void shouldThrowOnMissingCodeWordInResponse() throws Exception {
            setupClientWithInput("OK ()\r\n");

            assertThatThrownBy(() -> client.noop(null))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting LEFT_BRACKET");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        void shouldHandleResponseWithOnlyCodeNoMessage() throws Exception {
            setupClientWithInput("NO (QUOTA)\r\n");
            ManageSieveResponse resp = client.havespace("test.sieve", 99999);

            assertThat(resp.isNo()).isTrue();
            assertThat(resp.getCode()).isEqualTo(ManageSieveResponse.Code.QUOTA);
            assertThat(resp.getMessage()).isNull();
        }

        @Test
        void shouldHandleResponseWithOnlyMessageNoCode() throws Exception {
            setupClientWithInput("OK \"everything is fine\"\r\n");
            ManageSieveResponse resp = client.noop("tag");

            assertThat(resp.isOk()).isTrue();
            assertThat(resp.getMessage()).isEqualTo("everything is fine");
        }

        @Test
        void shouldHandleAUTH_TOO_WEAKCode() throws Exception {
            setupClientWithInput("NO (AUTH-TOO-WEAK) \"authentication too weak\"\r\n");

            ManageSieveResponse resp = client.noop(null);

            assertThat(resp.isNo()).isTrue();
            assertThat(resp.getCode()).isEqualTo(ManageSieveResponse.Code.AUTH_TOO_WEAK);
            assertThat(resp.getMessage()).isEqualTo("authentication too weak");
        }

        @Test
        void shouldHandleMultipleNoopCalls() throws Exception {
            setupClientWithInput("OK\r\nOK\r\nOK\r\n");

            assertThat(client.noop(null).isOk()).isTrue();
            assertThat(client.noop(null).isOk()).isTrue();
            assertThat(client.noop(null).isOk()).isTrue();
        }

        @Test
        void shouldHandleEmptyStringNameForDeletescript() throws Exception {
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.deletescript("");

            assertThat(resp.isOk()).isTrue();
        }

        @Test
        void shouldHandleVeryLongScriptName() throws Exception {
            String longName = "a".repeat(255);
            setupClientWithInput("OK\r\n");
            ManageSieveResponse resp = client.setactive(longName);

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).contains(longName);
        }

        @Test
        void shouldHandleLiteralStringWithNewlineBytes() throws Exception {
            setupClientWithInput("{2}\r\n\r\n");

            String result = client.parseString();

            assertThat(result).isEqualTo("\r\n");
        }
    }

    @Nested
    @DisplayName("Authentication")
    class AuthenticationTests {

        @Test
        void shouldAuthenticateWithUsernameAndPassword() throws Exception {
            String capabilityResponse = "\"IMPLEMENTATION\" \"test\"\r\n"
                + "\"SASL\" \"PLAIN\"\r\n"
                + "OK\r\n"
                + "OK\r\n";
            String authResponse = "OK \"Authenticated\"\r\n";

            StringReader in = new StringReader(capabilityResponse + authResponse);
            out = new StringWriter();
            client.setupForTesting(in, out);
            client.capability();

            ManageSieveResponse resp = client.authenticate("testuser", "testpass");

            assertThat(resp.isOk()).isTrue();
            assertThat(out.toString()).contains("AUTHENTICATE");
        }

        @Test
        void shouldAuthenticateWithAuthId() throws Exception {
            String capabilityResponse = "\"IMPLEMENTATION\" \"test\"\r\n"
                + "\"SASL\" \"PLAIN\"\r\n"
                + "OK\r\n"
                + "OK\r\n";
            String authResponse = "OK \"Authenticated\"\r\n";

            StringReader in = new StringReader(capabilityResponse + authResponse);
            out = new StringWriter();
            client.setupForTesting(in, out);
            client.capability();

            ManageSieveResponse resp = client.authenticate("testuser", "testpass", "authzId");

            assertThat(resp.isOk()).isTrue();
        }

        @Test
        void shouldHandleAuthenticationRejected() throws Exception {
            String capabilityResponse = "\"IMPLEMENTATION\" \"test\"\r\n"
                + "\"SASL\" \"PLAIN\"\r\n"
                + "OK\r\n"
                + "OK\r\n";
            String authResponse = "NO \"Authentication failed\"\r\n";

            StringReader in = new StringReader(capabilityResponse + authResponse);
            out = new StringWriter();
            client.setupForTesting(in, out);
            client.capability();

            ManageSieveResponse resp = client.authenticate("testuser", "wrongpass");

            assertThat(resp.isNo()).isTrue();
        }
    }
}
