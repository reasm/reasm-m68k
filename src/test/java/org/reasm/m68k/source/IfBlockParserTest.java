package org.reasm.m68k.source;

import static ca.fragag.testhelpers.HasType.hasType;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.reasm.m68k.source.BlockParserTestsCommon.COMPLETE_BLOCK;
import static org.reasm.m68k.source.BlockParserTestsCommon.INCOMPLETE_BLOCK;

import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.reasm.m68k.parseerrors.ElseOrElseIfAfterElseParseError;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;
import ca.fragag.text.Document;

/**
 * Test class for {@link IfBlockParser}.
 *
 * @author Francis Gagné
 */
public class IfBlockParserTest {

    private static void parseIfBlock(String code, Matcher<Object> blockParseErrorMatcher, int numberOfChildNodes) {
        SourceNode node = Parser.parse(new Document(code));
        assertThat(node.getParseError(), is(nullValue()));
        List<SourceNode> childNodes = ((CompositeSourceNode) node).getChildNodes();
        assertThat(childNodes.size(), is(1));

        node = childNodes.get(0);
        assertThat(node.getParseError(), blockParseErrorMatcher);
        assertThat(node, hasType(IfBlock.class));

        childNodes = ((CompositeSourceNode) node).getChildNodes();
        assertThat(childNodes.size(), is(numberOfChildNodes));
        for (int i = 0; i < childNodes.size(); i++) {
            final SourceNode childNode = childNodes.get(i);
            assertThat(childNode.getParseError(), is(nullValue()));
            assertThat(childNode, hasType((i & 1) == 0 ? BlockDirectiveLine.class : SimpleCompositeSourceNode.class));
        }
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>IF</code> block that ends with an <code>ENDIF</code> directive.
     */
    @Test
    public void parseCompleteIfBlockEndc() {
        parseIfBlock(" IF\n NOP\n ENDC", COMPLETE_BLOCK, 3);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>IF</code> block that ends with an <code>ENDC</code> directive.
     */
    @Test
    public void parseCompleteIfBlockEndif() {
        parseIfBlock(" IF\n NOP\n ENDIF", COMPLETE_BLOCK, 3);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>IF</code> block with an <code>ELSE</code> clause.
     */
    @Test
    public void parseCompleteIfBlockWithElseClause() {
        parseIfBlock(" IF\n NOP\n ELSE\n RTS\n ENDIF", COMPLETE_BLOCK, 5);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>IF</code> block with an <code>ELSE</code> clause and an <code>ELSE</code> clause.
     */
    @Test
    public void parseCompleteIfBlockWithElseClauseAfterElseClause() {
        final Matcher<Object> blockParseErrorMatcher = both(hasType(ElseOrElseIfAfterElseParseError.class)).and(
                hasProperty("text", startsWith("ELSE ")));
        parseIfBlock(" IF\n NOP\n ELSE\n RTS\n ELSE\n RTS\n ENDIF", blockParseErrorMatcher, 7);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>IF</code> block with an <code>ELSEIF</code> clause and an <code>ELSE</code> clause.
     */
    @Test
    public void parseCompleteIfBlockWithElseifAndElseClause() {
        parseIfBlock(" IF\n NOP\n ELSEIF\n RTE\n ELSE\n RTS\n ENDIF", COMPLETE_BLOCK, 7);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>IF</code> block with an <code>ELSEIF</code> clause.
     */
    @Test
    public void parseCompleteIfBlockWithElseifClause() {
        parseIfBlock(" IF\n NOP\n ELSEIF\n RTE\n ENDIF", COMPLETE_BLOCK, 5);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>IF</code> block with an <code>ELSEIF</code> clause and an <code>ELSE</code> clause.
     */
    @Test
    public void parseCompleteIfBlockWithElseifClauseAfterElseClause() {
        final Matcher<Object> blockParseErrorMatcher = both(hasType(ElseOrElseIfAfterElseParseError.class)).and(
                hasProperty("text", startsWith("ELSEIF ")));
        parseIfBlock(" IF\n NOP\n ELSE\n RTS\n ELSEIF\n RTE\n ENDIF", blockParseErrorMatcher, 7);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>IF</code> block with an <code>ELSEIF</code> clause and an <code>ELSE</code> clause.
     */
    @Test
    public void parseCompleteIfBlockWithTwoElseifClausesAfterElseClause() {
        final Matcher<Object> blockParseErrorMatcher = both(hasType(ElseOrElseIfAfterElseParseError.class)).and(
                hasProperty("text", startsWith("ELSEIF ")));
        parseIfBlock(" IF\n NOP\n ELSE\n RTS\n ELSEIF\n RTE\n ELSEIF\n RTE\n ENDIF", blockParseErrorMatcher, 9);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>IF</code> block.
     */
    @Test
    public void parseIfBlockLineWithNoMnemonic() {
        parseIfBlock(" IF\nfoo:\n ENDIF", COMPLETE_BLOCK, 3);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>IF</code> block.
     */
    @Test
    public void parseIncompleteIfBlock() {
        parseIfBlock(" IF\n NOP", INCOMPLETE_BLOCK, 2);
    }

    /**
     * Asserts that {@link IfBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>IF</code> block with an <code>ELSE</code> clause.
     */
    @Test
    public void parseIncompleteIfBlockWithElseClause() {
        parseIfBlock(" IF\n NOP\n ELSE\n RTS", INCOMPLETE_BLOCK, 4);
    }

}
