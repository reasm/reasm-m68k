package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;

import org.reasm.commons.messages.ValueOutOfRangeErrorMessage;

class BitManipulationInstruction extends TwoOperandIntegerInstruction {

    @Nonnull
    static final BitManipulationInstruction BCHG = new BitManipulationInstruction(0b01 << 6, AddressingModeCategory.DATA_ALTERABLE);
    @Nonnull
    static final BitManipulationInstruction BCLR = new BitManipulationInstruction(0b10 << 6, AddressingModeCategory.DATA_ALTERABLE);
    @Nonnull
    static final BitManipulationInstruction BSET = new BitManipulationInstruction(0b11 << 6, AddressingModeCategory.DATA_ALTERABLE);
    @Nonnull
    static final BitManipulationInstruction BTST = new BitManipulationInstruction(0b00 << 6, AddressingModeCategory.DATA,
            AddressingModeCategory.DATA_EXCEPT_IMMEDIATE_DATA);

    private final int opcode;
    @Nonnull
    private final Set<AddressingMode> validAddressingModesForDestinationDynamicForm;
    @Nonnull
    private final Set<AddressingMode> validAddressingModesForDestinationStaticForm;

    private BitManipulationInstruction(int opcode, @Nonnull Set<AddressingMode> validAddressingModesForDestination) {
        this(opcode, validAddressingModesForDestination, validAddressingModesForDestination);
    }

    private BitManipulationInstruction(int opcode, @Nonnull Set<AddressingMode> validAddressingModesForDestinationDynamicForm,
            @Nonnull Set<AddressingMode> validAddressingModesForDestinationStaticForm) {
        this.opcode = opcode;
        this.validAddressingModesForDestinationDynamicForm = validAddressingModesForDestinationDynamicForm;
        this.validAddressingModesForDestinationStaticForm = validAddressingModesForDestinationStaticForm;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        final EffectiveAddress ea0 = context.ea0;
        final EffectiveAddress ea1 = context.ea1;

        // Parse the source operand.
        context.getEffectiveAddress(context.getOperandText(0), AddressingModeCategory.DATA_REGISTER_DIRECT_OR_IMMEDIATE_DATA,
                InstructionSize.LONG, ea0);

        if (ea0.isDataRegisterDirect()) {
            // Parse the destination operand.
            context.getEffectiveAddress(context.getOperandText(1), this.validAddressingModesForDestinationDynamicForm,
                    InstructionSize.BYTE, ea1);

            // Encode the instruction (dynamic form).
            ea1.word0 |= 0b00000001_00000000 | this.opcode | ea0.getRegister() << 9;
            context.appendEffectiveAddress(ea1);
        } else if (ea0.isImmediateData()) {
            // Parse the destination operand.
            context.getEffectiveAddress(context.getOperandText(1), this.validAddressingModesForDestinationStaticForm,
                    InstructionSize.BYTE, 4, ea1);

            // Encode the instruction (static form).
            ea1.word0 |= 0b00001000_00000000 | this.opcode;
            context.appendWord(ea1.word0);
            context.appendWord((short) (ea0.word2 & 0xFF)); // bit number
            context.appendEffectiveAddress(ea1, 1);

            final int immediateData = ea0.word1 << 16 | ea0.word2;
            if (immediateData < 0 || immediateData > 0xFF) {
                context.addTentativeMessage(new ValueOutOfRangeErrorMessage(immediateData));
            }
        } else {
            return;
        }

        if (size != InstructionSize.DEFAULT) {
            if (size != (ea1.isDataRegisterDirect() ? InstructionSize.LONG : InstructionSize.BYTE)) {
                context.addInvalidSizeAttributeErrorMessage();
            }
        }
    }

}
