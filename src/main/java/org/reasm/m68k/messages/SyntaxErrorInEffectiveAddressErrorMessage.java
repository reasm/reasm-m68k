package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when there is a syntax error in an effective address.
 *
 * @author Francis Gagné
 */
public class SyntaxErrorInEffectiveAddressErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new SyntaxErrorInEffectiveAddressErrorMessage.
     */
    public SyntaxErrorInEffectiveAddressErrorMessage() {
        super("Syntax error in effective address");
    }

}
