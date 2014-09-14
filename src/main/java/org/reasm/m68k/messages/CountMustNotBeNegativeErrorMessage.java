package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the count operand of a <code>DCB</code>, <code>DS</code> or
 * <code>RS</code> directive is negative.
 *
 * @author Francis Gagné
 */
public class CountMustNotBeNegativeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new CountMustNotBeNegativeErrorMessage.
     */
    public CountMustNotBeNegativeErrorMessage() {
        super("The count must not be negative");
    }

}
