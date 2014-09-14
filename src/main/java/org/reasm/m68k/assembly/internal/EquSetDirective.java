package org.reasm.m68k.assembly.internal;

import org.reasm.SymbolContext;
import org.reasm.SymbolType;
import org.reasm.messages.DirectiveRequiresLabelErrorMessage;

class EquSetDirective extends Mnemonic {

    static final EquSetDirective EQU = new EquSetDirective(Mnemonics.EQU, SymbolType.CONSTANT);
    static final EquSetDirective SET = new EquSetDirective(Mnemonics.SET, SymbolType.VARIABLE);
    static final EquSetDirective EQUALS = new EquSetDirective(Mnemonics.EQUALS, SymbolType.VARIABLE);

    private final String directiveName;
    private final SymbolType symbolType;

    private EquSetDirective(String directiveName, SymbolType symbolType) {
        this.directiveName = directiveName;
        this.symbolType = symbolType;
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();

        if (context.numberOfLabels == 0) {
            context.addMessage(new DirectiveRequiresLabelErrorMessage(this.directiveName));
        } else {
            if (context.requireNumberOfOperands(1)) {
                context.defineSymbols(SymbolContext.VALUE, this.symbolType, evaluateExpressionOperand(context, 0));
            }
        }
    }

    @Override
    void defineLabels(M68KAssemblyContext context) {
        // Don't define any labels.
    }

}
