package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * The <code>ADDQ</code> and <code>SUBQ</code> instructions.
 *
 * @author Francis Gagné
 */
class AddqSubqInstruction extends TwoFixedEaInstruction {

    static final AddqSubqInstruction ADDQ = new AddqSubqInstruction(AddAndCmpEorOrSubForms.ADD);
    static final AddqSubqInstruction SUBQ = new AddqSubqInstruction(AddAndCmpEorOrSubForms.SUB);

    private final AddAndCmpEorOrSubForms.AddSubForms forms;

    private AddqSubqInstruction(AddAndCmpEorOrSubForms.AddSubForms forms) {
        super(AddressingModeCategory.IMMEDIATE_DATA, AddressingModeCategory.ALTERABLE);
        this.forms = forms;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        this.forms.encodeQuick(context, size, ea0, ea1, true);
    }

}
