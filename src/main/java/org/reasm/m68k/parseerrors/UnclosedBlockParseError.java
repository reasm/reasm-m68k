package org.reasm.m68k.parseerrors;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.source.ParseError;

/**
 * A parse error where a block is open but has no corresponding ending directive.
 *
 * @author Francis Gagné
 */
@Immutable
public class UnclosedBlockParseError extends ParseError {

    @Nonnull
    private final String startingDirective;

    /**
     * Initializes a new UnclosedBlockParseError.
     *
     * @param startingDirective
     *            the starting directive of the block that was not closed
     */
    public UnclosedBlockParseError(@Nonnull String startingDirective) {
        super("Block starting with \"" + Objects.requireNonNull(startingDirective, "startingDirective") + "\" is not closed");
        this.startingDirective = startingDirective;
    }

    /**
     * Gets the starting directive of the block that was not closed.
     *
     * @return the starting directive
     */
    @Nonnull
    public final String getStartingDirective() {
        return this.startingDirective;
    }

}
