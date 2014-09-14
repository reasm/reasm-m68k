package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

import org.reasm.Function;
import org.reasm.ValueVisitor;
import org.reasm.m68k.messages.FunctionCannotBeConvertedToStringErrorMessage;

import com.google.common.primitives.UnsignedLongs;

final class StringValueVisitor implements ValueVisitor<String> {

    @Nonnull
    private final M68KAssemblyContext context;

    StringValueVisitor(@Nonnull M68KAssemblyContext context) {
        this.context = context;
    }

    @Override
    public String visitFloat(double value) {
        return Double.toString(value);
    }

    @Override
    public String visitFunction(Function value) {
        this.context.addTentativeMessage(new FunctionCannotBeConvertedToStringErrorMessage());
        return null;
    }

    @Override
    public String visitSignedInt(long value) {
        return Long.toString(value);
    }

    @Override
    public String visitString(String value) {
        return value;
    }

    @Override
    public String visitUndetermined() {
        return null;
    }

    @Override
    public String visitUnsignedInt(long value) {
        return UnsignedLongs.toString(value);
    }

}
