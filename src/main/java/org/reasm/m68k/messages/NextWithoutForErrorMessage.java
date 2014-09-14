package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a <code>NEXT</code> directive is used out of a <code>FOR</code> block.
 *
 * @author Francis Gagné
 */
public class NextWithoutForErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new NextWithoutForErrorMessage.
     */
    public NextWithoutForErrorMessage() {
        super("NEXT directive not in a FOR block");
    }

}
