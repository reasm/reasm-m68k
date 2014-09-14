package org.reasm.m68k.assembly.internal;

import javax.annotation.concurrent.Immutable;

/**
 * An addressing mode.
 *
 * @author Francis Gagné
 */
@Immutable
enum AddressingMode {

    /** The data register direct addressing mode. */
    DATA_REGISTER_DIRECT,

    /** The address register direct addressing mode. */
    ADDRESS_REGISTER_DIRECT,

    /** The address register indirect addressing mode. */
    ADDRESS_REGISTER_INDIRECT,

    /** The address register indirect with postincrement addressing mode. */
    ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT,

    /** The address register indirect with predecrement addressing mode. */
    ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT,

    /** The address register indirect with displacement addressing mode. */
    ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT,

    /** The address register indirect indexed addressing modes. */
    ADDRESS_REGISTER_INDIRECT_INDEXED,

    /** The absolute addressing modes. */
    ABSOLUTE,

    /** The program counter with displacement addressing mode. */
    PROGRAM_COUNTER_INDIRECT_WITH_DISPLACEMENT,

    /** The program counter indexed addressing modes. */
    PROGRAM_COUNTER_INDIRECT_INDEXED,

    /** The immediate data addressing mode. */
    IMMEDIATE_DATA

}
