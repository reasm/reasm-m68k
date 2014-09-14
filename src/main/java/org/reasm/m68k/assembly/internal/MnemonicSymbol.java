package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

import org.reasm.Symbol;
import org.reasm.SymbolType;

/**
 * A built-in mnemonic symbol.
 *
 * @author Francis Gagné
 */
final class MnemonicSymbol extends Symbol {

    @Nonnull
    private final Mnemonic value;

    MnemonicSymbol(@Nonnull String name, @Nonnull Mnemonic value) {
        super(name, SymbolType.CONSTANT);
        this.value = value;
    }

    @Override
    public final Mnemonic getValue() {
        return this.value;
    }

}
