package org.reasm.m68k.assembly.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.CountMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.InvalidCharacterInHexDirectiveErrorMessage;
import org.reasm.m68k.messages.InvalidExpressionErrorMessage;
import org.reasm.m68k.messages.StringTooLongErrorMessage;
import org.reasm.m68k.messages.ValueOutOfRangeErrorMessage;
import org.reasm.messages.OddNumberOfCharactersInHexDirectiveErrorMessage;
import org.reasm.messages.OutOfMemoryErrorMessage;

/**
 * Test class for directives whose main purpose is outputting data (e.g. <code>DC</code>).
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class OutputDirectivesTest extends BaseProgramsTest {

    private static final List<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // DC
        addDataItem(" DC", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DC ~", new byte[] { 0, 0 }, new InvalidExpressionErrorMessage("~"));
        addDataItem(" DC UNDEFINED", new byte[] { 0, 0 }, UNDEFINED_SYMBOL);
        addDataItem(" DC 0", new byte[] { 0, 0 });
        addDataItem(" DC 0.0", new byte[] { 0, 0 });
        addDataItem(" DC ''", new byte[] { 0, 0 });
        addDataItem(" DC 'A'", new byte[] { 0, 0x41 });
        addDataItem(" DC 'AB'", new byte[] { 0x41, 0x42 });
        addDataItem(" DC 'ABC'", new byte[] { 0x41, 0x42 }, new StringTooLongErrorMessage("ABC"));
        // TODO: test with a built-in function symbol
        //addDataItem(" DC STRLEN", new byte[] { 0, 0 }, new FunctionCannotBeConvertedToRealErrorMessage());
        addDataItem(" DC. 0", new byte[] { 0, 0 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" DC.B 0", new byte[] { 0 });
        addDataItem(" DC.B $FF", new byte[] { -1 });
        addDataItem(" DC.B $100", new byte[] { 0 }, new ValueOutOfRangeErrorMessage(0x100));
        addDataItem(" DC.B $FFFFFFFFFFFFFFFF", new byte[] { -1 }, new ValueOutOfRangeErrorMessage(-1));
        addDataItem(" DC.B -$81", new byte[] { 0x7F }, new ValueOutOfRangeErrorMessage(-0x81));
        addDataItem(" DC.B -$80", new byte[] { -0x80 });
        addDataItem(" DC.B +$7F", new byte[] { 0x7F });
        addDataItem(" DC.B +$80", new byte[] { -0x80 }, new ValueOutOfRangeErrorMessage(0x80));
        addDataItem(" DC.B 'this string can be arbitrarily long'", new byte[] { 0x74, 0x68, 0x69, 0x73, 0x20, 0x73, 0x74, 0x72,
                0x69, 0x6E, 0x67, 0x20, 0x63, 0x61, 0x6E, 0x20, 0x62, 0x65, 0x20, 0x61, 0x72, 0x62, 0x69, 0x74, 0x72, 0x61, 0x72,
                0x69, 0x6C, 0x79, 0x20, 0x6C, 0x6F, 0x6E, 0x67 });
        addDataItem(" DC.B 0, 1, 2, 3", new byte[] { 0, 1, 2, 3 });
        addDataItem(" DC.B 'foo', 7", new byte[] { 0x66, 0x6F, 0x6F, 7 });
        addDataItem(" DC.W 0", new byte[] { 0, 0 });
        addDataItem(" DC.W $FF", new byte[] { 0, -1 });
        addDataItem(" DC.W $FF00", new byte[] { -1, 0 });
        addDataItem(" DC.W $FFFF", new byte[] { -1, -1 });
        addDataItem(" DC.W $10000", new byte[] { 0, 0 }, new ValueOutOfRangeErrorMessage(0x10000));
        addDataItem(" DC.W $FFFFFFFFFFFFFFFF", new byte[] { -1, -1 }, new ValueOutOfRangeErrorMessage(-1));
        addDataItem(" DC.W -$8001", new byte[] { 0x7F, -1 }, new ValueOutOfRangeErrorMessage(-0x8001));
        addDataItem(" DC.W -$8000", new byte[] { -0x80, 0 });
        addDataItem(" DC.W +$7FFF", new byte[] { 0x7F, -1 });
        addDataItem(" DC.W +$8000", new byte[] { -0x80, 0 }, new ValueOutOfRangeErrorMessage(0x8000));
        addDataItem(" DC.W 'AB'", new byte[] { 0x41, 0x42 });
        addDataItem(" DC.W 0, 1, 2, 3", new byte[] { 0, 0, 0, 1, 0, 2, 0, 3 });
        addDataItem(" DC.L 0", new byte[] { 0, 0, 0, 0 });
        addDataItem(" DC.L $FF", new byte[] { 0, 0, 0, -1 });
        addDataItem(" DC.L $FF00", new byte[] { 0, 0, -1, 0 });
        addDataItem(" DC.L $FF0000", new byte[] { 0, -1, 0, 0 });
        addDataItem(" DC.L $FF000000", new byte[] { -1, 0, 0, 0 });
        addDataItem(" DC.L $FFFFFFFF", new byte[] { -1, -1, -1, -1 });
        addDataItem(" DC.L $100000000", new byte[] { 0, 0, 0, 0 }, new ValueOutOfRangeErrorMessage(0x100000000L));
        addDataItem(" DC.L $FFFFFFFFFFFFFFFF", new byte[] { -1, -1, -1, -1 }, new ValueOutOfRangeErrorMessage(-1));
        addDataItem(" DC.L -$80000001", new byte[] { 0x7F, -1, -1, -1 }, new ValueOutOfRangeErrorMessage(-0x80000001L));
        addDataItem(" DC.L -$80000000", new byte[] { -0x80, 0, 0, 0 });
        addDataItem(" DC.L +$7FFFFFFF", new byte[] { 0x7F, -1, -1, -1 });
        addDataItem(" DC.L +$80000000", new byte[] { -0x80, 0, 0, 0 }, new ValueOutOfRangeErrorMessage(0x80000000L));
        addDataItem(" DC.L 'ABCD'", new byte[] { 0x41, 0x42, 0x43, 0x44 });
        addDataItem(" DC.L 0, 1, 2, 3", new byte[] { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3 });
        addDataItem(" DC.Q 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DC.Q $FF", new byte[] { 0, 0, 0, 0, 0, 0, 0, -1 });
        addDataItem(" DC.Q $FF00", new byte[] { 0, 0, 0, 0, 0, 0, -1, 0 });
        addDataItem(" DC.Q $FF0000", new byte[] { 0, 0, 0, 0, 0, -1, 0, 0 });
        addDataItem(" DC.Q $FF000000", new byte[] { 0, 0, 0, 0, -1, 0, 0, 0 });
        addDataItem(" DC.Q $FF00000000", new byte[] { 0, 0, 0, -1, 0, 0, 0, 0 });
        addDataItem(" DC.Q $FF0000000000", new byte[] { 0, 0, -1, 0, 0, 0, 0, 0 });
        addDataItem(" DC.Q $FF000000000000", new byte[] { 0, -1, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DC.Q $FF00000000000000", new byte[] { -1, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DC.Q $FFFFFFFFFFFFFFFF", new byte[] { -1, -1, -1, -1, -1, -1, -1, -1 });
        addDataItem(" DC.Q -$8000000000000001", new byte[] { 0x7F, -1, -1, -1, -1, -1, -1, -1 });
        addDataItem(" DC.Q -$8000000000000000", new byte[] { -0x80, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DC.Q +$7FFFFFFFFFFFFFFF", new byte[] { 0x7F, -1, -1, -1, -1, -1, -1, -1 });
        addDataItem(" DC.Q +$8000000000000000", new byte[] { -0x80, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DC.Q 'ABCDEFGH'", new byte[] { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48 });
        addDataItem(" DC.Q 0, 1, 2, 3", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0,
                0, 0, 0, 0, 0, 3 });
        addDataItem(" DC.S UNDEFINED", new byte[] { 0, 0, 0, 0 }, UNDEFINED_SYMBOL);
        addDataItem(" DC.S 0", new byte[] { 0, 0, 0, 0 });
        addDataItem(" DC.S $F000000000000000", new byte[] { 0x5F, 0x70, 0x00, 0x00 });
        addDataItem(" DC.S -$1000000000000000", new byte[] { (byte) 0xDD, (byte) 0x80, 0x00, 0x00 });
        addDataItem(" DC.S 0.0", new byte[] { 0, 0, 0, 0 });
        addDataItem(" DC.S 0.03125", new byte[] { 0x3D, 0x00, 0x00, 0x00 });
        addDataItem(" DC.S '0.03125'", new byte[] { 0x3D, 0x00, 0x00, 0x00 });
        // TODO: test with a built-in function symbol
        //addDataItem(" DC.S STRLEN", new byte[] { 0, 0, 0, 0 }, new FunctionCannotBeConvertedToRealErrorMessage());
        addDataItem(" DC.S 0, 1, 2, 3", new byte[] { 0, 0, 0, 0, 0x3F, (byte) 0x80, 0, 0, 0x40, 0, 0, 0, 0x40, 0x40, 0, 0 });
        addDataItem(" DC.D 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DC.D $F000000000000000", new byte[] { 0x43, (byte) 0xEE, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
        addDataItem(" DC.D -$1000000000000000", new byte[] { (byte) 0xC3, (byte) 0xB0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
        addDataItem(" DC.D 0.03125", new byte[] { 0x3F, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
        addDataItem(" DC.D '0.03125'", new byte[] { 0x3F, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
        // TODO: test with a built-in function symbol
        //addDataItem(" DC.D STRLEN", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new FunctionCannotBeConvertedToRealErrorMessage());
        addDataItem(" DC.D 0, 1, 2, 3", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0x3F, (byte) 0xF0, 0, 0, 0, 0, 0, 0, 0x40, 0, 0, 0, 0,
                0, 0, 0, 0x40, 8, 0, 0, 0, 0, 0, 0 });
        // TODO: implement DC.X
        //addDataItem(" DC.X 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
        // TODO: implement DC.P
        //addDataItem(" DC.P 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DC.Z 0", new byte[] { 0, 0 }, INVALID_SIZE_ATTRIBUTE_Z);

        // DCB
        addDataItem(" DCB", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DCB 4", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DCB 4, ~", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, new InvalidExpressionErrorMessage("~"));
        addDataItem(" DCB 4, UNDEFINED", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }, UNDEFINED_SYMBOL);
        addDataItem(" DCB 4, 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DCB 4, $137F", new byte[] { 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F });
        addDataItem(" DCB 4, $9137F", new byte[] { 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F },
                new ValueOutOfRangeErrorMessage(0x9137F));
        addDataItem(" DCB 4, 'AB'", new byte[] { 0x41, 0x42, 0x41, 0x42, 0x41, 0x42, 0x41, 0x42 });
        addDataItem(" DCB 0, 0", NO_DATA);
        addDataItem(" DCB -4, 0", NO_DATA, new CountMustNotBeNegativeErrorMessage());
        addDataItem(" DCB +4, 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DCB 4.2, 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DCB '4.2', 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        // TODO: test with a built-in function symbol
        //addDataItem(" DCB STRLEN, 0", NO_DATA, new FunctionCannotBeConvertedToRealErrorMessage());
        addDataItem(" DCB ~, 0", NO_DATA, new InvalidExpressionErrorMessage("~"));
        addDataItem(" DCB UNDEFINED, 0", NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" DCB 2, 4, 6", new byte[] { 0, 4, 0, 4 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DCB. 4, $137F", new byte[] { 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" DCB.B 4, $13", new byte[] { 0x13, 0x13, 0x13, 0x13 });
        addDataItem(" DCB.W 4, $137F", new byte[] { 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F });
        addDataItem(" DCB.L 4, $137F0248", new byte[] { 0x13, 0x7F, 0x02, 0x48, 0x13, 0x7F, 0x02, 0x48, 0x13, 0x7F, 0x02, 0x48,
                0x13, 0x7F, 0x02, 0x48 });
        addDataItem(" DCB.Q 4, $18293A4B5C6D7E0F", new byte[] { 0x18, 0x29, 0x3A, 0x4B, 0x5C, 0x6D, 0x7E, 0x0F, 0x18, 0x29, 0x3A,
                0x4B, 0x5C, 0x6D, 0x7E, 0x0F, 0x18, 0x29, 0x3A, 0x4B, 0x5C, 0x6D, 0x7E, 0x0F, 0x18, 0x29, 0x3A, 0x4B, 0x5C, 0x6D,
                0x7E, 0x0F });
        addDataItem(" DCB.S 4, 0.03125", new byte[] { 0x3D, 0x00, 0x00, 0x00, 0x3D, 0x00, 0x00, 0x00, 0x3D, 0x00, 0x00, 0x00, 0x3D,
                0x00, 0x00, 0x00 });
        addDataItem(" DCB.D 4, 0.03125", new byte[] { 0x3F, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3F, (byte) 0xA0,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3F, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x3F, (byte) 0xA0, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00 });
        // TODO: implement DCB.X
        //addDataItem(" DCB.X 4, 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        //        0, 0, 0, 0 });
        // TODO: implement DCB.P
        //addDataItem(" DCB.P 4, 0", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        //        0, 0, 0, 0 });
        addDataItem(" DCB.Z 4, $137F", new byte[] { 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F, 0x13, 0x7F }, INVALID_SIZE_ATTRIBUTE_Z);

        // DS
        addDataItem(" DS", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DS 0", NO_DATA);
        addDataItem(" DS 1", new byte[] { 0, 0 });
        addDataItem(" DS 13", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DS $8000000000000000", NO_DATA, new OutOfMemoryErrorMessage());
        addDataItem(" DS -1", NO_DATA, new CountMustNotBeNegativeErrorMessage());
        addDataItem(" DS ~", NO_DATA, new InvalidExpressionErrorMessage("~"));
        addDataItem(" DS 0, 0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DS. 1", new byte[] { 0, 0 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" DS.B 1", new byte[] { 0 });
        addDataItem(" DS.W 1", new byte[] { 0, 0 });
        addDataItem(" DS.L 1", new byte[] { 0, 0, 0, 0 });
        addDataItem(" DS.Q 1", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DS.S 1", new byte[] { 0, 0, 0, 0 });
        addDataItem(" DS.D 1", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DS.X 1", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DS.P 1", new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
        addDataItem(" DS.Z 1", new byte[] { 0, 0 }, INVALID_SIZE_ATTRIBUTE_Z);

        // HEX
        addDataItem(" HEX", NO_DATA);
        addDataItem(" HEX !", NO_DATA, new InvalidCharacterInHexDirectiveErrorMessage('!'));
        addDataItem(" HEX 0", NO_DATA, new OddNumberOfCharactersInHexDirectiveErrorMessage());
        addDataItem(" HEX G", NO_DATA, new InvalidCharacterInHexDirectiveErrorMessage('G'));
        addDataItem(" HEX 0!", NO_DATA, new InvalidCharacterInHexDirectiveErrorMessage('!'));
        addDataItem(" HEX 00", new byte[] { 0x00 });
        addDataItem(" HEX 123G45,67", new byte[] { 0x12, 0x67 }, new InvalidCharacterInHexDirectiveErrorMessage('G'));
        addDataItem(" HEX 11", new byte[] { 0x11 });
        addDataItem(" HEX FF", new byte[] { (byte) 0xFF });
        addDataItem(" HEX 1122", new byte[] { 0x11, 0x22 });
        addDataItem(" HEX 11,22", new byte[] { 0x11, 0x22 });
        addDataItem(" HEX 01234567,89ABCDEF,fedcba", new byte[] { 0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD,
                (byte) 0xEF, (byte) 0xFE, (byte) 0xDC, (byte) 0xBA });
        addDataItem(" HEX.B", NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
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

    private static void addDataItem(String code, byte[] output) {
        addDataItem(code, output, null);
    }

    private static void addDataItem(String code, byte[] output, AssemblyMessage expectedMessage) {
        TEST_DATA.add(new Object[] { code, output, expectedMessage });
    }

    /**
     * Initializes a new OutputDirectivesTest.
     *
     * @param code
     *            a line of code containing a directive
     * @param output
     *            the generated output for the instruction
     * @param expectedMessage
     *            an {@link AssemblyMessage} that is expected to be generated while assembling the line of code, or
     *            <code>null</code> if no message is expected
     */
    public OutputDirectivesTest(String code, byte[] output, AssemblyMessage expectedMessage) {
        super(code, 2, output, M68KArchitecture.MC68000, expectedMessage, null, null);
    }

}
