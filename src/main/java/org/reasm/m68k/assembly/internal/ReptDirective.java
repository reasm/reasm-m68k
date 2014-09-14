package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.AssemblyMessage;
import org.reasm.Value;
import org.reasm.m68k.messages.CountMustNotBeNegativeErrorMessage;

/**
 * The <code>REPT</code> directive.
 *
 * @author Francis Gagné
 */
class ReptDirective extends Mnemonic {

    static final ReptDirective REPT = new ReptDirective();

    private static final CardinalValueVisitor.ErrorFactory NEGATIVE_VALUE_ERROR_FACTORY = new CardinalValueVisitor.ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            return new CountMustNotBeNegativeErrorMessage();
        }
    };

    private ReptDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        final Object blockState = context.getParentBlock();
        if (!(blockState instanceof ReptBlockState)) {
            throw new AssertionError();
        }

        final ReptBlockState reptBlockState = (ReptBlockState) blockState;
        reptBlockState.count = 0;
        if (context.requireNumberOfOperands(1)) {
            final Value countValue = evaluateExpressionOperand(context, 0);
            final CardinalValueVisitor countVisitor = context.cardinalValueVisitor;
            countVisitor.reset(0, NEGATIVE_VALUE_ERROR_FACTORY);
            Value.accept(countValue, countVisitor);
            reptBlockState.count = countVisitor.getValue();
        }
    }

}
