package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.messages.EndwWithoutWhileErrorMessage;

/**
 * The <code>ENDW</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class EndwDirective extends Mnemonic {

    @Nonnull
    static final EndwDirective ENDW = new EndwDirective();

    private EndwDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        if (!(context.getParentBlock() instanceof WhileBlockState)) {
            context.addMessage(new EndwWithoutWhileErrorMessage());
        }
    }

}
