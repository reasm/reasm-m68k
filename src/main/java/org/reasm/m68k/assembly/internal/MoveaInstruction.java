package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>MOVEA</code> instruction.
 *
 * @author Francis Gagné
 */
@Immutable
class MoveaInstruction extends TwoOperandIntegerInstruction {

    @Nonnull
    static final MoveaInstruction MOVEA = new MoveaInstruction();

    private MoveaInstruction() {
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        MoveInstruction.assembleBasicMove(context, size, AddressingModeCategory.ADDRESS_REGISTER_DIRECT);
    }

}
