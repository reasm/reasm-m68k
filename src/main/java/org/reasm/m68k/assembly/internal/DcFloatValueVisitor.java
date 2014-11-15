package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.reasm.Function;
import org.reasm.StringValue;
import org.reasm.Value;
import org.reasm.expressions.UnaryOperator;
import org.reasm.m68k.messages.FunctionCannotBeConvertedToRealErrorMessage;

final class DcFloatValueVisitor implements DcValueVisitor {

    private static double unsignedToFloat(long value) {
        if (value < 0) { // value >= 2**63
            // By shifting, we lose the least significant bit, but a double doesn't have enough precision to represent that bit
            // anyway.
            return (value >>> 1) * 2.;
        }

        return value;
    }

    @Nonnull
    private final M68KAssemblyContext context;
    @Nonnull
    private InstructionSize size = InstructionSize.DEFAULT;
    private double output;

    DcFloatValueVisitor(@Nonnull M68KAssemblyContext context) {
        this.context = context;
    }

    @Override
    public void encode() throws IOException {
        switch (this.size) {
        case SINGLE:
            this.context.appendLong(Float.floatToRawIntBits((float) this.output));
            break;

        case DOUBLE:
        default:
            this.context.appendQuad(Double.doubleToRawLongBits(this.output));
            break;

        case EXTENDED:
            throw new RuntimeException("DC[B].X not implemented yet");

        case PACKED:
            throw new RuntimeException("DC[B].P not implemented yet");
        }
    }

    @Override
    public void reset(InstructionSize size) {
        this.size = size;
        this.output = 0;
    }

    @Override
    public Void visitFloat(double value) {
        this.output = value;
        return null;
    }

    @Override
    public Void visitFunction(Function value) {
        this.context.addTentativeMessage(new FunctionCannotBeConvertedToRealErrorMessage());
        return null;
    }

    @Override
    public Void visitSignedInt(long value) {
        return this.visitFloat(value);
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
        return this.visitFloat(DcFloatValueVisitor.unsignedToFloat(value));
    }

}
