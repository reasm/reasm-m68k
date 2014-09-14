package org.reasm.m68k.assembly.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.m68k.ConfigurationOptions;
import org.reasm.m68k.M68KArchitecture;

/**
 * Test class for the {@link ConfigurationOptions#OPTIMIZE_CMPI_TO_TST} configuration option.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class OptimizeCmpiToTstTest extends BaseInstructionsTest {

    private static final List<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // CMP
        addDataItem(" CMP D0,D0", new short[] { (short) 0xB040 });
        addDataItem(" CMP #0,D0", new short[] { 0x4A40 });
        addDataItem(" CMP #$1234,D0", new short[] { (short) 0xB07C, 0x1234 });
        addDataItem(" CMP #0,A0", new short[] { (short) 0xB0FC, 0x0000 });
        addDataItem(" CMP #0,A0", new short[] { 0x4A48 }, M68KArchitecture.CPU32);
        addDataItem(" CMP #0,A0", new short[] { 0x4A48 }, M68KArchitecture.MC68020);
        addDataItem(" CMP #$1234,A0", new short[] { (short) 0xB0FC, 0x1234 });
        addDataItem(" CMP #0,(A0)", new short[] { 0x4A50 });
        addDataItem(" CMP #$1234,(A0)", new short[] { 0x0C50, 0x1234 });
        addDataItem(" CMP.B #0,D0", new short[] { 0x4A00 });
        addDataItem(" CMP.W #0,D0", new short[] { 0x4A40 });
        addDataItem(" CMP.L #0,D0", new short[] { 0x4A80 });

        // CMPA
        addDataItem(" CMPA D0,A0", new short[] { (short) 0xB0C0 }, M68KArchitecture.MC68020);
        addDataItem(" CMPA #0,A0", new short[] { (short) 0xB0FC, 0x0000 });
        addDataItem(" CMPA #0,A0", new short[] { 0x4A48 }, M68KArchitecture.CPU32);
        addDataItem(" CMPA #0,A0", new short[] { 0x4A48 }, M68KArchitecture.MC68020);

        // CMPI
        addDataItem(" CMPI #0,D0", new short[] { 0x4A40 });
        addDataItem(" CMPI #$1234,D0", new short[] { 0x0C40, 0x1234 });
        addDataItem(" CMPI #0,A0", new short[] { (short) 0xB0FC, 0x0000 });
        addDataItem(" CMPI #0,A0", new short[] { 0x4A48 }, M68KArchitecture.CPU32);
        addDataItem(" CMPI #0,A0", new short[] { 0x4A48 }, M68KArchitecture.MC68020);
        addDataItem(" CMPI #$1234,A0", new short[] { (short) 0xB0FC, 0x1234 });
    }

    /**
     * Gets the test data for this parameterized test.
     *
     * @return the test data
     */
    @Parameters
    public static List<Object[]> data() {
        return TEST_DATA;
    }

    private static void addDataItem(String code, short[] output) {
        addDataItem(code, output, M68KArchitecture.MC68000);
    }

    private static void addDataItem(String code, short[] output, M68KArchitecture architecture) {
        TEST_DATA.add(new Object[] { code, output, architecture });
    }

    /**
     * Initializes a new OptimizeCmpiToTstTest.
     *
     * @param code
     *            a line of code containing an instruction
     * @param output
     *            the generated opcode for the instruction
     * @param architecture
     *            the target architecture
     */
    public OptimizeCmpiToTstTest(String code, short[] output, M68KArchitecture architecture) {
        super(code, output, architecture, null, null);
    }

    @Override
    protected Map<String, Object> getM68KConfigurationOptions() {
        final HashMap<String, Object> m68kOptions = new HashMap<>();
        m68kOptions.put(ConfigurationOptions.OPTIMIZE_CMPI_TO_TST, true);
        return m68kOptions;
    }

}
