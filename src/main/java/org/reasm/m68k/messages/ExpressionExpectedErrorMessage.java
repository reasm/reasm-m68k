package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an expression is expected but no valid expression could be parsed.
 *
 * @author Francis Gagné
 */
public class ExpressionExpectedErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new ExpressionExpectedErrorMessage.
     */
    public ExpressionExpectedErrorMessage() {
        super("Expression expected");
    }

}
