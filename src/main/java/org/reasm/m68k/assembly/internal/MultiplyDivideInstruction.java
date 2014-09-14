package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.m68k.messages.AddressingModeNotAllowedHereErrorMessage;
import org.reasm.m68k.messages.DivisionRemainderDiscardedWarningMessage;
import org.reasm.m68k.messages.MultiplicationResultsUndefinedWarningMessage;

/**
 * The <code>DIVS</code>, <code>DIVSL</code>, <code>DIVU</code>, <code>DIVUL</code>, <code>MULS</code> and <code>MULU</code>
 * instructions.
 *
 * @author Francis Gagné
 */
class MultiplyDivideInstruction extends TwoOperandIntegerInstruction {

    private enum Operation {
        MULX, DIVX, DIVXL
    }

    static final MultiplyDivideInstruction DIVS = new MultiplyDivideInstruction(Operation.DIVX, 1);
    static final MultiplyDivideInstruction DIVSL = new MultiplyDivideInstruction(Operation.DIVXL, 1);
    static final MultiplyDivideInstruction DIVU = new MultiplyDivideInstruction(Operation.DIVX, 0);
    static final MultiplyDivideInstruction DIVUL = new MultiplyDivideInstruction(Operation.DIVXL, 0);
    static final MultiplyDivideInstruction MULS = new MultiplyDivideInstruction(Operation.MULX, 1);
    static final MultiplyDivideInstruction MULU = new MultiplyDivideInstruction(Operation.MULX, 0);

    private final Operation operation;
    private final int signed;

    private MultiplyDivideInstruction(Operation operation, int signed) {
        this.operation = operation;
        this.signed = signed;
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        final EffectiveAddress ea = context.ea0;

        // Parse the source operand.
        context.getEffectiveAddress(context.getOperandText(0), AddressingModeCategory.DATA, size, ea);

        // Parse the destination operand.
        boolean errorInDestination = false;
        context.prepareOperandReader(1);
        final LogicalLineReader reader = context.logicalLineReader;

        GeneralPurposeRegister firstRegister = parseRegister(context, reader);
        if (firstRegister == null || firstRegister.compareTo(GeneralPurposeRegister.D7) > 0) {
            errorInDestination = true;
            firstRegister = GeneralPurposeRegister.D0;
        }

        GeneralPurposeRegister secondRegister = null;
        reader.skipWhitespace();
        if (!reader.atEnd() && reader.getCurrentChar() == (this.operation == Operation.MULX ? '-' : ':')) {
            reader.advance();
            reader.skipWhitespace();

            secondRegister = parseRegister(context, reader);
            if (secondRegister == null || secondRegister.compareTo(GeneralPurposeRegister.D7) > 0) {
                errorInDestination = true;
                secondRegister = GeneralPurposeRegister.D0;
            }
        }

        if (!reader.atEnd()) {
            errorInDestination = true;
        }

        if (size == InstructionSize.WORD) {
            if (secondRegister != null) {
                errorInDestination = true;
            }

            ea.word0 |= 0b10000000_11000000 | (this.operation == Operation.MULX ? 1 << 14 : 0) | (firstRegister.ordinal() & 7) << 9
                    | this.signed << 8;
            context.appendEffectiveAddress(ea);

            checkInstructionSet(InstructionSetCheck.M68000_FAMILY, context);
        } else {
            if (secondRegister == null && this.operation == Operation.DIVXL) {
                errorInDestination = true;
            }

            int firstRegisterNumber = firstRegister.ordinal() & 7;
            int secondRegisterNumber = firstRegisterNumber;
            int operationSize = 0;
            if (secondRegister != null) {
                secondRegisterNumber = secondRegister.ordinal() & 7;

                if (this.operation != Operation.DIVXL) {
                    operationSize = 1 << 10;
                }

                if (!errorInDestination && firstRegisterNumber == secondRegisterNumber) {
                    if (this.operation == Operation.MULX) {
                        context.addTentativeMessage(new MultiplicationResultsUndefinedWarningMessage());
                    } else {
                        context.addTentativeMessage(new DivisionRemainderDiscardedWarningMessage());
                    }
                }
            }

            context.appendWord((short) (0b01001100_00000000 | (this.operation == Operation.MULX ? 0 : 1 << 6) | ea.word0));
            context.appendWord((short) (secondRegisterNumber << 12 | this.signed << 11 | operationSize | firstRegisterNumber));
            context.appendEffectiveAddress(ea, 1);

            checkInstructionSet(InstructionSetCheck.CPU32_OR_MC68020_OR_LATER, context);
        }

        if (errorInDestination) {
            context.addTentativeMessage(new AddressingModeNotAllowedHereErrorMessage());
        }
    }

    @Override
    void checkInstructionSet(M68KAssemblyContext context) {
        // Don't check now.
    }

    @Override
    InstructionSize getInstructionSize(M68KAssemblyContext context) {
        InstructionSize size = super.getInstructionSize(context);
        InstructionSize defaultSize;

        if (this.operation == Operation.DIVXL) {
            defaultSize = InstructionSize.LONG;
        } else {
            defaultSize = InstructionSize.WORD;
        }

        if (size == InstructionSize.DEFAULT) {
            size = defaultSize;
        } else if (size != InstructionSize.LONG && (this.operation == Operation.DIVXL || size != InstructionSize.WORD)) {
            context.addInvalidSizeAttributeErrorMessage();
            size = defaultSize;
        }

        return size;
    }

}
