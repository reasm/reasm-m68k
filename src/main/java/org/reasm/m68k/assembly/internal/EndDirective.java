package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>END</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class EndDirective extends Mnemonic {

    @Nonnull
    static final EndDirective END = new EndDirective();

    private EndDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);
        context.builder.endPass();
    }

}
