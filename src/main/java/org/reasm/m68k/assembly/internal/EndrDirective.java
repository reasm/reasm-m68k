package org.reasm.m68k.assembly.internal;

import org.reasm.m68k.messages.EndrWithoutReptErrorMessage;

/**
 * The <code>ENDR</code> directive.
 *
 * @author Francis Gagné
 */
class EndrDirective extends Mnemonic {

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
