package org.reasm.m68k.source;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.SubstringBounds;
import org.reasm.m68k.parseerrors.LabelExpectedParseError;
import org.reasm.m68k.testhelpers.EquivalentParseError;
import org.reasm.source.ParseError;
import org.reasm.source.parseerrors.MismatchedParenthesisParseError;
import org.reasm.source.parseerrors.UnterminatedStringParseError;

import ca.fragag.text.CharSequenceReader;
import ca.fragag.text.GenericCharSequenceReader;

/**
 * Test class for {@link LogicalLineParser}.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
@SuppressWarnings("javadoc")
public class LogicalLineParserTest {

    @Nonnull
    private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

    @Nonnull
    private static final SubstringBounds[] NO_LABELS = new SubstringBounds[0];
    @Nonnull
    private static final SubstringBounds[] NO_OPERANDS = NO_LABELS;
    @Nonnull
    private static final int[] NO_CONTINUATION_CHARACTERS = new int[0];

    static {
        addDataItem("", 0, null, NO_LABELS, null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" ", 1, null, NO_LABELS, null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("\t", 1, null, NO_LABELS, null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a", 1, null, array(bounds(0, 1)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a ", 2, null, array(bounds(0, 1)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a:", 2, null, array(bounds(0, 1)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a: ", 3, null, array(bounds(0, 1)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a :", 3, null, array(bounds(0, 1)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("abc", 3, null, array(bounds(0, 3)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("abc ", 4, null, array(bounds(0, 3)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" a:", 3, null, array(bounds(1, 2)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" a: ", 4, null, array(bounds(1, 2)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("abc def:", 8, null, array(bounds(0, 3), bounds(4, 7)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("abc def: ", 9, null, array(bounds(0, 3), bounds(4, 7)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("abc def :", 9, null, array(bounds(0, 3), bounds(4, 7)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("abc: def:", 9, null, array(bounds(0, 3), bounds(5, 8)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(":", 1, new LabelExpectedParseError(0), array(bounds(0, 0)), null, NO_OPERANDS, null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem("abc::", 5, new LabelExpectedParseError(4), array(bounds(0, 3), bounds(4, 4)), null, NO_OPERANDS, null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem("abc:::", 6, new LabelExpectedParseError(4), array(bounds(0, 3), bounds(4, 4), bounds(5, 5)), null,
                NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" a", 2, null, NO_LABELS, bounds(1, 2), NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" a ", 3, null, NO_LABELS, bounds(1, 2), NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc", 4, null, NO_LABELS, bounds(1, 4), NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc ", 5, null, NO_LABELS, bounds(1, 4), NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("abc def", 7, null, array(bounds(0, 3)), bounds(4, 7), NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc d", 6, null, NO_LABELS, bounds(1, 4), array(bounds(5, 6)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc d ", 7, null, NO_LABELS, bounds(1, 4), array(bounds(5, 6)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc d  ", 8, null, NO_LABELS, bounds(1, 4), array(bounds(5, 6)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc d'", 7, null, NO_LABELS, bounds(1, 4), array(bounds(5, 7)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc 0", 6, null, NO_LABELS, bounds(1, 4), array(bounds(5, 6)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc 0'", 7, null, NO_LABELS, bounds(1, 4), array(bounds(5, 7)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc ,", 6, null, NO_LABELS, bounds(1, 4), array(bounds(5, 5), bounds(6, 6)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc def,ghi", 12, null, NO_LABELS, bounds(1, 4), array(bounds(5, 8), bounds(9, 12)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc def, ghi", 13, null, NO_LABELS, bounds(1, 4), array(bounds(5, 8), bounds(10, 13)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc def,ghi,jkl", 16, null, NO_LABELS, bounds(1, 4), array(bounds(5, 8), bounds(9, 12), bounds(13, 16)),
                null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc (", 6, new MismatchedParenthesisParseError(5), NO_LABELS, bounds(1, 4), array(bounds(5, 6)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc )", 6, new MismatchedParenthesisParseError(5), NO_LABELS, bounds(1, 4), array(bounds(5, 6)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc )(", 7, new MismatchedParenthesisParseError(5), NO_LABELS, bounds(1, 4), array(bounds(5, 7)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc ()", 7, null, NO_LABELS, bounds(1, 4), array(bounds(5, 7)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc (d)", 8, null, NO_LABELS, bounds(1, 4), array(bounds(5, 8)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc (,)", 8, null, NO_LABELS, bounds(1, 4), array(bounds(5, 8)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc (()", 8, new MismatchedParenthesisParseError(6), NO_LABELS, bounds(1, 4), array(bounds(5, 8)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc ())", 8, new MismatchedParenthesisParseError(7), NO_LABELS, bounds(1, 4), array(bounds(5, 8)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc ())d", 9, new MismatchedParenthesisParseError(7), NO_LABELS, bounds(1, 4), array(bounds(5, 9)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc ()))", 9, new MismatchedParenthesisParseError(7), NO_LABELS, bounds(1, 4), array(bounds(5, 9)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc (())", 9, null, NO_LABELS, bounds(1, 4), array(bounds(5, 9)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc ((d))", 10, null, NO_LABELS, bounds(1, 4), array(bounds(5, 10)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc (d,e)", 10, null, NO_LABELS, bounds(1, 4), array(bounds(5, 10)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc \"", 6, new UnterminatedStringParseError(5), NO_LABELS, bounds(1, 4), array(bounds(5, 6)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc \"d", 7, new UnterminatedStringParseError(5), NO_LABELS, bounds(1, 4), array(bounds(5, 7)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc \"\"", 7, null, NO_LABELS, bounds(1, 4), array(bounds(5, 7)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc \"d\"", 8, null, NO_LABELS, bounds(1, 4), array(bounds(5, 8)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc \",\"", 8, null, NO_LABELS, bounds(1, 4), array(bounds(5, 8)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc \"\\\"", 8, new UnterminatedStringParseError(5), NO_LABELS, bounds(1, 4), array(bounds(5, 8)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc )\"", 7, new MismatchedParenthesisParseError(5), NO_LABELS, bounds(1, 4), array(bounds(5, 7)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc '", 6, new UnterminatedStringParseError(5), NO_LABELS, bounds(1, 4), array(bounds(5, 6)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc 'd", 7, new UnterminatedStringParseError(5), NO_LABELS, bounds(1, 4), array(bounds(5, 7)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" abc ''", 7, null, NO_LABELS, bounds(1, 4), array(bounds(5, 7)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a=", 2, null, array(bounds(0, 1)), bounds(1, 2), NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a=0", 3, null, array(bounds(0, 1)), bounds(1, 2), array(bounds(2, 3)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem("*", 1, null, NO_LABELS, null, NO_OPERANDS, bounds(0, 1), NO_CONTINUATION_CHARACTERS);
        addDataItem("*xyz", 4, null, NO_LABELS, null, NO_OPERANDS, bounds(0, 4), NO_CONTINUATION_CHARACTERS);
        addDataItem(";", 1, null, NO_LABELS, null, NO_OPERANDS, bounds(0, 1), NO_CONTINUATION_CHARACTERS);
        addDataItem(";xyz", 4, null, NO_LABELS, null, NO_OPERANDS, bounds(0, 4), NO_CONTINUATION_CHARACTERS);
        addDataItem(" *xyz", 5, null, NO_LABELS, bounds(1, 5), NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" ;xyz", 5, null, NO_LABELS, null, NO_OPERANDS, bounds(1, 5), NO_CONTINUATION_CHARACTERS);
        addDataItem("a;xyz", 5, null, array(bounds(0, 1)), null, NO_OPERANDS, bounds(1, 5), NO_CONTINUATION_CHARACTERS);
        addDataItem("a:;xyz", 6, null, array(bounds(0, 1)), null, NO_OPERANDS, bounds(2, 6), NO_CONTINUATION_CHARACTERS);
        addDataItem("a: ;xyz", 7, null, array(bounds(0, 1)), null, NO_OPERANDS, bounds(3, 7), NO_CONTINUATION_CHARACTERS);
        addDataItem("a :;xyz", 7, null, array(bounds(0, 1)), null, NO_OPERANDS, bounds(3, 7), NO_CONTINUATION_CHARACTERS);
        addDataItem(" a;xyz", 6, null, NO_LABELS, bounds(1, 2), NO_OPERANDS, bounds(2, 6), NO_CONTINUATION_CHARACTERS);
        addDataItem(" a ;xyz", 7, null, NO_LABELS, bounds(1, 2), NO_OPERANDS, bounds(3, 7), NO_CONTINUATION_CHARACTERS);
        addDataItem(" a b;xyz", 8, null, NO_LABELS, bounds(1, 2), array(bounds(3, 4)), bounds(4, 8), NO_CONTINUATION_CHARACTERS);
        addDataItem(" a b ;xyz", 9, null, NO_LABELS, bounds(1, 2), array(bounds(3, 4)), bounds(5, 9), NO_CONTINUATION_CHARACTERS);
        addDataItem(" a b,;xyz", 9, null, NO_LABELS, bounds(1, 2), array(bounds(3, 4), bounds(5, 5)), bounds(5, 9),
                NO_CONTINUATION_CHARACTERS);
        addDataItem(" a (;xyz", 8, new MismatchedParenthesisParseError(3), NO_LABELS, bounds(1, 2), array(bounds(3, 4)),
                bounds(4, 8), NO_CONTINUATION_CHARACTERS);
        addDataItem(" a \";xyz", 8, new UnterminatedStringParseError(3), NO_LABELS, bounds(1, 2), array(bounds(3, 8)), null,
                NO_CONTINUATION_CHARACTERS);
        addDataItem("\n", 1, null, NO_LABELS, null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("\n ", 1, null, NO_LABELS, null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("\r", 1, null, NO_LABELS, null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("\r ", 1, null, NO_LABELS, null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("\r\n", 2, null, NO_LABELS, null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("\r\n ", 2, null, NO_LABELS, null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a\n", 2, null, array(bounds(0, 1)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a&", 2, null, array(bounds(0, 2)), null, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem("a&\n", 3, null, array(bounds(0, 3)), null, NO_OPERANDS, null, new int[] { 1 });
        addDataItem("a&\n\n", 4, null, array(bounds(0, 3)), null, NO_OPERANDS, null, new int[] { 1 });
        addDataItem("a&\nb", 4, null, array(bounds(0, 4)), null, NO_OPERANDS, null, new int[] { 1 });
        addDataItem("a&\rb", 4, null, array(bounds(0, 4)), null, NO_OPERANDS, null, new int[] { 1 });
        addDataItem("a&\r\nb", 5, null, array(bounds(0, 5)), null, NO_OPERANDS, null, new int[] { 1 });
        addDataItem("a&\n ", 4, null, array(bounds(0, 4)), null, NO_OPERANDS, null, new int[] { 1 });
        addDataItem("a&\n \n", 5, null, array(bounds(0, 4)), null, NO_OPERANDS, null, new int[] { 1 });
        addDataItem("a&\n b", 5, null, array(bounds(0, 5)), null, NO_OPERANDS, null, new int[] { 1 });
        addDataItem("a&\n  b", 6, null, array(bounds(0, 6)), null, NO_OPERANDS, null, new int[] { 1 });
        addDataItem(" a&", 3, null, NO_LABELS, bounds(1, 3), NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" a&\n", 4, null, NO_LABELS, bounds(1, 4), NO_OPERANDS, null, new int[] { 2 });
        addDataItem(" a&\nb", 5, null, NO_LABELS, bounds(1, 5), NO_OPERANDS, null, new int[] { 2 });
        addDataItem(" a b&", 5, null, NO_LABELS, bounds(1, 2), array(bounds(3, 5)), null, NO_CONTINUATION_CHARACTERS);
        addDataItem(" a b&\n", 6, null, NO_LABELS, bounds(1, 2), array(bounds(3, 6)), null, new int[] { 4 });
        addDataItem(" a b&\nc", 7, null, NO_LABELS, bounds(1, 2), array(bounds(3, 7)), null, new int[] { 4 });
        addDataItem(" a b,&\nc", 8, null, NO_LABELS, bounds(1, 2), array(bounds(3, 4), bounds(7, 8)), null, new int[] { 5 });
    }

    /**
     * Gets the test data for this parameterized test.
     *
     * @return the test data
     */
    @Nonnull
    @Parameters
    public static List<Object[]> data() {
        return TEST_DATA;
    }

    private static void addDataItem(@Nonnull String text, int length, @CheckForNull ParseError parseError,
            @Nonnull SubstringBounds[] labels, @CheckForNull SubstringBounds mnemonic, @Nonnull SubstringBounds[] operands,
            @CheckForNull SubstringBounds comment, @Nonnull int[] continuationCharacters) {
        TEST_DATA.add(new Object[] { text, 0, length, parseError, labels, mnemonic, operands, comment, continuationCharacters });
        TEST_DATA.add(new Object[] { "\n" + text, 1, length, parseError, labels, mnemonic, operands, comment,
                continuationCharacters });
    }

    @Nonnull
    @SafeVarargs
    private static <T> T[] array(@Nonnull T... bounds) {
        return bounds;
    }

    @Nonnull
    private static SubstringBounds bounds(int start, int end) {
        return new SubstringBounds(start, end);
    }

    @Nonnull
    private final String text;
    private final int start;
    private final int length;
    @CheckForNull
    private final ParseError parseError;
    @Nonnull
    private final SubstringBounds[] labels;
    @CheckForNull
    private final SubstringBounds mnemonic;
    @Nonnull
    private final SubstringBounds[] operands;
    @CheckForNull
    private final SubstringBounds comment;
    @Nonnull
    private final int[] continuationCharacters;

    public LogicalLineParserTest(@Nonnull String text, int start, int length, @CheckForNull ParseError parseError,
            @Nonnull SubstringBounds[] labels, @CheckForNull SubstringBounds mnemonic, @Nonnull SubstringBounds[] operands,
            @CheckForNull SubstringBounds comment, @Nonnull int[] continuationCharacters) {
        this.text = text;
        this.start = start;
        this.length = length;
        this.parseError = parseError;
        this.labels = labels;
        this.mnemonic = mnemonic;
        this.operands = operands;
        this.comment = comment;
        this.continuationCharacters = continuationCharacters;
    }

    /**
     * Asserts that {@link LogicalLineParser#parse(CharSequenceReader)} parses a {@link LogicalLine} with the correct attributes.
     */
    @Test
    public void parse() {
        final LogicalLine logicalLine = LogicalLineParser.parse(new GenericCharSequenceReader(this.text, this.start));

        // Check SourceNode properties
        assertThat(logicalLine.getLength(), is(this.length));
        if (this.parseError == null) {
            assertThat(logicalLine.getParseError(), is(nullValue()));
        } else {
            assertThat(logicalLine.getParseError(), is(new EquivalentParseError(this.parseError)));
        }

        // Check labels
        assertThat(logicalLine.getNumberOfLabels(), is(this.labels.length));
        for (int i = 0; i < this.labels.length; i++) {
            assertThat(logicalLine.getLabelBounds(i), is(this.labels[i]));
        }

        // Check mnemonic
        assertThat(logicalLine.getMnemonicBounds(), is(this.mnemonic));

        // Check operands
        assertThat(logicalLine.getNumberOfOperands(), is(this.operands.length));
        for (int i = 0; i < this.operands.length; i++) {
            assertThat(logicalLine.getOperandBounds(i), is(this.operands[i]));
        }

        // Check comment
        assertThat(logicalLine.getCommentBounds(), is(this.comment));

        // Check continuation characters
        assertThat(logicalLine.getNumberOfContinuationCharacters(), is(this.continuationCharacters.length));
        for (int i = 0; i < this.continuationCharacters.length; i++) {
            assertThat(logicalLine.getContinuationCharacter(i), is(this.continuationCharacters[i]));
        }
    }

}
