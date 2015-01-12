package org.reasm.m68k.parseerrors;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.source.BlockDirective;
import org.reasm.source.ParseError;

/**
 * A parse error where a block is open but has no corresponding ending directive.
 *
 * @author Francis Gagné
 */
@Immutable
public class UnclosedBlockParseError extends ParseError {

    @Nonnull
    private final BlockDirective startingBlockDirective;

    /**
     * Initializes a new UnclosedBlockParseError.
     *
     * @param startingBlockDirective
     *            the starting directive of the block that was not closed
     */
    public UnclosedBlockParseError(@Nonnull BlockDirective startingBlockDirective) {
        super("Block starting with \"" + Objects.requireNonNull(startingBlockDirective, "startingBlockDirective")
                + "\" is not closed");
        this.startingBlockDirective = startingBlockDirective;
    }

    /**
     * Gets the starting directive of the block that was not closed.
     *
     * @return the starting directive
     */
    @Nonnull
    public final BlockDirective getStartingBlockDirective() {
        return this.startingBlockDirective;
    }

}
