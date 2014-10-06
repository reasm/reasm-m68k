package org.reasm.m68k.source;

import java.io.IOException;

import org.reasm.AssemblyBuilder;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

/**
 * A <code>NAMESPACE</code> block.
 *
 * @author Francis Gagné
 */
public final class NamespaceBlock extends CompositeSourceNode {

    /**
     * Initializes a new NamespaceBlock.
     *
     * @param childNodes
     *            the child nodes
     * @param parseError
     *            the parse error on the source node, or <code>null</code> if no parse error occurred
     */
    public NamespaceBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
        super(childNodes, parseError);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        SourceNodesImpl.assembleNamespaceBlock(builder);
    }

}
