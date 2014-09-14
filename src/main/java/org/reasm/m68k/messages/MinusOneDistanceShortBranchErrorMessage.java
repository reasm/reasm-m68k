package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a byte-sized branch instruction (BRA.B or BRA.S) has a distance of -1.
 * The encoding for such an instruction is interpreted as a long-sized branch on MC68020 and later.
 *
 * @author Francis Gagné
 */
public class MinusOneDistanceShortBranchErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new MinusOneDistanceShortBranchErrorMessage.
     */
    public MinusOneDistanceShortBranchErrorMessage() {
        super("Distance of -1 not allowed for short branch");
    }

}
