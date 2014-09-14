package org.reasm.m68k.source;

import static ca.fragag.testhelpers.HasType.hasType;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import org.junit.Test;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.source.SourceFile;
import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNodeRangeReader;

/**
 * Test class for {@link SourceLocationUtils}.
 *
 * @author Francis Gagné
 */
public class SourceLocationUtilsTest {

    private static final SourceFile SOURCE_FILE_A = new SourceFile(" ", "");
    private static final SourceFile SOURCE_FILE_B = new SourceFile("label0: label1: MOVE D0,D1 ; Move D0 to D1", "");
    private static final SourceFile SOURCE_FILE_C = new SourceFile("lab&\nel0: label1: MO&\nVE D&\n0,D1 ; Move D0 to D1", "");
    private static final SourceFile SOURCE_FILE_D = new SourceFile(" IF 1\n ENDIF", "");
    private static final SourceLocation SOURCE_LOCATION_BLOCK = SOURCE_FILE_A.getSourceLocations(M68KArchitecture.MC68000).get(0);
    private static final SourceLocation SOURCE_LOCATION_A = SOURCE_FILE_A.getSourceLocations(M68KArchitecture.MC68000).get(0)
            .getChildSourceLocations().get(0);
    private static final SourceLocation SOURCE_LOCATION_B = SOURCE_FILE_B.getSourceLocations(M68KArchitecture.MC68000).get(0)
            .getChildSourceLocations().get(0);
    private static final SourceLocation SOURCE_LOCATION_C = SOURCE_FILE_C.getSourceLocations(M68KArchitecture.MC68000).get(0)
            .getChildSourceLocations().get(0);
    private static final SourceLocation SOURCE_LOCATION_D = SOURCE_FILE_D.getSourceLocations(M68KArchitecture.MC68000).get(0)
            .getChildSourceLocations().get(0).getChildSourceLocations().get(0);

    private static void advance(SourceNodeRangeReader reader, int count) {
        while (count > 0) {
            reader.advance();
            --count;
            assertThat(reader.atEnd(), is(count == 0));
        }
    }

    /**
     * Asserts that {@link SourceLocationUtils#getCommentReader(SourceLocation)} returns a {@link SourceNodeRangeReader} over the
     * comment of the specified {@link SourceLocation}.
     */
    @Test
    public void getCommentReader() {
        final SourceNodeRangeReader commentReader = SourceLocationUtils.getCommentReader(SOURCE_LOCATION_B);
        assertThat(commentReader.getSourceLocation(), is(SOURCE_LOCATION_B));
        assertThat(commentReader.getCurrentPosition(), is(0));
        assertThat(commentReader.getCurrentPositionInSourceNode(), is(27));
        advance(commentReader, 15);
    }

    /**
     * Asserts that {@link SourceLocationUtils#getCommentReader(SourceLocation)} throws an {@link IllegalArgumentException} when the
     * specified {@link SourceLocation} contains no comment.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getCommentReaderNoComment() {
        SourceLocationUtils.getCommentReader(SOURCE_LOCATION_A);
    }

    /**
     * Asserts that {@link SourceLocationUtils#getCommentText(SourceLocation)} returns the comment of the specified
     * {@link SourceLocation}.
     */
    @Test
    public void getCommentText() {
        final String commentText = SourceLocationUtils.getCommentText(SOURCE_LOCATION_B);
        assertThat(commentText, is("; Move D0 to D1"));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getLabelReader(SourceLocation, int)} returns a {@link SourceNodeRangeReader} over the
     * specified label of the specified {@link SourceLocation}.
     */
    @Test
    public void getLabelReader() {
        final SourceNodeRangeReader labelReader = SourceLocationUtils.getLabelReader(SOURCE_LOCATION_B, 0);
        assertThat(labelReader.getSourceLocation(), is(SOURCE_LOCATION_B));
        assertThat(labelReader.getCurrentPosition(), is(0));
        assertThat(labelReader.getCurrentPositionInSourceNode(), is(0));
        advance(labelReader, 6);
    }

    /**
     * Asserts that {@link SourceLocationUtils#getLabelText(SourceLocation, int)} returns the specified label of the specified
     * {@link SourceLocation}.
     */
    @Test
    public void getLabelText0() {
        final String labelText = SourceLocationUtils.getLabelText(SOURCE_LOCATION_B, 0);
        assertThat(labelText, is("label0"));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getLabelText(SourceLocation, int)} returns the specified label of the specified
     * {@link SourceLocation}, ignoring continuation characters in the source.
     */
    @Test
    public void getLabelText0WithContinuationCharacters() {
        final String labelText = SourceLocationUtils.getLabelText(SOURCE_LOCATION_C, 0);
        assertThat(labelText, is("label0"));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getLabelText(SourceLocation, int)} returns the specified label of the specified
     * {@link SourceLocation}.
     */
    @Test
    public void getLabelText1() {
        final String labelText = SourceLocationUtils.getLabelText(SOURCE_LOCATION_B, 1);
        assertThat(labelText, is("label1"));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getLogicalLine(SourceLocation)} uses the {@link LogicalLine} referenced by a
     * {@link BlockDirectiveLine}.
     */
    @Test
    public void getLogicalLineBlockDirectiveLine() {
        assumeThat(SOURCE_LOCATION_D.getSourceNode(), hasType(BlockDirectiveLine.class));
        assertThat(SourceLocationUtils.getLogicalLine(SOURCE_LOCATION_D), is(notNullValue()));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getLogicalLine(SourceLocation)} returns <code>null</code> when the
     * {@link SourceLocation} doesn't reference a {@link LogicalLine}.
     */
    @Test
    public void getLogicalLineNotLogicalLine() {
        assertThat(SourceLocationUtils.getLogicalLine(SOURCE_LOCATION_BLOCK), is(nullValue()));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getLogicalLineRequired(SourceLocation)} throws an {@link IllegalArgumentException}
     * when the {@link SourceLocation} doesn't reference a {@link LogicalLine}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getLogicalLineRequiredNotLogicalLine() {
        SourceLocationUtils.getLogicalLineRequired(SOURCE_LOCATION_BLOCK);
    }

    /**
     * Asserts that {@link SourceLocationUtils#getMnemonicReader(SourceLocation)} returns a {@link SourceNodeRangeReader} for the
     * mnemonic of the specified {@link SourceLocation}.
     */
    @Test
    public void getMnemonicReader() {
        final SourceNodeRangeReader mnemonicReader = SourceLocationUtils.getMnemonicReader(SOURCE_LOCATION_B);
        assertThat(mnemonicReader.getSourceLocation(), is(SOURCE_LOCATION_B));
        assertThat(mnemonicReader.getCurrentPosition(), is(0));
        assertThat(mnemonicReader.getCurrentPositionInSourceNode(), is(16));
        advance(mnemonicReader, 4);
    }

    /**
     * Asserts that {@link SourceLocationUtils#getMnemonicReader(SourceLocation)} throws an {@link IllegalArgumentException} when
     * the {@link SourceLocation} contains no mnemonic.
     */
    @Test(expected = IllegalArgumentException.class)
    public void getMnemonicReaderNoMnemonic() {
        final SourceNodeRangeReader mnemonicReader = SourceLocationUtils.getMnemonicReader(SOURCE_LOCATION_A);
        assertThat(mnemonicReader.getSourceLocation(), is(SOURCE_LOCATION_B));
        assertThat(mnemonicReader.getCurrentPosition(), is(0));
        assertThat(mnemonicReader.getCurrentPositionInSourceNode(), is(16));
        advance(mnemonicReader, 4);
    }

    /**
     * Asserts that {@link SourceLocationUtils#getMnemonicText(SourceLocation)} returns the mnemonic of the specified
     * {@link SourceLocation}.
     */
    @Test
    public void getMnemonicText() {
        final String mnemonicText = SourceLocationUtils.getMnemonicText(SOURCE_LOCATION_B);
        assertThat(mnemonicText, is("MOVE"));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getMnemonicText(SourceLocation)} returns the mnemonic of the specified
     * {@link SourceLocation}, ignoring continuation characters in the source.
     */
    @Test
    public void getMnemonicTextWithContinuationCharacters() {
        final String mnemonicText = SourceLocationUtils.getMnemonicText(SOURCE_LOCATION_C);
        assertThat(mnemonicText, is("MOVE"));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getOperandReader(SourceLocation, int)} returns an {@link SourceNodeRangeReader} for
     * the specified operand of the specified {@link SourceLocation}.
     */
    @Test
    public void getOperandReader() {
        final SourceNodeRangeReader operandReader = SourceLocationUtils.getOperandReader(SOURCE_LOCATION_B, 0);
        assertThat(operandReader.getSourceLocation(), is(SOURCE_LOCATION_B));
        assertThat(operandReader.getCurrentPosition(), is(0));
        assertThat(operandReader.getCurrentPositionInSourceNode(), is(21));
        advance(operandReader, 2);
        assertThat(operandReader.atEnd(), is(true));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getOperandText(SourceLocation, int)} returns the specified operand of the specified
     * {@link SourceLocation}.
     */
    @Test
    public void getOperandText0() {
        final String operandText = SourceLocationUtils.getOperandText(SOURCE_LOCATION_B, 0);
        assertThat(operandText, is("D0"));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getOperandText(SourceLocation, int)} returns the specified operand of the specified
     * {@link SourceLocation}, ignoring continuation characters in the source.
     */
    @Test
    public void getOperandText0WithContinuationCharacters() {
        final String labelText = SourceLocationUtils.getOperandText(SOURCE_LOCATION_C, 0);
        assertThat(labelText, is("D0"));
    }

    /**
     * Asserts that {@link SourceLocationUtils#getOperandText(SourceLocation, int)} returns the specified operand of the specified
     * {@link SourceLocation}.
     */
    @Test
    public void getOperandText1() {
        final String operandText = SourceLocationUtils.getOperandText(SOURCE_LOCATION_B, 1);
        assertThat(operandText, is("D1"));
    }

    /**
     * Asserts that {@link SourceLocationUtils#hasComment(SourceLocation)} returns <code>false</code> for a {@link SourceLocation}
     * that contains no comment.
     */
    @Test
    public void hasCommentFalse() {
        assertThat(SourceLocationUtils.hasComment(SOURCE_LOCATION_A), is(false));
    }

    /**
     * Asserts that {@link SourceLocationUtils#hasComment(SourceLocation)} returns <code>true</code> for a {@link SourceLocation}
     * that contains a comment.
     */
    @Test
    public void hasCommentTrue() {
        assertThat(SourceLocationUtils.hasComment(SOURCE_LOCATION_B), is(true));
    }

    /**
     * Asserts that {@link SourceLocationUtils#hasMnemonic(SourceLocation)} returns <code>false</code> for a {@link SourceLocation}
     * that contains no mnemonic.
     */
    @Test
    public void hasMnemonicFalse() {
        assertThat(SourceLocationUtils.hasMnemonic(SOURCE_LOCATION_A), is(false));
    }

    /**
     * Asserts that {@link SourceLocationUtils#hasMnemonic(SourceLocation)} returns <code>true</code> for a {@link SourceLocation}
     * that contains a mnemonic.
     */
    @Test
    public void hasMnemonicTrue() {
        assertThat(SourceLocationUtils.hasMnemonic(SOURCE_LOCATION_B), is(true));
    }

}
