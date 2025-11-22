package com.fluffypeople.managesieve;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests for ParseException class.
 * Tests exception creation, message propagation, and inheritance.
 */
public class ParseExceptionTest {

    @Test
    public void testDefaultConstructor() {
        ParseException exception = new ParseException();

        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isNull();
        assertThat(exception.getCause()).isNull();
    }

    @Test
    public void testMessageConstructor() {
        String message = "Invalid server response format";
        ParseException exception = new ParseException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    public void testMessageConstructor_EmptyMessage() {
        ParseException exception = new ParseException("");

        assertThat(exception.getMessage()).isEmpty();
    }

    @Test
    public void testMessageConstructor_NullMessage() {
        ParseException exception = new ParseException(null);

        assertThat(exception.getMessage()).isNull();
    }

    @Test
    public void testIsException() {
        ParseException exception = new ParseException("test");

        assertThat(exception).isInstanceOf(Exception.class);
    }

    @Test
    public void testCanBeThrown() {
        Throwable thrown = catchThrowable(() -> {
            throw new ParseException("Parse error at line 1");
        });

        assertThat(thrown)
            .isInstanceOf(ParseException.class)
            .hasMessage("Parse error at line 1");
    }

    @Test
    public void testCanBeCaught() {
        String result = "not caught";
        try {
            throw new ParseException("test error");
        } catch (ParseException e) {
            result = "caught";
        }

        assertThat(result).isEqualTo("caught");
    }

    @Test
    public void testMessageWithSpecialCharacters() {
        String message = "Error parsing: \"CAPABILITY\" (line 1, col 5)\nUnexpected token";
        ParseException exception = new ParseException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    public void testMessageWithUnicode() {
        String message = "解析错误: неверный ответ сервера";
        ParseException exception = new ParseException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    public void testSerialVersionUID() {
        // ParseException should be serializable since it extends Exception
        ParseException exception = new ParseException("test");

        assertThat(exception).isInstanceOf(java.io.Serializable.class);
    }

    @Test
    public void testExceptionChaining() throws Exception {
        // Even though ParseException doesn't have a cause constructor,
        // it should still work with initCause from Throwable
        ParseException exception = new ParseException("Parse failed");
        java.io.IOException cause = new java.io.IOException("Connection reset");
        exception.initCause(cause);

        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
