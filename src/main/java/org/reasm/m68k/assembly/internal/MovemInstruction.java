package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Set;

import org.reasm.m68k.messages.MovemRequiresARegisterListInExactlyOneOperandErrorMessage;

/**
 * The <code>MOVEM</code> instruction.
 *
 * @author Francis Gagné
 */
class MovemInstruction extends TwoOperandIntegerInstruction {

    static final MovemInstruction MOVEM = new MovemInstruction();

    private MovemInstruction() {
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        if (size == InstructionSize.BYTE) {
            context.addInvalidSizeAttributeErrorMessage();
            size = InstructionSize.DEFAULT;
        }

        final EffectiveAddress ea = context.ea0;
        final Set<GeneralPurposeRegister> registerListLeft = parseRegisterList(context, 0);
        final Set<GeneralPurposeRegister> registerListRight = parseRegisterList(context, 1);
        final Set<GeneralPurposeRegister> registerList;
        final int direction;
        if (registerListLeft != null) {
            if (registerListRight != null) {
                context.addMessage(new MovemRequiresARegisterListInExactlyOneOperandErrorMessage());
                return;
            }

            context.getEffectiveAddress(context.getOperandText(1), AddressingModeCategory.CONTROL_ALTERABLE_WITH_PREDECREMENT,
                    size, 4, ea);
            registerList = registerListLeft;
            direction = 0;
        } else {
            if (registerListRight == null) {
                context.addMessage(new MovemRequiresARegisterListInExactlyOneOperandErrorMessage());
                return;
            }

            context.getEffectiveAddress(context.getOperandText(0), AddressingModeCategory.CONTROL_WITH_POSTINCREMENT, size, 4, ea);
            registerList = registerListRight;
            direction = 1 << 10;
        }

        ea.word0 |= 0b01001000_10000000 | direction | (size == InstructionSize.LONG ? 1 << 6 : 0);

        short registerMask = 0;
        if (ea.isAddressRegisterIndirectWithPredecrement()) {
            for (GeneralPurposeRegister register : registerList) {
                registerMask |= 1 << 15 - register.ordinal();
            }
        } else {
            for (GeneralPurposeRegister register : registerList) {
                registerMask |= 1 << register.ordinal();
            }
        }

        context.appendWord(ea.word0);
        context.appendWord(registerMask);
        context.appendEffectiveAddress(ea, 1);
    }

}
