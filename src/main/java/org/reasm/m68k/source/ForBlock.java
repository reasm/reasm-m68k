package org.reasm.m68k.source;

import org.reasm.AssemblyBuilder;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

/**
 * A <code>FOR</code> block.
 *
 * @author Francis Gagné
 */
public final class ForBlock extends CompositeSourceNode {

    /**
     * Initializes a new ForBlock.
     *
     * @param childNodes
     *            the child nodes
     * @param parseError
     *            the parse error on the source node, or <code>null</code> if no parse error occurred
     */
    public ForBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
        super(childNodes, parseError);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) {
        SourceNodesImpl.assembleForBlock(builder);
    }

}
