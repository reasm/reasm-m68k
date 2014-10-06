package org.reasm.m68k.source;

import static ca.fragag.testhelpers.HasType.hasType;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.reasm.m68k.source.BlockParserTestsCommon.COMPLETE_BLOCK;
import static org.reasm.m68k.source.BlockParserTestsCommon.INCOMPLETE_BLOCK;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.reasm.AssemblyBuilder;
import org.reasm.SubstringBounds;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;
import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;

/**
 * Test class for {@link BasicBlockParser}.
 *
 * @author Francis Gagné
 */
public class BasicBlockParserTest {

    static final SourceNode MISSING_END_DIRECTIVE = new SourceNode(0, null) {
        @Override
        protected void assembleCore(AssemblyBuilder builder) {
            fail();
        }
    };

    private static final BasicBlockParser TEST_BLOCK_PARSER = new BasicBlockParser("ENDBLOCK") {
        @Override
        void missingEndDirective(ArrayList<SourceNode> nodes) {
            nodes.add(MISSING_END_DIRECTIVE);
        };
    };

    private static void parseBasicBlock(String code, Class<?> blockType, Class<?> bodyType,
            Matcher<? super ParseError> blockParseErrorMatcher, Matcher<? super SourceNode> thirdChildNodeMatcher) {
        BlockParserTestsCommon.parseBasicBlock(code, blockType, bodyType, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static List<SourceNode> parseBlock(String code, Matcher<? super ParseError> blockParseErrorMatcher) {
        final DocumentReader reader = new DocumentReader(new Document(code), 7);
        final LogicalLine firstLine = new LogicalLine(7, null, new SubstringBounds[0], new SubstringBounds(1, 6),
                new SubstringBounds[0], null, new int[0]);

        final SourceNode block = TEST_BLOCK_PARSER.parseBlock(reader, firstLine, "BLOCK");
        assertThat(block.getLength(), is(code.length()));
        assertThat(block.getParseError(), blockParseErrorMatcher);
        assertThat(block, hasType(Block.class));

        final List<SourceNode> childNodes = ((CompositeSourceNode) block).getChildNodes();
        assertThat(childNodes.size(), is(3));

        final SourceNode blockStart = childNodes.get(0);
        assertThat(blockStart.getLength(), is(7));
        assertThat(blockStart.getParseError(), is(nullValue()));
        assertThat(blockStart, hasType(BlockDirectiveLine.class));
        assertThat(((BlockDirectiveLine) blockStart).getLogicalLine(), is(firstLine));

        final SourceNode blockBody = childNodes.get(1);
        assertThat(blockBody, hasType(SimpleCompositeSourceNode.class));

        return childNodes;
    }

    private static CompositeSourceNode parseCompleteBlock(String code) {
        final List<SourceNode> childNodes = parseBlock(code, COMPLETE_BLOCK);

        final SourceNode blockEnd = childNodes.get(2);
        assertThat(blockEnd.getLength(), is(9));
        assertThat(blockEnd.getParseError(), is(nullValue()));
        assertThat(blockEnd, hasType(BlockDirectiveLine.class));

        return (CompositeSourceNode) childNodes.get(1);
    }

    private static void parseForBlock(String code, Matcher<? super ParseError> blockParseErrorMatcher,
            Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, ForBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static CompositeSourceNode parseIncompleteBlock(String code) {
        final Matcher<Object> blockParseErrorMatcher = both(INCOMPLETE_BLOCK).and(
                hasProperty("startingDirective", equalTo("BLOCK")));
        final List<SourceNode> childNodes = parseBlock(code, blockParseErrorMatcher);

        final SourceNode blockEnd = childNodes.get(2);
        assertThat(blockEnd.getLength(), is(0));
        assertThat(blockEnd.getParseError(), is(nullValue()));
        assertThat(blockEnd, is(sameInstance(MISSING_END_DIRECTIVE)));

        return (CompositeSourceNode) childNodes.get(1);
    }

    private static void parseMacroBlock(String code, Matcher<? super ParseError> blockParseErrorMatcher,
            Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, MacroBlock.class, MacroBody.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseNamespaceBlock(String code, Matcher<? super ParseError> blockParseErrorMatcher,
            Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, NamespaceBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseReptBlock(String code, Matcher<? super ParseError> blockParseErrorMatcher,
            Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, ReptBlock.class, ReptBody.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseTransformBlock(String code, Matcher<? super ParseError> blockParseErrorMatcher,
            Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, TransformBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseWhileBlock(String code, Matcher<? super ParseError> blockParseErrorMatcher,
            Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, WhileBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a block with an
     * empty body.
     */
    @Test
    public void parseBlockEmptyBody() {
        final String code = " BLOCK\n ENDBLOCK";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(0));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a block with a
     * logical line in its body that has no mnemonic.
     */
    @Test
    public void parseBlockLineWithNoMnemonic() {
        final String code = " BLOCK\nfoo:\n ENDBLOCK";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(5));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(LogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a block with the
     * end directive missing.
     */
    @Test
    public void parseBlockMissingEndDirective() {
        final String code = " BLOCK\n NOP";
        final CompositeSourceNode body = parseIncompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(4));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(LogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a block with a
     * nested block in its body.
     */
    @Test
    public void parseBlockNestedBlock() {
        final String code = " BLOCK\n WHILE\n NOP\n ENDW\n ENDBLOCK";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(18));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(WhileBlock.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a block with 1
     * logical line in its body.
     */
    @Test
    public void parseBlockOneLineBody() {
        final String code = " BLOCK\n NOP\n ENDBLOCK";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(5));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(LogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a block with 2
     * logical lines in its body.
     */
    @Test
    public void parseBlockTwoLineBody() {
        final String code = " BLOCK\n NOP\n ILLEGAL\n ENDBLOCK";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(2));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(5));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(LogicalLine.class));

        final SourceNode bodyNode1 = bodyNodes.get(1);
        assertThat(bodyNode1.getLength(), is(9));
        assertThat(bodyNode1.getParseError(), is(nullValue()));
        assertThat(bodyNode1, hasType(LogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>FOR</code> block.
     */
    @Test
    public void parseCompleteForBlock() {
        parseForBlock(" FOR\n NOP\n NEXT", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>MACRO</code> block.
     */
    @Test
    public void parseCompleteMacroBlock() {
        parseMacroBlock(" MACRO\n NOP\n ENDM", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>NAMESPACE</code> block.
     */
    @Test
    public void parseCompleteNamespaceBlock() {
        parseNamespaceBlock(" NAMESPACE\n NOP\n ENDNS", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>REPT</code> block.
     */
    @Test
    public void parseCompleteReptBlock() {
        parseReptBlock(" REPT\n NOP\n ENDR", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>TRANSFORM</code> block.
     */
    @Test
    public void parseCompleteTransformBlock() {
        parseTransformBlock(" TRANSFORM\n NOP\n ENDTRANSFORM", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>WHILE</code> block.
     */
    @Test
    public void parseCompleteWhileBlock() {
        parseWhileBlock(" WHILE\n NOP\n ENDW", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>FOR</code> block.
     */
    @Test
    public void parseIncompleteForBlock() {
        parseForBlock(" FOR\n NOP", INCOMPLETE_BLOCK, is(sameInstance((SourceNode) ImplicitNextNode.INSTANCE)));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>MACRO</code> block.
     */
    @Test
    public void parseIncompleteMacroBlock() {
        parseMacroBlock(" MACRO\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>NAMESPACE</code> block.
     */
    @Test
    public void parseIncompleteNamespaceBlock() {
        parseNamespaceBlock(" NAMESPACE\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>REPT</code> block.
     */
    @Test
    public void parseIncompleteReptBlock() {
        parseReptBlock(" REPT\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>TRANSFORM</code> block.
     */
    @Test
    public void parseIncompleteTransformBlock() {
        parseTransformBlock(" TRANSFORM\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>WHILE</code> block.
     */
    @Test
    public void parseIncompleteWhileBlock() {
        parseWhileBlock(" WHILE\n NOP", INCOMPLETE_BLOCK, null);
    }

}
