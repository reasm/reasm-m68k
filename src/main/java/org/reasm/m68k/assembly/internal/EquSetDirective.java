package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.SymbolContext;
import org.reasm.SymbolType;
import org.reasm.messages.DirectiveRequiresLabelErrorMessage;

/**
 * The <code>EQU</code>, <code>SET</code> and <code>=</code> directives.
 *
 * @author Francis Gagné
 */
@Immutable
class EquSetDirective extends Mnemonic {

    @Nonnull
    static final EquSetDirective EQU = new EquSetDirective(Mnemonics.EQU, SymbolType.CONSTANT);
    @Nonnull
    static final EquSetDirective SET = new EquSetDirective(Mnemonics.SET, SymbolType.VARIABLE);
    @Nonnull
    static final EquSetDirective EQUALS = new EquSetDirective(Mnemonics.EQUALS, SymbolType.VARIABLE);

    @Nonnull
    private final String directiveName;
    @Nonnull
    private final SymbolType symbolType;

    private EquSetDirective(@Nonnull String directiveName, @Nonnull SymbolType symbolType) {
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
