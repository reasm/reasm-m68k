package org.reasm.m68k.source;

import static ca.fragag.testhelpers.HasType.hasType;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.reasm.m68k.source.BlockParserTestsCommon.COMPLETE_BLOCK;
import static org.reasm.m68k.source.BlockParserTestsCommon.INCOMPLETE_BLOCK;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.reasm.SubstringBounds;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;

/**
 * Test class for {@link BasicBlockParser}.
 *
 * @author Francis Gagné
 */
public class BasicBlockParserTest {

    private static void parseBasicBlock(@Nonnull String code, @Nonnull Class<?> blockType, @Nonnull Class<?> bodyType,
            @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        BlockParserTestsCommon.parseBasicBlock(code, blockType, bodyType, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static List<SourceNode> parseBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            int childrenCount) {
        final DocumentReader reader = new DocumentReader(new Document(code), 5);
        final BlockDirectiveLine firstLine = new BlockDirectiveLine(new LogicalLine(5, null, new SubstringBounds[0],
                new SubstringBounds(1, 4), new SubstringBounds[0], null, new int[0]), BlockDirective.FOR);

        final SourceNode block = BasicBlockParser.FOR.parseBlock(new SourceNodeProducer(reader), firstLine, BlockDirective.FOR);
        assertThat(block.getLength(), is(code.length()));
        assertThat(block.getParseError(), blockParseErrorMatcher);
        assertThat(block, hasType(ForBlock.class));

        final List<SourceNode> childNodes = ((CompositeSourceNode) block).getChildNodes();
        assertThat(childNodes.size(), is(childrenCount));

        final SourceNode blockStart = childNodes.get(0);
        assertThat(blockStart, is((SourceNode) firstLine));

        final SourceNode blockBody = childNodes.get(1);
        assertThat(blockBody, hasType(SimpleCompositeSourceNode.class));

        return childNodes;
    }

    private static CompositeSourceNode parseCompleteBlock(@Nonnull String code) {
        final List<SourceNode> childNodes = parseBlock(code, COMPLETE_BLOCK, 3);

        final SourceNode blockEnd = childNodes.get(2);
        assertThat(blockEnd.getLength(), is(5));
        assertThat(blockEnd.getParseError(), is(nullValue()));
        assertThat(blockEnd, hasType(BlockDirectiveLine.class));

        return (CompositeSourceNode) childNodes.get(1);
    }

    private static void parseForBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, ForBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static CompositeSourceNode parseIncompleteBlock(@Nonnull String code) {
        final Matcher<Object> blockParseErrorMatcher = both(INCOMPLETE_BLOCK).and(
                hasProperty("startingBlockDirective", equalTo(BlockDirective.FOR)));
        final List<SourceNode> childNodes = parseBlock(code, blockParseErrorMatcher, 2);
        return (CompositeSourceNode) childNodes.get(1);
    }

    private static void parseMacroBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, MacroBlock.class, MacroBody.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseNamespaceBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, NamespaceBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseReptBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, ReptBlock.class, ReptBody.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseTransformBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, TransformBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseWhileBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, WhileBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * block with an empty body.
     */
    @Test
    public void parseBlockEmptyBody() {
        final String code = " FOR\n NEXT";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(0));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * block with a logical line in its body that has no mnemonic.
     */
    @Test
    public void parseBlockLineWithNoMnemonic() {
        final String code = " FOR\nfoo:\n NEXT";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(5));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(LogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * block with the end directive missing.
     */
    @Test
    public void parseBlockMissingEndDirective() {
        final String code = " FOR\n NOP";
        final CompositeSourceNode body = parseIncompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(4));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(LogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * block with a nested block in its body.
     */
    @Test
    public void parseBlockNestedBlock() {
        final String code = " FOR\n WHILE\n NOP\n ENDW\n NEXT";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(18));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(WhileBlock.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * block with 1 logical line in its body.
     */
    @Test
    public void parseBlockOneLineBody() {
        final String code = " FOR\n NOP\n NEXT";
        final CompositeSourceNode body = parseCompleteBlock(code);
        final List<SourceNode> bodyNodes = body.getChildNodes();
        assertThat(bodyNodes.size(), is(1));

        final SourceNode bodyNode0 = bodyNodes.get(0);
        assertThat(bodyNode0.getLength(), is(5));
        assertThat(bodyNode0.getParseError(), is(nullValue()));
        assertThat(bodyNode0, hasType(LogicalLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * block with 2 logical lines in its body.
     */
    @Test
    public void parseBlockTwoLineBody() {
        final String code = " FOR\n NOP\n ILLEGAL\n NEXT";
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
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * complete <code>FOR</code> block.
     */
    @Test
    public void parseCompleteForBlock() {
        parseForBlock(" FOR\n NOP\n NEXT", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * complete <code>MACRO</code> block.
     */
    @Test
    public void parseCompleteMacroBlock() {
        parseMacroBlock(" MACRO\n NOP\n ENDM", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * complete <code>NAMESPACE</code> block.
     */
    @Test
    public void parseCompleteNamespaceBlock() {
        parseNamespaceBlock(" NAMESPACE\n NOP\n ENDNS", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * complete <code>REPT</code> block.
     */
    @Test
    public void parseCompleteReptBlock() {
        parseReptBlock(" REPT\n NOP\n ENDR", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * complete <code>TRANSFORM</code> block.
     */
    @Test
    public void parseCompleteTransformBlock() {
        parseTransformBlock(" TRANSFORM\n NOP\n ENDTRANSFORM", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses a
     * complete <code>WHILE</code> block.
     */
    @Test
    public void parseCompleteWhileBlock() {
        parseWhileBlock(" WHILE\n NOP\n ENDW", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses an
     * incomplete <code>FOR</code> block.
     */
    @Test
    public void parseIncompleteForBlock() {
        parseForBlock(" FOR\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses an
     * incomplete <code>MACRO</code> block.
     */
    @Test
    public void parseIncompleteMacroBlock() {
        parseMacroBlock(" MACRO\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses an
     * incomplete <code>NAMESPACE</code> block.
     */
    @Test
    public void parseIncompleteNamespaceBlock() {
        parseNamespaceBlock(" NAMESPACE\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses an
     * incomplete <code>REPT</code> block.
     */
    @Test
    public void parseIncompleteReptBlock() {
        parseReptBlock(" REPT\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses an
     * incomplete <code>TRANSFORM</code> block.
     */
    @Test
    public void parseIncompleteTransformBlock() {
        parseTransformBlock(" TRANSFORM\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(SourceNodeProducer, BlockDirectiveLine, BlockDirective)} correctly parses an
     * incomplete <code>WHILE</code> block.
     */
    @Test
    public void parseIncompleteWhileBlock() {
        parseWhileBlock(" WHILE\n NOP", INCOMPLETE_BLOCK, null);
    }

}
