package org.reasm.m68k.source;

import org.reasm.AssemblyBuilder;
import org.reasm.source.SourceNode;

final class ImplicitExitNamespaceNode extends SourceNode {

    static final ImplicitExitNamespaceNode INSTANCE = new ImplicitExitNamespaceNode();

    private ImplicitExitNamespaceNode() {
        super(0, null);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) {
        builder.exitNamespace();
    }

}
