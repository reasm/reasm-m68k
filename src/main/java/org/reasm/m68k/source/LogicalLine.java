package org.reasm.m68k.source;

import java.io.IOException;
import java.util.Arrays;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.SubstringBounds;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

/**
 * A logical line. A logical line is comprised of one or more source lines. A logical line contains more than one source line when a
 * line ends with a continuation character.
 *
 * @author Francis Gagné
 */
@Immutable
public final class LogicalLine extends SourceNode {

    /**
     * Gets the {@link LogicalLine} for the specified {@link SourceNode}.
     *
     * @param sourceNode
     *            a {@link SourceNode}
     * @return If <code>sourceNode</code> is a {@link LogicalLine}, returns <code>sourceNode</code> cast to {@link LogicalLine}.
     *         Otherwise, if <code>sourceNode</code> is a {@link BlockDirectiveLine}, returns the result of
     *         {@link BlockDirectiveLine#getLogicalLine()} called on <code>sourceNode</code>. Otherwise, returns <code>null</code>.
     */
    @CheckForNull
    public static LogicalLine get(@CheckForNull SourceNode sourceNode) {
        if (sourceNode instanceof LogicalLine) {
            return (LogicalLine) sourceNode;
        }

        if (sourceNode instanceof BlockDirectiveLine) {
            return ((BlockDirectiveLine) sourceNode).getLogicalLine();
        }

        return null;
    }

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

    /**
     * Initializes a new logical line.
     *
     * @param length
     *            the number of characters on the logical line
     * @param parseError
     *            the parse error on the logical line
     * @param labels
     *            the bounds of the labels on the logical line
     * @param mnemonic
     *            the bounds of the mnemonic on the logical line
     * @param operands
     *            the bounds of the operands on the logical line
     * @param comment
     *            the bounds of the comment on the logical line
     * @param continuationCharacters
     *            the position of the continuation characters on the logical line
     */
    LogicalLine(int length, @CheckForNull ParseError parseError, @Nonnull SubstringBounds[] labels,
            @CheckForNull SubstringBounds mnemonic, @Nonnull SubstringBounds[] operands, @CheckForNull SubstringBounds comment,
            @Nonnull int[] continuationCharacters) {
        super(length, parseError);
        this.labels = labels;
        this.mnemonic = mnemonic;
        this.operands = operands;
        this.comment = comment;
        this.continuationCharacters = continuationCharacters;
    }

    /**
     * Gets the bounds of the comment on this logical line.
     *
     * @return the bounds of the comment
     */
    @CheckForNull
    public final SubstringBounds getCommentBounds() {
        return this.comment;
    }

    /**
     * Gets the position of a continuation character on this logical line.
     *
     * @param index
     *            the index of the continuation character
     * @return the position of the continuation character
     */
    public final int getContinuationCharacter(int index) {
        return this.continuationCharacters[index];
    }

    /**
     * Gets the bounds of a label on this logical line.
     *
     * @param index
     *            the index of the label
     * @return the bounds of the label
     */
    @Nonnull
    public final SubstringBounds getLabelBounds(int index) {
        return this.labels[index];
    }

    /**
     * Gets the bounds of the mnemonic on this logical line.
     *
     * @return the bounds of the mnemonic
     */
    @CheckForNull
    public final SubstringBounds getMnemonicBounds() {
        return this.mnemonic;
    }

    /**
     * Gets the number of continuation characters on this logical line.
     *
     * @return the number of continuation characters
     */
    public final int getNumberOfContinuationCharacters() {
        return this.continuationCharacters.length;
    }

    /**
     * Gets the number of labels on this logical line.
     *
     * @return the number of labels
     */
    public final int getNumberOfLabels() {
        return this.labels.length;
    }

    /**
     * Gets the number of operands on this logical line.
     *
     * @return the number of operands
     */
    public final int getNumberOfOperands() {
        return this.operands.length;
    }

    /**
     * Gets the bounds of an operand on this logical line.
     *
     * @param index
     *            the index of the operand
     * @return the bounds of the operand
     */
    @Nonnull
    public final SubstringBounds getOperandBounds(int index) {
        return this.operands[index];
    }

    /**
     * Determines whether the character at the specified position is a continuation character.
     *
     * @param position
     *            the position to test
     * @return <code>true</code> if the character is a continuation character; otherwise, <code>false</code>
     */
    public final boolean isContinuationCharacter(int position) {
        // Assumes that this.continuationCharacters is sorted. This is true for the LogicalLine objects created by
        // LogicalLineParser.parse().
        return Arrays.binarySearch(this.continuationCharacters, position) >= 0;
    }

    @Override
    protected void assembleCore(@Nonnull AssemblyBuilder builder) throws IOException {
        SourceNodesImpl.assembleLogicalLine(builder);
    }

}
