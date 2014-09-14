package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an effective address specifies a base displacement that is out of the
 * range supported by the instruction set.
 *
 * @author Francis Gagné
 */
public class BaseDisplacementOutOfRangeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new BaseDisplacementOutOfRangeErrorMessage.
     */
    public BaseDisplacementOutOfRangeErrorMessage() {
        super("Base displacement out of range");
    }

}
