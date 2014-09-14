package org.reasm.m68k.assembly.internal;

import org.reasm.m68k.messages.EndifWithoutIfErrorMessage;

class EndifDirective extends Mnemonic {

    public static final EndifDirective ENDIF = new EndifDirective();

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
