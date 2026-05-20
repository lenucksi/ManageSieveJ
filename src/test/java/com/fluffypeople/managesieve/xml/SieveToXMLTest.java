package com.fluffypeople.managesieve.xml;

import com.fluffypeople.managesieve.ParseException;
import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SieveToXMLTest {

    private SieveToXML converter;

    @BeforeEach
    void setUp() {
        converter = new SieveToXML();
    }

    private Document convertDoc(String script) throws Exception {
        return converter.convert(script).getDocument();
    }

    private String xmlString(String script) throws Exception {
        return converter.convert(script).toString();
    }

    private static Element childElement(Element parent, int index) {
        int count = 0;
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                if (count == index) {
                    return (Element) children.item(i);
                }
                count++;
            }
        }
        return null;
    }

    private static List<Element> childElements(Element parent) {
        List<Element> result = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                result.add((Element) children.item(i));
            }
        }
        return result;
    }

    private static Element firstByTag(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() > 0) {
            return (Element) list.item(0);
        }
        return null;
    }

    @Nested
    @DisplayName("Conversion entry points")
    class ConversionEntryTests {

        @Test
        void shouldConvertFromStringScript() throws Exception {
            Document doc = convertDoc("discard;");
            Element root = doc.getDocumentElement();
            assertThat(root.getTagName()).isEqualTo("sieve");
            assertThat(root.getAttribute("xmlns")).isEqualTo("urn:ietf:params:xml:ns:sieve");
        }

        @Test
        void shouldConvertFromReader() throws Exception {
            Document doc = converter.convert(new StringReader("stop;")).getDocument();
            Element root = doc.getDocumentElement();
            assertThat(root.getTagName()).isEqualTo("sieve");
            assertThat(childElement(root, 0).getAttribute("name")).isEqualTo("stop");
        }

        @Test
        void shouldHandleEmptyScript() throws Exception {
            Document doc = convertDoc("");
            Element root = doc.getDocumentElement();
            assertThat(childElements(root)).isEmpty();
        }

        @Test
        void shouldHandleScriptWithOnlyWhitespace() throws Exception {
            Document doc = convertDoc("  \t  \n  ");
            Element root = doc.getDocumentElement();
            assertThat(childElements(root)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Control structures")
    class ControlTests {

        @Test
        void shouldConvertRequireWithFileinto() throws Exception {
            String xml = xmlString("require [\"fileinto\"];");
            assertThat(xml).contains("<control name=\"require\">");
            assertThat(xml).contains("<list>");
            assertThat(xml).contains("<str>fileinto</str>");
        }

        @Test
        void shouldConvertRequireWithMultipleCapabilities() throws Exception {
            String xml = xmlString("require [\"fileinto\", \"copy\", \"vacation\"];");
            assertThat(xml).contains("<control name=\"require\">");
            assertThat(xml).contains("<str>fileinto</str>");
            assertThat(xml).contains("<str>copy</str>");
            assertThat(xml).contains("<str>vacation</str>");
        }

        @Test
        void shouldConvertIfBlockWithDiscard() throws Exception {
            Document doc = convertDoc("if true { discard; }");
            Element root = doc.getDocumentElement();
            Element control = childElement(root, 0);
            assertThat(control.getAttribute("name")).isEqualTo("if");
            Element test = childElement(control, 0);
            assertThat(test.getTagName()).isEqualTo("test");
            assertThat(test.getAttribute("name")).isEqualTo("true");
            Element block = childElement(control, 1);
            assertThat(block.getTagName()).isEqualTo("action");
            assertThat(block.getAttribute("name")).isEqualTo("discard");
        }

        @Test
        void shouldConvertIfElseChain() throws Exception {
            String xml = xmlString("if false { } elsif true { } else { }");
            assertThat(xml).contains("<control name=\"if\">");
            assertThat(xml).contains("<control name=\"elsif\">");
            assertThat(xml).contains("<control name=\"else\"/>");
        }

        @Test
        void shouldConvertIfWithComplexBlock() throws Exception {
            Document doc = convertDoc("if true { fileinto \"INBOX\"; redirect \"x@y.com\"; }");
            Element root = doc.getDocumentElement();
            Element control = childElement(root, 0);
            Element test = childElement(control, 0);
            assertThat(test.getTagName()).isEqualTo("test");
            assertThat(test.getAttribute("name")).isEqualTo("true");
            Element block = childElement(control, 1);
            assertThat(block.getTagName()).isEqualTo("action");
            assertThat(block.getAttribute("name")).isEqualTo("fileinto");
            Element action2 = childElement(control, 2);
            assertThat(action2.getTagName()).isEqualTo("action");
            assertThat(action2.getAttribute("name")).isEqualTo("redirect");
        }

        @Test
        void shouldConvertStop() throws Exception {
            Document doc = convertDoc("stop;");
            Element control = childElement(doc.getDocumentElement(), 0);
            assertThat(control.getTagName()).isEqualTo("control");
            assertThat(control.getAttribute("name")).isEqualTo("stop");
            assertThat(childElements(control)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Actions")
    class ActionTests {

        @Test
        void shouldConvertDiscard() throws Exception {
            Document doc = convertDoc("discard;");
            Element action = childElement(doc.getDocumentElement(), 0);
            assertThat(action.getTagName()).isEqualTo("action");
            assertThat(action.getAttribute("name")).isEqualTo("discard");
            assertThat(childElements(action)).isEmpty();
        }

        @Test
        void shouldConvertFileintoWithString() throws Exception {
            String xml = xmlString("fileinto \"INBOX\";");
            assertThat(xml).contains("<action name=\"fileinto\">");
            assertThat(xml).contains("<str>INBOX</str>");
        }

        @Test
        void shouldConvertRedirect() throws Exception {
            String xml = xmlString("redirect \"user@example.com\";");
            assertThat(xml).contains("<action name=\"redirect\">");
            assertThat(xml).contains("<str>user@example.com</str>");
        }

        @Test
        void shouldConvertFileintoWithCopyTag() throws Exception {
            Document doc = convertDoc("fileinto :copy \"INBOX.foo\";");
            Element action = childElement(doc.getDocumentElement(), 0);
            List<Element> children = childElements(action);
            assertThat(children).hasSize(2);
            assertThat(children.get(0).getTagName()).isEqualTo("tag");
            assertThat(children.get(0).getTextContent()).isEqualTo("copy");
            assertThat(children.get(1).getTagName()).isEqualTo("str");
            assertThat(children.get(1).getTextContent()).isEqualTo("INBOX.foo");
        }

        @Test
        void shouldConvertRejectWithString() throws Exception {
            String xml = xmlString("reject \"I don't like it\";");
            assertThat(xml).contains("<action name=\"reject\">");
            assertThat(xml).contains("<str>I don't like it</str>");
        }

        @Test
        void shouldConvertVacationWithMultipleArgs() throws Exception {
            String xml = xmlString("vacation :days 5 :subject \"Away\" \"I am out\";");
            assertThat(xml).contains("<action name=\"vacation\">");
            assertThat(xml).contains("<tag>days</tag>");
            assertThat(xml).contains("<num>5</num>");
            assertThat(xml).contains("<tag>subject</tag>");
            assertThat(xml).contains("<str>Away</str>");
            assertThat(xml).contains("<str>I am out</str>");
        }

        @Test
        void shouldConvertKeep() throws Exception {
            Document doc = convertDoc("keep;");
            Element action = childElement(doc.getDocumentElement(), 0);
            assertThat(action.getTagName()).isEqualTo("action");
            assertThat(action.getAttribute("name")).isEqualTo("keep");
        }
    }

    @Nested
    @DisplayName("String arguments")
    class StringTests {

        @Test
        void shouldConvertQuotedString() throws Exception {
            String xml = xmlString("fileinto \"INBOX\";");
            assertThat(xml).contains("<str>INBOX</str>");
        }

        @Test
        void shouldConvertEmptyQuotedString() throws Exception {
            String xml = xmlString("fileinto \"\";");
            assertThat(xml).contains("<str/>");
        }

        @Test
        void shouldConvertStringWithSpecialChars() throws Exception {
            String xml = xmlString("fileinto \"foo bar\";");
            assertThat(xml).contains("<str>foo bar</str>");
        }

        @Test
        void shouldConvertStringWithNumbers() throws Exception {
            String xml = xmlString("fileinto \"folder123\";");
            assertThat(xml).contains("<str>folder123</str>");
        }

        @Test
        void shouldConvertStringList() throws Exception {
            Document doc = convertDoc("require [\"fileinto\", \"vacation\", \"copy\"];");
            Element root = doc.getDocumentElement();
            Element list = firstByTag(root, "list");
            assertThat(list).isNotNull();
            List<Element> strs = childElements(list);
            assertThat(strs).hasSize(3);
            assertThat(strs.get(0).getTextContent()).isEqualTo("fileinto");
            assertThat(strs.get(1).getTextContent()).isEqualTo("vacation");
            assertThat(strs.get(2).getTextContent()).isEqualTo("copy");
        }

        @Test
        void shouldConvertStringListWithSingleElement() throws Exception {
            String xml = xmlString("require [\"fileinto\"];");
            assertThat(xml).contains("<list>");
            assertThat(xml).contains("<str>fileinto</str>");
        }

        @Test
        void shouldConvertStringListWithEmptyString() throws Exception {
            String xml = xmlString("require [\"\"];");
            assertThat(xml).contains("<list>");
            assertThat(xml).contains("<str/>");
        }
    }

    @Nested
    @DisplayName("Multi-line strings")
    class MultiLineStringTests {

        @Test
        void shouldConvertBasicMultiLineString() throws Exception {
            String xml = xmlString("vacation text:\nhello\nworld\n.\n;");
            assertThat(xml).contains("<str>hello");
            assertThat(xml).contains("world");
        }

        @Test
        void shouldConvertMultiLineStringWithDotStuffing() throws Exception {
            String xml = xmlString("vacation text:\n..escaped\n.\n;");
            assertThat(xml).contains("<str>.escaped");
        }

        @Test
        void shouldConvertMultiLineStringEmpty() throws Exception {
            String xml = xmlString("vacation text:\n.\n;");
            assertThat(xml).contains("<str/>");
        }

        @Test
        void shouldConvertMultiLineStringSingleLine() throws Exception {
            String xml = xmlString("vacation text:\nHello there\n.\n;");
            assertThat(xml).contains("<str>Hello there");
        }
    }

    @Nested
    @DisplayName("Numbers and tags")
    class NumberAndTagTests {

        @Test
        void shouldConvertPlainNumber() throws Exception {
            String xml = xmlString("vacation :days 5;");
            assertThat(xml).contains("<num>5</num>");
        }

        @Test
        void shouldConvertNumberWithKMultiplier() throws Exception {
            String xml = xmlString("vacation :size 3K;");
            assertThat(xml).contains("<num>3072</num>");
        }

        @Test
        void shouldConvertNumberWithMMultiplier() throws Exception {
            String xml = xmlString("vacation :size 2M;");
            assertThat(xml).contains("<num>2097152</num>");
        }

        @Test
        void shouldConvertNumberWithGMultiplier() throws Exception {
            String xml = xmlString("vacation :size 1G;");
            assertThat(xml).contains("<num>1073741824</num>");
        }

        @Test
        void shouldConvertNumberWithLowercaseMultiplier() throws Exception {
            String xml = xmlString("vacation :size 3k;");
            assertThat(xml).contains("<num>3072</num>");
        }

        @Test
        void shouldConvertTag() throws Exception {
            String xml = xmlString("fileinto :copy \"INBOX\";");
            assertThat(xml).contains("<tag>copy</tag>");
        }

        @Test
        void shouldConvertMultipleTags() throws Exception {
            String xml = xmlString("fileinto :copy :owner \"INBOX\";");
            assertThat(xml).contains("<tag>copy</tag>");
            assertThat(xml).contains("<tag>owner</tag>");
            assertThat(xml).contains("<str>INBOX</str>");
        }

        @Test
        void shouldConvertNumberWithMultipleDigits() throws Exception {
            String xml = xmlString("vacation :days 365;");
            assertThat(xml).contains("<num>365</num>");
        }
    }

    @Nested
    @DisplayName("Tests and conditions")
    class TestTests {

        @Test
        void shouldConvertAddressTest() throws Exception {
            String xml = xmlString("if address :is \"me@here.com\" { discard; }");
            assertThat(xml).contains("<test name=\"address\">");
            assertThat(xml).contains("<tag>is</tag>");
            assertThat(xml).contains("<str>me@here.com</str>");
        }

        @Test
        void shouldConvertAddressTestWithStringList() throws Exception {
            String xml = xmlString("if address :is [\"me@here.com\", \"you@there.com\"] { discard; }");
            assertThat(xml).contains("<test name=\"address\">");
            assertThat(xml).contains("<tag>is</tag>");
            assertThat(xml).contains("<list>");
            assertThat(xml).contains("<str>me@here.com</str>");
            assertThat(xml).contains("<str>you@there.com</str>");
        }

        @Test
        void shouldConvertAddressWithMultipleTags() throws Exception {
            String xml = xmlString("if address :all :is \"me@here.com\" { discard; }");
            assertThat(xml).contains("<test name=\"address\">");
            assertThat(xml).contains("<tag>all</tag>");
            assertThat(xml).contains("<tag>is</tag>");
            assertThat(xml).contains("<str>me@here.com</str>");
        }

        @Test
        void shouldConvertExistsTest() throws Exception {
            String xml = xmlString("if exists \"From\" { discard; }");
            assertThat(xml).contains("<test name=\"exists\">");
            assertThat(xml).contains("<str>From</str>");
        }

        @Test
        void shouldConvertNotExistsTest() throws Exception {
            String xml = xmlString("if not exists \"From\" { discard; }");
            assertThat(xml).contains("<test name=\"not\">");
        }

        @Test
        void shouldConvertSizeTest() throws Exception {
            String xml = xmlString("if size :over 100K { discard; }");
            assertThat(xml).contains("<test name=\"size\">");
            assertThat(xml).contains("<tag>over</tag>");
            assertThat(xml).contains("<num>102400</num>");
        }

        @Test
        void shouldConvertTestList() throws Exception {
            String xml = xmlString("if anyof (exists \"From\", exists \"Subject\") { discard; }");
            assertThat(xml).contains("<test name=\"anyof\">");
        }

        @Test
        void shouldConvertAllofTestList() throws Exception {
            String xml = xmlString("if allof (exists \"From\", exists \"Subject\") { fileinto \"INBOX\"; }");
            assertThat(xml).contains("<test name=\"allof\">");
        }

        @Test
        void shouldConvertTrueTest() throws Exception {
            String xml = xmlString("if true { stop; }");
            assertThat(xml).contains("<test name=\"true\"/>");
        }
    }

    @Nested
    @DisplayName("Comments")
    class CommentTests {

        @Test
        void shouldIgnoreHashComment() throws Exception {
            Document doc = convertDoc("# this is a comment\ndiscard;");
            Element root = doc.getDocumentElement();
            List<Element> children = childElements(root);
            assertThat(children).hasSize(1);
            assertThat(children.get(0).getAttribute("name")).isEqualTo("discard");
        }

        @Test
        void shouldIgnoreSlashStarComment() throws Exception {
            Document doc = convertDoc("/* comment */ discard;");
            Element root = doc.getDocumentElement();
            List<Element> children = childElements(root);
            assertThat(children).hasSize(1);
            assertThat(children.get(0).getAttribute("name")).isEqualTo("discard");
        }

        @Test
        void shouldIgnoreCommentsBetweenCommands() throws Exception {
            Document doc = convertDoc("discard; # first\nkeep; /* second */");
            Element root = doc.getDocumentElement();
            List<Element> children = childElements(root);
            assertThat(children).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Error handling")
    class ErrorHandlingTests {

        @Test
        void shouldThrowWhenMissingSemicolon() {
            assertThatThrownBy(() -> converter.convert("fileinto \"INBOX\""))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting");
        }

        @Test
        void shouldThrowWhenUnclosedBlock() {
            assertThatThrownBy(() -> converter.convert("if true { discard;"))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting");
        }

        @Test
        void shouldThrowWhenUnexpectedTokenInBlock() {
            assertThatThrownBy(() -> converter.convert("if true { discard ]"))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting");
        }

        @Test
        void shouldThrowWhenTagMissingWord() {
            assertThatThrownBy(() -> converter.convert("fileinto : ;"))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting");
        }

        @Test
        void shouldThrowWhenTextMissingColon() {
            assertThatThrownBy(() -> converter.convert("fileinto text ;"))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting");
        }

        @Test
        void shouldThrowWhenInvalidTokenExpectingString() {
            assertThatThrownBy(() -> converter.convert("fileinto : ;"))
                    .isInstanceOf(ParseException.class);
        }

        @Test
        void shouldThrowWhenAddressTestListMissingCloseParen() {
            assertThatThrownBy(() -> converter.convert("if anyof (exists \"From\" { discard; }"))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting");
        }

        @Test
        void shouldParseIfWithTestNamedDiscard() throws Exception {
            Document doc = convertDoc("if true discard; }");
            Element root = doc.getDocumentElement();
            Element ifControl = childElement(root, 0);
            assertThat(ifControl.getAttribute("name")).isEqualTo("if");
            Element test = childElement(ifControl, 0);
            assertThat(test.getAttribute("name")).isEqualTo("true");
            Element nestedTest = childElement(test, 0);
            assertThat(nestedTest.getAttribute("name")).isEqualTo("discard");
        }

        @Test
        void shouldThrowWhenNumberExpectedButGotString() {
            assertThatThrownBy(() -> converter.convert("fileinto discard : ;"))
                    .isInstanceOf(ParseException.class);
        }

        @Test
        void shouldThrowOnUnclosedStringList() {
            assertThatThrownBy(() -> converter.convert("require [\"fileinto\";"))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting");
        }

        @Test
        void shouldThrowOnCommandWithoutSemicolon() {
            assertThatThrownBy(() -> converter.convert("discard; keep"))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting");
        }

        @Test
        void shouldThrowOnBareTextKeyword() {
            assertThatThrownBy(() -> converter.convert("if text { }"))
                    .isInstanceOf(ParseException.class)
                    .hasMessageContaining("Expecting");
        }
    }

    @Nested
    @DisplayName("Complex scripts")
    class ComplexScriptTests {

        @Test
        void shouldConvertRequireAndSimpleRule() throws Exception {
            Document doc = convertDoc("require [\"fileinto\"];\nif true { fileinto \"INBOX\"; }");
            Element root = doc.getDocumentElement();
            List<Element> commands = childElements(root);
            assertThat(commands).hasSize(2);
            assertThat(commands.get(0).getAttribute("name")).isEqualTo("require");
            assertThat(commands.get(1).getAttribute("name")).isEqualTo("if");
        }

        @Test
        void shouldConvertMultipleActionsInBlock() throws Exception {
            Document doc = convertDoc("if true { fileinto \"INBOX\"; redirect \"x@y.com\"; }");
            Element root = doc.getDocumentElement();
            Element ifControl = childElement(root, 0);
            List<Element> children = childElements(ifControl);
            assertThat(children).hasSize(3);
            assertThat(children.get(0).getTagName()).isEqualTo("test");
            assertThat(children.get(1).getTagName()).isEqualTo("action");
            assertThat(children.get(1).getAttribute("name")).isEqualTo("fileinto");
            assertThat(children.get(2).getTagName()).isEqualTo("action");
            assertThat(children.get(2).getAttribute("name")).isEqualTo("redirect");
        }
    }
}
