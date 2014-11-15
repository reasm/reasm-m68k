package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * The <code>OBJ</code> (a.k.a. <code>PHASE</code>) directive.
 *
 * @author Francis Gagné
 */
@Immutable
class ObjDirective extends Mnemonic {

    @Nonnull
    static final ObjDirective OBJ = new ObjDirective();

    private ObjDirective() {
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        context.sizeNotAllowed();

        final Object block = context.getParentBlock();
        if (!(block instanceof ObjBlockState)) {
            throw new AssertionError();
        }

        final ObjBlockState objBlockState = (ObjBlockState) block;
        final Long newProgramCounter = readSingleUnsignedIntOperand(context);
        if (newProgramCounter != null) {
            final long newProgramCounterValue = newProgramCounter.longValue();
            objBlockState.programCounterOffset = context.builder.getAssembly().getProgramCounter() - newProgramCounterValue;
            context.builder.setProgramCounter(newProgramCounterValue);
        } else {
            objBlockState.programCounterOffset = 0;
        }
    }

}
