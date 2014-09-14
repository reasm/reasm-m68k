package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a value that is out of range in a particular context is encountered.
 *
 * @author Francis Gagné
 */
public class ValueOutOfRangeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new ValueOutOfRangeErrorMessage.
     *
     * @param value
     *            the value that is out of bounds
     */
    public ValueOutOfRangeErrorMessage(long value) {
        super("Value out of range: " + value);
    }

}
