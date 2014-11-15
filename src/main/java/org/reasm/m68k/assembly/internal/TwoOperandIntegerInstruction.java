package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * An integer instruction that takes exactly two operands.
 *
 * @author Francis Gagné
 */
@Immutable
abstract class TwoOperandIntegerInstruction extends Instruction {

    abstract void assemble(@Nonnull M68KAssemblyContext context, @Nonnull InstructionSize size) throws IOException;

    @Override
    final void assemble2(M68KAssemblyContext context) throws IOException {
        final InstructionSize size = this.getInstructionSize(context);

        if (context.requireNumberOfOperands(2)) {
            this.assemble(context, size);
        }
    }

    @Nonnull
    InstructionSize getInstructionSize(@Nonnull M68KAssemblyContext context) {
        InstructionSize size = context.parseIntegerInstructionSize();
        if (size == InstructionSize.INVALID) {
            context.addInvalidSizeAttributeErrorMessage();
            size = InstructionSize.DEFAULT;
        }

        return size;
    }

}
