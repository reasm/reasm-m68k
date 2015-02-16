package org.reasm.m68k.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.source.BlockDirective;
import org.reasm.commons.source.BlockDirectiveLine;
import org.reasm.commons.source.BlockDirectiveLineFactory;
import org.reasm.commons.source.LogicalLine;

@Immutable
final class M68KBlockDirectiveLineFactory implements BlockDirectiveLineFactory {

    /** The single instance of the {@link M68KBlockDirectiveLineFactory} class. */
    @Nonnull
    static final M68KBlockDirectiveLineFactory INSTANCE = new M68KBlockDirectiveLineFactory();

    private M68KBlockDirectiveLineFactory() {
    }

    @Override
    public final BlockDirectiveLine createBlockDirectiveLine(LogicalLine logicalLine, BlockDirective blockDirective) {
        return new M68KBlockDirectiveLine(logicalLine, blockDirective);
    }

    @Override
    public final Class<? extends BlockDirectiveLine> getOutputType() {
        return M68KBlockDirectiveLine.class;
    }

}
