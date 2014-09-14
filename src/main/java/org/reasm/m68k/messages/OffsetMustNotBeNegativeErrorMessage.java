package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the offset operand of a <code>CNOP</code> directive is a negative
 * number.
 *
 * @author Francis Gagné
 */
public class OffsetMustNotBeNegativeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new OffsetMustNotBeNegativeErrorMessage.
     */
    public OffsetMustNotBeNegativeErrorMessage() {
        super("The offset must not be negative");
    }

}
