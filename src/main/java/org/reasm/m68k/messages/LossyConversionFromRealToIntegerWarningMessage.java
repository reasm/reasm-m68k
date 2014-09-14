package org.reasm.m68k.messages;

import org.reasm.AssemblyWarningMessage;

/**
 * A warning message that is generated during an assembly when a real (floating-point) value is converted to an integer and that
 * value, when converted back to real, is different from the original value, which represents loss of information.
 *
 * @author Francis Gagné
 */
public class LossyConversionFromRealToIntegerWarningMessage extends AssemblyWarningMessage {

    private final double value;

    /**
     * Initializes a new LossyConversionFromRealToIntegerWarningMessage.
     *
     * @param value
     *            the value that is involved in a lossy conversion
     */
    public LossyConversionFromRealToIntegerWarningMessage(double value) {
        super("Lossy conversion from real to integer: " + value);
        this.value = value;
    }

    /**
     * Gets the value that generated this warning message.
     *
     * @return the value
     */
    public final double getValue() {
        return this.value;
    }

}
