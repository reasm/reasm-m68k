package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.reasm.m68k.InstructionSet;

/**
 * The <code>CMP</code> instruction.
 *
 * @author Francis Gagné
 */
class CmpInstruction extends TwoEaInstruction {

    static final CmpInstruction CMP = new CmpInstruction();

    private static final AddAndCmpEorOrSubForms FORMS = AddAndCmpEorOrSubForms.CMP;

    private static final Set<AddressingMode> DATA_OR_ADDRESS_REGISTER_DIRECT_OR_ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT = Collections
            .unmodifiableSet(EnumSet.of(AddressingMode.DATA_REGISTER_DIRECT, AddressingMode.ADDRESS_REGISTER_DIRECT,
                    AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT));

    static boolean encodeCmpiAsTst(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
            throws IOException {
        if (context.optimizeCmpiToTst) {
            if (ea0.isImmediateData()) {
                // TST only accepts an address register direct as the destination on CPU32 and on MC68020 or later
                if (!ea1.isAddressRegisterDirect()
                        || InstructionSetCheck.CPU32_OR_MC68020_OR_LATER.isSupported(context.instructionSet)) {
                    final int immediateData;

                    switch (size) {
                    case LONG:
                        immediateData = ea0.word1 << 16 | ea0.word2;
                        break;

                    default:
                        immediateData = ea0.word1;
                        break;
                    }

                    if (immediateData == 0) {
                        // Encode as TST
                        OneEaInstruction.TST.encode(context, size, ea1);
                        context.appendEffectiveAddress(ea1);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private CmpInstruction() {
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1) throws IOException {
        if (!CmpmInstruction.assembleCmpm(context, size, ea0, ea1)) {
            if (!encodeCmpiAsTst(context, size, ea0, ea1)) {
                if (!FORMS.encodeBase(context, size, ea0, ea1, true)) {
                    FORMS.encodeImmediate(context, size, ea0, ea1);
                }
            }
        }
    }

    @Override
    Set<AddressingMode> getValidAddressingModesForDestinationOperand(InstructionSet instructionSet, EffectiveAddress ea0) {
        if (ea0.isImmediateData()) {
            return AddiCmpiSubiInstruction.CMPI.getValidAddressingModesForDestinationOperand(instructionSet, ea0);
        }

        if (ea0.isAddressRegisterIndirectWithPostincrement()) {
            return DATA_OR_ADDRESS_REGISTER_DIRECT_OR_ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT;
        }

        return AddressingModeCategory.DATA_OR_ADDRESS_REGISTER_DIRECT;
    }

    @Override
    Set<AddressingMode> getValidAddressingModesForSourceOperand(InstructionSet instructionSet) {
        return AddressingModeCategory.ALL;
    }

}
