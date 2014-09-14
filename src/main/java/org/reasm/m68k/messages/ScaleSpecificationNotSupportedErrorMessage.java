package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a scale specification (other than <code>*1</code>) is present on an
 * index register but the architecture does not support scale specifications.
 *
 * @author Francis Gagné
 */
public class ScaleSpecificationNotSupportedErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new ScaleSpecificationNotSupportedErrorMessage.
     */
    public ScaleSpecificationNotSupportedErrorMessage() {
        super("Scale specification not supported on the current architecture");
    }

}
