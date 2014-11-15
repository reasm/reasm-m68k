package org.reasm.m68k.assembly.internal;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
enum GeneralPurposeRegister {

    D0, D1, D2, D3, D4, D5, D6, D7, A0, A1, A2, A3, A4, A5, A6, A7;

    @Nonnull
    private static final GeneralPurposeRegister[] VALUES = GeneralPurposeRegister.values();

    @CheckForNull
    static GeneralPurposeRegister identify(@Nonnull String identifier) {
        if (identifier.length() == 2) {
            final char ch = identifier.charAt(0);
            final boolean isDataRegister = equalsAsciiCaseInsensitive(ch, 'D');
            if (isDataRegister || equalsAsciiCaseInsensitive(ch, 'A')) {
                final int registerNumber = parseRegisterNumber(identifier.charAt(1));
                if (registerNumber != -1) {
                    return VALUES[(isDataRegister ? 0 : 8) | registerNumber];
                }
            } else if (equalsAsciiCaseInsensitive(ch, 'S')) {
                if (equalsAsciiCaseInsensitive(identifier.charAt(1), 'P')) {
                    return A7;
                }
            }
        }

        return null;
    }

    private static boolean equalsAsciiCaseInsensitive(char a, char b) {
        assert b >= 'A' && b <= 'Z';
        return a == b || a == (b | 0x20);
    }

    private static int parseRegisterNumber(char ch) {
        if (ch >= '0' && ch <= '7') {
            return ch - '0';
        }

        return -1;
    }

}
