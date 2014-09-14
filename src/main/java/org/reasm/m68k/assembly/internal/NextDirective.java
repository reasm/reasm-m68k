package org.reasm.m68k.assembly.internal;

import org.reasm.expressions.BinaryOperator;
import org.reasm.m68k.messages.NextWithoutForErrorMessage;

/**
 * The <code>NEXT</code> directive.
 *
 * @author Francis Gagné
 */
final class NextDirective extends Mnemonic {

    static final NextDirective NEXT = new NextDirective();

    static void assembleCore(M68KAssemblyContext context) {
        final Object blockState = context.getParentBlock();
        if (blockState instanceof ForBlockState) {
            final ForBlockState forBlockState = (ForBlockState) blockState;

            forBlockState.counter = BinaryOperator.ADDITION.apply(forBlockState.counter, forBlockState.step,
                    context.getEvaluationContext());
        } else {
            context.addMessage(new NextWithoutForErrorMessage());
        }
    }

    private NextDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);
        assembleCore(context);
    }

}
