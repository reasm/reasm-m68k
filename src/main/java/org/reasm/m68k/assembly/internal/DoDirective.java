package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>DO</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class DoDirective extends Mnemonic {

    @Nonnull
    static final DoDirective DO = new DoDirective();

    private DoDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.requireNumberOfOperands(0);
        context.sizeNotAllowed();
    }

}
