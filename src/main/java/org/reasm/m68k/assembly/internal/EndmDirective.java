package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.messages.EndmWithoutMacroErrorMessage;

/**
 * The <code>ENDM</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class EndmDirective extends Mnemonic {

    @Nonnull
    static final EndmDirective ENDM = new EndmDirective();

    private EndmDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        if (!(context.getParentBlock() instanceof MacroBlockState)) {
            context.addMessage(new EndmWithoutMacroErrorMessage());
        }
    }

}
