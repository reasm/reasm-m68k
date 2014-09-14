package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a size attribute is specified on a directive that doesn't allow it.
 *
 * @author Francis Gagné
 */
public class SizeAttributeNotAllowedErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new SizeAttributeNotAllowedErrorMessage.
     */
    public SizeAttributeNotAllowedErrorMessage() {
        super("Size attribute not allowed");
    }

}
