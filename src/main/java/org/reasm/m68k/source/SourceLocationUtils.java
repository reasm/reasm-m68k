package org.reasm.m68k.source;

import org.reasm.SubstringBounds;
import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNode;
import org.reasm.source.SourceNodeRangeReader;

/**
 * Provides utility methods to work with {@link SourceLocation} objects that refer to a {@link LogicalLine} (or a
 * {@link BlockDirectiveLine}, which wraps a {@link LogicalLine}).
 *
 * @author Francis Gagné
 */
public final class SourceLocationUtils {

    /**
     * Gets a {@link SourceNodeRangeReader} for the comment on the logical line at the specified source location.
     *
     * @param location
     *            the source location
     * @return a {@link SourceNodeRangeReader} to read the comment
     */
    public static SourceNodeRangeReader getCommentReader(SourceLocation location) {
        final SubstringBounds commentBounds = getLogicalLineRequired(location).getCommentBounds();
        if (commentBounds == null) {
            throw new IllegalArgumentException("LogicalLine has no comment");
        }

        return new SourceNodeRangeReader(location, commentBounds, new LogicalLineRangeReaderSkipHandler());
    }

    /**
     * Gets the text of the comment on the logical line at the specified source location.
     *
     * @param location
     *            the source location
     * @return the text of the comment
     */
    public static String getCommentText(SourceLocation location) {
        return getCommentReader(location).readToString();
    }

    /**
     * Gets a {@link SourceNodeRangeReader} for a label on the logical line at the specified source location.
     *
     * @param location
     *            the source location
     * @param index
     *            the index of the label
     * @return a {@link SourceNodeRangeReader} to read the label
     */
    public static SourceNodeRangeReader getLabelReader(SourceLocation location, int index) {
        return new SourceNodeRangeReader(location, getLogicalLineRequired(location).getLabelBounds(index),
                new LogicalLineRangeReaderSkipHandler());
    }

    /**
     * Gets the text of a label on the logical line at the specified source location.
     *
     * @param location
     *            the source location
     * @param index
     *            the index of the label
     * @return the text of the label
     */
    public static String getLabelText(SourceLocation location, int index) {
        return getLabelReader(location, index).readToString();
    }

    /**
     * Gets the {@link LogicalLine} referenced by a {@link SourceLocation}.
     *
     * @param location
     *            the {@link SourceLocation} to get the {@link LogicalLine} from
     * @return the {@link LogicalLine} referenced by the {@link SourceLocation}, or <code>null</code> if the {@link SourceLocation}
     *         doesn't reference a {@link LogicalLine}
     */
    public static LogicalLine getLogicalLine(SourceLocation location) {
        final SourceNode sourceNode = location.getSourceNode();

        if (sourceNode instanceof LogicalLine) {
            return (LogicalLine) sourceNode;
        }

        if (sourceNode instanceof BlockDirectiveLine) {
            return ((BlockDirectiveLine) sourceNode).getLogicalLine();
        }

        return null;
    }

    /**
     * Gets the {@link LogicalLine} referenced by a {@link SourceLocation}.
     *
     * @param location
     *            the {@link SourceLocation} to get the {@link LogicalLine} from
     * @return the {@link LogicalLine} referenced by the {@link SourceLocation}
     * @throws IllegalArgumentException
     *             the {@link SourceLocation} doesn't reference a {@link LogicalLine} or a {@link BlockDirectiveLine}
     */
    public static LogicalLine getLogicalLineRequired(SourceLocation location) {
        final LogicalLine logicalLine = getLogicalLine(location);
        if (logicalLine != null) {
            return logicalLine;
        }

        throw new IllegalArgumentException("The SourceLocation doesn't reference a LogicalLine");
    }

    /**
     * Gets a {@link SourceNodeRangeReader} for the mnemonic on the logical line at the specified source location.
     *
     * @param location
     *            the source location
     * @return a {@link SourceNodeRangeReader} to read the mnemonic
     */
    public static SourceNodeRangeReader getMnemonicReader(SourceLocation location) {
        final SubstringBounds mnemonicBounds = getLogicalLineRequired(location).getMnemonicBounds();
        if (mnemonicBounds == null) {
            throw new IllegalArgumentException("LogicalLine has no mnemonic");
        }

        return new SourceNodeRangeReader(location, mnemonicBounds, new LogicalLineRangeReaderSkipHandler());
    }

    /**
     * Gets the text of the mnemonic on the logical line at the specified source location.
     *
     * @param location
     *            the source location
     * @return the text of the mnemonic
     */
    public static String getMnemonicText(SourceLocation location) {
        return getMnemonicReader(location).readToString();
    }

    /**
     * Gets a {@link SourceNodeRangeReader} for an operand on the logical line at the specified source location.
     *
     * @param location
     *            the source location
     * @param index
     *            the index of the operand
     * @return a {@link SourceNodeRangeReader} to read the operand
     */
    public static SourceNodeRangeReader getOperandReader(SourceLocation location, int index) {
        return new SourceNodeRangeReader(location, getLogicalLineRequired(location).getOperandBounds(index),
                new LogicalLineRangeReaderSkipHandler());
    }

    /**
     * Gets the text of an operand on the logical line at the specified source location.
     *
     * @param location
     *            the source location
     * @param index
     *            the index of the operand
     * @return the text of the operand
     */
    public static String getOperandText(SourceLocation location, int index) {
        return getOperandReader(location, index).readToString();
    }

    /**
     * Determines whether the logical line contains a comment.
     *
     * @param location
     *            the source location
     * @return <code>true</code> if the logical line contains a comment; otherwise, <code>false</code>
     */
    public static boolean hasComment(SourceLocation location) {
        return getLogicalLineRequired(location).getCommentBounds() != null;
    }

    /**
     * Determines whether the logical line contains a mnemonic.
     *
     * @param location
     *            the source location
     * @return <code>true</code> if the logical line contains a mnemonic; otherwise, <code>false</code>
     */
    public static boolean hasMnemonic(SourceLocation location) {
        return getLogicalLineRequired(location).getMnemonicBounds() != null;
    }

    // This class isn't meant to be instantiated.
    private SourceLocationUtils() {
    }

}
