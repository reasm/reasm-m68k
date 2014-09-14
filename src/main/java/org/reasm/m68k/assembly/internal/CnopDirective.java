package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.AssemblyMessage;
import org.reasm.Value;
import org.reasm.m68k.messages.AlignmentMustNotBeZeroOrNegativeErrorMessage;
import org.reasm.m68k.messages.OffsetMustNotBeNegativeErrorMessage;

import com.google.common.primitives.UnsignedLongs;

/**
 * The <code>CNOP</code> directive.
 *
 * @author Francis Gagné
 */
class CnopDirective extends Mnemonic {

    static final CnopDirective CNOP = new CnopDirective();

    private static final CardinalValueVisitor.ErrorFactory OFFSET_NEGATIVE_VALUE_ERROR_FACTORY = new CardinalValueVisitor.ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            return new OffsetMustNotBeNegativeErrorMessage();
        }
    };

    private static final CardinalValueVisitor.ErrorFactory ALIGNMENT_NEGATIVE_VALUE_ERROR_FACTORY = new CardinalValueVisitor.ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            return new AlignmentMustNotBeZeroOrNegativeErrorMessage();
        }
    };

    private CnopDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        if (context.requireNumberOfOperands(2)) {
            final CardinalValueVisitor cardinalValueVisitor = context.cardinalValueVisitor;

            final Value offsetValue = evaluateExpressionOperand(context, 0);
            if (offsetValue != null) {
                cardinalValueVisitor.reset(0, OFFSET_NEGATIVE_VALUE_ERROR_FACTORY);
                Value.accept(offsetValue, cardinalValueVisitor);
                final long offset = cardinalValueVisitor.getValue();
                final Value alignmentValue = evaluateExpressionOperand(context, 1);
                if (alignmentValue != null) {
                    cardinalValueVisitor.reset(1, ALIGNMENT_NEGATIVE_VALUE_ERROR_FACTORY);
                    Value.accept(alignmentValue, cardinalValueVisitor);
                    final long alignment = cardinalValueVisitor.getValue();
                    if (alignment != 0) {
                        long boundary = UnsignedLongs.divide(context.programCounter, alignment) * alignment;
                        if (boundary + offset < context.programCounter) {
                            boundary += alignment;
                            assert boundary + offset >= context.programCounter;
                        }

                        final long paddingSize = boundary + offset - context.programCounter;
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

}
