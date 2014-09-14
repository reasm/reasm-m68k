package org.reasm.m68k.source;

import org.reasm.AssemblyBuilder;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;
import org.reasm.source.SourceNode;

final class ImplicitNextNode extends SourceNode {

    static final ImplicitNextNode INSTANCE = new ImplicitNextNode();

    private ImplicitNextNode() {
        super(0, null);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) {
        SourceNodesImpl.assembleImplicitNext(builder);
    }

}
