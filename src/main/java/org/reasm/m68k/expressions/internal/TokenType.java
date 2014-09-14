package org.reasm.m68k.expressions.internal;

import javax.annotation.concurrent.Immutable;

/**
 * The type of a token in an expression or an effective address.
 *
 * @author Francis Gagné
 */
@Immutable
public enum TokenType {

    /** An invalid token. */
    INVALID,

    /** The end of the input. Tokens of this type always have a length of 0. */
    END,

    /** An integer in decimal notation. */
    DECIMAL_INTEGER,

    /** An integer in binary notation. */
    BINARY_INTEGER,

    /** An integer in hexadecimal notation. */
    HEXADECIMAL_INTEGER,

    /** A floating-point number. */
    REAL,

    /** A quoted string. */
    STRING,

    /** An identifier. */
    IDENTIFIER,

    /** An operator. */
    OPERATOR,

    /** A sequence of '+' or '-' characters. */
    PLUS_OR_MINUS_SEQUENCE,

    /** The '#' character. */
    IMMEDIATE,

    /** The '(' character. */
    OPENING_PARENTHESIS,

    /** The ')' character. */
    CLOSING_PARENTHESIS,

    /** The ',' character. */
    COMMA,

    /** The '.' operator. */
    PERIOD,

    /** The '?' character. */
    CONDITIONAL_OPERATOR_FIRST,

    /** The ':' character. */
    CONDITIONAL_OPERATOR_SECOND,

    /** The '[' character. */
    OPENING_BRACKET,

    /** The ']' character. */
    CLOSING_BRACKET,

    /** The '{' character. */
    OPENING_BRACE,

    /** The '}' character. */
    CLOSING_BRACE

}
