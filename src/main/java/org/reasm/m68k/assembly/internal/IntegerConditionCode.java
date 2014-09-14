package org.reasm.m68k.assembly.internal;

/**
 * The condition codes for integer instructions (Bcc, DBcc, Scc, TRAPcc).
 * <p>
 * The enum constants are defined in the order that matches their encoding. Thus, {@link Enum#ordinal()} can be used to get the
 * encoding of a condition code.
 * <p>
 * The constants <code>HS</code> and <code>LO</code> are aliases for <code>CC</code> and <code>CS</code>, respectively. They are not
 * part of the enum per se.
 *
 * @author Francis Gagné
 */
enum IntegerConditionCode {

    // The order of these values must match their encoding, because Enum.ordinal() is used on this type.
    T, F, HI, LS, CC, CS, NE, EQ, VC, VS, PL, MI, GE, LT, GT, LE;

    // HS is an alias for CC, and LO is an alias for CS.
    public static final IntegerConditionCode HS = CC;
    public static final IntegerConditionCode LO = CS;

}
