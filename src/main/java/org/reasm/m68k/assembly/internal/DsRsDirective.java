package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.EnumMap;

import org.reasm.AssemblyMessage;
import org.reasm.SymbolContext;
import org.reasm.SymbolType;
import org.reasm.Value;
import org.reasm.m68k.messages.CountMustNotBeNegativeErrorMessage;

import com.google.common.primitives.UnsignedLongs;

/**
 * The <code>DS</code> and <code>RS</code> directives.
 *
 * @author Francis Gagné
 */
abstract class DsRsDirective extends Mnemonic {

    static final DsRsDirective DS = new DsRsDirective() {
        @Override
        void assemble(M68KAssemblyContext context, long count, long itemSize) throws IOException {
            // If count * itemSize is greater than UnsignedLongs.MAX_VALUE, don't even try.
            if (UnsignedLongs.compare(count, UnsignedLongs.divide(UnsignedLongs.MAX_VALUE, itemSize)) > 0) {
                throw new OutOfMemoryError();
            }

            final long byteCount = count * itemSize;
            for (long i = 0; i < byteCount; i++) {
                context.appendByte((byte) 0);
            }
        }

        @Override
        void automaticEven(M68KAssemblyContext context) throws IOException {
            context.automaticEven();
        }
    };

    static final DsRsDirective RS = new DsRsDirective() {
        @Override
        void assemble(M68KAssemblyContext context, long count, long itemSize) throws IOException {
            context.defineSymbols(SymbolContext.VALUE, SymbolType.CONSTANT, context.rs.getValue());
            context.rs.incrementBy(count * itemSize);
        }

        @Override
        void automaticEven(M68KAssemblyContext context) {
            context.rs.automaticEven();
        }

        @Override
        void defineLabels(M68KAssemblyContext context) {
            // Don't define any labels.
        }
    };

    private static final CardinalValueVisitor.ErrorFactory NEGATIVE_VALUE_ERROR_FACTORY = new CardinalValueVisitor.ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            return new CountMustNotBeNegativeErrorMessage();
        }
    };

    private static final EnumMap<InstructionSize, Long> ITEM_SIZE_BY_INSTRUCTION_SIZE = new EnumMap<>(InstructionSize.class);

    static {
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.DEFAULT, 2L);
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.BYTE, 1L);
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.WORD, 2L);
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.LONG, 4L);
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.QUAD, 8L);
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.SINGLE, 4L);
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.DOUBLE, 8L);
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.EXTENDED, 12L);
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.PACKED, 12L);
        ITEM_SIZE_BY_INSTRUCTION_SIZE.put(InstructionSize.INVALID, 2L);
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        InstructionSize size = context.parseInstructionSize();
        if (size == InstructionSize.INVALID) {
            context.addInvalidSizeAttributeErrorMessage();
            size = InstructionSize.DEFAULT;
        }

        if (size != InstructionSize.BYTE && context.automaticEven) {
            this.automaticEven(context);
        }

        if (context.requireNumberOfOperands(1)) {
            final Value countValue = evaluateExpressionOperand(context, 0);
            if (countValue != null) {
                final CardinalValueVisitor countVisitor = context.cardinalValueVisitor;
                countVisitor.reset(0, NEGATIVE_VALUE_ERROR_FACTORY);
                Value.accept(countValue, countVisitor);
                final long count = countVisitor.getValue();
                final long itemSize = ITEM_SIZE_BY_INSTRUCTION_SIZE.get(size);
                this.assemble(context, count, itemSize);
            }
        }
    }

    abstract void assemble(M68KAssemblyContext context, long count, long itemSize) throws IOException;

    abstract void automaticEven(M68KAssemblyContext context) throws IOException;

}
