package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>ORG</code> directive.
 *
 * @author Francis Gagné
 */
@Immutable
class OrgDirective extends Mnemonic {

    @Nonnull
    static final OrgDirective ORG = new OrgDirective();

    private OrgDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();

        final Long value = readSingleUnsignedIntOperand(context);
        if (value != null) {
            // TODO What should ORG actually do?
            context.builder.setProgramCounter(value.longValue());
        }
    }

}
