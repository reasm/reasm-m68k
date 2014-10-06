package org.reasm.m68k.assembly.internal;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.reasm.Architecture;
import org.reasm.Value;
import org.reasm.messages.ArchitectureNotRegisteredErrorMessage;
import org.reasm.source.SourceFile;

/**
 * The <code>INCLUDE</code> directive.
 *
 * @author Francis Gagné
 */
class IncludeDirective extends Mnemonic {

    static final IncludeDirective INCLUDE = new IncludeDirective();

    static String getFilePath(M68KAssemblyContext context, int operandIndex) {
        final Value value = evaluateExpressionOperand(context, operandIndex);
        return Value.accept(value, context.stringValueVisitor);
    }

    private IncludeDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        if (context.numberOfOperands < 1) {
            context.addWrongNumberOfOperandsErrorMessage();
            return;
        }

        if (context.numberOfOperands > 2) {
            context.addWrongNumberOfOperandsErrorMessage();
        }

        final String filePath = getFilePath(context, 0);
        if (filePath != null) {
            final SourceFile sourceFile = context.builder.getAssembly().fetchSourceFile(filePath);
            if (sourceFile == null) {
                throw new FileNotFoundException(filePath);
            }

            Architecture architecture = null;
            if (context.numberOfOperands >= 2) {
                final String architectureName = context.getOperandText(1);
                architecture = context.builder.getAssembly().getConfiguration().getEnvironment()
                        .findArchitectureByName(architectureName);
                if (architecture == null) {
                    context.addMessage(new ArchitectureNotRegisteredErrorMessage(architectureName));
                }
            }

            context.builder.enterFile(sourceFile, architecture);
        }
    }

}
