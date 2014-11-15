package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.messages.ShiftCountOutOfRangeErrorMessage;

/**
 * A shift or a rotate instruction (<code>ASL</code>, <code>ASR</code>, <code>LSL</code>, <code>LSR</code>, <code>ROL</code>,
 * <code>ROR</code>, <code>ROXL</code>, <code>ROXR</code>).
 *
 * @author Francis Gagné
 */
@Immutable
class ShiftRotateInstruction extends Instruction {

    @Nonnull
    static final ShiftRotateInstruction ASL = new ShiftRotateInstruction(0, 1);
    @Nonnull
    static final ShiftRotateInstruction ASR = new ShiftRotateInstruction(0, 0);
    @Nonnull
    static final ShiftRotateInstruction LSL = new ShiftRotateInstruction(1, 1);
    @Nonnull
    static final ShiftRotateInstruction LSR = new ShiftRotateInstruction(1, 0);
    @Nonnull
    static final ShiftRotateInstruction ROL = new ShiftRotateInstruction(3, 1);
    @Nonnull
    static final ShiftRotateInstruction ROR = new ShiftRotateInstruction(3, 0);
    @Nonnull
    static final ShiftRotateInstruction ROXL = new ShiftRotateInstruction(2, 1);
    @Nonnull
    static final ShiftRotateInstruction ROXR = new ShiftRotateInstruction(2, 0);

    private final int operation;
    private final int direction;

    private ShiftRotateInstruction(int operation, int direction) {
        this.operation = operation;
        this.direction = direction;
    }

    @Override
    void assemble2(M68KAssemblyContext context) throws IOException {
        InstructionSize size = context.parseIntegerInstructionSize();
        if (size == InstructionSize.INVALID) {
            context.addInvalidSizeAttributeErrorMessage();
            size = InstructionSize.DEFAULT;
        }

        if (context.numberOfOperands != 1 && context.numberOfOperands != 2) {
            context.addWrongNumberOfOperandsErrorMessage();
        }

        final EffectiveAddress ea0 = context.ea0;
        final EffectiveAddress ea1 = context.ea1;

        if (context.numberOfOperands >= 2) {
            // Parse the shift count.
            context.getEffectiveAddress(context.getOperandText(0), AddressingModeCategory.DATA_REGISTER_DIRECT_OR_IMMEDIATE_DATA,
                    InstructionSize.LONG, ea0);

            // Parse the destination operand.
            context.getEffectiveAddress(context.getOperandText(1), AddressingModeCategory.DATA_REGISTER_DIRECT, size, ea1);

            // Encode the instruction.
            if (ea0.isDataRegisterDirect()) {
                context.appendWord((short) (0b11100000_00100000 | ea0.getRegister() << 9 | this.direction << 8
                        | encodeIntegerSizeStandard(size) | this.operation << 3 | ea1.getRegister()));
            } else if (ea0.isImmediateData()) {
                final int immediateData = ea0.word1 << 16 | ea0.word2;
                if (immediateData < 1 || immediateData > 8) {
                    context.addTentativeMessage(new ShiftCountOutOfRangeErrorMessage());
                }

                context.appendWord((short) (0b11100000_00000000 | (immediateData & 7) << 9 | this.direction << 8
                        | encodeIntegerSizeStandard(size) | this.operation << 3 | ea1.getRegister()));
            }
        } else if (context.numberOfOperands == 1) {
            // Parse the destination operand.
            context.getEffectiveAddress(context.getOperandText(0), AddressingModeCategory.DATA_ALTERABLE, InstructionSize.WORD, ea0);

            if (ea0.isDataRegisterDirect()) {
                // Encode the instruction.
                context.appendWord((short) (0b11100000_00000000 | 0b001 << 9 | this.direction << 8
                        | encodeIntegerSizeStandard(size) | this.operation << 3 | ea0.getRegister()));
            } else {
                if (size != InstructionSize.DEFAULT && size != InstructionSize.WORD) {
                    context.addInvalidSizeAttributeErrorMessage();
                }

                // Encode the instruction.
                ea0.word0 |= 0b11100000_11000000 | this.operation << 9 | this.direction << 8;
                context.appendEffectiveAddress(ea0);
            }
        }
    }

}
