package org.reasm.m68k;

import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Configuration;

import ca.fragag.Consumer;

/**
 * Contains configuration options specific to the M68000 family assembler.
 *
 * @author Francis Gagné
 */
@Immutable
public final class ConfigurationOptions {

    /** The key used in a {@link Configuration} for ConfigurationOptions objects. */
    public static final Object KEY = new Object();

    /** The option key for {@link #automaticEven()}. Value type: {@link Boolean}. */
    public static final String AUTOMATIC_EVEN = "automaticEven";

    /** The option key for {@link #optimizeCmpiToTst()}. Value type: {@link Boolean}. */
    public static final String OPTIMIZE_CMPI_TO_TST = "optimizeCmpiToTst";

    /** The option key for {@link #optimizeMoveToMoveq()}. Value type: {@link Boolean}. */
    public static final String OPTIMIZE_MOVE_TO_MOVEQ = "optimizeMoveToMoveq";

    /** The option key for {@link #optimizeToAddqSubq()}. Value type: {@link Boolean}. */
    public static final String OPTIMIZE_TO_ADDQ_SUBQ = "optimizeToAddqSubq";

    /** The option key for {@link #optimizeUnsizedAbsoluteAddressingToPcRelative()}. Value type: {@link Boolean}. */
    public static final String OPTIMIZE_UNSIZED_ABSOLUTE_ADDRESSING_TO_PC_RELATIVE = "optimizeUnsizedAbsoluteAddressingToPcRelative";

    /** The option key for {@link #optimizeUnsizedBranches()}. Value type: {@link Boolean}. */
    public static final String OPTIMIZE_UNSIZED_BRANCHES = "optimizeUnsizedBranches";

    /** The option key for {@link #optimizeZeroDisplacement()}. Value type: {@link Boolean}. */
    public static final String OPTIMIZE_ZERO_DISPLACEMENT = "optimizeZeroDisplacement";

    /**
     * Creates a new ConfigurationOptions with the specified options.
     *
     * @param options
     *            a {@link Map} of option names (keys) to values. The option names are the name of the public methods in this class
     *            and are also available as constants in this class. The option names are case sensitive.
     * @param invalidEntriesConsumer
     *            an object that receives entries in the specified options that are invalid. If this is <code>null</code>, invalid
     *            entries are ignored.
     * @return the ConfigurationOptions
     */
    public static ConfigurationOptions create(@Nonnull Map<String, Object> options,
            @CheckForNull Consumer<Map.Entry<String, Object>> invalidEntriesConsumer) {
        if (options == null) {
            throw new NullPointerException("options");
        }

        boolean automaticEven = false;
        boolean optimizeCmpiToTst = false;
        boolean optimizeUnsizedBranches = false;
        boolean optimizeUnsizedAbsoluteAddressingToPcRelative = false;
        boolean optimizeToAddqSubq = false;
        boolean optimizeMoveToMoveq = false;
        boolean optimizeZeroDisplacement = false;

        for (Map.Entry<String, Object> option : options.entrySet()) {
            final Object value = option.getValue();
            boolean isEntryValid = false;
            switch (option.getKey()) {
            case AUTOMATIC_EVEN:
                if (value instanceof Boolean) {
                    automaticEven = ((Boolean) value).booleanValue();
                    isEntryValid = true;
                }

                break;

            case OPTIMIZE_CMPI_TO_TST:
                if (value instanceof Boolean) {
                    optimizeCmpiToTst = ((Boolean) value).booleanValue();
                    isEntryValid = true;
                }

                break;

            case OPTIMIZE_MOVE_TO_MOVEQ:
                if (value instanceof Boolean) {
                    optimizeMoveToMoveq = ((Boolean) value).booleanValue();
                    isEntryValid = true;
                }

                break;

            case OPTIMIZE_TO_ADDQ_SUBQ:
                if (value instanceof Boolean) {
                    optimizeToAddqSubq = ((Boolean) value).booleanValue();
                    isEntryValid = true;
                }

                break;

            case OPTIMIZE_UNSIZED_ABSOLUTE_ADDRESSING_TO_PC_RELATIVE:
                if (value instanceof Boolean) {
                    optimizeUnsizedAbsoluteAddressingToPcRelative = ((Boolean) value).booleanValue();
                    isEntryValid = true;
                }

                break;

            case OPTIMIZE_UNSIZED_BRANCHES:
                if (value instanceof Boolean) {
                    optimizeUnsizedBranches = ((Boolean) value).booleanValue();
                    isEntryValid = true;
                }

                break;

            case OPTIMIZE_ZERO_DISPLACEMENT:
                if (value instanceof Boolean) {
                    optimizeZeroDisplacement = ((Boolean) value).booleanValue();
                    isEntryValid = true;
                }

                break;
            }

            if (!isEntryValid && invalidEntriesConsumer != null) {
                invalidEntriesConsumer.accept(option);
            }
        }

        return new ConfigurationOptions(automaticEven, optimizeCmpiToTst, optimizeMoveToMoveq, optimizeToAddqSubq,
                optimizeUnsizedAbsoluteAddressingToPcRelative, optimizeUnsizedBranches, optimizeZeroDisplacement);
    }

    private final boolean automaticEven;
    private final boolean optimizeCmpiToTst;
    private final boolean optimizeMoveToMoveq;
    private final boolean optimizeToAddqSubq;
    private final boolean optimizeUnsizedAbsoluteAddressingToPcRelative;
    private final boolean optimizeUnsizedBranches;
    private final boolean optimizeZeroDisplacement;

    private ConfigurationOptions(boolean automaticEven, boolean optimizeCmpiToTst, boolean optimizeMoveToMoveq,
            boolean optimizeToAddqSubq, boolean optimizeUnsizedAbsoluteAddressingToPcRelative, boolean optimizeUnsizedBranches,
            boolean optimizeZeroDisplacement) {
        this.automaticEven = automaticEven;
        this.optimizeCmpiToTst = optimizeCmpiToTst;
        this.optimizeUnsizedBranches = optimizeUnsizedBranches;
        this.optimizeUnsizedAbsoluteAddressingToPcRelative = optimizeUnsizedAbsoluteAddressingToPcRelative;
        this.optimizeToAddqSubq = optimizeToAddqSubq;
        this.optimizeMoveToMoveq = optimizeMoveToMoveq;
        this.optimizeZeroDisplacement = optimizeZeroDisplacement;
    }

    /**
     * Gets a value indicating whether misaligned instructions or data should automatically be aligned.
     *
     * @return <code>true</code> to automatically align misaligned instructions or data, or <code>false</code> to leave it
     *         misaligned
     */
    public final boolean automaticEven() {
        return this.automaticEven;
    }

    /**
     * Gets a value indicating whether the <code>CMPI</code> instruction should be encoded as <code>TST</code> when the immediate
     * data is zero (<code>#0</code>).
     *
     * @return <code>true</code> to encode the <code>CMPI</code> instruction as <code>TST</code> when the immediate data is zero, or
     *         <code>false</code> to always encode the <code>CMPI</code> instruction as a <code>CMPI</code> instruction
     */
    public final boolean optimizeCmpiToTst() {
        return this.optimizeCmpiToTst;
    }

    /**
     * Gets a value indicating whether the <code>MOVE</code> instruction should be encoded as <code>MOVEQ</code> if possible.
     *
     * @return <code>true</code> to encode the <code>MOVE</code> instruction as <code>MOVEQ</code> if possible, or
     *         <code>false</code> to always encode the <code>MOVE</code> instruction as a <code>MOVE</code> instruction
     */
    public final boolean optimizeMoveToMoveq() {
        return this.optimizeMoveToMoveq;
    }

    /**
     * Gets a value indicating whether the <code>ADD</code>, <code>ADDA</code>, <code>ADDI</code>, <code>SUB</code>,
     * <code>SUBA</code> and <code>SUBI</code> instructions should be encoded as <code>ADDQ</code> and <code>SUBQ</code>
     * respectively if possible.
     *
     * @return <code>true</code> to encode <code>ADD</code>, <code>ADDA</code>, <code>ADDI</code>, <code>SUB</code>,
     *         <code>SUBA</code> or <code>SUBI</code> instructions as <code>ADDQ</code> or <code>SUBQ</code> if possible, or
     *         <code>false</code> to always encode such instructions using the normal encoding
     */
    public final boolean optimizeToAddqSubq() {
        return this.optimizeToAddqSubq;
    }

    /**
     * Gets a value indicating whether an effective address using unsized absolute addressing should be encoded using the program
     * counter indirect with displacement mode, if it is shorter.
     *
     * @return <code>true</code> to use the program counter indirect with displacement mode if the encoding is shorter, or
     *         <code>false</code> to use the appropriate absolute addressing mode
     */
    public final boolean optimizeUnsizedAbsoluteAddressingToPcRelative() {
        return this.optimizeUnsizedAbsoluteAddressingToPcRelative;
    }

    /**
     * Gets a value indicating whether unsized branches should use the shortest valid encoding.
     *
     * @return <code>true</code> to use the shortest valid encoding for unsized branches, or <code>false</code> to use the word-size
     *         encoding
     */
    public final boolean optimizeUnsizedBranches() {
        return this.optimizeUnsizedBranches;
    }

    /**
     * Gets a value indicating whether an effective address using the address register indirect with displacement mode with a base
     * displacement of zero should be encoded using the address register indirect mode instead.
     *
     * @return <code>true</code> to use the address register indirect mode, or <code>false</code> to use the address register
     *         indirect with displacement mode
     */
    public final boolean optimizeZeroDisplacement() {
        return this.optimizeZeroDisplacement;
    }

}
