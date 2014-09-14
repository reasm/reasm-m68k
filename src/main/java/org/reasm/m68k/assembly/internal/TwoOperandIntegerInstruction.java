package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * An integer instruction that takes exactly two operands.
 *
 * @author Francis Gagné
 */
abstract class TwoOperandIntegerInstruction extends Instruction {

    abstract void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException;

    @Override
    final void assemble2(M68KAssemblyContext context) throws IOException {
        final InstructionSize size = this.getInstructionSize(context);

        if (context.requireNumberOfOperands(2)) {
            this.assemble(context, size);
        }
    }

    InstructionSize getInstructionSize(M68KAssemblyContext context) {
        InstructionSize size = context.parseIntegerInstructionSize();
        if (size == InstructionSize.INVALID) {
            context.addInvalidSizeAttributeErrorMessage();
            size = InstructionSize.DEFAULT;
        }

        return size;
    }

}
