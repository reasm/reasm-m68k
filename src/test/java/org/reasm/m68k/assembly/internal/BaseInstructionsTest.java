package org.reasm.m68k.assembly.internal;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.AssemblyMessage;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.AddressingModeNotAllowedHereErrorMessage;
import org.reasm.m68k.messages.BranchTargetOutOfRangeErrorMessage;
import org.reasm.m68k.messages.BreakpointNumberOutOfRangeErrorMessage;
import org.reasm.m68k.messages.LabelExpectedErrorMessage;
import org.reasm.m68k.messages.TrapVectorOutOfRangeErrorMessage;

/**
 * Base test class for individual M68000 family instructions.
 *
 * @author Francis Gagné
 */
public abstract class BaseInstructionsTest extends BaseProgramsTest {

    @Nonnull
    static final short[] NO_DATA = new short[0];
    @Nonnull
    static final AssemblyMessage ADDRESSING_MODE_NOT_ALLOWED_HERE = new AddressingModeNotAllowedHereErrorMessage();
    @Nonnull
    static final AssemblyMessage BREAKPOINT_NUMBER_OUT_OF_RANGE = new BreakpointNumberOutOfRangeErrorMessage();
    @Nonnull
    static final AssemblyMessage BRANCH_TARGET_OUT_OF_RANGE = new BranchTargetOutOfRangeErrorMessage();
    @Nonnull
    static final AssemblyMessage LABEL_EXPECTED = new LabelExpectedErrorMessage();
    @Nonnull
    static final AssemblyMessage TRAP_VECTOR_OUT_OF_RANGE = new TrapVectorOutOfRangeErrorMessage();

    @Nonnull
    private static byte[] wordsToBytes(@Nonnull short[] words) {
        final byte[] bytes = new byte[words.length * 2];

        for (int i = 0; i < words.length; i++) {
            bytes[i * 2 + 0] = (byte) (words[i] >>> 8);
            bytes[i * 2 + 1] = (byte) (words[i] >>> 0);
        }

        return bytes;
    }

    /**
     * Initializes a new BaseInstructionsTest.
     *
     * @param code
     *            a line of code containing an instruction
     * @param output
     *            the generated opcode for the instruction
     * @param architecture
     *            the target architecture
     * @param expectedMessage
     *            an {@link AssemblyMessage} that is expected to be generated while assembling the line of code
     * @param expectedMessages
     *            an array of {@link AssemblyMessage} that are expected to be generated while assembling the line of code. Takes
     *            priority over <code>expectedMessage</code>.
     */
    public BaseInstructionsTest(@Nonnull String code, @Nonnull short[] output, @Nonnull M68KArchitecture architecture,
            @CheckForNull AssemblyMessage expectedMessage, @CheckForNull AssemblyMessage[] expectedMessages) {
        super(code, 2, wordsToBytes(output), architecture, expectedMessage, expectedMessages, null);
    }

}
