package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a function value is obtained from an expression in a context where an
 * integer value is expected.
 *
 * @author Francis Gagné
 */
public class FunctionCannotBeConvertedToIntegerErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new FunctionCannotBeConvertedToIntegerErrorMessage.
     */
    public FunctionCannotBeConvertedToIntegerErrorMessage() {
        super("Expected a value convertible to integer but got a function");
    }

}
