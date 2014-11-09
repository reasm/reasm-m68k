package org.reasm.m68k.assembly.internal;

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

    static final short[] NO_DATA = new short[0];
    static final AssemblyMessage ADDRESSING_MODE_NOT_ALLOWED_HERE = new AddressingModeNotAllowedHereErrorMessage();
    static final AssemblyMessage BREAKPOINT_NUMBER_OUT_OF_RANGE = new BreakpointNumberOutOfRangeErrorMessage();
    static final AssemblyMessage BRANCH_TARGET_OUT_OF_RANGE = new BranchTargetOutOfRangeErrorMessage();
    static final AssemblyMessage LABEL_EXPECTED = new LabelExpectedErrorMessage();
    static final AssemblyMessage TRAP_VECTOR_OUT_OF_RANGE = new TrapVectorOutOfRangeErrorMessage();

    private static byte[] wordsToBytes(short[] words) {
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
    public BaseInstructionsTest(String code, short[] output, M68KArchitecture architecture, AssemblyMessage expectedMessage,
            AssemblyMessage[] expectedMessages) {
        super(code, 2, wordsToBytes(output), architecture, expectedMessage, expectedMessages, null);
    }

}
