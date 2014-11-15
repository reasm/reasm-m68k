package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.messages.EndrWithoutReptErrorMessage;

/**
 * The <code>ENDR</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class EndrDirective extends Mnemonic {

    @Nonnull
    static final EndrDirective ENDR = new EndrDirective();

    private EndrDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        if (!(context.getParentBlock() instanceof ReptBlockState)) {
            context.addMessage(new EndrWithoutReptErrorMessage());
        }
    }

}
