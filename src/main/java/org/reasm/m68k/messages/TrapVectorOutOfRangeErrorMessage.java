package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a <code>TRAP</code> instruction specifies a trap vector that is not
 * between 0 and 15.
 *
 * @author Francis Gagné
 */
public class TrapVectorOutOfRangeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new TrapVectorOutOfRangeErrorMessage.
     */
    public TrapVectorOutOfRangeErrorMessage() {
        super("Trap vector out of range");
    }

}
