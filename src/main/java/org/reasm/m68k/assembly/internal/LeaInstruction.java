package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * The <code>LEA</code> instruction.
 *
 * @author Francis Gagné
 */
class LeaInstruction extends TwoFixedEaInstruction {

    static final LeaInstruction LEA = new LeaInstruction();

    private LeaInstruction() {
        super(AddressingModeCategory.CONTROL, AddressingModeCategory.ADDRESS_REGISTER_DIRECT);
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        ea0.word0 |= ea1.getRegister() << 9 | 0b01000001_11000000;

        if (size != InstructionSize.LONG && size != InstructionSize.DEFAULT) {
            context.addInvalidSizeAttributeErrorMessage();
        }

        context.appendEffectiveAddress(ea0);
    }

}
