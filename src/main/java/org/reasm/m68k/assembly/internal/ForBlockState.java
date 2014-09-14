package org.reasm.m68k.assembly.internal;

import org.reasm.AssemblyStepIterationController;
import org.reasm.Value;

final class ForBlockState implements AssemblyStepIterationController {

    DynamicSourceLocationIterator iterator;
    boolean hasNextIteration = true;
    Object labels; // null (no labels), String (one label) or String[] (many labels)
    Value counter, to, step;
    boolean stepIsNegative;

    @Override
    public boolean hasNextIteration() {
        return this.hasNextIteration;
    }

}
