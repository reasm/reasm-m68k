package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyErrorMessage;
import org.reasm.m68k.messages.DephaseWithoutPhaseErrorMessage;
import org.reasm.m68k.messages.ObjendWithoutObjErrorMessage;

/**
 * The <code>OBJEND</code> and <code>DEPHASE</code> directives.
 *
 * @author Francis Gagné
 */
@Immutable
abstract class ObjendDirective extends Mnemonic {

    @Nonnull
    static final ObjendDirective DEPHASE = new ObjendDirective() {
        @Override
        AssemblyErrorMessage createWrongContextErrorMessage() {
            return new DephaseWithoutPhaseErrorMessage();
        }
    };

    @Nonnull
    static final ObjendDirective OBJEND = new ObjendDirective() {
        @Override
        AssemblyErrorMessage createWrongContextErrorMessage() {
            return new ObjendWithoutObjErrorMessage();
        }
    };

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);

        final Object block = context.getParentBlock();
        if (!(block instanceof ObjBlockState)) {
            context.addMessage(this.createWrongContextErrorMessage());
        }
    }

    @Nonnull
    abstract AssemblyErrorMessage createWrongContextErrorMessage();

}
