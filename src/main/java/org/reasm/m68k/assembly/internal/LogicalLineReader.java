package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

import org.reasm.SubstringBounds;
import org.reasm.commons.source.LogicalLine;
import org.reasm.commons.source.Syntax;
import org.reasm.source.SourceLocation;

import ca.fragag.text.DocumentReader;

final class LogicalLineReader {

    private DocumentReader reader;
    private LogicalLine logicalLine;
    private int startOfLogicalLine;
    private int endOfRange;

    void advance() {
        this.reader.advance();
        this.skipContinuationCharacters();
    }

    boolean atEnd() {
        return this.reader.getCurrentPosition() >= this.endOfRange;
    }

    int backupPosition() {
        return this.reader.getCurrentPosition();
    }

    char getCurrentChar() {
        return this.reader.getCurrentChar();
    }

    int getCurrentCodePoint() {
        return this.reader.getCurrentCodePoint();
    }

    @Nonnull
    String readToString() {
        final StringBuilder sb = new StringBuilder();
        while (!this.atEnd()) {
            sb.appendCodePoint(this.reader.getCurrentCodePoint());
            this.advance();
        }

        return sb.toString();
    }

    // restorePosition() doesn't check if the specified position is a position that would be skipped due to continuation characters.
    // This method should only be called with values returned from backupPosition() since the last call to setRange().
    void restorePosition(int position) {
        this.reader.setCurrentPosition(position);
    }

    void setRange(@Nonnull SourceLocation sourceLocation, @Nonnull LogicalLine logicalLine, int start, int end) {
        if (this.reader == null || sourceLocation.getFile().getText() != this.reader.getDocument()) {
            this.reader = new DocumentReader(sourceLocation.getFile().getText());
        }

        this.logicalLine = logicalLine;
        this.startOfLogicalLine = sourceLocation.getTextPosition();
        this.reader.setCurrentPosition(this.startOfLogicalLine + start);
        this.endOfRange = this.startOfLogicalLine + end;
        this.skipContinuationCharacters();
    }

    void setRange(@Nonnull SourceLocation sourceLocation, @Nonnull LogicalLine logicalLine, @Nonnull SubstringBounds bounds) {
        this.setRange(sourceLocation, logicalLine, bounds.getStart(), bounds.getEnd());
    }

    void skipWhitespace() {
        while (!this.atEnd() && Syntax.isWhitespace(this.reader.getCurrentCodePoint())) {
            this.advance();
        }
    }

    private void skipContinuationCharacters() {
        while (this.logicalLine.isContinuationCharacter(this.reader.getCurrentPosition() - this.startOfLogicalLine)) {
            this.reader.advance();

            // Skip the line separator and the leading whitespace on the following line.
            while (!this.atEnd() && Syntax.isWhitespace(this.reader.getCurrentCodePoint())) {
                this.reader.advance();
            }
        }
    }

}
