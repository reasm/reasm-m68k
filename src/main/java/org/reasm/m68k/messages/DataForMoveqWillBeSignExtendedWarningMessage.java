package org.reasm.m68k.messages;

import org.reasm.AssemblyWarningMessage;

/**
 * A warning message that is generated during an assembly when the immediate data for a <code>MOVEQ</code> instruction is between
 * 128 and 255.
 *
 * @author Francis Gagné
 * @see DataForMoveqOutOfRangeErrorMessage
 */
public class DataForMoveqWillBeSignExtendedWarningMessage extends AssemblyWarningMessage {

    /**
     * Initializes a new DataForMoveqWillBeSignExtendedWarningMessage.
     */
    public DataForMoveqWillBeSignExtendedWarningMessage() {
        super("The immediate data for the MOVEQ instruction will be sign-extended to a 32-bit value");
    }

}
