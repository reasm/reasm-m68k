package org.reasm.m68k.assembly.internal;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.m68k.InstructionSet;

abstract class M68KBasicAssemblyContext {

    boolean optimizeUnsizedAbsoluteAddressingToPcRelative;
    boolean optimizeZeroDisplacement;

    long programCounter;
    InstructionSet instructionSet;

    @CheckForNull
    abstract GeneralPurposeRegister getRegisterAliasByName(@Nonnull String identifier);

}
