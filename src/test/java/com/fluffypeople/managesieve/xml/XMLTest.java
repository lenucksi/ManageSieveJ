package com.fluffypeople.managesieve.xml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XMLTest {

    private XML xml;

    @BeforeEach
    void setUp() {
        xml = new XML();
    }

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("should create empty document with no document element")
        void shouldCreateEmptyDocumentWhenDefaultConstructor() {
            assertThat(xml.getDocument()).isNotNull();
            assertThat(xml.getDocument().getDocumentElement()).isNull();
        }

        @Test
        @DisplayName("should create document with stylesheet PI when ref provided")
        void shouldCreateDocumentWithStylesheetPiWhenXsltRefProvided() {
            XML xsltXml = new XML("style.xsl");
            Document doc = xsltXml.getDocument();
            Node firstChild = doc.getFirstChild();
            assertThat(firstChild).isInstanceOf(ProcessingInstruction.class);
            ProcessingInstruction pi = (ProcessingInstruction) firstChild;
            assertThat(pi.getTarget()).isEqualTo("xml-stylesheet");
            assertThat(pi.getData()).contains("style.xsl");
        }

        @Test
        @DisplayName("should create standalone document via static newDocument")
        void shouldCreateStandaloneDocumentWhenNewDocumentCalled() {
            Document doc = XML.newDocument();
            assertThat(doc).isNotNull();
            assertThat(doc.getXmlStandalone()).isTrue();
        }

        @Test
        @DisplayName("should create document with stylesheet via static method with ref")
        void shouldCreateDocumentWithStylesheetWhenNewDocumentWithRef() {
            Document doc = XML.newDocument("ref.xsl");
            Node firstChild = doc.getFirstChild();
            assertThat(firstChild).isInstanceOf(ProcessingInstruction.class);
            assertThat(((ProcessingInstruction) firstChild).getData()).contains("ref.xsl");
        }
    }

    @Nested
    @DisplayName("Element building")
    class ElementBuildingTests {

        @Test
        @DisplayName("should create root element with start and end")
        void shouldCreateRootElementWhenStartAndEnd() {
            XML returned = xml.start("root");
            assertThat(returned).isSameAs(xml);
            xml.end();
            Element root = xml.getDocument().getDocumentElement();
            assertThat(root.getTagName()).isEqualTo("root");
        }

        @Test
        @DisplayName("should create nested elements")
        void shouldCreateNestedElementsWhenStartStartEndEnd() {
            xml.start("a").start("b").end().end();
            Element a = xml.getDocument().getDocumentElement();
            assertThat(a.getTagName()).isEqualTo("a");
            Element b = (Element) a.getFirstChild();
            assertThat(b.getTagName()).isEqualTo("b");
        }

        @Test
        @DisplayName("should create multiple children under same parent")
        void shouldCreateMultipleChildrenUnderSameParent() {
            xml.start("ul");
            xml.add("li", "one");
            xml.add("li", "two");
            xml.end();
            Element ul = xml.getDocument().getDocumentElement();
            assertThat(ul.getChildNodes().getLength()).isEqualTo(2);
        }

        @Test
        @DisplayName("should return to correct parent after end")
        void shouldReturnToCorrectParentWhenEnd() {
            xml.start("a").start("b").end();
            xml.add("c", "text");
            xml.end();
            Element a = xml.getDocument().getDocumentElement();
            assertThat(a.getChildNodes().getLength()).isEqualTo(2);
            Element c = (Element) a.getLastChild();
            assertThat(c.getTagName()).isEqualTo("c");
        }
    }

    @Nested
    @DisplayName("Attributes")
    class AttributeTests {

        @Test
        @DisplayName("should add attributes from map")
        void shouldAddAttributesWhenMapProvided() {
            xml.start("e", Map.of("k", "v")).end();
            assertThat(xml.getDocument().getDocumentElement().getAttribute("k")).isEqualTo("v");
        }

        @Test
        @DisplayName("should add attributes from varargs")
        void shouldAddAttributesWhenVarargProvided() {
            xml.start("e", "k1", "v1", "k2", "v2").end();
            Element e = xml.getDocument().getDocumentElement();
            assertThat(e.getAttribute("k1")).isEqualTo("v1");
            assertThat(e.getAttribute("k2")).isEqualTo("v2");
        }

        @Test
        @DisplayName("should ignore null map when starting element")
        void shouldIgnoreNullMapWhenStartingElement() {
            xml.start("e", (Map<String, String>) null).end();
            assertThat(xml.getDocument().getDocumentElement().getTagName()).isEqualTo("e");
        }

        @Test
        @DisplayName("should handle null attribute value in varargs")
        void shouldHandleNullAttributeValueInVararg() {
            xml.start("e", "k", (String) null).end();
            Element e = xml.getDocument().getDocumentElement();
            assertThat(e.hasAttribute("k")).isTrue();
        }

        @Test
        @DisplayName("should handle null varargs array")
        void shouldHandleNullVarargsArray() {
            xml.start("e", (String[]) null).end();
            assertThat(xml.getDocument().getDocumentElement().getTagName()).isEqualTo("e");
        }

        @Test
        @DisplayName("should add element with text and map attributes")
        void shouldAddElementWithTextAndMapAttributes() {
            xml.add("item", "text", Map.of("a", "b"));
            Element item = xml.getDocument().getDocumentElement();
            assertThat(item.getTagName()).isEqualTo("item");
            assertThat(item.getAttribute("a")).isEqualTo("b");
            assertThat(item.getTextContent()).isEqualTo("text");
        }

        @Test
        @DisplayName("should add element with text and vararg attributes")
        void shouldAddElementWithTextAndVarargAttributes() {
            xml.add("item", "text", "a", "b");
            Element item = xml.getDocument().getDocumentElement();
            assertThat(item.getTagName()).isEqualTo("item");
            assertThat(item.getAttribute("a")).isEqualTo("b");
            assertThat(item.getTextContent()).isEqualTo("text");
        }

        @Test
        @DisplayName("should add element with vararg attributes and null text")
        void shouldAddVarargAttributesWithNullText() {
            xml.add("tag", null, "a", "b");
            Element tag = xml.getDocument().getDocumentElement();
            assertThat(tag.getTagName()).isEqualTo("tag");
            assertThat(tag.getAttribute("a")).isEqualTo("b");
            assertThat(tag.getChildNodes().getLength()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Add methods")
    class AddMethodTests {

        @Test
        @DisplayName("should add empty element when tag only")
        void shouldAddEmptyElementWhenTagOnly() {
            xml.add("tag");
            assertThat(xml.getDocument().getDocumentElement().getTagName()).isEqualTo("tag");
        }

        @Test
        @DisplayName("should add element with text content")
        void shouldAddElementWithTextWhenTagAndText() {
            xml.add("tag", "hello");
            assertThat(xml.getDocument().getDocumentElement().getTextContent()).isEqualTo("hello");
        }

        @Test
        @DisplayName("should add empty element when text is null")
        void shouldAddEmptyElementWhenTextIsNull() {
            xml.add("tag", null);
            assertThat(xml.getDocument().getDocumentElement().getChildNodes().getLength()).isEqualTo(0);
        }

        @Test
        @DisplayName("should add element with int content")
        void shouldAddElementWithIntContent() {
            xml.add("tag", 42);
            assertThat(xml.getDocument().getDocumentElement().getTextContent()).isEqualTo("42");
        }

        @Test
        @DisplayName("should add element with float content")
        void shouldAddElementWithFloatContent() {
            xml.add("tag", 3.14f);
            assertThat(xml.getDocument().getDocumentElement().getTextContent()).isEqualTo("3.14");
        }

        @Test
        @DisplayName("should add element with double content")
        void shouldAddElementWithDoubleContent() {
            xml.add("tag", 2.718);
            assertThat(xml.getDocument().getDocumentElement().getTextContent()).isEqualTo("2.718");
        }

        @Test
        @DisplayName("should add element with boolean content")
        void shouldAddElementWithBooleanContent() {
            xml.add("tag", true);
            assertThat(xml.getDocument().getDocumentElement().getTextContent()).isEqualTo("true");
        }

        @Test
        @DisplayName("should add element with long content")
        void shouldAddElementWithLongContent() {
            xml.add("tag", 100L);
            assertThat(xml.getDocument().getDocumentElement().getTextContent()).isEqualTo("100");
        }

        @Test
        @DisplayName("should add element with object toString content")
        void shouldAddElementWithObjectContent() {
            xml.addObject("tag", "hello");
            assertThat(xml.getDocument().getDocumentElement().getTextContent()).isEqualTo("hello");
        }

        @Test
        @DisplayName("should add empty element when object is null")
        void shouldAddEmptyElementWhenObjectIsNull() {
            xml.addObject("tag", null);
            assertThat(xml.getDocument().getDocumentElement()).isNotNull();
            assertThat(xml.getDocument().getDocumentElement().getChildNodes().getLength()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Serialization")
    class SerializationTests {

        @Test
        @DisplayName("should serialize with XML declaration")
        void shouldSerializeWithXmlDeclaration() {
            xml.start("root").end();
            assertThat(xml.toString()).contains("<?xml version=\"1.0\"");
        }

        @Test
        @DisplayName("should serialize embedded without declaration")
        void shouldSerializeEmbeddedWithoutDeclaration() {
            xml.start("root").end();
            String result = xml.toString(true, false);
            assertThat(result).doesNotContain("<?xml");
            assertThat(result).contains("<root");
        }

        @Test
        @DisplayName("should serialize with indent")
        void shouldSerializeWithIndent() {
            xml.start("root").end();
            assertThat(xml.toString(false, true)).contains("<?xml");
        }

        @Test
        @DisplayName("should serialize without indent")
        void shouldSerializeWithoutIndent() {
            xml.start("root").end();
            assertThat(xml.toString(false, false)).contains("<?xml");
        }

        @Test
        @DisplayName("should serialize with CDATA section")
        void shouldSerializeWithCdataSection() {
            xml.addCdata("script");
            xml.add("script", "code");
            assertThat(xml.toString()).contains("<![CDATA[code]]>");
        }
    }

    @Nested
    @DisplayName("Node operations")
    class NodeOperationTests {

        @Test
        @DisplayName("should import node deep from another document")
        void shouldImportNodeDeepFromAnotherDocument() {
            Document otherDoc = XML.newDocument();
            Element source = otherDoc.createElement("imported");
            source.setTextContent("value");
            otherDoc.appendChild(source);

            xml.start("root");
            xml.add(source, true);
            xml.end();

            assertThat(xml.toString()).contains("<imported>value</imported>");
        }

        @Test
        @DisplayName("should clone node shallow from same document")
        void shouldCloneNodeShallowFromSameDocument() {
            xml.start("parent");
            xml.start("child").end();
            Node childNode = xml.getDocument().getDocumentElement().getFirstChild();
            xml.add(childNode, false);
            xml.end();

            Element parent = xml.getDocument().getDocumentElement();
            assertThat(parent.getChildNodes().getLength()).isEqualTo(2);
        }

        @Test
        @DisplayName("should merge another XML document")
        void shouldMergeAnotherXmlDocument() {
            XML other = new XML();
            other.add("item", "content");

            xml.start("root");
            xml.add(other);
            xml.end();

            assertThat(xml.toString()).contains("<item>content</item>");
        }

        @Test
        @DisplayName("should return underlying Document")
        void shouldReturnUnderlyingDocument() {
            assertThat(xml.getDocument()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Clear")
    class ClearTests {

        @Test
        @DisplayName("should reset document when clear called")
        void shouldResetDocumentWhenClearCalled() {
            xml.add("tag", "content");
            xml.clear();
            assertThat(xml.getDocument().getDocumentElement()).isNull();
        }

        @Test
        @DisplayName("should allow new elements after clear")
        void shouldAllowNewElementsAfterClear() {
            xml.start("first").end();
            xml.clear();
            xml.start("second").end();
            String result = xml.toString();
            assertThat(result).contains("<second");
            assertThat(result).doesNotContain("<first");
        }
    }

    @Nested
    @DisplayName("Stylesheet")
    class StylesheetTests {

        @Test
        @DisplayName("should add stylesheet PI to empty document")
        void shouldAddStylesheetPiToEmptyDocument() {
            xml.setStylesheet("style.xsl");
            Node first = xml.getDocument().getFirstChild();
            assertThat(first).isInstanceOf(ProcessingInstruction.class);
            assertThat(((ProcessingInstruction) first).getTarget()).isEqualTo("xml-stylesheet");
        }

        @Test
        @DisplayName("should insert stylesheet PI before existing content")
        void shouldInsertStylesheetPiBeforeContent() {
            xml.start("root").end();
            xml.setStylesheet("style.xsl");
            Document doc = xml.getDocument();
            Node first = doc.getFirstChild();
            assertThat(first).isInstanceOf(ProcessingInstruction.class);
            assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("should throw when start with odd-length attributes")
        void shouldThrowWhenStartWithOddLengthAttributes() {
            assertThatThrownBy(() -> xml.start("e", "k1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("even length");
        }

        @Test
        @DisplayName("should throw when add with odd-length attributes")
        void shouldThrowWhenAddWithOddLengthAttributes() {
            assertThatThrownBy(() -> xml.add("tag", "text", "k1"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should throw DOMException for invalid attribute name in map")
        void shouldThrowDomExceptionForInvalidAttributeName() {
            assertThatThrownBy(() -> xml.start("e", Map.of("", "value")))
                    .isInstanceOf(DOMException.class);
        }
    }
}
