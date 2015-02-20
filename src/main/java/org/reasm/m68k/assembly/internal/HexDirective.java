package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.source.Syntax;
import org.reasm.m68k.messages.InvalidCharacterInHexDirectiveErrorMessage;
import org.reasm.messages.OddNumberOfCharactersInHexDirectiveErrorMessage;

/**
 * The <code>HEX</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class HexDirective extends Mnemonic {

    @Nonnull
    static final HexDirective HEX = new HexDirective();

    private static int getHexDigitValue(int codePoint) {
        if (Syntax.isDigit(codePoint)) {
            return codePoint - '0';
        }

        // 'a'..'f' => 'A'..'F'
        codePoint &= ~0x20;

        if (codePoint >= 'A' && codePoint <= 'F') {
            return codePoint - ('A' - 10);
        }

        return -1;
    }

    private HexDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        final LogicalLineReader reader = context.logicalLineReader;
        final int numberOfOperands = context.numberOfOperands;
        for (int i = 0; i < numberOfOperands; i++) {
            context.prepareOperandReader(i);
            for (int currentCodePoint; !reader.atEnd(); reader.advance(), reader.skipWhitespace()) {
                currentCodePoint = reader.getCurrentCodePoint();
                assert currentCodePoint != -1;

                // Parse the first nybble of a nybble pair.
                int hexDigitValue = getHexDigitValue(currentCodePoint);
                if (hexDigitValue == -1) {
                    context.addMessage(new InvalidCharacterInHexDirectiveErrorMessage(currentCodePoint));

                    // Continue to next operand.
                    break;
                }

                byte byteValue = (byte) (hexDigitValue << 4);

                reader.advance();
                reader.skipWhitespace();
                if (reader.atEnd()) {
                    context.addMessage(new OddNumberOfCharactersInHexDirectiveErrorMessage());

                    // Continue to next operand.
                    break;
                }

                currentCodePoint = reader.getCurrentCodePoint();
                assert currentCodePoint != -1;

                // Parse the second nybble of a nybble pair.
                hexDigitValue = getHexDigitValue(currentCodePoint);
                if (hexDigitValue == -1) {
                    context.addMessage(new InvalidCharacterInHexDirectiveErrorMessage(currentCodePoint));

                    // Continue to next operand.
                    break;
                }

                byteValue |= hexDigitValue;

                // Add the parsed byte to the buffer.
                context.builder.appendAssembledData(byteValue);
            }
        }
    }

}
