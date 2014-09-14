package org.reasm.m68k;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Iterator;

import org.junit.Test;
import org.reasm.Architecture;

/**
 * Test class for {@link M68KArchitectureProvider}.
 *
 * @author Francis Gagné
 */
public class M68KArchitectureProviderTest {

    private static final M68KArchitectureProvider M68K_ARCHITECTURE_PROVIDER = new M68KArchitectureProvider();

    /**
     * Asserts that {@link M68KArchitectureProvider#iterator()} returns an {@link Iterator} over all the {@link M68KArchitecture}
     * objects.
     */
    @Test
    public void iterator() {
        final Iterator<Architecture> iterator = M68K_ARCHITECTURE_PROVIDER.iterator();
        assertThat(iterator.next(), is(sameInstance((Architecture) M68KArchitecture.MC68000)));
        assertThat(iterator.next(), is(sameInstance((Architecture) M68KArchitecture.MC68EC000)));
        assertThat(iterator.next(), is(sameInstance((Architecture) M68KArchitecture.MC68010)));
        assertThat(iterator.next(), is(sameInstance((Architecture) M68KArchitecture.CPU32)));
        assertThat(iterator.next(), is(sameInstance((Architecture) M68KArchitecture.MC68020)));
        assertThat(iterator.next(), is(sameInstance((Architecture) M68KArchitecture.MC68030)));
        assertThat(iterator.next(), is(sameInstance((Architecture) M68KArchitecture.MC68EC030)));
        assertThat(iterator.next(), is(sameInstance((Architecture) M68KArchitecture.MC68040)));
        assertThat(iterator.next(), is(sameInstance((Architecture) M68KArchitecture.MC68EC040)));
        assertThat(iterator.hasNext(), is(false));
    }

}
