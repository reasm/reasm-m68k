package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.InstructionSet;
import org.reasm.m68k.messages.BreakpointNumberOutOfRangeErrorMessage;
import org.reasm.m68k.messages.TrapVectorOutOfRangeErrorMessage;

/**
 * Base class for all instructions that take one operand that is an effective address.
 *
 * @author Francis Gagné
 */
@Immutable
abstract class OneEaInstruction extends Instruction {

    @Immutable
    private abstract static class AnySize extends OneEaInstruction {

        @Immutable
        private static class DataAlterable extends AnySize {

            DataAlterable(short fixedBits) {
                super(fixedBits);
            }

            @Override
            Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
                return AddressingModeCategory.DATA_ALTERABLE;
            }

        }

        @Immutable
        private static class Tst extends AnySize {

            Tst() {
                super((short) 0b01001010_00000000);
            }

            @Override
            Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
                if (InstructionSetCheck.CPU32_OR_MC68020_OR_LATER.isSupported(instructionSet)) {
                    return AddressingModeCategory.ALL;
                }

                return AddressingModeCategory.DATA_ALTERABLE;
            }

        }

        private final short fixedBits;

        AnySize(short fixedBits) {
            this.fixedBits = fixedBits;
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            ea.word0 |= this.fixedBits | encodeIntegerSizeStandard(size);

            if (size == InstructionSize.BYTE) {
                context.validateForByteAccess(ea);
            }
        }

    }

    @Immutable
    private static class Ext extends OneEaInstruction {

        Ext() {
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            final int register = ea.isDataRegisterDirect() ? ea.getRegister() : 0;
            ea.word0 = (short) (0b01001000_10000000 | register);

            switch (size) {
            case BYTE:
                context.addInvalidSizeAttributeErrorMessage();
                break;

            case WORD:
            default:
                break;

            case LONG:
                ea.word0 |= 0b00000000_01000000;
                break;
            }
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.DATA_REGISTER_DIRECT;
        }

    }

    @Immutable
    private static class Extb extends OneEaInstruction {

        Extb() {
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            final int register = ea.isDataRegisterDirect() ? ea.getRegister() : 0;
            ea.word0 = (short) (0b01001001_11000000 | register);

            switch (size) {
            case BYTE:
            case WORD:
                context.addInvalidSizeAttributeErrorMessage();
                break;

            case LONG:
            default:
                break;
            }
        }

        @Override
        InstructionSetCheck getInstructionSetCheck() {
            return InstructionSetCheck.CPU32_OR_MC68020_OR_LATER;
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.DATA_REGISTER_DIRECT;
        }

    }

    @Immutable
    private static abstract class Immediate extends OneEaInstruction {

        @Immutable
        private static class Bkpt extends Immediate {

            Bkpt() {
            }

            @Override
            void encode(M68KAssemblyContext context, EffectiveAddress ea, int immediateData) {
                final int breakpointNumber = immediateData;

                if (breakpointNumber < 0 || breakpointNumber > 7) {
                    context.addTentativeMessage(new BreakpointNumberOutOfRangeErrorMessage());
                }

                ea.word0 = (short) (0b01001000_01001000 | breakpointNumber & 7);
                ea.numberOfWords = 1;
            }

            @Override
            InstructionSize getDefaultImmediateSize() {
                // Accept immediate values of any size; encode() does its own bounds checking.
                return InstructionSize.LONG;
            }

            @Override
            InstructionSetCheck getInstructionSetCheck() {
                return InstructionSetCheck.MC68EC000_OR_LATER;
            }

        }

        // M68000PRM says LPSTOP is a word-size instruction, but STOP is unsized.
        // We implement both as unsized instructions.
        @Immutable
        private static class Lpstop extends Immediate {

            Lpstop() {
            }

            @Override
            void encode(M68KAssemblyContext context, EffectiveAddress ea, int immediateData) {
                ea.word0 = (short) 0b11111000_00000000;
                ea.word1 = 0b00000001_11000000;
                ea.word2 = (short) immediateData;
                ea.numberOfWords = 3;
            }

            @Override
            InstructionSetCheck getInstructionSetCheck() {
                return InstructionSetCheck.CPU32_ONLY;
            }

        }

        @Immutable
        private static class Rtd extends Immediate {

            Rtd() {
            }

            @Override
            void encode(M68KAssemblyContext context, EffectiveAddress ea, int immediateData) {
                ea.word0 = 0b01001110_01110100;
                ea.word1 = (short) immediateData;
                ea.numberOfWords = 2;
            }

            @Override
            InstructionSetCheck getInstructionSetCheck() {
                return InstructionSetCheck.MC68010_OR_LATER;
            }

        }

        @Immutable
        private static class Stop extends Immediate {

            Stop() {
            }

            @Override
            void encode(M68KAssemblyContext context, EffectiveAddress ea, int immediateData) {
                ea.word0 = 0b01001110_01110010;
                ea.word1 = (short) immediateData;
                ea.numberOfWords = 2;
            }

            @Override
            InstructionSetCheck getInstructionSetCheck() {
                return InstructionSetCheck.M68000_FAMILY;
            }

        }

        @Immutable
        private static class Trap extends Immediate {

            Trap() {
            }

            @Override
            void encode(M68KAssemblyContext context, EffectiveAddress ea, int immediateData) {
                final int trapVector = immediateData;

                if (trapVector < 0 || trapVector > 15) {
                    context.addTentativeMessage(new TrapVectorOutOfRangeErrorMessage());
                }

                ea.word0 = (short) (0b01001110_01000000 | trapVector & 15);
                ea.numberOfWords = 1;
            }

            @Override
            InstructionSize getDefaultImmediateSize() {
                // Accept immediate values of any size; encode() does its own bounds checking.
                return InstructionSize.LONG;
            }

        }

        Immediate() {
        }

        abstract void encode(@Nonnull M68KAssemblyContext context, @Nonnull EffectiveAddress ea, int immediateData);

        @Override
        final void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            final int immediateData;
            if (ea.isImmediateData()) {
                if (ea.numberOfWords == 2) {
                    immediateData = ea.word1;
                } else {
                    immediateData = ea.word1 << 16 | ea.word2;
                }
            } else {
                immediateData = 0;
            }

            this.encode(context, ea, immediateData);

            if (size != InstructionSize.DEFAULT) {
                context.addInvalidSizeAttributeErrorMessage();
            }
        }

        @Override
        final Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.IMMEDIATE_DATA;
        }

    }

    @Immutable
    private static class Jump extends OneEaInstruction {

        private final int instruction;

        Jump(int instruction) {
            this.instruction = instruction;
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            ea.word0 |= 0b01001110_10000000 | this.instruction;

            if (size != InstructionSize.DEFAULT) {
                context.addInvalidSizeAttributeErrorMessage();
            }
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.CONTROL;
        }

    }

    @Immutable
    private static class Nbcd extends OneEaInstruction {

        Nbcd() {
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            ea.word0 |= 0b01001000_00000000;

            switch (size) {
            case BYTE:
            default:
                break;

            case WORD:
            case LONG:
                context.addInvalidSizeAttributeErrorMessage();
                break;
            }
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.DATA_ALTERABLE;
        }

    }

    @Immutable
    private static class Pea extends OneEaInstruction {

        Pea() {
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            ea.word0 |= 0b01001000_01000000;

            switch (size) {
            case BYTE:
            case WORD:
                context.addInvalidSizeAttributeErrorMessage();
                break;

            case LONG:
            default:
                break;
            }
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.CONTROL;
        }

    }

    @Immutable
    private static class Rtm extends OneEaInstruction {

        Rtm() {
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            ea.word0 |= 0b00000110_11000000;

            if (size != InstructionSize.DEFAULT) {
                context.addInvalidSizeAttributeErrorMessage();
            }
        }

        @Override
        InstructionSetCheck getInstructionSetCheck() {
            return InstructionSetCheck.MC68020_ONLY;
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.DATA_OR_ADDRESS_REGISTER_DIRECT;
        }

    }

    @Immutable
    private static class Scc extends OneEaInstruction {

        @Nonnull
        private final IntegerConditionCode cc;

        Scc(@Nonnull IntegerConditionCode cc) {
            this.cc = cc;
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            ea.word0 |= 0b01010000_11000000 | this.cc.ordinal() << 8;

            switch (size) {
            case BYTE:
            default:
                break;

            case WORD:
            case LONG:
                context.addInvalidSizeAttributeErrorMessage();
                break;
            }
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.DATA_ALTERABLE;
        }

    }

    @Immutable
    private static class Swap extends OneEaInstruction {

        Swap() {
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            final int register = ea.isDataRegisterDirect() ? ea.getRegister() : 0;
            ea.word0 = (short) (0b01001000_01000000 | register);

            switch (size) {
            case BYTE:
            case LONG:
                context.addInvalidSizeAttributeErrorMessage();
                break;

            case WORD:
            default:
                break;
            }
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.DATA_REGISTER_DIRECT;
        }

    }

    @Immutable
    private static class Tas extends OneEaInstruction {

        Tas() {
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            ea.word0 |= 0b01001010_11000000;

            switch (size) {
            case BYTE:
            default:
                break;

            case WORD:
            case LONG:
                context.addInvalidSizeAttributeErrorMessage();
                break;
            }
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.DATA_ALTERABLE;
        }

    }

    @Immutable
    private static class Unlk extends OneEaInstruction {

        Unlk() {
        }

        @Override
        void encode(M68KAssemblyContext context, InstructionSize size, EffectiveAddress ea) {
            ea.word0 |= 0b01001110_01011000;

            if (size != InstructionSize.DEFAULT) {
                context.addInvalidSizeAttributeErrorMessage();
            }
        }

        @Override
        Set<AddressingMode> getValidAddressingModes(InstructionSet instructionSet) {
            return AddressingModeCategory.ADDRESS_REGISTER_DIRECT;
        }

    }

    @Nonnull
    static final OneEaInstruction BKPT = new Immediate.Bkpt();
    @Nonnull
    static final OneEaInstruction CLR = new AnySize.DataAlterable((short) 0b01000010_00000000);
    @Nonnull
    static final OneEaInstruction EXT = new Ext();
    @Nonnull
    static final OneEaInstruction EXTB = new Extb();
    @Nonnull
    static final OneEaInstruction JMP = new Jump(0b00000000_01000000);
    @Nonnull
    static final OneEaInstruction JSR = new Jump(0b00000000_00000000);
    @Nonnull
    static final OneEaInstruction LPSTOP = new Immediate.Lpstop();
    @Nonnull
    static final OneEaInstruction NBCD = new Nbcd();
    @Nonnull
    static final OneEaInstruction NEG = new AnySize.DataAlterable((short) 0b01000100_00000000);
    @Nonnull
    static final OneEaInstruction NEGX = new AnySize.DataAlterable((short) 0b01000000_00000000);
    @Nonnull
    static final OneEaInstruction NOT = new AnySize.DataAlterable((short) 0b01000110_00000000);
    @Nonnull
    static final OneEaInstruction PEA = new Pea();
    @Nonnull
    static final OneEaInstruction RTD = new Immediate.Rtd();
    @Nonnull
    static final OneEaInstruction RTM = new Rtm();
    @Nonnull
    static final OneEaInstruction SCC = new Scc(IntegerConditionCode.CC);
    @Nonnull
    static final OneEaInstruction SCS = new Scc(IntegerConditionCode.CS);
    @Nonnull
    static final OneEaInstruction SEQ = new Scc(IntegerConditionCode.EQ);
    @Nonnull
    static final OneEaInstruction SF = new Scc(IntegerConditionCode.F);
    @Nonnull
    static final OneEaInstruction SGE = new Scc(IntegerConditionCode.GE);
    @Nonnull
    static final OneEaInstruction SGT = new Scc(IntegerConditionCode.GT);
    @Nonnull
    static final OneEaInstruction SHI = new Scc(IntegerConditionCode.HI);
    @Nonnull
    static final OneEaInstruction SHS = new Scc(IntegerConditionCode.HS);
    @Nonnull
    static final OneEaInstruction SLE = new Scc(IntegerConditionCode.LE);
    @Nonnull
    static final OneEaInstruction SLO = new Scc(IntegerConditionCode.LO);
    @Nonnull
    static final OneEaInstruction SLS = new Scc(IntegerConditionCode.LS);
    @Nonnull
    static final OneEaInstruction SLT = new Scc(IntegerConditionCode.LT);
    @Nonnull
    static final OneEaInstruction SMI = new Scc(IntegerConditionCode.MI);
    @Nonnull
    static final OneEaInstruction SNE = new Scc(IntegerConditionCode.NE);
    @Nonnull
    static final OneEaInstruction SPL = new Scc(IntegerConditionCode.PL);
    @Nonnull
    static final OneEaInstruction ST = new Scc(IntegerConditionCode.T);
    @Nonnull
    static final OneEaInstruction STOP = new Immediate.Stop();
    @Nonnull
    static final OneEaInstruction SVC = new Scc(IntegerConditionCode.VC);
    @Nonnull
    static final OneEaInstruction SVS = new Scc(IntegerConditionCode.VS);
    @Nonnull
    static final OneEaInstruction SWAP = new Swap();
    @Nonnull
    static final OneEaInstruction TAS = new Tas();
    @Nonnull
    static final OneEaInstruction TRAP = new Immediate.Trap();
    @Nonnull
    static final OneEaInstruction TST = new AnySize.Tst();
    @Nonnull
    static final OneEaInstruction UNLK = new Unlk();

    @Override
    void assemble2(M68KAssemblyContext context) throws IOException {
        InstructionSize size = context.parseIntegerInstructionSize();
        if (size == InstructionSize.INVALID) {
            context.addInvalidSizeAttributeErrorMessage();
            size = InstructionSize.DEFAULT;
        }

        InstructionSize immediateSize = size;
        if (size == InstructionSize.DEFAULT) {
            immediateSize = this.getDefaultImmediateSize();
        }

        if (context.requireNumberOfOperands(1)) {
            final EffectiveAddress ea = context.ea0;
            context.getEffectiveAddress(context.getOperandText(0), this.getValidAddressingModes(context.instructionSet),
                    immediateSize, ea);
            this.encode(context, size, ea);
            context.appendEffectiveAddress(ea);
        }
    }

    @Override
    void checkInstructionSet(M68KAssemblyContext context) {
        checkInstructionSet(this.getInstructionSetCheck(), context);
    }

    abstract void encode(@Nonnull M68KAssemblyContext context, @Nonnull InstructionSize size, @Nonnull EffectiveAddress ea);

    @Nonnull
    InstructionSize getDefaultImmediateSize() {
        return InstructionSize.WORD;
    }

    @Nonnull
    InstructionSetCheck getInstructionSetCheck() {
        return InstructionSetCheck.M68000_FAMILY;
    }

    @Nonnull
    abstract Set<AddressingMode> getValidAddressingModes(@Nonnull InstructionSet instructionSet);

}
