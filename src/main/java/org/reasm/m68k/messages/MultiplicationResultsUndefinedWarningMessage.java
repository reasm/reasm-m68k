package org.reasm.m68k.messages;

import org.reasm.AssemblyWarningMessage;

/**
 * A warning message that is generated during an assembly when the two destination registers on a <code>MULS</code> or
 * <code>MULU</code> instruction are the same to signal that the results are undefined.
 *
 * @author Francis Gagné
 */
public class MultiplicationResultsUndefinedWarningMessage extends AssemblyWarningMessage {

    /**
     * Initializes a new MultiplicationResultsUndefinedWarningMessage.
     */
    public MultiplicationResultsUndefinedWarningMessage() {
        super("The results of this multiplication are undefined because the two destination registers are the same");
    }

}
