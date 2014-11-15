package org.reasm.m68k.assembly.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.m68k.ConfigurationOptions;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.InvalidExpressionErrorMessage;
import org.reasm.m68k.messages.MinusOneDistanceShortBranchErrorMessage;
import org.reasm.m68k.messages.ZeroDistanceShortBranchErrorMessage;

/**
 * Test class for the {@link ConfigurationOptions#OPTIMIZE_UNSIZED_BRANCHES} configuration option.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class OptimizeUnsizedBranchesTest extends BaseInstructionsTest {

    @Nonnull
    private static final List<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // BRA
        addDataItem(" BRA", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BRA UNDEFINED", new short[] { 0x60FE }, UNDEFINED_SYMBOL);
        addDataItem(" BRA 0", new short[] { 0x60FE });
        addDataItem(" BRA 0-", new short[] { 0x60FE }, new InvalidExpressionErrorMessage("0-"));
        addDataItem(" BRA 0a", new short[] { 0x60FE }, new InvalidExpressionErrorMessage("0a"));
        addDataItem(" BRA 1", new short[] { 0x6000, -1 });
        addDataItem(" BRA 1", new short[] { 0x6000, -1 }, M68KArchitecture.MC68020);
        addDataItem(" BRA 2", new short[] { 0x6000, 0x0000 });
        addDataItem(" BRA $8000", new short[] { 0x6000, 0x7FFE });
        addDataItem(" BRA $8002", new short[] { 0x6000, -0x8000 }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA $8002", new short[] { 0x60FF, 0, -0x8000 }, M68KArchitecture.MC68020);
        addDataItem(" BRA -$8000", new short[] { 0x6000, 0x7FFE }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA -$8000", new short[] { 0x60FF, -1, 0x7FFE }, M68KArchitecture.MC68020);
        addDataItem(" BRA -$7FFE", new short[] { 0x6000, -0x8000 });
        addDataItem(" BRA -$7FFC", new short[] { 0x6000, -0x7FFE });
        addDataItem(" BRA $80000002", new short[] { 0x60FF, -0x8000, 0 }, M68KArchitecture.MC68020, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA -$80000000", new short[] { 0x60FF, 0x7FFF, -2 }, M68KArchitecture.MC68020, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem("HANG: BRA HANG", new short[] { 0x60FE });
        addDataItem(" BRA 0.5", new short[] { 0x60FE }, LABEL_EXPECTED);
        addDataItem(" BRA 'ab'", new short[] { 0x60FE }, LABEL_EXPECTED);
        // TODO: test with a built-in function symbol
        //addDataItem(" BRA STRLEN", new short[] { 0x60FE }, LABEL_EXPECTED);
        addDataItem(" BRA. 0", new short[] { 0x60FE }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" BRA.B 0", new short[] { 0x60FE });
        addDataItem(" BRA.B 1", new short[] { 0x60FF });
        addDataItem(" BRA.B 1", new short[] { 0x60FE }, M68KArchitecture.MC68020, new MinusOneDistanceShortBranchErrorMessage());
        addDataItem(" BRA.B 2", new short[] { 0x60FE }, new ZeroDistanceShortBranchErrorMessage());
        addDataItem(" BRA.B 128", new short[] { 0x607E });
        addDataItem(" BRA.B 130", new short[] { 0x6080 }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA.B -128", new short[] { 0x607E }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA.B -126", new short[] { 0x6080 });
        addDataItem(" BRA.S 0", new short[] { 0x60FE });
        addDataItem(" BRA.W 0", new short[] { 0x6000, -2 });
        addDataItem(" BRA.W 1", new short[] { 0x6000, -1 });
        addDataItem(" BRA.W 1", new short[] { 0x6000, -1 }, M68KArchitecture.MC68020);
        addDataItem(" BRA.W 2", new short[] { 0x6000, 0 });
        addDataItem(" BRA.W $8000", new short[] { 0x6000, 0x7FFE });
        addDataItem(" BRA.W $8002", new short[] { 0x6000, -0x8000 }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA.W -$8000", new short[] { 0x6000, 0x7FFE }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA.W -$7FFE", new short[] { 0x6000, -0x8000 });
        addDataItem(" BRA.L 0", new short[] { 0x60FF, -1, -2 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" BRA.L 0", new short[] { 0x60FF, -1, -2 }, M68KArchitecture.MC68020);
        addDataItem(" BRA.L 1", new short[] { 0x60FF, -1, -1 }, M68KArchitecture.MC68020);
        addDataItem(" BRA.L 2", new short[] { 0x60FF, 0, 0 }, M68KArchitecture.MC68020);
        addDataItem(" BRA.L $80000000", new short[] { 0x60FF, 0x7FFF, -2 }, M68KArchitecture.MC68020);
        addDataItem(" BRA.L $80000002", new short[] { 0x60FF, -0x8000, 0 }, M68KArchitecture.MC68020, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA.L -$80000000", new short[] { 0x60FF, 0x7FFF, -2 }, M68KArchitecture.MC68020, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA.L -$7FFFFFFE", new short[] { 0x60FF, -0x8000, 0 }, M68KArchitecture.MC68020);
        addDataItem(" BRA.Z 0", new short[] { 0x60FE }, INVALID_SIZE_ATTRIBUTE_Z);
        // --> see also InstructionsTest for tests with the "optimize unsized branches" option disabled
    }

    /**
     * Gets the test data for this parameterized test.
     *
     * @return the test data
     */
    @Nonnull
    @Parameters
    public static List<Object[]> data() {
        return TEST_DATA;
    }

    private static void addDataItem(@Nonnull String code, @Nonnull short[] output) {
        addDataItem(code, output, M68KArchitecture.MC68000, (AssemblyMessage) null);
    }

    private static void addDataItem(@Nonnull String code, @Nonnull short[] output, @CheckForNull AssemblyMessage expectedMessage) {
        addDataItem(code, output, M68KArchitecture.MC68000, expectedMessage);
    }

    private static void addDataItem(@Nonnull String code, @Nonnull short[] output, @Nonnull M68KArchitecture architecture) {
        addDataItem(code, output, architecture, (AssemblyMessage) null);
    }

    private static void addDataItem(@Nonnull String code, @Nonnull short[] output, @Nonnull M68KArchitecture architecture,
            @CheckForNull AssemblyMessage expectedMessage) {
        TEST_DATA.add(new Object[] { code, output, architecture, expectedMessage });
    }

    /**
     * Initializes a new OptimizeUnsizedBranchesTest.
     *
     * @param code
     *            a line of code containing an instruction
     * @param output
     *            the generated opcode for the instruction
     * @param architecture
     *            the target architecture
     * @param expectedMessage
     *            an {@link AssemblyMessage} that is expected to be generated while assembling the line of code
     */
    public OptimizeUnsizedBranchesTest(@Nonnull String code, @Nonnull short[] output, @Nonnull M68KArchitecture architecture,
            @CheckForNull AssemblyMessage expectedMessage) {
        super(code, output, architecture, expectedMessage, null);
    }

    @Nonnull
    @Override
    protected Map<String, Object> getM68KConfigurationOptions() {
        final HashMap<String, Object> m68kOptions = new HashMap<>();
        m68kOptions.put(ConfigurationOptions.OPTIMIZE_UNSIZED_BRANCHES, true);
        return m68kOptions;
    }

}
