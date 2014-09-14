package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.SymbolType;
import org.reasm.m68k.messages.RegisterExpectedErrorMessage;
import org.reasm.messages.DirectiveRequiresLabelErrorMessage;

/**
 * The <code>EQUR</code> directive.
 *
 * @author Francis Gagné
 */
public class EqurDirective extends Mnemonic {

    static final EqurDirective EQUR = new EqurDirective();

    private EqurDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        if (context.numberOfLabels == 0) {
            context.addMessage(new DirectiveRequiresLabelErrorMessage(Mnemonics.EQUR));
        } else {
            if (context.requireNumberOfOperands(1)) {
                context.prepareOperandReader(0);
                final GeneralPurposeRegister register = parseRegister(context, context.logicalLineReader);
                if (register != null) {
                    context.defineSymbols(M68KAssemblyContext.REGISTER_ALIAS, SymbolType.CONSTANT, register);
                } else {
                    context.addMessage(new RegisterExpectedErrorMessage());
                }
            }
        }
    }

    @Override
    void defineLabels(M68KAssemblyContext context) {
        // Don't define any labels.
    }

}
