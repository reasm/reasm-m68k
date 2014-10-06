package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import org.reasm.BlockEvents;

abstract class ScopedEffectBlockEvents extends BlockEvents {

    private boolean effectApplied;

    public void effectApplied() {
        this.effectApplied = true;
    }

    @Override
    public void exitBlock() throws IOException {
        if (this.effectApplied) {
            this.cancelEffect();
        }
    }

    protected abstract void cancelEffect() throws IOException;

}
