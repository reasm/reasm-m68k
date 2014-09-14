package org.reasm.m68k;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.UnsignedIntValue;
import org.reasm.expressions.BinaryOperator;
import org.reasm.expressions.BinaryOperatorExpression;
import org.reasm.expressions.Expression;
import org.reasm.expressions.ValueExpression;

import ca.fragag.Consumer;

/**
 * Test class for {@link Expressions}.
 *
 * @author Francis Gagné
 */
public class ExpressionsTest {

    /**
     * Asserts that {@link Expressions#parse(CharSequence, Consumer)} throws an {@link IllegalArgumentException} when the specified
     * expression is invalid.
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseInvalid() {
        Expressions.parse("!", null);
    }

    /**
     * Asserts that {@link Expressions#parse(CharSequence, Consumer)} throws an {@link IllegalArgumentException} when the specified
     * expression has an invalid token.
     */
    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidToken() {
        Expressions.parse("\\", null);
    }

    /**
     * Asserts that {@link Expressions#parse(CharSequence, Consumer)} throws a {@link NullPointerException} when the
     * <code>expression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void parseNull() {
        Expressions.parse(null, null);
    }

    /**
     * Asserts that {@link Expressions#parse(CharSequence, Consumer)} throws an {@link IllegalArgumentException} when an expression
     * could be parsed, but other characters remained in the specified string.
     */
    @Test(expected = IllegalArgumentException.class)
    public void parsePartiallyValid() {
        Expressions.parse("2+", null);
    }

    /**
     * Asserts that {@link Expressions#parse(CharSequence, Consumer)} returns the parsed expression.
     */
    @Test
    public void parseValid() {
        final ValueExpression two = new ValueExpression(new UnsignedIntValue(2));
        assertThat(Expressions.parse("2+2", null), is((Expression) new BinaryOperatorExpression(BinaryOperator.ADDITION, two, two)));
    }

    /**
     * Asserts that {@link Expressions#serializeString(String)} correctly serializes a string that contains no special characters.
     */
    @Test
    public void serializeStringBasic() {
        assertThat(Expressions.serializeString("abc"), is("\"abc\""));
    }

    /**
     * Asserts that {@link Expressions#serializeString(String)} correctly serializes a string that contains special characters.
     */
    @Test
    public void serializeStringEscapes() {
        assertThat(Expressions.serializeString("\0\u0007\b\t\n\f\r\""), is("\"\\0\\a\\b\\t\\n\\f\\r\\\"\""));
    }

    /**
     * Asserts that {@link Expressions#serializeString(String)} throws a {@link NullPointerException} when the <code>string</code>
     * argument is <code>null</code>
     */
    @Test(expected = NullPointerException.class)
    public void serializeStringNull() {
        Expressions.serializeString(null);
    }
}
