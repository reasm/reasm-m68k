package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a MOVEM instruction is encountered that has no operand that can be
 * parsed as a register list.
 *
 * @author Francis Gagné
 */
public class MovemRequiresARegisterListInOneOperandErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new MovemRequiresARegisterListInOneOperandErrorMessage.
     */
    public MovemRequiresARegisterListInOneOperandErrorMessage() {
        super("MOVEM requires a register list in one operand");
    }

}
