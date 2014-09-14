package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>ENDIF</code> or <code>ENDC</code> directive is used out of an
 * <code>IF</code> block.
 *
 * @author Francis Gagné
 */
public class EndifWithoutIfErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new EndifWithoutIfErrorMessage.
     */
    public EndifWithoutIfErrorMessage() {
        super("ENDIF/ENDC directive not in an IF block");
    }

}
