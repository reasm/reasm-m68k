package org.reasm.m68k.testhelpers;

import static org.hamcrest.Matchers.nullValue;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.beans.PropertyUtil;
import org.reasm.m68k.source.LogicalLine;
import org.reasm.source.ParseError;

import ca.fragag.testhelpers.LinePrefixDescription;

/**
 * Matches a {@link LogicalLine} with the same properties as the {@link LogicalLine} specified in the constructor.
 *
 * @author Francis Gagné
 */
public final class EquivalentLogicalLine extends TypeSafeDiagnosingMatcher<LogicalLine> {

    private final LogicalLine expectedValue;
    private final Matcher<? super ParseError> parseErrorMatcher;

    /**
     * Initializes a new EquivalentLogicalLine.
     *
     * @param expectedValue
     *            the {@link LogicalLine} from which the expected property values are extracted
     */
    public EquivalentLogicalLine(@Nonnull LogicalLine expectedValue) {
        if (expectedValue == null) {
            throw new NullPointerException("expectedValue");
        }

        this.expectedValue = expectedValue;

        final ParseError parseError = this.expectedValue.getParseError();
        if (parseError == null) {
            this.parseErrorMatcher = nullValue();
        } else {
            this.parseErrorMatcher = new EquivalentParseError(parseError);
        }
    }

    @Override
    public final void describeTo(Description description) {
        description.appendText("a logical line with these properties:");

        final LinePrefixDescription linePrefixDescription = new LinePrefixDescription(description, "    ", false);
        linePrefixDescription.appendText("\nlength = ").appendValue(this.expectedValue.getLength());
        linePrefixDescription.appendText("\nparse error = ").appendDescriptionOf(this.parseErrorMatcher);
        this.describeLabels(linePrefixDescription);
        linePrefixDescription.appendText("\nmnemonic bounds = ").appendValue(this.expectedValue.getMnemonicBounds());
        this.describeOperands(linePrefixDescription);
        linePrefixDescription.appendText("\ncomment bounds = ").appendValue(this.expectedValue.getCommentBounds());
        this.describeContinuationCharacters(linePrefixDescription);
    }

    @Override
    public final boolean matchesSafely(LogicalLine item, Description mismatchDescription) {
        return this.matchesProperty("length", item, mismatchDescription) && this.matchesParseError(item, mismatchDescription)
                && this.matchesProperty("numberOfLabels", item, mismatchDescription)
                && this.matchesLabels(item, mismatchDescription)
                && this.matchesProperty("mnemonicBounds", item, mismatchDescription)
                && this.matchesProperty("numberOfOperands", item, mismatchDescription)
                && this.matchesOperands(item, mismatchDescription)
                && this.matchesProperty("commentBounds", item, mismatchDescription)
                && this.matchesProperty("numberOfContinuationCharacters", item, mismatchDescription)
                && this.matchesContinuationCharacters(item, mismatchDescription);
    }

    private final void describeContinuationCharacters(Description description) {
        final int numberOfContinuationCharacters = this.expectedValue.getNumberOfContinuationCharacters();
        description.appendText("\n").appendText(String.valueOf(numberOfContinuationCharacters))
                .appendText(" continuation characters");
        if (numberOfContinuationCharacters > 0) {
            description.appendText(": ");
        }

        for (int i = 0; i < numberOfContinuationCharacters; i++) {
            if (i > 0) {
                description.appendText(", ");
            }

            description.appendValue(this.expectedValue.getContinuationCharacter(i));
        }
    }

    private final void describeLabels(Description description) {
        final int numberOfLabels = this.expectedValue.getNumberOfLabels();
        description.appendText("\n").appendText(String.valueOf(numberOfLabels)).appendText(" labels");
        if (numberOfLabels > 0) {
            description.appendText(": ");
        }

        for (int i = 0; i < numberOfLabels; i++) {
            if (i > 0) {
                description.appendText(", ");
            }

            description.appendValue(this.expectedValue.getLabelBounds(i));
        }
    }

    private final void describeOperands(Description description) {
        final int numberOfOperands = this.expectedValue.getNumberOfOperands();
        description.appendText("\n").appendText(String.valueOf(numberOfOperands)).appendText(" operands");
        if (numberOfOperands > 0) {
            description.appendText(": ");
        }

        for (int i = 0; i < numberOfOperands; i++) {
            if (i > 0) {
                description.appendText(", ");
            }

            description.appendValue(this.expectedValue.getOperandBounds(i));
        }
    }

    private final boolean matchesContinuationCharacters(LogicalLine item, Description mismatchDescription) {
        for (int i = 0; i < this.expectedValue.getNumberOfContinuationCharacters(); i++) {
            if (item.getContinuationCharacter(i) != this.expectedValue.getContinuationCharacter(i)) {
                mismatchDescription.appendText("got continuationCharacter[").appendText(String.valueOf(i)).appendText("] = ")
                        .appendValue(item.getContinuationCharacter(i));
                return false;
            }
        }

        return true;
    }

    private final boolean matchesLabels(LogicalLine item, Description mismatchDescription) {
        for (int i = 0; i < this.expectedValue.getNumberOfLabels(); i++) {
            if (!Objects.equals(item.getLabelBounds(i), this.expectedValue.getLabelBounds(i))) {
                mismatchDescription.appendText("got labelBounds[").appendText(String.valueOf(i)).appendText("] = ")
                        .appendValue(item.getLabelBounds(i));
                return false;
            }
        }

        return true;
    }

    private final boolean matchesOperands(LogicalLine item, Description mismatchDescription) {
        for (int i = 0; i < this.expectedValue.getNumberOfOperands(); i++) {
            if (!Objects.equals(item.getOperandBounds(i), this.expectedValue.getOperandBounds(i))) {
                mismatchDescription.appendText("got operandBounds[").appendText(String.valueOf(i)).appendText("] = ")
                        .appendValue(item.getOperandBounds(i));
                return false;
            }
        }

        return true;
    }

    private final boolean matchesParseError(LogicalLine item, Description mismatchDescription) {
        if (!this.parseErrorMatcher.matches(item.getParseError())) {
            mismatchDescription.appendText("got parseError = ").appendValue(item.getParseError());
            return false;
        }

        return true;
    }

    private final boolean matchesProperty(@Nonnull String propertyName, @Nonnull LogicalLine item,
            @Nonnull Description mismatchDescription) {
        final Object expectedPropertyValue;
        try {
            expectedPropertyValue = PropertyUtil.getPropertyDescriptor(propertyName, this.expectedValue).getReadMethod()
                    .invoke(this.expectedValue);
        } catch (Exception e) {
            mismatchDescription.appendText("property ").appendValue(propertyName).appendText(" is not readable on expected value");
            return false;
        }

        final Object actualPropertyValue;
        try {
            actualPropertyValue = PropertyUtil.getPropertyDescriptor(propertyName, item).getReadMethod().invoke(item);
        } catch (Exception e) {
            mismatchDescription.appendText("property ").appendValue(propertyName).appendText(" is not readable on actual value");
            return false;
        }

        if (!Objects.equals(actualPropertyValue, expectedPropertyValue)) {
            mismatchDescription.appendText("got ").appendText(propertyName).appendText(" = ").appendValue(actualPropertyValue);
            return false;
        }

        return true;
    }

}
