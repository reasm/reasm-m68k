package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.Value;

/**
 * The <code>RSRESET</code> and <code>RSSET</code> directives.
 *
 * @author Francis Gagné
 */
class RsresetRssetDirective extends Mnemonic {

    static final RsresetRssetDirective RSRESET = new RsresetRssetDirective(true);
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
            if (countValue != null) {
                final IntegerValueVisitor valueVisitor = context.integerValueVisitor;
                valueVisitor.reset();
                Value.accept(countValue, valueVisitor);
                value = valueVisitor.getValue();
                signed = valueVisitor.getSigned();
            }
        }

        context.rs.set(value, signed);
    }

}
