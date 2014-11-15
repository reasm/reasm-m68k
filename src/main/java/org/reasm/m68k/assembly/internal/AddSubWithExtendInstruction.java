package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.InstructionSet;

/**
 * The <code>ABCD</code>, <code>ADDX</code>, <code>SBCD</code> and <code>SUBX</code> instructions.
 *
 * @author Francis Gagné
 */
@Immutable
class AddSubWithExtendInstruction extends TwoEaInstruction {

    @Immutable
    private static class AbcdSbcd extends AddSubWithExtendInstruction {

        AbcdSbcd(int fixedBits) {
            super(fixedBits);
        }

        @Override
        InstructionSize getInstructionSize(M68KAssemblyContext context) {
            final InstructionSize size = context.parseIntegerInstructionSize();
            if (size != InstructionSize.DEFAULT && size != InstructionSize.BYTE) {
                context.addInvalidSizeAttributeErrorMessage();
            }

            return InstructionSize.BYTE;
        }

    }

    @Nonnull
    static final AddSubWithExtendInstruction ABCD = new AddSubWithExtendInstruction.AbcdSbcd(0b100 << 12);
    @Nonnull
    static final AddSubWithExtendInstruction ADDX = new AddSubWithExtendInstruction(0b101 << 12);
    @Nonnull
    static final AddSubWithExtendInstruction SBCD = new AddSubWithExtendInstruction.AbcdSbcd(0b000 << 12);
    @Nonnull
    static final AddSubWithExtendInstruction SUBX = new AddSubWithExtendInstruction(0b001 << 12);

    @Nonnull
    private static final Set<AddressingMode> DATA_REGISTER_DIRECT_OR_ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT = Collections
            .unmodifiableSet(EnumSet.of(AddressingMode.DATA_REGISTER_DIRECT,
                    AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT));

    private final int fixedBits;

    AddSubWithExtendInstruction(int fixedBits) {
        this.fixedBits = fixedBits | 0b10000001 << 8;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        final int mode;
        if (ea0.isDataRegisterDirect() && ea1.isDataRegisterDirect()) {
            // Register to register
            mode = 0;
        } else if (ea0.isAddressRegisterIndirectWithPredecrement() && ea1.isAddressRegisterIndirectWithPredecrement()) {
            // Memory to memory
            mode = 1 << 3;
        } else {
            return;
        }

        context.appendWord((short) (this.fixedBits | ea1.getRegister() << 9 | encodeIntegerSizeStandard(size) | mode | ea0
                .getRegister()));
    }

    @Override
    Set<AddressingMode> getValidAddressingModesForDestinationOperand(InstructionSet instructionSet, EffectiveAddress ea0) {
        if (ea0.isDataRegisterDirect()) {
            return AddressingModeCategory.DATA_REGISTER_DIRECT;
        }

        if (ea0.isAddressRegisterIndirectWithPredecrement()) {
            return AddressingModeCategory.ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT;
        }

        return DATA_REGISTER_DIRECT_OR_ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT;
    }

    @Override
    Set<AddressingMode> getValidAddressingModesForSourceOperand(InstructionSet instructionSet) {
        return DATA_REGISTER_DIRECT_OR_ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT;
    }

}
