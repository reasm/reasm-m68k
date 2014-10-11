package org.reasm.m68k.assembly.internal;

import java.io.IOException;

final class EndtransformDirective extends Mnemonic {

    static final EndtransformDirective ENDTRANSFORM = new EndtransformDirective();

    private EndtransformDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);
    }

}
