package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.SymbolContext;
import org.reasm.SymbolType;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.ValueToBooleanVisitor;
import org.reasm.expressions.BinaryOperator;

/**
 * The <code>FOR</code> directive.
 *
 * @author Francis Gagné
 */
final class ForDirective extends Mnemonic {

    static final ForDirective FOR = new ForDirective();

    private static final Value ZERO = new UnsignedIntValue(0);
    private static final Value ONE = new UnsignedIntValue(1);

    private static void defineVariable(M68KAssemblyContext context, String label, Value counter) {
        context.defineSymbol(SymbolContext.VALUE, label, SymbolType.VARIABLE, counter);
    }

    private ForDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        final Object blockState = context.getParentBlock();
        if (!(blockState instanceof ForBlockState)) {
            throw new AssertionError();
        }

        final ForBlockState forBlockState = (ForBlockState) blockState;

        // The FOR directive is assembled on every iteration.
        // On the first iteration, evaluate the operands and store information to use on subsequent iterations.
        if (forBlockState.counter == null) {
            if (context.numberOfLabels == 0) {
                forBlockState.labels = null;
            } else if (context.numberOfLabels == 1) {
                forBlockState.labels = context.getLabelText(0);
            } else {
                final String[] labels = new String[context.numberOfLabels];
                for (int i = 0; i < context.numberOfLabels; i++) {
                    labels[i] = context.getLabelText(i);
                }

                forBlockState.labels = labels;
            }

            if (context.numberOfOperands != 2 && context.numberOfOperands != 3) {
                context.addWrongNumberOfOperandsErrorMessage();
            }

            forBlockState.counter = null;
            forBlockState.to = null;
            if (context.numberOfOperands >= 1) {
                forBlockState.counter = evaluateExpressionOperand(context, 0);
                if (context.numberOfOperands >= 2) {
                    forBlockState.to = evaluateExpressionOperand(context, 1);
                    if (context.numberOfOperands >= 3) {
                        forBlockState.step = evaluateExpressionOperand(context, 2);
                        final Boolean stepIsNegative = Value.accept(
                                BinaryOperator.LESS_THAN.apply(forBlockState.step, ZERO, context.getEvaluationContext()),
                                ValueToBooleanVisitor.INSTANCE);
                        forBlockState.stepIsNegative = stepIsNegative != null && stepIsNegative.booleanValue();
                    } else {
                        forBlockState.step = ONE;
                        forBlockState.stepIsNegative = false;
                    }
                }
            }
        }

        if (forBlockState.labels != null) {
            if (forBlockState.labels instanceof String) {
                defineVariable(context, (String) forBlockState.labels, forBlockState.counter);
            } else {
                final String[] labels = (String[]) forBlockState.labels;
                for (String label : labels) {
                    defineVariable(context, label, forBlockState.counter);
                }
            }
        }

        final BinaryOperator operator = forBlockState.stepIsNegative ? BinaryOperator.GREATER_THAN_OR_EQUAL_TO
                : BinaryOperator.LESS_THAN_OR_EQUAL_TO;
        final Boolean result = Value.accept(
                operator.apply(forBlockState.counter, forBlockState.to, context.getEvaluationContext()),
                ValueToBooleanVisitor.INSTANCE);
        if (!(result != null && result.booleanValue())) {
            // Skip the block body and stop the iteration.
            forBlockState.iterator.next();
            forBlockState.hasNextIteration = false;
        }
    }

    @Override
    void defineLabels(M68KAssemblyContext context) {
        // Don't define any labels.
    }

}
