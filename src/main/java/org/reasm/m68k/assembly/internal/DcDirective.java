package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;

/**
 * The <code>DC</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class DcDirective extends Mnemonic {

    @Nonnull
    static final DcDirective DC = new DcDirective();

    private DcDirective() {
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

        final int numberOfOperands = context.numberOfOperands;
        if (numberOfOperands == 0) {
            context.addWrongNumberOfOperandsErrorMessage();
        }

        final DcValueVisitor visitor = context.getDcValueVisitor(size);

        for (int i = 0; i < numberOfOperands; i++) {
            final Value value = evaluateExpressionOperand(context, i);
            visitor.reset(size);
            Value.accept(value, visitor);
            visitor.encode();
        }
    }

}
