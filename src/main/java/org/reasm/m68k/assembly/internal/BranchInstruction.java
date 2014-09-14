package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.Value;
import org.reasm.m68k.messages.MinusOneDistanceShortBranchErrorMessage;
import org.reasm.m68k.messages.ZeroDistanceShortBranchErrorMessage;

/**
 * The <code>Bcc</code> instructions.
 *
 * @author Francis Gagné
 */
class BranchInstruction extends Instruction {

    static final BranchInstruction BCC = new BranchInstruction(IntegerConditionCode.CC);
    static final BranchInstruction BCS = new BranchInstruction(IntegerConditionCode.CS);
    static final BranchInstruction BEQ = new BranchInstruction(IntegerConditionCode.EQ);
    static final BranchInstruction BGE = new BranchInstruction(IntegerConditionCode.GE);
    static final BranchInstruction BGT = new BranchInstruction(IntegerConditionCode.GT);
    static final BranchInstruction BHI = new BranchInstruction(IntegerConditionCode.HI);
    static final BranchInstruction BHS = new BranchInstruction(IntegerConditionCode.HS);
    static final BranchInstruction BLE = new BranchInstruction(IntegerConditionCode.LE);
    static final BranchInstruction BLO = new BranchInstruction(IntegerConditionCode.LO);
    static final BranchInstruction BLS = new BranchInstruction(IntegerConditionCode.LS);
    static final BranchInstruction BLT = new BranchInstruction(IntegerConditionCode.LT);
    static final BranchInstruction BMI = new BranchInstruction(IntegerConditionCode.MI);
    static final BranchInstruction BNE = new BranchInstruction(IntegerConditionCode.NE);
    static final BranchInstruction BPL = new BranchInstruction(IntegerConditionCode.PL);
    static final BranchInstruction BRA = new BranchInstruction(IntegerConditionCode.T);
    static final BranchInstruction BSR = new BranchInstruction(IntegerConditionCode.F);
    static final BranchInstruction BVC = new BranchInstruction(IntegerConditionCode.VC);
    static final BranchInstruction BVS = new BranchInstruction(IntegerConditionCode.VS);

    private final IntegerConditionCode cc;

    private BranchInstruction(IntegerConditionCode cc) {
        this.cc = cc;
    }

    @Override
    void assemble2(final M68KAssemblyContext context) throws IOException {
        InstructionSize size = context.parseIntegerInstructionSize();
        if (size == InstructionSize.INVALID) {
            if ("S".equalsIgnoreCase(context.attribute)) {
                size = InstructionSize.BYTE;
            } else {
                context.addInvalidSizeAttributeErrorMessage();
                size = InstructionSize.DEFAULT;
            }
        }

        if (context.requireNumberOfOperands(1)) {
            final Value value = evaluateExpressionOperand(context, 0);
            final BranchLabelValueVisitor visitor = context.branchLabelValueVisitor;
            visitor.reset(size);
            short leadWord = (short) (0b01100000_00000000 | this.cc.ordinal() << 8);
            final InstructionSize outputSize;
            switch (size) {
            case DEFAULT:
            default:
                if (context.optimizeUnsizedBranches) {
                    outputSize = InstructionSize.BYTE;
                } else {
                    outputSize = InstructionSize.WORD;
                }

                break;

            case BYTE:
                outputSize = InstructionSize.BYTE;
                break;

            case WORD:
                outputSize = InstructionSize.WORD;
                break;

            case LONG:
                outputSize = InstructionSize.LONG;
                break;
            }

            visitor.outputSize = outputSize;

            // By default, generate a branch to self (BRA.B *-2).
            // For a byte-sized branch, putting 0 in the lower byte would turn the instruction into a word-sized branch (BRA.W).
            visitor.distance = -2;

            Value.accept(value, visitor);

            switch (visitor.outputSize) {
            case BYTE:
                short distance = (short) (visitor.distance & 0x00FF);
                if (distance == 0) {
                    context.addTentativeMessage(new ZeroDistanceShortBranchErrorMessage());
                    distance = 0x00FE;
                } else if (distance == 0x00FF && InstructionSetCheck.MC68020_OR_LATER.isSupported(context.instructionSet)) {
                    context.addTentativeMessage(new MinusOneDistanceShortBranchErrorMessage());
                    distance = 0x00FE;
                }

                leadWord |= distance;
                context.appendWord(leadWord);
                checkInstructionSet(InstructionSetCheck.M68000_FAMILY, context);
                break;

            case WORD:
            default:
                context.appendWord(leadWord);
                context.appendWord((short) visitor.distance);
                checkInstructionSet(InstructionSetCheck.M68000_FAMILY, context);
                break;

            case LONG:
                leadWord |= 0xFF;
                context.appendWord(leadWord);
                context.appendLong(visitor.distance);
                checkInstructionSet(InstructionSetCheck.MC68020_OR_LATER, context);
                break;
            }
        }
    }

    @Override
    void checkInstructionSet(M68KAssemblyContext context) {
        // Don't check here: depends on the effective instruction size.
    }

}
