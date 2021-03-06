package org.reasm.m68k;

import java.util.Iterator;

import javax.annotation.concurrent.Immutable;

import org.reasm.Architecture;
import org.reasm.ArchitectureProvider;

/**
 * The implementation of {@link ArchitectureProvider} for the M68000 architecture family.
 *
 * @author Francis Gagné
 */
@Immutable
public final class M68KArchitectureProvider implements ArchitectureProvider {

    @Override
    public Iterator<Architecture> iterator() {
        return M68KArchitecture.ALL_ARCHITECTURES.iterator();
    }

}
