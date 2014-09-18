package org.reasm.m68k.source;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.Document;

/**
 * Test class for {@link Parser}.
 *
 * @author Francis Gagné
 */
public class ParserTest {

    /**
     * Asserts that {@link Parser#isWhitespace(int)} returns <code>true</code> when the specified character is interpreted as
     * whitespace by the parser and <code>false</code> when it isn't.
     */
    @Test
    public void isWhitespace() {
        assertThat(Parser.isWhitespace(0), is(false));
        assertThat(Parser.isWhitespace('\t'), is(true));
        assertThat(Parser.isWhitespace('\n'), is(true));
        assertThat(Parser.isWhitespace('\f'), is(true));
        assertThat(Parser.isWhitespace('\r'), is(true));
        assertThat(Parser.isWhitespace(' '), is(true));
        assertThat(Parser.isWhitespace('0'), is(false));
        assertThat(Parser.isWhitespace('A'), is(false));
        assertThat(Parser.isWhitespace('\u00a0'), is(false));
        assertThat(Parser.isWhitespace('\u2000'), is(false));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with a single {@link WhileBlock} child node when the
     * document contains a <code>WHILE</code> block.
     */
    @Test
    public void parseBlock() {
        final SourceNode block = Parser.parse(new Document(" WHILE 1\n NOP\n ENDW"));
        assertThat(block.getLength(), is(19));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        final List<SourceNode> childNodes = ((Block) block).getChildNodes();
        assertThat(childNodes.size(), is(1));
        final SourceNode node = childNodes.get(0);
        assertThat(node.getLength(), is(19));
        assertThat(node.getParseError(), is(nullValue()));
        assertThat(node, is(instanceOf(WhileBlock.class)));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with a single {@link WhileBlock} child node when the
     * document contains a <code>WHILE</code> block, even if the <code>WHILE</code> directive is preceded by a <code>!</code>.
     */
    @Test
    public void parseBlockNoMacro() {
        final SourceNode block = Parser.parse(new Document(" !WHILE 1\n NOP\n ENDW"));
        assertThat(block.getLength(), is(20));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        final List<SourceNode> childNodes = ((Block) block).getChildNodes();
        assertThat(childNodes.size(), is(1));
        final SourceNode node = childNodes.get(0);
        assertThat(node.getLength(), is(20));
        assertThat(node.getParseError(), is(nullValue()));
        assertThat(node, is(instanceOf(WhileBlock.class)));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with a single {@link WhileBlock} child node when the
     * document contains a <code>WHILE</code> block, even if the <code>WHILE</code> directive has a size attribute.
     */
    @Test
    public void parseBlockSizeAttribute() {
        final SourceNode block = Parser.parse(new Document(" WHILE.W 1\n NOP\n ENDW"));
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

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with no child nodes when the document is empty.
     */
    @Test
    public void parseEmptyDocument() {
        final SourceNode block = Parser.parse(new Document(""));
        assertThat(block.getLength(), is(0));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        assertThat(((CompositeSourceNode) block).getChildNodes(), is(empty()));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with a single {@link LogicalLine} child node when the
     * document contains a single line with no mnemonic.
     */
    @Test
    public void parseNoMnemonic() {
        final SourceNode block = Parser.parse(new Document("; This is a comment"));
        assertThat(block.getLength(), is(19));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        final List<SourceNode> childNodes = ((Block) block).getChildNodes();
        assertThat(childNodes.size(), is(1));
        final SourceNode node = childNodes.get(0);
        assertThat(node.getLength(), is(19));
        assertThat(node.getParseError(), is(nullValue()));
        assertThat(node, is(instanceOf(LogicalLine.class)));
    }

    /**
     * Asserts that {@link Parser#parse(Document)} returns a {@link Block} with a single {@link LogicalLine} child node when the
     * document contains a single line with a mnemonic that doesn't start a block.
     */
    @Test
    public void parseNotABlock() {
        final SourceNode block = Parser.parse(new Document(" NOP"));
        assertThat(block.getLength(), is(4));
        assertThat(block.getParseError(), is(nullValue()));
        assertThat(block, is(instanceOf(Block.class)));
        final List<SourceNode> childNodes = ((Block) block).getChildNodes();
        assertThat(childNodes.size(), is(1));
        final SourceNode node = childNodes.get(0);
        assertThat(node.getLength(), is(4));
        assertThat(node.getParseError(), is(nullValue()));
        assertThat(node, is(instanceOf(LogicalLine.class)));
    }

}
