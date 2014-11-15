package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>CMPM</code> instruction.
 *
 * @author Francis Gagné
 */
@Immutable
class CmpmInstruction extends TwoFixedEaInstruction {

    @Nonnull
    static final CmpmInstruction CMPM = new CmpmInstruction();

    static boolean assembleCmpm(@Nonnull M68KAssemblyContext context, @Nonnull InstructionSize size, @Nonnull EffectiveAddress ea0,
            @Nonnull EffectiveAddress ea1) throws IOException {
        if (ea0.isAddressRegisterIndirectWithPostincrement() && ea1.isAddressRegisterIndirectWithPostincrement()) {
            // The encoding for CMPM fills the "holes" left by the missing "EOR Dn,An" instruction.
            ea0.word0 = (short) (EffectiveAddress.MODE_ADDRESS_REGISTER_DIRECT | ea0.getRegister());
            ea1.word0 = (short) (EffectiveAddress.MODE_DATA_REGISTER_DIRECT | ea1.getRegister());
            AddAndCmpEorOrSubForms.EOR.encodeBase(context, size, ea0, ea1, true);
            return true;
        }

        return false;
    }

    private CmpmInstruction() {
        super(AddressingModeCategory.ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT,
                AddressingModeCategory.ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT);
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        assembleCmpm(context, size, ea0, ea1);
    }

}
