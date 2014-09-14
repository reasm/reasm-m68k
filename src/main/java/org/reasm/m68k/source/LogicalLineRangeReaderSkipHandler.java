package org.reasm.m68k.source;

import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNodeRangeReader;

/**
 * An implementation of {@link SourceNodeRangeReader} for {@link SourceLocation}s referencing a {@link LogicalLine}.
 *
 * @author Francis Gagné
 */
public final class LogicalLineRangeReaderSkipHandler extends SourceNodeRangeReader.SkipHandler {

    /**
     * The state of the process of skipping a continuation character and the following line break and initial whitespace.
     */
    private enum ContinuationCharacterSkipState {
        LOOK_FOR_CONTINUATION_CHARACTER, EXPECT_CR_OR_LF, EXPECT_LF_OR_WHITESPACE, EXPECT_WHITESPACE
    }

    private ContinuationCharacterSkipState continuationCharacterSkipState = ContinuationCharacterSkipState.LOOK_FOR_CONTINUATION_CHARACTER;

    /**
     * Initializes a new LogicalLineRangeReaderSkipHandler.
     */
    public LogicalLineRangeReaderSkipHandler() {
    }

    @Override
    protected boolean skipCurrentCodePoint() {
        final SourceNodeRangeReader reader = this.getReader();
        assert reader != null;
        final LogicalLine logicalLine = SourceLocationUtils.getLogicalLineRequired(reader.getSourceLocation());

        switch (this.continuationCharacterSkipState) {
        case LOOK_FOR_CONTINUATION_CHARACTER:
            // This state is processed after the switch, because some states may reset the state, in which case we must look for a
            // continuation character.
            break;

        case EXPECT_CR_OR_LF:
            if (reader.getCurrentChar() == '\r') {
                this.continuationCharacterSkipState = ContinuationCharacterSkipState.EXPECT_LF_OR_WHITESPACE;
                return true;
            }

            if (reader.getCurrentChar() == '\n') {
                this.continuationCharacterSkipState = ContinuationCharacterSkipState.EXPECT_WHITESPACE;
                return true;
            }

            throw new AssertionError("Unexpected character: '" + reader.getCurrentChar() + "'"); // unreachable

        case EXPECT_LF_OR_WHITESPACE:
            this.continuationCharacterSkipState = ContinuationCharacterSkipState.EXPECT_WHITESPACE;

            if (reader.getCurrentChar() == '\n') {
                return true;
            }

            // fall through

        case EXPECT_WHITESPACE:
            if (Parser.isWhitespace(reader.getCurrentCodePoint())) {
                // Remain in this state as long as we encounter whitespace.
                return true;
            }

            break;

        default:
            throw new AssertionError(); // unreachable
        }

        // Reset the state to LOOK_FOR_CONTINUATION_CHARACTER.
        this.continuationCharacterSkipState = ContinuationCharacterSkipState.LOOK_FOR_CONTINUATION_CHARACTER;

        if (logicalLine.isContinuationCharacter(reader.getCurrentPositionInSourceNode())) {
            this.continuationCharacterSkipState = ContinuationCharacterSkipState.EXPECT_CR_OR_LF;
            return true;
        }

        return false;
    }

}
