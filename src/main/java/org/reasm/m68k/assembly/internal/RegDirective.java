package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Set;

import org.reasm.SymbolType;
import org.reasm.m68k.messages.RegisterListExpectedErrorMessage;
import org.reasm.messages.DirectiveRequiresLabelErrorMessage;

class RegDirective extends Mnemonic {

    static final RegDirective REG = new RegDirective();

    private RegDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        if (context.numberOfLabels == 0) {
            context.addMessage(new DirectiveRequiresLabelErrorMessage(Mnemonics.REG));
        } else {
            if (context.requireNumberOfOperands(1)) {
                final Set<GeneralPurposeRegister> registerList = parseRegisterList(context, 0);
                if (registerList != null) {
                    context.defineSymbols(M68KAssemblyContext.REGISTER_LIST_ALIAS, SymbolType.CONSTANT, new RegisterList(
                            registerList));
                } else {
                    context.addMessage(new RegisterListExpectedErrorMessage());
                }
            }
        }
    }

    @Override
    void defineLabels(M68KAssemblyContext context) {
        // Don't define any labels.
    }

}
