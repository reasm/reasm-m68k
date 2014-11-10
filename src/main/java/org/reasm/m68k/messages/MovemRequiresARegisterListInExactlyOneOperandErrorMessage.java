package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a <code>MOVEM</code> instruction is encountered in which either no
 * operand can be parsed as a register list or both operands can be parsed as a register list.
 *
 * @author Francis Gagné
 */
public class MovemRequiresARegisterListInExactlyOneOperandErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new MovemRequiresARegisterListInExactlyOneOperandErrorMessage.
     */
    public MovemRequiresARegisterListInExactlyOneOperandErrorMessage() {
        super("MOVEM requires a register list in exactly one operand");
    }

}
