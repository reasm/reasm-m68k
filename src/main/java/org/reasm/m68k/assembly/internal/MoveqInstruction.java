package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.m68k.messages.DataForMoveqOutOfRangeErrorMessage;
import org.reasm.m68k.messages.DataForMoveqWillBeSignExtendedWarningMessage;

class MoveqInstruction extends TwoFixedEaInstruction {

    static final MoveqInstruction MOVEQ = new MoveqInstruction();

    static void encode(M68KAssemblyContext context, int immediateData, int dataRegister) throws IOException {
        context.appendWord((short) (0b01110000_00000000 | dataRegister << 9 | immediateData & 0xFF));
    }

    private MoveqInstruction() {
        super(AddressingModeCategory.IMMEDIATE_DATA, AddressingModeCategory.DATA_REGISTER_DIRECT);
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        if (ea0.isImmediateData() && ea1.isDataRegisterDirect()) {
            final int immediateData = ea0.word1 << 16 | ea0.word2;
            if (immediateData < -0x80 || immediateData > 0x7F) {
                if ((immediateData & 0xFFFFFF00) == 0) {
                    context.addTentativeMessage(new DataForMoveqWillBeSignExtendedWarningMessage());
                } else {
                    context.addTentativeMessage(new DataForMoveqOutOfRangeErrorMessage());
                }
            }

            encode(context, immediateData, ea1.getRegister());
        } else {
            encode(context, 0, 0);
        }
    }

    @Override
    InstructionSize getInstructionSize(M68KAssemblyContext context) {
        InstructionSize size = context.parseIntegerInstructionSize();
        if (size != InstructionSize.DEFAULT && size != InstructionSize.LONG) {
            context.addInvalidSizeAttributeErrorMessage();
        }

        return InstructionSize.LONG;
    }

}
