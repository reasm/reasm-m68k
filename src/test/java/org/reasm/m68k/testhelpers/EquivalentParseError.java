package org.reasm.m68k.testhelpers;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.reasm.source.ParseError;

/**
 * A matcher that checks whether a {@link ParseError} is of the same class and has the same {@linkplain ParseError#getText() text}
 * as an expected value.
 *
 * @author Francis Gagné
 */
@Immutable
public class EquivalentParseError extends TypeSafeDiagnosingMatcher<ParseError> {

    @Nonnull
    private final ParseError expectedValue;

    /**
     * Initializes a new EquivalentParseError.
     *
     * @param expectedValue
     *            the expected value
     */
    public EquivalentParseError(@Nonnull ParseError expectedValue) {
        if (expectedValue == null) {
            throw new NullPointerException("expectedValue");
        }

        this.expectedValue = expectedValue;
    }

    @Override
    public void describeTo(@Nonnull Description description) {
        description.appendText("a parse error of type ").appendText(this.expectedValue.getClass().getName())
                .appendText(" with text ").appendText(this.expectedValue.getText());
    }

    @Override
    protected boolean matchesSafely(@Nonnull ParseError item, @Nonnull Description mismatchDescription) {
        if (!Objects.equals(item.getClass(), this.expectedValue.getClass())) {
            mismatchDescription.appendText("has type ").appendValue(item.getClass().getName());
            return false;
        }

        if (!Objects.equals(item.getText(), this.expectedValue.getText())) {
            mismatchDescription.appendText("has text ").appendValue(item.getText());
            return false;
        }

        return true;
    }

}
