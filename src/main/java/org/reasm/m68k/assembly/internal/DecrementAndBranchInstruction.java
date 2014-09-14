package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.Value;

/**
 * The <code>DBcc</code> instructions.
 *
 * @author Francis Gagné
 */
class DecrementAndBranchInstruction extends TwoOperandIntegerInstruction {

    static final DecrementAndBranchInstruction DBCC = new DecrementAndBranchInstruction(IntegerConditionCode.CC);
    static final DecrementAndBranchInstruction DBCS = new DecrementAndBranchInstruction(IntegerConditionCode.CS);
    static final DecrementAndBranchInstruction DBEQ = new DecrementAndBranchInstruction(IntegerConditionCode.EQ);
    static final DecrementAndBranchInstruction DBF = new DecrementAndBranchInstruction(IntegerConditionCode.F);
    static final DecrementAndBranchInstruction DBGE = new DecrementAndBranchInstruction(IntegerConditionCode.GE);
    static final DecrementAndBranchInstruction DBGT = new DecrementAndBranchInstruction(IntegerConditionCode.GT);
    static final DecrementAndBranchInstruction DBHI = new DecrementAndBranchInstruction(IntegerConditionCode.HI);
    static final DecrementAndBranchInstruction DBHS = new DecrementAndBranchInstruction(IntegerConditionCode.HS);
    static final DecrementAndBranchInstruction DBLE = new DecrementAndBranchInstruction(IntegerConditionCode.LE);
    static final DecrementAndBranchInstruction DBLO = new DecrementAndBranchInstruction(IntegerConditionCode.LO);
    static final DecrementAndBranchInstruction DBLS = new DecrementAndBranchInstruction(IntegerConditionCode.LS);
    static final DecrementAndBranchInstruction DBLT = new DecrementAndBranchInstruction(IntegerConditionCode.LT);
    static final DecrementAndBranchInstruction DBMI = new DecrementAndBranchInstruction(IntegerConditionCode.MI);
    static final DecrementAndBranchInstruction DBNE = new DecrementAndBranchInstruction(IntegerConditionCode.NE);
    static final DecrementAndBranchInstruction DBPL = new DecrementAndBranchInstruction(IntegerConditionCode.PL);
    static final DecrementAndBranchInstruction DBRA = new DecrementAndBranchInstruction(IntegerConditionCode.F);
    static final DecrementAndBranchInstruction DBT = new DecrementAndBranchInstruction(IntegerConditionCode.T);
    static final DecrementAndBranchInstruction DBVC = new DecrementAndBranchInstruction(IntegerConditionCode.VC);
    static final DecrementAndBranchInstruction DBVS = new DecrementAndBranchInstruction(IntegerConditionCode.VS);

    private final IntegerConditionCode cc;

    private DecrementAndBranchInstruction(IntegerConditionCode cc) {
        this.cc = cc;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        if (size != InstructionSize.DEFAULT && size != InstructionSize.WORD) {
            context.addInvalidSizeAttributeErrorMessage();
        }

        // Parse the first operand (loop counter).
        final EffectiveAddress ea = context.ea0;
        context.getEffectiveAddress(context.getOperandText(0), AddressingModeCategory.DATA_REGISTER_DIRECT, InstructionSize.WORD,
                ea);

        // Parse and evaluate the label.
        final Value value = evaluateExpressionOperand(context, 1);
        final BranchLabelValueVisitor visitor = context.branchLabelValueVisitor;
        visitor.reset(size);
        visitor.outputSize = InstructionSize.BYTE;
        visitor.distance = 0;

        Value.accept(value, visitor);

        context.appendWord((short) (0b01010000_11001000 | this.cc.ordinal() << 8 | ea.getRegister()));
        context.appendWord((short) visitor.distance);
    }

}
