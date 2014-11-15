package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.reasm.ValueVisitor;

interface DcValueVisitor extends ValueVisitor<Void> {

    void encode() throws IOException;

    void reset(@Nonnull InstructionSize size);

}
