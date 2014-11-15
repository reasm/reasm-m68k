package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyMessage;
import org.reasm.Value;
import org.reasm.m68k.messages.CountMustNotBeNegativeErrorMessage;

import com.google.common.primitives.UnsignedLongs;

/**
 * The <code>DCB</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class DcbDirective extends Mnemonic {

    @Nonnull
    static final DcbDirective DCB = new DcbDirective();

    @Nonnull
    private static final CardinalValueVisitor.ErrorFactory NEGATIVE_VALUE_ERROR_FACTORY = new CardinalValueVisitor.ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            return new CountMustNotBeNegativeErrorMessage();
        }
    };

    private DcbDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        InstructionSize size = context.parseInstructionSize();
        if (size == InstructionSize.INVALID) {
            context.addInvalidSizeAttributeErrorMessage();
            size = InstructionSize.DEFAULT;
        }

        if (size != InstructionSize.BYTE) {
            context.automaticEven();
        }

        if (context.requireNumberOfOperands(2)) {
            final Value countValue = evaluateExpressionOperand(context, 0);
            if (countValue != null) {
                final CardinalValueVisitor countVisitor = context.cardinalValueVisitor;
                countVisitor.reset(0, NEGATIVE_VALUE_ERROR_FACTORY);
                Value.accept(countValue, countVisitor);
                final long count = countVisitor.getValue();

                final Value dataValue = evaluateExpressionOperand(context, 1);
                final DcValueVisitor dataVisitor = context.getDcValueVisitor(size);
                dataVisitor.reset(size);
                Value.accept(dataValue, dataVisitor);
                for (long i = 0; UnsignedLongs.compare(i, count) < 0; i++) {
                    dataVisitor.encode();
                }
            }
        }
    }

}
