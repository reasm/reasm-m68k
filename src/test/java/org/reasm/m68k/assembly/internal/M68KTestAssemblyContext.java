package org.reasm.m68k.assembly.internal;

import java.util.TreeMap;

import javax.annotation.Nonnull;

final class M68KTestAssemblyContext extends M68KBasicAssemblyContext {

    @Nonnull
    final TreeMap<String, GeneralPurposeRegister> registerAliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    GeneralPurposeRegister getRegisterAliasByName(String identifier) {
        return this.registerAliases.get(identifier);
    }

}
