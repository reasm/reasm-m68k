package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.m68k.messages.EndmWithoutMacroErrorMessage;

class EndmDirective extends Mnemonic {

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
