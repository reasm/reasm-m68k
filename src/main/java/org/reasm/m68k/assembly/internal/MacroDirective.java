package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyStepLocation;
import org.reasm.SymbolType;
import org.reasm.messages.DirectiveRequiresLabelErrorMessage;
import org.reasm.source.SourceLocation;

/**
 * The <code>MACRO</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class MacroDirective extends Mnemonic {

    @Nonnull
    static final MacroDirective MACRO = new MacroDirective();

    private MacroDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        final Object block = context.getParentBlock();
        if (!(block instanceof MacroBlockState)) {
            throw new AssertionError();
        }

        if (context.numberOfLabels < 1) {
            context.addMessage(new DirectiveRequiresLabelErrorMessage(Mnemonics.MACRO));
        }

        final MacroBlockState macroBlockState = (MacroBlockState) block;

        // Get the macro's operands.
        final int numberOfOperands = context.numberOfOperands;
        final String[] operands = new String[numberOfOperands];
        for (int i = 0; i < numberOfOperands; i++) {
            operands[i] = context.getOperandText(i);
        }

        // Store a reference to the macro body in the macro symbol (and don't assemble it now).
        // IMPORTANT: We MUST call macroBlockState.iterator.next() in all cases,
        // because we don't want to assemble the macro body now!
        final SourceLocation macroBody = macroBlockState.iterator.next();

        // In a subsequent pass, when we meet a macro that was already defined,
        // we must reuse the Macro object we created earlier
        // to avoid changing the symbol's value and triggering new passes indefinitely.
        final AssemblyStepLocation stepLocation = context.step.getLocation();
        Macro macro = context.macrosByDefinitionLocation.get(stepLocation);
        if (macro == null) {
            macro = new Macro(context, operands, macroBody);
            context.macrosByDefinitionLocation.put(stepLocation, macro);
        }

        context.defineSymbols(M68KAssemblyContext.MNEMONIC, SymbolType.VARIABLE, macro);
    }

    @Override
    void defineLabels(M68KAssemblyContext context) {
        // Don't define any labels.
    }

}
