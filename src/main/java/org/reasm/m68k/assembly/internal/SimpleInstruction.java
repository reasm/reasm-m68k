package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Instruction that takes no operands and is unsized.
 *
 * @author Francis Gagné
 */
@Immutable
class SimpleInstruction extends Instruction {

    @Nonnull
    static final SimpleInstruction BGND = new SimpleInstruction(0x4AFA, InstructionSetCheck.CPU32_ONLY);
    @Nonnull
    static final SimpleInstruction ILLEGAL = new SimpleInstruction(0x4AFC, InstructionSetCheck.M68000_FAMILY);
    @Nonnull
    static final SimpleInstruction NOP = new SimpleInstruction(0x4E71, InstructionSetCheck.M68000_FAMILY);
    @Nonnull
    static final SimpleInstruction RESET = new SimpleInstruction(0x4E70, InstructionSetCheck.M68000_FAMILY);
    @Nonnull
    static final SimpleInstruction RTE = new SimpleInstruction(0x4E73, InstructionSetCheck.M68000_FAMILY);
    @Nonnull
    static final SimpleInstruction RTR = new SimpleInstruction(0x4E77, InstructionSetCheck.M68000_FAMILY);
    @Nonnull
    static final SimpleInstruction RTS = new SimpleInstruction(0x4E75, InstructionSetCheck.M68000_FAMILY);
    @Nonnull
    static final SimpleInstruction TRAPV = new SimpleInstruction(0x4E76, InstructionSetCheck.M68000_FAMILY);

    private final int opcode;
    @Nonnull
    private final InstructionSetCheck instructionSetCheck;

    private SimpleInstruction(int opcode, @Nonnull InstructionSetCheck instructionSetCheck) {
        this.opcode = opcode;
        this.instructionSetCheck = instructionSetCheck;
    }

    @Override
    void assemble2(M68KAssemblyContext context) throws IOException {
        if (context.attribute != null) {
            context.addInvalidSizeAttributeErrorMessage();
        }

        context.requireNumberOfOperands(0);
        context.appendWord((short) this.opcode);
    }

    @Override
    void checkInstructionSet(M68KAssemblyContext context) {
        checkInstructionSet(this.instructionSetCheck, context);
    }

}
