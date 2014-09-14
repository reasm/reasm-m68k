package org.reasm.m68k;

import javax.annotation.Nonnull;

/**
 * A reader of code points in a string.
 *
 * @param <T>
 *            the concrete type of ParserReader
 *
 * @author Francis Gagné
 */
public interface ParserReader<T extends ParserReader<T>> {

    /**
     * Advances the reader to the next code point.
     */
    void advance();

    /**
     * Copies the state from another reader.
     *
     * @param other
     *            the other reader to copy the state from
     */
    void copyFrom(@Nonnull T other);

    /**
     * Creates a new instance of T with the current state of this reader. The new reader is independant from this reader.
     *
     * @return the new reader
     */
    @Nonnull
    T duplicate();

    /**
     * Gets the current code point, or -1 if the reader is at the end of the string.
     *
     * @return the current code point
     */
    int getCurrentCodePoint();

}
