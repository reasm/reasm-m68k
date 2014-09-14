package org.reasm.m68k.assembly.internal;

import static java.util.Collections.unmodifiableSet;

import java.util.EnumSet;
import java.util.Set;

/**
 * Defines constants for sets of addressing modes that are allowed in instructions.
 *
 * @author Francis Gagné
 */
final class AddressingModeCategory {

    public static final Set<AddressingMode> ALL;
    public static final Set<AddressingMode> DATA;
    public static final Set<AddressingMode> MEMORY;
    public static final Set<AddressingMode> CONTROL_WITH_POSTINCREMENT;
    public static final Set<AddressingMode> CONTROL;

    public static final Set<AddressingMode> ALTERABLE;
    public static final Set<AddressingMode> DATA_ALTERABLE;
    public static final Set<AddressingMode> MEMORY_ALTERABLE;
    public static final Set<AddressingMode> CONTROL_ALTERABLE_WITH_PREDECREMENT;
    public static final Set<AddressingMode> CONTROL_ALTERABLE;

    public static final Set<AddressingMode> DATA_REGISTER_DIRECT;
    public static final Set<AddressingMode> ADDRESS_REGISTER_DIRECT;
    public static final Set<AddressingMode> DATA_OR_ADDRESS_REGISTER_DIRECT;
    public static final Set<AddressingMode> ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT;
    public static final Set<AddressingMode> ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT;
    public static final Set<AddressingMode> IMMEDIATE_DATA;
    public static final Set<AddressingMode> DATA_REGISTER_DIRECT_OR_IMMEDIATE_DATA;
    public static final Set<AddressingMode> ALL_EXCEPT_IMMEDIATE_DATA;
    public static final Set<AddressingMode> DATA_EXCEPT_IMMEDIATE_DATA;

    static {
        EnumSet<AddressingMode> all = EnumSet.allOf(AddressingMode.class);
        EnumSet<AddressingMode> data = sameWithout(all, AddressingMode.ADDRESS_REGISTER_DIRECT);
        EnumSet<AddressingMode> memory = sameWithout(data, AddressingMode.DATA_REGISTER_DIRECT);
        EnumSet<AddressingMode> controlWithPostincrement = sameWithout(memory,
                AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT, AddressingMode.IMMEDIATE_DATA);
        EnumSet<AddressingMode> control = sameWithout(controlWithPostincrement,
                AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT);

        EnumSet<AddressingMode> alterable = sameWithout(all, AddressingMode.PROGRAM_COUNTER_INDIRECT_INDEXED,
                AddressingMode.PROGRAM_COUNTER_INDIRECT_WITH_DISPLACEMENT, AddressingMode.IMMEDIATE_DATA);
        EnumSet<AddressingMode> dataAlterable = conjunction(data, alterable);
        EnumSet<AddressingMode> memoryAlterable = conjunction(memory, alterable);
        EnumSet<AddressingMode> controlAlterableWithPredecrement = sameWithout(memoryAlterable,
                AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT);
        EnumSet<AddressingMode> controlAlterable = conjunction(control, alterable);

        EnumSet<AddressingMode> dataRegisterDirect = EnumSet.of(AddressingMode.DATA_REGISTER_DIRECT);
        EnumSet<AddressingMode> addressRegisterDirect = EnumSet.of(AddressingMode.ADDRESS_REGISTER_DIRECT);
        EnumSet<AddressingMode> dataOrAddressRegisterDirect = EnumSet.of(AddressingMode.DATA_REGISTER_DIRECT,
                AddressingMode.ADDRESS_REGISTER_DIRECT);
        EnumSet<AddressingMode> addressRegisterIndirectWithPostincrement = EnumSet
                .of(AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT);
        EnumSet<AddressingMode> addressRegisterIndirectWithPredecrement = EnumSet
                .of(AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT);
        EnumSet<AddressingMode> immediateData = EnumSet.of(AddressingMode.IMMEDIATE_DATA);
        EnumSet<AddressingMode> dataRegisterDirectOrImmediateData = EnumSet.of(AddressingMode.DATA_REGISTER_DIRECT,
                AddressingMode.IMMEDIATE_DATA);
        EnumSet<AddressingMode> allExceptImmediateData = sameWithout(all, AddressingMode.IMMEDIATE_DATA);
        EnumSet<AddressingMode> dataExceptImmediateData = sameWithout(data, AddressingMode.IMMEDIATE_DATA);

        ALL = unmodifiableSet(all);
        DATA = unmodifiableSet(data);
        MEMORY = unmodifiableSet(memory);
        CONTROL_WITH_POSTINCREMENT = unmodifiableSet(controlWithPostincrement);
        CONTROL = unmodifiableSet(control);

        ALTERABLE = unmodifiableSet(alterable);
        DATA_ALTERABLE = unmodifiableSet(dataAlterable);
        MEMORY_ALTERABLE = unmodifiableSet(memoryAlterable);
        CONTROL_ALTERABLE_WITH_PREDECREMENT = unmodifiableSet(controlAlterableWithPredecrement);
        CONTROL_ALTERABLE = unmodifiableSet(controlAlterable);

        DATA_REGISTER_DIRECT = unmodifiableSet(dataRegisterDirect);
        ADDRESS_REGISTER_DIRECT = unmodifiableSet(addressRegisterDirect);
        DATA_OR_ADDRESS_REGISTER_DIRECT = unmodifiableSet(dataOrAddressRegisterDirect);
        ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT = unmodifiableSet(addressRegisterIndirectWithPostincrement);
        ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT = unmodifiableSet(addressRegisterIndirectWithPredecrement);
        IMMEDIATE_DATA = unmodifiableSet(immediateData);
        DATA_REGISTER_DIRECT_OR_IMMEDIATE_DATA = unmodifiableSet(dataRegisterDirectOrImmediateData);
        ALL_EXCEPT_IMMEDIATE_DATA = unmodifiableSet(allExceptImmediateData);
        DATA_EXCEPT_IMMEDIATE_DATA = unmodifiableSet(dataExceptImmediateData);
    }

    private static <E extends Enum<E>> EnumSet<E> conjunction(EnumSet<E> left, EnumSet<E> right) {
        final EnumSet<E> result = EnumSet.copyOf(left);
        result.retainAll(right);
        return result;
    }

    private static <E extends Enum<E>> EnumSet<E> sameWithout(EnumSet<E> baseSet, E excluded) {
        final EnumSet<E> result = EnumSet.copyOf(baseSet);
        result.remove(excluded);
        return result;
    }

    private static <E extends Enum<E>> EnumSet<E> sameWithout(EnumSet<E> baseSet, E excluded1, E excluded2) {
        final EnumSet<E> result = EnumSet.copyOf(baseSet);
        result.remove(excluded1);
        result.remove(excluded2);
        return result;
    }

    private static <E extends Enum<E>> EnumSet<E> sameWithout(EnumSet<E> baseSet, E excluded1, E excluded2, E excluded3) {
        final EnumSet<E> result = EnumSet.copyOf(baseSet);
        result.remove(excluded1);
        result.remove(excluded2);
        result.remove(excluded3);
        return result;
    }

    // This class is not meant to be instantiated.
    private AddressingModeCategory() {
    }

}
