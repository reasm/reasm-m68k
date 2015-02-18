package org.reasm.m68k.expressions.internal;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.commons.source.Syntax;
import org.reasm.m68k.CharSequenceParserReader;
import org.reasm.m68k.source.M68KParser;

/**
 * The tokenizer for expressions and effective addresses.
 *
 * @author Francis Gagné
 */
public final class Tokenizer {

    private CharSequenceParserReader reader;
    private int endOfBrokenSequence;
    private TokenType tokenType;
    private int tokenStart;
    private int tokenEnd;

    /**
     * Initializes a new Tokenizer.
     */
    public Tokenizer() {
    }

    /**
     * Initializes a new Tokenizer from another Tokenizer.
     *
     * @param tokenizer
     *            the other tokenizer to copy
     */
    private Tokenizer(@Nonnull Tokenizer tokenizer) {
        this.reader = tokenizer.reader.duplicate();
        this.endOfBrokenSequence = tokenizer.endOfBrokenSequence;
        this.tokenType = tokenizer.tokenType;
        this.tokenStart = tokenizer.tokenStart;
        this.tokenEnd = tokenizer.tokenEnd;
    }

    /**
     * Advances to the next token.
     *
     * @see #getTokenType()
     * @see #getTokenStart()
     * @see #getTokenEnd()
     * @see #getTokenLength()
     * @see #getTokenText()
     */
    public final void advance() {
        if (this.endOfBrokenSequence != -1) {
            int start = this.tokenEnd;
            if (start < this.endOfBrokenSequence) {
                this.setToken(TokenType.OPERATOR, start, start + 1);
                return;
            }

            this.endOfBrokenSequence = -1;
        }

        this.setToken(TokenType.END, this.tokenEnd, this.tokenEnd);

        while (Syntax.isWhitespace(this.reader.getCurrentCodePoint())) {
            this.reader.advance();
        }

        final int start = this.reader.getPosition();
        TokenType tokenType;

        final int firstCodePoint = this.reader.getCurrentCodePoint();
        int codePoint;
        switch (firstCodePoint) {
        case -1:
            return;

        case '!': // either "!" or "!="
        case '=': // either "=" or "=="
            tokenType = TokenType.OPERATOR;
            this.reader.advance();

            switch (this.reader.getCurrentCodePoint()) {
            case '=':
                this.reader.advance();
                break;
            }

            break;

        case '"': // a string delimited by double quotes
        case '\'': // a string delimited by apostrophes
            tokenType = TokenType.STRING;
            this.reader.advance();

            boolean lastWasEscape = false;
            for (;; this.reader.advance()) {
                codePoint = this.reader.getCurrentCodePoint();
                if (codePoint == -1) {
                    // The string is not terminated properly: make the token invalid.
                    tokenType = TokenType.INVALID;
                    break;
                }

                if (lastWasEscape) {
                    lastWasEscape = false;
                } else {
                    if (codePoint == firstCodePoint) {
                        // Finish the string.
                        this.reader.advance();
                        break;
                    }

                    lastWasEscape = codePoint == '\\';
                }
            }

            break;

        case '#':
            tokenType = TokenType.IMMEDIATE;
            this.reader.advance();
            break;

        case '$': // an hexadecimal integer literal
            tokenType = TokenType.HEXADECIMAL_INTEGER;
            this.reader.advance();

            boolean haveHexDigit = false;
            for (;; this.reader.advance()) {
                codePoint = this.reader.getCurrentCodePoint();

                // If the next character is not a valid identifier character or if it's a period, it's the end of the token.
                if (!M68KParser.SYNTAX.isValidIdentifierCodePoint(codePoint) || codePoint == '.') {
                    break;
                }

                // If the next character is not an hexadecimal digit, make the token invalid.
                if (!Syntax.isHexDigit(codePoint)) {
                    tokenType = TokenType.INVALID;
                    this.finishIdentifier();
                    break;
                }

                haveHexDigit = true;
            }

            // If there are no valid digits after the '$', make the token invalid.
            if (!haveHexDigit) {
                tokenType = TokenType.INVALID;
                this.finishIdentifier();
            }

            break;

        case '%': // the '%' (modulus) operator or a binary integer literal (see changeToBinaryInteger())
        case '*':
        case '/':
        case '^':
        case '~':
            tokenType = TokenType.OPERATOR;
            this.reader.advance();
            break;

        case '&': // either '&' or '&&'
            tokenType = TokenType.OPERATOR;
            this.reader.advance();

            switch (this.reader.getCurrentCodePoint()) {
            case '&':
                this.reader.advance();
                break;
            }

            break;

        case '(':
            tokenType = TokenType.OPENING_PARENTHESIS;
            this.reader.advance();
            break;

        case ')':
            tokenType = TokenType.CLOSING_PARENTHESIS;
            this.reader.advance();
            break;

        case '+': // one or more '+'
        case '-': // one or more '-'
            tokenType = TokenType.PLUS_OR_MINUS_SEQUENCE;
            this.reader.advance();

            while (this.reader.getCurrentCodePoint() == firstCodePoint) {
                this.reader.advance();
            }

            break;

        case ',':
            tokenType = TokenType.COMMA;
            this.reader.advance();
            break;

        case ':':
            tokenType = TokenType.CONDITIONAL_OPERATOR_SECOND;
            this.reader.advance();
            break;

        case ';': // a comment (not supposed to happen!)
            tokenType = TokenType.INVALID;
            this.reader.advance();
            break;

        case '<': // either "<", "<<", "<=", or "<>"
            tokenType = TokenType.OPERATOR;
            this.reader.advance();

            switch (this.reader.getCurrentCodePoint()) {
            case '<':
            case '=':
            case '>':
                this.reader.advance();
                break;
            }

            break;

        case '>': // either ">", ">=" or ">>"
            tokenType = TokenType.OPERATOR;
            this.reader.advance();

            switch (this.reader.getCurrentCodePoint()) {
            case '=':
            case '>':
                this.reader.advance();
                break;
            }

            break;

        case '?':
            tokenType = TokenType.CONDITIONAL_OPERATOR_FIRST;
            this.reader.advance();
            break;

        case '[':
            tokenType = TokenType.OPENING_BRACKET;
            this.reader.advance();
            break;

        case '\\':
            tokenType = TokenType.INVALID;
            this.reader.advance();
            for (; M68KParser.SYNTAX.isValidIdentifierCodePoint(codePoint = this.reader.getCurrentCodePoint()); this.reader
                    .advance()) {
            }

            break;

        case ']':
            tokenType = TokenType.CLOSING_BRACKET;
            this.reader.advance();
            break;

        case '{':
            tokenType = TokenType.OPENING_BRACE;
            this.reader.advance();
            break;

        case '|':
            tokenType = TokenType.OPERATOR;
            this.reader.advance();

            switch (this.reader.getCurrentCodePoint()) {
            case '|':
                this.reader.advance();
                break;
            }

            break;

        case '}':
            tokenType = TokenType.CLOSING_BRACE;
            this.reader.advance();
            break;

        default:
            if (firstCodePoint == '.' || Syntax.isDigit(firstCodePoint)) {
                // If it's a digit, then it's an integer or a real. Assume it's a decimal integer literal for now.
                // If it's a point, then it's an operator or a real. In the first pass in the loop below, the point will be found
                // and the token type will switch to REAL if there is a valid real.
                tokenType = TokenType.DECIMAL_INTEGER;

                codePoint = firstCodePoint;
                for (; codePoint != -1; this.reader.advance(), codePoint = this.reader.getCurrentCodePoint()) {
                    // If the character is a point, try to parse a real number.
                    if (codePoint == '.') {
                        CharSequenceParserReader reader2 = this.reader.duplicate();
                        reader2.advance();
                        codePoint = reader2.getCurrentCodePoint();

                        // If the decimal point is followed by a character that is not a valid identifier character, keep the point
                        // as part of this token, unless the token is only a point.
                        if (!M68KParser.SYNTAX.isValidIdentifierCodePoint(codePoint)) {
                            if (firstCodePoint == '.') {
                                break;
                            }

                            this.reader.copyFrom(reader2);
                            tokenType = TokenType.REAL;
                            break;
                        }

                        // If the decimal point is followed by a valid identifier character that is not a digit, then reject the
                        // point as a decimal separator and stay with the integer. The point will then be parsed as an operator
                        // and an identifier will follow it.
                        if (!Syntax.isDigit(codePoint)) {
                            break;
                        }

                        this.reader.copyFrom(reader2);

                        tokenType = this.readRealDigits(true);
                        if (tokenType == TokenType.INVALID) {
                            break;
                        }

                        codePoint = this.reader.getCurrentCodePoint();
                    }

                    // If the character is an 'E' or 'e', try to parse the exponential part of a floating-point number.
                    if (codePoint == 'E' || codePoint == 'e') {
                        this.reader.advance();
                        int codePoint2 = this.reader.getCurrentCodePoint();

                        // If the 'E' or 'e' is immediately followed by a '+' or '-', accept that character and advance
                        // the reader.
                        if (codePoint2 == '+' || codePoint2 == '-') {
                            this.reader.advance();
                            codePoint2 = this.reader.getCurrentCodePoint();

                            if (!Syntax.isDigit(codePoint2)) {
                                // If the '+' or '-' is not followed by a digit, make the token invalid.
                                tokenType = TokenType.INVALID;
                                this.finishIdentifier();
                                break;
                            }
                        } else if (!Syntax.isDigit(codePoint2)) {
                            // If the 'E' or 'e' is not followed by a '+', a '-' or a digit, make the token invalid.
                            tokenType = TokenType.INVALID;
                            this.finishIdentifier();
                            break;
                        }

                        tokenType = this.readRealDigits(false);
                        if (tokenType == TokenType.INVALID) {
                            break;
                        }

                        codePoint = this.reader.getCurrentCodePoint();
                    }

                    // If the token is now a real, we've reached the end of it already.
                    if (tokenType == TokenType.REAL) {
                        break;
                    }

                    // If the next character is not a valid identifier character, it's the end of the integer token.
                    if (!M68KParser.SYNTAX.isValidIdentifierCodePoint(codePoint)) {
                        break;
                    }

                    // If it's not a digit, make the token invalid.
                    if (!Syntax.isDigit(codePoint)) {
                        tokenType = TokenType.INVALID;
                        this.finishIdentifier();
                        break;
                    }
                }

                // If the first character was a point and the reader is still at its initial position, parse the period operator.
                if (firstCodePoint == '.' && this.reader.getPosition() == start) {
                    tokenType = TokenType.PERIOD;
                    this.reader.advance();
                }
            } else {
                assert M68KParser.SYNTAX.isValidIdentifierCodePoint(firstCodePoint);

                // If it's a valid code point for an identifier, then it's an identifier.
                tokenType = TokenType.IDENTIFIER;
                this.finishIdentifier();
            }

            break;
        }

        this.setToken(tokenType, start, this.reader.getPosition());
    }

    /**
     * Breaks a token of type {@link TokenType#PLUS_OR_MINUS_SEQUENCE} into a series of {@link TokenType#OPERATOR} tokens.
     *
     * @throws IllegalStateException
     *             the current token is not of type {@link TokenType#PLUS_OR_MINUS_SEQUENCE}
     * @see #getTokenType()
     * @see #getTokenStart()
     * @see #getTokenEnd()
     * @see #getTokenLength()
     * @see #getTokenText()
     */
    public final void breakSequence() {
        if (this.tokenType != TokenType.PLUS_OR_MINUS_SEQUENCE) {
            throw new IllegalStateException("The current token's type is not PLUS_OR_MINUS_SEQUENCE");
        }

        this.endOfBrokenSequence = this.tokenEnd;
        this.setToken(TokenType.OPERATOR, this.tokenStart, this.tokenStart + 1);
    }

    /**
     * Reparses the <code>%</code> operator as a binary integer.
     *
     * @throws IllegalStateException
     *             the current token is not the <code>%</code> operator
     * @see #getTokenType()
     * @see #getTokenStart()
     * @see #getTokenEnd()
     * @see #getTokenLength()
     * @see #getTokenText()
     */
    public final void changeToBinaryInteger() {
        if (this.tokenType != TokenType.OPERATOR || !this.tokenEqualsString("%")) {
            throw new IllegalStateException("The current token is not the '%' operator");
        }

        TokenType tokenType = TokenType.BINARY_INTEGER;

        boolean haveBinDigit = false;
        for (;; this.reader.advance()) {
            int codePoint = this.reader.getCurrentCodePoint();

            // If the next character is not a valid identifier character or if it's a period, it's the end of the token.
            if (!M68KParser.SYNTAX.isValidIdentifierCodePoint(codePoint) || codePoint == '.') {
                break;
            }

            // If the next character is not an hexadecimal digit, make the token invalid.
            if (!Syntax.isBinDigit(codePoint)) {
                tokenType = TokenType.INVALID;
                this.finishIdentifier();
                break;
            }

            haveBinDigit = true;
        }

        // If there are no valid digits after the '%', make the token invalid.
        if (!haveBinDigit) {
            tokenType = TokenType.INVALID;
        }

        this.setToken(tokenType, this.tokenStart, this.reader.getPosition());
    }

    /**
     * Copies the state from another Tokenizer that reads from the same {@link CharSequence}, usually a Tokenizer returned by
     * {@link #duplicateAndAdvance()}.
     *
     * @param other
     *            the other Tokenizer
     * @see #duplicateAndAdvance()
     */
    public final void copyFrom(@Nonnull Tokenizer other) {
        this.reader.copyFrom(other.reader);
        this.endOfBrokenSequence = other.endOfBrokenSequence;
        this.tokenType = other.tokenType;
        this.tokenStart = other.tokenStart;
        this.tokenEnd = other.tokenEnd;
    }

    /**
     * Creates a copy of this tokenizer and advances it to the next token.
     *
     * @return the new Tokenizer
     * @see #copyFrom(Tokenizer)
     */
    @Nonnull
    public final Tokenizer duplicateAndAdvance() {
        final Tokenizer duplicate = new Tokenizer(this);
        duplicate.advance();
        return duplicate;
    }

    /**
     * Gets the ending position of this tokenizer's current token.
     *
     * @return the current token's ending position
     */
    public final int getTokenEnd() {
        return this.tokenEnd;
    }

    /**
     * Gets the length of this tokenizer's current token.
     *
     * @return the current token's length
     */
    public final int getTokenLength() {
        return this.tokenEnd - this.tokenStart;
    }

    /**
     * Gets the starting position of this tokenizer's current token.
     *
     * @return the current token's starting position
     */
    public final int getTokenStart() {
        return this.tokenStart;
    }

    /**
     * Gets the text of this tokenizer's current token.
     *
     * @return the current token's text
     */
    @Nonnull
    public final CharSequence getTokenText() {
        return this.reader.getCharSequence().subSequence(this.tokenStart, this.tokenEnd);
    }

    /**
     * Gets the type of this tokenizer's current token.
     *
     * @return the current token's type
     */
    public final TokenType getTokenType() {
        return this.tokenType;
    }

    /**
     * Sets that {@link CharSequence} this tokenizer will read from. The first token is parsed.
     *
     * @param charSequence
     *            the {@link CharSequence} to read from
     */
    public final void setCharSequence(@Nonnull CharSequence charSequence) {
        if (charSequence == null) {
            throw new NullPointerException("charSequence");
        }

        this.reader = new CharSequenceParserReader(charSequence);
        this.endOfBrokenSequence = -1;
        this.setToken(TokenType.END, 0, 0);
        this.advance();
    }

    /**
     * Gets the character at the specified index in the text of this tokenizer's current token.
     *
     * @param index
     *            the index of the character to get
     * @return the character
     */
    public char tokenCharAt(int index) {
        return this.reader.getCharSequence().charAt(this.tokenStart + index);
    }

    /**
     * Determines whether the text of this tokenizer's current token is the same as the specified string.
     *
     * @param string
     *            the string to compare the token's text with
     * @return <code>true</code> if the token's text is equal to the string, otherwise <code>false</code>
     */
    public final boolean tokenEqualsString(@CheckForNull String string) {
        if (string == null) {
            return false;
        }

        if (this.getTokenLength() != string.length()) {
            return false;
        }

        for (int i = 0; i < this.getTokenLength(); i++) {
            if (this.tokenCharAt(i) != string.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Advances the reader until a code point that is not valid for an identifier is found.
     */
    private final void finishIdentifier() {
        int codePoint;
        do {
            this.reader.advance();
            codePoint = this.reader.getCurrentCodePoint();
        } while (M68KParser.SYNTAX.isValidIdentifierCodePoint(codePoint));
    }

    @Nonnull
    private final TokenType readRealDigits(boolean acceptScientificENotation) {
        for (;;) {
            this.reader.advance();
            int codePoint = this.reader.getCurrentCodePoint();

            // If the next character is not a valid identifier character, it's the end of the real token.
            if (!M68KParser.SYNTAX.isValidIdentifierCodePoint(codePoint)) {
                break;
            }

            // If scientific E notation is allowed at this point, and the next character is 'E' or 'e', stop here.
            if (acceptScientificENotation && (codePoint == 'E' || codePoint == 'e')) {
                break;
            }

            // If the next character is not a digit, make the token invalid.
            if (!Syntax.isDigit(codePoint)) {
                this.finishIdentifier();
                return TokenType.INVALID;
            }
        }

        return TokenType.REAL;
    }

    private final void setToken(@Nonnull TokenType tokenType, int tokenStart, int tokenEnd) {
        this.tokenType = tokenType;
        this.tokenStart = tokenStart;
        this.tokenEnd = tokenEnd;
    }

}
