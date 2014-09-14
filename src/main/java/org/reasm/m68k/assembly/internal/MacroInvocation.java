package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.source.MacroInstantiation;

/**
 * A macro invocation.
 *
 * @author Francis Gagné
 */
final class MacroInvocation extends Mnemonic {

    private final Macro macro;

    /**
     * Initializes a new MacroInvocation.
     *
     * @param macro
     *            the macro to invoke
     */
    MacroInvocation(Macro macro) {
        this.macro = macro;
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        final MacroInstantiation macroInstantiation = this.macro.substituteMacroOperands(context);
        context.builder.enterChildFile(macroInstantiation, this.macro.getArchitecture());
    }

    @Override
    void defineLabels(M68KAssemblyContext context) {
        if (this.macro.hasLabelSubstitutions()) {
            context.defineExtraLabels();
        } else {
            context.defineLabels();
        }
    }

}
