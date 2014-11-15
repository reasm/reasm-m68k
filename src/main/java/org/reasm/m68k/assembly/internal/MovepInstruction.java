package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.InstructionSet;

/**
 * The <code>MOVEP</code> instruction.
 *
 * @author Francis Gagné
 */
@Immutable
class MovepInstruction extends TwoEaInstruction {

    @Nonnull
    static final MovepInstruction MOVEP = new MovepInstruction();

    @Nonnull
    private static final Set<AddressingMode> ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT = Collections.unmodifiableSet(EnumSet
            .of(AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT));
    @Nonnull
    private static final Set<AddressingMode> DATA_REGISTER_DIRECT_OR_ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT = Collections
            .unmodifiableSet(EnumSet.of(AddressingMode.DATA_REGISTER_DIRECT,
                    AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT));

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        final EffectiveAddress eaOutput;
        final int dataRegister;
        int mode;
        if (ea0.isAddressRegisterIndirectWithDisplacement() && ea1.isDataRegisterDirect()) {
            eaOutput = ea0;
            dataRegister = ea1.getRegister();
            mode = 0;
        } else if (ea0.isDataRegisterDirect() && ea1.isAddressRegisterIndirectWithDisplacement()) {
            eaOutput = ea1;
            dataRegister = ea0.getRegister();
            mode = 1 << 7;
        } else {
            return;
        }

        switch (size) {
        case BYTE:
            context.addInvalidSizeAttributeErrorMessage();
            break;

        case WORD:
        case DEFAULT:
        default:
            break;

        case LONG:
            mode |= 1 << 6;
            break;
        }

        eaOutput.word0 = (short) (0b00000001_00001000 | dataRegister << 9 | mode | eaOutput.getRegister());
        context.appendEffectiveAddress(eaOutput);
    }

    @Override
    Set<AddressingMode> getValidAddressingModesForDestinationOperand(InstructionSet instructionSet, EffectiveAddress ea0) {
        if (ea0.isDataRegisterDirect()) {
            return ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT;
        }

        if (ea0.isAddressRegisterIndirectWithDisplacement()) {
            return AddressingModeCategory.DATA_REGISTER_DIRECT;
        }

        return DATA_REGISTER_DIRECT_OR_ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT;
    }

    @Override
    Set<AddressingMode> getValidAddressingModesForSourceOperand(InstructionSet instructionSet) {
        return DATA_REGISTER_DIRECT_OR_ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT;
    }

}
