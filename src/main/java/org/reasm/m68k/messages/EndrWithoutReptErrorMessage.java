package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>ENDR</code> directive is used out of a <code>REPT</code>
 * block.
 *
 * @author Francis Gagné
 */
public class EndrWithoutReptErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new EndrWithoutReptErrorMessage.
     */
    public EndrWithoutReptErrorMessage() {
        super("ENDR directive not in a REPT block");
    }

}
