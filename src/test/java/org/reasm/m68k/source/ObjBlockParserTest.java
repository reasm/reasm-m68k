package org.reasm.m68k.source;

import static ca.fragag.testhelpers.HasType.hasType;
import static org.reasm.m68k.source.BlockParserTestsCommon.COMPLETE_BLOCK;
import static org.reasm.m68k.source.BlockParserTestsCommon.INCOMPLETE_BLOCK;
import static org.reasm.m68k.source.BlockParserTestsCommon.parseBasicBlock;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;

/**
 * Test class for {@link ObjBlockParser}.
 *
 * @author Francis Gagné
 */
public class ObjBlockParserTest {

    private static void parseObjBlock(String code, Matcher<? super ParseError> blockParseErrorMatcher,
            Matcher<? super SourceNode> thirdChildNodeMatcher) {
        parseBasicBlock(code, ObjBlock.class, SimpleCompositeSourceNode.class, blockParseErrorMatcher, thirdChildNodeMatcher);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>OBJ</code> block.
     */
    @Test
    public void parseCompleteObjBlock() {
        parseObjBlock(" OBJ\n NOP\n OBJEND", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses a complete
     * <code>PHASE</code> block.
     */
    @Test
    public void parseCompletePhaseBlock() {
        parseObjBlock(" PHASE\n NOP\n DEPHASE", COMPLETE_BLOCK, hasType(BlockDirectiveLine.class));
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>OBJ</code> block.
     */
    @Test
    public void parseIncompleteObjBlock() {
        parseObjBlock(" OBJ\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that {@link BasicBlockParser#parseBlock(CharSequenceReader, LogicalLine, String)} correctly parses an incomplete
     * <code>PHASE</code> block.
     */
    @Test
    public void parseIncompletePhaseBlock() {
        parseObjBlock(" PHASE\n NOP", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that an <code>OBJ</code> block cannot be closed with the <code>DEPHASE</code> directive.
     */
    @Test
    public void parseObjDephaseBlock() {
        parseObjBlock(" OBJ\n NOP\n DEPHASE", INCOMPLETE_BLOCK, null);
    }

    /**
     * Asserts that a <code>PHASE</code> block cannot be closed with the <code>OBJEND</code> directive.
     */
    @Test
    public void parsePhaseObjendBlock() {
        parseObjBlock(" PHASE\n NOP\n OBJEND", INCOMPLETE_BLOCK, null);
    }

}
