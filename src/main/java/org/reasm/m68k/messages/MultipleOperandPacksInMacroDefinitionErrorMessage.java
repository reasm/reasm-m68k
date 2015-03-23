package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when a macro defines more than one operand pack.
 *
 * @author Francis Gagné
 */
public class MultipleOperandPacksInMacroDefinitionErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new MultipleOperandPacksInMacroDefinitionErrorMessage.
     */
    public MultipleOperandPacksInMacroDefinitionErrorMessage() {
        super("The macro defines more than one operand pack; only one is allowed");
    }

}
