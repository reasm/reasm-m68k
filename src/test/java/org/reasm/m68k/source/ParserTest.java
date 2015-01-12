package org.reasm.m68k.source;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.SimpleCompositeSourceNode;
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

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} reparses a document.
     */
    @Test
    public void reparseDelete() {
        final String oldText = " NOP\n IF 1\n MOVE #123,D0\n ENDIF\n NOP";
        final int replaceOffset = 18;
        final int lengthToRemove = 3;
        final String textToInsert = "";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = Parser.parse(oldDocument);

        final Document newDocument = oldDocument.replace(replaceOffset, lengthToRemove, textToInsert);
        assertThat(newDocument.toString(), is(" NOP\n IF 1\n MOVE #,D0\n ENDIF\n NOP"));

        final SourceNode newNode = Parser.reparse(newDocument, oldNode, replaceOffset, lengthToRemove, textToInsert.length());
        assertThat(newNode.getLength(), is(33));
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} reparses a document.
     */
    @Test
    public void reparseInsert() {
        final String oldText = " NOP\n IF 1\n MOVE #,D0\n ENDIF\n NOP";
        final int replaceOffset = 18;
        final int lengthToRemove = 0;
        final String textToInsert = "123";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = Parser.parse(oldDocument);

        final Document newDocument = oldDocument.replace(replaceOffset, lengthToRemove, textToInsert);
        assertThat(newDocument.toString(), is(" NOP\n IF 1\n MOVE #123,D0\n ENDIF\n NOP"));

        final SourceNode newNode = Parser.reparse(newDocument, oldNode, replaceOffset, lengthToRemove, textToInsert.length());
        assertThat(newNode.getLength(), is(36));
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} throws an {@link IllegalArgumentException} when the
     * length of the new document doesn't match the old root source node and the replacement.
     */
    @Test
    public void reparseNonsensical() {
        final String oldText = " NOP\n IF 1\n MOVE #0,D0\n ENDIF\n NOP";
        final int replaceOffset = 18;
        final int lengthToRemove = 1;
        final String textToInsert = "123";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = Parser.parse(oldDocument);

        final Document newDocument = new Document(" NOP\n IF 1\n MOVE #1234,D0\n ENDIF\n NOP");

        try {
            Parser.reparse(newDocument, oldNode, replaceOffset, lengthToRemove, textToInsert.length());
            fail("Parser.reparse() should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} returns the old root source node when the arguments
     * describe no replacement.
     */
    @Test
    public void reparseNoReplacement() {
        final String oldText = " NOP\n IF 1\n MOVE #0,D0\n ENDIF\n NOP";
        final int replaceOffset = 18;
        final int lengthToRemove = 0;
        final String textToInsert = "";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = Parser.parse(oldDocument);

        final Document newDocument = oldDocument.replace(replaceOffset, lengthToRemove, textToInsert);
        assertThat(newDocument.toString(), is(oldText));

        final SourceNode newNode = Parser.reparse(newDocument, oldNode, replaceOffset, lengthToRemove, textToInsert.length());
        assertThat(newNode, is(sameInstance(oldNode)));
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} throws a {@link NullPointerException} when the
     * <code>oldSourceFileRootNode</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void reparseNullOldSourceFileRootNode() {
        Parser.reparse(new Document("new"), null, 0, 0, 3);
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} throws a {@link NullPointerException} when the
     * <code>text</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void reparseNullText() {
        Parser.reparse(null, new SimpleCompositeSourceNode(Collections.<SourceNode> emptySet()), 0, 0, 3);
    }

    /**
     * Asserts that {@link Parser#reparse(Document, SourceNode, int, int, int)} reparses a document.
     */
    @Test
    public void reparseReplace() {
        final String oldText = " NOP\n IF 1\n MOVE #0,D0\n ENDIF\n NOP";
        final int replaceOffset = 18;
        final int lengthToRemove = 1;
        final String textToInsert = "123";

        final Document oldDocument = new Document(oldText);
        final SourceNode oldNode = Parser.parse(oldDocument);

        final Document newDocument = oldDocument.replace(replaceOffset, lengthToRemove, textToInsert);
        assertThat(newDocument.toString(), is(" NOP\n IF 1\n MOVE #123,D0\n ENDIF\n NOP"));

        final SourceNode newNode = Parser.reparse(newDocument, oldNode, replaceOffset, lengthToRemove, textToInsert.length());
        assertThat(newNode.getLength(), is(36));
    }

}
