package org.reasm.m68k.assembly.internal;

/**
 * The <code>ORG</code> directive.
 *
 * @author Francis Gagné
 */
class OrgDirective extends Mnemonic {

    static final OrgDirective ORG = new OrgDirective();

    private OrgDirective() {
    }

    @Override
    protected void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();

        final Long value = readSingleUnsignedIntOperand(context);
        if (value != null) {
            // TODO What should ORG actually do?
            context.builder.setProgramCounter(value.longValue());
        }
    }

}
