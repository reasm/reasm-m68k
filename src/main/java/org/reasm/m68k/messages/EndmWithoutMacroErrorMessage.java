package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>ENDM</code> directive is used out of a <code>MACRO</code>
 * block.
 *
 * @author Francis Gagné
 */
public class EndmWithoutMacroErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new EndmWithoutMacroErrorMessage.
     */
    public EndmWithoutMacroErrorMessage() {
        super("ENDM directive not in a MACRO block");
    }

}
