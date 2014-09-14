package org.reasm.m68k.parseerrors;

import java.util.Objects;

import org.reasm.source.ParseError;

/**
 * A syntax error that occurs when an <code>IF</code> block contains an <code>ELSE</code> or <code>ELSEIF</code> clause after an
 * <code>ELSE</code> clause.
 *
 * @author Francis Gagné
 */
public class ElseOrElseIfAfterElseParseError extends ParseError {

    /**
     * Initializes a new ElseOrElseIfAfterElseParseError.
     *
     * @param mnemonic
     *            the mnemonic of the offending clause
     */
    public ElseOrElseIfAfterElseParseError(String mnemonic) {
        super(Objects.requireNonNull(mnemonic, "mnemonic") + " clause after an ELSE clause");
    }

}
