package org.reasm.m68k.assembly.internal;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.SymbolContext;

/**
 * Wraps a {@link Set}&lt;{@link GeneralPurposeRegister}&gt; to work around type erasure when used with {@link SymbolContext}.
 *
 * @author Francis Gagné
 */
@Immutable
final class RegisterList {

    @Nonnull
    private final Set<GeneralPurposeRegister> registers;

    RegisterList(@Nonnull Set<GeneralPurposeRegister> registers) {
        this.registers = registers;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        final RegisterList other = (RegisterList) obj;
        if (!this.registers.equals(other.registers)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.registers.hashCode();
        return result;
    }

    @Nonnull
    final Set<GeneralPurposeRegister> getRegisters() {
        return this.registers;
    }

}
