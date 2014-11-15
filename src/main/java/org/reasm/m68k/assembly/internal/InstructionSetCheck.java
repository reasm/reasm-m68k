package org.reasm.m68k.assembly.internal;

import javax.annotation.Nonnull;

import org.reasm.m68k.InstructionSet;

interface InstructionSetCheck {

    @Nonnull
    static final InstructionSetCheck M68000_FAMILY = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            // TODO Floating-point architectures?
            return true;
        }
    };

    @Nonnull
    static final InstructionSetCheck MC68EC000_OR_LATER = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet.compareTo(InstructionSet.MC68EC000) >= 0;
        }
    };

    @Nonnull
    static final InstructionSetCheck MC68010_OR_LATER = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet.compareTo(InstructionSet.MC68010) >= 0;
        }
    };

    @Nonnull
    static final InstructionSetCheck CPU32_ONLY = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet == InstructionSet.CPU32;
        }
    };

    @Nonnull
    static final InstructionSetCheck CPU32_OR_MC68020_OR_LATER = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet.compareTo(InstructionSet.CPU32) >= 0;
        }
    };

    @Nonnull
    static final InstructionSetCheck MC68020_ONLY = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet == InstructionSet.MC68020;
        };
    };

    @Nonnull
    static final InstructionSetCheck MC68020_OR_LATER = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet.compareTo(InstructionSet.MC68020) >= 0;
        }
    };

    boolean isSupported(@Nonnull InstructionSet instructionSet);

}
