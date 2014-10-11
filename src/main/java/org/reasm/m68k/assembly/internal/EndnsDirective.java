package org.reasm.m68k.assembly.internal;

import org.reasm.m68k.messages.EndnsWithoutNamespaceErrorMessage;
import org.reasm.m68k.source.NamespaceBlock;

/**
 * The <code>ENDNS</code> directive.
 *
 * @author Francis Gagné
 */
class EndnsDirective extends Mnemonic {

    static final EndnsDirective ENDNS = new EndnsDirective();

    private EndnsDirective() {
    }

    @Override
    protected void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        if (!(context.getParentNode() instanceof NamespaceBlock)) {
            context.addMessage(new EndnsWithoutNamespaceErrorMessage());
        }
    }

}
