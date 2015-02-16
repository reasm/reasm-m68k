package org.reasm.m68k.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.source.LogicalLine;
import org.reasm.commons.source.LogicalLineAttributes;
import org.reasm.commons.source.LogicalLineFactory;

@Immutable
final class M68KLogicalLineFactory implements LogicalLineFactory {

    /** The single instance of the {@link M68KLogicalLineFactory} class. */
    @Nonnull
    static final M68KLogicalLineFactory INSTANCE = new M68KLogicalLineFactory();

    private M68KLogicalLineFactory() {
    }

    @Override
    public final LogicalLine createLogicalLine(LogicalLineAttributes attributes) {
        return new M68KLogicalLine(attributes);
    }

    @Override
    public final Class<? extends LogicalLine> getOutputType() {
        return M68KLogicalLine.class;
    }

}
