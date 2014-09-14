package org.reasm.m68k.assembly.internal;

import org.reasm.AssemblyStepIterationController;

final class ReptBlockState implements AssemblyStepIterationController {

    long count;

    @Override
    public boolean hasNextIteration() {
        if (this.count > 0) {
            this.count--;
            return true;
        }

        return false;
    }

}
