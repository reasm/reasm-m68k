package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * The <code>MOVEA</code> instruction.
 *
 * @author Francis Gagné
 */
class MoveaInstruction extends TwoOperandIntegerInstruction {

    static final MoveaInstruction MOVEA = new MoveaInstruction();

    private MoveaInstruction() {
    }

    @Override
    void assemble(M68KAssemblyContext context, InstructionSize size) throws IOException {
        MoveInstruction.assembleBasicMove(context, size, AddressingModeCategory.ADDRESS_REGISTER_DIRECT);
    }

}
