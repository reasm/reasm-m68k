package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Symbol;
import org.reasm.SymbolType;

/**
 * A built-in mnemonic symbol.
 *
 * @author Francis Gagné
 */
@Immutable
final class MnemonicSymbol extends Symbol {

    @Nonnull
    private final Mnemonic value;

    MnemonicSymbol(@Nonnull String name, @Nonnull Mnemonic value) {
        super(name, SymbolType.CONSTANT);
        this.value = value;
    }

    @Nonnull
    @Override
    public final Mnemonic getValue() {
        return this.value;
    }

}
