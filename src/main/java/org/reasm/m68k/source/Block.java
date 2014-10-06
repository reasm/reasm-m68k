package org.reasm.m68k.source;

import org.reasm.AssemblyBuilder;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

/**
 * A simple block.
 *
 * @author Francis Gagné
 */
public final class Block extends CompositeSourceNode {

    /**
     * Initializes a new Block.
     *
     * @param childNodes
     *            the block's child nodes
     * @param parseError
     *            the parse error on the block
     */
    public Block(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
        super(childNodes, parseError);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) {
        builder.enterComposite(true, null);
    }

}
