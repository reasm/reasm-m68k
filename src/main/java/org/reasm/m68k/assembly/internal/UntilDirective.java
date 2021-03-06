package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;
import org.reasm.ValueToBooleanVisitor;
import org.reasm.m68k.messages.UntilWithoutDoErrorMessage;

/**
 * The <code>UNTIL</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class UntilDirective extends Mnemonic {

    @Nonnull
    static final UntilDirective UNTIL = new UntilDirective();

    private UntilDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        final Object blockState = context.getParentBlock();
        if (blockState instanceof DoBlockState) {
            final DoBlockState doBlockState = (DoBlockState) blockState;

            // The UNTIL directive is assembled on every iteration.
            // Parse the condition operand on the first iteration only.
            if (!doBlockState.parsedCondition) {
                if (context.requireNumberOfOperands(1)) {
                    doBlockState.conditionExpression = parseExpressionOperand(context, 0);
                }

                doBlockState.parsedCondition = true;
            }

            final Value condition;
            if (doBlockState.conditionExpression != null) {
                condition = doBlockState.conditionExpression.evaluate(context.getEvaluationContext());
            } else {
                condition = null;
            }

            final Boolean result = Value.accept(condition, ValueToBooleanVisitor.INSTANCE);
            if (result == null || result.booleanValue()) {
                // Stop the iteration.
                doBlockState.hasNextIteration = false;
            }
        } else {
            context.addMessage(new UntilWithoutDoErrorMessage());
        }
    }

}
