package org.reasm.m68k.assembly.internal;

import javax.annotation.CheckForNull;

import org.reasm.AssemblyStepIterationController;
import org.reasm.Value;

final class ForBlockState implements AssemblyStepIterationController {

    DynamicSourceLocationIterator iterator;
    boolean hasNextIteration = true;
    boolean parsed;
    @CheckForNull
    Object labels; // null (no labels), String (one label) or String[] (many labels)
    @CheckForNull
    Value counter;
    @CheckForNull
    Value to;
    @CheckForNull
    Value step;
    boolean stepIsNegative;

    @Override
    public boolean hasNextIteration() {
        return this.hasNextIteration;
    }

}
