package org.reasm.m68k.messages;

import org.reasm.AssemblyErrorMessage;

/**
 * An error message that is generated during an assembly when the operand for an <code>ORG</code>, <code>OBJ</code> or
 * <code>PHASE</code> directive is of an invalid data type.
 *
 * @author Francis Gagné
 */
public class InvalidDataTypeForOrgOrObjDirectiveErrorMessage extends AssemblyErrorMessage {

    /**
     * Initializes a new InvalidDataTypeForOrgOrObjDirectiveErrorMessage.
     */
    public InvalidDataTypeForOrgOrObjDirectiveErrorMessage() {
        super("Address operand for ORG, OBJ or PHASE directive must be an integer");
    }

}
