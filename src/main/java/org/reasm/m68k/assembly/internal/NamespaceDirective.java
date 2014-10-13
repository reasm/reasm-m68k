package org.reasm.m68k.assembly.internal;

import org.reasm.messages.DirectiveRequiresLabelErrorMessage;

/**
 * The <code>NAMESPACE</code> directive.
 *
 * @author Francis Gagné
 */
class NamespaceDirective extends Mnemonic {

    static final NamespaceDirective NAMESPACE = new NamespaceDirective();

    private NamespaceDirective() {
    }

    @Override
    protected void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        final ScopedEffectBlockEvents blockEvents = this.getScopedEffectBlockEvents(context);

        if (context.numberOfLabels < 1) {
            context.addMessage(new DirectiveRequiresLabelErrorMessage(Mnemonics.NAMESPACE));
        } else {
            context.builder.enterNamespace(context.getLabelText(context.numberOfLabels - 1));
            blockEvents.effectApplied();
        }
    }

    @Override
    void defineLabels(M68KAssemblyContext context) {
        context.defineExtraLabels();
    }

}
