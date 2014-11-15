package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.messages.EndnsWithoutNamespaceErrorMessage;
import org.reasm.m68k.source.NamespaceBlock;

/**
 * The <code>ENDNS</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class EndnsDirective extends Mnemonic {

    @Nonnull
    static final EndnsDirective ENDNS = new EndnsDirective();

    private EndnsDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        if (!(context.getParentNode() instanceof NamespaceBlock)) {
            context.addMessage(new EndnsWithoutNamespaceErrorMessage());
        }
    }

}
