package org.reasm.m68k.assembly.internal;

/**
 * The <code>OBJ</code> (a.k.a. <code>PHASE</code>) directive.
 *
 * @author Francis Gagné
 */
class ObjDirective extends Mnemonic {

    static final ObjDirective OBJ = new ObjDirective();

    private ObjDirective() {
    }

    @Override
    protected void assemble(M68KAssemblyContext context) {
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
