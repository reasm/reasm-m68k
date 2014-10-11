package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.m68k.messages.EndtransformWithoutTransformErrorMessage;
import org.reasm.m68k.source.TransformBlock;

final class EndtransformDirective extends Mnemonic {

    static final EndtransformDirective ENDTRANSFORM = new EndtransformDirective();

    private EndtransformDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        if (!(context.getParentNode() instanceof TransformBlock)) {
            context.addMessage(new EndtransformWithoutTransformErrorMessage());
        }
    }

}
