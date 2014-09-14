package org.reasm.m68k.assembly.internal;

import org.reasm.Value;
import org.reasm.ValueToBooleanVisitor;
import org.reasm.m68k.messages.ElseifWithoutIfErrorMessage;

/**
 * The <code>IF</code> and <code>ELSEIF</code> directives.
 *
 * @author Francis Gagné
 */
abstract class IfElseifDirective extends Mnemonic {

    static final IfElseifDirective IF = new IfElseifDirective() {
        @Override
        void notInIfBlock(M68KAssemblyContext context) {
            throw new AssertionError();
        }
    };

    static final IfElseifDirective ELSEIF = new IfElseifDirective() {
        @Override
        void notInIfBlock(M68KAssemblyContext context) {
            context.addMessage(new ElseifWithoutIfErrorMessage());
        }
    };

    IfElseifDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();

        final Object block = context.getParentBlock();
        if (block instanceof IfBlockState) {
            final IfBlockState ifBlockState = (IfBlockState) block;
            final Value condition = context.requireNumberOfOperands(1) ? evaluateExpressionOperand(context, 0) : null;
            final Boolean result = condition == null ? null : Value.accept(condition, ValueToBooleanVisitor.INSTANCE);
            if (result != null && result.booleanValue()) {
                // Process the block body, then stop.
                ifBlockState.iterator.stopAfterNext();
            } else {
                // Skip the block body.
                ifBlockState.iterator.next();
            }
        } else {
            this.notInIfBlock(context);
        }
    }

    abstract void notInIfBlock(M68KAssemblyContext context);

}
