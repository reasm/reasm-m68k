package org.reasm.m68k.assembly.internal;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
final class RegisterList {

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

        RegisterList other = (RegisterList) obj;
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

    final Set<GeneralPurposeRegister> getRegisters() {
        return this.registers;
    }

}
