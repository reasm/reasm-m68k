package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.FunctionValue;
import org.reasm.SymbolContext;
import org.reasm.SymbolType;
import org.reasm.Value;
import org.reasm.expressions.Expression;
import org.reasm.m68k.expressions.internal.TokenType;
import org.reasm.m68k.messages.FunctionParameterIsNotSimpleIdentifierErrorMessage;
import org.reasm.messages.DirectiveRequiresLabelErrorMessage;

final class FunctionDirective extends Mnemonic {

    static final FunctionDirective FUNCTION = new FunctionDirective();

    private FunctionDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        final int numberOfOperands = context.numberOfOperands;
        if (context.numberOfLabels == 0) {
            context.addMessage(new DirectiveRequiresLabelErrorMessage("FUNCTION"));
        } else if (numberOfOperands == 0) {
            context.addWrongNumberOfOperandsErrorMessage();
        } else {
            // All operands except the last one must be simple identifiers.
            final String[] parameterNames = new String[numberOfOperands - 1];
            boolean argumentsAreValid = true;
            for (int i = 0; i < numberOfOperands - 1; i++) {
                final String operandText = context.getOperandText(i);
                context.tokenizer.setCharSequence(operandText);
                boolean isValid = context.tokenizer.getTokenType() == TokenType.IDENTIFIER;
                if (isValid) {
                    parameterNames[i] = context.tokenizer.getTokenText().toString();
                    context.tokenizer.advance();
                    isValid = context.tokenizer.getTokenType() == TokenType.END;
                }

                if (!isValid) {
                    context.addMessage(new FunctionParameterIsNotSimpleIdentifierErrorMessage(operandText));
                    argumentsAreValid = false;
                }
            }

            // The last operand must be a valid expression.
            final Expression functionExpression = parseExpressionOperand(context, numberOfOperands - 1);
            final Value functionValue;
            if (functionExpression != null && argumentsAreValid) {
                functionValue = new FunctionValue(new UserFunction(context, functionExpression, parameterNames));
            } else {
                functionValue = null;
            }

            context.defineSymbols(SymbolContext.VALUE, SymbolType.CONSTANT, functionValue);
        }
    }

    @Override
    void defineLabels(M68KAssemblyContext context) {
        // Don't define any labels.
    }

}
