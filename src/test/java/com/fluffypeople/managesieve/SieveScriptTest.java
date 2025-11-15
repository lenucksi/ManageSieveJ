package com.fluffypeople.managesieve;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

/**
 * Tests for SieveScript class.
 * Tests data model, equals/hashCode contract, and basic functionality.
 */
public class SieveScriptTest {

    @Test
    public void testDefaultConstructor() {
        SieveScript script = new SieveScript();

        assertThat(script.getName()).isNull();
        assertThat(script.getBody()).isNull();
        assertThat(script.isActive()).isFalse();
    }

    @Test
    public void testParameterizedConstructor() {
        SieveScript script = new SieveScript("vacation", "vacation :days 7 \"On vacation\";", true);

        assertThat(script.getName()).isEqualTo("vacation");
        assertThat(script.getBody()).isEqualTo("vacation :days 7 \"On vacation\";");
        assertThat(script.isActive()).isTrue();
    }

    @Test
    public void testSetName() {
        SieveScript script = new SieveScript();
        script.setName("filter");

        assertThat(script.getName()).isEqualTo("filter");
    }

    @Test
    public void testSetBody() {
        SieveScript script = new SieveScript();
        script.setBody("require \"fileinto\";\nfileinto \"Folder\";");

        assertThat(script.getBody()).isEqualTo("require \"fileinto\";\nfileinto \"Folder\";");
    }

    @Test
    public void testSetActive() {
        SieveScript script = new SieveScript();
        assertThat(script.isActive()).isFalse();

        script.setActive(true);
        assertThat(script.isActive()).isTrue();

        script.setActive(false);
        assertThat(script.isActive()).isFalse();
    }

    @Test
    public void testEquals_Reflexive() {
        SieveScript script = new SieveScript("test", "body", true);

        assertEquals(script, script);
    }

    @Test
    public void testEquals_Symmetric() {
        SieveScript script1 = new SieveScript("test", "body1", true);
        SieveScript script2 = new SieveScript("test", "body2", false);

        assertEquals(script1, script2);
        assertEquals(script2, script1);
    }

    @Test
    public void testEquals_Transitive() {
        SieveScript script1 = new SieveScript("test", "body1", true);
        SieveScript script2 = new SieveScript("test", "body2", false);
        SieveScript script3 = new SieveScript("test", "body3", true);

        assertEquals(script1, script2);
        assertEquals(script2, script3);
        assertEquals(script1, script3);
    }

    @Test
    public void testEquals_Null() {
        SieveScript script = new SieveScript("test", "body", true);

        assertNotEquals(script, null);
    }

    @Test
    public void testEquals_DifferentClass() {
        SieveScript script = new SieveScript("test", "body", true);

        assertNotEquals(script, "not a SieveScript");
    }

    @Test
    public void testEquals_SameName() {
        SieveScript script1 = new SieveScript("vacation", "old body", false);
        SieveScript script2 = new SieveScript("vacation", "new body", true);

        assertEquals(script1, script2);
    }

    @Test
    public void testEquals_DifferentName() {
        SieveScript script1 = new SieveScript("vacation", "body", true);
        SieveScript script2 = new SieveScript("filter", "body", true);

        assertNotEquals(script1, script2);
    }

    @Test
    public void testEquals_BothNullNames() {
        SieveScript script1 = new SieveScript(null, "body1", true);
        SieveScript script2 = new SieveScript(null, "body2", false);

        assertEquals(script1, script2);
    }

    @Test
    public void testEquals_OneNullName() {
        SieveScript script1 = new SieveScript("test", "body", true);
        SieveScript script2 = new SieveScript(null, "body", true);

        assertNotEquals(script1, script2);
        assertNotEquals(script2, script1);
    }

    @Test
    public void testHashCode_Consistent() {
        SieveScript script = new SieveScript("test", "body", true);

        int hash1 = script.hashCode();
        int hash2 = script.hashCode();

        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCode_EqualObjects() {
        SieveScript script1 = new SieveScript("vacation", "body1", true);
        SieveScript script2 = new SieveScript("vacation", "body2", false);

        assertEquals(script1.hashCode(), script2.hashCode());
    }

    @Test
    public void testHashCode_DifferentNames() {
        SieveScript script1 = new SieveScript("vacation", "body", true);
        SieveScript script2 = new SieveScript("filter", "body", true);

        // Different names should (likely) have different hash codes
        // This is not guaranteed by the contract, but highly probable
        assertNotEquals(script1.hashCode(), script2.hashCode());
    }

    @Test
    public void testHashCode_NullName() {
        SieveScript script1 = new SieveScript(null, "body", true);
        SieveScript script2 = new SieveScript(null, "body", false);

        // Both null names should have same hash code
        assertEquals(script1.hashCode(), script2.hashCode());
    }

    @Test
    public void testBodyAndActiveNotInEquality() {
        // Equality is based only on name, not body or active status
        SieveScript script1 = new SieveScript("test", "body1", true);
        SieveScript script2 = new SieveScript("test", "body2", false);

        assertEquals(script1, script2);
    }

    @Test
    public void testRealWorldExample_InactiveScript() {
        SieveScript script = new SieveScript();
        script.setName("spam-filter");
        script.setBody("require [\"fileinto\", \"reject\"];\n" +
                "if header :contains \"subject\" \"spam\" {\n" +
                "  fileinto \"Spam\";\n" +
                "}");
        script.setActive(false);

        assertThat(script.getName()).isEqualTo("spam-filter");
        assertThat(script.getBody()).contains("fileinto \"Spam\"");
        assertThat(script.isActive()).isFalse();
    }

    @Test
    public void testRealWorldExample_ActiveScript() {
        SieveScript script = new SieveScript();
        script.setName("main");
        script.setBody("require \"vacation\";\n" +
                "vacation :days 7 :subject \"Out of office\" \"I am on vacation.\";");
        script.setActive(true);

        assertThat(script.getName()).isEqualTo("main");
        assertThat(script.isActive()).isTrue();
    }

    @Test
    public void testUpdate_KeepsIdentity() {
        SieveScript script = new SieveScript("test", "old body", false);
        int originalHash = script.hashCode();

        // Update body and active status
        script.setBody("new body");
        script.setActive(true);

        // Name hasn't changed, so hashCode should be same
        assertThat(script.hashCode()).isEqualTo(originalHash);
        assertThat(script.getName()).isEqualTo("test");
    }

    @Test
    public void testUpdate_ChangeName() {
        SieveScript script = new SieveScript("old-name", "body", true);
        SieveScript otherScript = new SieveScript("old-name", "different", false);

        // Initially equal (same name)
        assertEquals(script, otherScript);

        // Change name
        script.setName("new-name");

        // No longer equal
        assertNotEquals(script, otherScript);
    }
}
