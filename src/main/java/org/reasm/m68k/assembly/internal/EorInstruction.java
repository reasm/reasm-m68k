package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>EOR</code> instruction.
 *
 * @author Francis Gagné
 */
@Immutable
class EorInstruction extends TwoFixedEaInstruction {

    @Nonnull
    static final EorInstruction EOR = new EorInstruction();

    @Nonnull
    private static final AddAndCmpEorOrSubForms FORMS = AddAndCmpEorOrSubForms.EOR;

    private EorInstruction() {
        super(AddressingModeCategory.DATA_REGISTER_DIRECT_OR_IMMEDIATE_DATA, AddressingModeCategory.DATA_ALTERABLE);
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        // Check if the destination is CCR or SR.
        if (!FORMS.assembleImmediateToCcrSr(context, size)) {
            super.assemble(context, size);
        }
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        if (!FORMS.encodeImmediate(context, size, ea0, ea1)) {
            FORMS.encodeBase(context, size, ea0, ea1, false);
        }
    }

}
