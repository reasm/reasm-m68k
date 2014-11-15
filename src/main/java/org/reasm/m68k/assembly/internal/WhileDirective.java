package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;
import org.reasm.ValueToBooleanVisitor;

/**
 * The <code>WHILE</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class WhileDirective extends Mnemonic {

    @Nonnull
    static final WhileDirective WHILE = new WhileDirective();

    private WhileDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();

        final Object blockState = context.getParentBlock();
        if (!(blockState instanceof WhileBlockState)) {
            throw new AssertionError();
        }

        final WhileBlockState whileBlockState = (WhileBlockState) blockState;

        // The WHILE directive is assembled on every iteration.
        // Parse the condition operand on the first iteration only.
        if (!whileBlockState.parsedCondition) {
            if (context.requireNumberOfOperands(1)) {
                whileBlockState.conditionExpression = parseExpressionOperand(context, 0);
            }

            whileBlockState.parsedCondition = true;
        }

        final Value condition;
        if (whileBlockState.conditionExpression != null) {
            condition = whileBlockState.conditionExpression.evaluate(context.getEvaluationContext());
        } else {
            condition = null;
        }

        final Boolean result = Value.accept(condition, ValueToBooleanVisitor.INSTANCE);
        if (!(result != null && result.booleanValue())) {
            // Skip the block body and stop the iteration.
            whileBlockState.iterator.next();
            whileBlockState.hasNextIteration = false;
        }
    }

}
