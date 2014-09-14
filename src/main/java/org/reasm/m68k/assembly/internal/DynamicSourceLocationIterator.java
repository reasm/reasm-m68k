package org.reasm.m68k.assembly.internal;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.reasm.source.SourceLocation;

final class DynamicSourceLocationIterator implements Iterator<SourceLocation> {

    private final Iterator<SourceLocation> iterator;
    private boolean stop;
    private boolean stopAfterNext;

    DynamicSourceLocationIterator(Iterator<SourceLocation> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return !this.stop && this.iterator.hasNext();
    }

    @Override
    public SourceLocation next() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        }

        if (this.stopAfterNext) {
            this.stop = true;
        }

        return this.iterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    void stop() {
        this.stop = true;
    }

    void stopAfterNext() {
        this.stopAfterNext = true;
    }

}
