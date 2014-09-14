package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * The <code>ANDI</code>, <code>EORI</code> and <code>ORI</code> instructions.
 *
 * @author Francis Gagné
 */
class AndiEoriOriInstruction extends TwoFixedEaInstruction {

    static final AndiEoriOriInstruction ANDI = new AndiEoriOriInstruction(AddAndCmpEorOrSubForms.AND);
    static final AndiEoriOriInstruction EORI = new AndiEoriOriInstruction(AddAndCmpEorOrSubForms.EOR);
    static final AndiEoriOriInstruction ORI = new AndiEoriOriInstruction(AddAndCmpEorOrSubForms.OR);

    private final AddAndCmpEorOrSubForms forms;

    private AndiEoriOriInstruction(AddAndCmpEorOrSubForms forms) {
        super(AddressingModeCategory.IMMEDIATE_DATA, AddressingModeCategory.DATA_ALTERABLE);
        this.forms = forms;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        // Check if the destination is CCR or SR.
        if (!this.forms.assembleImmediateToCcrSr(context, size)) {
            super.assemble(context, size);
        }
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        this.forms.encodeImmediate(context, size, ea0, ea1);
    }

}
