package org.reasm.m68k.assembly.internal;

import java.util.TreeMap;

final class M68KTestAssemblyContext extends M68KBasicAssemblyContext {

    final TreeMap<String, GeneralPurposeRegister> registerAliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Override
    GeneralPurposeRegister getRegisterAliasByName(String identifier) {
        return this.registerAliases.get(identifier);
    }

}
