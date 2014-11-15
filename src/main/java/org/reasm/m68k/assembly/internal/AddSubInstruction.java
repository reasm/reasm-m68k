package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>ADD</code> and <code>SUB</code> instructions.
 *
 * @author Francis Gagné
 */
@Immutable
class AddSubInstruction extends TwoOperandIntegerInstruction {

    @Nonnull
    static final AddSubInstruction ADD = new AddSubInstruction(AddAndCmpEorOrSubForms.ADD);
    @Nonnull
    static final AddSubInstruction SUB = new AddSubInstruction(AddAndCmpEorOrSubForms.SUB);

    @Nonnull
    private final AddAndCmpEorOrSubForms forms;

    private AddSubInstruction(@Nonnull AddAndCmpEorOrSubForms forms) {
        this.forms = forms;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        final EffectiveAddress ea0 = context.ea0;
        final EffectiveAddress ea1 = context.ea1;

        // Parse the source operand.
        context.getEffectiveAddress(context.getOperandText(0), AddressingModeCategory.ALL, size, ea0);

        // Parse the destination operand.
        final Set<AddressingMode> validAddressingModesForDestinationOperand;
        if (ea0.isDataRegisterDirect() || ea0.isImmediateData()) {
            validAddressingModesForDestinationOperand = AddressingModeCategory.ALTERABLE;
        } else {
            validAddressingModesForDestinationOperand = AddressingModeCategory.DATA_OR_ADDRESS_REGISTER_DIRECT;
        }

        context.getEffectiveAddress(context.getOperandText(1), validAddressingModesForDestinationOperand, size,
                ea0.numberOfWords * 2, ea1);

        if (!this.forms.encodeQuick(context, size, ea0, ea1)) {
            if (!this.forms.encodeBase(context, size, ea0, ea1, true)) {
                this.forms.encodeImmediate(context, size, ea0, ea1);
            }
        }
    }
}
