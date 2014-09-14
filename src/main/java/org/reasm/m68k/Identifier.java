package org.reasm.m68k;

/**
 * Contains utility methods related to identifiers.
 *
 * @author Francis Gagné
 */
public final class Identifier {

    /**
     * Determines whether the specified code point is a digit.
     *
     * @param codePoint
     *            the code point to check
     * @return <code>true</code> if the code point is in the range U+0030 to U+0039 (inclusive); otherwise, <code>false</code>
     */
    public static boolean isDigit(int codePoint) {
        return codePoint >= '0' && codePoint <= '9';
    }

    /**
     * Determines whether the specified code point is valid in an identifier.
     *
     * @param codePoint
     *            the code point to check
     * @return <code>true</code> if the code point is valid in an identifier; otherwise, <code>false</code>
     */
    public static boolean isValidIdentifierCodePoint(int codePoint) {
        switch (codePoint) {
        case '\t': // horizontal tab
        case '\n': // line feed
        case '\r': // carriage return
        case ' ': // space
        case '!': // logical NOT operator; "strictly different from" operator
        case '"': // string delimiter
        case '#': // start of immediate data
        case '$': // prefix for hexadecimal integer literals
        case '%': // modulo operator; prefix for binary integer literals
        case '&': // bitwise AND operator; logical AND operator
        case '\'': // string delimiter
        case '(': // grouping left parenthesis; start of function call argument list
        case ')': // grouping right parenthesis; end of function call argument list
        case '*': // multiplication operator; part of index register scale specification
        case '+': // addition operator; unary plus operator
        case ',': // argument separator
        case '-': // subtraction operator; negation operator; part of register range specification
            //case '.': // pseudo object member accessor (actually part of the symbol name)
        case '/': // division operator; part of register list specification
        case ':': // end of label; conditional operator, second part
        case ';': // start of comment
        case '<': // "less than" operator; "less than or equal to" operator; "different from" operator; bit shift left operator
        case '=': // "equal to" operator; "strictly equal to" operator; "strictly different from" operator; "less than or equal to" operator; "greater than or equal to" operator
        case '>': // "greater than" operator; "greater than or equal to" operator; "different from" operator; bit shift right operator
        case '?': // conditional operator, first part
            //case '@': // prefix for local symbols (part of the symbol name)
        case '[': // start of array indexer
        case '\\': // prefix for reference to a macro argument
        case ']': // end of array indexer
        case '^': // bitwise XOR operator
            //case '`': // not assigned
        case '{': // start of bit field specification
        case '|': // bitwise OR operator; logical OR operator
        case '}': // end of bit field specification
        case '~': // bitwise NOT operator
            return false;
        }

        return Character.isValidCodePoint(codePoint);
    }

    // This class is not meant to be instantiated.
    private Identifier() {
    }

}
