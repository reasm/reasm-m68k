package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.messages.EndifWithoutIfErrorMessage;

/**
 * The <code>ENDIF</code> and <code>ENDC</code> directives.
 *
 * @author Francis Gagné
 */
@Immutable
class EndifDirective extends Mnemonic {

    @Nonnull
    static final EndifDirective ENDIF = new EndifDirective();

    private EndifDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        if (!(context.getParentBlock() instanceof IfBlockState)) {
            context.addMessage(new EndifWithoutIfErrorMessage());
        }
    }

}
