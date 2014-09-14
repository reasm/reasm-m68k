package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Set;

import org.reasm.m68k.InstructionSet;

/**
 * Base class for instructions that take two operands that are effective addresses. Subclasses can also handle exceptions by
 * overriding {@link #assemble(M68KAssemblyContext, InstructionSize)} and not calling the superclass's implementation if an
 * instruction uses an exceptional notation.
 *
 * @author Francis Gagné
 */
abstract class TwoEaInstruction extends TwoOperandIntegerInstruction {

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        final EffectiveAddress ea0 = context.ea0;
        final EffectiveAddress ea1 = context.ea1;

        // Parse the source operand.
        context.getEffectiveAddress(context.getOperandText(0),
                this.getValidAddressingModesForSourceOperand(context.instructionSet), size, ea0);

        // Parse the destination operand.
        context.getEffectiveAddress(context.getOperandText(1),
                this.getValidAddressingModesForDestinationOperand(context.instructionSet, ea0), size, ea0.numberOfWords * 2, ea1);

        this.assemble(context, size, ea0, ea1);
    }

    abstract void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
            throws IOException;

    abstract Set<AddressingMode> getValidAddressingModesForDestinationOperand(InstructionSet instructionSet, EffectiveAddress ea0);

    abstract Set<AddressingMode> getValidAddressingModesForSourceOperand(InstructionSet instructionSet);

}
