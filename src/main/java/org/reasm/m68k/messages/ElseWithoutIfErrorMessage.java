package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an ELSE directive is used out of an IF block.
 *
 * @author Francis Gagné
 */
public class ElseWithoutIfErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new ElseWithoutIfErrorMessage.
     */
    public ElseWithoutIfErrorMessage() {
        super("ELSE directive not in an IF block");
    }

}
