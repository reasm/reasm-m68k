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
 * Test class for the {@link ConfigurationOptions#AUTOMATIC_EVEN} configuration option.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class AutomaticEvenTest extends BaseProgramsTest {

    private static final List<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // Instruction
        addDataItem(" DC.B $FF\n MOVE.B (A0),D1", 3, new byte[] { (byte) 0xFF, 0x00, 0x12, 0x10 });
        addDataItem(" DC.B $FF\n MOVE.W (A0),D1", 3, new byte[] { (byte) 0xFF, 0x00, 0x32, 0x10 });
        addDataItem(" DC.B $FF\n MOVE.L (A0),D1", 3, new byte[] { (byte) 0xFF, 0x00, 0x22, 0x10 });
        addDataItem(" DC.B $FF,$FF\n MOVE.W (A0),D1", 3, new byte[] { (byte) 0xFF, (byte) 0xFF, 0x32, 0x10 });

        // DC
        addDataItem(" DC.B $FF\n DC $1234", 3, new byte[] { (byte) 0xFF, 0x00, 0x12, 0x34 });
        addDataItem(" DC.B $FF\n DC.B $12", 3, new byte[] { (byte) 0xFF, 0x12 });
        addDataItem(" DC.B $FF\n DC.W $1234", 3, new byte[] { (byte) 0xFF, 0x00, 0x12, 0x34 });
        addDataItem(" DC.B $FF\n DC.L $12345678", 3, new byte[] { (byte) 0xFF, 0x00, 0x12, 0x34, 0x56, 0x78 });
        addDataItem(" DC.B $FF,$FF\n DC.W $1234", 3, new byte[] { (byte) 0xFF, (byte) 0xFF, 0x12, 0x34 });

        // DCB
        addDataItem(" DC.B $FF\n DCB 1,$1234", 3, new byte[] { (byte) 0xFF, 0x00, 0x12, 0x34 });
        addDataItem(" DC.B $FF\n DCB.B 1,$12", 3, new byte[] { (byte) 0xFF, 0x12 });
        addDataItem(" DC.B $FF\n DCB.W 1,$1234", 3, new byte[] { (byte) 0xFF, 0x00, 0x12, 0x34 });
        addDataItem(" DC.B $FF\n DCB.L 1,$12345678", 3, new byte[] { (byte) 0xFF, 0x00, 0x12, 0x34, 0x56, 0x78 });
        addDataItem(" DC.B $FF,$FF\n DCB.W 1,$1234", 3, new byte[] { (byte) 0xFF, (byte) 0xFF, 0x12, 0x34 });

        // DS
        addDataItem(" DC.B $FF\n DS 0", 3, new byte[] { (byte) 0xFF, 0x00 });
        addDataItem(" DC.B $FF\n DS 1", 3, new byte[] { (byte) 0xFF, 0x00, 0x00, 0x00 });
        addDataItem(" DC.B $FF\n DS.B 0", 3, new byte[] { (byte) 0xFF });
        addDataItem(" DC.B $FF\n DS.B 1", 3, new byte[] { (byte) 0xFF, 0x00 });
        addDataItem(" DC.B $FF\n DS.W 0", 3, new byte[] { (byte) 0xFF, 0x00 });
        addDataItem(" DC.B $FF\n DS.W 1", 3, new byte[] { (byte) 0xFF, 0x00, 0x00, 0x00 });
        addDataItem(" DC.B $FF\n DS.L 0", 3, new byte[] { (byte) 0xFF, 0x00 });
        addDataItem(" DC.B $FF\n DS.L 1", 3, new byte[] { (byte) 0xFF, 0x00, 0x00, 0x00, 0x00, 0x00 });
        addDataItem(" DC.B $FF,$FF\n DS.W 0", 3, new byte[] { (byte) 0xFF, (byte) 0xFF });
        addDataItem(" DC.B $FF,$FF\n DS.W 1", 3, new byte[] { (byte) 0xFF, (byte) 0xFF, 0x00, 0x00 });

        // RS
        addDataItem(" RSRESET 1\nA: RS 1\n DC.W A", 4, new byte[] { 0x00, 0x02 });
        addDataItem(" RSRESET 1\nA: RS.B 1\n DC.W A", 4, new byte[] { 0x00, 0x01 });
        addDataItem(" RSRESET 1\nA: RS.W 1\n DC.W A", 4, new byte[] { 0x00, 0x02 });
        addDataItem(" RSRESET 1\nA: RS.L 1\n DC.W A", 4, new byte[] { 0x00, 0x02 });
        addDataItem(" RSRESET 2\nA: RS.W 1\n DC.W A", 4, new byte[] { 0x00, 0x02 });
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

    private static void addDataItem(String code, int steps, byte[] output) {
        TEST_DATA.add(new Object[] { code, steps, output });
    }

    /**
     * Initializes a new AutomaticEvenTest.
     *
     * @param code
     *            assembly code to assemble
     * @param steps
     *            the number of steps the program is expected to take to assemble completely
     * @param output
     *            the program's output
     */
    public AutomaticEvenTest(String code, int steps, byte[] output) {
        super(code, steps, output, M68KArchitecture.MC68000, null, null, null);
    }

    @Override
    protected Map<String, Object> getM68KConfigurationOptions() {
        final HashMap<String, Object> m68kOptions = new HashMap<>();
        m68kOptions.put(ConfigurationOptions.AUTOMATIC_EVEN, true);
        return m68kOptions;
    }

}
