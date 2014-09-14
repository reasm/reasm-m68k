package org.reasm.m68k.parseerrors;

import javax.annotation.concurrent.Immutable;

import org.reasm.source.ParseError;

/**
 * A parse error where a label is expected but none is provided. This is generally caused by having a colon with no label before it.
 *
 * @author Francis Gagné
 */
@Immutable
public class LabelExpectedParseError extends ParseError {

    private final int logicalPosition;

    /**
     * Initializes a new LabelExpectedParseError.
     *
     * @param logicalPosition
     *            the position in the source node where the label was expected
     */
    public LabelExpectedParseError(int logicalPosition) {
        super("Label expected");
        this.logicalPosition = logicalPosition;
    }

    /**
     * Gets the position in the source node where the label was expected.
     *
     * @return the position in the source node where the label was expected
     */
    public final int getLogicalPosition() {
        return this.logicalPosition;
    }

}
