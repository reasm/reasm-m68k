package org.reasm.m68k.assembly.internal;

/**
 * The <code>END</code> directive.
 *
 * @author Francis Gagné
 */
class EndDirective extends Mnemonic {

    static final EndDirective END = new EndDirective();

    private EndDirective() {
    }

    @Override
    protected void assemble(M68KAssemblyContext context) {
        context.builder.endPass();
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);
    }

}
