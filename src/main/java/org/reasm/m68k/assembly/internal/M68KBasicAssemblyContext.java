package org.reasm.m68k.assembly.internal;

import org.reasm.m68k.InstructionSet;

abstract class M68KBasicAssemblyContext {

    boolean optimizeUnsizedAbsoluteAddressingToPcRelative;
    boolean optimizeZeroDisplacement;

    long programCounter;
    InstructionSet instructionSet;

    abstract GeneralPurposeRegister getRegisterAliasByName(String identifier);

}
