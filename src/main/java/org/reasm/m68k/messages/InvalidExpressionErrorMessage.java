package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an expression is expected in some context but a valid expression could
 * not be parsed.
 *
 * @author Francis Gagné
 */
public class InvalidExpressionErrorMessage extends AssemblyErrorMessage {

    private final String expression;

    /**
     * Initializes a new InvalidExpressionErrorMessage.
     *
     * @param expression
     *            the text that could not be parsed as an expression
     */
    public InvalidExpressionErrorMessage(String expression) {
        super("Invalid expression: " + expression);
        this.expression = expression;
    }

    /**
     * Returns the text that could not be parsed as an expression.
     *
     * @return the text that could not be parsed as an expression
     */
    public final String getExpression() {
        return this.expression;
    }

}
