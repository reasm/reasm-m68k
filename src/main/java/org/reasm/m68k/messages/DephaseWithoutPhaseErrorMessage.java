package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a <code>DEPHASE</code> directive is used out of a <code>PHASE</code>
 * block.
 *
 * @author Francis Gagné
 */
public class DephaseWithoutPhaseErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new DephaseWithoutPhaseErrorMessage.
     */
    public DephaseWithoutPhaseErrorMessage() {
        super("DEPHASE directive not in a PHASE block");
    }

}
