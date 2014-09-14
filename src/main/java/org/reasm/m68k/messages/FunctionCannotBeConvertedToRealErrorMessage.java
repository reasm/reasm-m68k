package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a function value is obtained from an expression in a context where a
 * real value is expected.
 *
 * @author Francis Gagné
 */
public class FunctionCannotBeConvertedToRealErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new FunctionCannotBeConvertedToRealErrorMessage.
     */
    public FunctionCannotBeConvertedToRealErrorMessage() {
        super("Expected a value convertible to real but got a function");
    }

}
