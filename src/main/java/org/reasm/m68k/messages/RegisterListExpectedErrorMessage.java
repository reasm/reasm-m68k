package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a register list is expected.
 *
 * @author Francis Gagné
 */
public class RegisterListExpectedErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new RegisterListExpectedErrorMessage.
     */
    public RegisterListExpectedErrorMessage() {
        super("Register list expected");
    }

}
