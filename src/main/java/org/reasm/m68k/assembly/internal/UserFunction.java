package org.reasm.m68k.assembly.internal;

import java.util.Arrays;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Function;
import org.reasm.Symbol;
import org.reasm.SymbolContext;
import org.reasm.SymbolLookupContext;
import org.reasm.expressions.*;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.messages.WrongNumberOfArgumentsErrorMessage;

@Immutable
final class UserFunction implements Function, SymbolLookup {

    @Nonnull
    private final M68KAssemblyContext context;
    @Nonnull
    private final SymbolLookupContext lookupContext;
    @Nonnull
    private final Expression functionExpression;
    @Nonnull
    private final String[] parameterNames;

    UserFunction(@Nonnull M68KAssemblyContext context, @Nonnull Expression functionExpression, @Nonnull String[] parameterNames) {
        this.context = context;
        this.lookupContext = context.builder.getAssembly().getCurrentSymbolLookupContext();
        this.functionExpression = functionExpression;
        this.parameterNames = parameterNames;
    }

    @Override
    public final Expression call(@Nonnull Expression[] arguments, @Nonnull EvaluationContext evaluationContext) {
        if (arguments.length != this.parameterNames.length) {
            evaluationContext.getAssemblyMessageConsumer().accept(new WrongNumberOfArgumentsErrorMessage());
        }

        // Wrap the expression in an ExpressionWithOverriddenSymbolLookup
        // so that identifiers that appear in the function expression
        // (or identifiers that are produced using the period or indexer operators)
        // are resolved in the context where the function is defined,
        // rather than in the context where the function is called.
        return new ExpressionWithOverriddenSymbolLookup(this.replaceArguments(this.functionExpression, arguments,
                evaluationContext.getSymbolLookup()), this);
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
        if (!this.functionExpression.equals(other.functionExpression)) {
            return false;
        }

        if (!Arrays.equals(this.parameterNames, other.parameterNames)) {
            return false;
        }

        return true;
    }

    @Override
    public final Symbol getSymbol(String name) {
        return this.context.builder.resolveSymbolReference(SymbolContext.VALUE, name, M68KArchitecture.isLocalName(name), false,
                this.lookupContext, this.context).getSymbol();
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.functionExpression.hashCode();
        result = prime * result + Arrays.hashCode(this.parameterNames);
        return result;
    }

    private final Expression replaceArguments(@Nonnull Expression expression, @Nonnull Expression[] arguments,
            @CheckForNull SymbolLookup originalSymbolLookup) {
        final Class<? extends Expression> expressionClass = expression.getClass();
        if (expressionClass == IdentifierExpression.class) {
            final String identifier = ((IdentifierExpression) expression).getIdentifier();
            for (int i = 0; i < this.parameterNames.length; i++) {
                final String parameterName = this.parameterNames[i];

                if (parameterName.equalsIgnoreCase(identifier)) {
                    if (i < arguments.length) {
                        // Wrap the argument in an ExpressionWithOverriddenSymbolLookup,
                        // so that identifiers in the arguments are looked up in the context of the function call,
                        // rather than in the context of the function definition.
                        return new ExpressionWithOverriddenSymbolLookup(arguments[i], originalSymbolLookup);
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
                        // Wrap the argument in an ExpressionWithOverriddenSymbolLookup,
                        // so that identifiers in the arguments are looked up in the context of the function call,
                        // rather than in the context of the function definition.
                        leftExpression = new ExpressionWithOverriddenSymbolLookup(arguments[i], originalSymbolLookup);
                    } else {
                        leftExpression = ValueExpression.UNDETERMINED;
                    }

                    final Expression rightExpression = new IdentifierExpression(identifier.substring(parameterName.length() + 1));
                    return new PeriodExpression(leftExpression, rightExpression);
                }
            }
        } else if (expressionClass == GroupingExpression.class) {
            final Expression childExpression = ((GroupingExpression) expression).getChildExpression();
            final Expression replacedExpression = this.replaceArguments(childExpression, arguments, originalSymbolLookup);
            if (replacedExpression != childExpression) {
                return new GroupingExpression(replacedExpression);
            }
        } else if (expressionClass == UnaryOperatorExpression.class) {
            final UnaryOperatorExpression unaryOperatorExpression = (UnaryOperatorExpression) expression;
            final Expression operand = unaryOperatorExpression.getOperand();
            final Expression replacedOperand = this.replaceArguments(operand, arguments, originalSymbolLookup);
            if (replacedOperand != operand) {
                return new UnaryOperatorExpression(unaryOperatorExpression.getOperator(), replacedOperand);
            }
        } else if (expressionClass == PeriodExpression.class) {
            final PeriodExpression periodExpression = (PeriodExpression) expression;
            final Expression leftExpression = periodExpression.getLeftExpression();
            final Expression rightExpression = periodExpression.getRightExpression();
            final Expression replacedLeftExpression = this.replaceArguments(leftExpression, arguments, originalSymbolLookup);
            final Expression replacedRightExpression = this.replaceArguments(rightExpression, arguments, originalSymbolLookup);
            if (replacedLeftExpression != leftExpression || replacedRightExpression != rightExpression) {
                return new PeriodExpression(replacedLeftExpression, replacedRightExpression);
            }
        } else if (expressionClass == IndexerExpression.class) {
            final IndexerExpression indexerExpression = (IndexerExpression) expression;
            final Expression subjectExpression = indexerExpression.getSubjectExpression();
            final Expression indexExpression = indexerExpression.getIndexExpression();
            final Expression replacedSubjectExpression = this.replaceArguments(subjectExpression, arguments, originalSymbolLookup);
            final Expression replacedIndexExpression = this.replaceArguments(indexExpression, arguments, originalSymbolLookup);
            if (replacedSubjectExpression != subjectExpression || replacedIndexExpression != indexExpression) {
                return new IndexerExpression(replacedSubjectExpression, replacedIndexExpression);
            }
        } else if (expressionClass == BinaryOperatorExpression.class) {
            final BinaryOperatorExpression binaryOperatorExpression = (BinaryOperatorExpression) expression;
            final Expression operand1 = binaryOperatorExpression.getOperand1();
            final Expression operand2 = binaryOperatorExpression.getOperand2();
            final Expression replacedOperand1 = this.replaceArguments(operand1, arguments, originalSymbolLookup);
            final Expression replacedOperand2 = this.replaceArguments(operand2, arguments, originalSymbolLookup);
            if (replacedOperand1 != operand1 || replacedOperand2 != operand2) {
                return new BinaryOperatorExpression(binaryOperatorExpression.getOperator(), replacedOperand1, replacedOperand2);
            }
        } else if (expressionClass == ConditionalExpression.class) {
            final ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
            final Expression condition = conditionalExpression.getCondition();
            final Expression truePart = conditionalExpression.getTruePart();
            final Expression falsePart = conditionalExpression.getFalsePart();
            final Expression replacedCondition = this.replaceArguments(condition, arguments, originalSymbolLookup);
            final Expression replacedTruePart = this.replaceArguments(truePart, arguments, originalSymbolLookup);
            final Expression replacedFalsePart = this.replaceArguments(falsePart, arguments, originalSymbolLookup);
            if (replacedCondition != condition || replacedTruePart != truePart || replacedFalsePart != falsePart) {
                return new ConditionalExpression(replacedCondition, replacedTruePart, replacedFalsePart);
            }
        } else if (expressionClass == FunctionCallExpression.class) {
            final FunctionCallExpression functionCallExpression = (FunctionCallExpression) expression;
            final Expression function = functionCallExpression.getFunction();
            final Expression replacedFunction = this.replaceArguments(function, arguments, originalSymbolLookup);
            boolean replacedSomething = function != replacedFunction;

            final Expression[] arguments2 = functionCallExpression.getArguments();
            final Expression[] replacedArguments = new Expression[arguments2.length];
            for (int i = 0; i < replacedArguments.length; i++) {
                replacedArguments[i] = this.replaceArguments(arguments2[i], arguments, originalSymbolLookup);
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
