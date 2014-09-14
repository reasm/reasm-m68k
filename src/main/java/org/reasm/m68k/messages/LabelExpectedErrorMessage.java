package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the operand of a branch instruction is not a valid label.
 *
 * @author Francis Gagné
 */
public class LabelExpectedErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new LabelExpectedErrorMessage.
     */
    public LabelExpectedErrorMessage() {
        super("Branch instruction requires a label");
    }

}
