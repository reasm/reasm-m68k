package org.reasm.m68k.parseerrors;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.reasm.m68k.source.BlockDirective;
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
     * @param directive
     *            the directive of the offending clause
     */
    public ElseOrElseIfAfterElseParseError(@Nonnull BlockDirective directive) {
        super(Objects.requireNonNull(directive, "directive") + " clause after an ELSE clause");
    }

}
