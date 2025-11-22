package com.fluffypeople.managesieve;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ResponseAndPayload class.
 * Tests the value object that holds a ManageSieveResponse and its associated payload.
 */
public class ResponseAndPayloadTest {

    @Test
    public void testConstructor_WithValidArguments() {
        ManageSieveResponse response = new ManageSieveResponse();
        String payload = "script body content";

        ResponseAndPayload rap = new ResponseAndPayload(response, payload);

        assertThat(rap.getResponse()).isSameAs(response);
        assertThat(rap.getPayload()).isEqualTo(payload);
    }

    @Test
    public void testConstructor_WithNullPayload() {
        ManageSieveResponse response = new ManageSieveResponse();

        ResponseAndPayload rap = new ResponseAndPayload(response, null);

        assertThat(rap.getResponse()).isSameAs(response);
        assertThat(rap.getPayload()).isNull();
    }

    @Test
    public void testConstructor_WithNullResponse() {
        String payload = "some payload";

        ResponseAndPayload rap = new ResponseAndPayload(null, payload);

        assertThat(rap.getResponse()).isNull();
        assertThat(rap.getPayload()).isEqualTo(payload);
    }

    @Test
    public void testConstructor_WithBothNull() {
        ResponseAndPayload rap = new ResponseAndPayload(null, null);

        assertThat(rap.getResponse()).isNull();
        assertThat(rap.getPayload()).isNull();
    }

    @Test
    public void testGetResponse_ReturnsSameInstance() {
        ManageSieveResponse response = new ManageSieveResponse();
        ResponseAndPayload rap = new ResponseAndPayload(response, "payload");

        ManageSieveResponse retrieved1 = rap.getResponse();
        ManageSieveResponse retrieved2 = rap.getResponse();

        assertThat(retrieved1).isSameAs(retrieved2);
        assertThat(retrieved1).isSameAs(response);
    }

    @Test
    public void testGetPayload_ReturnsSameValue() {
        String payload = "test payload";
        ResponseAndPayload rap = new ResponseAndPayload(new ManageSieveResponse(), payload);

        String retrieved1 = rap.getPayload();
        String retrieved2 = rap.getPayload();

        assertThat(retrieved1).isEqualTo(retrieved2);
        assertThat(retrieved1).isEqualTo(payload);
    }

    @Test
    public void testPayload_EmptyString() {
        ResponseAndPayload rap = new ResponseAndPayload(new ManageSieveResponse(), "");

        assertThat(rap.getPayload()).isEmpty();
    }

    @Test
    public void testPayload_WithNewlines() {
        String payload = "require \"fileinto\";\n\nif header :contains \"from\" \"boss\" {\n  fileinto \"Important\";\n}";
        ResponseAndPayload rap = new ResponseAndPayload(new ManageSieveResponse(), payload);

        assertThat(rap.getPayload()).isEqualTo(payload);
        assertThat(rap.getPayload()).contains("\n");
    }

    @Test
    public void testPayload_WithUnicode() {
        String payload = "# Фильтр для почты\n# 邮件过滤器\nrequire \"vacation\";";
        ResponseAndPayload rap = new ResponseAndPayload(new ManageSieveResponse(), payload);

        assertThat(rap.getPayload()).isEqualTo(payload);
    }

    @Test
    public void testPayload_WithSpecialCharacters() {
        String payload = "# Comments with special chars: !@#$%^&*()_+-={}[]|\\:\";<>?,./\n";
        ResponseAndPayload rap = new ResponseAndPayload(new ManageSieveResponse(), payload);

        assertThat(rap.getPayload()).isEqualTo(payload);
    }

    @Test
    public void testPayload_LargeContent() {
        // Simulate a large script
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("# Line ").append(i).append("\n");
        }
        String largePayload = sb.toString();

        ResponseAndPayload rap = new ResponseAndPayload(new ManageSieveResponse(), largePayload);

        assertThat(rap.getPayload()).isEqualTo(largePayload);
        assertThat(rap.getPayload().length()).isGreaterThan(10000);
    }

    @Test
    public void testImmutability_ResponseFieldIsImmutable() {
        ManageSieveResponse response = new ManageSieveResponse();
        ResponseAndPayload rap = new ResponseAndPayload(response, "payload");

        // The field itself is final (immutable reference)
        // But we can verify it always returns the same instance
        assertThat(rap.getResponse()).isSameAs(response);
    }

    @Test
    public void testImmutability_PayloadFieldIsImmutable() {
        String payload = "original";
        ResponseAndPayload rap = new ResponseAndPayload(new ManageSieveResponse(), payload);

        // Strings are immutable in Java, so this is safe
        assertThat(rap.getPayload()).isEqualTo(payload);
    }

    @Test
    public void testRealWorldScenario_GetScriptResponse() {
        // Simulate a GETSCRIPT response with OK status and script body
        ManageSieveResponse response = new ManageSieveResponse();
        String scriptBody = "require [\"fileinto\", \"reject\"];\n" +
                "if anyof (header :contains \"from\" \"spam\") {\n" +
                "  reject \"Go away spammer\";\n" +
                "} else {\n" +
                "  keep;\n" +
                "}";

        ResponseAndPayload result = new ResponseAndPayload(response, scriptBody);

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getPayload()).contains("fileinto");
        assertThat(result.getPayload()).contains("reject");
    }
}
