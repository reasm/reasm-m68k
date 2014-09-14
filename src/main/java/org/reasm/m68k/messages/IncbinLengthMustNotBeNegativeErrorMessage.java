package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the <em>length</em> operand of an <code>INCBIN</code> directive is
 * negative.
 *
 * @author Francis Gagné
 */
public class IncbinLengthMustNotBeNegativeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new IncbinLengthMustNotBeNegativeErrorMessage.
     */
    public IncbinLengthMustNotBeNegativeErrorMessage() {
        super("Length operand on INCBIN or BINCLUDE must not be negative");
    }

}
