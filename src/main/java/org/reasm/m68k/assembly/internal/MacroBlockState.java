package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

final class MacroBlockState {

    @Nonnull
    final DynamicSourceLocationIterator iterator;

    MacroBlockState(@Nonnull DynamicSourceLocationIterator iterator) {
        this.iterator = iterator;
    }

}
