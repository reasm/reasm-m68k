package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>ADDA</code>, <code>CMPA</code> and <code>SUBA</code> instructions.
 *
 * @author Francis Gagné
 */
@Immutable
class AddaCmpaSubaInstruction extends TwoFixedEaInstruction {

    @Nonnull
    static final AddaCmpaSubaInstruction ADDA = new AddaCmpaSubaInstruction(AddAndCmpEorOrSubForms.ADD);

    @Nonnull
    static final AddaCmpaSubaInstruction CMPA = new AddaCmpaSubaInstruction(AddAndCmpEorOrSubForms.CMP) {
        @Override
        boolean encodeTst(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
                throws IOException {
            return CmpInstruction.encodeCmpiAsTst(context, size, ea0, ea1);
        };
    };

    @Nonnull
    static final AddaCmpaSubaInstruction SUBA = new AddaCmpaSubaInstruction(AddAndCmpEorOrSubForms.SUB);

    @Nonnull
    private final AddAndCmpEorOrSubForms forms;

    AddaCmpaSubaInstruction(@Nonnull AddAndCmpEorOrSubForms forms) {
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
    boolean encodeTst(@Nonnull M68KAssemblyContext context, @Nonnull InstructionSize size, @Nonnull EffectiveAddress ea0,
            @Nonnull EffectiveAddress ea1) throws IOException {
        return false;
    }

}
