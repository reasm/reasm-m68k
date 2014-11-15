package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>EXG</code> instruction.
 *
 * @author Francis Gagné
 */
@Immutable
class ExgInstruction extends TwoFixedEaInstruction {

    @Nonnull
    static final ExgInstruction EXG = new ExgInstruction();

    private static final int OPMODE_DATA_REGISTERS = 0b01000 << 3;
    private static final int OPMODE_ADDRESS_REGISTERS = 0b01001 << 3;
    private static final int OPMODE_DATA_REGISTER_AND_ADDRESS_REGISTER = 0b10001 << 3;

    private static void encode(@Nonnull M68KAssemblyContext context, int opmode, int registerRx, int registerRy) throws IOException {
        context.appendWord((short) (0b11000001_00000000 | registerRx << 9 | opmode | registerRy));
    }

    private ExgInstruction() {
        super(AddressingModeCategory.DATA_OR_ADDRESS_REGISTER_DIRECT, AddressingModeCategory.DATA_OR_ADDRESS_REGISTER_DIRECT);
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        if (ea0.isDataRegisterDirect()) {
            if (ea1.isDataRegisterDirect()) {
                // 2 data registers
                encode(context, OPMODE_DATA_REGISTERS, ea0.getRegister(), ea1.getRegister());
            } else if (ea1.isAddressRegisterDirect()) {
                // Data register and address register
                encode(context, OPMODE_DATA_REGISTER_AND_ADDRESS_REGISTER, ea0.getRegister(), ea1.getRegister());
            }
        } else if (ea0.isAddressRegisterDirect()) {
            if (ea1.isDataRegisterDirect()) {
                // Address register and data register
                // The data register must be in Rx and the address register must be in Ry.
                encode(context, OPMODE_DATA_REGISTER_AND_ADDRESS_REGISTER, ea1.getRegister(), ea0.getRegister());
            } else if (ea1.isAddressRegisterDirect()) {
                // 2 address registers
                encode(context, OPMODE_ADDRESS_REGISTERS, ea0.getRegister(), ea1.getRegister());
            }
        }
    }

    @Override
    InstructionSize getInstructionSize(M68KAssemblyContext context) {
        final InstructionSize size = context.parseIntegerInstructionSize();
        if (size != InstructionSize.DEFAULT && size != InstructionSize.LONG) {
            context.addInvalidSizeAttributeErrorMessage();
        }

        return InstructionSize.LONG;
    }

}
