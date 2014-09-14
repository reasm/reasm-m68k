package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a function value is obtained from an expression in a context where a
 * string value is expected.
 *
 * @author Francis Gagné
 */
public class FunctionCannotBeConvertedToStringErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new FunctionCannotBeConvertedToStringErrorMessage.
     */
    public FunctionCannotBeConvertedToStringErrorMessage() {
        super("Expected a value convertible to string but got a function");
    }

}
