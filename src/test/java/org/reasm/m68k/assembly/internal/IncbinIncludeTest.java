package org.reasm.m68k.assembly.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.Environment;
import org.reasm.FileFetcher;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.IncbinLengthMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.IncbinStartMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.NotSupportedOnArchitectureErrorMessage;
import org.reasm.m68k.messages.ValueOutOfRangeErrorMessage;
import org.reasm.messages.ArchitectureNotRegisteredErrorMessage;
import org.reasm.messages.IOErrorMessage;
import org.reasm.source.SourceFile;

/**
 * Test class for the <code>BINCLUDE</code>, <code>INCBIN</code> and <code>INCLUDE</code> directives.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class IncbinIncludeTest extends BaseProgramsTest {

    private static final IOErrorMessage FILE_A_NOT_FOUND = new IOErrorMessage(new FileNotFoundException("A"));
    static final byte[] FILE_B = new byte[] { 1, 2, 3, 4 };
    static final SourceFile FILE_C = new SourceFile(" DC.B $77", "C");
    private static final byte[] FILE_C_OUTPUT = new byte[] { 0x77 };
    static final SourceFile FILE_D = new SourceFile(" LPSTOP #$1234", "D");
    private static final byte[] FILE_D_OUTPUT = new byte[] { (byte) 0xF8, 0x00, 0x01, (byte) 0xC0, 0x12, 0x34 };

    private static final FileFetcher FILE_FETCHER = new FileFetcher() {
        @Override
        public byte[] fetchBinaryFile(String filePath) throws IOException {
            if ("B".equals(filePath)) {
                return FILE_B.clone();
            }

            return this.getNull();
        }

        @Override
        public SourceFile fetchSourceFile(String filePath) throws IOException {
            if ("C".equals(filePath)) {
                return FILE_C;
            }

            if ("D".equals(filePath)) {
                return FILE_D;
            }

            return this.getNull();
        }

        // method to bypass FindBugs's null analysis
        private <T> T getNull() {
            return null;
        }
    };

    private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // BINCLUDE
        addDataItem(" BINCLUDE 'B'", 2, FILE_B);
        addDataItem(" BINCLUDE 'B',1", 2, new byte[] { 2, 3, 4 });
        addDataItem(" BINCLUDE 'B',0,4", 2, FILE_B);
        // --> see INCBIN for more tests

        // INCBIN
        addDataItem(" INCBIN", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" INCBIN 'A'", 2, NO_DATA, FILE_A_NOT_FOUND);
        addDataItem(" INCBIN 'B'", 2, FILE_B);
        addDataItem(" INCBIN 'B',0", 2, FILE_B);
        addDataItem(" INCBIN 'B',1", 2, new byte[] { 2, 3, 4 });
        addDataItem(" INCBIN 'B',4", 2, NO_DATA);
        addDataItem(" INCBIN 'B',5", 2, NO_DATA, new ValueOutOfRangeErrorMessage(5));
        addDataItem(" INCBIN 'B',0,0", 2, NO_DATA);
        addDataItem(" INCBIN 'B',0,4", 2, FILE_B);
        addDataItem(" INCBIN 'B',0,-1", 2, FILE_B, new IncbinLengthMustNotBeNegativeErrorMessage());
        addDataItem(" INCBIN 'B',0,5", 2, FILE_B, new ValueOutOfRangeErrorMessage(5));
        addDataItem(" INCBIN 'B',-1,4", 2, FILE_B, new IncbinStartMustNotBeNegativeErrorMessage());
        addDataItem(" INCBIN 'B',1,3", 2, new byte[] { 2, 3, 4 });
        addDataItem(" INCBIN 'B',1,4", 2, new byte[] { 2, 3, 4 }, new ValueOutOfRangeErrorMessage(4));
        addDataItem(" INCBIN 'B',0,4,1", 2, FILE_B, WRONG_NUMBER_OF_OPERANDS);

        // INCLUDE
        addDataItem(" INCLUDE", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" INCLUDE 'A'", 2, NO_DATA, FILE_A_NOT_FOUND);
        addDataItem(" INCLUDE 'C'", 4, FILE_C_OUTPUT);
        addDataItem(" INCLUDE 'C',UNREGISTERED", 4, FILE_C_OUTPUT, new ArchitectureNotRegisteredErrorMessage("UNREGISTERED"));
        addDataItem(" INCLUDE 'D'", 4, FILE_D_OUTPUT, new NotSupportedOnArchitectureErrorMessage());
        addDataItem(" INCLUDE 'D',CPU32", 4, FILE_D_OUTPUT);
        addDataItem(" INCLUDE 'D',CPU32,1", 4, FILE_D_OUTPUT, WRONG_NUMBER_OF_OPERANDS);
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
        addDataItem(code, steps, output, (AssemblyMessage) null);
    }

    private static void addDataItem(String code, int steps, byte[] output, AssemblyMessage expectedMessage) {
        TEST_DATA.add(new Object[] { code, steps, output, expectedMessage });
    }

    /**
     * Initializes a new IncbinIncludeTest.
     *
     * @param code
     *            assembly code to assemble
     * @param steps
     *            the number of steps the program is expected to take to assemble completely
     * @param output
     *            the program's output
     * @param expectedMessage
     *            an {@link AssemblyMessage} that is expected to be generated while assembling the code
     */
    public IncbinIncludeTest(String code, int steps, byte[] output, AssemblyMessage expectedMessage) {
        super(code, steps, output, M68KArchitecture.MC68000, expectedMessage, null, null);
    }

    @Override
    protected Environment getEnvironment() {
        return super.getEnvironment().addArchitecture(M68KArchitecture.CPU32);
    }

    @Override
    protected FileFetcher getFileFetcher() {
        return FILE_FETCHER;
    }

}
