package org.reasm.m68k.assembly.internal;

final class MacroBlockState {

    final DynamicSourceLocationIterator iterator;

    MacroBlockState(DynamicSourceLocationIterator iterator) {
        this.iterator = iterator;
    }

}
