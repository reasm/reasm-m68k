package org.reasm.m68k;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test class for {@link Syntax}.
 *
 * @author Francis Gagné
 */
public class SyntaxTest {

    /**
     * Asserts that {@link Syntax#isBinDigit(int)} returns <code>true</code> for binary digits and <code>false</code> for other code
     * points.
     */
    @Test
    public void isBinDigit() {
        assertThat(Syntax.isBinDigit(0), is(false));
        assertThat(Syntax.isBinDigit(9), is(false));
        assertThat(Syntax.isBinDigit('.'), is(false));
        assertThat(Syntax.isBinDigit(0x2F), is(false));
        assertThat(Syntax.isBinDigit('0'), is(true));
        assertThat(Syntax.isBinDigit('1'), is(true));
        assertThat(Syntax.isBinDigit('2'), is(false));
        assertThat(Syntax.isBinDigit('A'), is(false));
        assertThat(Syntax.isBinDigit(0x660), is(false)); // ARABIC-INDIC DIGIT ZERO
    }

    /**
     * Asserts that {@link Syntax#isDigit(int)} returns <code>true</code> for decimal digits and <code>false</code> for other code
     * points.
     */
    @Test
    public void isDigit() {
        assertThat(Syntax.isDigit(0), is(false));
        assertThat(Syntax.isDigit(9), is(false));
        assertThat(Syntax.isDigit('.'), is(false));
        assertThat(Syntax.isDigit(0x2F), is(false));
        assertThat(Syntax.isDigit('0'), is(true));
        assertThat(Syntax.isDigit('3'), is(true));
        assertThat(Syntax.isDigit('9'), is(true));
        assertThat(Syntax.isDigit(0x3A), is(false));
        assertThat(Syntax.isDigit('A'), is(false));
        assertThat(Syntax.isDigit(0x660), is(false)); // ARABIC-INDIC DIGIT ZERO
    }

    /**
     * Asserts that {@link Syntax#isHexDigit(int)} returns <code>true</code> for hexadecimal digits and <code>false</code> for other
     * code points.
     */
    @Test
    public void isHexDigit() {
        assertThat(Syntax.isHexDigit(0), is(false));
        assertThat(Syntax.isHexDigit(9), is(false));
        assertThat(Syntax.isHexDigit('.'), is(false));
        assertThat(Syntax.isHexDigit(0x2F), is(false));
        assertThat(Syntax.isHexDigit('0'), is(true));
        assertThat(Syntax.isHexDigit('3'), is(true));
        assertThat(Syntax.isHexDigit('9'), is(true));
        assertThat(Syntax.isHexDigit(0x3A), is(false));
        assertThat(Syntax.isHexDigit('@'), is(false));
        assertThat(Syntax.isHexDigit('A'), is(true));
        assertThat(Syntax.isHexDigit('F'), is(true));
        assertThat(Syntax.isHexDigit('G'), is(false));
        assertThat(Syntax.isHexDigit('`'), is(false));
        assertThat(Syntax.isHexDigit('a'), is(true));
        assertThat(Syntax.isHexDigit('f'), is(true));
        assertThat(Syntax.isHexDigit('g'), is(false));
        assertThat(Syntax.isHexDigit(0x660), is(false)); // ARABIC-INDIC DIGIT ZERO
    }

    /**
     * Asserts that {@link Syntax#isValidIdentifierCodePoint(int)} returns <code>true</code> for code points that are valid as part
     * of an identifier and <code>false</code> for other code points.
     */
    @Test
    public void isValidIdentifierCodePoint() {
        assertThat(Syntax.isValidIdentifierCodePoint(0), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('\t'), is(false));
        assertThat(Syntax.isValidIdentifierCodePoint('\n'), is(false));
        assertThat(Syntax.isValidIdentifierCodePoint(0xB), is(true)); // LINE TABULATION
        assertThat(Syntax.isValidIdentifierCodePoint('\f'), is(false));
        assertThat(Syntax.isValidIdentifierCodePoint('\r'), is(false));
        assertThat(Syntax.isValidIdentifierCodePoint(' '), is(false));
        assertThat(Syntax.isValidIdentifierCodePoint('"'), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('#'), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('$'), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('\''), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('.'), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('0'), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('@'), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('A'), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('`'), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint('a'), is(true));
        assertThat(Syntax.isValidIdentifierCodePoint(-1), is(false));
    }

    /**
     * Asserts that {@link Syntax#isValidIdentifierInitialCodePoint(int)} returns <code>true</code> for code points that are valid
     * as the first code point of an identifier and <code>false</code> for other code points.
     */
    @Test
    public void isValidIdentifierInitialCodePoint() {
        assertThat(Syntax.isValidIdentifierInitialCodePoint(0), is(true));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('\t'), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('\n'), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint(0xB), is(true)); // LINE TABULATION
        assertThat(Syntax.isValidIdentifierInitialCodePoint('\f'), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('\r'), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint(' '), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('"'), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('#'), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('$'), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('\''), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('.'), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('0'), is(false));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('@'), is(true));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('A'), is(true));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('`'), is(true));
        assertThat(Syntax.isValidIdentifierInitialCodePoint('a'), is(true));
        assertThat(Syntax.isValidIdentifierInitialCodePoint(-1), is(false));
    }

    /**
     * Asserts that {@link Syntax#isValidNumberInitialCodePoint(int)} returns <code>true</code> for decimal digits and for the
     * decimal point, and <code>false</code> for other code points.
     */
    public void isValidNumberInitialCodePoint() {
        assertThat(Syntax.isValidNumberInitialCodePoint(0), is(false));
        assertThat(Syntax.isValidNumberInitialCodePoint(9), is(false));
        assertThat(Syntax.isValidNumberInitialCodePoint('.'), is(true));
        assertThat(Syntax.isValidNumberInitialCodePoint(0x2F), is(false));
        assertThat(Syntax.isValidNumberInitialCodePoint('0'), is(true));
        assertThat(Syntax.isValidNumberInitialCodePoint('3'), is(true));
        assertThat(Syntax.isValidNumberInitialCodePoint('9'), is(true));
        assertThat(Syntax.isValidNumberInitialCodePoint(0x3A), is(false));
        assertThat(Syntax.isValidNumberInitialCodePoint('A'), is(false));
        assertThat(Syntax.isValidNumberInitialCodePoint(0x660), is(false)); // ARABIC-INDIC DIGIT ZERO
    }

    /**
     * Asserts that {@link Syntax#isWhitespace(int)} returns <code>true</code> when the specified character is interpreted as
     * whitespace and <code>false</code> when it isn't.
     */
    @Test
    public void isWhitespace() {
        assertThat(Syntax.isWhitespace(0), is(false));
        assertThat(Syntax.isWhitespace('\t'), is(true));
        assertThat(Syntax.isWhitespace('\n'), is(true));
        assertThat(Syntax.isWhitespace(0xB), is(false)); // LINE TABULATION
        assertThat(Syntax.isWhitespace('\f'), is(true));
        assertThat(Syntax.isWhitespace('\r'), is(true));
        assertThat(Syntax.isWhitespace(' '), is(true));
        assertThat(Syntax.isWhitespace('0'), is(false));
        assertThat(Syntax.isWhitespace('A'), is(false));
        assertThat(Syntax.isWhitespace(0xA0), is(false)); // NO-BREAK SPACE
        assertThat(Syntax.isWhitespace(0x2000), is(false)); // EN QUAD
    }

}
