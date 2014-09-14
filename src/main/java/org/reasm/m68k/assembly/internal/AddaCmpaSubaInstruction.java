package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * The <code>ADDA</code>, <code>CMPA</code> and <code>SUBA</code> instructions.
 *
 * @author Francis Gagné
 */
class AddaCmpaSubaInstruction extends TwoFixedEaInstruction {

    static final AddaCmpaSubaInstruction ADDA = new AddaCmpaSubaInstruction(AddAndCmpEorOrSubForms.ADD);

    static final AddaCmpaSubaInstruction CMPA = new AddaCmpaSubaInstruction(AddAndCmpEorOrSubForms.CMP) {
        @Override
        boolean encodeTst(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
                throws IOException {
            return CmpInstruction.encodeCmpiAsTst(context, size, ea0, ea1);
        };
    };

    static final AddaCmpaSubaInstruction SUBA = new AddaCmpaSubaInstruction(AddAndCmpEorOrSubForms.SUB);

    private final AddAndCmpEorOrSubForms forms;

    AddaCmpaSubaInstruction(AddAndCmpEorOrSubForms forms) {
        super(AddressingModeCategory.ALL, AddressingModeCategory.ADDRESS_REGISTER_DIRECT);
        this.forms = forms;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        if (!this.forms.encodeQuick(context, size, ea0, ea1)) {
            if (!this.encodeTst(context, size, ea0, ea1)) {
                this.forms.encodeBase(context, size, ea0, ea1, true);
            }
        }
    }

    /**
     * Encodes the instruction as a <code>TST</code> instruction, if possible.
     *
     * @param context
     *            the {@link M68KAssemblyContext}
     * @param size
     *            the instruction size
     * @param ea0
     *            the source operand
     * @param ea1
     *            the destination operand
     * @return <code>true</code> if the instruction has encoded as a <code>TST</code> instruction, otherwise <code>false</code>
     * @throws IOException
     *             an I/O exception occurred
     */
    boolean encodeTst(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
            throws IOException {
        return false;
    }

}
