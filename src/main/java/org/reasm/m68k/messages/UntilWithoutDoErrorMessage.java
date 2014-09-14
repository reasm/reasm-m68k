package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>UNTIL</code> directive is used out of a <code>DO</code>
 * block.
 *
 * @author Francis Gagné
 */
public class UntilWithoutDoErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new UntilWithoutDoErrorMessage.
     */
    public UntilWithoutDoErrorMessage() {
        super("UNTIL directive not in a DO block");
    }

}
