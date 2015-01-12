package org.reasm.m68k.source;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.source.SourceNode;

import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;

/**
 * The parser for M68000 family assembler source files.
 *
 * @author Francis Gagné
 */
public final class Parser {

    @Nonnull
    static final Map<BlockDirective, BlockParser> BLOCKS = new TreeMap<>();

    static {
        BLOCKS.put(BlockDirective.DO, BasicBlockParser.DO);
        BLOCKS.put(BlockDirective.FOR, BasicBlockParser.FOR);
        BLOCKS.put(BlockDirective.IF, IfBlockParser.IF);
        BLOCKS.put(BlockDirective.MACRO, BasicBlockParser.MACRO);
        BLOCKS.put(BlockDirective.NAMESPACE, BasicBlockParser.NAMESPACE);
        BLOCKS.put(BlockDirective.OBJ, ObjBlockParser.OBJ);
        BLOCKS.put(BlockDirective.PHASE, ObjBlockParser.PHASE);
        BLOCKS.put(BlockDirective.REPT, BasicBlockParser.REPT);
        BLOCKS.put(BlockDirective.TRANSFORM, BasicBlockParser.TRANSFORM);
        BLOCKS.put(BlockDirective.WHILE, BasicBlockParser.WHILE);
    }

    /**
     * Determines whether the specified code point represents whitespace or not.
     *
     * @param codePoint
     *            the code point to test
     * @return <code>true</code> if the code point represents whitespace, otherwise <code>false</code>
     */
    public static boolean isWhitespace(int codePoint) {
        switch (codePoint) {
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
            return true;
        }

        return false;
    }

    /**
     * Parses the contents of a source file.
     *
     * @param text
     *            the contents of the source file
     * @return a {@link SourceNode} that is the root of the source file's abstract syntax tree
     */
    @Nonnull
    public static SourceNode parse(@Nonnull Document text) {
        return parse(new SourceNodeProducer(new DocumentReader(text)));
    }

    /**
     * Re-parses the contents of a source file after it has been altered.
     *
     * @param text
     *            the new contents of the source file
     * @param oldSourceFileRootNode
     *            the root source node of the old source
     * @param replaceOffset
     *            the offset at which the replace occurred
     * @param lengthToRemove
     *            the length of text from the old source file that was removed
     * @param lengthToInsert
     *            the length of text from the new source file that was inserted
     * @return a {@link SourceNode} that is the root of the source file's abstract syntax tree
     */
    @Nonnull
    public static SourceNode reparse(@Nonnull Document text, @Nonnull SourceNode oldSourceFileRootNode, int replaceOffset,
            int lengthToRemove, int lengthToInsert) {
        // TODO Implement incremental re-parsing
        return parse(text);
    }

    static SourceNode parse(@Nonnull SourceNodeProducer sourceNodeProducer) {
        final ArrayList<SourceNode> nodes = new ArrayList<>();
        while (!sourceNodeProducer.atEnd()) {
            final SourceNode sourceNode = sourceNodeProducer.next();

            // Get the block directive on this logical line, if any.
            final BlockDirective blockDirective = BlockDirective.getBlockDirective(sourceNode);

            processBlockBodyLine(sourceNodeProducer, nodes, sourceNode, blockDirective);
        }

        return new Block(nodes, null);
    }

    static void processBlockBodyLine(@Nonnull SourceNodeProducer sourceNodeProducer, @Nonnull ArrayList<SourceNode> childNodes,
            @Nonnull SourceNode sourceNode, @CheckForNull BlockDirective blockDirective) {
        // Check if this mnemonic starts a block.
        final BlockParser blockParser = blockDirective == null ? null : BLOCKS.get(blockDirective);

        // If the mnemonic doesn't start a block, add the logical line to the child nodes list.
        // Otherwise, parse a block.
        if (blockParser == null) {
            childNodes.add(sourceNode);
        } else {
            childNodes.add(blockParser.parseBlock(sourceNodeProducer, (BlockDirectiveLine) sourceNode, blockDirective));
        }
    }

    // This class is not meant to be instantiated.
    private Parser() {
    }

}
