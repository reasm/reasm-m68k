package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import javax.annotation.Nonnull;

import org.reasm.Value;
import org.reasm.messages.UnknownEncodingNameErrorMessage;

/**
 * The <code>ENCODING</code> directive.
 *
 * @author Francis Gagné
 */
final class EncodingDirective extends Mnemonic {

    @Nonnull
    static final EncodingDirective ENCODING = new EncodingDirective();

    private EncodingDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        if (context.requireNumberOfOperands(1)) {
            final Value encodingNameValue = evaluateExpressionOperand(context, 0);
            final String encodingName = Value.accept(encodingNameValue, context.stringValueVisitor);
            if (encodingName != null) {
                final Charset charset;
                try {
                    charset = Charset.forName(encodingName);
                } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
                    context.addTentativeMessage(new UnknownEncodingNameErrorMessage(encodingName, e));
                    return;
                }

                context.builder.setCurrentEncoding(charset);
            }
        }
    }

}
