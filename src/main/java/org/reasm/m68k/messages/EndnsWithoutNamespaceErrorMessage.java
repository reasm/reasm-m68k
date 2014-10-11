package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>ENDNS</code> directive is used out of a
 * <code>NAMESPACE</code> block.
 *
 * @author Francis Gagné
 */
public class EndnsWithoutNamespaceErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new EndnsWithoutNamespaceErrorMessage.
     */
    public EndnsWithoutNamespaceErrorMessage() {
        super("ENDNS directive not in a NAMESPACE block");
    }

}
