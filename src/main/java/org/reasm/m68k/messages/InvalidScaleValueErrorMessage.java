package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an index register specifies an invalid value in the scale
 * specification.
 *
 * @author Francis Gagné
 */
public class InvalidScaleValueErrorMessage extends AssemblyErrorMessage {

    private final int scale;

    /**
     * Initializes a new InvalidScaleValueErrorMessage.
     *
     * @param scale
     *            the invalid scale value
     */
    public InvalidScaleValueErrorMessage(int scale) {
        super("Invalid scale value: " + scale);
        this.scale = scale;
    }

    /**
     * Gets the invalid scale value that generated this error message.
     *
     * @return the invalid scale value
     */
    public final int getScale() {
        return this.scale;
    }

}
