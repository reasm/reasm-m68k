package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the shift/rotate count of a shift/rotate instruction is out of range
 * (between 1 and 8).
 *
 * @author Francis Gagné
 */
public class ShiftCountOutOfRangeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new ShiftCountOutOfRangeErrorMessage.
     */
    public ShiftCountOutOfRangeErrorMessage() {
        super("Shift/rotate count must be between 1 and 8");
    }

}
