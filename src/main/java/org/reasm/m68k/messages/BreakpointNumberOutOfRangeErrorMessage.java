package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a <code>BKPT</code> instruction specifies a breakpoint number that is
 * not between 0 and 7.
 *
 * @author Francis Gagné
 */
public class BreakpointNumberOutOfRangeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new BreakpointNumberOutOfRangeErrorMessage.
     */
    public BreakpointNumberOutOfRangeErrorMessage() {
        super("Breakpoint number out of range");
    }

}
