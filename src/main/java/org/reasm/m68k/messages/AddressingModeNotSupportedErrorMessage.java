package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an effective addressing uses an addressing mode that is not supported
 * on the current architecture.
 *
 * @author Francis Gagné
 */
public class AddressingModeNotSupportedErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new AddressingModeNotSupportedErrorMessage.
     */
    public AddressingModeNotSupportedErrorMessage() {
        super("Addressing mode not supported on the current architecture");
    }

}
