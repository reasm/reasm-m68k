package org.reasm.m68k.source;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;
import org.reasm.source.SourceNode;

/**
 * Wraps a {@link LogicalLine} whose mnemonic is a directive that delimits a block in order to skip macro lookup.
 *
 * @author Francis Gagné
 */
@Immutable
public final class BlockDirectiveLine extends SourceNode {

    @Nonnull
    private final LogicalLine logicalLine;

    BlockDirectiveLine(@Nonnull LogicalLine logicalLine) {
        super(logicalLine.getLength(), logicalLine.getParseError());
        this.logicalLine = logicalLine;
    }

    /**
     * Gets the wrapped {@link LogicalLine}.
     *
     * @return the {@link LogicalLine}
     */
    @Nonnull
    public final LogicalLine getLogicalLine() {
        return this.logicalLine;
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        SourceNodesImpl.assembleBlockDirectiveLine(builder);
    }

}
