package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the distance between a branch instruction and its target is too large
 * for the size specified on the instruction.
 *
 * @author Francis Gagné
 */
public class BranchTargetOutOfRangeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new BranchTargetOutOfRangeErrorMessage.
     */
    public BranchTargetOutOfRangeErrorMessage() {
        super("Branch target out of range");
    }

}
