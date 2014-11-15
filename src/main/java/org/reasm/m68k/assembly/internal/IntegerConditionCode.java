package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The condition codes for integer instructions (Bcc, DBcc, Scc, TRAPcc).
 * <p>
 * The enum constants are defined in the order that matches their encoding. Thus, {@link Enum#ordinal()} can be used to get the
 * encoding of a condition code.
 * <p>
 * The constants {@link #HS} and {@link #LO} are aliases for {@link #CC} and {@link #CS}, respectively. They are not part of the
 * enum per se.
 *
 * @author Francis Gagné
 */
@Immutable
enum IntegerConditionCode {

    // The order of these values must match their encoding, because Enum.ordinal() is used on this type.
    T, F, HI, LS, CC, CS, NE, EQ, VC, VS, PL, MI, GE, LT, GT, LE;

    // HS is an alias for CC, and LO is an alias for CS.
    @Nonnull
    public static final IntegerConditionCode HS = CC;
    @Nonnull
    public static final IntegerConditionCode LO = CS;

}
