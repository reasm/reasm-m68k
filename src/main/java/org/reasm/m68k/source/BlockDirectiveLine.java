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
    @Nonnull
    private final BlockDirective blockDirective;

    BlockDirectiveLine(@Nonnull LogicalLine logicalLine, @Nonnull BlockDirective blockDirective) {
        super(logicalLine.getLength(), logicalLine.getParseError());
        this.logicalLine = logicalLine;
        this.blockDirective = blockDirective;
    }

    /**
     * Gets the {@link BlockDirective} on this line.
     *
     * @return the {@link BlockDirective}
     */
    @Nonnull
    public final BlockDirective getBlockDirective() {
        return this.blockDirective;
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
