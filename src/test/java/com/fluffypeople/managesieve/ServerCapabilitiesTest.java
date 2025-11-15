package com.fluffypeople.managesieve;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

/**
 * Tests for ServerCapabilities class.
 * Tests capability parsing, validation, and accessors.
 */
public class ServerCapabilitiesTest {

    private ServerCapabilities capabilities;

    @BeforeMethod
    public void setUp() {
        capabilities = new ServerCapabilities();
    }

    @Test
    public void testSetImplementationName() {
        capabilities.setImplementationName("Dovecot 2.3");
        assertThat(capabilities.getImplementationName()).isEqualTo("Dovecot 2.3");
    }

    @Test
    public void testSetSASLMethods_Single() {
        capabilities.setSASLMethods("PLAIN");

        assertThat(capabilities.getSASLMethods()).containsExactly("PLAIN");
        assertTrue(capabilities.hasSASLMethod("PLAIN"));
        assertFalse(capabilities.hasSASLMethod("CRAM-MD5"));
    }

    @Test
    public void testSetSASLMethods_Multiple() {
        capabilities.setSASLMethods("PLAIN CRAM-MD5 DIGEST-MD5");

        assertThat(capabilities.getSASLMethods()).containsExactlyInAnyOrder("PLAIN", "CRAM-MD5", "DIGEST-MD5");
        assertTrue(capabilities.hasSASLMethod("PLAIN"));
        assertTrue(capabilities.hasSASLMethod("CRAM-MD5"));
        assertTrue(capabilities.hasSASLMethod("DIGEST-MD5"));
    }

    @Test
    public void testSetSASLMethods_Replace() {
        capabilities.setSASLMethods("PLAIN");
        capabilities.setSASLMethods("CRAM-MD5");

        assertThat(capabilities.getSASLMethods()).containsExactly("CRAM-MD5");
        assertFalse(capabilities.hasSASLMethod("PLAIN"));
    }

    @Test
    public void testSetSieveExtensions_Single() {
        capabilities.setSieveExtensions("fileinto");

        assertTrue(capabilities.hasSieveExtension("fileinto"));
        assertFalse(capabilities.hasSieveExtension("vacation"));
    }

    @Test
    public void testSetSieveExtensions_Multiple() {
        capabilities.setSieveExtensions("fileinto vacation reject envelope");

        assertTrue(capabilities.hasSieveExtension("fileinto"));
        assertTrue(capabilities.hasSieveExtension("vacation"));
        assertTrue(capabilities.hasSieveExtension("reject"));
        assertTrue(capabilities.hasSieveExtension("envelope"));
    }

    @Test
    public void testSetSieveExtensions_Replace() {
        capabilities.setSieveExtensions("fileinto");
        capabilities.setSieveExtensions("vacation");

        assertFalse(capabilities.hasSieveExtension("fileinto"));
        assertTrue(capabilities.hasSieveExtension("vacation"));
    }

    @Test
    public void testSetHasTLS() {
        assertFalse(capabilities.hasTLS());

        capabilities.setHasTLS(true);
        assertTrue(capabilities.hasTLS());

        capabilities.setHasTLS(false);
        assertFalse(capabilities.hasTLS());
    }

    @Test
    public void testSetNotify() {
        capabilities.setNotify("mailto xmpp");

        assertTrue(capabilities.hasNotify("mailto"));
        assertTrue(capabilities.hasNotify("xmpp"));
        assertFalse(capabilities.hasNotify("sms"));
    }

    @Test
    public void testSetNotify_CaseInsensitive() {
        capabilities.setNotify("MailTo XMPP");

        // hasNotify should check lowercase
        assertTrue(capabilities.hasNotify("mailto"));
        assertTrue(capabilities.hasNotify("MAILTO"));
        assertTrue(capabilities.hasNotify("xmpp"));
    }

    @Test
    public void testSetMaxRedirects() {
        capabilities.setMaxRedirects(5);
        assertThat(capabilities.getMaxRedirects()).isEqualTo(5);
    }

    @Test
    public void testSetLanguage() {
        capabilities.setLanguage("en");
        assertThat(capabilities.getLanguage()).isEqualTo("en");
    }

    @Test
    public void testSetOwner() {
        capabilities.setOwner("user@example.com");
        assertThat(capabilities.getOwner()).isEqualTo("user@example.com");
    }

    @Test
    public void testSetVersion() {
        capabilities.setVersion("1.0");
        assertThat(capabilities.getVersion()).isEqualTo("1.0");
    }

    @Test
    public void testIsValid_True() {
        capabilities.setVersion("1.0");
        capabilities.setImplementationName("TestServer");
        capabilities.setSieveExtensions("fileinto");

        assertTrue(capabilities.isValid());
    }

    @Test
    public void testIsValid_NullVersion() {
        capabilities.setImplementationName("TestServer");
        capabilities.setSieveExtensions("fileinto");

        assertFalse(capabilities.isValid());
    }

    @Test
    public void testIsValid_WrongVersion() {
        capabilities.setVersion("2.0");
        capabilities.setImplementationName("TestServer");
        capabilities.setSieveExtensions("fileinto");

        assertFalse(capabilities.isValid());
    }

    @Test
    public void testIsValid_NullImplementationName() {
        capabilities.setVersion("1.0");
        capabilities.setSieveExtensions("fileinto");

        assertFalse(capabilities.isValid());
    }

    @Test
    public void testIsValid_EmptyImplementationName() {
        capabilities.setVersion("1.0");
        capabilities.setImplementationName("");
        capabilities.setSieveExtensions("fileinto");

        assertFalse(capabilities.isValid());
    }

    @Test
    public void testIsValid_NoSieveExtensions() {
        capabilities.setVersion("1.0");
        capabilities.setImplementationName("TestServer");

        assertFalse(capabilities.isValid());
    }

    @Test
    public void testGetSASLMethods_ReturnsArray() {
        capabilities.setSASLMethods("PLAIN CRAM-MD5");

        String[] methods = capabilities.getSASLMethods();
        assertThat(methods).hasSize(2);
        assertThat(methods).containsExactlyInAnyOrder("PLAIN", "CRAM-MD5");
    }

    @Test
    public void testParsing_MultipleSpaces() {
        capabilities.setSASLMethods("PLAIN    CRAM-MD5     DIGEST-MD5");

        assertThat(capabilities.getSASLMethods()).containsExactlyInAnyOrder("PLAIN", "CRAM-MD5", "DIGEST-MD5");
    }

    @Test
    public void testParsing_Tabs() {
        capabilities.setSieveExtensions("fileinto\tvacation\treject");

        assertTrue(capabilities.hasSieveExtension("fileinto"));
        assertTrue(capabilities.hasSieveExtension("vacation"));
        assertTrue(capabilities.hasSieveExtension("reject"));
    }

    @Test
    public void testDefaultValues() {
        ServerCapabilities fresh = new ServerCapabilities();

        assertThat(fresh.getImplementationName()).isNull();
        assertThat(fresh.getVersion()).isNull();
        assertThat(fresh.getLanguage()).isNull();
        assertThat(fresh.getOwner()).isNull();
        assertThat(fresh.getMaxRedirects()).isEqualTo(0);
        assertThat(fresh.hasTLS()).isFalse();
        assertThat(fresh.getSASLMethods()).isEmpty();
        assertThat(fresh.isValid()).isFalse();
    }

    @Test
    public void testRealisticDovecotCapabilities() {
        // Simulate a typical Dovecot server response
        capabilities.setImplementationName("Dovecot Pigeonhole");
        capabilities.setVersion("1.0");
        capabilities.setSASLMethods("PLAIN LOGIN");
        capabilities.setSieveExtensions("fileinto reject envelope encoded-character vacation subaddress comparator-i;ascii-numeric relational regex imap4flags copy include variables body enotify environment mailbox date index ihave duplicate mime foreverypart extracttext");
        capabilities.setHasTLS(true);
        capabilities.setMaxRedirects(4);

        assertTrue(capabilities.isValid());
        assertTrue(capabilities.hasTLS());
        assertTrue(capabilities.hasSASLMethod("PLAIN"));
        assertTrue(capabilities.hasSASLMethod("LOGIN"));
        assertTrue(capabilities.hasSieveExtension("fileinto"));
        assertTrue(capabilities.hasSieveExtension("vacation"));
        assertTrue(capabilities.hasSieveExtension("regex"));
        assertThat(capabilities.getMaxRedirects()).isEqualTo(4);
    }
}
