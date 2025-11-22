package com.fluffypeople.managesieve;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ManageSieveResponse class.
 * Tests response parsing, type handling, codes, and subcodes.
 */
public class ManageSieveResponseTest {

    @Test
    public void testSetType_OK() throws ParseException {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setType("OK");

        assertThat(response.getType()).isEqualTo(ManageSieveResponse.Type.OK);
        assertTrue(response.isOk());
        assertFalse(response.isNo());
        assertFalse(response.isBye());
    }

    @Test
    public void testSetType_NO() throws ParseException {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setType("NO");

        assertThat(response.getType()).isEqualTo(ManageSieveResponse.Type.NO);
        assertFalse(response.isOk());
        assertTrue(response.isNo());
        assertFalse(response.isBye());
    }

    @Test
    public void testSetType_BYE() throws ParseException {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setType("BYE");

        assertThat(response.getType()).isEqualTo(ManageSieveResponse.Type.BYE);
        assertFalse(response.isOk());
        assertFalse(response.isNo());
        assertTrue(response.isBye());
    }

    @Test
    public void testSetType_CaseInsensitive() throws ParseException {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setType("ok");
        assertThat(response.getType()).isEqualTo(ManageSieveResponse.Type.OK);

        response = new ManageSieveResponse();
        response.setType("No");
        assertThat(response.getType()).isEqualTo(ManageSieveResponse.Type.NO);

        response = new ManageSieveResponse();
        response.setType("bYe");
        assertThat(response.getType()).isEqualTo(ManageSieveResponse.Type.BYE);
    }

    @Test
    public void testSetType_Invalid() {
        ManageSieveResponse response = new ManageSieveResponse();

        assertThatThrownBy(() -> response.setType("INVALID"))
            .isInstanceOf(ParseException.class)
            .hasMessageContaining("Invalid response type");
    }

    @Test
    public void testSetCode_SASL() {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setCode("SASL");

        assertThat(response.getCode()).isEqualTo(ManageSieveResponse.Code.SASL);
        assertThat(response.getSubCodes()).containsExactly("SASL");
    }

    @Test
    public void testSetCode_WithDashes() {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setCode("AUTH-TOO-WEAK");

        assertThat(response.getCode()).isEqualTo(ManageSieveResponse.Code.AUTH_TOO_WEAK);
    }

    @Test
    public void testSetCode_WithSubCodes() {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setCode("SASL/PLAIN/SERVER-ERROR");

        assertThat(response.getCode()).isEqualTo(ManageSieveResponse.Code.SASL);
        assertThat(response.getSubCodes()).containsExactly("SASL", "PLAIN", "SERVER-ERROR");
    }

    @Test
    public void testSetCode_UnknownExtension() {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setCode("CUSTOM-EXTENSION");

        assertThat(response.getCode()).isEqualTo(ManageSieveResponse.Code.extension);
    }

    @Test
    public void testCodeHasParam_SASL() {
        assertThat(ManageSieveResponse.Code.SASL.hasParam()).isTrue();
    }

    @Test
    public void testCodeHasParam_REFERRAL() {
        assertThat(ManageSieveResponse.Code.REFERRAL.hasParam()).isTrue();
    }

    @Test
    public void testCodeHasParam_TAG() {
        assertThat(ManageSieveResponse.Code.TAG.hasParam()).isTrue();
    }

    @Test
    public void testCodeHasParam_NoParam() {
        assertThat(ManageSieveResponse.Code.AUTH_TOO_WEAK.hasParam()).isFalse();
        assertThat(ManageSieveResponse.Code.ENCRYPT_NEEDED.hasParam()).isFalse();
        assertThat(ManageSieveResponse.Code.QUOTA.hasParam()).isFalse();
    }

    @Test
    public void testSetMessage() {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setMessage("Test message");

        assertThat(response.getMessage()).isEqualTo("Test message");
    }

    @Test
    public void testSetParam() {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setParam("PLAIN");

        assertThat(response.getParam()).isEqualTo("PLAIN");
    }

    @Test
    public void testToString_Simple() throws ParseException {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setType("OK");

        assertThat(response.toString()).isEqualTo("OK");
    }

    @Test
    public void testToString_WithCode() throws ParseException {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setType("NO");
        response.setCode("QUOTA");

        assertThat(response.toString()).isEqualTo("NO (QUOTA)");
    }

    @Test
    public void testToString_WithMessage() throws ParseException {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setType("OK");
        response.setMessage("Command completed");

        assertThat(response.toString()).isEqualTo("OK \"Command completed\"");
    }

    @Test
    public void testToString_Complete() throws ParseException {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setType("NO");
        response.setCode("QUOTA");
        response.setMessage("Quota exceeded");

        assertThat(response.toString()).isEqualTo("NO (QUOTA) \"Quota exceeded\"");
    }

    @Test
    public void testCodeFromString_AllKnownCodes() {
        assertThat(ManageSieveResponse.Code.fromString("AUTH-TOO-WEAK"))
            .isEqualTo(ManageSieveResponse.Code.AUTH_TOO_WEAK);
        assertThat(ManageSieveResponse.Code.fromString("ENCRYPT-NEEDED"))
            .isEqualTo(ManageSieveResponse.Code.ENCRYPT_NEEDED);
        assertThat(ManageSieveResponse.Code.fromString("SASL"))
            .isEqualTo(ManageSieveResponse.Code.SASL);
        assertThat(ManageSieveResponse.Code.fromString("REFERRAL"))
            .isEqualTo(ManageSieveResponse.Code.REFERRAL);
        assertThat(ManageSieveResponse.Code.fromString("TRANSITION-NEEDED"))
            .isEqualTo(ManageSieveResponse.Code.TRANSITION_NEEDED);
        assertThat(ManageSieveResponse.Code.fromString("TRYLATER"))
            .isEqualTo(ManageSieveResponse.Code.TRYLATER);
        assertThat(ManageSieveResponse.Code.fromString("ACTIVE"))
            .isEqualTo(ManageSieveResponse.Code.ACTIVE);
        assertThat(ManageSieveResponse.Code.fromString("NONEXISTENT"))
            .isEqualTo(ManageSieveResponse.Code.NONEXISTENT);
        assertThat(ManageSieveResponse.Code.fromString("ALREADYEXITS"))
            .isEqualTo(ManageSieveResponse.Code.ALREADYEXITS);
        assertThat(ManageSieveResponse.Code.fromString("WARNINGS"))
            .isEqualTo(ManageSieveResponse.Code.WARNINGS);
        assertThat(ManageSieveResponse.Code.fromString("TAG"))
            .isEqualTo(ManageSieveResponse.Code.TAG);
        assertThat(ManageSieveResponse.Code.fromString("QUOTA"))
            .isEqualTo(ManageSieveResponse.Code.QUOTA);
    }

    @Test
    public void testGetSubCodes_CopyDefensively() {
        ManageSieveResponse response = new ManageSieveResponse();
        response.setCode("SASL/PLAIN");

        String[] subCodes = response.getSubCodes();
        subCodes[0] = "MODIFIED";

        // Original should not be modified
        assertThat(response.getSubCodes()[0]).isEqualTo("SASL");
    }
}
