package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when an <code>OBJEND</code> directive is used out of an <code>OBJ</code>
 * block.
 *
 * @author Francis Gagné
 */
public class ObjendWithoutObjErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new ObjendWithoutObjErrorMessage.
     */
    public ObjendWithoutObjErrorMessage() {
        super("OBJEND directive not in an OBJ block");
    }

}
