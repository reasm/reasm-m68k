package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

import org.reasm.Function;
import org.reasm.ValueVisitor;
import org.reasm.m68k.messages.BranchTargetOutOfRangeErrorMessage;
import org.reasm.m68k.messages.LabelExpectedErrorMessage;

final class BranchLabelValueVisitor implements ValueVisitor<Void> {

    @Nonnull
    private final M68KAssemblyContext context;
    @Nonnull
    private InstructionSize size = InstructionSize.DEFAULT;

    @Nonnull
    InstructionSize outputSize = InstructionSize.DEFAULT;
    int distance;

    BranchLabelValueVisitor(@Nonnull M68KAssemblyContext context) {
        this.context = context;
    }

    @Override
    public Void visitFloat(double value) {
        this.context.addTentativeMessage(new LabelExpectedErrorMessage());
        return null;
    }

    @Override
    public Void visitFunction(Function value) {
        this.context.addTentativeMessage(new LabelExpectedErrorMessage());
        return null;
    }

    @Override
    public Void visitSignedInt(long value) {
        value -= this.context.programCounter + 2;

        switch (this.size) {
        case BYTE:
            if (!(value >= -0x80 && value <= 0x7F)) {
                this.context.addTentativeMessage(new BranchTargetOutOfRangeErrorMessage());
            }

            break;

        case DEFAULT:
        default:
            if (this.context.optimizeUnsizedBranches) {
                final InstructionSize outputSize;
                if (value >= -0x80 && value <= 0x7F && value != 0 && value != -1) {
                    // byte size
                    outputSize = InstructionSize.BYTE;
                } else {
                    final boolean fitsInWord = value >= -0x8000 && value <= 0x7FFF;
                    if (!InstructionSetCheck.MC68020_OR_LATER.isSupported(this.context.instructionSet) || fitsInWord) {
                        if (!fitsInWord) {
                            this.context.addTentativeMessage(new BranchTargetOutOfRangeErrorMessage());
                        }

                        // word size
                        outputSize = InstructionSize.WORD;
                    } else {
                        if (!(value >= -0x80000000 && value <= 0x7FFFFFFF)) {
                            this.context.addTentativeMessage(new BranchTargetOutOfRangeErrorMessage());
                        }

                        // long size
                        outputSize = InstructionSize.LONG;
                    }
                }

                this.outputSize = outputSize;
                break;
            }

            // fall-through

        case WORD:
            if (!(value >= -0x8000 && value <= 0x7FFF)) {
                this.context.addTentativeMessage(new BranchTargetOutOfRangeErrorMessage());
            }

            break;

        case LONG:
            if (!(value >= -0x80000000 && value <= 0x7FFFFFFF)) {
                this.context.addTentativeMessage(new BranchTargetOutOfRangeErrorMessage());
            }

            break;
        }

        this.distance = (int) value;

        return null;
    }

    @Override
    public Void visitString(String value) {
        this.context.addTentativeMessage(new LabelExpectedErrorMessage());
        return null;
    }

    @Override
    public Void visitUndetermined() {
        return null;
    }

    @Override
    public Void visitUnsignedInt(long value) {
        return this.visitSignedInt(value);
    }

    void reset(@Nonnull InstructionSize size) {
        this.size = size;
    }

}
