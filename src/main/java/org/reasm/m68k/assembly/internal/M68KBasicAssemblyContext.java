package org.reasm.m68k.assembly.internal;

import java.nio.charset.Charset;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.m68k.InstructionSet;

abstract class M68KBasicAssemblyContext {

    boolean optimizeUnsizedAbsoluteAddressingToPcRelative;
    boolean optimizeZeroDisplacement;

    long programCounter;
    InstructionSet instructionSet;
    Charset encoding;

    @CheckForNull
    abstract GeneralPurposeRegister getRegisterAliasByName(@Nonnull String identifier);

}
