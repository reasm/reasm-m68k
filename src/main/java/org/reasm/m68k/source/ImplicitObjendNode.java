package org.reasm.m68k.source;

import java.io.IOException;

import org.reasm.AssemblyBuilder;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;
import org.reasm.source.SourceNode;

final class ImplicitObjendNode extends SourceNode {

    static final ImplicitObjendNode INSTANCE = new ImplicitObjendNode();

    private ImplicitObjendNode() {
        super(0, null);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        SourceNodesImpl.assembleImplicitObjend(builder);
    }

}
