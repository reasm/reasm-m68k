package org.reasm.m68k.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;
import org.reasm.source.SourceNode;

@Immutable
final class ImplicitNextNode extends SourceNode {

    @Nonnull
    static final ImplicitNextNode INSTANCE = new ImplicitNextNode();

    private ImplicitNextNode() {
        super(0, null);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) {
        SourceNodesImpl.assembleImplicitNext(builder);
    }

}
