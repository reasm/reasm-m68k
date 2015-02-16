package org.reasm.m68k.source;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.commons.source.LogicalLine;
import org.reasm.commons.source.LogicalLineAttributes;
import org.reasm.m68k.assembly.internal.SourceNodesImpl;

/**
 * A logical line in a Motorola 68000 family assembly source file.
 *
 * @author Francis Gagné
 */
@Immutable
public final class M68KLogicalLine extends LogicalLine {

    M68KLogicalLine(@Nonnull LogicalLineAttributes attributes) {
        super(attributes);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) throws IOException {
        SourceNodesImpl.assembleLogicalLine(builder);
    }

}
