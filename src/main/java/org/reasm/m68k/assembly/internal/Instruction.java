package org.reasm.m68k.assembly.internal;

import java.io.IOException;

/**
 * Base class for all instructions. This class implements the "automatic even" configuration option.
 *
 * @author Francis Gagné
 */
abstract class Instruction extends Mnemonic {

    @Override
    final void assemble(M68KAssemblyContext context) throws IOException {
        context.automaticEven();

        this.assemble2(context);
    }

    abstract void assemble2(M68KAssemblyContext context) throws IOException;

}
