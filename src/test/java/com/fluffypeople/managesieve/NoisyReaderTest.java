package com.fluffypeople.managesieve;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for NoisyReader class.
 * NoisyReader is a debug wrapper around a Reader that logs what has been read.
 */
public class NoisyReaderTest {

    @Test
    public void testConstructor_WithValidReader() {
        Reader base = new StringReader("test");
        NoisyReader reader = new NoisyReader(base);

        assertThat(reader).isNotNull();
    }

    @Test
    public void testRead_SingleCharacter() throws IOException {
        String input = "Hello";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer = new char[1];
        int result = reader.read(buffer, 0, 1);

        assertThat(result).isEqualTo(1);
        assertThat(buffer[0]).isEqualTo('H');

        reader.close();
    }

    @Test
    public void testRead_MultipleCharacters() throws IOException {
        String input = "Hello World";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer = new char[5];
        int result = reader.read(buffer, 0, 5);

        assertThat(result).isEqualTo(5);
        assertThat(new String(buffer)).isEqualTo("Hello");

        reader.close();
    }

    @Test
    public void testRead_EntireContent() throws IOException {
        String input = "Test";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer = new char[10];
        int result = reader.read(buffer, 0, 10);

        assertThat(result).isEqualTo(4);
        assertThat(new String(buffer, 0, result)).isEqualTo("Test");

        reader.close();
    }

    @Test
    public void testRead_WithOffset() throws IOException {
        String input = "ABCDE";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer = new char[10];
        buffer[0] = 'X';
        buffer[1] = 'Y';
        int result = reader.read(buffer, 2, 5);

        assertThat(result).isEqualTo(5);
        assertThat(buffer[0]).isEqualTo('X');
        assertThat(buffer[1]).isEqualTo('Y');
        assertThat(buffer[2]).isEqualTo('A');
        assertThat(buffer[3]).isEqualTo('B');

        reader.close();
    }

    @Test
    public void testRead_WithNewline() throws IOException {
        String input = "Line1\nLine2";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer = new char[20];
        int result = reader.read(buffer, 0, 20);

        assertThat(result).isEqualTo(11);
        assertThat(new String(buffer, 0, result)).isEqualTo("Line1\nLine2");

        reader.close();
    }

    @Test
    public void testRead_MultipleNewlines() throws IOException {
        String input = "A\nB\nC\n";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer = new char[10];
        int result = reader.read(buffer, 0, 10);

        assertThat(result).isEqualTo(6);
        assertThat(new String(buffer, 0, result)).isEqualTo("A\nB\nC\n");

        reader.close();
    }

    @Test
    public void testRead_EmptyInput() throws IOException {
        String input = "";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer = new char[10];
        int result = reader.read(buffer, 0, 10);

        assertThat(result).isEqualTo(-1); // EOF

        reader.close();
    }

    @Test
    public void testRead_SequentialReads() throws IOException {
        String input = "ABCDEFGHIJ";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer1 = new char[3];
        char[] buffer2 = new char[3];
        char[] buffer3 = new char[10];

        int result1 = reader.read(buffer1, 0, 3);
        int result2 = reader.read(buffer2, 0, 3);
        int result3 = reader.read(buffer3, 0, 10);

        assertThat(result1).isEqualTo(3);
        assertThat(new String(buffer1)).isEqualTo("ABC");

        assertThat(result2).isEqualTo(3);
        assertThat(new String(buffer2)).isEqualTo("DEF");

        assertThat(result3).isEqualTo(4);
        assertThat(new String(buffer3, 0, result3)).isEqualTo("GHIJ");

        reader.close();
    }

    @Test
    public void testClose_ClosesUnderlyingReader() throws IOException {
        TrackingReader base = new TrackingReader("test");
        NoisyReader reader = new NoisyReader(base);

        assertThat(base.isClosed()).isFalse();

        reader.close();

        assertThat(base.isClosed()).isTrue();
    }

    @Test
    public void testRead_Unicode() throws IOException {
        String input = "日本語テスト";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer = new char[20];
        int result = reader.read(buffer, 0, 20);

        assertThat(result).isEqualTo(6);
        assertThat(new String(buffer, 0, result)).isEqualTo(input);

        reader.close();
    }

    @Test
    public void testRead_ManageSieveResponse() throws IOException {
        // Simulate a typical ManageSieve response
        String input = "OK \"Putscript completed.\"\r\n";
        Reader base = new StringReader(input);
        NoisyReader reader = new NoisyReader(base);

        char[] buffer = new char[50];
        int result = reader.read(buffer, 0, 50);

        assertThat(result).isEqualTo(27);
        assertThat(new String(buffer, 0, result)).startsWith("OK");
        assertThat(new String(buffer, 0, result)).contains("Putscript");

        reader.close();
    }

    @Test
    public void testIsReader() {
        NoisyReader reader = new NoisyReader(new StringReader("test"));

        assertThat(reader).isInstanceOf(Reader.class);
    }

    /**
     * Helper class to track if close() was called on the underlying reader.
     */
    private static class TrackingReader extends Reader {
        private final StringReader delegate;
        private boolean closed = false;

        TrackingReader(String content) {
            this.delegate = new StringReader(content);
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            return delegate.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            delegate.close();
        }

        boolean isClosed() {
            return closed;
        }
    }
}
