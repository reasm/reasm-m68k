package org.reasm.m68k.assembly.internal;

import org.reasm.AssemblyErrorMessage;
import org.reasm.m68k.messages.DephaseWithoutPhaseErrorMessage;
import org.reasm.m68k.messages.ObjendWithoutObjErrorMessage;

/**
 * The <code>OBJEND</code> (a.k.a. <code>DEPHASE</code>) directive.
 *
 * @author Francis Gagné
 */
abstract class ObjendDirective extends Mnemonic {

    static final ObjendDirective DEPHASE = new ObjendDirective() {
        @Override
        AssemblyErrorMessage createWrongContextErrorMessage() {
            return new DephaseWithoutPhaseErrorMessage();
        }
    };

    static final ObjendDirective OBJEND = new ObjendDirective() {
        @Override
        AssemblyErrorMessage createWrongContextErrorMessage() {
            return new ObjendWithoutObjErrorMessage();
        }
    };

    @Override
    protected void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();
        context.requireNumberOfOperands(0);
        this.assembleCore(context);
    }

    void assembleCore(M68KAssemblyContext context) {
        final Object block = context.getParentBlock();
        if (block instanceof ObjBlockState) {
            final ObjBlockState objBlockState = (ObjBlockState) block;
            if (objBlockState.programCounterOffset != 0) {
                context.builder.setProgramCounter(context.builder.getAssembly().getProgramCounter()
                        + objBlockState.programCounterOffset);
            }
        } else {
            context.addMessage(this.createWrongContextErrorMessage());
        }
    }

    abstract AssemblyErrorMessage createWrongContextErrorMessage();

}
