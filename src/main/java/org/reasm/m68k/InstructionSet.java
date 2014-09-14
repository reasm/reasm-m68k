package org.reasm.m68k;

import javax.annotation.concurrent.Immutable;

/**
 * An identifier for a specific instruction set for one or many processor architectures in the Motorola M68K family.
 *
 * @author Francis Gagné
 */
@Immutable
public enum InstructionSet {

    /** Identifies the instruction set of the MC68000 and MC68008 processor architectures. */
    MC68000,

    /** Identifies the instruction set of the MC68EC000 processor architecture. */
    MC68EC000,

    /** Identifies the instruction set of the MC68010 and MC68012 processor architectures. */
    MC68010,

    /** Identifies the instruction set of the CPU32 processor architecture. */
    CPU32,

    /** Identifies the instruction set of the MC68020 processor architecture. */
    MC68020,

    /** Identifies the instruction set of the MC68030 processor architecture. */
    MC68030,

    /** Identifies the instruction set of the MC68EC030 processor architecture. */
    MC68EC030,

    /** Identifies the instruction set of the MC68040 processor architecture. */
    MC68040,

    /** Identifies the instruction set of the MC68EC040 processor architecture. */
    MC68EC040;

    /**
     * Returns a value indicating whether the instruction set supports the full extension word format, which allows encoding larger
     * base displacements and suppressing the base register, amongst others.
     *
     * @return <code>true</code> if the instruction set supports the full extension word format; otherwise, <code>false</code>
     */
    public final boolean supportsFullExtensionWordFormat() {
        return this.compareTo(CPU32) >= 0;
    }

    /**
     * Returns a value indicating whether the instruction set supports memory indirect addressing modes.
     *
     * @return <code>true</code> if the instruction set supports memory indirect; otherwise, <code>false</code>
     */
    public final boolean supportsMemoryIndirect() {
        return this.compareTo(MC68020) >= 0;
    }

    /**
     * Returns a value indicating whether the instruction set supports scale specifications on effective addresses.
     *
     * @return <code>true</code> if the instruction set supports scale specifications; otherwise, <code>false</code>
     */
    public final boolean supportsScale() {
        return this.compareTo(CPU32) >= 0;
    }

}
