package org.reasm.m68k.expressions.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;
import org.reasm.m68k.testhelpers.TokenMatcher;

/**
 * Test class for {@link Tokenizer}.
 *
 * @author Francis Gagné
 */
public class TokenizerTest {

    /**
     * Test class for {@link Tokenizer#changeToBinaryInteger()}.
     *
     * @author Francis Gagné
     */
    @RunWith(Parameterized.class)
    public static class ChangeToBinaryIntegerTest {

        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            final TokenMatcher invalid2 = new TokenMatcher(TokenType.INVALID, 0, 2);
            final TokenMatcher bin2 = new TokenMatcher(TokenType.BINARY_INTEGER, 0, 2);
            addDataItem("%", new TokenMatcher(TokenType.INVALID, 0, 1));
            addDataItem("%0", bin2);
            addDataItem("%1", bin2);
            addDataItem("%1100100101", new TokenMatcher(TokenType.BINARY_INTEGER, 0, 11));
            addDataItem("%2", invalid2);
            addDataItem("%A", invalid2);
            addDataItem("%012", new TokenMatcher(TokenType.INVALID, 0, 4));
            addDataItem("%0.W", bin2, new TokenMatcher(TokenType.PERIOD, 2, 3), new TokenMatcher(TokenType.IDENTIFIER, 3, 4));
        }

        /**
         * Gets the test data.
         *
         * @return the test data
         */
        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static void addDataItem(@Nonnull String input, @Nonnull TokenMatcher... expectedResult) {
            TEST_DATA.add(new Object[] { input, expectedResult });
        }

        @Nonnull
        private final String input;
        @Nonnull
        private final TokenMatcher[] expectedResult;

        /**
         * Initializes a new ChangeToBinaryIntegerTest.
         *
         * @param input
         *            the text to tokenize
         * @param expectedResult
         *            an array of {@link TokenMatcher}s that match the tokens that the tokenizer is expected to produce
         */
        public ChangeToBinaryIntegerTest(@Nonnull String input, @Nonnull TokenMatcher[] expectedResult) {
            this.input = input;
            this.expectedResult = expectedResult;
        }

        /**
         * Asserts that {@link Tokenizer#changeToBinaryInteger()} parses the correct tokens after changing the first token to a
         * binary integer.
         */
        @Test
        public void test() {
            Tokenizer tokenizer = new Tokenizer();
            tokenizer.setCharSequence(this.input);
            tokenizer.changeToBinaryInteger();

            int i = 0;
            for (; tokenizer.getTokenType() != TokenType.END; tokenizer.advance(), i++) {
                assertThat(i, is(not(this.expectedResult.length)));
                assertThat(tokenizer, this.expectedResult[i]);
            }

            assertThat(i, is(this.expectedResult.length));
        }

    }

    /**
     * Test suite for {@link TokenizerTest} and its inner classes.
     *
     * @author Francis Gagné
     */
    @RunWith(org.junit.runners.Suite.class)
    @SuiteClasses({ TokenizerTest.class, ChangeToBinaryIntegerTest.class, TokenizeTest.class })
    public static class Suite {
    }

    /**
     * Parameterized test class for {@link Tokenizer}.
     *
     * @author Francis Gagné
     */
    @RunWith(Parameterized.class)
    public static class TokenizeTest {

        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            // No tokens
            addDataItem("");
            addDataItem("\t");
            addDataItem("\r");
            addDataItem("\n");
            addDataItem("\r\n");
            addDataItem(" ");
            addDataItem(" \r\n\t");

            // Single tokens
            final TokenMatcher invalid1 = new TokenMatcher(TokenType.INVALID, 0, 1);
            final TokenMatcher invalid2 = new TokenMatcher(TokenType.INVALID, 0, 2);
            final TokenMatcher invalid3 = new TokenMatcher(TokenType.INVALID, 0, 3);
            final TokenMatcher invalid4 = new TokenMatcher(TokenType.INVALID, 0, 4);
            final TokenMatcher invalid5 = new TokenMatcher(TokenType.INVALID, 0, 5);
            final TokenMatcher invalid6 = new TokenMatcher(TokenType.INVALID, 0, 6);
            final TokenMatcher invalid7 = new TokenMatcher(TokenType.INVALID, 0, 7);
            final TokenMatcher operator1 = new TokenMatcher(TokenType.OPERATOR, 0, 1);
            final TokenMatcher operator2 = new TokenMatcher(TokenType.OPERATOR, 0, 2);
            final TokenMatcher string3 = new TokenMatcher(TokenType.STRING, 0, 3);
            final TokenMatcher string9 = new TokenMatcher(TokenType.STRING, 0, 9);
            final TokenMatcher hex2 = new TokenMatcher(TokenType.HEXADECIMAL_INTEGER, 0, 2);
            final TokenMatcher plusOrMinusSequence1 = new TokenMatcher(TokenType.PLUS_OR_MINUS_SEQUENCE, 0, 1);
            final TokenMatcher plusOrMinusSequence5 = new TokenMatcher(TokenType.PLUS_OR_MINUS_SEQUENCE, 0, 5);
            final TokenMatcher dec1 = new TokenMatcher(TokenType.DECIMAL_INTEGER, 0, 1);
            final TokenMatcher real2 = new TokenMatcher(TokenType.REAL, 0, 2);
            final TokenMatcher real3 = new TokenMatcher(TokenType.REAL, 0, 3);
            final TokenMatcher real4 = new TokenMatcher(TokenType.REAL, 0, 4);
            final TokenMatcher real5 = new TokenMatcher(TokenType.REAL, 0, 5);
            final TokenMatcher real6 = new TokenMatcher(TokenType.REAL, 0, 6);
            addDataItem("!", operator1);
            addDataItem("!=", operator2);
            addDataItem("\"", invalid1);
            addDataItem("\"a", invalid2);
            addDataItem("\"a\"", string3);
            addDataItem("\"a\\\"b\\\"c\"", string9);
            addDataItem("#", new TokenMatcher(TokenType.IMMEDIATE, 0, 1));
            addDataItem("$", invalid1);
            addDataItem("$0", hex2);
            addDataItem("$@", invalid2);
            addDataItem("$A", hex2);
            addDataItem("$F", hex2);
            addDataItem("$0123456789ABCDEFabcdef", new TokenMatcher(TokenType.HEXADECIMAL_INTEGER, 0, 23));
            addDataItem("$G", invalid2);
            addDataItem("$FG", invalid3);
            addDataItem("$a", hex2);
            addDataItem("$f", hex2);
            addDataItem("$g", invalid2);
            addDataItem("$fg", invalid3);
            addDataItem("%", operator1);
            addDataItem("&", operator1);
            addDataItem("&&", operator2);
            addDataItem("'", invalid1);
            addDataItem("'a", invalid2);
            addDataItem("'a'", string3);
            addDataItem("'a\\'b\\'c'", string9);
            addDataItem("(", new TokenMatcher(TokenType.OPENING_PARENTHESIS, 0, 1));
            addDataItem(")", new TokenMatcher(TokenType.CLOSING_PARENTHESIS, 0, 1));
            addDataItem("*", operator1);
            addDataItem("+", plusOrMinusSequence1);
            addDataItem("+++++", plusOrMinusSequence5);
            addDataItem(",", new TokenMatcher(TokenType.COMMA, 0, 1));
            addDataItem("-", plusOrMinusSequence1);
            addDataItem("-----", plusOrMinusSequence5);
            addDataItem(".", new TokenMatcher(TokenType.PERIOD, 0, 1));
            addDataItem(".0", real2);
            addDataItem(".0E", invalid3);
            addDataItem(".0E0", real4);
            addDataItem(".0E+0", real5);
            addDataItem("/", operator1);
            addDataItem("0", dec1);
            addDataItem("9", dec1);
            addDataItem("0123456789", new TokenMatcher(TokenType.DECIMAL_INTEGER, 0, 10));
            addDataItem("0.", real2);
            addDataItem("0.0", real3);
            addDataItem("0.00", real4);
            addDataItem("0.0E", invalid4);
            addDataItem("0.0E0", real5);
            addDataItem("0.0e0", real5);
            addDataItem("0.0E0a", invalid6);
            addDataItem("0.0E+0", real6);
            addDataItem("0.0E+0a", invalid7);
            addDataItem("0.0E+a", invalid6);
            addDataItem("0.0E-0", real6);
            addDataItem("0.0E-0a", invalid7);
            addDataItem("0.0E-a", invalid6);
            addDataItem("0.0Ea", invalid5);
            addDataItem("0.0a", invalid4);
            addDataItem("0E", invalid2);
            addDataItem("0E0", real3);
            addDataItem("0E+0", real4);
            addDataItem("0a", invalid2);
            addDataItem(":", new TokenMatcher(TokenType.CONDITIONAL_OPERATOR_SECOND, 0, 1));
            addDataItem(";", invalid1);
            addDataItem("<", operator1);
            addDataItem("<<", operator2);
            addDataItem("<=", operator2);
            addDataItem("<>", operator2);
            addDataItem("=", operator1);
            addDataItem("==", operator2);
            addDataItem(">", operator1);
            addDataItem(">=", operator2);
            addDataItem(">>", operator2);
            addDataItem("?", new TokenMatcher(TokenType.CONDITIONAL_OPERATOR_FIRST, 0, 1));
            addDataItem("A", new TokenMatcher(TokenType.IDENTIFIER, 0, 1));
            addDataItem("A@b_c`d\u00A0é¶\uFF46¬9.h", new TokenMatcher(TokenType.IDENTIFIER, 0, 15));
            addDataItem("[", new TokenMatcher(TokenType.OPENING_BRACKET, 0, 1));
            addDataItem("\\", invalid1);
            addDataItem("\\0", invalid2);
            addDataItem("\\abc", invalid4);
            addDataItem("]", new TokenMatcher(TokenType.CLOSING_BRACKET, 0, 1));
            addDataItem("{", new TokenMatcher(TokenType.OPENING_BRACE, 0, 1));
            addDataItem("|", operator1);
            addDataItem("||", operator2);
            addDataItem("}", new TokenMatcher(TokenType.CLOSING_BRACE, 0, 1));

            // Multiple tokens
            final TokenMatcher operator_1_2 = new TokenMatcher(TokenType.OPERATOR, 1, 2);
            addDataItem("!!", operator1, operator_1_2);
            addDataItem("+++++0", plusOrMinusSequence5, new TokenMatcher(TokenType.DECIMAL_INTEGER, 5, 6));
            addDataItem("0.a", dec1, new TokenMatcher(TokenType.PERIOD, 1, 2), new TokenMatcher(TokenType.IDENTIFIER, 2, 3));
            addDataItem("0.*", real2, new TokenMatcher(TokenType.OPERATOR, 2, 3));
            addDataItem("0*0", dec1, operator_1_2, new TokenMatcher(TokenType.DECIMAL_INTEGER, 2, 3));
            addDataItem(" 0 * 0 ", new TokenMatcher(TokenType.DECIMAL_INTEGER, 1, 2), new TokenMatcher(TokenType.OPERATOR, 3, 4),
                    new TokenMatcher(TokenType.DECIMAL_INTEGER, 5, 6));
            addDataItem("$0.W", hex2, new TokenMatcher(TokenType.PERIOD, 2, 3), new TokenMatcher(TokenType.IDENTIFIER, 3, 4));
            addDataItem("0%0", dec1, operator_1_2, new TokenMatcher(TokenType.DECIMAL_INTEGER, 2, 3));
            addDataItem("<!", operator1, operator_1_2);
            addDataItem(">!", operator1, operator_1_2);
            addDataItem("\\\\", invalid1, new TokenMatcher(TokenType.INVALID, 1, 2));
        }

        /**
         * Returns the test data.
         *
         * @return the test data
         */
        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static void addDataItem(@Nonnull String input, @Nonnull TokenMatcher... expectedResult) {
            TEST_DATA.add(new Object[] { input, expectedResult });
        }

        @Nonnull
        private final String input;
        @Nonnull
        private final TokenMatcher[] expectedResult;

        /**
         * Initializes a new TokenizeTest.
         *
         * @param input
         *            the text to tokenize
         * @param expectedResult
         *            an array of {@link TokenMatcher}s that match the tokens that the tokenizer is expected to produce
         */
        public TokenizeTest(@Nonnull String input, @Nonnull TokenMatcher[] expectedResult) {
            this.input = input;
            this.expectedResult = expectedResult;
        }

        /**
         * Asserts that {@link Tokenizer#advance()} parses the correct tokens.
         */
        @Test
        public void test() {
            Tokenizer tokenizer = new Tokenizer();
            tokenizer.setCharSequence(this.input);

            int i = 0;
            for (; tokenizer.getTokenType() != TokenType.END; tokenizer.advance(), i++) {
                assertThat(i, is(not(this.expectedResult.length)));
                assertThat(tokenizer, this.expectedResult[i]);
            }

            assertThat(i, is(this.expectedResult.length));
        }

    }

    /**
     * Asserts that {@link Tokenizer#breakSequence()} breaks a {@link TokenType#PLUS_OR_MINUS_SEQUENCE} token into a series of
     * {@link TokenType#OPERATOR} tokens.
     */
    @Test
    public void breakSequence() {
        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence("+++2");
        assertThat(tokenizer, new TokenMatcher(TokenType.PLUS_OR_MINUS_SEQUENCE, 0, 3));
        tokenizer.breakSequence();
        assertThat(tokenizer, new TokenMatcher(TokenType.OPERATOR, 0, 1));
        tokenizer.advance();
        assertThat(tokenizer, new TokenMatcher(TokenType.OPERATOR, 1, 2));
        tokenizer.advance();
        assertThat(tokenizer, new TokenMatcher(TokenType.OPERATOR, 2, 3));
        tokenizer.advance();
        assertThat(tokenizer, new TokenMatcher(TokenType.DECIMAL_INTEGER, 3, 4));
        tokenizer.advance();
        assertThat(tokenizer, new TokenMatcher(TokenType.END, 4, 4));
    }

    /**
     * Asserts that {@link Tokenizer#breakSequence()} throws an {@link IllegalStateException} when the current token has the wrong
     * type.
     */
    @Test(expected = IllegalStateException.class)
    public void breakSequenceWrongTokenType() {
        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence("1");
        assertThat(tokenizer, new TokenMatcher(TokenType.DECIMAL_INTEGER, 0, 1));
        tokenizer.breakSequence();
    }

    /**
     * Asserts that {@link Tokenizer#changeToBinaryInteger()} throws an {@link IllegalStateException} when the current token has the
     * wrong type.
     */
    @Test(expected = IllegalStateException.class)
    public void changeToBinaryIntegerWrongOperator() {
        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence("^");
        assertThat(tokenizer, new TokenMatcher(TokenType.OPERATOR, 0, 1));
        tokenizer.changeToBinaryInteger();
    }

    /**
     * {@link Tokenizer#changeToBinaryInteger()} throws an {@link IllegalStateException} when the current token has the wrong type.
     */
    @Test(expected = IllegalStateException.class)
    public void changeToBinaryIntegerWrongTokenType() {
        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence("1");
        assertThat(tokenizer, new TokenMatcher(TokenType.DECIMAL_INTEGER, 0, 1));
        tokenizer.changeToBinaryInteger();
    }

    /**
     * Asserts that {@link Tokenizer#copyFrom(Tokenizer)} copies the attributes of another {@link Tokenizer}.
     */
    @Test
    public void copyFrom() {
        final Tokenizer tokenizer0 = new Tokenizer();
        tokenizer0.setCharSequence("abc + def");
        final Tokenizer tokenizer1 = tokenizer0.duplicateAndAdvance();

        tokenizer0.copyFrom(tokenizer1);
        assertThat(tokenizer0.getTokenType(), is(TokenType.PLUS_OR_MINUS_SEQUENCE));
        assertThat(tokenizer0.getTokenStart(), is(4));
        assertThat(tokenizer0.getTokenEnd(), is(5));

        tokenizer0.advance();
        assertThat(tokenizer0.getTokenType(), is(TokenType.IDENTIFIER));
        assertThat(tokenizer0.getTokenStart(), is(6));
        assertThat(tokenizer0.getTokenEnd(), is(9));
    }

    /**
     * Asserts that {@link Tokenizer#duplicateAndAdvance()} returns a new {@link Tokenizer} that is positioned on the token
     * following the original {@link Tokenizer}'s current token.
     */
    @Test
    public void duplicateAndAdvance() {
        final Tokenizer tokenizer0 = new Tokenizer();
        tokenizer0.setCharSequence("abc + def");

        final Tokenizer tokenizer1 = tokenizer0.duplicateAndAdvance();
        assertThat(tokenizer1, is(not(sameInstance(tokenizer0))));
        assertThat(tokenizer0.getTokenType(), is(TokenType.IDENTIFIER));
        assertThat(tokenizer1.getTokenType(), is(TokenType.PLUS_OR_MINUS_SEQUENCE));
        assertThat(tokenizer1.getTokenStart(), is(4));
        assertThat(tokenizer1.getTokenEnd(), is(5));
    }

    /**
     * Asserts that {@link Tokenizer#getTokenText()} returns a {@link CharSequence} that contains the text of the tokenizer's
     * current token.
     */
    @Test
    public void getTokenText() {
        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence("abc + def");
        assertThat(tokenizer.getTokenText().toString(), is("abc"));
    }

    /**
     * Asserts that {@link Tokenizer#setCharSequence(CharSequence)} throws a {@link NullPointerException} when the
     * <code>charSequence</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void setCharSequenceNull() {
        new Tokenizer().setCharSequence(null);
    }

    /**
     * Asserts that {@link Tokenizer#tokenEqualsString(String)} returns <code>false</code> when the tokenizer's current token text's
     * length is different from the specified string's length.
     */
    @Test
    public void tokenEqualsStringDifferentLength() {
        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence("a");
        assertThat(tokenizer.tokenEqualsString("ab"), is(false));
    }

    /**
     * Asserts that {@link Tokenizer#tokenEqualsString(String)} returns <code>true</code> when the tokenizer's current token text is
     * different from the specified string.
     */
    @Test
    public void tokenEqualsStringDifferentString() {
        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence("abc");
        assertThat(tokenizer.tokenEqualsString("aac"), is(false));
    }

    /**
     * Asserts that {@link Tokenizer#tokenEqualsString(String)} returns <code>false</code> when the <code>string</code> argument is
     * <code>null</code>.
     */
    @Test
    public void tokenEqualsStringNull() {
        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence("a");
        assertThat(tokenizer.tokenEqualsString(null), is(false));
    }

    /**
     * Asserts that {@link Tokenizer#tokenEqualsString(String)} returns <code>true</code> when the tokenizer's current token text is
     * equal to the specified string.
     */
    @Test
    public void tokenEqualsStringTrue() {
        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence("abc + def");
        assertThat(tokenizer.tokenEqualsString("abc"), is(true));
    }

}
