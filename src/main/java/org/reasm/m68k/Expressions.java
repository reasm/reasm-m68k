package org.reasm.m68k;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.AssemblyMessage;
import org.reasm.expressions.EvaluationContext;
import org.reasm.expressions.Expression;
import org.reasm.expressions.IdentifierExpression;
import org.reasm.expressions.SymbolLookup;
import org.reasm.m68k.expressions.internal.ExpressionParser;
import org.reasm.m68k.expressions.internal.InvalidTokenException;
import org.reasm.m68k.expressions.internal.TokenType;
import org.reasm.m68k.expressions.internal.Tokenizer;

import ca.fragag.Consumer;

/**
 * Contains methods related to expressions.
 *
 * @author Francis Gagné
 */
public final class Expressions {

    /**
     * Parses an expression from a {@link CharSequence}.
     *
     * @param expression
     *            the expression text
     * @param symbolLookup
     *            an object that looks up symbols by name, which will be used to look up the symbol for identifiers when the
     *            identifier is {@linkplain IdentifierExpression#evaluate(EvaluationContext) evaluated}, or <code>null</code> to
     *            consider all identifiers undefined
     * @param assemblyMessageConsumer
     *            a {@link Consumer} that will receive the assembly messages that were raised while evaluating the expression
     * @return an {@link Expression} corresponding to the expression text
     */
    @Nonnull
    public static Expression parse(@Nonnull CharSequence expression,
            @CheckForNull SymbolLookup symbolLookup, @CheckForNull Consumer<AssemblyMessage> assemblyMessageConsumer) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }

        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence(expression);

        Expression result;
        try {
            result = ExpressionParser.parse(tokenizer, symbolLookup, assemblyMessageConsumer);
        } catch (InvalidTokenException e) {
            throw new IllegalArgumentException("Not a valid expression: " + expression.toString());
        }

        if (result == null || tokenizer.getTokenType() != TokenType.END) {
            throw new IllegalArgumentException("Not a valid expression: " + expression.toString());
        }

        return result;
    }

    /**
     * Serializes a string such that it can be parsed by {@link Expressions#parse(CharSequence, SymbolLookup, Consumer)}. The string
     * is surrounded with quotes and characters are escaped where necessary.
     *
     * @param string
     *            the string to serialize
     * @return the serialized string
     */
    @Nonnull
    public static String serializeString(@Nonnull String string) {
        if (string == null) {
            throw new NullPointerException("string");
        }

        StringBuilder sb = new StringBuilder();
        sb.append('"');

        int codePoint;
        for (int i = 0; i < string.length(); i += Character.charCount(codePoint)) {
            codePoint = string.codePointAt(i);

            switch (codePoint) {
            case 0:
                sb.append("\\0");
                break;

            case 7: // bell
                sb.append("\\a");
                break;

            case '\b':
                sb.append("\\b");
                break;

            case '\t':
                sb.append("\\t");
                break;

            case '\n':
                sb.append("\\n");
                break;

            case '\f':
                sb.append("\\f");
                break;

            case '\r':
                sb.append("\\r");
                break;

            case '"':
                sb.append("\\\"");
                break;

            default:
                sb.appendCodePoint(codePoint);
                break;
            }
        }

        return sb.append('"').toString();
    }

    // This class is not meant to be instantiated.
    private Expressions() {
    }

}
