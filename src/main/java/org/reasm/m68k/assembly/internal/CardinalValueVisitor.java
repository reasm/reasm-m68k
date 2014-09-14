package org.reasm.m68k.assembly.internal;

import org.reasm.AssemblyMessage;
import org.reasm.Function;
import org.reasm.StringValue;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.expressions.UnaryOperator;
import org.reasm.m68k.messages.FunctionCannotBeConvertedToIntegerErrorMessage;

class CardinalValueVisitor implements ValueVisitor<Void> {

    interface ErrorFactory {
        AssemblyMessage createMessage();
    }

    private final M68KAssemblyContext context;
    private long value;
    private ErrorFactory negativeValueErrorFactory;

    CardinalValueVisitor(M68KAssemblyContext context) {
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

    void reset(long value, ErrorFactory negativeValueErrorFactory) {
        this.value = value;
        this.negativeValueErrorFactory = negativeValueErrorFactory;
    }

}
