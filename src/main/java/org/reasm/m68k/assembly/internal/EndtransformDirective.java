package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.messages.EndtransformWithoutTransformErrorMessage;
import org.reasm.m68k.source.TransformBlock;

/**
 * The <code>ENDTRANSFORM</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class EndtransformDirective extends Mnemonic {

    @Nonnull
    static final EndtransformDirective ENDTRANSFORM = new EndtransformDirective();

    private EndtransformDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        if (!(context.getParentNode() instanceof TransformBlock)) {
            context.addMessage(new EndtransformWithoutTransformErrorMessage());
        }
    }

}
