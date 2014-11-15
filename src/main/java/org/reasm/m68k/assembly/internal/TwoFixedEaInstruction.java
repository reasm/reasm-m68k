package org.reasm.m68k.assembly.internal;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.InstructionSet;

/**
 * Base class for instructions that take two operands that are effective addresses for which the valid addressing modes are fixed.
 *
 * @author Francis Gagné
 */
@Immutable
abstract class TwoFixedEaInstruction extends TwoEaInstruction {

    @Nonnull
    private final Set<AddressingMode> validAddressingModesForFirstOperand;
    @Nonnull
    private final Set<AddressingMode> validAddressingModesForSecondOperand;

    TwoFixedEaInstruction(@Nonnull Set<AddressingMode> validAddressingModesForFirstOperand,
            @Nonnull Set<AddressingMode> validAddressingModesForSecondOperand) {
        this.validAddressingModesForFirstOperand = validAddressingModesForFirstOperand;
        this.validAddressingModesForSecondOperand = validAddressingModesForSecondOperand;
    }

    @Override
    final Set<AddressingMode> getValidAddressingModesForDestinationOperand(InstructionSet instructionSet, EffectiveAddress ea0) {
        return this.validAddressingModesForSecondOperand;
    }

    @Override
    final Set<AddressingMode> getValidAddressingModesForSourceOperand(InstructionSet instructionSet) {
        return this.validAddressingModesForFirstOperand;
    }

}
