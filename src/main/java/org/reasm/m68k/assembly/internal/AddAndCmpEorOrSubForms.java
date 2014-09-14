package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.m68k.ConfigurationOptions;
import org.reasm.m68k.messages.DataForAddqSubqOutOfRangeErrorMessage;

/**
 * Contains the encoding data and logic for the different forms of the <code>ADD</code>, <code>AND</code>, <code>CMP</code>
 * (excluding <code>CMPM</code>), <code>EOR</code>, <code>OR</code> and <code>SUB</code> instructions.
 *
 * @author Francis Gagné
 */
class AddAndCmpEorOrSubForms {

    /**
     * Extends {@link AddAndCmpEorOrSubForms} to add the data and logic for the quick form of the <code>ADD</code> and
     * <code>SUB</code> instructions.
     *
     * @author Francis Gagné
     */
    static final class AddSubForms extends AddAndCmpEorOrSubForms {

        private final int quickOpcode;

        /**
         * Initializes a new AddSubForms.
         *
         * @param baseOpcode
         *            the fixed bits of the base form's opcode
         * @param immediateOpcode
         *            the fixed bits of the immediate form's opcode
         * @param quickOpcode
         *            the fixed bits of the quick form's opcode that are specific to the instruction
         */
        AddSubForms(int baseOpcode, int immediateOpcode, int quickOpcode) {
            super(baseOpcode, immediateOpcode);
            this.quickOpcode = 0b0101 << 12 | quickOpcode;
        }

        @Override
        final boolean encodeQuick(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
                throws IOException {
            return this.encodeQuick(context, size, ea0, ea1, false);
        }

        /**
         * Encodes the quick form of an <code>ADD</code>, <code>ADDA</code> or <code>ADDI</code> (<code>ADDQ</code>) or
         * <code>SUB</code> , <code>SUBA</code> or <code>SUBI</code> (<code>SUBQ</code>) instruction, if the
         * {@link ConfigurationOptions#OPTIMIZE_TO_ADDQ_SUBQ} configuration option is enabled or if <code>force</code> is
         * <code>true</code>.
         *
         * @param context
         *            the assembly context
         * @param size
         *            the instruction size
         * @param ea0
         *            the source operand
         * @param ea1
         *            the destination operand
         * @param force
         *            encode the quick form even if the immediate data is out of range (add an error in that case)
         * @return <code>true</code> if the instruction could be encoded in the quick form, otherwise <code>false</code>
         * @throws IOException
         *             an I/O exception occurred while encoding the instruction
         */
        final boolean encodeQuick(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1,
                boolean force) throws IOException {
            if (ea0.isImmediateData() && (context.optimizeToAddqSubq || force)) {
                final int immediateData;

                switch (size) {
                case LONG:
                    immediateData = ea0.word1 << 16 | ea0.word2;
                    break;

                default:
                    immediateData = ea0.word1;
                    break;
                }

                if (!(immediateData >= 1 && immediateData <= 8)) {
                    if (!force) {
                        return false;
                    }

                    context.addTentativeMessage(new DataForAddqSubqOutOfRangeErrorMessage());
                }

                // AND the immediate data with 7 to turn 8 into 0.
                ea1.word0 |= (short) (this.quickOpcode | (immediateData & 7) << 9) | Mnemonic.encodeIntegerSizeStandard(size);

                if (size == InstructionSize.BYTE) {
                    context.validateForByteAccess(ea1);
                }

                context.appendEffectiveAddress(ea1);
                return true;
            }

            return false;
        }

    }

    static final AddSubForms ADD = new AddSubForms(0b1101 << 12, 0b00000110 << 8, 0b0 << 8);
    static final AddAndCmpEorOrSubForms AND = new AddAndCmpEorOrSubForms(0b1100 << 12, 0b00000010 << 8);
    static final AddAndCmpEorOrSubForms CMP = new AddAndCmpEorOrSubForms(0b1011 << 12, 0b00001100 << 8);
    static final AddAndCmpEorOrSubForms EOR = new AddAndCmpEorOrSubForms(0b1011 << 12 | 1 << 8, 0b00001010 << 8);
    static final AddAndCmpEorOrSubForms OR = new AddAndCmpEorOrSubForms(0b1000 << 12, 0b00000000 << 8);
    static final AddSubForms SUB = new AddSubForms(0b1001 << 12, 0b00000100 << 8, 0b1 << 8);

    private final int baseOpcode;
    private final int immediateOpcode;

    /**
     * Initializes a new AddAndCmpEorOrSubForms.
     *
     * @param baseOpcode
     *            the fixed bits of the base form's opcode
     * @param immediateOpcode
     *            the fixed bits of the immediate form's opcode
     */
    AddAndCmpEorOrSubForms(int baseOpcode, int immediateOpcode) {
        this.baseOpcode = baseOpcode;
        this.immediateOpcode = immediateOpcode;
    }

    /**
     * Assembles an <code>ANDI</code> to <code>CCR</code>, <code>ANDI</code> to <code>SR</code>, <code>EORI</code> to
     * <code>CCR</code>, <code>EORI</code> to <code>SR</code>, <code>ORI</code> to <code>CCR</code> or <code>ORI</code> to
     * <code>SR</code> instruction.
     *
     * @param context
     *            the assembly context
     * @param size
     *            the instruction size
     * @return <code>true</code> if the destination is <code>CCR</code> or <code>SR</code> and the instruction was assembled,
     *         otherwise <code>false</code>
     * @throws IOException
     *             an I/O exception occurred while encoding the instruction
     */
    boolean assembleImmediateToCcrSr(M68KAssemblyContext context, InstructionSize size) throws IOException {
        boolean isCcr = false;
        if (Mnemonic.parseSpecialRegister(context, 1, "SR") || (isCcr = Mnemonic.parseSpecialRegister(context, 1, "CCR"))) {
            final InstructionSize validSize = isCcr ? InstructionSize.BYTE : InstructionSize.WORD;
            if (size != InstructionSize.DEFAULT && size != validSize) {
                context.addInvalidSizeAttributeErrorMessage();
            }

            // Parse the source operand.
            final EffectiveAddress ea = context.ea0;
            context.getEffectiveAddress(context.getOperandText(0), AddressingModeCategory.IMMEDIATE_DATA, validSize, ea);

            if (ea.isImmediateData()) {
                ea.word0 = (short) (this.immediateOpcode | (isCcr ? 0 : 1 << 6) | EffectiveAddress.EA_IMMEDIATE_DATA);
                context.appendEffectiveAddress(ea);
            }

            return true;
        }

        return false;
    }

    /**
     * Encodes the base form of an <code>ADD</code>, <code>ADDA</code>, <code>AND</code>, <code>CMP</code>, <code>CMPA</code>,
     * <code>EOR</code>, <code>OR</code>, <code>SUB</code> or <code>SUBA</code> instruction.
     *
     * @param context
     *            the assembly context
     * @param size
     *            the instruction size
     * @param ea0
     *            the source operand
     * @param ea1
     *            the destination operand
     * @param allowMemoryToRegister
     *            <code>true</code> to allow the memory-to-register encoding form, or <code>false</code> to disallow it
     * @return <code>true</code> if the instruction could be encoded in the base form, otherwise <code>false</code>
     * @throws IOException
     *             an I/O exception occurred while encoding the instruction
     */
    boolean encodeBase(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1,
            boolean allowMemoryToRegister) throws IOException {
        final EffectiveAddress eaOutput;
        if (allowMemoryToRegister && (ea1.isDataRegisterDirect() || ea1.isAddressRegisterDirect())) {
            eaOutput = ea0;
            eaOutput.word0 |= this.baseOpcode | ea1.getRegister() << 9;

            // Encode the opmode (which includes the instruction size).
            if (ea1.isAddressRegisterDirect()) {
                eaOutput.word0 |= 0b011 << 6;

                switch (size) {
                case BYTE:
                    // Byte size is invalid when address register direct is used for the destination.
                    context.addInvalidSizeAttributeErrorMessage();
                    break;

                case WORD:
                case DEFAULT:
                default:
                    break;

                case LONG:
                    eaOutput.word0 |= 0b100 << 6;
                    break;
                }
            } else {
                eaOutput.word0 |= Mnemonic.encodeIntegerSizeStandard(size);

                if (size == InstructionSize.BYTE) {
                    // Byte size is invalid when address register direct is used for the source.
                    context.validateForByteAccess(ea0);
                }
            }
        } else if (ea0.isDataRegisterDirect()) {
            eaOutput = ea1;
            eaOutput.word0 |= this.baseOpcode | ea0.getRegister() << 9 | 0b100 << 6 | Mnemonic.encodeIntegerSizeStandard(size);
        } else {
            // Invalid operands: do not encode.
            return false;
        }

        context.appendEffectiveAddress(eaOutput);
        return true;
    }

    /**
     * Encodes the immediate form of an <code>ADD</code> (<code>ADDI</code>), <code>AND</code> (<code>ANDI</code>), <code>CMP</code>
     * (<code>CMPI</code>), <code>EOR</code> (<code>EORI</code>), <code>OR</code> (<code>ORI</code>) or <code>SUB</code> (
     * <code>SUBI</code>) instruction.
     *
     * @param context
     *            the assembly context
     * @param size
     *            the instruction size
     * @param ea0
     *            the source operand
     * @param ea1
     *            the destination operand
     * @return <code>true</code> if the instruction could be encoded in the immediate form, otherwise <code>false</code>
     * @throws IOException
     *             an I/O exception occurred while encoding the instruction
     */
    boolean encodeImmediate(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
            throws IOException {
        if (ea0.isImmediateData()) {
            // ea0 contains the immediate operand. ea1 contains the destination.
            ea0.word0 = ea1.word0;
            ea0.word0 |= (short) this.immediateOpcode | Mnemonic.encodeIntegerSizeStandard(size);

            context.appendEffectiveAddress(ea0);
            context.appendEffectiveAddress(ea1, 1);
            return true;
        }

        return false;
    }

    /**
     * Encodes the quick form of an <code>ADD</code>, <code>ADDA</code> or <code>ADDI</code> (<code>ADDQ</code>) or <code>SUB</code>
     * , <code>SUBA</code> or <code>SUBI</code> (<code>SUBQ</code>) instruction, if the
     * {@link ConfigurationOptions#OPTIMIZE_TO_ADDQ_SUBQ} configuration option is enabled or if <code>force</code> is
     * <code>true</code>.
     *
     * @param context
     *            the assembly context
     * @param size
     *            the instruction size
     * @param ea0
     *            the source operand
     * @param ea1
     *            the destination operand
     * @return <code>true</code> if the instruction could be encoded in the quick form, otherwise <code>false</code>
     * @throws IOException
     *             an I/O exception occurred while encoding the instruction
     */
    boolean encodeQuick(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea0, EffectiveAddress ea1)
            throws IOException {
        return false;
    }

}
