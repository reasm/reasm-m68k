package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>ELSEIF</code> directive is used out of an <code>IF</code>
 * block.
 *
 * @author Francis Gagné
 */
public class ElseifWithoutIfErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new ElseifWithoutIfErrorMessage.
     */
    public ElseifWithoutIfErrorMessage() {
        super("ELSEIF directive not in an IF block");
    }

}
