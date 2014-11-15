package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

final class IfBlockState {

    @Nonnull
    final DynamicSourceLocationIterator iterator;

    IfBlockState(@Nonnull DynamicSourceLocationIterator iterator) {
        this.iterator = iterator;
    }

}
