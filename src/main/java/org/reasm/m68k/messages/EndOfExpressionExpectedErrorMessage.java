package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an expression is successfully parsed but there are other tokens
 * following the expression.
 *
 * @author Francis Gagné
 */
public class EndOfExpressionExpectedErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new EndOfExpressionExpectedErrorMessage.
     */
    public EndOfExpressionExpectedErrorMessage() {
        super("End of expression expected");
    }

}
