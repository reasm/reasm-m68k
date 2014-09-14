package org.reasm.m68k.source;

import java.io.IOException;

import org.reasm.AssemblyBuilder;
import org.reasm.source.SourceNode;

final class ImplicitExitTransformationBlockNode extends SourceNode {

    static final ImplicitExitTransformationBlockNode INSTANCE = new ImplicitExitTransformationBlockNode();

    private ImplicitExitTransformationBlockNode() {
        super(0, null);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        builder.exitTransformationBlock();
    }

}
