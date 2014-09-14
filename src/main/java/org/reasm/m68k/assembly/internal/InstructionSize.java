package org.reasm.m68k.assembly.internal;

import javax.annotation.concurrent.Immutable;

/**
 * The size of an instruction.
 *
 * @author Francis Gagné
 */
@Immutable
enum InstructionSize {

    /** The size attribute was not specified. */
    DEFAULT,

    /** Byte (.b). */
    BYTE,

    /** Word (.w). */
    WORD,

    /** Long word (.l). */
    LONG,

    /** Quad word (.q). */
    QUAD,

    /** Single-precision floating-point (.s). */
    SINGLE,

    /** Double-precision floating-point (.d). */
    DOUBLE,

    /** Extended-precision floating-point (.x). */
    EXTENDED,

    /** Packed decimal (.p). */
    PACKED,

    /** Invalid size attribute. */
    INVALID;

}
