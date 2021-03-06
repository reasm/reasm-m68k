package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.Function;
import org.reasm.commons.messages.FunctionCannotBeConvertedToIntegerErrorMessage;
import org.reasm.commons.messages.StringTooLongErrorMessage;
import org.reasm.commons.messages.ValueOutOfRangeErrorMessage;

final class DcIntegerValueVisitor implements DcValueVisitor {

    @Nonnull
    private final M68KAssemblyContext context;
    @Nonnull
    private InstructionSize size = InstructionSize.DEFAULT;
    private long output;
    private boolean signed;
    @CheckForNull
    private ByteBuffer outputBytes;

    DcIntegerValueVisitor(@Nonnull M68KAssemblyContext context) {
        this.context = context;
    }

    @Override
    public void encode() throws IOException {
        switch (this.size) {
        case BYTE:
            if (this.outputBytes != null) {
                this.context.builder.appendAssembledData(this.outputBytes);
            } else {
                this.context.appendByte((byte) this.output);
            }

            break;

        case DEFAULT:
        case WORD:
        default:
            this.context.appendWord((short) this.output);
            break;

        case LONG:
            this.context.appendLong((int) this.output);
            break;

        case QUAD:
            this.context.appendQuad(this.output);
            break;
        }
    }

    @Override
    public void reset(InstructionSize size) {
        this.size = size;
        this.output = 0;
        this.signed = false;
        this.outputBytes = null;
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
        this.output = value;
        this.signed = true;
        this.validate();
        return null;
    }

    @Override
    public Void visitString(String value) {
        final ByteBuffer stringBytes = this.context.encoding.encode(value);

        int maxLength;

        switch (this.size) {
        case BYTE:
            // DC.B, when given a string operand, outputs the whole string (encoded)
            this.outputBytes = stringBytes;
            return null;

        case DEFAULT:
        case WORD:
        default:
            maxLength = 2;
            break;

        case LONG:
            maxLength = 4;
            break;

        case QUAD:
            maxLength = 8;
            break;
        }

        if (stringBytes.limit() > maxLength) {
            this.context.addTentativeMessage(new StringTooLongErrorMessage(value));
        }

        if (maxLength > stringBytes.limit()) {
            maxLength = stringBytes.limit();
        }

        long result = 0;
        for (; maxLength != 0; maxLength--) {
            result <<= 8;
            result |= stringBytes.get() & 0xFF;
        }

        this.output = result;
        this.signed = false;
        return null;
    }

    @Override
    public Void visitUndetermined() {
        return null;
    }

    @Override
    public Void visitUnsignedInt(long value) {
        this.output = value;
        this.signed = false;
        this.validate();
        return null;
    }

    /**
     * Validates the output value against the size. This is done once while visiting the value because {@link #encode()} may be
     * called multiple times in a <code>DCB</code> directive.
     */
    private void validate() {
        switch (this.size) {
        case BYTE:
            if (this.signed) {
                if (this.output < -0x80 || this.output > 0xFF) {
                    this.context.addTentativeMessage(new ValueOutOfRangeErrorMessage(this.output));
                }
            } else {
                if (this.output < 0 || this.output > 0xFF) {
                    this.context.addTentativeMessage(new ValueOutOfRangeErrorMessage(this.output));
                }
            }

            break;

        case WORD:
        case DEFAULT:
        default:
            if (this.signed) {
                if (this.output < -0x8000 || this.output > 0xFFFF) {
                    this.context.addTentativeMessage(new ValueOutOfRangeErrorMessage(this.output));
                }
            } else {
                if (this.output < 0 || this.output > 0xFFFF) {
                    this.context.addTentativeMessage(new ValueOutOfRangeErrorMessage(this.output));
                }
            }

            break;

        case LONG:
            if (this.signed) {
                if (this.output < -0x80000000L || this.output > 0xFFFFFFFFL) {
                    this.context.addTentativeMessage(new ValueOutOfRangeErrorMessage(this.output));
                }
            } else {
                if (this.output < 0 || this.output > 0xFFFFFFFFL) {
                    this.context.addTentativeMessage(new ValueOutOfRangeErrorMessage(this.output));
                }
            }

            break;

        case QUAD:
            break;
        }
    }

}
