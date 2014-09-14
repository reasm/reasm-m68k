package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an instruction or a specific format of an instruction is not supported
 * on the current architecture.
 *
 * @author Francis Gagné
 */
public class NotSupportedOnArchitectureErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new NotSupportedOnArchitectureErrorMessage.
     */
    public NotSupportedOnArchitectureErrorMessage() {
        super("Instruction or instruction format not supported on the current architecture");
    }

}
