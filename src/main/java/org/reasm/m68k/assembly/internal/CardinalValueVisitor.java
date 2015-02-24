package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

import org.reasm.AssemblyMessage;
import org.reasm.Function;
import org.reasm.StringValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.commons.messages.FunctionCannotBeConvertedToIntegerErrorMessage;
import org.reasm.expressions.UnaryOperator;

final class CardinalValueVisitor implements ValueVisitor<Void> {

    interface ErrorFactory {
        @Nonnull
        AssemblyMessage createMessage();
    }

    private static final ErrorFactory DEFAULT_NEGATIVE_VALUE_ERROR_FACTORY = new ErrorFactory() {
        @Override
        public AssemblyMessage createMessage() {
            throw new RuntimeException("CardinalValueVisitor.negativeValueErrorFactory not initialized");
        }
    };

    @Nonnull
    private final M68KAssemblyContext context;
    private long value;
    @Nonnull
    private ErrorFactory negativeValueErrorFactory = DEFAULT_NEGATIVE_VALUE_ERROR_FACTORY;

    CardinalValueVisitor(@Nonnull M68KAssemblyContext context) {
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
        if (value < 0) {
            this.context.addTentativeMessage(this.negativeValueErrorFactory.createMessage());
        } else {
            this.value = value;
        }

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
        return null;
    }

    long getValue() {
        return this.value;
    }

    void reset(long value, @Nonnull ErrorFactory negativeValueErrorFactory) {
        this.value = value;
        this.negativeValueErrorFactory = negativeValueErrorFactory;
    }

}
