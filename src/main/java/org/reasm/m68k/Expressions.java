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
    public static Expression parse(@Nonnull CharSequence expression, @CheckForNull SymbolLookup symbolLookup,
            @CheckForNull Consumer<AssemblyMessage> assemblyMessageConsumer) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }

        final Tokenizer tokenizer = new Tokenizer();
        tokenizer.setCharSequence(expression);

        final Expression result;
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

    // This class is not meant to be instantiated.
    private Expressions() {
    }

}
