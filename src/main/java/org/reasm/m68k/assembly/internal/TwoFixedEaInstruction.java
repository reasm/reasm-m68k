package org.reasm.m68k.assembly.internal;

import java.util.Set;

import org.reasm.m68k.InstructionSet;

/**
 * Base class for instructions that take two operands that are effective addresses for which the valid addressing modes are fixed.
 *
 * @author Francis Gagné
 */
abstract class TwoFixedEaInstruction extends TwoEaInstruction {

    private final Set<AddressingMode> validAddressingModesForFirstOperand;
    private final Set<AddressingMode> validAddressingModesForSecondOperand;

    TwoFixedEaInstruction(Set<AddressingMode> validAddressingModesForFirstOperand,
            Set<AddressingMode> validAddressingModesForSecondOperand) {
        this.validAddressingModesForFirstOperand = validAddressingModesForFirstOperand;
        this.validAddressingModesForSecondOperand = validAddressingModesForSecondOperand;
    }

    @Override
    protected Set<AddressingMode> getValidAddressingModesForDestinationOperand(InstructionSet instructionSet, EffectiveAddress ea0) {
        return this.validAddressingModesForSecondOperand;
    }

    @Override
    protected Set<AddressingMode> getValidAddressingModesForSourceOperand(InstructionSet instructionSet) {
        return this.validAddressingModesForFirstOperand;
    }

}
