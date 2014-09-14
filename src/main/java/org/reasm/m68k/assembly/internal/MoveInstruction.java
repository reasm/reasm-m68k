package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Set;

/**
 * The <code>MOVE</code> instruction.
 *
 * @author Francis Gagné
 */
class MoveInstruction extends TwoOperandIntegerInstruction {

    static final MoveInstruction MOVE = new MoveInstruction();

    static void assembleBasicMove(M68KAssemblyContext context, InstructionSize size,
            Set<AddressingMode> validAddressingModesForDestination) throws IOException {
        final EffectiveAddress ea0 = context.ea0;
        final EffectiveAddress ea1 = context.ea1;

        // Parse and evaluate the operands.
        context.getEffectiveAddress(context.getOperandText(0), AddressingModeCategory.ALL, size, 2, ea0);
        context.getEffectiveAddress(context.getOperandText(1), validAddressingModesForDestination, size, ea0.numberOfWords * 2, ea1);

        // Try to optimize to MOVEQ.
        if (context.optimizeMoveToMoveq && size == InstructionSize.LONG && ea0.isImmediateData() && ea1.isDataRegisterDirect()) {
            final int immediateData = ea0.word1 << 16 | ea0.word2;
            if (immediateData >= -0x80 && immediateData <= 0x7F) {
                MoveqInstruction.encode(context, immediateData, ea1.getRegister());
                return;
            }
        }

        // Encode the instruction.
        switch (size) {
        case BYTE:
            ea0.word0 |= 0b00010000_00000000;

            // Byte size is invalid when address register direct is used for the source and/or the destination.
            context.validateForByteAccess(ea0);
            context.validateForByteAccess(ea1);

            break;

        case LONG:
            ea0.word0 |= 0b00100000_00000000;
            break;

        case WORD:
        default:
            ea0.word0 |= 0b00110000_00000000;
            break;
        }

        ea0.word0 |= (short) (ea1.getRegister() << 9);
        ea0.word0 |= (short) (ea1.getMode() << 3);
        context.appendEffectiveAddress(ea0);
        context.appendEffectiveAddress(ea1, 1);
    }

    private static void assembleMoveSrCcr(M68KAssemblyContext context, int otherOperandIndex, InstructionSize size, boolean isCcr,
            boolean isDest) throws IOException {
        final EffectiveAddress ea = context.ea0;

        // Parse and evaluate the operand that is not the CCR or SR register.
        context.getEffectiveAddress(context.getOperandText(otherOperandIndex), isDest ? AddressingModeCategory.DATA
                : AddressingModeCategory.ALTERABLE, InstructionSize.WORD, ea);

        // Encode the instruction.
        ea.word0 |= (short) (0b01000000_11000000 | (isDest ? 0b00000100_00000000 : 0) | (isCcr ^ isDest ? 0b00000010_00000000 : 0));
        context.appendEffectiveAddress(ea);

        // Yes, MOVE from/to CCR is a word-sized operation. Go figure.
        if (size != InstructionSize.DEFAULT && size != InstructionSize.WORD) {
            context.addInvalidSizeAttributeErrorMessage();
        }
    }

    private static void assembleMoveUsp(M68KAssemblyContext context, int otherOperandIndex, InstructionSize size, boolean isDest)
            throws IOException {
        final EffectiveAddress ea = context.ea0;

        // Parse and evaluate the operand that is not the USP register.
        context.getEffectiveAddress(context.getOperandText(otherOperandIndex), AddressingModeCategory.ADDRESS_REGISTER_DIRECT,
                InstructionSize.LONG, ea);

        // Encode the instruction.
        final int register;
        register = ea.isAddressRegisterDirect() ? ea.word0 & EffectiveAddress.REGISTER_MASK : 0;

        ea.word0 = (short) (0b01001110_01100000 | (isDest ? 0 : 0b00000000_00001000) | register);
        ea.numberOfWords = 1;
        context.appendEffectiveAddress(ea);

        if (size != InstructionSize.DEFAULT && size != InstructionSize.LONG) {
            context.addInvalidSizeAttributeErrorMessage();
        }
    }

    private MoveInstruction() {
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        boolean isCcr = false;
        boolean isDest = false;
        if (parseSpecialRegister(context, 0, "SR") || (isCcr = parseSpecialRegister(context, 0, "CCR"))
                || (isDest = parseSpecialRegister(context, 1, "SR")) || (isCcr = isDest = parseSpecialRegister(context, 1, "CCR"))) {
            assembleMoveSrCcr(context, isDest ? 0 : 1, size, isCcr, isDest);
        } else if (parseSpecialRegister(context, 0, "USP") || (isDest = parseSpecialRegister(context, 1, "USP"))) {
            assembleMoveUsp(context, isDest ? 0 : 1, size, isDest);
        } else {
            assembleBasicMove(context, size, AddressingModeCategory.ALTERABLE);
        }
    }

}
