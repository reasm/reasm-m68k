package org.reasm.m68k.messages;

import org.reasm.AssemblyWarningMessage;

/**
 * A warning message that is generated during an assembly when a register list in a <code>MOVEM</code> instruction or a
 * <code>REG</code> directive contains duplicate registers.
 *
 * @author Francis Gagné
 */
public class DuplicateRegistersInRegisterListWarningMessage extends AssemblyWarningMessage {

    /**
     * Initializes a new DuplicateRegistersInRegisterListWarningMessage.
     */
    public DuplicateRegistersInRegisterListWarningMessage() {
        super("Duplicate registers in register list");
    }

}
