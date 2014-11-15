package org.reasm.m68k.assembly.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.m68k.ConfigurationOptions;
import org.reasm.m68k.M68KArchitecture;

/**
 * Test class for the {@link ConfigurationOptions#OPTIMIZE_MOVE_TO_MOVEQ} configuration option.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class OptimizeMoveToMoveqTest extends BaseInstructionsTest {

    @Nonnull
    private static final List<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // MOVE
        addDataItem(" MOVE.B #0,D0", new short[] { 0x103C, 0x0000 });
        addDataItem(" MOVE.W #0,D0", new short[] { 0x303C, 0x0000 });
        addDataItem(" MOVE.L #0,D0", new short[] { 0x7000 });
        addDataItem(" MOVE.L #$7F,D0", new short[] { 0x707F });
        addDataItem(" MOVE.L #-$80,D0", new short[] { 0x7080 });
        addDataItem(" MOVE.L #$FFFFFF80,D0", new short[] { 0x7080 });
        addDataItem(" MOVE.L #$80,D0", new short[] { 0x203C, 0x0000, 0x0080 });
        addDataItem(" MOVE.L #-$81,D0", new short[] { 0x203C, -1, -0x0081 });
        addDataItem(" MOVE.L #$FFFFFF7F,D0", new short[] { 0x203C, -1, -0x0081 });
        addDataItem(" MOVE.L #0,(A0)", new short[] { 0x20BC, 0x0000, 0x0000 });
        addDataItem(" MOVE.L 0.W,D0", new short[] { 0x2038, 0x0000 });
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
        TEST_DATA.add(new Object[] { code, output });
    }

    /**
     * Initializes a new OptimizeMoveToMoveqTest.
     *
     * @param code
     *            a line of code containing an instruction
     * @param output
     *            the generated opcode for the instruction
     */
    public OptimizeMoveToMoveqTest(@Nonnull String code, @Nonnull short[] output) {
        super(code, output, M68KArchitecture.MC68000, null, null);
    }

    @Nonnull
    @Override
    protected Map<String, Object> getM68KConfigurationOptions() {
        final HashMap<String, Object> m68kOptions = new HashMap<>();
        m68kOptions.put(ConfigurationOptions.OPTIMIZE_MOVE_TO_MOVEQ, true);
        return m68kOptions;
    }

}
