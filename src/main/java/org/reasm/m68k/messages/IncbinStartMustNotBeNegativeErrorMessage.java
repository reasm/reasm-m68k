package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the <em>start</em> operand of an <code>INCBIN</code> or
 * <code>BINCLUDE</code> directive is negative.
 *
 * @author Francis Gagné
 */
public class IncbinStartMustNotBeNegativeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new IncbinStartMustNotBeNegativeErrorMessage.
     */
    public IncbinStartMustNotBeNegativeErrorMessage() {
        super("Start operand on INCBIN or BINCLUDE must not be negative");
    }

}
