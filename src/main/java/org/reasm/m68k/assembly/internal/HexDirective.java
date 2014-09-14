package org.reasm.m68k.assembly.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.reasm.m68k.messages.InvalidCharacterInHexDirectiveErrorMessage;
import org.reasm.messages.OddNumberOfCharactersInHexDirectiveErrorMessage;

/**
 * The <code>HEX</code> directive.
 *
 * @author Francis Gagné
 */
class HexDirective extends Mnemonic {

    static final HexDirective HEX = new HexDirective();

    private HexDirective() {
    }

    @Override
    protected void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        final LogicalLineReader reader = context.logicalLineReader;
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        final int numberOfOperands = context.numberOfOperands;
        for (int i = 0; i < numberOfOperands; i++) {
            context.prepareOperandReader(i);
            for (int currentCodePoint; reader.getCurrentCodePoint() != -1; reader.advance()) {
                reader.skipWhitespace();
                currentCodePoint = reader.getCurrentCodePoint();
                if (currentCodePoint == -1) {
                    // Continue to next operand.
                    break;
                }

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
                data.write(byteValue);
            }
        }

        context.builder.appendAssembledData(data.toByteArray());
    }

}
