package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;

/**
 * The <code>DBcc</code> instructions.
 *
 * @author Francis Gagné
 */
@Immutable
class DecrementAndBranchInstruction extends TwoOperandIntegerInstruction {

    @Nonnull
    static final DecrementAndBranchInstruction DBCC = new DecrementAndBranchInstruction(IntegerConditionCode.CC);
    @Nonnull
    static final DecrementAndBranchInstruction DBCS = new DecrementAndBranchInstruction(IntegerConditionCode.CS);
    @Nonnull
    static final DecrementAndBranchInstruction DBEQ = new DecrementAndBranchInstruction(IntegerConditionCode.EQ);
    @Nonnull
    static final DecrementAndBranchInstruction DBF = new DecrementAndBranchInstruction(IntegerConditionCode.F);
    @Nonnull
    static final DecrementAndBranchInstruction DBGE = new DecrementAndBranchInstruction(IntegerConditionCode.GE);
    @Nonnull
    static final DecrementAndBranchInstruction DBGT = new DecrementAndBranchInstruction(IntegerConditionCode.GT);
    @Nonnull
    static final DecrementAndBranchInstruction DBHI = new DecrementAndBranchInstruction(IntegerConditionCode.HI);
    @Nonnull
    static final DecrementAndBranchInstruction DBHS = new DecrementAndBranchInstruction(IntegerConditionCode.HS);
    @Nonnull
    static final DecrementAndBranchInstruction DBLE = new DecrementAndBranchInstruction(IntegerConditionCode.LE);
    @Nonnull
    static final DecrementAndBranchInstruction DBLO = new DecrementAndBranchInstruction(IntegerConditionCode.LO);
    @Nonnull
    static final DecrementAndBranchInstruction DBLS = new DecrementAndBranchInstruction(IntegerConditionCode.LS);
    @Nonnull
    static final DecrementAndBranchInstruction DBLT = new DecrementAndBranchInstruction(IntegerConditionCode.LT);
    @Nonnull
    static final DecrementAndBranchInstruction DBMI = new DecrementAndBranchInstruction(IntegerConditionCode.MI);
    @Nonnull
    static final DecrementAndBranchInstruction DBNE = new DecrementAndBranchInstruction(IntegerConditionCode.NE);
    @Nonnull
    static final DecrementAndBranchInstruction DBPL = new DecrementAndBranchInstruction(IntegerConditionCode.PL);
    @Nonnull
    static final DecrementAndBranchInstruction DBRA = new DecrementAndBranchInstruction(IntegerConditionCode.F);
    @Nonnull
    static final DecrementAndBranchInstruction DBT = new DecrementAndBranchInstruction(IntegerConditionCode.T);
    @Nonnull
    static final DecrementAndBranchInstruction DBVC = new DecrementAndBranchInstruction(IntegerConditionCode.VC);
    @Nonnull
    static final DecrementAndBranchInstruction DBVS = new DecrementAndBranchInstruction(IntegerConditionCode.VS);

    @Nonnull
    private final IntegerConditionCode cc;

    private DecrementAndBranchInstruction(@Nonnull IntegerConditionCode cc) {
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
