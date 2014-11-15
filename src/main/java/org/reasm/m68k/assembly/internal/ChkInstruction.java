package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>CHK</code> instruction.
 *
 * @author Francis Gagné
 */
@Immutable
class ChkInstruction extends TwoFixedEaInstruction {

    @Nonnull
    static final ChkInstruction CHK = new ChkInstruction();

    private ChkInstruction() {
        super(AddressingModeCategory.DATA, AddressingModeCategory.DATA_REGISTER_DIRECT);
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        final int sizeField;
        switch (size) {
        case BYTE:
            context.addInvalidSizeAttributeErrorMessage();
            //$FALL-THROUGH$

        case WORD:
        case DEFAULT:
        default:
            sizeField = 1 << 7;
            break;

        case LONG:
            checkInstructionSet(InstructionSetCheck.MC68020_OR_LATER, context);
            sizeField = 0;
            break;
        }

        ea0.word0 |= 0b01000001_00000000 | ea1.getRegister() << 9 | sizeField;
        context.appendEffectiveAddress(ea0);
    }

}
