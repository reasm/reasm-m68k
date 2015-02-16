package org.reasm.m68k.source;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.reasm.commons.source.Block;
import org.reasm.commons.source.Parser;
import org.reasm.commons.source.Syntax;
import org.reasm.source.SourceNode;

import ca.fragag.text.Document;

/**
 * Test class for {@link M68KParser}.
 *
 * @author Francis Gagné
 */
public class M68KParserTest {

    /**
     * Asserts that {@link Syntax#isValidIdentifierCodePoint(int)} returns <code>true</code> for code points that are valid as part
     * of an identifier and <code>false</code> for other code points.
     */
    @Test
    public void isValidIdentifierCodePoint() {
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint(0), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('\t'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('\n'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint(0xB), is(true)); // LINE TABULATION
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('\f'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('\r'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint(' '), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('!'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('"'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('#'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('$'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('\''), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('.'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('0'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('@'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('A'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('\\'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('`'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint('a'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierCodePoint(-1), is(false));
    }

    /**
     * Asserts that {@link Syntax#isValidIdentifierInitialCodePoint(int)} returns <code>true</code> for code points that are valid
     * as the first code point of an identifier and <code>false</code> for other code points.
     */
    @Test
    public void isValidIdentifierInitialCodePoint() {
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint(0), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('\t'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('\n'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint(0xB), is(true)); // LINE TABULATION
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('\f'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('\r'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint(' '), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('!'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('"'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('#'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('$'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('\''), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('.'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('0'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('@'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('A'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('\\'), is(false));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('`'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint('a'), is(true));
        assertThat(M68KParser.SYNTAX.isValidIdentifierInitialCodePoint(-1), is(false));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} on {@link M68KParser#INSTANCE} returns a {@link Block} with a single
     * {@link WhileBlock} child node when the document contains a <code>WHILE</code> block, even if the <code>WHILE</code> directive
     * has a size attribute.
     */
    @Test
    public void parseBlockSizeAttribute() {
        final SourceNode block = M68KParser.INSTANCE.parse(new Document(" WHILE.W 1\n NOP\n ENDW"));
        assertThat(block.getLength(), is(21));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        final List<SourceNode> childNodes = ((Block) block).getChildNodes();
        assertThat(childNodes.size(), is(1));
        final SourceNode node = childNodes.get(0);
        assertThat(node.getLength(), is(21));
        assertThat(node.getParseError(), is(nullValue()));
        assertThat(node, is(instanceOf(WhileBlock.class)));
    }

}
