package org.reasm.m68k.messages;

import org.reasm.AssemblyWarningMessage;

/**
 * A warning message that is generated during an assembly when the two destination registers on a <code>DIVS</code>,
 * <code>DIVSL</code>, <code>DIVU</code> or <code>DIVUL</code> instruction are the same to signal that the remainder will be
 * discarded.
 *
 * @author Francis Gagné
 */
public class DivisionRemainderDiscardedWarningMessage extends AssemblyWarningMessage {

    /**
     * Initializes a new DivisionRemainderDiscardedWarningMessage.
     */
    public DivisionRemainderDiscardedWarningMessage() {
        super("The remainder of this division will be discarded because the two destination registers are the same");
    }

}
