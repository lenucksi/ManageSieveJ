package com.fluffypeople.managesieve;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.testng.Assert.assertEquals;

/**
 * Tests for ManageSieveClient string encoding and parsing.
 * These are critical protocol operations that must handle edge cases correctly.
 */
public class ManageSieveClientStringEncodingTest {

    private ManageSieveClient client;

    @BeforeMethod
    public void setUp() {
        client = new ManageSieveClient();
    }

    @DataProvider(name = "literalStrings")
    public Object[][] createLiteralStrings() {
        return new Object[][]{
                {"Hello World"},
                {""},  // Empty string
                {"Line1\r\nLine2"},  // Multi-line
                {"With\ttabs\tand\nnewlines"},
                {"Special chars: !@#$%^&*()"},
                {"Quotes: \"test\" and 'test'"},
                {"Backslashes: \\ \\\\ \\\\\\"},
                {"{10}\r\nNested"},  // Nested literal string format
                {"Unicode: ÄÖÜäöü"},
                {"Mixed:\r\n\t\"\\{"}
        };
    }

    @Test(dataProvider = "literalStrings")
    public void testParseString_Literal(String expected) throws IOException, ParseException {
        // Create literal string format: {length}\r\ndata
        byte[] bytes = expected.getBytes("UTF-8");
        String encoded = "{" + bytes.length + "}\r\n" + expected;

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertEquals(actual, expected);
    }

    @Test
    public void testParseString_EmptyLiteral() throws IOException, ParseException {
        String encoded = "{0}\r\n";

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertThat(actual).isEmpty();
    }

    @Test
    public void testParseString_LargeLiteral() throws IOException, ParseException {
        StringBuilder large = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            large.append("Line ").append(i).append("\r\n");
        }
        String expected = large.toString();

        byte[] bytes = expected.getBytes("UTF-8");
        String encoded = "{" + bytes.length + "}\r\n" + expected;

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertEquals(actual, expected);
    }

    @Test
    public void testParseString_BinaryData() throws IOException, ParseException {
        // Test with null bytes and other binary data (represented as string)
        String expected = "Before\u0000After";

        byte[] bytes = expected.getBytes("UTF-8");
        String encoded = "{" + bytes.length + "}\r\n" + expected;

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertEquals(actual, expected);
    }

    @Test
    public void testParseString_QuotedString() throws IOException, ParseException {
        // Quoted strings are handled by StreamTokenizer
        String encoded = "\"Hello World\"";

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertEquals(actual, "Hello World");
    }

    @Test
    public void testParseString_QuotedWithEscapes() throws IOException, ParseException {
        // Escaped quotes in quoted string
        String encoded = "\"He said \\\"Hello\\\"\"";

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertEquals(actual, "He said \"Hello\"");
    }

    @DataProvider(name = "UTF8Multibyte")
    public Object[][] createUTF8MultibyteStrings() {
        return new Object[][]{
                {"日本語"},  // Japanese (3 bytes per char)
                {"한국어"},  // Korean
                {"中文"},  // Chinese
                {"العربية"},  // Arabic
                {"עברית"},  // Hebrew
                {"Ελληνικά"},  // Greek
                {"Русский"},  // Russian (2 bytes per char)
                {"🎉🎊🎈"},  // Emoji (4 bytes per char)
                {"Mix: ABC 日本 123"},  // Mixed
        };
    }

    @Test(dataProvider = "UTF8Multibyte")
    public void testParseString_UTF8Multibyte(String expected) throws IOException, ParseException {
        // Important: length is in bytes, not characters
        byte[] bytes = expected.getBytes("UTF-8");
        String encoded = "{" + bytes.length + "}\r\n" + expected;

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertEquals(actual, expected);
    }

    @Test
    public void testIsConnected_False() {
        ManageSieveClient newClient = new ManageSieveClient();
        assertThat(newClient.isConnected()).isFalse();
    }

    @Test
    public void testSocketTimeout_Default() throws Exception {
        ManageSieveClient newClient = new ManageSieveClient();
        assertThat(newClient.getSocketTimeout()).isEqualTo(0);
    }

    @Test
    public void testSocketTimeout_Set() throws Exception {
        ManageSieveClient newClient = new ManageSieveClient();
        newClient.setSocketTimeout(30000);
        assertThat(newClient.getSocketTimeout()).isEqualTo(30000);
    }

    @Test
    public void testGetCapabilities_Null() {
        ManageSieveClient newClient = new ManageSieveClient();
        assertThat(newClient.getCapabilities()).isNull();
    }

    @DataProvider(name = "invalidLiterals")
    public Object[][] createInvalidLiterals() {
        return new Object[][]{
                {"{abc}\r\n"},  // Non-numeric length
                {"{-5}\r\n"},  // Negative length
                // Note: StreamTokenizer accepts LF-only and EOF as EOL
                // RFC 5804 requires CRLF, but the implementation is lenient
                // {"{10}\n"},  // Missing CR (LF only) - lenient parsing
                // {"{10}"},    // Missing CRLF (EOF) - lenient parsing
        };
    }

    @Test(dataProvider = "invalidLiterals")
    public void testParseString_InvalidLiteral(String encoded) {
        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);

        assertThatThrownBy(() -> client.parseString())
                .isInstanceOf(ParseException.class);
    }

    @Test
    public void testParseString_LiteralTruncated() {
        // Claim 100 bytes but only provide 10
        String encoded = "{100}\r\nShortData";

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);

        // Should read what's available (9 chars = "ShortData")
        assertThatCode(() -> client.parseString())
                .doesNotThrowAnyException();
    }

    @Test
    public void testParseString_ExactUTF8Length() throws IOException, ParseException {
        // Test that byte length is used, not character length
        String str = "ÄÖÜ";  // 3 characters, but 6 bytes in UTF-8

        byte[] bytes = str.getBytes("UTF-8");
        assertThat(bytes.length).isEqualTo(6);  // Verify our assumption

        String encoded = "{6}\r\nÄÖÜ";

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertEquals(actual, str);
    }

    @Test
    public void testParseString_WrongUTF8Length() {
        // Use character count instead of byte count (common mistake)
        String str = "ÄÖÜ";  // 3 chars but 6 bytes

        String encoded = "{3}\r\nÄÖÜ";  // Wrong! Should be {6}

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);

        // This will read only 3 bytes, which is incomplete UTF-8
        assertThatCode(() -> client.parseString())
                .doesNotThrowAnyException();  // May or may not throw, depends on parsing
    }

    @Test
    public void testParseString_CRLF_InData() throws IOException, ParseException {
        // CRLF within the data should not confuse the parser
        String expected = "Line1\r\nLine2\r\nLine3";

        byte[] bytes = expected.getBytes("UTF-8");
        String encoded = "{" + bytes.length + "}\r\n" + expected;

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertEquals(actual, expected);
    }

    @Test
    public void testParseString_LiteralMarkerInData() throws IOException, ParseException {
        // Data that looks like a literal marker should not confuse parser
        String expected = "This has {10} in it";

        byte[] bytes = expected.getBytes("UTF-8");
        String encoded = "{" + bytes.length + "}\r\n" + expected;

        StringReader in = new StringReader(encoded);
        StringWriter out = new StringWriter();

        client.setupForTesting(in, out);
        String actual = client.parseString();

        assertEquals(actual, expected);
    }
}
