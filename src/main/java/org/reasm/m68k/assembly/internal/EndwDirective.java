package org.reasm.m68k.assembly.internal;

import org.reasm.m68k.messages.EndwWithoutWhileErrorMessage;

class EndwDirective extends Mnemonic {

    public static final EndwDirective ENDW = new EndwDirective();

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
