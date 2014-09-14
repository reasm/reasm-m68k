package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * The <code>LINK</code> instruction.
 *
 * @author Francis Gagné
 */
class LinkInstruction extends TwoFixedEaInstruction {

    static final LinkInstruction LINK = new LinkInstruction();

    private LinkInstruction() {
        super(AddressingModeCategory.ADDRESS_REGISTER_DIRECT, AddressingModeCategory.IMMEDIATE_DATA);
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        switch (size) {
        case BYTE:
            context.addInvalidSizeAttributeErrorMessage();
            //$FALL-THROUGH$

        case WORD:
        case DEFAULT:
        default:
            context.appendWord((short) (0b01001110_01010000 | ea0.getRegister()));
            context.appendWord(ea1.word1);
            break;

        case LONG:
            checkInstructionSet(InstructionSetCheck.CPU32_OR_MC68020_OR_LATER, context);

            context.appendWord((short) (0b01001000_00001000 | ea0.getRegister()));
            context.appendWord(ea1.word1);
            context.appendWord(ea1.word2);
            break;
        }
    }

}
