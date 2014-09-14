package org.reasm.m68k.assembly.internal;

import org.reasm.m68k.InstructionSet;

interface InstructionSetCheck {

    static final InstructionSetCheck M68000_FAMILY = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            // TODO Floating-point architectures?
            return true;
        }
    };

    static final InstructionSetCheck MC68EC000_OR_LATER = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet.compareTo(InstructionSet.MC68EC000) >= 0;
        }
    };

    static final InstructionSetCheck MC68010_OR_LATER = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet.compareTo(InstructionSet.MC68010) >= 0;
        }
    };

    static final InstructionSetCheck CPU32_ONLY = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet == InstructionSet.CPU32;
        }
    };

    static final InstructionSetCheck CPU32_OR_MC68020_OR_LATER = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet.compareTo(InstructionSet.CPU32) >= 0;
        }
    };

    static final InstructionSetCheck MC68020_ONLY = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet == InstructionSet.MC68020;
        };
    };

    static final InstructionSetCheck MC68020_OR_LATER = new InstructionSetCheck() {
        @Override
        public boolean isSupported(InstructionSet instructionSet) {
            return instructionSet.compareTo(InstructionSet.MC68020) >= 0;
        }
    };

    boolean isSupported(InstructionSet instructionSet);

}
