package org.reasm.m68k.assembly.internal;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Environment;
import org.reasm.OutputTransformation;
import org.reasm.OutputTransformationFactory;
import org.reasm.messages.InvalidTransformationArgumentsErrorMessage;
import org.reasm.messages.UnknownTransformationMethodErrorMessage;

/**
 * The <code>TRANSFORM</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class TransformDirective extends Mnemonic {

    @Nonnull
    static final TransformDirective TRANSFORM = new TransformDirective();

    private TransformDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) throws IOException {
        context.sizeNotAllowed();

        final ScopedEffectBlockEvents blockEvents = getScopedEffectBlockEvents(context);

        if (context.numberOfOperands >= 1) {
            // The first operand is the name of an output transformation factory.
            final Environment environment = context.builder.getAssembly().getConfiguration().getEnvironment();
            final String transformationName = context.getOperandText(0);
            final OutputTransformationFactory factory = environment.findOutputTransformationFactoryByName(transformationName);
            if (factory != null) {
                // The remaining operands are the arguments to the output transformation factory.
                final String[] transformationArguments = new String[context.numberOfOperands - 1];
                for (int i = 0; i < transformationArguments.length; i++) {
                    transformationArguments[i] = context.getOperandText(i + 1);
                }

                final OutputTransformation userTransformation = factory.create(transformationArguments, context);
                if (userTransformation != null) {
                    context.builder.enterTransformationBlock(userTransformation);
                    blockEvents.effectApplied();
                } else {
                    context.addMessage(new InvalidTransformationArgumentsErrorMessage(transformationName, transformationArguments));
                }
            } else {
                context.addMessage(new UnknownTransformationMethodErrorMessage(transformationName));
            }
        } else {
            context.addWrongNumberOfOperandsErrorMessage();
        }
    }

}
