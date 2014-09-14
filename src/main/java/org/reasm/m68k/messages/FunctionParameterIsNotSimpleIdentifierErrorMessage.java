package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an operand to the <code>FUNCTION</code> directive that is not the last
 * operand (i.e. a parameter name) is not a simple identifier.
 *
 * @author Francis Gagné
 */
public class FunctionParameterIsNotSimpleIdentifierErrorMessage extends AssemblyErrorMessage {

    private final String operandText;

    /**
     * Initializes a new FunctionArgumentIsNotSimpleIdentifierErrorMessage.
     *
     * @param operandText
     *            the text of the operand that caused this error
     */
    public FunctionParameterIsNotSimpleIdentifierErrorMessage(String operandText) {
        super("Function argument \"" + operandText + "\" is not a simple identifier");
        this.operandText = operandText;
    }

    /**
     * Gets the text of the operand that caused this error.
     *
     * @return the text of the operand
     */
    public final String getOperandText() {
        return this.operandText;
    }

}
