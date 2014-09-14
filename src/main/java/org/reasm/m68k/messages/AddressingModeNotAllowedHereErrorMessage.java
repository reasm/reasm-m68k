package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an effective address uses an addressing mode that is not allowed in a
 * particular context.
 *
 * @author Francis Gagné
 */
public class AddressingModeNotAllowedHereErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new AddressingModeNotAllowedHereErrorMessage.
     */
    public AddressingModeNotAllowedHereErrorMessage() {
        super("Addressing mode not allowed here");
    }

}
