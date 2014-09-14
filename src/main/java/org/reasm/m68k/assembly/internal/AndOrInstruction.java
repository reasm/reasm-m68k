package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Set;

import org.reasm.m68k.InstructionSet;

/**
 * The <code>AND</code> and <code>OR</code> instructions.
 *
 * @author Francis Gagné
 */
class AndOrInstruction extends TwoEaInstruction {

    static final AndOrInstruction AND = new AndOrInstruction(AddAndCmpEorOrSubForms.AND);
    static final AndOrInstruction OR = new AndOrInstruction(AddAndCmpEorOrSubForms.OR);

    private final AddAndCmpEorOrSubForms forms;

    private AndOrInstruction(AddAndCmpEorOrSubForms forms) {
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
        if (!this.forms.encodeBase(context, size, ea0, ea1, true)) {
            this.forms.encodeImmediate(context, size, ea0, ea1);
        }
    }

    @Override
    Set<AddressingMode> getValidAddressingModesForDestinationOperand(InstructionSet instructionSet, EffectiveAddress ea0) {
        if (ea0.isDataRegisterDirect() || ea0.isImmediateData()) {
            return AddressingModeCategory.DATA_ALTERABLE;
        }

        return AddressingModeCategory.DATA_REGISTER_DIRECT;
    }

    @Override
    Set<AddressingMode> getValidAddressingModesForSourceOperand(InstructionSet instructionSet) {
        return AddressingModeCategory.DATA;
    }

}
