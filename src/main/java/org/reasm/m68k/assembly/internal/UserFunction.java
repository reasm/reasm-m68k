package org.reasm.m68k.assembly.internal;

import java.util.Arrays;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Function;
import org.reasm.expressions.*;
import org.reasm.messages.WrongNumberOfArgumentsErrorMessage;

@Immutable
final class UserFunction implements Function {

    @Nonnull
    private final M68KAssemblyContext context;
    @Nonnull
    private final Expression functionExpression;
    @Nonnull
    private final String[] parameterNames;

    UserFunction(@Nonnull M68KAssemblyContext context, @Nonnull Expression functionExpression, @Nonnull String[] parameterNames) {
        this.context = context;
        this.functionExpression = functionExpression;
        this.parameterNames = parameterNames;
    }

    @Override
    public final Expression call(@Nonnull Expression[] arguments, @Nonnull EvaluationContext evaluationContext) {
        if (arguments.length != this.parameterNames.length) {
            evaluationContext.getAssemblyMessageConsumer().accept(new WrongNumberOfArgumentsErrorMessage());
        }

        return this.replaceArguments(this.functionExpression, arguments);
    }

    @Override
    public final boolean equals(@CheckForNull Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        UserFunction other = (UserFunction) obj;
        if (!this.context.equals(other.context)) {
            return false;
        }

        if (!this.functionExpression.equals(other.functionExpression)) {
            return false;
        }

        if (!Arrays.equals(this.parameterNames, other.parameterNames)) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.context.hashCode();
        result = prime * result + this.functionExpression.hashCode();
        result = prime * result + Arrays.hashCode(this.parameterNames);
        return result;
    }

    @Nonnull
    private final Expression replaceArguments(@Nonnull Expression expression, @Nonnull Expression[] arguments) {
        final Class<? extends Expression> expressionClass = expression.getClass();
        if (expressionClass == IdentifierExpression.class) {
            final IdentifierExpression identifierExpression = (IdentifierExpression) expression;
            final String identifier = identifierExpression.getIdentifier();
            for (int i = 0; i < this.parameterNames.length; i++) {
                final String parameterName = this.parameterNames[i];

                if (parameterName.equalsIgnoreCase(identifier)) {
                    if (i < arguments.length) {
                        return arguments[i];
                    }

                    return ValueExpression.UNDETERMINED;
                }

                // Break identifiers of the form 'X.Y' into a PeriodExpression
                // if 'X' is the name of one of the function's parameters.
                // Test for the '.' character before calling substring() to avoid unnecessary allocations.
                if (identifier.startsWith(".", parameterName.length())
                        && parameterName.equalsIgnoreCase(identifier.substring(0, parameterName.length()))) {
                    final Expression leftExpression;
                    if (i < arguments.length) {
                        leftExpression = arguments[i];
                    } else {
                        leftExpression = ValueExpression.UNDETERMINED;
                    }

                    final Expression rightExpression = new IdentifierExpression(identifier.substring(parameterName.length() + 1),
                            identifierExpression.getSymbolLookup());
                    return new PeriodExpression(leftExpression, rightExpression, identifierExpression.getSymbolLookup());
                }
            }
        } else if (expressionClass == GroupingExpression.class) {
            final Expression childExpression = ((GroupingExpression) expression).getChildExpression();
            final Expression replacedExpression = this.replaceArguments(childExpression, arguments);
            if (replacedExpression != childExpression) {
                return new GroupingExpression(replacedExpression);
            }
        } else if (expressionClass == UnaryOperatorExpression.class) {
            final UnaryOperatorExpression unaryOperatorExpression = (UnaryOperatorExpression) expression;
            final Expression operand = unaryOperatorExpression.getOperand();
            final Expression replacedOperand = this.replaceArguments(operand, arguments);
            if (replacedOperand != operand) {
                return new UnaryOperatorExpression(unaryOperatorExpression.getOperator(), replacedOperand);
            }
        } else if (expressionClass == PeriodExpression.class) {
            final PeriodExpression periodExpression = (PeriodExpression) expression;
            final Expression leftExpression = periodExpression.getLeftExpression();
            final Expression rightExpression = periodExpression.getRightExpression();
            final Expression replacedLeftExpression = this.replaceArguments(leftExpression, arguments);
            final Expression replacedRightExpression = this.replaceArguments(rightExpression, arguments);
            if (replacedLeftExpression != leftExpression || replacedRightExpression != rightExpression) {
                return new PeriodExpression(replacedLeftExpression, replacedRightExpression,
                        periodExpression.getFallbackSymbolLookup());
            }
        } else if (expressionClass == IndexerExpression.class) {
            final IndexerExpression indexerExpression = (IndexerExpression) expression;
            final Expression subjectExpression = indexerExpression.getSubjectExpression();
            final Expression indexExpression = indexerExpression.getIndexExpression();
            final Expression replacedSubjectExpression = this.replaceArguments(subjectExpression, arguments);
            final Expression replacedIndexExpression = this.replaceArguments(indexExpression, arguments);
            if (replacedSubjectExpression != subjectExpression || replacedIndexExpression != indexExpression) {
                return new IndexerExpression(replacedSubjectExpression, replacedIndexExpression,
                        indexerExpression.getFallbackSymbolLookup());
            }
        } else if (expressionClass == BinaryOperatorExpression.class) {
            final BinaryOperatorExpression binaryOperatorExpression = (BinaryOperatorExpression) expression;
            final Expression operand1 = binaryOperatorExpression.getOperand1();
            final Expression operand2 = binaryOperatorExpression.getOperand2();
            final Expression replacedOperand1 = this.replaceArguments(operand1, arguments);
            final Expression replacedOperand2 = this.replaceArguments(operand2, arguments);
            if (replacedOperand1 != operand1 || replacedOperand2 != operand2) {
                return new BinaryOperatorExpression(binaryOperatorExpression.getOperator(), replacedOperand1, replacedOperand2);
            }
        } else if (expressionClass == ConditionalExpression.class) {
            final ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
            final Expression condition = conditionalExpression.getCondition();
            final Expression truePart = conditionalExpression.getTruePart();
            final Expression falsePart = conditionalExpression.getFalsePart();
            final Expression replacedCondition = this.replaceArguments(condition, arguments);
            final Expression replacedTruePart = this.replaceArguments(truePart, arguments);
            final Expression replacedFalsePart = this.replaceArguments(falsePart, arguments);
            if (replacedCondition != condition || replacedTruePart != truePart || replacedFalsePart != falsePart) {
                return new ConditionalExpression(replacedCondition, replacedTruePart, replacedFalsePart);
            }
        } else if (expressionClass == FunctionCallExpression.class) {
            final FunctionCallExpression functionCallExpression = (FunctionCallExpression) expression;
            final Expression function = functionCallExpression.getFunction();
            final Expression replacedFunction = this.replaceArguments(function, arguments);
            boolean replacedSomething = function != replacedFunction;

            final Expression[] arguments2 = functionCallExpression.getArguments();
            final Expression[] replacedArguments = new Expression[arguments2.length];
            for (int i = 0; i < replacedArguments.length; i++) {
                replacedArguments[i] = this.replaceArguments(arguments2[i], arguments);
                if (replacedArguments[i] != arguments2[i]) {
                    replacedSomething = true;
                }
            }

            if (replacedSomething) {
                return new FunctionCallExpression(replacedFunction, replacedArguments);
            }
        }

        return expression;
    }

}
