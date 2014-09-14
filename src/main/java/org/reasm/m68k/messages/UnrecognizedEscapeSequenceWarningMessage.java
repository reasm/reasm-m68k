package org.reasm.m68k.messages;

import org.reasm.AssemblyWarningMessage;

/**
 * A warning message that is generated during an assembly when an unrecognized escape sequence is encountered in a string literal.
 * The code point following the backslash is simply taken as is.
 *
 * @author Francis Gagné
 */
public class UnrecognizedEscapeSequenceWarningMessage extends AssemblyWarningMessage {

    private final int codePoint;

    /**
     * Initializes a new UnrecognizedEscapeSequenceWarningMessage.
     *
     * @param codePoint
     *            the code point following the backslash in the string literal
     */
    public UnrecognizedEscapeSequenceWarningMessage(int codePoint) {
        super(new StringBuilder("Unrecognized escape sequence: \\").appendCodePoint(codePoint).toString());
        this.codePoint = codePoint;
    }

    /**
     * Gets the code point that triggered this message.
     *
     * @return the code point
     */
    public final int getCodePoint() {
        return this.codePoint;
    }

}
