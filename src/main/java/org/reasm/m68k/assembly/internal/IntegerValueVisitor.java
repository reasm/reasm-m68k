package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

import org.reasm.Function;
import org.reasm.StringValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.commons.messages.FunctionCannotBeConvertedToIntegerErrorMessage;
import org.reasm.expressions.UnaryOperator;

final class IntegerValueVisitor implements ValueVisitor<Void> {

    @Nonnull
    private final M68KAssemblyContext context;
    long value;
    boolean signed;

    IntegerValueVisitor(@Nonnull M68KAssemblyContext context) {
        this.context = context;
    }

    @Override
    public Void visitFloat(double value) {
        return this.visitSignedInt((long) value);
    }

    @Override
    public Void visitFunction(Function value) {
        this.context.addTentativeMessage(new FunctionCannotBeConvertedToIntegerErrorMessage());
        return null;
    }

    @Override
    public Void visitSignedInt(long value) {
        this.value = value;
        this.signed = true;
        return null;
    }

    @Override
    public Void visitString(String value) {
        // Pass the value through the unary plus operator to convert it to a real value.
        return Value.accept(UnaryOperator.UNARY_PLUS.apply(new StringValue(value), this.context.getEvaluationContext()), this);
    }

    @Override
    public Void visitUndetermined() {
        return null;
    }

    @Override
    public Void visitUnsignedInt(long value) {
        this.value = value;
        this.signed = false;
        return null;
    }

    boolean getSigned() {
        return this.signed;
    }

    long getValue() {
        return this.value;
    }

    void reset() {
        this.value = 0;
        this.signed = false;
    }

}
