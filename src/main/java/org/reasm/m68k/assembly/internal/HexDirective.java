package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

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

    private HexDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        final LogicalLineReader reader = context.logicalLineReader;
        final int numberOfOperands = context.numberOfOperands;
        for (int i = 0; i < numberOfOperands; i++) {
            context.prepareOperandReader(i);
            for (int currentCodePoint; !reader.atEnd(); reader.advance()) {
                reader.skipWhitespace();
                currentCodePoint = reader.getCurrentCodePoint();
                assert currentCodePoint != -1;

                // Parse the first nybble of a nybble pair.
                int hexCharValue = Character.digit(currentCodePoint, 16);
                if (hexCharValue == -1) {
                    context.addMessage(new InvalidCharacterInHexDirectiveErrorMessage(currentCodePoint));

                    // Continue to next operand.
                    break;
                }

                byte byteValue = (byte) (hexCharValue << 4);

                reader.advance();
                reader.skipWhitespace();
                currentCodePoint = reader.getCurrentCodePoint();
                if (currentCodePoint == -1) {
                    context.addMessage(new OddNumberOfCharactersInHexDirectiveErrorMessage());

                    // Continue to next operand.
                    break;
                }

                // Parse the second nybble of a nybble pair.
                hexCharValue = Character.digit(currentCodePoint, 16);
                if (hexCharValue == -1) {
                    context.addMessage(new InvalidCharacterInHexDirectiveErrorMessage(currentCodePoint));

                    // Continue to next operand.
                    break;
                }

                byteValue |= hexCharValue;

                // Add the parsed byte to the buffer.
                context.builder.appendAssembledData(byteValue);
            }
        }
    }

}
