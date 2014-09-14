package org.reasm.m68k.assembly.internal;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.reasm.AssemblyMessage;
import org.reasm.Value;
import org.reasm.m68k.messages.IncbinLengthMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.IncbinStartMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.ValueOutOfRangeErrorMessage;

/**
 * The <code>INCBIN</code> (a.k.a. <code>BINCLUDE</code>) directive.
 *
 * @author Francis Gagné
 */
class IncbinDirective extends Mnemonic {

    static final IncbinDirective INCBIN = new IncbinDirective();

    private static final CardinalValueVisitor.ErrorFactory START_NEGATIVE_VALUE_ERROR_FACTORY = new CardinalValueVisitor.ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            return new IncbinStartMustNotBeNegativeErrorMessage();
        }
    };

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
                final long startLong = startVisitor.getValue();
                if (startLong < 0 || startLong > Integer.MAX_VALUE) {
                    new ValueOutOfRangeErrorMessage(startLong);
                }

                start = (int) startLong;

                if (context.numberOfOperands >= 3) {
                    final Value lengthOperand = evaluateExpressionOperand(context, 2);
                    final CardinalValueVisitor lengthVisitor = context.cardinalValueVisitor;
                    lengthVisitor.reset(start, LENGTH_NEGATIVE_VALUE_ERROR_FACTORY);
                    Value.accept(lengthOperand, lengthVisitor);
                    final long lengthLong = lengthVisitor.getValue();
                    if (lengthLong < 0 || lengthLong > Integer.MAX_VALUE) {
                        new ValueOutOfRangeErrorMessage(lengthLong);
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
