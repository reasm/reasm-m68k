package org.reasm.m68k.source;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.SourceNode;

/**
 * The body of a <code>REPT</code> block.
 *
 * @author Francis Gagné
 */
@Immutable
public final class ReptBody extends CompositeSourceNode {

    /**
     * Initializes a new ReptBody.
     *
     * @param childNodes
     *            the child nodes
     */
    public ReptBody(@Nonnull Iterable<? extends SourceNode> childNodes) {
        super(childNodes, null);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        SourceNodesImpl.assembleReptBody(builder);
    }

}
