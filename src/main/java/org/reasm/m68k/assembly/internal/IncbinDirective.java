package org.reasm.m68k.assembly.internal;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyMessage;
import org.reasm.Value;
import org.reasm.m68k.messages.IncbinLengthMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.IncbinStartMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.ValueOutOfRangeErrorMessage;

/**
 * The <code>INCBIN</code> and <code>BINCLUDE</code> directives.
 *
 * @author Francis Gagné
 */
@Immutable
class IncbinDirective extends Mnemonic {

    @Nonnull
    static final IncbinDirective INCBIN = new IncbinDirective();

    @Nonnull
    private static final CardinalValueVisitor.ErrorFactory START_NEGATIVE_VALUE_ERROR_FACTORY = new CardinalValueVisitor.ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            return new IncbinStartMustNotBeNegativeErrorMessage();
        }
    };

    @Nonnull
    private static final CardinalValueVisitor.ErrorFactory LENGTH_NEGATIVE_VALUE_ERROR_FACTORY = new CardinalValueVisitor.ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            return new IncbinLengthMustNotBeNegativeErrorMessage();
        }
    };

    private IncbinDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        if (context.numberOfOperands < 1) {
            context.addWrongNumberOfOperandsErrorMessage();
            return;
        }

        if (context.numberOfOperands > 3) {
            context.addWrongNumberOfOperandsErrorMessage();
        }

        final String filePath = IncludeDirective.getFilePath(context, 0);
        if (filePath != null) {
            final byte[] data = context.builder.getAssembly().fetchBinaryFile(filePath);
            if (data == null) {
                throw new FileNotFoundException(filePath);
            }

            int start = 0;
            int length = data.length;
            if (context.numberOfOperands >= 2) {
                final Value startOperand = evaluateExpressionOperand(context, 1);
                final CardinalValueVisitor startVisitor = context.cardinalValueVisitor;
                startVisitor.reset(start, START_NEGATIVE_VALUE_ERROR_FACTORY);
                Value.accept(startOperand, startVisitor);

                long startLong = startVisitor.getValue();
                if (startLong > data.length) {
                    context.addTentativeMessage(new ValueOutOfRangeErrorMessage(startLong));
                    startLong = data.length;
                }

                start = (int) startLong;
                length = data.length - start;

                if (context.numberOfOperands >= 3) {
                    final Value lengthOperand = evaluateExpressionOperand(context, 2);
                    final CardinalValueVisitor lengthVisitor = context.cardinalValueVisitor;
                    lengthVisitor.reset(length, LENGTH_NEGATIVE_VALUE_ERROR_FACTORY);
                    Value.accept(lengthOperand, lengthVisitor);

                    long lengthLong = lengthVisitor.getValue();
                    if (lengthLong > data.length - start) {
                        context.addTentativeMessage(new ValueOutOfRangeErrorMessage(lengthLong));
                        lengthLong = data.length - start;
                    }

                    length = (int) lengthLong;
                }
            }

            if (length != 0) {
                context.builder.appendAssembledData(data, start, length);
            }
        }
    }

}
