package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * The <code>CMPM</code> instruction.
 *
 * @author Francis Gagné
 */
class CmpmInstruction extends TwoFixedEaInstruction {

    static final CmpmInstruction CMPM = new CmpmInstruction();

    static boolean assembleCmpm(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
            throws IOException {
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
