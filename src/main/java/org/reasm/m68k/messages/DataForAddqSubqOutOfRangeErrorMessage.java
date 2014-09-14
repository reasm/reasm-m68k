package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the immediate data for an <code>ADDQ</code> or <code>SUBQ</code>
 * instruction is out of range (between 1 and 8).
 *
 * @author Francis Gagné
 */
public class DataForAddqSubqOutOfRangeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new DataForAddqSubqOutOfRangeErrorMessage.
     */
    public DataForAddqSubqOutOfRangeErrorMessage() {
        super("Immediate data must be between 1 and 8");
    }

}
