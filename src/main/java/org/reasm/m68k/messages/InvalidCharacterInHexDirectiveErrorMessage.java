package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an invalid character is encountered in an <code>HEX</code> directive.
 *
 * @author Francis Gagné
 */
public class InvalidCharacterInHexDirectiveErrorMessage extends AssemblyErrorMessage {

    private final int codePoint;

    /**
     * Initializes a new InvalidCharacterInHexDirectiveErrorMessage.
     *
     * @param codePoint
     *            the invalid code point
     */
    public InvalidCharacterInHexDirectiveErrorMessage(int codePoint) {
        super(new StringBuilder("Invalid character in HEX directive: ").appendCodePoint(codePoint).toString());
        this.codePoint = codePoint;
    }

    /**
     * Gets the invalid code point that was encountered.
     *
     * @return the code point
     */
    public final int getCodePoint() {
        return this.codePoint;
    }

}
