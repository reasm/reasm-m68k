package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.InstructionSet;

/**
 * The <code>ADDI</code>, <code>CMPI</code> and <code>SUBI</code> instructions.
 *
 * @author Francis Gagné
 */
@Immutable
class AddiCmpiSubiInstruction extends TwoEaInstruction {

    @Nonnull
    static final AddiCmpiSubiInstruction ADDI = new AddiCmpiSubiInstruction(AddAndCmpEorOrSubForms.ADD);

    @Nonnull
    static final AddiCmpiSubiInstruction CMPI = new AddiCmpiSubiInstruction(AddAndCmpEorOrSubForms.CMP) {
        @Override
        boolean encodeTst(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
                throws IOException {
            return CmpInstruction.encodeCmpiAsTst(context, size, ea0, ea1);
        }

        @Override
        Set<AddressingMode> getValidAddressingModesForDestinationOperand(InstructionSet instructionSet, EffectiveAddress ea0) {
            if (InstructionSetCheck.CPU32_OR_MC68020_OR_LATER.isSupported(instructionSet)) {
                return AddressingModeCategory.ALL_EXCEPT_IMMEDIATE_DATA;
            }

            return super.getValidAddressingModesForDestinationOperand(instructionSet, ea0);
        }
    };

    @Nonnull
    static final AddiCmpiSubiInstruction SUBI = new AddiCmpiSubiInstruction(AddAndCmpEorOrSubForms.SUB);

    @Nonnull
    private final AddAndCmpEorOrSubForms forms;

    AddiCmpiSubiInstruction(@Nonnull AddAndCmpEorOrSubForms forms) {
        this.forms = forms;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        if (ea0.isImmediateData()) {
            if (!this.forms.encodeQuick(context, size, ea0, ea1)) {
                if (!this.encodeTst(context, size, ea0, ea1)) {
                    // ADDI/CMPI/SUBI don't accept address register direct for the destination.
                    // If the destination is an address register direct, encode as ADDA/CMPA/SUBA.
                    if (ea1.isAddressRegisterDirect()) {
                        this.forms.encodeBase(context, size, ea0, ea1, true);
                    } else {
                        this.forms.encodeImmediate(context, size, ea0, ea1);
                    }
                }
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

    @Override
    Set<AddressingMode> getValidAddressingModesForDestinationOperand(InstructionSet instructionSet, EffectiveAddress ea0) {
        // Overridden by CMPI
        return AddressingModeCategory.ALTERABLE;
    }

    @Override
    Set<AddressingMode> getValidAddressingModesForSourceOperand(InstructionSet instructionSet) {
        return AddressingModeCategory.IMMEDIATE_DATA;
    }

}
