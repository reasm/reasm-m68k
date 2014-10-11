package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>ENDTRANSFORM</code> directive is used out of a
 * <code>TRANSFORM</code> block.
 *
 * @author Francis Gagné
 */
public class EndtransformWithoutTransformErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new EndtransformWithoutTransformErrorMessage.
     */
    public EndtransformWithoutTransformErrorMessage() {
        super("ENDTRANSFORM directive not in a TRANSFORM block");
    }

}
