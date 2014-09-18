package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.m68k.messages.ElseWithoutIfErrorMessage;

/**
 * The <code>ELSE</code> directive.
 *
 * @author Francis Gagné
 */
class ElseDirective extends Mnemonic {

    static final ElseDirective ELSE = new ElseDirective();

    private ElseDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        final Object block = context.getParentBlock();
        if (block instanceof IfBlockState) {
            final IfBlockState ifBlockState = (IfBlockState) block;

            // Process the block body, then stop.
            ifBlockState.iterator.stopAfterNext();
        } else {
            context.addMessage(new ElseWithoutIfErrorMessage());
        }
    }

}
