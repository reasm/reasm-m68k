package org.reasm.m68k.source;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

/**
 * An <code>OBJ</code> or <code>PHASE</code> block.
 *
 * @author Francis Gagné
 */
@Immutable
public final class ObjBlock extends CompositeSourceNode {

    /**
     * Initializes a new ObjBlock.
     *
     * @param childNodes
     *            the child nodes
     * @param parseError
     *            the parse error on the source node, or <code>null</code> if no parse error occurred
     */
    public ObjBlock(@Nonnull Iterable<? extends SourceNode> childNodes, @CheckForNull ParseError parseError) {
        super(childNodes, parseError);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        SourceNodesImpl.assembleObjBlock(builder);
    }

}
