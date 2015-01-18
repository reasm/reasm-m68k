package org.reasm.m68k.source;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.SubstringBounds;
import org.reasm.m68k.Syntax;
import org.reasm.m68k.parseerrors.LabelExpectedParseError;
import org.reasm.source.ParseError;
import org.reasm.source.parseerrors.MismatchedParenthesisParseError;
import org.reasm.source.parseerrors.UnterminatedStringParseError;

import ca.fragag.text.CharSequenceReader;

/**
 * The logical line parser.
 *
 * @author Francis Gagné
 */
final class LogicalLineParser {

    @Nonnull
    private static final SubstringBounds[] EMPTY_SUBSTRING_BOUNDS_ARRAY = new SubstringBounds[0];
    @Nonnull
    private static final int[] EMPTY_INT_ARRAY = new int[0];

    /**
     * Parses a logical line starting at the current position of the specified reader.
     *
     * @param reader
     *            the {@link CharSequenceReader} to read from
     * @return the parsed logical line
     */
    @Nonnull
    static LogicalLine parse(@Nonnull CharSequenceReader<?> reader) {
        ParseError parseError = null;
        ArrayList<SubstringBounds> labels = new ArrayList<>();
        SubstringBounds mnemonic = null;
        ArrayList<SubstringBounds> operands = new ArrayList<>();
        SubstringBounds comment = null;
        ArrayList<Integer> continuationCharacters = new ArrayList<>();

        int startOfLogicalLine = reader.getCurrentPosition();

        if (readLogicalChar(reader, startOfLogicalLine, continuationCharacters)) {
            int currentCodePoint = reader.getCurrentCodePoint();

            // When this flag becomes false, we have reached the end of the logical line.
            boolean onTheLine = true;

            // Check if the line is a full-line comment.
            if (currentCodePoint == '*' || currentCodePoint == ';') {
                comment = readComment(reader, startOfLogicalLine);
                onTheLine = false;
            } else {
                // Parse the labels.
                if (!Syntax.isWhitespace(currentCodePoint)) {
                    // The line starts with a label. This label doesn't need to end with a colon, but if one is found, it
                    // will just be skipped.
                    int start = reader.getCurrentPosition() - startOfLogicalLine;

                    onTheLine = readLabelOrMnemonic(reader, startOfLogicalLine, continuationCharacters);

                    // The colon, semicolon, whitespace or end of the line ends the label.
                    int end = reader.getCurrentPosition() - startOfLogicalLine;
                    parseError = addLabel(labels, start, end, parseError);

                    // If the character wasn't a colon, look for one.
                    currentCodePoint = reader.getCurrentCodePoint();
                    if (currentCodePoint != ':') {
                        if (onTheLine = skipWhitespaceFromCurrent(reader, onTheLine, startOfLogicalLine, continuationCharacters)) {
                            currentCodePoint = reader.getCurrentCodePoint();
                        }
                    }

                    // If the following character is a colon, skip it.
                    if (currentCodePoint == ':') {
                        onTheLine = advanceReadLogicalChar(reader, startOfLogicalLine, continuationCharacters);
                    }
                }

                // Parse additional labels and the mnemonic.
                while (onTheLine = skipWhitespaceFromCurrent(reader, onTheLine, startOfLogicalLine, continuationCharacters)) {
                    currentCodePoint = reader.getCurrentCodePoint();
                    if (currentCodePoint == ';') {
                        // No mnemonic, just a comment.
                        comment = readComment(reader, startOfLogicalLine);
                        onTheLine = false;
                        break;
                    }

                    // The line may still have labels, but they must be followed by a colon. If what we parse isn't
                    // followed by a colon, then it's the mnemonic.
                    int start = reader.getCurrentPosition() - startOfLogicalLine;

                    onTheLine = readLabelOrMnemonic(reader, startOfLogicalLine, continuationCharacters);

                    int end = reader.getCurrentPosition() - startOfLogicalLine;
                    if ((onTheLine = skipWhitespaceFromCurrent(reader, onTheLine, startOfLogicalLine, continuationCharacters))
                            && reader.getCurrentCodePoint() == ':') {
                        parseError = addLabel(labels, start, end, parseError);
                        onTheLine = advanceReadLogicalChar(reader, startOfLogicalLine, continuationCharacters);
                    } else {
                        mnemonic = new SubstringBounds(start, end);
                        break;
                    }
                }

                // Parse the operands.
                if (onTheLine = skipWhitespaceFromCurrent(reader, onTheLine, startOfLogicalLine, continuationCharacters)) {
                    currentCodePoint = reader.getCurrentCodePoint();
                    if (currentCodePoint == ';') {
                        // There are no operands; this is just a comment.
                        comment = readComment(reader, startOfLogicalLine);
                        onTheLine = false;
                    } else {
                        int currentOperandStart = reader.getCurrentPosition() - startOfLogicalLine;
                        int startOfTrailingWhitespace = -1;
                        int numberOfParentheses = 0;
                        int inString = -1;
                        int startOfString = -1;
                        boolean inNumberOrIdentifier = false;
                        boolean readComment = false;

                        inner: do {
                            currentCodePoint = reader.getCurrentCodePoint();

                            if (inString != -1) {
                                if (currentCodePoint == '\\') {
                                    // Skip the character after the backslash.
                                    onTheLine = advanceReadLogicalChar(reader, startOfLogicalLine, continuationCharacters);
                                } else if (currentCodePoint == inString) {
                                    inString = -1;
                                    startOfString = -1;
                                }
                            } else {
                                if (inNumberOrIdentifier && !Syntax.isValidIdentifierCodePoint(currentCodePoint)) {
                                    inNumberOrIdentifier = false;
                                }

                                if (!inNumberOrIdentifier) {
                                    switch (currentCodePoint) {
                                    case '(':
                                        ++numberOfParentheses;
                                        break;

                                    case ')':
                                        if (numberOfParentheses == 0) {
                                            if (parseError == null) {
                                                parseError = new MismatchedParenthesisParseError(reader.getCurrentPosition()
                                                        - startOfLogicalLine);
                                            }
                                        } else {
                                            --numberOfParentheses;
                                        }

                                        break;

                                    case '"':
                                    case '\'':
                                        inString = currentCodePoint;
                                        startOfString = reader.getCurrentPosition() - startOfLogicalLine;
                                        break;

                                    case ';':
                                        readComment = true;
                                        break inner;

                                    case ',':
                                        if (numberOfParentheses == 0) {
                                            addOperand(operands, currentOperandStart, startOfTrailingWhitespace,
                                                    reader.getCurrentPosition() - startOfLogicalLine);
                                            onTheLine = skipWhitespaceFromNext(reader, startOfLogicalLine, continuationCharacters);
                                            currentOperandStart = reader.getCurrentPosition() - startOfLogicalLine;
                                            continue;
                                        }

                                        break;

                                    }

                                    if (Syntax.isWhitespace(currentCodePoint)) {
                                        if (startOfTrailingWhitespace == -1) {
                                            startOfTrailingWhitespace = reader.getCurrentPosition() - startOfLogicalLine;
                                        }
                                    } else {
                                        startOfTrailingWhitespace = -1;

                                        if (Syntax.isValidNumberInitialCodePoint(currentCodePoint)
                                                || Syntax.isValidIdentifierInitialCodePoint(currentCodePoint)) {
                                            inNumberOrIdentifier = true;
                                        }
                                    }
                                }
                            }

                            onTheLine = advanceReadLogicalChar(reader, startOfLogicalLine, continuationCharacters);
                        } while (onTheLine);

                        if (inString != -1) {
                            if (parseError == null) {
                                parseError = new UnterminatedStringParseError(startOfString);
                            }
                        }

                        if (numberOfParentheses > 0) {
                            if (parseError == null) {
                                parseError = new MismatchedParenthesisParseError(reader.getCurrentPosition() - startOfLogicalLine);
                            }
                        }

                        addOperand(operands, currentOperandStart, startOfTrailingWhitespace, reader.getCurrentPosition()
                                - startOfLogicalLine);

                        if (readComment) {
                            comment = readComment(reader, startOfLogicalLine);
                        }
                    }
                }
            }
        }

        int[] continuationCharactersArray;
        if (continuationCharacters.isEmpty()) {
            continuationCharactersArray = EMPTY_INT_ARRAY;
        } else {
            continuationCharactersArray = new int[continuationCharacters.size()];
            for (int i = 0; i < continuationCharacters.size(); i++) {
                continuationCharactersArray[i] = continuationCharacters.get(i);
            }
        }

        if (!reader.atEnd()) {
            if (reader.getCurrentCodePoint() == '\r') {
                reader.advance();
                if (reader.getCurrentCodePoint() == '\n') {
                    reader.advance();
                }
            } else if (reader.getCurrentCodePoint() == '\n') {
                reader.advance();
            } else {
                throw new AssertionError(); // unreachable
            }
        }

        final int length = reader.getCurrentPosition() - startOfLogicalLine;
        final LogicalLine logicalLine = new LogicalLine(length, parseError, labels.toArray(EMPTY_SUBSTRING_BOUNDS_ARRAY), mnemonic,
                operands.toArray(EMPTY_SUBSTRING_BOUNDS_ARRAY), comment, continuationCharactersArray);
        return logicalLine;
    }

    /**
     * Adds a label to the logical line.
     *
     * @param labels
     *            the list of labels on the logical line
     * @param start
     *            the starting position of the label
     * @param end
     *            the ending position of the label
     * @param parseError
     *            the current parse error for the logical line
     * @return the new parse error for the logical line
     */
    @CheckForNull
    private static ParseError addLabel(@Nonnull ArrayList<SubstringBounds> labels, int start, int end,
            @CheckForNull ParseError parseError) {
        if (parseError == null && start == end) {
            parseError = new LabelExpectedParseError(start);
        }

        labels.add(new SubstringBounds(start, end));
        return parseError;
    }

    /**
     * Adds an operand to the logical line.
     *
     * @param operands
     *            the list of operands on the logical line
     * @param currentOperandStart
     *            the starting position of the operand
     * @param startOfTrailingWhitespace
     *            the position of the first whitespace at the end of the operand, or -1 if there is no trailing whitespace
     * @param currentPosition
     *            the reader's current position within the logical line
     */
    private static void addOperand(@Nonnull ArrayList<SubstringBounds> operands, int currentOperandStart,
            int startOfTrailingWhitespace, int currentPosition) {
        final int currentOperandEnd;
        if (startOfTrailingWhitespace != -1) {
            currentOperandEnd = startOfTrailingWhitespace;
        } else {
            currentOperandEnd = currentPosition;
        }

        operands.add(new SubstringBounds(currentOperandStart, currentOperandEnd));
    }

    /**
     * Advances the reader and reads the current character and code point, processing continuation characters as they are
     * encountered.
     *
     * @param reader
     *            the {@link CharSequenceReader} to read from
     * @param startOfLogicalLine
     *            the logical line's starting position
     * @param continuationCharacters
     *            the list of continuation characters on the logical line
     * @return <code>true</code> if the next character is on the same logical line, or <code>false</code> if the next character is a
     *         line separator.
     */
    private static boolean advanceReadLogicalChar(@Nonnull CharSequenceReader<?> reader, int startOfLogicalLine,
            @Nonnull ArrayList<Integer> continuationCharacters) {
        reader.advance();
        return readLogicalChar(reader, startOfLogicalLine, continuationCharacters);
    }

    /**
     * Determines if the specified reader's current character is on the logical line being parsed or ends the logical line.
     *
     * @param reader
     *            the {@link CharSequenceReader} to read from
     * @return <code>true</code> if the current character is on the logical line being parsed, or <code>false</code> if the reader
     *         is at the end of its input or the current character is a line separator.
     */
    private static boolean isOnTheLine(@Nonnull CharSequenceReader<?> reader) {
        switch (reader.getCurrentCodePoint()) {
        case -1:
        case '\n':
        case '\r':
            return false;
        }

        return true;
    }

    /**
     * Reads the comment on the logical line.
     *
     * @param reader
     *            the {@link CharSequenceReader} to read from
     * @param startOfLogicalLine
     *            the logical line's starting position
     * @return the {@link SubstringBounds} for the comment
     */
    @Nonnull
    private static SubstringBounds readComment(@Nonnull CharSequenceReader<?> reader, int startOfLogicalLine) {
        int start = reader.getCurrentPosition() - startOfLogicalLine;

        // Skip to the end of the line, ignoring potential continuation characters.
        do {
            reader.advance();
        } while (isOnTheLine(reader));

        return new SubstringBounds(start, reader.getCurrentPosition() - startOfLogicalLine);
    }

    /**
     * Reads a label or mnemonic.
     *
     * @param reader
     *            the {@link CharSequenceReader} to read from
     * @param startOfLogicalLine
     *            the logical line's starting position
     * @param continuationCharacters
     *            the list of continuation characters on the logical line
     * @return <code>true</code> if the current character is on the same logical line, or <code>false</code> if the current
     *         character is a line separator
     */
    private static boolean readLabelOrMnemonic(CharSequenceReader<?> reader, int startOfLogicalLine,
            ArrayList<Integer> continuationCharacters) {
        boolean onTheLine;
        boolean isFirstCodePoint = true;

        // Find the end of the label or mnemonic.
        while (onTheLine = readLogicalChar(reader, startOfLogicalLine, continuationCharacters)) {
            int currentCodePoint = reader.getCurrentCodePoint();

            if (currentCodePoint == '=') {
                // If the label or mnemonic starts with a '=' character,
                // then it is the only character in that label or mnemonic.
                if (isFirstCodePoint) {
                    onTheLine = advanceReadLogicalChar(reader, startOfLogicalLine, continuationCharacters);
                }

                break;
            }

            if (currentCodePoint == ':' || currentCodePoint == ';' || Syntax.isWhitespace(currentCodePoint)) {
                // We found the end of the label or mnemonic, break out of the loop.
                break;
            }

            reader.advance();
            isFirstCodePoint = false;
        }

        return onTheLine;
    }

    /**
     * Reads the current character and code point, processing continuation characters as they are encountered.
     *
     * @param reader
     *            the {@link CharSequenceReader} to read from
     * @param startOfLogicalLine
     *            the logical line's starting position
     * @param continuationCharacters
     *            the list of continuation characters on the logical line
     * @return <code>true</code> if the current character is on the same logical line, or <code>false</code> if the current
     *         character is a line separator
     */
    private static boolean readLogicalChar(@Nonnull CharSequenceReader<?> reader, int startOfLogicalLine,
            @Nonnull ArrayList<Integer> continuationCharacters) {
        boolean onTheLine = isOnTheLine(reader);

        while (onTheLine && reader.getCurrentCodePoint() == '&') {
            final int position = reader.getCurrentPosition() - startOfLogicalLine;

            reader.advance();
            final int codePoint = reader.getCurrentCodePoint();

            // If the ampersand is followed by a line separator, then it is a continuation character.
            // Otherwise, it is just a normal character.
            if (codePoint != '\r' && codePoint != '\n') {
                reader.rewind();
                break;
            }

            continuationCharacters.add(position);

            // Read the line separator.
            reader.advance();
            if (codePoint == '\r' && reader.getCurrentCodePoint() == '\n') {
                reader.advance();
            }

            // Skip any leading whitespace on the line.
            onTheLine = isOnTheLine(reader);
            while (onTheLine && Syntax.isWhitespace(reader.getCurrentCodePoint())) {
                reader.advance();
                onTheLine = isOnTheLine(reader);
            }
        }

        return onTheLine;
    }

    /**
     * Skips whitespace, starting from the current character.
     *
     * @param reader
     *            the {@link CharSequenceReader} to read from
     * @param onTheLine
     *            <code>true</code> if the current character is on the logical line, or <code>false</code> if the current character
     *            is a line separator
     * @param startOfLogicalLine
     *            the logical line's starting position
     * @param continuationCharacters
     *            the list of continuation characters on the logical line
     * @return <code>true</code> if the next character is on the same logical line, or <code>false</code> if the next character is a
     *         line separator.
     */
    private static boolean skipWhitespaceFromCurrent(@Nonnull CharSequenceReader<?> reader, boolean onTheLine,
            int startOfLogicalLine, @Nonnull ArrayList<Integer> continuationCharacters) {
        while (onTheLine && Syntax.isWhitespace(reader.getCurrentCodePoint())) {
            reader.advance();
            onTheLine = readLogicalChar(reader, startOfLogicalLine, continuationCharacters);
        }

        return onTheLine;
    }

    /**
     * Moves to the next character, then skips whitespace.
     *
     * @param reader
     *            the {@link CharSequenceReader} to read from
     * @param startOfLogicalLine
     *            the logical line's starting position
     * @param continuationCharacters
     *            the list of continuation characters on the logical line
     * @return <code>true</code> if the next character is on the same logical line, or <code>false</code> if the next character is a
     *         line separator.
     */
    private static boolean skipWhitespaceFromNext(@Nonnull CharSequenceReader<?> reader, int startOfLogicalLine,
            @Nonnull ArrayList<Integer> continuationCharacters) {
        boolean onTheLine;
        do {
            reader.advance();
        } while ((onTheLine = readLogicalChar(reader, startOfLogicalLine, continuationCharacters))
                && Syntax.isWhitespace(reader.getCurrentCodePoint()));

        return onTheLine;
    }

    // This class is not meant to be instantiated.
    private LogicalLineParser() {
    }

}
