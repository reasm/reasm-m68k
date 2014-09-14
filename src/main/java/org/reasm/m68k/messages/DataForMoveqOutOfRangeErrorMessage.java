package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the immediate data for a <code>MOVEQ</code> instruction is out of
 * range (between -128 and 255).
 *
 * @author Francis Gagné
 * @see DataForMoveqWillBeSignExtendedWarningMessage
 */
public class DataForMoveqOutOfRangeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new DataForMoveqOutOfRangeErrorMessage.
     */
    public DataForMoveqOutOfRangeErrorMessage() {
        super("The immediate data for the MOVEQ instruction is out of range");
    }

}
