package org.reasm.m68k.testhelpers;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.reasm.m68k.expressions.internal.TokenType;
import org.reasm.m68k.expressions.internal.Tokenizer;

/**
 * Matches the current token of a {@link Tokenizer} with specific properties.
 *
 * @author Francis Gagné
 */
@Immutable
public class TokenMatcher extends TypeSafeDiagnosingMatcher<Tokenizer> {

    @Nonnull
    private final TokenType tokenType;
    private final int start;
    private final int end;

    /**
     * Initializes a new TokenMatcher.
     *
     * @param tokenType
     *            the token's expected type
     * @param start
     *            the token's expected start position
     * @param end
     *            the token's expected end position
     */
    public TokenMatcher(@Nonnull TokenType tokenType, int start, int end) {
        this.tokenType = tokenType;
        this.start = start;
        this.end = end;
    }

    @Override
    public void describeTo(@Nonnull Description description) {
        description.appendText("is on a token with type ").appendValue(this.tokenType).appendText(" that starts at position ")
                .appendValue(this.start).appendText(" and ends at position ").appendValue(this.end);
    }

    @Override
    protected boolean matchesSafely(@Nonnull Tokenizer item, @Nonnull Description mismatchDescription) {
        if (item.getTokenType() != this.tokenType) {
            mismatchDescription.appendText("has token type ").appendValue(item.getTokenType());
            return false;
        }

        if (item.getTokenStart() != this.start) {
            mismatchDescription.appendText("has start position ").appendValue(item.getTokenStart());
            return false;
        }

        if (item.getTokenEnd() != this.end) {
            mismatchDescription.appendText("has end position ").appendValue(item.getTokenEnd());
            return false;
        }

        return true;
    }

}
