package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Value;

/**
 * The <code>RSRESET</code> and <code>RSSET</code> directives.
 *
 * @author Francis Gagné
 */
@Immutable
class RsresetRssetDirective extends Mnemonic {

    @Nonnull
    static final RsresetRssetDirective RSRESET = new RsresetRssetDirective(true);
    @Nonnull
    static final RsresetRssetDirective RSSET = new RsresetRssetDirective(false);

    private final boolean operandIsOptional;

    private RsresetRssetDirective(boolean operandIsOptional) {
        this.operandIsOptional = operandIsOptional;
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        if (!(this.operandIsOptional && context.numberOfOperands == 0 || context.numberOfOperands == 1)) {
            context.addWrongNumberOfOperandsErrorMessage();
        }

        long value = 0;
        boolean signed = false;
        if (context.numberOfOperands >= 1) {
            final Value countValue = evaluateExpressionOperand(context, 0);
            final IntegerValueVisitor valueVisitor = context.integerValueVisitor;
            valueVisitor.reset();
            Value.accept(countValue, valueVisitor);
            value = valueVisitor.getValue();
            signed = valueVisitor.getSigned();
        }

        context.rs.set(value, signed);
    }

}
