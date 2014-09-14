package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a byte-sized branch instruction (<code>BRA.B</code> or
 * <code>BRA.S</code>) targets the following instruction, yielding a distance of zero. The encoding for such an instruction is
 * interpreted as a word-sized branch.
 *
 * @author Francis Gagné
 */
public class ZeroDistanceShortBranchErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new ZeroDistanceShortBranchErrorMessage.
     */
    public ZeroDistanceShortBranchErrorMessage() {
        super("Distance of 0 not allowed for short branch");
    }

}
