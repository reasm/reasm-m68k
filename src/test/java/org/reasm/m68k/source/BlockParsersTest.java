package org.reasm.m68k.source;

import static ca.fragag.testhelpers.HasType.hasType;
import static org.reasm.m68k.source.BlockParserTestsCommon.COMPLETE_BLOCK;
import static org.reasm.m68k.source.BlockParserTestsCommon.INCOMPLETE_BLOCK;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

/**
 * Test class for {@link BlockParsers}.
 *
 * @author Francis Gagné
 */
public class BlockParsersTest {

    private static void parseBasicBlock(@Nonnull String code, @Nonnull Class<?> blockType, @Nonnull Class<?> bodyType,
            @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        BlockParserTestsCommon.parseBasicBlock(code, blockType, bodyType, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseDoBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, DoBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    private static void parseForBlock(@Nonnull String code, @Nonnull Matcher<? super ParseError> blockParseErrorMatcher,
            @CheckForNull Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, ForBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
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
     * Asserts that {@link M68KParser} correctly parses a complete <code>DO</code> block.
     */
    @Test
    public void parseCompleteDoBlock() {
        parseDoBlock(" DO\n NOP\n UNTIL", COMPLETE_BLOCK, hasType(M68KBlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link M68KParser} correctly parses a complete <code>FOR</code> block.
     */
    @Test
    public void parseCompleteForBlock() {
        parseForBlock(" FOR\n NOP\n NEXT", COMPLETE_BLOCK, hasType(M68KBlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link M68KParser} correctly parses a complete <code>MACRO</code> block.
     */
    @Test
    public void parseCompleteMacroBlock() {
        parseMacroBlock(" MACRO\n NOP\n ENDM", COMPLETE_BLOCK, hasType(M68KBlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link M68KParser} correctly parses a complete <code>NAMESPACE</code> block.
     */
    @Test
    public void parseCompleteNamespaceBlock() {
        parseNamespaceBlock(" NAMESPACE\n NOP\n ENDNS", COMPLETE_BLOCK, hasType(M68KBlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link M68KParser} correctly parses a complete <code>REPT</code> block.
     */
    @Test
    public void parseCompleteReptBlock() {
        parseReptBlock(" REPT\n NOP\n ENDR", COMPLETE_BLOCK, hasType(M68KBlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link M68KParser} correctly parses a complete <code>TRANSFORM</code> block.
     */
    @Test
    public void parseCompleteTransformBlock() {
        parseTransformBlock(" TRANSFORM\n NOP\n ENDTRANSFORM", COMPLETE_BLOCK, hasType(M68KBlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link M68KParser} correctly parses a complete <code>WHILE</code> block.
     */
    @Test
    public void parseCompleteWhileBlock() {
        parseWhileBlock(" WHILE\n NOP\n ENDW", COMPLETE_BLOCK, hasType(M68KBlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link M68KParser} correctly parses an incomplete <code>DO</code> block.
     */
    @Test
    public void parseIncompleteDOBlock() {
        parseDoBlock(" DO\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link M68KParser} correctly parses an incomplete <code>FOR</code> block.
     */
    @Test
    public void parseIncompleteForBlock() {
        parseForBlock(" FOR\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link M68KParser} correctly parses an incomplete <code>MACRO</code> block.
     */
    @Test
    public void parseIncompleteMacroBlock() {
        parseMacroBlock(" MACRO\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link M68KParser} correctly parses an incomplete <code>NAMESPACE</code> block.
     */
    @Test
    public void parseIncompleteNamespaceBlock() {
        parseNamespaceBlock(" NAMESPACE\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link M68KParser} correctly parses an incomplete <code>REPT</code> block.
     */
    @Test
    public void parseIncompleteReptBlock() {
        parseReptBlock(" REPT\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link M68KParser} correctly parses an incomplete <code>TRANSFORM</code> block.
     */
    @Test
    public void parseIncompleteTransformBlock() {
        parseTransformBlock(" TRANSFORM\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link M68KParser} correctly parses an incomplete <code>WHILE</code> block.
     */
    @Test
    public void parseIncompleteWhileBlock() {
        parseWhileBlock(" WHILE\n NOP", INCOMPLETE_BLOCK, null);
    }

}
