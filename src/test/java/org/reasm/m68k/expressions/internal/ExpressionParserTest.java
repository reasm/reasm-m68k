package org.reasm.m68k.expressions.internal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.FloatValue;
import org.reasm.StringValue;
import org.reasm.UnsignedIntValue;
import org.reasm.commons.messages.UnrecognizedEscapeSequenceWarningMessage;
import org.reasm.commons.util.ParserReader;
import org.reasm.expressions.*;
import org.reasm.messages.OverflowInLiteralWarningMessage;
import org.reasm.testhelpers.DummySymbolLookup;
import org.reasm.testhelpers.EquivalentAssemblyMessage;

import ca.fragag.Consumer;

/**
 * Parameterized test class for {@link ExpressionParser#parse(ParserReader, Consumer)}.
 *
 * @author Francis Gagné
 */
@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class ExpressionParserTest {

    /**
     * Stores the parameters and expected result for a test.
     *
     * @author Francis Gagné
     */
    @Immutable
    public static class DataItem {

        @Nonnull
        final String expressionText;
        final int finalPosition;
        @CheckForNull
        final Expression expectedExpression;
        @CheckForNull
        final List<Matcher<? super AssemblyMessage>> expectedAssemblyMessageMatchers;

        /**
         * Initializes a new DataItem.
         *
         * @param expressionText
         *            the text of the expression to parse
         * @param finalPosition
         *            the expected final position. Specify -1 to indicate that the parse is expected to fail with an
         *            {@link InvalidTokenException}.
         * @param expectedExpression
         *            the expected expression
         * @param expectedAssemblyMessages
         *            the expected assembly messages that should be generated when parsing the expression
         */
        public DataItem(@Nonnull String expressionText, int finalPosition, @CheckForNull Expression expectedExpression,
                @CheckForNull AssemblyMessage... expectedAssemblyMessages) {
            this.expressionText = expressionText;
            this.finalPosition = finalPosition;
            this.expectedExpression = expectedExpression;

            if (expectedAssemblyMessages == null) {
                this.expectedAssemblyMessageMatchers = null;
            } else {
                final ArrayList<Matcher<? super AssemblyMessage>> matchers = new ArrayList<>(expectedAssemblyMessages.length);
                for (AssemblyMessage expectedAssemblyMessage : expectedAssemblyMessages) {
                    matchers.add(new EquivalentAssemblyMessage(expectedAssemblyMessage));
                }

                this.expectedAssemblyMessageMatchers = matchers;
            }
        }

        @Nonnull
        @Override
        public String toString() {
            return this.expressionText;
        }

    }

    @Nonnull
    private static final ValueExpression VALUE_UINT_0 = new ValueExpression(new UnsignedIntValue(0));
    @Nonnull
    private static final ValueExpression VALUE_UINT_3 = new ValueExpression(new UnsignedIntValue(3));
    @Nonnull
    private static final ValueExpression VALUE_UINT_123456789 = new ValueExpression(new UnsignedIntValue(123456789));
    @Nonnull
    private static final ValueExpression VALUE_UINT_1234567890123456789 = new ValueExpression(new UnsignedIntValue(
            1234567890123456789L));
    @Nonnull
    private static final ValueExpression VALUE_UINT_18446744073709551615 = new ValueExpression(new UnsignedIntValue(-1));
    @Nonnull
    private static final ValueExpression VALUE_UINT_5097733592125636885 = new ValueExpression(new UnsignedIntValue(
            5097733592125636885L));

    @Nonnull
    private static final ValueExpression VALUE_FLOAT_3_POINT_5 = new ValueExpression(new FloatValue(3.5));

    @Nonnull
    private static final ValueExpression VALUE_STRING_EMPTY = new ValueExpression(new StringValue(""));
    @Nonnull
    private static final ValueExpression VALUE_STRING_ABCDEF = new ValueExpression(new StringValue("abcdef"));
    @Nonnull
    private static final ValueExpression VALUE_STRING_BACKSLASH = new ValueExpression(new StringValue("\\"));

    @Nonnull
    private static final IdentifierExpression IDENTIFIER_A = new IdentifierExpression("a", DummySymbolLookup.DEFAULT);
    @Nonnull
    private static final IdentifierExpression IDENTIFIER_B = new IdentifierExpression("b", DummySymbolLookup.DEFAULT);
    @Nonnull
    private static final IdentifierExpression IDENTIFIER_C = new IdentifierExpression("c", DummySymbolLookup.DEFAULT);
    @Nonnull
    private static final IdentifierExpression IDENTIFIER_D = new IdentifierExpression("d", DummySymbolLookup.DEFAULT);
    @Nonnull
    private static final IdentifierExpression IDENTIFIER_E = new IdentifierExpression("e", DummySymbolLookup.DEFAULT);
    @Nonnull
    private static final IdentifierExpression IDENTIFIER_FOO = new IdentifierExpression("foo", DummySymbolLookup.DEFAULT);

    @Nonnull
    private static final AssemblyMessage[] DONT_CHECK_ASSEMBLY_MESSAGES = null;

    @Nonnull
    private static final UnrecognizedEscapeSequenceWarningMessage UNRECOGNIZED_ESCAPE_SEQUENCE_WARNING_MESSAGE = new UnrecognizedEscapeSequenceWarningMessage(
            'z');

    @Nonnull
    private static final Object[][] TEST_DATA_ARRAY = new Object[][] {
            // Empty string
            { new DataItem("", 0, null) },

            // A single space
            { new DataItem(" ", 1, null) },

            // An anonymous symbol
            { new DataItem("+++", 3, new IdentifierExpression("+++", DummySymbolLookup.DEFAULT)) },

            // An anonymous symbol
            { new DataItem("-----", 5, new IdentifierExpression("-----", DummySymbolLookup.DEFAULT)) },

            // An anonymous symbol followed by a closing parenthesis
            { new DataItem("+)", 1, new IdentifierExpression("+", DummySymbolLookup.DEFAULT)) },

            // A short decimal integer literal
            { new DataItem("0", 1, VALUE_UINT_0) },

            // A short decimal integer literal followed by a closing parenthesis
            { new DataItem("0)", 1, VALUE_UINT_0) },

            // A long decimal integer literal followed by a closing parenthesis
            { new DataItem("123456789)", 9, VALUE_UINT_123456789) },

            // A long decimal integer literal
            { new DataItem("123456789", 9, VALUE_UINT_123456789) },

            // A longer decimal integer literal
            { new DataItem("1234567890123456789", 19, VALUE_UINT_1234567890123456789) },

            // An almost-overflowing decimal integer literal
            { new DataItem("18446744073709551615", 20, VALUE_UINT_18446744073709551615) },

            // An overflowing decimal integer literal
            { new DataItem("12345678901234567890123456789", 29, VALUE_UINT_5097733592125636885,
                    new OverflowInLiteralWarningMessage("12345678901234567890123456789")) },

            // An overflowing decimal integer literal (parse with assemblyMessageConsumer == null)
            { new DataItem("12345678901234567890123456789", 29, VALUE_UINT_5097733592125636885, DONT_CHECK_ASSEMBLY_MESSAGES) },

            // An overflowing decimal integer literal that fails a too simplistic overflow check
            { new DataItem("26000000000000000000", 20, new ValueExpression(new UnsignedIntValue(7553255926290448384L)),
                    new OverflowInLiteralWarningMessage("26000000000000000000")) },

            // An overflowing decimal integer literal that overflows when summing r and s
            { new DataItem("18446744073709551620", 20, new ValueExpression(new UnsignedIntValue(4L)),
                    new OverflowInLiteralWarningMessage("18446744073709551620")) },

            // An overflowing decimal integer literal that overflows when adding the last digit to the result
            { new DataItem("18446744073709551619", 20, VALUE_UINT_3, new OverflowInLiteralWarningMessage("18446744073709551619")) },

            // A float literal with no digits after the decimal point
            { new DataItem("3.", 2, new ValueExpression(new FloatValue(3.))) },

            // A float literal with no digits after the decimal point followed by "?"
            { new DataItem("3.?", 2, new ValueExpression(new FloatValue(3.))) },

            // A short float literal
            { new DataItem("3.5", 3, VALUE_FLOAT_3_POINT_5) },

            // An invalid float literal
            { new DataItem("3.5e", -1, null) },

            // An invalid float literal
            { new DataItem("3.5e?", -1, null) },

            // A short float literal with scientific E notation (upper-case E)
            { new DataItem("3.5E3", 5, new ValueExpression(new FloatValue(3.5e3))) },

            // A short float literal with scientific E notation (lower-case E)
            { new DataItem("3.5e3", 5, new ValueExpression(new FloatValue(3.5e3))) },

            // A short float literal with scientific E notation with a long exponent
            { new DataItem("3.5e120", 7, new ValueExpression(new FloatValue(3.5e120))) },

            // A short float literal with scientific E notation with a too big exponent
            { new DataItem("3.5e400", 7, new ValueExpression(new FloatValue(Double.POSITIVE_INFINITY))) },

            // An invalid float literal
            { new DataItem("3.5e+", -1, null) },

            // An invalid float literal
            { new DataItem("3.5e+?", -1, null) },

            // A short float literal with scientific E notation with prefix + and a short exponent
            { new DataItem("3.5e+3", 6, new ValueExpression(new FloatValue(3.5e+3))) },

            // A short float literal with scientific E notation with prefix + and a long exponent
            { new DataItem("3.5e+120", 8, new ValueExpression(new FloatValue(3.5e+120))) },

            // An invalid float literal
            { new DataItem("3.5e-", -1, null) },

            // An invalid float literal
            { new DataItem("3.5e-?", -1, null) },

            // A short float literal with scientific E notation with prefix - and a short exponent
            { new DataItem("3.5e-3", 6, new ValueExpression(new FloatValue(3.5e-3))) },

            // A short float literal with scientific E notation with prefix - and a long exponent
            { new DataItem("3.5e-120", 8, new ValueExpression(new FloatValue(3.5e-120))) },

            // A float literal with a long integral part
            { new DataItem(
                    "10000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.0", 103,
                    new ValueExpression(new FloatValue(1e100))) },

            // A float literal with a long fractional part
            { new DataItem("3.99609375", 10, new ValueExpression(new FloatValue(3.99609375))) },

            // A short float literal with scientific E notation but no fractional part
            { new DataItem("3e3", 3, new ValueExpression(new FloatValue(3e3))) },

            // A long float literal with scientific E notation but no fractional part
            { new DataItem("987654321e123", 13, new ValueExpression(new FloatValue(987654321e123))) },

            // An invalid expression
            { new DataItem("%", -1, null) },

            // A short binary integer literal
            { new DataItem("%0", 2, VALUE_UINT_0) },

            // A short binary integer literal followed by a closing parenthesis
            { new DataItem("%0)", 2, VALUE_UINT_0) },

            // A long binary integer literal
            { new DataItem("%111010110111100110100010101", 28, VALUE_UINT_123456789) },

            // A long binary integer literal followed by a closing parenthesis
            { new DataItem("%111010110111100110100010101)", 28, VALUE_UINT_123456789) },

            // A longer binary integer literal
            { new DataItem("%1000100100010000100001111010001111101111010011000000100010101", 62, VALUE_UINT_1234567890123456789) },

            // An almost-overflowing binary integer literal
            { new DataItem("%1111111111111111111111111111111111111111111111111111111111111111", 65, VALUE_UINT_18446744073709551615) },

            // An overflowing binary integer literal
            { new DataItem("%1001111110010000011011001100100100011010111110110010011011000101101110001110011000000100010101", 95,
                    VALUE_UINT_5097733592125636885, new OverflowInLiteralWarningMessage(
                            "1001111110010000011011001100100100011010111110110010011011000101101110001110011000000100010101")) },

            // An overflowing binary integer literal (parse with assemblyMessageConsumer == null)
            { new DataItem("%1001111110010000011011001100100100011010111110110010011011000101101110001110011000000100010101", 95,
                    VALUE_UINT_5097733592125636885, DONT_CHECK_ASSEMBLY_MESSAGES) },

            // An invalid expression
            { new DataItem("$", -1, null) },

            // A short hexadecimal integer literal
            { new DataItem("$0", 2, VALUE_UINT_0) },

            // A short hexadecimal integer literal followed by a closing parenthesis
            { new DataItem("$0)", 2, VALUE_UINT_0) },

            // A long hexadecimal integer literal (upper-case letters)
            { new DataItem("$75BCD15", 8, VALUE_UINT_123456789) },

            // A long hexadecimal integer literal (lower-case letters)
            { new DataItem("$75bcd15", 8, VALUE_UINT_123456789) },

            // A long hexadecimal integer literal followed by a closing parenthesis
            { new DataItem("$75BCD15)", 8, VALUE_UINT_123456789) },

            // A longer hexadecimal integer literal
            { new DataItem("$112210F47DE98115", 17, VALUE_UINT_1234567890123456789) },

            // An almost-overflowing hexadecimal integer literal
            { new DataItem("$FFFFFFFFFFFFFFFF", 17, VALUE_UINT_18446744073709551615) },

            // An overflowing hexadecimal integer literal
            { new DataItem("$27E41B3246BEC9B16E398115", 25, VALUE_UINT_5097733592125636885, new OverflowInLiteralWarningMessage(
                    "27E41B3246BEC9B16E398115")) },

            // An overflowing hexadecimal integer literal (parse with assemblyMessageConsumer == null)
            { new DataItem("$27E41B3246BEC9B16E398115", 25, VALUE_UINT_5097733592125636885, DONT_CHECK_ASSEMBLY_MESSAGES) },

            // An invalid hexadecimal integer literal
            { new DataItem("$0g", -1, null) },

            // The program counter
            { new DataItem("*", 1, ProgramCounterExpression.INSTANCE) },

            // An empty string delimited by apostrophes (single quotes)
            { new DataItem("''", 2, VALUE_STRING_EMPTY) },

            // An empty string delimited by double quotes
            { new DataItem("\"\"", 2, VALUE_STRING_EMPTY) },

            // A string delimited by apostrophes (single quotes)
            { new DataItem("'abcdef'", 8, VALUE_STRING_ABCDEF) },

            // A string delimited by double quotes
            { new DataItem("\"abcdef\"", 8, VALUE_STRING_ABCDEF) },

            // A string delimited by apostrophes (single quotes) with all valid escape sequences plus one invalid escape sequence
            { new DataItem("'\\0\\\"\\\'\\\\\\a\\b\\f\\n\\r\\t\\z'", 24, new ValueExpression(new StringValue(
                    "\0\"\'\\\u0007\b\f\n\r\tz")), UNRECOGNIZED_ESCAPE_SEQUENCE_WARNING_MESSAGE) },

            // A string delimited by double quotes with all valid escape sequences plus one invalid escape sequence
            { new DataItem("\"\\0\\\"\\\'\\\\\\a\\b\\f\\n\\r\\t\\z\"", 24, new ValueExpression(new StringValue(
                    "\0\"\'\\\u0007\b\f\n\r\tz")), UNRECOGNIZED_ESCAPE_SEQUENCE_WARNING_MESSAGE) },

            // A string delimited by apostrophes (single quotes) with all valid escape sequences plus one invalid escape sequence (parse with assemblyMessageConsumer == null)
            { new DataItem("'\\0\\\"\\\'\\\\\\a\\b\\f\\n\\r\\t\\z'", 24, new ValueExpression(new StringValue(
                    "\0\"\'\\\u0007\b\f\n\r\tz")), DONT_CHECK_ASSEMBLY_MESSAGES) },

            // A string delimited by double quotes with all valid escape sequences plus one invalid escape sequence (parse with assemblyMessageConsumer == null)
            { new DataItem("\"\\0\\\"\\\'\\\\\\a\\b\\f\\n\\r\\t\\z\"", 24, new ValueExpression(new StringValue(
                    "\0\"\'\\\u0007\b\f\n\r\tz")), DONT_CHECK_ASSEMBLY_MESSAGES) },

            // An unterminated string starting with an apostrophe (single quote)
            { new DataItem("'abcdef", -1, null) },

            // An unterminated string starting with a double quote
            { new DataItem("\"abcdef", -1, null) },

            // An unterminated string starting with an apostrophe (single quote) and containing an escaped apostrophe
            { new DataItem("'\\'", -1, null) },

            // An unterminated string starting with a double quote and containing an escaped double quote
            { new DataItem("\"\\\"", -1, null) },

            // A string delimited by apostrophes (single quotes) with an escaped backslash
            { new DataItem("'\\\\'", 4, VALUE_STRING_BACKSLASH) },

            // A string delimited by double quotes with an escaped backslash
            { new DataItem("\"\\\\\"", 4, VALUE_STRING_BACKSLASH) },

            // A short identifier
            { new DataItem("a", 1, IDENTIFIER_A) },

            // A long identifier
            { new DataItem("abcdef", 6, new IdentifierExpression("abcdef", DummySymbolLookup.DEFAULT)) },

            // A long identifier followed by a closing parenthesis
            { new DataItem("abcdef)", 6, new IdentifierExpression("abcdef", DummySymbolLookup.DEFAULT)) },

            // An identifier containing a period
            { new DataItem("abc.def", 7, new IdentifierExpression("abc.def", DummySymbolLookup.DEFAULT)) },

            // The unary plus operator
            { new DataItem("+a", 2, new UnaryOperatorExpression(UnaryOperator.UNARY_PLUS, IDENTIFIER_A)) },

            // The negation operator
            { new DataItem("-a", 2, new UnaryOperatorExpression(UnaryOperator.NEGATION, IDENTIFIER_A)) },

            // The bitwise NOT operator
            { new DataItem("~a", 2, new UnaryOperatorExpression(UnaryOperator.BITWISE_NOT, IDENTIFIER_A)) },

            // The logical NOT operator
            { new DataItem("!a", 2, new UnaryOperatorExpression(UnaryOperator.LOGICAL_NOT, IDENTIFIER_A)) },

            // An invalid expression
            { new DataItem("^a", 0, null) },

            // The unary plus operator surrounded with spaces
            { new DataItem(" + a", 4, new UnaryOperatorExpression(UnaryOperator.UNARY_PLUS, IDENTIFIER_A)) },

            // An invalid expression
            { new DataItem("~", 0, null) },

            // An invalid expression
            { new DataItem("~)", 0, null) },

            // An invalid expression
            { new DataItem("?", 0, null) },

            // An invalid expression
            { new DataItem("!=a", 0, null) },

            // An invalid expression
            { new DataItem("(", 0, null) },

            // An invalid expression
            { new DataItem("(0", 0, null) },

            // A decimal integer literal surrounded with parentheses
            { new DataItem("(0)", 3, new GroupingExpression(VALUE_UINT_0)) },

            // A decimal integer literal surrounded with parentheses, with spaces added
            { new DataItem(" ( 0 ) ", 7, new GroupingExpression(VALUE_UINT_0)) },

            // An identifier followed by a question mark
            { new DataItem("a?", 1, IDENTIFIER_A) },

            // An identifier followed by "?b"
            { new DataItem("a?b", 1, IDENTIFIER_A) },

            // An identifier followed by "?b:"
            { new DataItem("a?b:", 1, IDENTIFIER_A) },

            // The conditional operator
            { new DataItem("a?b:c", 5, new ConditionalExpression(IDENTIFIER_A, IDENTIFIER_B, IDENTIFIER_C)) },

            // An identifier followed by an opening parenthesis
            { new DataItem("foo(", 3, IDENTIFIER_FOO) },

            // An identifier followed by "(0"
            { new DataItem("foo(0", 3, IDENTIFIER_FOO) },

            // An identifier followed by "(0,"
            { new DataItem("foo(0,", 3, IDENTIFIER_FOO) },

            // An identifier followed by "(0, "
            { new DataItem("foo(0, ", 3, IDENTIFIER_FOO) },

            // An identifier followed by an opening parenthesis and an opening bracket
            { new DataItem("foo([", 3, IDENTIFIER_FOO) },

            // A function call with no arguments
            { new DataItem("foo()", 5, new FunctionCallExpression(IDENTIFIER_FOO)) },

            // A function call with one argument
            { new DataItem("foo(0)", 6, new FunctionCallExpression(IDENTIFIER_FOO, VALUE_UINT_0)) },

            // A function call with three arguments
            { new DataItem("foo(0,'a', i)", 13, new FunctionCallExpression(IDENTIFIER_FOO, VALUE_UINT_0, new ValueExpression(
                    new StringValue("a")), new IdentifierExpression("i", DummySymbolLookup.DEFAULT))) },

            // A function call with three arguments and a bunch of spaces and tabs all around
            { new DataItem("foo \t( \t0 \t, \t'a' \t, \ti \t) \t", 28, new FunctionCallExpression(IDENTIFIER_FOO, VALUE_UINT_0,
                    new ValueExpression(new StringValue("a")), new IdentifierExpression("i", DummySymbolLookup.DEFAULT))) },

            // A chained function call
            { new DataItem("foo(0)(0)", 9, new FunctionCallExpression(new FunctionCallExpression(IDENTIFIER_FOO, VALUE_UINT_0),
                    VALUE_UINT_0)) },

            // An identifier followed by an opening bracket
            { new DataItem("a[", 1, IDENTIFIER_A) },

            // An identifier followed by "[b"
            { new DataItem("a[b", 1, IDENTIFIER_A) },

            // The indexer operator
            { new DataItem("a[b]", 4, new IndexerExpression(IDENTIFIER_A, IDENTIFIER_B, DummySymbolLookup.DEFAULT)) },

            // The period operator
            { new DataItem("3.a", 3, new PeriodExpression(VALUE_UINT_3, IDENTIFIER_A, DummySymbolLookup.DEFAULT)) },

            // An identifier followed by a period
            { new DataItem("a .", 2, IDENTIFIER_A) },

            // The period operator used twice
            { new DataItem("a . b . c", 9, new PeriodExpression(new PeriodExpression(IDENTIFIER_A, IDENTIFIER_B,
                    DummySymbolLookup.DEFAULT), IDENTIFIER_C, DummySymbolLookup.DEFAULT)) },

            // The period operator followed by a function call
            { new DataItem("a . b ( c )", 11, new FunctionCallExpression(new PeriodExpression(IDENTIFIER_A, IDENTIFIER_B,
                    DummySymbolLookup.DEFAULT), IDENTIFIER_C)) },

            // An integer literal followed by an asterisk
            { new DataItem("3*", 1, VALUE_UINT_3) },

            // The multiplication operator
            { new DataItem("3*a", 3, new BinaryOperatorExpression(BinaryOperator.MULTIPLICATION, VALUE_UINT_3, IDENTIFIER_A)) },

            // The division operator
            { new DataItem("3/a", 3, new BinaryOperatorExpression(BinaryOperator.DIVISION, VALUE_UINT_3, IDENTIFIER_A)) },

            // The modulus operator
            { new DataItem("3%a", 3, new BinaryOperatorExpression(BinaryOperator.MODULUS, VALUE_UINT_3, IDENTIFIER_A)) },

            // The addition operator
            { new DataItem("3+a", 3, new BinaryOperatorExpression(BinaryOperator.ADDITION, VALUE_UINT_3, IDENTIFIER_A)) },

            // The subtraction operator
            { new DataItem("3-a", 3, new BinaryOperatorExpression(BinaryOperator.SUBTRACTION, VALUE_UINT_3, IDENTIFIER_A)) },

            // The bitwise XOR operator
            { new DataItem("3^a", 3, new BinaryOperatorExpression(BinaryOperator.BITWISE_XOR, VALUE_UINT_3, IDENTIFIER_A)) },

            // The bit shift left operator
            { new DataItem("3<<a", 4, new BinaryOperatorExpression(BinaryOperator.BIT_SHIFT_LEFT, VALUE_UINT_3, IDENTIFIER_A)) },

            // The less than or equal to operator
            { new DataItem("3<=a", 4,
                    new BinaryOperatorExpression(BinaryOperator.LESS_THAN_OR_EQUAL_TO, VALUE_UINT_3, IDENTIFIER_A)) },

            // The different from operator
            { new DataItem("3<>a", 4, new BinaryOperatorExpression(BinaryOperator.DIFFERENT_FROM, VALUE_UINT_3, IDENTIFIER_A)) },

            // The less than operator
            { new DataItem("3<a", 3, new BinaryOperatorExpression(BinaryOperator.LESS_THAN, VALUE_UINT_3, IDENTIFIER_A)) },

            // The greater than or equal to operator
            { new DataItem("3>=a", 4, new BinaryOperatorExpression(BinaryOperator.GREATER_THAN_OR_EQUAL_TO, VALUE_UINT_3,
                    IDENTIFIER_A)) },

            // The bit shift right operator
            { new DataItem("3>>a", 4, new BinaryOperatorExpression(BinaryOperator.BIT_SHIFT_RIGHT, VALUE_UINT_3, IDENTIFIER_A)) },

            // The greater than operator
            { new DataItem("3>a", 3, new BinaryOperatorExpression(BinaryOperator.GREATER_THAN, VALUE_UINT_3, IDENTIFIER_A)) },

            // The strictly equal to operator
            { new DataItem("3==a", 4, new BinaryOperatorExpression(BinaryOperator.STRICTLY_EQUAL_TO, VALUE_UINT_3, IDENTIFIER_A)) },

            // The equal to operator
            { new DataItem("3=a", 3, new BinaryOperatorExpression(BinaryOperator.EQUAL_TO, VALUE_UINT_3, IDENTIFIER_A)) },

            // The logical AND operator
            { new DataItem("3&&a", 4, new BinaryOperatorExpression(BinaryOperator.LOGICAL_AND, VALUE_UINT_3, IDENTIFIER_A)) },

            // The bitwise AND operator
            { new DataItem("3&a", 3, new BinaryOperatorExpression(BinaryOperator.BITWISE_AND, VALUE_UINT_3, IDENTIFIER_A)) },

            // The logical OR operator
            { new DataItem("3||a", 4, new BinaryOperatorExpression(BinaryOperator.LOGICAL_OR, VALUE_UINT_3, IDENTIFIER_A)) },

            // The bitwise OR operator
            { new DataItem("3|a", 3, new BinaryOperatorExpression(BinaryOperator.BITWISE_OR, VALUE_UINT_3, IDENTIFIER_A)) },

            // The strictly different from operator
            { new DataItem("3!=a", 4, new BinaryOperatorExpression(BinaryOperator.STRICTLY_DIFFERENT_FROM, VALUE_UINT_3,
                    IDENTIFIER_A)) },

            // The multiplication operator surrounded with spaces
            { new DataItem("3 * a", 5, new BinaryOperatorExpression(BinaryOperator.MULTIPLICATION, VALUE_UINT_3, IDENTIFIER_A)) },

            // The addition operator used twice
            { new DataItem("a+b+c", 5, new BinaryOperatorExpression(BinaryOperator.ADDITION, new BinaryOperatorExpression(
                    BinaryOperator.ADDITION, IDENTIFIER_A, IDENTIFIER_B), IDENTIFIER_C)) },

            // The addition operator followed by the subtraction operator
            { new DataItem("a+b-c", 5, new BinaryOperatorExpression(BinaryOperator.SUBTRACTION, new BinaryOperatorExpression(
                    BinaryOperator.ADDITION, IDENTIFIER_A, IDENTIFIER_B), IDENTIFIER_C)) },

            // The subtraction operator followed by the addition operator
            { new DataItem("a-b+c", 5, new BinaryOperatorExpression(BinaryOperator.ADDITION, new BinaryOperatorExpression(
                    BinaryOperator.SUBTRACTION, IDENTIFIER_A, IDENTIFIER_B), IDENTIFIER_C)) },

            // The addition operator followed by the multiplication operator
            { new DataItem("a+b*c", 5, new BinaryOperatorExpression(BinaryOperator.ADDITION, IDENTIFIER_A,
                    new BinaryOperatorExpression(BinaryOperator.MULTIPLICATION, IDENTIFIER_B, IDENTIFIER_C))) },

            // The multiplication operator followed by the addition operator
            { new DataItem("a*b+c", 5, new BinaryOperatorExpression(BinaryOperator.ADDITION, new BinaryOperatorExpression(
                    BinaryOperator.MULTIPLICATION, IDENTIFIER_A, IDENTIFIER_B), IDENTIFIER_C)) },

            // A decimal integer literal followed by an exclamation mark and a letter
            { new DataItem("3!a", 1, VALUE_UINT_3) },

            // A decimal integer literal followed by a tilde and a letter
            { new DataItem("3~a", 1, VALUE_UINT_3) },

            // The program counter multiplied by the program counter
            { new DataItem("***", 3, new BinaryOperatorExpression(BinaryOperator.MULTIPLICATION, ProgramCounterExpression.INSTANCE,
                    ProgramCounterExpression.INSTANCE)) },

            // The period operator with the program counter on both sides
            { new DataItem("*.*", 3, new PeriodExpression(ProgramCounterExpression.INSTANCE, ProgramCounterExpression.INSTANCE,
                    DummySymbolLookup.DEFAULT)) },

            // A binary integer literal modulo another binary integer literal
            { new DataItem("%11%%11", 7, new BinaryOperatorExpression(BinaryOperator.MODULUS, VALUE_UINT_3, VALUE_UINT_3)) },

            // The negation operator applied to the result of a function call
            { new DataItem("-a(b)", 5, new UnaryOperatorExpression(UnaryOperator.NEGATION, new FunctionCallExpression(IDENTIFIER_A,
                    IDENTIFIER_B))) },

            // An unary operator after a period operator
            { new DataItem("a . !b", 2, IDENTIFIER_A) },

            // Grouping parentheses after a period operator
            { new DataItem("a . (b)", 7, new PeriodExpression(IDENTIFIER_A, new GroupingExpression(IDENTIFIER_B),
                    DummySymbolLookup.DEFAULT)) },

            // A conditional expression mixed with binary operators
            { new DataItem("a + b = c ? d : e", 17, new ConditionalExpression(new BinaryOperatorExpression(BinaryOperator.EQUAL_TO,
                    new BinaryOperatorExpression(BinaryOperator.ADDITION, IDENTIFIER_A, IDENTIFIER_B), IDENTIFIER_C), IDENTIFIER_D,
                    IDENTIFIER_E)) },

            // Nested conditional expressions
            { new DataItem("a ? b ? c : d : c ? b : a", 25, new ConditionalExpression(IDENTIFIER_A, new ConditionalExpression(
                    IDENTIFIER_B, IDENTIFIER_C, IDENTIFIER_D), new ConditionalExpression(IDENTIFIER_C, IDENTIFIER_B, IDENTIFIER_A))) },

    };

    @Nonnull
    private static final List<Object[]> TEST_DATA_LIST = Arrays.asList(TEST_DATA_ARRAY);

    @Nonnull
    @Parameters
    public static List<Object[]> data() {
        return TEST_DATA_LIST;
    }

    @Nonnull
    private final DataItem data;

    /**
     * Initializes a new ExpressionParserTest.
     *
     * @param data
     *            the data to test with
     */
    public ExpressionParserTest(@Nonnull DataItem data) {
        this.data = data;
    }

    /**
     * Asserts that {@link ExpressionParser#parse(Tokenizer, Consumer)} parses the correct expression, leaves the tokenizer in the
     * expected state and emits the expected {@link AssemblyMessage AssemblyMessages}.
     */
    @Test
    public void parse() {
        try {
            final Tokenizer tokenizer = new Tokenizer();
            final ArrayList<AssemblyMessage> assemblyMessages = new ArrayList<>();
            final Consumer<AssemblyMessage> assemblyMessageConsumer = this.data.expectedAssemblyMessageMatchers == null ? null
                    : new Consumer<AssemblyMessage>() {
                        @Override
                        public void accept(AssemblyMessage assemblyMessage) {
                            assemblyMessages.add(assemblyMessage);
                        }
                    };
            Expression expression;

            tokenizer.setCharSequence(this.data.expressionText);
            try {
                expression = ExpressionParser.parse(tokenizer, DummySymbolLookup.DEFAULT, assemblyMessageConsumer);
            } catch (InvalidTokenException e) {
                assertThat(-1, is(this.data.finalPosition));
                return;
            }

            assertThat(expression, is(this.data.expectedExpression));
            if (tokenizer.getTokenType() == TokenType.END) {
                assertThat(this.data.expressionText.length(), is(this.data.finalPosition));
            } else {
                assertThat(tokenizer.getTokenStart(), is(this.data.finalPosition));
            }

            if (this.data.expectedAssemblyMessageMatchers != null) {
                if (this.data.expectedAssemblyMessageMatchers.isEmpty()) {
                    assertThat(assemblyMessages, is(empty()));
                } else {
                    assertThat(assemblyMessages, contains(this.data.expectedAssemblyMessageMatchers));
                }
            }
        } catch (AssertionError e) {
            throw new AssertionError(this.data.expressionText + e.getMessage(), e);
        }
    }

}
