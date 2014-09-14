package org.reasm.m68k.assembly.internal;

import org.reasm.AssemblyMessage;
import org.reasm.m68k.messages.InvalidSizeAttributeErrorMessage;
import org.reasm.m68k.messages.NotSupportedOnArchitectureErrorMessage;
import org.reasm.messages.UnresolvedSymbolReferenceErrorMessage;
import org.reasm.messages.WrongNumberOfOperandsErrorMessage;

final class CommonExpectedMessages {

    static final AssemblyMessage WRONG_NUMBER_OF_OPERANDS = new WrongNumberOfOperandsErrorMessage();
    static final AssemblyMessage NOT_SUPPORTED_ON_ARCHITECTURE = new NotSupportedOnArchitectureErrorMessage();
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_EMPTY = new InvalidSizeAttributeErrorMessage("");
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_B = new InvalidSizeAttributeErrorMessage("B");
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_W = new InvalidSizeAttributeErrorMessage("W");
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_L = new InvalidSizeAttributeErrorMessage("L");
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_Z = new InvalidSizeAttributeErrorMessage("Z");
    static final AssemblyMessage UNDEFINED_SYMBOL = new UnresolvedSymbolReferenceErrorMessage("UNDEFINED");

}
