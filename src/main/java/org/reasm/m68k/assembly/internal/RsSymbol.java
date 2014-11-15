package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

import org.reasm.SignedIntValue;
import org.reasm.Symbol;
import org.reasm.SymbolType;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;

/**
 * The symbol containing the value used by the <code>RS</code> directive.
 *
 * @author Francis Gagné
 */
final class RsSymbol extends Symbol {

    private long longValue;
    private boolean signed;
    private Value value;

    RsSymbol() {
        super("__RS", SymbolType.VARIABLE);
    }

    @Nonnull
    @Override
    public final Value getValue() {
        if (this.value == null) {
            if (this.signed) {
                this.value = new SignedIntValue(this.longValue);
            } else {
                this.value = new UnsignedIntValue(this.longValue);
            }
        }

        return this.value;
    }

    final void automaticEven() {
        if ((this.longValue & 1) != 0) {
            this.value = null;
            this.longValue += 1;
        }
    }

    final void incrementBy(long increment) {
        if (increment != 0) {
            this.value = null;
            this.longValue += increment;
        }
    }

    final void set(long value, boolean signed) {
        this.value = null;
        this.longValue = value;
        this.signed = signed;
    }

}
