package org.reasm.m68k;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import javax.annotation.Nonnull;

import org.junit.Test;

/**
 * Test class for {@link CharSequenceParserReader}.
 *
 * @author Francis Gagné
 */
public class CharSequenceParserReaderTest {

    @Nonnull
    private static final CharSequence CHAR_SEQUENCE = new String("abc");
    @Nonnull
    private static final CharSequence CHAR_SEQUENCE_NON_BMP = new String("x\uD83C\uDF41y");

    /**
     * Asserts that {@link CharSequenceParserReader#advance()} advances the {@link CharSequenceParserReader} by one code point.
     */
    @Test
    public void advance() {
        final CharSequenceParserReader r = new CharSequenceParserReader(CHAR_SEQUENCE);
        assertThat(r.getPosition(), is(0));
        assertThat(r.getCurrentCodePoint(), is(0x61));

        r.advance();
        assertThat(r.getPosition(), is(1));
        assertThat(r.getCurrentCodePoint(), is(0x62));

        r.advance();
        assertThat(r.getPosition(), is(2));
        assertThat(r.getCurrentCodePoint(), is(0x63));

        r.advance();
        assertThat(r.getPosition(), is(3));
        assertThat(r.getCurrentCodePoint(), is(-1));

        r.advance();
        assertThat(r.getPosition(), is(3));
        assertThat(r.getCurrentCodePoint(), is(-1));
    }

    /**
     * Asserts that {@link CharSequenceParserReader#advance()} advances the {@link CharSequenceParserReader} by one code point.
     */
    @Test
    public void advanceNonBmp() {
        final CharSequenceParserReader r = new CharSequenceParserReader(CHAR_SEQUENCE_NON_BMP);
        assertThat(r.getPosition(), is(0));
        assertThat(r.getCurrentCodePoint(), is(0x78));

        r.advance();
        assertThat(r.getPosition(), is(1));
        assertThat(r.getCurrentCodePoint(), is(0x1F341));

        r.advance();
        assertThat(r.getPosition(), is(3));
        assertThat(r.getCurrentCodePoint(), is(0x79));

        r.advance();
        assertThat(r.getPosition(), is(4));
        assertThat(r.getCurrentCodePoint(), is(-1));

        r.advance();
        assertThat(r.getPosition(), is(4));
        assertThat(r.getCurrentCodePoint(), is(-1));
    }

    /**
     * Asserts that {@link CharSequenceParserReader#CharSequenceParserReader(CharSequence)} correctly initializes a
     * {@link CharSequenceParserReader}.
     */
    @Test
    public void charSequenceParserReaderCharSequence() {
        final CharSequenceParserReader r = new CharSequenceParserReader(CHAR_SEQUENCE);
        assertThat(r.getCharSequence(), is(sameInstance(CHAR_SEQUENCE)));
        assertThat(r.getPosition(), is(0));
        assertThat(r.getCurrentCodePoint(), is(0x61));
    }

    /**
     * Asserts that {@link CharSequenceParserReader#CharSequenceParserReader(CharSequence, int)} correctly initializes a
     * {@link CharSequenceParserReader}.
     */
    @Test
    public void charSequenceParserReaderCharSequenceInt() {
        final CharSequenceParserReader r = new CharSequenceParserReader(CHAR_SEQUENCE, 0);
        assertThat(r.getCharSequence(), is(sameInstance(CHAR_SEQUENCE)));
        assertThat(r.getPosition(), is(0));
        assertThat(r.getCurrentCodePoint(), is(0x61));
    }

    /**
     * Asserts that {@link CharSequenceParserReader#CharSequenceParserReader(CharSequence, int)} throws a
     * {@link NullPointerException} when the <code>charSequence</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void charSequenceParserReaderCharSequenceIntNullCharSequence() {
        new CharSequenceParserReader(null, 0);
    }

    /**
     * Asserts that {@link CharSequenceParserReader#CharSequenceParserReader(CharSequence, int)} accepts the end of the
     * {@link CharSequence} as the initial position.
     */
    @Test
    public void charSequenceParserReaderCharSequenceIntPositionAtEnd() {
        final CharSequenceParserReader r = new CharSequenceParserReader(CHAR_SEQUENCE, 3);
        assertThat(r.getCharSequence(), is(sameInstance(CHAR_SEQUENCE)));
        assertThat(r.getPosition(), is(3));
        assertThat(r.getCurrentCodePoint(), is(-1));
    }

    /**
     * Asserts that {@link CharSequenceParserReader#CharSequenceParserReader(CharSequence, int)} throws an
     * {@link IllegalArgumentException} when the <code>position</code> argument is greater than the {@link CharSequence}'s length.
     */
    @Test(expected = IllegalArgumentException.class)
    public void charSequenceParserReaderCharSequenceIntPositionTooHigh() {
        new CharSequenceParserReader(CHAR_SEQUENCE, 4);
    }

    /**
     * Asserts that {@link CharSequenceParserReader#CharSequenceParserReader(CharSequence, int)} throws an
     * {@link IllegalArgumentException} when the <code>position</code> argument is negative.
     */
    @Test(expected = IllegalArgumentException.class)
    public void charSequenceParserReaderCharSequenceIntPositionTooLow() {
        new CharSequenceParserReader(CHAR_SEQUENCE, -1);
    }

    /**
     * Asserts that {@link CharSequenceParserReader#CharSequenceParserReader(CharSequence)} throws a {@link NullPointerException}
     * when the <code>charSequence</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void charSequenceParserReaderCharSequenceNullCharSequence() {
        new CharSequenceParserReader(null);
    }

    /**
     * Asserts that {@link CharSequenceParserReader#copyFrom(CharSequenceParserReader)} copies the position of a different
     * compatible {@link CharSequenceParserReader}.
     */
    @Test
    public void copyFrom() {
        final CharSequenceParserReader r0 = new CharSequenceParserReader(CHAR_SEQUENCE, 1);
        final CharSequenceParserReader r1 = r0.duplicate();
        r1.advance();
        r0.copyFrom(r1);
        assertThat(r0.getCharSequence(), is(sameInstance(CHAR_SEQUENCE)));
        assertThat(r0.getPosition(), is(2));
        assertThat(r0.getCurrentCodePoint(), is(0x63));
    }

    /**
     * Asserts that {@link CharSequenceParserReader#copyFrom(CharSequenceParserReader)} throws a {@link NullPointerException} when
     * the <code>other</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void copyFromNull() {
        final CharSequenceParserReader r0 = new CharSequenceParserReader(CHAR_SEQUENCE, 1);
        r0.copyFrom(null);
    }

    /**
     * Asserts that {@link CharSequenceParserReader#copyFrom(CharSequenceParserReader)} throws an {@link IllegalArgumentException}
     * when the other {@link CharSequenceParserReader}'s CharSequence is different from the receiver's CharSequence.
     */
    @Test(expected = IllegalArgumentException.class)
    public void copyFromWrongCharSequence() {
        final CharSequenceParserReader r0 = new CharSequenceParserReader(CHAR_SEQUENCE, 1);
        final CharSequenceParserReader r1 = new CharSequenceParserReader(CHAR_SEQUENCE_NON_BMP, 1);
        r0.copyFrom(r1);
    }

    /**
     * Asserts that {@link CharSequenceParserReader#duplicate()} returns a new {@link CharSequenceParserReader} whose position is
     * independent from the original {@link CharSequenceParserReader}.
     */
    @Test
    public void duplicate() {
        final CharSequenceParserReader r0 = new CharSequenceParserReader(CHAR_SEQUENCE, 1);
        final CharSequenceParserReader r1 = r0.duplicate();
        assertThat(r1, is(not(sameInstance(r0))));

        assertThat(r0.getCharSequence(), is(sameInstance(CHAR_SEQUENCE)));
        assertThat(r0.getPosition(), is(1));
        assertThat(r0.getCurrentCodePoint(), is(0x62));
        assertThat(r1.getCharSequence(), is(sameInstance(CHAR_SEQUENCE)));
        assertThat(r1.getPosition(), is(1));
        assertThat(r1.getCurrentCodePoint(), is(0x62));

        r1.advance();
        assertThat(r0.getCharSequence(), is(sameInstance(CHAR_SEQUENCE)));
        assertThat(r0.getPosition(), is(1));
        assertThat(r0.getCurrentCodePoint(), is(0x62));
        assertThat(r1.getCharSequence(), is(sameInstance(CHAR_SEQUENCE)));
        assertThat(r1.getPosition(), is(2));
        assertThat(r1.getCurrentCodePoint(), is(0x63));
    }

}
