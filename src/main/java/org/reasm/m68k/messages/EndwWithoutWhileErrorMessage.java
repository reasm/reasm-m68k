package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>ENDW</code> directive is used out of a <code>WHILE</code>
 * block.
 *
 * @author Francis Gagné
 */
public class EndwWithoutWhileErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new EndwWithoutWhileErrorMessage.
     */
    public EndwWithoutWhileErrorMessage() {
        super("ENDW directive not in a WHILE block");
    }

}
