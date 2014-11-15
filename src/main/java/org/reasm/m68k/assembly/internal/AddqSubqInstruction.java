package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>ADDQ</code> and <code>SUBQ</code> instructions.
 *
 * @author Francis Gagné
 */
@Immutable
class AddqSubqInstruction extends TwoFixedEaInstruction {

    @Nonnull
    static final AddqSubqInstruction ADDQ = new AddqSubqInstruction(AddAndCmpEorOrSubForms.ADD);
    @Nonnull
    static final AddqSubqInstruction SUBQ = new AddqSubqInstruction(AddAndCmpEorOrSubForms.SUB);

    @Nonnull
    private final AddAndCmpEorOrSubForms.AddSubForms forms;

    private AddqSubqInstruction(@Nonnull AddAndCmpEorOrSubForms.AddSubForms forms) {
        super(AddressingModeCategory.IMMEDIATE_DATA, AddressingModeCategory.ALTERABLE);
        this.forms = forms;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        this.forms.encodeQuick(context, size, ea0, ea1, true);
    }

}
