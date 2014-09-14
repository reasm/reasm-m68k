package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a register is expected.
 *
 * @author Francis Gagné
 */
public class RegisterExpectedErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new RegisterExpectedErrorMessage.
     */
    public RegisterExpectedErrorMessage() {
        super("Register expected");
    }

}
