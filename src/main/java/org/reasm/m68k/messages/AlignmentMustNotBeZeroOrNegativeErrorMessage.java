package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the alignment operand of an <code>ALIGN</code> or <code>CNOP</code>
 * directive is zero or a negative number.
 *
 * @author Francis Gagné
 */
public class AlignmentMustNotBeZeroOrNegativeErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new AlignmentMustNotBeZeroOrNegativeErrorMessage.
     */
    public AlignmentMustNotBeZeroOrNegativeErrorMessage() {
        super("The alignment must not be 0 or negative");
    }

}
