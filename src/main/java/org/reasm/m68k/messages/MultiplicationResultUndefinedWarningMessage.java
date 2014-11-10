package org.reasm.m68k.messages;

import org.reasm.AssemblyWarningMessage;

/**
 * A warning message that is generated during an assembly when the two destination registers on a <code>MULS</code> or
 * <code>MULU</code> instruction are the same to signal that the result is undefined.
 *
 * @author Francis Gagné
 */
public class MultiplicationResultUndefinedWarningMessage extends AssemblyWarningMessage {

    /**
     * Initializes a new MultiplicationResultUndefinedWarningMessage.
     */
    public MultiplicationResultUndefinedWarningMessage() {
        super("The result of this multiplication is undefined because the two destination registers are the same");
    }

}
