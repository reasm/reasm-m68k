package org.reasm.m68k;

import javax.annotation.Nonnull;

/**
 * An implementation of {@link ParserReader} reading from a {@link CharSequence}.
 *
 * @author Francis Gagné
 */
public final class CharSequenceParserReader implements ParserReader<CharSequenceParserReader> {

    @Nonnull
    private final CharSequence charSequence;
    private int position;

    /**
     * Initializes a new CharSequenceParserReader.
     *
     * @param charSequence
     *            the {@link CharSequence} to read from
     */
    public CharSequenceParserReader(@Nonnull CharSequence charSequence) {
        if (charSequence == null) {
            throw new NullPointerException("charSequence");
        }

        this.charSequence = charSequence;
    }

    /**
     * Initializes a new CharSequenceParserReader.
     *
     * @param charSequence
     *            the {@link CharSequence} to read from
     * @param position
     *            the reader's initial position
     */
    public CharSequenceParserReader(@Nonnull CharSequence charSequence, int position) {
        if (charSequence == null) {
            throw new NullPointerException("charSequence");
        }

        if (position < 0) {
            throw new IllegalArgumentException("position < 0");
        }

        if (position > charSequence.length()) {
            throw new IllegalArgumentException("position > charSequence.length()");
        }

        this.charSequence = charSequence;
        this.position = position;
    }

    @Override
    public final void advance() {
        if (this.position < this.charSequence.length()) {
            this.position += Character.charCount(this.getCurrentCodePoint());
        }
    }

    @Override
    public final void copyFrom(@Nonnull CharSequenceParserReader other) {
        if (other == null) {
            throw new NullPointerException("other");
        }

        if (this.charSequence != other.charSequence) {
            throw new IllegalArgumentException("The other reader's CharSequence is different from this reader's CharSequence.");
        }

        this.position = other.position;
    }

    @Nonnull
    @Override
    public final CharSequenceParserReader duplicate() {
        return new CharSequenceParserReader(this.charSequence, this.position);
    }

    /**
     * Gets the {@link CharSequence} that this reader reads from.
     *
     * @return the {@link CharSequence}
     */
    @Nonnull
    public final CharSequence getCharSequence() {
        return this.charSequence;
    }

    @Override
    public final int getCurrentCodePoint() {
        if (this.position >= this.charSequence.length()) {
            return -1;
        }

        return Character.codePointAt(this.charSequence, this.position);
    }

    /**
     * Gets this reader's current position.
     *
     * @return the reader's position
     */
    public final int getPosition() {
        return this.position;
    }

}
