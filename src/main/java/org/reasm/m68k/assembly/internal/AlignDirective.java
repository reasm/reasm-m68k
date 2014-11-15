package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyMessage;
import org.reasm.Value;
import org.reasm.m68k.messages.AlignmentMustNotBeZeroOrNegativeErrorMessage;

import com.google.common.primitives.UnsignedLongs;

/**
 * The <code>ALIGN</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class AlignDirective extends Mnemonic {

    @Nonnull
    static final AlignDirective ALIGN = new AlignDirective();

    @Nonnull
    private static final CardinalValueVisitor.ErrorFactory NEGATIVE_VALUE_ERROR_FACTORY = new CardinalValueVisitor.ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            return new AlignmentMustNotBeZeroOrNegativeErrorMessage();
        }
    };

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();
        if (context.requireNumberOfOperands(1)) {
            final Value alignmentValue = evaluateExpressionOperand(context, 0);
            if (alignmentValue != null) {
                final CardinalValueVisitor alignmentVisitor = context.cardinalValueVisitor;
                alignmentVisitor.reset(1, NEGATIVE_VALUE_ERROR_FACTORY);
                Value.accept(alignmentValue, alignmentVisitor);
                final long alignment = alignmentVisitor.getValue();
                if (alignment != 0) {
                    final long remainder = UnsignedLongs.remainder(context.programCounter, alignment);
                    final long paddingSize = remainder == 0 ? 0 : alignment - remainder;

                    for (long i = 0; UnsignedLongs.compare(i, paddingSize) < 0; i++) {
                        context.appendByte((byte) 0);
                    }
                } else {
                    context.addTentativeMessage(new AlignmentMustNotBeZeroOrNegativeErrorMessage());
                }
            }
        }
    }

}
