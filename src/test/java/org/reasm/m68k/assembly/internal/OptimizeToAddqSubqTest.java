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
 * Test class for the {@link ConfigurationOptions#OPTIMIZE_TO_ADDQ_SUBQ} configuration option.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class OptimizeToAddqSubqTest extends BaseInstructionsTest {

    @Nonnull
    private static final List<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // ADD
        addDataItem(" ADD #0,D0", new short[] { (short) 0xD07C, 0x0000 });
        addDataItem(" ADD #1,D0", new short[] { 0x5240 });
        addDataItem(" ADD #2,D0", new short[] { 0x5440 });
        addDataItem(" ADD #3,D0", new short[] { 0x5640 });
        addDataItem(" ADD #4,D0", new short[] { 0x5840 });
        addDataItem(" ADD #5,D0", new short[] { 0x5A40 });
        addDataItem(" ADD #6,D0", new short[] { 0x5C40 });
        addDataItem(" ADD #7,D0", new short[] { 0x5E40 });
        addDataItem(" ADD #8,D0", new short[] { 0x5040 });
        addDataItem(" ADD #9,D0", new short[] { (short) 0xD07C, 0x0009 });
        addDataItem(" ADD #$1234,D0", new short[] { (short) 0xD07C, 0x1234 });
        addDataItem(" ADD #0,A0", new short[] { (short) 0xD0FC, 0x0000 });
        addDataItem(" ADD #2,A0", new short[] { 0x5448 });
        addDataItem(" ADD #$1234,A0", new short[] { (short) 0xD0FC, 0x1234 });
        addDataItem(" ADD #0,(A0)", new short[] { 0x0650, 0x0000 });
        addDataItem(" ADD #2,(A0)", new short[] { 0x5450 });
        addDataItem(" ADD #$1234,(A0)", new short[] { 0x0650, 0x1234 });

        // ADDA
        addDataItem(" ADDA #0,A0", new short[] { (short) 0xD0FC, 0x0000 });
        addDataItem(" ADDA #2,A0", new short[] { 0x5448 });
        addDataItem(" ADDA #$1234,A0", new short[] { (short) 0xD0FC, 0x1234 });

        // ADDI
        addDataItem(" ADDI #0,D0", new short[] { 0x0640, 0x0000 });
        addDataItem(" ADDI #1,D0", new short[] { 0x5240 });
        addDataItem(" ADDI #2,D0", new short[] { 0x5440 });
        addDataItem(" ADDI #3,D0", new short[] { 0x5640 });
        addDataItem(" ADDI #4,D0", new short[] { 0x5840 });
        addDataItem(" ADDI #5,D0", new short[] { 0x5A40 });
        addDataItem(" ADDI #6,D0", new short[] { 0x5C40 });
        addDataItem(" ADDI #7,D0", new short[] { 0x5E40 });
        addDataItem(" ADDI #8,D0", new short[] { 0x5040 });
        addDataItem(" ADDI #9,D0", new short[] { 0x0640, 0x0009 });
        addDataItem(" ADDI #$1234,D0", new short[] { 0x0640, 0x1234 });
        addDataItem(" ADDI #0,A0", new short[] { (short) 0xD0FC, 0x0000 });
        addDataItem(" ADDI #2,A0", new short[] { 0x5448 });
        addDataItem(" ADDI #$1234,A0", new short[] { (short) 0xD0FC, 0x1234 });
        addDataItem(" ADDI #0,(A0)", new short[] { 0x0650, 0x0000 });
        addDataItem(" ADDI #2,(A0)", new short[] { 0x5450 });
        addDataItem(" ADDI #$1234,(A0)", new short[] { 0x0650, 0x1234 });

        // CMPA
        addDataItem(" CMPA #2,A0", new short[] { (short) 0xB0FC, 0x0002 });

        // CMPI
        addDataItem(" CMPI #2,D0", new short[] { 0x0C40, 0x0002 });

        // SUB
        addDataItem(" SUB #2,D0", new short[] { 0x5540 });
        addDataItem(" SUB #2,A0", new short[] { 0x5548 });
        // --> see ADD for more tests

        // SUBA
        addDataItem(" SUBA #2,A0", new short[] { 0x5548 });
        // --> see ADDA for more tests

        // SUBI
        addDataItem(" SUBI #2,D0", new short[] { 0x5540 });
        addDataItem(" SUBI #2,A0", new short[] { 0x5548 });
        // --> see ADDI for more tests
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
     * Initializes a new OptimizeToAddqSubqTest.
     *
     * @param code
     *            a line of code containing an instruction
     * @param output
     *            the generated opcode for the instruction
     */
    public OptimizeToAddqSubqTest(@Nonnull String code, @Nonnull short[] output) {
        super(code, output, M68KArchitecture.MC68000, null, null);
    }

    @Nonnull
    @Override
    protected Map<String, Object> getM68KConfigurationOptions() {
        final HashMap<String, Object> m68kOptions = new HashMap<>();
        m68kOptions.put(ConfigurationOptions.OPTIMIZE_TO_ADDQ_SUBQ, true);
        return m68kOptions;
    }

}
