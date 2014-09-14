package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * The <code>DO</code> directive.
 *
 * @author Francis Gagné
 */
class DoDirective extends Mnemonic {

    static final DoDirective DO = new DoDirective();

    private DoDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.requireNumberOfOperands(0);
        context.sizeNotAllowed();
    }

}
