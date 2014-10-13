package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * The <code>EVEN</code> directive.
 *
 * @author Francis Gagné
 */
class EvenDirective extends Mnemonic {

    static final EvenDirective EVEN = new EvenDirective();

    private EvenDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        if ((context.programCounter & 1) != 0) {
            context.appendByte((byte) 0);
        }
    }

}
