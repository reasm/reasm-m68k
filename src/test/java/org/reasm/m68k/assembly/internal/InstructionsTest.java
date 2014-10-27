package org.reasm.m68k.assembly.internal;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.*;

/**
 * Test class for all M68000 family instructions in a default configuration.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class InstructionsTest extends BaseInstructionsTest {

    private static final AssemblyMessage DATA_FOR_ADDQ_SUBQ_OUT_OF_RANGE = new DataForAddqSubqOutOfRangeErrorMessage();
    private static final AssemblyMessage MOVEM_REQUIRES_A_REGISTER_LIST_IN_ONE_OPERAND = new MovemRequiresARegisterListInOneOperandErrorMessage();
    private static final AssemblyMessage DATA_FOR_MOVEQ_WILL_BE_SIGN_EXTENDED = new DataForMoveqWillBeSignExtendedWarningMessage();
    private static final AssemblyMessage DATA_FOR_MOVEQ_OUT_OF_RANGE = new DataForMoveqOutOfRangeErrorMessage();
    private static final AssemblyMessage SHIFT_COUNT_OUT_OF_RANGE = new ShiftCountOutOfRangeErrorMessage();

    private static final List<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // ABCD
        addDataItem(" ABCD", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ABCD D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ABCD D0,D0", new short[] { (short) 0xC100 });
        addDataItem(" ABCD D0,D0,D0", new short[] { (short) 0xC100 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ABCD D0,D1", new short[] { (short) 0xC300 });
        addDataItem(" ABCD D0,D7", new short[] { (short) 0xCF00 });
        addDataItem(" ABCD D1,D0", new short[] { (short) 0xC101 });
        addDataItem(" ABCD D7,D0", new short[] { (short) 0xC107 });
        addDataItem(" ABCD D0,(A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ABCD D0,-(A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ABCD (A0),D0", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ABCD -(A0),-(A0)", new short[] { (short) 0xC108 });
        addDataItem(" ABCD -(A0),-(A1)", new short[] { (short) 0xC308 });
        addDataItem(" ABCD -(A0),-(A7)", new short[] { (short) 0xCF08 });
        addDataItem(" ABCD -(A1),-(A0)", new short[] { (short) 0xC109 });
        addDataItem(" ABCD -(A7),-(A0)", new short[] { (short) 0xC10F });
        addDataItem(" ABCD -(A0),D0", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ABCD -(A0),(A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ABCD. D0,D0", new short[] { (short) 0xC100 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ABCD.B D0,D0", new short[] { (short) 0xC100 });
        addDataItem(" ABCD.W D0,D0", new short[] { (short) 0xC100 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" ABCD.L D0,D0", new short[] { (short) 0xC100 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" ABCD.Z D0,D0", new short[] { (short) 0xC100 }, INVALID_SIZE_ATTRIBUTE_Z);

        // ADD
        addDataItem(" ADD", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADD D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADD D0,D0", new short[] { (short) 0xD040 });
        addDataItem(" ADD D0,D0,D0", new short[] { (short) 0xD040 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADD D0,D7", new short[] { (short) 0xDE40 });
        addDataItem(" ADD D7,D0", new short[] { (short) 0xD047 });
        addDataItem(" ADD A0,D0", new short[] { (short) 0xD048 });
        addDataItem(" ADD (A0),D0", new short[] { (short) 0xD050 });
        addDataItem(" ADD #0,D0", new short[] { (short) 0xD07C, 0x0000 });
        addDataItem(" ADD #2,D0", new short[] { (short) 0xD07C, 0x0002 });
        addDataItem(" ADD #9,D0", new short[] { (short) 0xD07C, 0x0009 });
        addDataItem(" ADD #$1234,D0", new short[] { (short) 0xD07C, 0x1234 });
        addDataItem(" ADD D0,A0", new short[] { (short) 0xD0C0 });
        addDataItem(" ADD A0,A0", new short[] { (short) 0xD0C8 });
        addDataItem(" ADD #0,A0", new short[] { (short) 0xD0FC, 0x0000 });
        addDataItem(" ADD #2,A0", new short[] { (short) 0xD0FC, 0x0002 });
        addDataItem(" ADD #9,A0", new short[] { (short) 0xD0FC, 0x0009 });
        addDataItem(" ADD #$1234,A0", new short[] { (short) 0xD0FC, 0x1234 });
        addDataItem(" ADD D0,(A0)", new short[] { (short) 0xD150 });
        addDataItem(" ADD #0,(A0)", new short[] { 0x0650, 0x0000 });
        addDataItem(" ADD #2,(A0)", new short[] { 0x0650, 0x0002 });
        addDataItem(" ADD #$1234,(A0)", new short[] { 0x0650, 0x1234 });
        addDataItem(" ADD (A0),(A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ADD. D0,D0", new short[] { (short) 0xD040 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ADD.B D0,D0", new short[] { (short) 0xD000 });
        addDataItem(" ADD.B A0,D0", new short[] { (short) 0xD008 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" ADD.B D0,A0", new short[] { (short) 0xD0C0 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" ADD.B D0,(A0)", new short[] { (short) 0xD110 });
        addDataItem(" ADD.W D0,D0", new short[] { (short) 0xD040 });
        addDataItem(" ADD.W A0,D0", new short[] { (short) 0xD048 });
        addDataItem(" ADD.W D0,A0", new short[] { (short) 0xD0C0 });
        addDataItem(" ADD.W D0,(A0)", new short[] { (short) 0xD150 });
        addDataItem(" ADD.L D0,D0", new short[] { (short) 0xD080 });
        addDataItem(" ADD.L A0,D0", new short[] { (short) 0xD088 });
        addDataItem(" ADD.L D0,A0", new short[] { (short) 0xD1C0 });
        addDataItem(" ADD.L D0,(A0)", new short[] { (short) 0xD190 });
        addDataItem(" ADD.Z D0,D0", new short[] { (short) 0xD040 }, INVALID_SIZE_ATTRIBUTE_Z);
        // --> see also OptimizeToAddqSubqTest for tests with the "optimizeToAddqSubq" option enabled

        // ADDA
        addDataItem(" ADDA", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADDA D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADDA D0,A0", new short[] { (short) 0xD0C0 });
        addDataItem(" ADDA D0,A0,A0", new short[] { (short) 0xD0C0 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADDA D0,A7", new short[] { (short) 0xDEC0 });
        addDataItem(" ADDA D7,A0", new short[] { (short) 0xD0C7 });
        addDataItem(" ADDA A0,A0", new short[] { (short) 0xD0C8 });
        addDataItem(" ADDA (A0),A0", new short[] { (short) 0xD0D0 });
        addDataItem(" ADDA #0,A0", new short[] { (short) 0xD0FC, 0x0000 });
        addDataItem(" ADDA #2,A0", new short[] { (short) 0xD0FC, 0x0002 });
        addDataItem(" ADDA #9,A0", new short[] { (short) 0xD0FC, 0x0009 });
        addDataItem(" ADDA #$1234,A0", new short[] { (short) 0xD0FC, 0x1234 });
        addDataItem(" ADDA D0,D0", new short[] { (short) 0xD040 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ADDA D0,(A0)", new short[] { (short) 0xD150 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ADDA. D0,A0", new short[] { (short) 0xD0C0 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ADDA.B D0,A0", new short[] { (short) 0xD0C0 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" ADDA.B A0,A0", new short[] { (short) 0xD0C8 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" ADDA.W D0,A0", new short[] { (short) 0xD0C0 });
        addDataItem(" ADDA.W A0,A0", new short[] { (short) 0xD0C8 });
        addDataItem(" ADDA.L D0,A0", new short[] { (short) 0xD1C0 });
        addDataItem(" ADDA.L A0,A0", new short[] { (short) 0xD1C8 });
        addDataItem(" ADDA.Z D0,A0", new short[] { (short) 0xD0C0 }, INVALID_SIZE_ATTRIBUTE_Z);
        // --> see also OptimizeToAddqSubqTest for tests with the "optimizeToAddqSubq" option enabled

        // ADDI
        addDataItem(" ADDI", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADDI #0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADDI #0,D0", new short[] { 0x0640, 0x0000 });
        addDataItem(" ADDI #0,D0,D0", new short[] { 0x0640, 0x0000 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADDI #2,D0", new short[] { 0x0640, 0x0002 });
        addDataItem(" ADDI #9,D0", new short[] { 0x0640, 0x0009 });
        addDataItem(" ADDI #$1234,D0", new short[] { 0x0640, 0x1234 });
        addDataItem(" ADDI #$1234,D7", new short[] { 0x0647, 0x1234 });
        addDataItem(" ADDI #0,A0", new short[] { (short) 0xD0FC, 0x0000 });
        addDataItem(" ADDI #2,A0", new short[] { (short) 0xD0FC, 0x0002 });
        addDataItem(" ADDI #9,A0", new short[] { (short) 0xD0FC, 0x0009 });
        addDataItem(" ADDI #$1234,A0", new short[] { (short) 0xD0FC, 0x1234 });
        addDataItem(" ADDI #0,(A0)", new short[] { 0x0650, 0x0000 });
        addDataItem(" ADDI #2,(A0)", new short[] { 0x0650, 0x0002 });
        addDataItem(" ADDI #$1234,(A0)", new short[] { 0x0650, 0x1234 });
        addDataItem(" ADDI #$1234,$5678(A0)", new short[] { 0x0668, 0x1234, 0x5678 });
        addDataItem(" ADDI D0,D1", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ADDI. #$1234,D0", new short[] { 0x0640, 0x1234 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ADDI.B #$12,D0", new short[] { 0x0600, 0x0012 });
        addDataItem(" ADDI.B #$12,A0", new short[] { (short) 0xD0FC, 0x0012 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" ADDI.W #$1234,D0", new short[] { 0x0640, 0x1234 });
        addDataItem(" ADDI.W #$1234,A0", new short[] { (short) 0xD0FC, 0x1234 });
        addDataItem(" ADDI.L #$12345678,D0", new short[] { 0x0680, 0x1234, 0x5678 });
        addDataItem(" ADDI.L #$12345678,A0", new short[] { (short) 0xD1FC, 0x1234, 0x5678 });
        addDataItem(" ADDI.Z #$1234,D0", new short[] { 0x0640, 0x1234 }, INVALID_SIZE_ATTRIBUTE_Z);
        // --> see also OptimizeToAddqSubqTest for tests with the "optimizeToAddqSubq" option enabled

        // ADDQ
        addDataItem(" ADDQ", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADDQ #1", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADDQ #1,D0", new short[] { 0x5240 });
        addDataItem(" ADDQ #1,D0,D0", new short[] { 0x5240 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ADDQ #2,D0", new short[] { 0x5440 });
        addDataItem(" ADDQ #3,D0", new short[] { 0x5640 });
        addDataItem(" ADDQ #4,D0", new short[] { 0x5840 });
        addDataItem(" ADDQ #5,D0", new short[] { 0x5A40 });
        addDataItem(" ADDQ #6,D0", new short[] { 0x5C40 });
        addDataItem(" ADDQ #7,D0", new short[] { 0x5E40 });
        addDataItem(" ADDQ #8,D0", new short[] { 0x5040 });
        addDataItem(" ADDQ #9,D0", new short[] { 0x5240 }, DATA_FOR_ADDQ_SUBQ_OUT_OF_RANGE);
        addDataItem(" ADDQ #0,D0", new short[] { 0x5040 }, DATA_FOR_ADDQ_SUBQ_OUT_OF_RANGE);
        addDataItem(" ADDQ #$1234,D0", new short[] { 0x5840 }, DATA_FOR_ADDQ_SUBQ_OUT_OF_RANGE);
        addDataItem(" ADDQ #0,A0", new short[] { 0x5048 }, DATA_FOR_ADDQ_SUBQ_OUT_OF_RANGE);
        addDataItem(" ADDQ #2,A0", new short[] { 0x5448 });
        addDataItem(" ADDQ #$1234,A0", new short[] { 0x5848 }, DATA_FOR_ADDQ_SUBQ_OUT_OF_RANGE);
        addDataItem(" ADDQ #0,(A0)", new short[] { 0x5050 }, DATA_FOR_ADDQ_SUBQ_OUT_OF_RANGE);
        addDataItem(" ADDQ #2,(A0)", new short[] { 0x5450 });
        addDataItem(" ADDQ #$1234,(A0)", new short[] { 0x5850 }, DATA_FOR_ADDQ_SUBQ_OUT_OF_RANGE);
        addDataItem(" ADDQ D1,D0", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ADDQ. #1,D0", new short[] { 0x5240 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ADDQ.B #1,D0", new short[] { 0x5200 });
        addDataItem(" ADDQ.B #1,A0", new short[] { 0x5208 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" ADDQ.W #1,D0", new short[] { 0x5240 });
        addDataItem(" ADDQ.W #1,A0", new short[] { 0x5248 });
        addDataItem(" ADDQ.L #1,D0", new short[] { 0x5280 });
        addDataItem(" ADDQ.L #1,A0", new short[] { 0x5288 });
        addDataItem(" ADDQ.Z #1,D0", new short[] { 0x5240 }, INVALID_SIZE_ATTRIBUTE_Z);

        // ADDX
        addDataItem(" ADDX D0,D0", new short[] { (short) 0xD140 });
        addDataItem(" ADDX. D0,D0", new short[] { (short) 0xD140 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ADDX.B D0,D0", new short[] { (short) 0xD100 });
        addDataItem(" ADDX.W D0,D0", new short[] { (short) 0xD140 });
        addDataItem(" ADDX.L D0,D0", new short[] { (short) 0xD180 });
        addDataItem(" ADDX.Z D0,D0", new short[] { (short) 0xD140 }, INVALID_SIZE_ATTRIBUTE_Z);
        // --> see ABCD for more tests

        // AND
        addDataItem(" AND", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" AND D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" AND D0,D0", new short[] { (short) 0xC040 });
        addDataItem(" AND D0,D0,D0", new short[] { (short) 0xC040 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" AND D0,D7", new short[] { (short) 0xCE40 });
        addDataItem(" AND D7,D0", new short[] { (short) 0xC047 });
        addDataItem(" AND A0,D0", new short[] { (short) 0xC048 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" AND (A0),D0", new short[] { (short) 0xC050 });
        addDataItem(" AND #$1234,D0", new short[] { (short) 0xC07C, 0x1234 });
        addDataItem(" AND D0,A0", new short[] { (short) 0xC0C0 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" AND D0,(A0)", new short[] { (short) 0xC150 });
        addDataItem(" AND #$1234,(A0)", new short[] { 0x0250, 0x1234 });
        addDataItem(" AND (A0),(A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" AND D0,CCR", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" AND #$12,CCR", new short[] { 0x023C, 0x0012 });
        addDataItem(" AND #$1234,SR", new short[] { 0x027C, 0x1234 });
        addDataItem(" AND. D0,D0", new short[] { (short) 0xC040 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" AND.B D0,D0", new short[] { (short) 0xC000 });
        addDataItem(" AND.B D0,(A0)", new short[] { (short) 0xC110 });
        addDataItem(" AND.B #$12,CCR", new short[] { 0x023C, 0x0012 });
        addDataItem(" AND.B #$1234,SR", new short[] { 0x027C, 0x1234 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" AND.W D0,D0", new short[] { (short) 0xC040 });
        addDataItem(" AND.W D0,(A0)", new short[] { (short) 0xC150 });
        addDataItem(" AND.W #$12,CCR", new short[] { (short) 0x023C, 0x0012 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" AND.W #$1234,SR", new short[] { (short) 0x027C, 0x1234 });
        addDataItem(" AND.L D0,D0", new short[] { (short) 0xC080 });
        addDataItem(" AND.L D0,(A0)", new short[] { (short) 0xC190 });
        addDataItem(" AND.L #$12,CCR", new short[] { 0x023C, 0x0012 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" AND.L #$1234,SR", new short[] { 0x027C, 0x1234 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" AND.Z D0,D0", new short[] { (short) 0xC040 }, INVALID_SIZE_ATTRIBUTE_Z);

        // ANDI
        addDataItem(" ANDI", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ANDI #$1234", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ANDI #$1234,D0", new short[] { 0x0240, 0x1234 });
        addDataItem(" ANDI #$1234,D0,D0", new short[] { 0x0240, 0x1234 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ANDI #$1234,A0", new short[] { 0x0248, 0x1234 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ANDI #$1234,(A0)", new short[] { 0x0250, 0x1234 });
        addDataItem(" ANDI D0,CCR", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ANDI #$12,CCR", new short[] { 0x023C, 0x0012 });
        addDataItem(" ANDI #$1234,CCR", new short[] { 0x023C, 0x0034 }, new ValueOutOfRangeErrorMessage(0x1234));
        addDataItem(" ANDI #$12345678,CCR", new short[] { 0x023C, 0x0078 }, new ValueOutOfRangeErrorMessage(0x12345678));
        addDataItem(" ANDI #$12,SR", new short[] { 0x027C, 0x0012 });
        addDataItem(" ANDI #$1234,SR", new short[] { 0x027C, 0x1234 });
        addDataItem(" ANDI #$12345678,SR", new short[] { 0x027C, 0x5678 }, new ValueOutOfRangeErrorMessage(0x12345678));
        addDataItem(" ANDI. #$1234,D0", new short[] { 0x0240, 0x1234 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ANDI.B #$12,D0", new short[] { 0x0200, 0x0012 });
        addDataItem(" ANDI.B #$12,CCR", new short[] { 0x023C, 0x0012 });
        addDataItem(" ANDI.B #$12,SR", new short[] { 0x027C, 0x0012 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" ANDI.W #$1234,D0", new short[] { 0x0240, 0x1234 });
        addDataItem(" ANDI.W #$12,CCR", new short[] { 0x023C, 0x0012 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" ANDI.W #$1234,SR", new short[] { 0x027C, 0x1234 });
        addDataItem(" ANDI.L #$12345678,D0", new short[] { 0x0280, 0x1234, 0x5678 });
        addDataItem(" ANDI.L #$12,CCR", new short[] { 0x023C, 0x0012 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" ANDI.L #$1234,SR", new short[] { 0x027C, 0x1234 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" ANDI.Z #$1234,D0", new short[] { 0x0240, 0x1234 }, INVALID_SIZE_ATTRIBUTE_Z);

        // ASL
        addDataItem(" ASL D0", new short[] { (short) 0xE340 });
        addDataItem(" ASL #1,D0", new short[] { (short) 0xE340 });
        addDataItem(" ASL D0,D0", new short[] { (short) 0xE160 });
        addDataItem(" ASL (A0)", new short[] { (short) 0xE1D0 });
        // --> see ASR for more tests

        // ASR
        addDataItem(" ASR", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        // - Register shift
        addDataItem(" ASR D0", new short[] { (short) 0xE240 });
        addDataItem(" ASR #1,D0", new short[] { (short) 0xE240 });
        addDataItem(" ASR #1,D0,D0", new short[] { (short) 0xE240 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ASR #2,D0", new short[] { (short) 0xE440 });
        addDataItem(" ASR #7,D0", new short[] { (short) 0xEE40 });
        addDataItem(" ASR #8,D0", new short[] { (short) 0xE040 });
        addDataItem(" ASR #9,D0", new short[] { (short) 0xE240 }, SHIFT_COUNT_OUT_OF_RANGE);
        addDataItem(" ASR #0,D0", new short[] { (short) 0xE040 }, SHIFT_COUNT_OUT_OF_RANGE);
        addDataItem(" ASR #1,D1", new short[] { (short) 0xE241 });
        addDataItem(" ASR #1,D7", new short[] { (short) 0xE247 });
        addDataItem(" ASR #1,(A0)", new short[] { (short) 0xE240 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ASR. #1,D0", new short[] { (short) 0xE240 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ASR.B #1,D0", new short[] { (short) 0xE200 });
        addDataItem(" ASR.W #1,D0", new short[] { (short) 0xE240 });
        addDataItem(" ASR.L #1,D0", new short[] { (short) 0xE280 });
        addDataItem(" ASR.Z #1,D0", new short[] { (short) 0xE240 }, INVALID_SIZE_ATTRIBUTE_Z);
        addDataItem(" ASR D0,D0", new short[] { (short) 0xE060 });
        addDataItem(" ASR D0,D1", new short[] { (short) 0xE061 });
        addDataItem(" ASR D0,D7", new short[] { (short) 0xE067 });
        addDataItem(" ASR D1,D0", new short[] { (short) 0xE260 });
        addDataItem(" ASR D7,D0", new short[] { (short) 0xEE60 });
        addDataItem(" ASR D0,(A0)", new short[] { (short) 0xE060 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ASR. D0,D0", new short[] { (short) 0xE060 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ASR.B D0,D0", new short[] { (short) 0xE020 });
        addDataItem(" ASR.W D0,D0", new short[] { (short) 0xE060 });
        addDataItem(" ASR.L D0,D0", new short[] { (short) 0xE0A0 });
        addDataItem(" ASR.Z D0,D0", new short[] { (short) 0xE060 }, INVALID_SIZE_ATTRIBUTE_Z);
        // - Memory shift
        addDataItem(" ASR A0", new short[] { (short) 0xE0C8 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ASR (A0)", new short[] { (short) 0xE0D0 });
        addDataItem(" ASR (2,A0)", new short[] { (short) 0xE0E8, 0x0002 });
        addDataItem(" ASR (2,PC)", new short[] { (short) 0xE0FA, 0x0000 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ASR. (A0)", new short[] { (short) 0xE0D0 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ASR.B (A0)", new short[] { (short) 0xE0D0 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" ASR.W (A0)", new short[] { (short) 0xE0D0 });
        addDataItem(" ASR.L (A0)", new short[] { (short) 0xE0D0 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" ASR.Z (A0)", new short[] { (short) 0xE0D0 }, INVALID_SIZE_ATTRIBUTE_Z);
        // - broken shift
        addDataItem(" ASR (A0),D0", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);

        // BCC
        addDataItem(" BCC 0", new short[] { 0x6400, -2 });
        // --> see BRA for more tests

        // BCHG
        addDataItem(" BCHG", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        // - Dynamic form
        addDataItem(" BCHG D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BCHG D0,D0", new short[] { 0x0140 });
        addDataItem(" BCHG D0,D0,D0", new short[] { 0x0140 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BCHG D0,D1", new short[] { 0x0141 });
        addDataItem(" BCHG D0,D7", new short[] { 0x0147 });
        addDataItem(" BCHG D1,D0", new short[] { 0x0340 });
        addDataItem(" BCHG D7,D0", new short[] { 0x0F40 });
        addDataItem(" BCHG D0,A0", new short[] { 0x0148 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" BCHG D0,(A0)", new short[] { 0x0150 });
        addDataItem(" BCHG D0,$1234(PC)", new short[] { 0x017A, 0x1232 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" BCHG. D0,D0", new short[] { 0x0140 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" BCHG. D0,(A0)", new short[] { 0x0150 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" BCHG.B D0,D0", new short[] { 0x0140 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" BCHG.B D0,(A0)", new short[] { 0x0150 });
        addDataItem(" BCHG.W D0,D0", new short[] { 0x0140 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" BCHG.W D0,(A0)", new short[] { 0x0150 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" BCHG.L D0,D0", new short[] { 0x0140 });
        addDataItem(" BCHG.L D0,(A0)", new short[] { 0x0150 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" BCHG.Z D0,D0", new short[] { 0x0140 }, INVALID_SIZE_ATTRIBUTE_Z);
        addDataItem(" BCHG.Z D0,(A0)", new short[] { 0x0150 }, INVALID_SIZE_ATTRIBUTE_Z);
        // - Static form
        addDataItem(" BCHG #0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BCHG #0,D0", new short[] { 0x0840, 0x0000 });
        addDataItem(" BCHG #0,D0,D0", new short[] { 0x0840, 0x0000 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BCHG #0,D1", new short[] { 0x0841, 0x0000 });
        addDataItem(" BCHG #0,D7", new short[] { 0x0847, 0x0000 });
        addDataItem(" BCHG #$1F,D0", new short[] { 0x0840, 0x001F });
        addDataItem(" BCHG #$FF,D0", new short[] { 0x0840, 0x00FF });
        addDataItem(" BCHG #$100,D0", new short[] { 0x0840, 0x0000 }, new ValueOutOfRangeErrorMessage(0x100));
        addDataItem(" BCHG #-1,D0", new short[] { 0x0840, 0x00FF }, new ValueOutOfRangeErrorMessage(-1));
        addDataItem(" BCHG #$FFFFFFFF,D0", new short[] { 0x0840, 0x00FF }, new ValueOutOfRangeErrorMessage(-1));
        addDataItem(" BCHG #0,A0", new short[] { 0x0848, 0x0000 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" BCHG #0,(A0)", new short[] { 0x0850, 0x0000 });
        addDataItem(" BCHG #0,$1234(PC)", new short[] { 0x087A, 0x0000, 0x1230 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" BCHG. #0,D0", new short[] { 0x0840, 0x0000 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" BCHG. #0,(A0)", new short[] { 0x0850, 0x0000 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" BCHG.B #0,D0", new short[] { 0x0840, 0x0000 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" BCHG.B #0,(A0)", new short[] { 0x0850, 0x0000 });
        addDataItem(" BCHG.W #0,D0", new short[] { 0x0840, 0x0000 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" BCHG.W #0,(A0)", new short[] { 0x0850, 0x0000 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" BCHG.L #0,D0", new short[] { 0x0840, 0x0000 });
        addDataItem(" BCHG.L #0,(A0)", new short[] { 0x0850, 0x0000 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" BCHG.Z #0,D0", new short[] { 0x0840, 0x0000 }, INVALID_SIZE_ATTRIBUTE_Z);
        addDataItem(" BCHG.Z #0,(A0)", new short[] { 0x0850, 0x0000 }, INVALID_SIZE_ATTRIBUTE_Z);
        // - Unknown form
        addDataItem(" BCHG A0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BCHG A0,D1", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);

        // BCLR
        // - Dynamic form
        addDataItem(" BCLR D0,D0", new short[] { 0x0180 });
        // - Static form
        addDataItem(" BCLR #0,D0", new short[] { 0x0880, 0x0000 });
        // --> see BCHG for more tests

        // BCS
        addDataItem(" BCS 0", new short[] { 0x6500, -2 });
        // --> see BRA for more tests

        // BEQ
        addDataItem(" BEQ 0", new short[] { 0x6700, -2 });
        // --> see BRA for more tests

        // BGE
        addDataItem(" BGE 0", new short[] { 0x6C00, -2 });
        // --> see BRA for more tests

        // BGND
        addDataItem(" BGND", new short[] { 0x4AFA }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" BGND", new short[] { 0x4AFA }, M68KArchitecture.CPU32);
        addDataItem(" BGND", new short[] { 0x4AFA }, M68KArchitecture.MC68020, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" BGND D0", new short[] { 0x4AFA }, M68KArchitecture.CPU32, WRONG_NUMBER_OF_OPERANDS);
        // --> see NOP for more tests

        // BGT
        addDataItem(" BGT 0", new short[] { 0x6E00, -2 });
        // --> see BRA for more tests

        // BHI
        addDataItem(" BHI 0", new short[] { 0x6200, -2 });
        // --> see BRA for more tests

        // BHS
        addDataItem(" BHS 0", new short[] { 0x6400, -2 });
        // --> see BRA for more tests

        // BKPT
        addDataItem(" BKPT", NO_DATA, M68KArchitecture.MC68010, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BKPT #0", new short[] { 0x4848 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" BKPT #0", new short[] { 0x4848 }, M68KArchitecture.MC68EC000);
        addDataItem(" BKPT #0,D0", new short[] { 0x4848 }, M68KArchitecture.MC68EC000, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BKPT #0", new short[] { 0x4848 }, M68KArchitecture.MC68010);
        addDataItem(" BKPT #7", new short[] { 0x484F }, M68KArchitecture.MC68010);
        addDataItem(" BKPT #8", new short[] { 0x4848 }, M68KArchitecture.MC68010, BREAKPOINT_NUMBER_OUT_OF_RANGE);
        addDataItem(" BKPT #-1", new short[] { 0x484F }, M68KArchitecture.MC68010, BREAKPOINT_NUMBER_OUT_OF_RANGE);
        addDataItem(" BKPT. #0", new short[] { 0x4848 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" BKPT.B #0", new short[] { 0x4848 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" BKPT.W #0", new short[] { 0x4848 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" BKPT.L #0", new short[] { 0x4848 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" BKPT.Z #0", new short[] { 0x4848 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_Z);
        addDataItem(" BKPT D0", new short[] { 0x4848 }, M68KArchitecture.MC68010, ADDRESSING_MODE_NOT_ALLOWED_HERE);

        // BLE
        addDataItem(" BLE 0", new short[] { 0x6F00, -2 });
        // --> see BRA for more tests

        // BLO
        addDataItem(" BLO 0", new short[] { 0x6500, -2 });
        // --> see BRA for more tests

        // BLS
        addDataItem(" BLS 0", new short[] { 0x6300, -2 });
        // --> see BRA for more tests

        // BLT
        addDataItem(" BLT 0", new short[] { 0x6D00, -2 });
        // --> see BRA for more tests

        // BMI
        addDataItem(" BMI 0", new short[] { 0x6B00, -2 });
        // --> see BRA for more tests

        // BNE
        addDataItem(" BNE 0", new short[] { 0x6600, -2 });
        // --> see BRA for more tests

        // BPL
        addDataItem(" BPL 0", new short[] { 0x6A00, -2 });
        // --> see BRA for more tests

        // BRA
        addDataItem(" BRA", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BRA UNDEFINED", new short[] { 0x6000, -2 }, UNDEFINED_SYMBOL);
        addDataItem(" BRA 0", new short[] { 0x6000, -2 });
        addDataItem(" BRA 0,0", new short[] { 0x6000, -2 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" BRA 0-", new short[] { 0x6000, -2 }, new InvalidExpressionErrorMessage("0-"));
        addDataItem(" BRA 0a", new short[] { 0x6000, -2 }, new InvalidExpressionErrorMessage("0a"));
        addDataItem(" BRA $8000", new short[] { 0x6000, 0x7FFE });
        addDataItem(" BRA $8002", new short[] { 0x6000, -0x8000 }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA $8002", new short[] { 0x6000, -0x8000 }, M68KArchitecture.MC68020, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA -$8000", new short[] { 0x6000, 0x7FFE }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA -$8000", new short[] { 0x6000, 0x7FFE }, M68KArchitecture.MC68020, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" BRA -$7FFE", new short[] { 0x6000, -0x8000 });
        addDataItem(" BRA -$7FFC", new short[] { 0x6000, -0x7FFE });
        addDataItem("SELF: BRA SELF", new short[] { 0x6000, -2 });
        addDataItem(" BRA 0.5", new short[] { 0x6000, -2 }, LABEL_EXPECTED);
        addDataItem(" BRA 'ab'", new short[] { 0x6000, -2 }, LABEL_EXPECTED);
        // TODO: test with a built-in function symbol
        //addDataItem(" BRA STRLEN", new short[] { 0x6000, -2 }, LABEL_EXPECTED);
        addDataItem(" BRA. 0", new short[] { 0x6000, -2 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
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
        addDataItem(" BRA.Z 0", new short[] { 0x6000, -2 }, INVALID_SIZE_ATTRIBUTE_Z);
        // --> see also OptimizeUnsizedBranchesTest for tests with the "optimizeUnsizedBranches" option enabled

        // BSET
        // - Dynamic form
        addDataItem(" BSET D0,D0", new short[] { 0x01C0 });
        // - Static form
        addDataItem(" BSET #0,D0", new short[] { 0x08C0, 0x0000 });
        // --> see BCHG for more tests

        // BSR
        addDataItem(" BSR 0", new short[] { 0x6100, -2 });
        // --> see BRA for more tests

        // BTST
        // - Dynamic form
        addDataItem(" BTST D0,D0", new short[] { 0x0100 });
        addDataItem(" BTST D0,$1234(PC)", new short[] { 0x013A, 0x1232 });
        // - Static form
        addDataItem(" BTST #0,D0", new short[] { 0x0800, 0x0000 });
        addDataItem(" BTST #0,$1234(PC)", new short[] { 0x083A, 0x0000, 0x1230 });
        // --> see BCHG for more tests

        // BVC
        addDataItem(" BVC 0", new short[] { 0x6800, -2 });
        // --> see BRA for more tests

        // BVS
        addDataItem(" BVS 0", new short[] { 0x6900, -2 });
        // --> see BRA for more tests

        // CHK
        addDataItem(" CHK", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CHK D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CHK D0,D0", new short[] { 0x4180 });
        addDataItem(" CHK D0,D0,D0", new short[] { 0x4180 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CHK D7,D0", new short[] { 0x4187 });
        addDataItem(" CHK A0,D0", new short[] { 0x4188 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" CHK (A0),D0", new short[] { 0x4190 });
        addDataItem(" CHK (2,A0),D0", new short[] { 0x41A8, 0x0002 });
        addDataItem(" CHK D0,D7", new short[] { 0x4F80 });
        addDataItem(" CHK D0,A0", new short[] { 0x4180 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" CHK D0,(A0)", new short[] { 0x4180 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" CHK. D0,D0", new short[] { 0x4180 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" CHK.B D0,D0", new short[] { 0x4180 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" CHK.W D0,D0", new short[] { 0x4180 });
        addDataItem(" CHK.L D0,D0", new short[] { 0x4100 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" CHK.L D0,D0", new short[] { 0x4100 }, M68KArchitecture.CPU32, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" CHK.L D0,D0", new short[] { 0x4100 }, M68KArchitecture.MC68020);
        addDataItem(" CHK.Z D0,D0", new short[] { 0x4180 }, INVALID_SIZE_ATTRIBUTE_Z);

        // CLR
        addDataItem(" CLR", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CLR D0", new short[] { 0x4240 });
        addDataItem(" CLR A0", new short[] { 0x4248 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" CLR 2(A0)", new short[] { 0x4268, 0x0002 });
        addDataItem(" CLR #0", new short[] { 0x427C, 0x0000 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" CLR. D0", new short[] { 0x4240 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" CLR.B D0", new short[] { 0x4200 });
        addDataItem(" CLR.W D0", new short[] { 0x4240 });
        addDataItem(" CLR.L D0", new short[] { 0x4280 });
        addDataItem(" CLR.Z D0", new short[] { 0x4240 }, INVALID_SIZE_ATTRIBUTE_Z);

        // CMP
        addDataItem(" CMP", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CMP D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CMP D0,D0", new short[] { (short) 0xB040 });
        addDataItem(" CMP D0,D7", new short[] { (short) 0xBE40 });
        addDataItem(" CMP D7,D0", new short[] { (short) 0xB047 });
        addDataItem(" CMP A0,D0", new short[] { (short) 0xB048 });
        addDataItem(" CMP (A0),D0", new short[] { (short) 0xB050 });
        addDataItem(" CMP #0,D0", new short[] { (short) 0xB07C, 0x0000 });
        addDataItem(" CMP #$1234,D0", new short[] { (short) 0xB07C, 0x1234 });
        addDataItem(" CMP D0,A0", new short[] { (short) 0xB0C0 });
        addDataItem(" CMP A0,A0", new short[] { (short) 0xB0C8 });
        addDataItem(" CMP #0,A0", new short[] { (short) 0xB0FC, 0x0000 });
        addDataItem(" CMP #$1234,A0", new short[] { (short) 0xB0FC, 0x1234 });
        addDataItem(" CMP D0,(A0)", new short[] { (short) 0xB150 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" CMP #0,(A0)", new short[] { 0x0C50, 0x0000 });
        addDataItem(" CMP #$1234,(A0)", new short[] { 0x0C50, 0x1234 });
        addDataItem(" CMP (A0),(A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" CMP (A1)+,(A0)+", new short[] { (short) 0xB149 });
        addDataItem(" CMP (A0)+,D0", new short[] { (short) 0xB058 });
        addDataItem(" CMP #0,(A0)+", new short[] { 0x0C58, 0x0000 });
        addDataItem(" CMP #$1234,(A0)+", new short[] { 0x0C58, 0x1234 });
        addDataItem(" CMP. D0,D0", new short[] { (short) 0xB040 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" CMP.B D0,D0", new short[] { (short) 0xB000 });
        addDataItem(" CMP.B (A0),D0", new short[] { (short) 0xB010 });
        addDataItem(" CMP.W D0,D0", new short[] { (short) 0xB040 });
        addDataItem(" CMP.W (A0),D0", new short[] { (short) 0xB050 });
        addDataItem(" CMP.L D0,D0", new short[] { (short) 0xB080 });
        addDataItem(" CMP.L (A0),D0", new short[] { (short) 0xB090 });
        addDataItem(" CMP.Z D0,D0", new short[] { (short) 0xB040 }, INVALID_SIZE_ATTRIBUTE_Z);

        // CMPA
        addDataItem(" CMPA D0,A0", new short[] { (short) 0xB0C0 });
        // --> see ADDA for more tests

        // CMPI
        addDataItem(" CMPI #0,D0", new short[] { 0x0C40, 0x0000 });
        addDataItem(" CMPI #$1234,D0", new short[] { 0x0C40, 0x1234 });
        addDataItem(" CMPI #0,A0", new short[] { (short) 0xB0FC, 0x0000 });
        addDataItem(" CMPI #$1234,A0", new short[] { (short) 0xB0FC, 0x1234 });
        addDataItem(" CMPI #$1234,0(PC)", new short[] { (short) 0x0C7A, 0x1234, -4 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" CMPI #$1234,0(PC)", new short[] { (short) 0x0C7A, 0x1234, -4 }, M68KArchitecture.CPU32);
        addDataItem(" CMPI #$1234,0(PC)", new short[] { (short) 0x0C7A, 0x1234, -4 }, M68KArchitecture.MC68020);
        // --> see ADDI for more tests

        // CMPM
        addDataItem(" CMPM", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CMPM (A1)+", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CMPM (A1)+,(A0)+", new short[] { (short) 0xB149 });
        addDataItem(" CMPM (A1)+,D0", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" CMPM D0,(A0)+", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);

        // DBCC
        addDataItem("SELF: DBCC D0,SELF", new short[] { 0x54C8, -2 });
        // --> see DBF for more tests

        // DBCS
        addDataItem("SELF: DBCS D0,SELF", new short[] { 0x55C8, -2 });
        // --> see DBF for more tests

        // DBEQ
        addDataItem("SELF: DBEQ D0,SELF", new short[] { 0x57C8, -2 });
        // --> see DBF for more tests

        // DBF
        addDataItem(" DBF", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DBF D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DBF D0,UNDEFINED", new short[] { 0x51C8, 0 }, UNDEFINED_SYMBOL);
        addDataItem(" DBF D0,0", new short[] { 0x51C8, -2 });
        addDataItem(" DBF D0,0-", new short[] { 0x51C8, 0 }, new InvalidExpressionErrorMessage("0-"));
        addDataItem(" DBF D0,0a", new short[] { 0x51C8, 0 }, new InvalidExpressionErrorMessage("0a"));
        addDataItem(" DBF D0,$8000", new short[] { 0x51C8, 0x7FFE });
        addDataItem(" DBF D0,$8002", new short[] { 0x51C8, -0x8000 }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" DBF D0,$8002", new short[] { 0x51C8, -0x8000 }, M68KArchitecture.MC68020, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" DBF D0,-$8000", new short[] { 0x51C8, 0x7FFE }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" DBF D0,-$8000", new short[] { 0x51C8, 0x7FFE }, M68KArchitecture.MC68020, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" DBF D0,-$7FFE", new short[] { 0x51C8, -0x8000 });
        addDataItem(" DBF D0,-$7FFC", new short[] { 0x51C8, -0x7FFE });
        addDataItem("SELF: DBF D0,SELF", new short[] { 0x51C8, -2 });
        addDataItem(" DBF D0,0.5", new short[] { 0x51C8, 0 }, LABEL_EXPECTED);
        addDataItem(" DBF D0,'ab'", new short[] { 0x51C8, 0 }, LABEL_EXPECTED);
        // TODO: test with a built-in function symbol
        //addDataItem(" DBF STRLEN", new short[] { 0x51C8, 0 }, LABEL_EXPECTED);
        addDataItem(" DBF D1,0", new short[] { 0x51C9, -2 });
        addDataItem(" DBF D7,0", new short[] { 0x51CF, -2 });
        addDataItem(" DBF A0,0", new short[] { 0x51C8, -2 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DBF (A0),0", new short[] { 0x51C8, -2 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DBF. D0,0", new short[] { 0x51C8, -2 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" DBF.B D0,0", new short[] { 0x51C8, -2 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" DBF.W D0,0", new short[] { 0x51C8, -2 });
        addDataItem(" DBF.W D0,1", new short[] { 0x51C8, -1 });
        addDataItem(" DBF.W D0,2", new short[] { 0x51C8, 0 });
        addDataItem(" DBF.W D0,$8000", new short[] { 0x51C8, 0x7FFE });
        addDataItem(" DBF.W D0,$8002", new short[] { 0x51C8, -0x8000 }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" DBF.W D0,-$8000", new short[] { 0x51C8, 0x7FFE }, BRANCH_TARGET_OUT_OF_RANGE);
        addDataItem(" DBF.W D0,-$7FFE", new short[] { 0x51C8, -0x8000 });
        addDataItem(" DBF.L D0,0", new short[] { 0x51C8, -2 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" DBF.Z D0,0", new short[] { 0x51C8, -2 }, INVALID_SIZE_ATTRIBUTE_Z);

        // DBGE
        addDataItem("SELF: DBGE D0,SELF", new short[] { 0x5CC8, -2 });
        // --> see DBF for more tests

        // DBGT
        addDataItem("SELF: DBGT D0,SELF", new short[] { 0x5EC8, -2 });
        // --> see DBF for more tests

        // DBHI
        addDataItem("SELF: DBHI D0,SELF", new short[] { 0x52C8, -2 });
        // --> see DBF for more tests

        // DBHS
        addDataItem("SELF: DBHS D0,SELF", new short[] { 0x54C8, -2 });
        // --> see DBF for more tests

        // DBLE
        addDataItem("SELF: DBLE D0,SELF", new short[] { 0x5FC8, -2 });
        // --> see DBF for more tests

        // DBLO
        addDataItem("SELF: DBLO D0,SELF", new short[] { 0x55C8, -2 });
        // --> see DBF for more tests

        // DBLS
        addDataItem("SELF: DBLS D0,SELF", new short[] { 0x53C8, -2 });
        // --> see DBF for more tests

        // DBLT
        addDataItem("SELF: DBLT D0,SELF", new short[] { 0x5DC8, -2 });
        // --> see DBF for more tests

        // DBMI
        addDataItem("SELF: DBMI D0,SELF", new short[] { 0x5BC8, -2 });
        // --> see DBF for more tests

        // DBNE
        addDataItem("SELF: DBNE D0,SELF", new short[] { 0x56C8, -2 });
        // --> see DBF for more tests

        // DBPL
        addDataItem("SELF: DBPL D0,SELF", new short[] { 0x5AC8, -2 });
        // --> see DBF for more tests

        // DBRA
        addDataItem("SELF: DBRA D0,SELF", new short[] { 0x51C8, -2 });
        // --> see DBF for more tests

        // DBT
        addDataItem("SELF: DBT D0,SELF", new short[] { 0x50C8, -2 });
        // --> see DBF for more tests

        // DBVC
        addDataItem("SELF: DBVC D0,SELF", new short[] { 0x58C8, -2 });
        // --> see DBF for more tests

        // DBVS
        addDataItem("SELF: DBVS D0,SELF", new short[] { 0x59C8, -2 });
        // --> see DBF for more tests

        // DIVS
        addDataItem(" DIVS D1,D0", new short[] { (short) 0x81C1 });
        addDataItem(" DIVS.W D1,D0", new short[] { (short) 0x81C1 });
        addDataItem(" DIVS.L D1,D0", new short[] { 0x4C41, 0x0800 }, M68KArchitecture.CPU32);
        addDataItem(" DIVS.L D2,D1:D0", new short[] { 0x4C42, 0x0C01 }, M68KArchitecture.CPU32);
        // --> see DIVU for more tests

        // DIVSL
        addDataItem(" DIVSL D2,D1:D0", new short[] { 0x4C42, 0x0801 }, M68KArchitecture.CPU32);
        // --> see DIVUL for more tests

        // DIVU
        addDataItem(" DIVU", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DIVU D1", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DIVU D1,D0", new short[] { (short) 0x80C1 });
        addDataItem(" DIVU D0,D1", new short[] { (short) 0x82C0 });
        addDataItem(" DIVU D0,D7", new short[] { (short) 0x8EC0 });
        addDataItem(" DIVU A0,D0", new short[] { (short) 0x80C8 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU (A0),D0", new short[] { (short) 0x80D0 });
        addDataItem(" DIVU D1,A0", new short[] { (short) 0x80C1 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU D1,(A0)", new short[] { (short) 0x80C1 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU D2,D1:D0", new short[] { (short) 0x82C2 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU. D1,D0", new short[] { (short) 0x80C1 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" DIVU.B D1,D0", new short[] { (short) 0x80C1 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" DIVU.W D1,D0", new short[] { (short) 0x80C1 });
        addDataItem(" DIVU.L D1,D0", new short[] { 0x4C41, 0x0000 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" DIVU.L D1,D0", new short[] { 0x4C41, 0x0000 }, M68KArchitecture.CPU32);
        addDataItem(" DIVU.L D1,D0", new short[] { 0x4C41, 0x0000 }, M68KArchitecture.MC68020);
        addDataItem(" DIVU.L D0,D1", new short[] { 0x4C40, 0x1001 }, M68KArchitecture.CPU32);
        addDataItem(" DIVU.L D0,D7", new short[] { 0x4C40, 0x7007 }, M68KArchitecture.CPU32);
        addDataItem(" DIVU.L A0,D0", new short[] { 0x4C48, 0x0000 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU.L (A0),D0", new short[] { 0x4C50, 0x0000 }, M68KArchitecture.CPU32);
        addDataItem(" DIVU.L D2,D1:D0", new short[] { 0x4C42, 0x0401 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" DIVU.L D2,D1:D0", new short[] { 0x4C42, 0x0401 }, M68KArchitecture.CPU32);
        addDataItem(" DIVU.L D2,D1:D0", new short[] { 0x4C42, 0x0401 }, M68KArchitecture.MC68020);
        addDataItem(" DIVU.L D2,D1 : D0", new short[] { 0x4C42, 0x0401 }, M68KArchitecture.MC68020);
        addDataItem(" DIVU.L D2,D0:D0", new short[] { 0x4C42, 0x0400 }, M68KArchitecture.CPU32,
                new DivisionRemainderDiscardedWarningMessage());
        addDataItem(" DIVU.L D2,D0:D1", new short[] { 0x4C42, 0x1400 }, M68KArchitecture.CPU32);
        addDataItem(" DIVU.L D0,D4:D7", new short[] { 0x4C40, 0x7404 }, M68KArchitecture.CPU32);
        addDataItem(" DIVU.L A0,D1:D0", new short[] { 0x4C48, 0x0401 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU.L (A0),D1:D0", new short[] { 0x4C50, 0x0401 }, M68KArchitecture.CPU32);
        addDataItem(" DIVU.L D2,A0:D0", new short[] { 0x4C42, 0x0400 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU.L D2,D0:A0", new short[] { 0x4C42, 0x0400 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU.L D2,D0:(A0)", new short[] { 0x4C42, 0x0400 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU.L D2,D1-D0", new short[] { 0x4C42, 0x1001 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVU.Z D1,D0", new short[] { (short) 0x80C1 }, INVALID_SIZE_ATTRIBUTE_Z);

        // DIVUL
        addDataItem(" DIVUL", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DIVUL D1", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DIVUL D2,D1", new short[] { 0x4C42, 0x1001 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVUL D2,D1:D0", new short[] { 0x4C42, 0x0001 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" DIVUL D2,D1:D0", new short[] { 0x4C42, 0x0001 }, M68KArchitecture.CPU32);
        addDataItem(" DIVUL D2,D1:D0", new short[] { 0x4C42, 0x0001 }, M68KArchitecture.MC68020);
        addDataItem(" DIVUL D2,D0:D0", new short[] { 0x4C42, 0x0000 }, M68KArchitecture.CPU32,
                new DivisionRemainderDiscardedWarningMessage());
        addDataItem(" DIVUL D2,D0:D1", new short[] { 0x4C42, 0x1000 }, M68KArchitecture.CPU32);
        addDataItem(" DIVUL D0,D4:D7", new short[] { 0x4C40, 0x7004 }, M68KArchitecture.CPU32);
        addDataItem(" DIVUL D2,A0:D0", new short[] { 0x4C42, 0x0000 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVUL D2,D0:A0", new short[] { 0x4C42, 0x0000 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVUL D2,D0:(A0)", new short[] { 0x4C42, 0x0000 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVUL A0,D1:D0", new short[] { 0x4C48, 0x0001 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" DIVUL (A0),D1:D0", new short[] { 0x4C50, 0x0001 }, M68KArchitecture.CPU32);
        addDataItem(" DIVUL. D2,D1:D0", new short[] { 0x4C42, 0x0001 }, M68KArchitecture.CPU32, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" DIVUL.B D2,D1:D0", new short[] { 0x4C42, 0x0001 }, M68KArchitecture.CPU32, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" DIVUL.W D2,D1:D0", new short[] { 0x4C42, 0x0001 }, M68KArchitecture.CPU32, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" DIVUL.L D2,D1:D0", new short[] { 0x4C42, 0x0001 }, M68KArchitecture.CPU32);
        addDataItem(" DIVUL.Z D2,D1:D0", new short[] { 0x4C42, 0x0001 }, M68KArchitecture.CPU32, INVALID_SIZE_ATTRIBUTE_Z);

        // EOR
        addDataItem(" EOR", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" EOR D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" EOR D0,D0", new short[] { (short) 0xB140 });
        addDataItem(" EOR D0,D7", new short[] { (short) 0xB147 });
        addDataItem(" EOR D7,D0", new short[] { (short) 0xBF40 });
        addDataItem(" EOR A0,D0", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EOR (A0),D0", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EOR #$1234,D0", new short[] { 0x0A40, 0x1234 });
        addDataItem(" EOR D0,A0", new short[] { (short) 0xB148 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EOR D0,(A0)", new short[] { (short) 0xB150 });
        addDataItem(" EOR #$1234,(A0)", new short[] { 0x0A50, 0x1234 });
        addDataItem(" EOR (A0),(A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EOR D0,CCR", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EOR #$12,CCR", new short[] { 0x0A3C, 0x0012 });
        addDataItem(" EOR #$1234,SR", new short[] { 0x0A7C, 0x1234 });
        addDataItem(" EOR. D0,D0", new short[] { (short) 0xB140 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" EOR.B D0,D0", new short[] { (short) 0xB100 });
        addDataItem(" EOR.B D0,(A0)", new short[] { (short) 0xB110 });
        addDataItem(" EOR.B #$12,CCR", new short[] { 0x0A3C, 0x0012 });
        addDataItem(" EOR.B #$1234,SR", new short[] { 0x0A7C, 0x1234 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" EOR.W D0,D0", new short[] { (short) 0xB140 });
        addDataItem(" EOR.W D0,(A0)", new short[] { (short) 0xB150 });
        addDataItem(" EOR.W #$12,CCR", new short[] { (short) 0x0A3C, 0x0012 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" EOR.W #$1234,SR", new short[] { (short) 0x0A7C, 0x1234 });
        addDataItem(" EOR.L D0,D0", new short[] { (short) 0xB180 });
        addDataItem(" EOR.L D0,(A0)", new short[] { (short) 0xB190 });
        addDataItem(" EOR.L #$12,CCR", new short[] { 0x0A3C, 0x0012 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" EOR.L #$1234,SR", new short[] { 0x0A7C, 0x1234 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" EOR.Z D0,D0", new short[] { (short) 0xB140 }, INVALID_SIZE_ATTRIBUTE_Z);

        // EORI
        addDataItem(" EORI #$1234,D0", new short[] { 0x0A40, 0x1234 });
        // --> see ANDI for more tests

        // EXG
        addDataItem(" EXG", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" EXG D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" EXG D0,D0", new short[] { (short) 0xC140 });
        addDataItem(" EXG D0,D1", new short[] { (short) 0xC141 });
        addDataItem(" EXG D0,D7", new short[] { (short) 0xC147 });
        addDataItem(" EXG D1,D0", new short[] { (short) 0xC340 });
        addDataItem(" EXG D7,D0", new short[] { (short) 0xCF40 });
        addDataItem(" EXG D0,A0", new short[] { (short) 0xC188 });
        addDataItem(" EXG D0,A1", new short[] { (short) 0xC189 });
        addDataItem(" EXG D0,A7", new short[] { (short) 0xC18F });
        addDataItem(" EXG D1,A0", new short[] { (short) 0xC388 });
        addDataItem(" EXG D7,A0", new short[] { (short) 0xCF88 });
        addDataItem(" EXG D0,(A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EXG A0,D0", new short[] { (short) 0xC188 });
        addDataItem(" EXG A0,D1", new short[] { (short) 0xC388 });
        addDataItem(" EXG A0,D7", new short[] { (short) 0xCF88 });
        addDataItem(" EXG A1,D0", new short[] { (short) 0xC189 });
        addDataItem(" EXG A7,D0", new short[] { (short) 0xC18F });
        addDataItem(" EXG A0,A0", new short[] { (short) 0xC148 });
        addDataItem(" EXG A0,A1", new short[] { (short) 0xC149 });
        addDataItem(" EXG A0,A7", new short[] { (short) 0xC14F });
        addDataItem(" EXG A1,A0", new short[] { (short) 0xC348 });
        addDataItem(" EXG A7,A0", new short[] { (short) 0xCF48 });
        addDataItem(" EXG A0,(A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EXG (A0),D0", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EXG. D0,D0", new short[] { (short) 0xC140 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" EXG.B D0,D0", new short[] { (short) 0xC140 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" EXG.W D0,D0", new short[] { (short) 0xC140 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" EXG.L D0,D0", new short[] { (short) 0xC140 });
        addDataItem(" EXG.Z D0,D0", new short[] { (short) 0xC140 }, INVALID_SIZE_ATTRIBUTE_Z);

        // EXT
        addDataItem(" EXT", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" EXT D0", new short[] { 0x4880 });
        addDataItem(" EXT D7", new short[] { 0x4887 });
        addDataItem(" EXT A7", new short[] { 0x4880 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EXT. D0", new short[] { 0x4880 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" EXT.B D0", new short[] { 0x4880 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" EXT.W D0", new short[] { 0x4880 });
        addDataItem(" EXT.L D0", new short[] { 0x48C0 });
        addDataItem(" EXT.Z D0", new short[] { 0x4880 }, INVALID_SIZE_ATTRIBUTE_Z);

        // EXTB
        addDataItem(" EXTB", NO_DATA, M68KArchitecture.CPU32, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" EXTB D0", new short[] { 0x49C0 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" EXTB D0", new short[] { 0x49C0 }, M68KArchitecture.CPU32);
        addDataItem(" EXTB D0", new short[] { 0x49C0 }, M68KArchitecture.MC68020);
        addDataItem(" EXTB D7", new short[] { 0x49C7 }, M68KArchitecture.CPU32);
        addDataItem(" EXTB A7", new short[] { 0x49C0 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" EXTB. D0", new short[] { 0x49C0 }, M68KArchitecture.CPU32, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" EXTB.B D0", new short[] { 0x49C0 }, M68KArchitecture.CPU32, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" EXTB.W D0", new short[] { 0x49C0 }, M68KArchitecture.CPU32, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" EXTB.L D0", new short[] { 0x49C0 }, M68KArchitecture.CPU32);
        addDataItem(" EXTB.Z D0", new short[] { 0x49C0 }, M68KArchitecture.CPU32, INVALID_SIZE_ATTRIBUTE_Z);

        // ILLEGAL
        addDataItem(" ILLEGAL", new short[] { 0x4AFC });
        // --> see NOP for more tests

        // JMP
        addDataItem(" JMP", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" JMP D0", new short[] { 0x4EC0 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" JMP (A0)", new short[] { 0x4ED0 });
        addDataItem(" JMP ($400).W", new short[] { 0x4EF8, 0x0400 });
        addDataItem(" JMP ($123456).L", new short[] { 0x4EF9, 0x0012, 0x3456 });
        addDataItem(" JMP. (A0)", new short[] { 0x4ED0 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" JMP.B (A0)", new short[] { 0x4ED0 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" JMP.W (A0)", new short[] { 0x4ED0 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" JMP.L (A0)", new short[] { 0x4ED0 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" JMP.Z (A0)", new short[] { 0x4ED0 }, INVALID_SIZE_ATTRIBUTE_Z);

        // JSR
        addDataItem(" JSR (A0)", new short[] { 0x4E90 });
        // --> see JMP for more tests

        // LEA
        addDataItem(" LEA", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" LEA ($1234).W", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" LEA $1234(A7),A0", new short[] { 0x41EF, 0x1234 });
        addDataItem(" LEA ($1234).W,A0", new short[] { 0x41F8, 0x1234 });
        addDataItem(" LEA D0,A0", new short[] { 0x41C0 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" LEA (A0)+,A0", new short[] { 0x41D8 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" LEA ($1234).W,D0", new short[] { 0x41F8, 0x1234 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" LEA. ($1234).W,A0", new short[] { 0x41F8, 0x1234 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" LEA.B ($1234).W,A0", new short[] { 0x41F8, 0x1234 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" LEA.W ($1234).W,A0", new short[] { 0x41F8, 0x1234 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" LEA.L ($1234).W,A0", new short[] { 0x41F8, 0x1234 });
        addDataItem(" LEA.Z ($1234).W,A0", new short[] { 0x41F8, 0x1234 }, INVALID_SIZE_ATTRIBUTE_Z);

        // LINK
        addDataItem(" LINK", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" LINK A0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" LINK A0,#0", new short[] { 0x4E50, 0x0000 });
        addDataItem(" LINK A0,#-2", new short[] { 0x4E50, (short) 0xFFFE });
        addDataItem(" LINK A0,#2", new short[] { 0x4E50, 0x0002 });
        addDataItem(" LINK A0,#-$8000", new short[] { 0x4E50, (short) 0x8000 });
        addDataItem(" LINK A0,#-$8001", new short[] { 0x4E50, 0x7FFF }, new ValueOutOfRangeErrorMessage(-0x8001));
        addDataItem(" LINK A0,#$FFFF", new short[] { 0x4E50, (short) 0xFFFF });
        addDataItem(" LINK A0,#$10000", new short[] { 0x4E50, 0 }, new ValueOutOfRangeErrorMessage(0x10000));
        addDataItem(" LINK A1,#0", new short[] { 0x4E51, 0x0000 });
        addDataItem(" LINK A6,#0", new short[] { 0x4E56, 0x0000 });
        addDataItem(" LINK A7,#0", new short[] { 0x4E57, 0x0000 });
        addDataItem(" LINK D0,#0", new short[] { 0x4E50, 0x0000 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" LINK. A0,#0", new short[] { 0x4E50, 0x0000 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" LINK.B A0,#0", new short[] { 0x4E50, 0x0000 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" LINK.W A0,#0", new short[] { 0x4E50, 0x0000 });
        addDataItem(" LINK.L A0,#0", new short[] { 0x4808, 0x0000, 0x0000 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" LINK.L A0,#0", new short[] { 0x4808, 0x0000, 0x0000 }, M68KArchitecture.CPU32);
        addDataItem(" LINK.L A0,#0", new short[] { 0x4808, 0x0000, 0x0000 }, M68KArchitecture.MC68020);
        addDataItem(" LINK.L A0,#-2", new short[] { 0x4808, (short) 0xFFFF, (short) 0xFFFE }, M68KArchitecture.CPU32);
        addDataItem(" LINK.L A0,#2", new short[] { 0x4808, 0x0000, 0x0002 }, M68KArchitecture.CPU32);
        addDataItem(" LINK.L A0,#-$8000", new short[] { 0x4808, (short) 0xFFFF, (short) 0x8000 }, M68KArchitecture.CPU32);
        addDataItem(" LINK.L A0,#-$8001", new short[] { 0x4808, (short) 0xFFFF, 0x7FFF }, M68KArchitecture.CPU32);
        addDataItem(" LINK.L A0,#-$80000000", new short[] { 0x4808, (short) 0x8000, 0 }, M68KArchitecture.CPU32);
        addDataItem(" LINK.L A0,#$FFFF", new short[] { 0x4808, 0, (short) 0xFFFF }, M68KArchitecture.CPU32);
        addDataItem(" LINK.L A0,#$10000", new short[] { 0x4808, 1, 0 }, M68KArchitecture.CPU32);
        addDataItem(" LINK.L A0,#$FFFFFFFF", new short[] { 0x4808, (short) 0xFFFF, (short) 0xFFFF }, M68KArchitecture.CPU32);
        addDataItem(" LINK.Z A0,#0", new short[] { 0x4E50, 0x0000 }, INVALID_SIZE_ATTRIBUTE_Z);

        // LPSTOP
        addDataItem(" LPSTOP", NO_DATA, M68KArchitecture.CPU32, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" LPSTOP #$1234", new short[] { (short) 0xF800, 0x01C0, 0x1234 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" LPSTOP #$1234", new short[] { (short) 0xF800, 0x01C0, 0x1234 }, M68KArchitecture.CPU32);
        addDataItem(" LPSTOP #$1234", new short[] { (short) 0xF800, 0x01C0, 0x1234 }, M68KArchitecture.MC68020,
                NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" LPSTOP. #$1234", new short[] { (short) 0xF800, 0x01C0, 0x1234 }, M68KArchitecture.CPU32,
                INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" LPSTOP.B #$1234", new short[] { (short) 0xF800, 0x01C0, 0x0034 }, M68KArchitecture.CPU32,
                INVALID_SIZE_ATTRIBUTE_B, new ValueOutOfRangeErrorMessage(0x1234));
        addDataItem(" LPSTOP.W #$1234", new short[] { (short) 0xF800, 0x01C0, 0x1234 }, M68KArchitecture.CPU32,
                INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" LPSTOP.L #$1234", new short[] { (short) 0xF800, 0x01C0, 0x1234 }, M68KArchitecture.CPU32,
                INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" LPSTOP.Z #$1234", new short[] { (short) 0xF800, 0x01C0, 0x1234 }, M68KArchitecture.CPU32,
                INVALID_SIZE_ATTRIBUTE_Z);

        // LSL
        addDataItem(" LSL D0", new short[] { (short) 0xE348 });
        addDataItem(" LSL #1,D0", new short[] { (short) 0xE348 });
        addDataItem(" LSL D0,D0", new short[] { (short) 0xE168 });
        addDataItem(" LSL (A0)", new short[] { (short) 0xE3D0 });
        // --> see ASR for more tests

        // LSR
        addDataItem(" LSR D0", new short[] { (short) 0xE248 });
        addDataItem(" LSR #1,D0", new short[] { (short) 0xE248 });
        addDataItem(" LSR D0,D0", new short[] { (short) 0xE068 });
        addDataItem(" LSR (A0)", new short[] { (short) 0xE2D0 });
        // --> see ASR for more tests

        // MOVE
        // - broken MOVE
        addDataItem(" MOVE", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MOVE D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        // - MOVE
        addDataItem(" MOVE D0,D0", new short[] { 0x3000 });
        addDataItem(" MOVE A0,D0", new short[] { 0x3008 });
        addDataItem(" MOVE D0,A0", new short[] { 0x3040 });
        addDataItem("S: MOVE D0,S", new short[] { 0x31C0, 0x0000 });
        addDataItem("SRQ: MOVE D0,SRQ", new short[] { 0x31C0, 0x0000 });
        addDataItem(" MOVE (A1)+,-(A2)", new short[] { 0x3519 });
        addDataItem(" MOVE. (A1)+,-(A2)", new short[] { 0x3519 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" MOVE.B (A1)+,-(A2)", new short[] { 0x1519 });
        addDataItem(" MOVE.B A0,D0", new short[] { 0x1008 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" MOVE.B D0,A0", new short[] { 0x1040 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" MOVE.W (A1)+,-(A2)", new short[] { 0x3519 });
        addDataItem(" MOVE.L (A1)+,-(A2)", new short[] { 0x2519 });
        addDataItem(" MOVE.L #0,D0", new short[] { 0x203C, 0x0000, 0x0000 });
        addDataItem(" MOVE.Z (A1)+,-(A2)", new short[] { 0x3519 }, INVALID_SIZE_ATTRIBUTE_Z);
        // - MOVE from CCR
        addDataItem(" MOVE CCR,2(A0)", new short[] { 0x42E8, 0x0002 });
        addDataItem(" MOVE ccr,2(A0)", new short[] { 0x42E8, 0x0002 });
        addDataItem(" MOVE. CCR,2(A0)", new short[] { 0x42E8, 0x0002 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" MOVE.B CCR,2(A0)", new short[] { 0x42E8, 0x0002 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" MOVE.W CCR,2(A0)", new short[] { 0x42E8, 0x0002 });
        addDataItem(" MOVE.L CCR,2(A0)", new short[] { 0x42E8, 0x0002 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" MOVE.Z CCR,2(A0)", new short[] { 0x42E8, 0x0002 }, INVALID_SIZE_ATTRIBUTE_Z);
        // - MOVE from SR
        addDataItem(" MOVE SR,2(A0)", new short[] { 0x40E8, 0x0002 });
        addDataItem(" MOVE sr,2(A0)", new short[] { 0x40E8, 0x0002 });
        addDataItem(" MOVE.W SR,2(A0)", new short[] { 0x40E8, 0x0002 });
        // - MOVE to CCR
        addDataItem(" MOVE #$1F,CCR", new short[] { 0x44FC, 0x001F });
        addDataItem(" MOVE #$1F,ccr", new short[] { 0x44FC, 0x001F });
        addDataItem(" MOVE.W #$1F,CCR", new short[] { 0x44FC, 0x001F });
        // - MOVE to SR
        addDataItem(" MOVE #$E71F,SR", new short[] { 0x46FC, (short) 0xE71F });
        addDataItem(" MOVE #$E71F,sr", new short[] { 0x46FC, (short) 0xE71F });
        addDataItem(" MOVE.W #$E71F,SR", new short[] { 0x46FC, (short) 0xE71F });
        // - MOVE USP
        addDataItem(" MOVE USP,A0", new short[] { 0x4E68 });
        addDataItem(" MOVE USP,A7", new short[] { 0x4E6F });
        addDataItem(" MOVE A0,USP", new short[] { 0x4E60 });
        addDataItem(" MOVE A7,USP", new short[] { 0x4E67 });
        addDataItem(" MOVE (2).W,USP", new short[] { 0x4E60 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MOVE usp,A0", new short[] { 0x4E68 });
        addDataItem(" MOVE. USP,A0", new short[] { 0x4E68 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" MOVE.B USP,A0", new short[] { 0x4E68 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" MOVE.W USP,A0", new short[] { 0x4E68 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" MOVE.L USP,A0", new short[] { 0x4E68 });
        addDataItem(" MOVE.Z USP,A0", new short[] { 0x4E68 }, INVALID_SIZE_ATTRIBUTE_Z);
        // --> see also OptimizeMoveToMoveqTest for tests with the "optimizeMoveToMoveq" option enabled

        // MOVEA
        addDataItem(" MOVEA", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MOVEA D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MOVEA D0,A0", new short[] { 0x3040 });
        addDataItem(" MOVEA D0,D1", new short[] { 0x3200 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MOVEA. D0,A0", new short[] { 0x3040 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" MOVEA.B D0,A0", new short[] { 0x1040 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" MOVEA.W D0,A0", new short[] { 0x3040 });
        addDataItem(" MOVEA.L D0,A0", new short[] { 0x2040 });
        addDataItem(" MOVEA.Z D0,A0", new short[] { 0x3040 }, INVALID_SIZE_ATTRIBUTE_Z);

        // MOVEM
        addDataItem(" MOVEM", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MOVEM D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MOVEM D0,D0", new short[] { 0x4880, 0x0001 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MOVEM D0,(A0)", new short[] { 0x4890, 0x0001 });
        addDataItem(" MOVEM D1,(A0)", new short[] { 0x4890, 0x0002 });
        addDataItem(" MOVEM D7,(A0)", new short[] { 0x4890, 0x0080 });
        addDataItem(" MOVEM A0,(A0)", new short[] { 0x4890, 0x0100 });
        addDataItem(" MOVEM A1,(A0)", new short[] { 0x4890, 0x0200 });
        addDataItem(" MOVEM A7,(A0)", new short[] { 0x4890, (short) 0x8000 });
        addDataItem(" MOVEM SP,(A0)", new short[] { 0x4890, (short) 0x8000 });
        addDataItem(" MOVEM D0-D3,(A0)", new short[] { 0x4890, 0x000F });
        addDataItem(" MOVEM D0 - D3,(A0)", new short[] { 0x4890, 0x000F });
        addDataItem(" MOVEM D3-D0,(A0)", new short[] { 0x4890, 0x000F });
        addDataItem(" MOVEM D0/D3,(A0)", new short[] { 0x4890, 0x0009 });
        addDataItem(" MOVEM D0 / D3,(A0)", new short[] { 0x4890, 0x0009 });
        addDataItem(" MOVEM D0-D4/D3-D6,(A0)", new short[] { 0x4890, 0x007F }, new DuplicateRegistersInRegisterListWarningMessage());
        addDataItem(" MOVEM D0-D4/A0-A2,(A0)", new short[] { 0x4890, 0x071F });
        addDataItem(" MOVEM D0 - D4 /\tA0 - A2,(A0)", new short[] { 0x4890, 0x071F });
        addDataItem(" MOVEM D0,-(A0)", new short[] { 0x48A0, (short) 0x8000 });
        addDataItem(" MOVEM D0-D4/A0-A2,-(A0)", new short[] { 0x48A0, (short) 0xF8E0 });
        addDataItem(" MOVEM (A0),D0", new short[] { 0x4C90, 0x0001 });
        addDataItem(" MOVEM (A0),D0-D4/A0-A2", new short[] { 0x4C90, 0x071F });
        addDataItem(" MOVEM (A0),D0 - D4 / A0 - A2", new short[] { 0x4C90, 0x071F });
        addDataItem(" MOVEM (A0)+,D0", new short[] { 0x4C98, 0x0001 });
        addDataItem(" MOVEM (A0),(A0)", NO_DATA, MOVEM_REQUIRES_A_REGISTER_LIST_IN_ONE_OPERAND);
        addDataItem(" MOVEM D0-,(A0)", NO_DATA, MOVEM_REQUIRES_A_REGISTER_LIST_IN_ONE_OPERAND);
        addDataItem(" MOVEM D0/,(A0)", NO_DATA, MOVEM_REQUIRES_A_REGISTER_LIST_IN_ONE_OPERAND);
        addDataItem(" MOVEM D0~,(A0)", NO_DATA, MOVEM_REQUIRES_A_REGISTER_LIST_IN_ONE_OPERAND);
        addDataItem("FOO: MOVEM FOO.W,D0-A6", new short[] { 0x4CB8, 0x7FFF, 0x0000 });
        addDataItem(" MOVEM. D0,(A0)", new short[] { 0x4890, 0x0001 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" MOVEM.B D0,(A0)", new short[] { 0x4890, 0x0001 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" MOVEM.W D0,(A0)", new short[] { 0x4890, 0x0001 });
        addDataItem(" MOVEM.L D0,(A0)", new short[] { 0x48D0, 0x0001 });
        addDataItem(" MOVEM.Z D0,(A0)", new short[] { 0x4890, 0x0001 }, INVALID_SIZE_ATTRIBUTE_Z);

        // MOVEP
        addDataItem(" MOVEP", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MOVEP D0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MOVEP D0,D0", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MOVEP (A0),D0", new short[] { 0x0108, 0x0000 });
        addDataItem(" MOVEP (0,A0),D0", new short[] { 0x0108, 0x0000 });
        addDataItem(" MOVEP (0,A1),D0", new short[] { 0x0109, 0x0000 });
        addDataItem(" MOVEP (0,A7),D0", new short[] { 0x010F, 0x0000 });
        addDataItem(" MOVEP (0,A0),D1", new short[] { 0x0308, 0x0000 });
        addDataItem(" MOVEP (0,A0),D7", new short[] { 0x0F08, 0x0000 });
        addDataItem(" MOVEP ($1234,A0),D0", new short[] { 0x0108, 0x1234 });
        addDataItem(" MOVEP ($12345678,A0),D0", NO_DATA, M68KArchitecture.MC68020, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MOVEP D0,(A0)", new short[] { 0x0188, 0x0000 });
        addDataItem(" MOVEP D0,(0,A0)", new short[] { 0x0188, 0x0000 });
        addDataItem(" MOVEP D0,(0,A1)", new short[] { 0x0189, 0x0000 });
        addDataItem(" MOVEP D0,(0,A7)", new short[] { 0x018F, 0x0000 });
        addDataItem(" MOVEP D1,(0,A0)", new short[] { 0x0388, 0x0000 });
        addDataItem(" MOVEP D7,(0,A0)", new short[] { 0x0F88, 0x0000 });
        addDataItem(" MOVEP D0,($1234,A0)", new short[] { 0x0188, 0x1234 });
        addDataItem(" MOVEP D0,($12345678,A0)", NO_DATA, M68KArchitecture.MC68020, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MOVEP (0,A0),(0,A0)", NO_DATA, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MOVEP. (A0),D0", new short[] { 0x0108, 0x0000 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" MOVEP.B (A0),D0", new short[] { 0x0108, 0x0000 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" MOVEP.W (A0),D0", new short[] { 0x0108, 0x0000 });
        addDataItem(" MOVEP.L (A0),D0", new short[] { 0x0148, 0x0000 });
        addDataItem(" MOVEP.Z (A0),D0", new short[] { 0x0108, 0x0000 }, INVALID_SIZE_ATTRIBUTE_Z);

        // MOVEQ
        addDataItem(" MOVEQ", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MOVEQ #0", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MOVEQ #0,D0", new short[] { 0x7000 });
        addDataItem(" MOVEQ #$7F,D0", new short[] { 0x707F });
        addDataItem(" MOVEQ #-$80,D0", new short[] { 0x7080 });
        addDataItem(" MOVEQ #-1,D0", new short[] { 0x70FF });
        addDataItem(" MOVEQ #$FFFFFF80,D0", new short[] { 0x7080 });
        addDataItem(" MOVEQ #$FFFFFFFF,D0", new short[] { 0x70FF });
        addDataItem(" MOVEQ #$80,D0", new short[] { 0x7080 }, DATA_FOR_MOVEQ_WILL_BE_SIGN_EXTENDED);
        addDataItem(" MOVEQ #$FF,D0", new short[] { 0x70FF }, DATA_FOR_MOVEQ_WILL_BE_SIGN_EXTENDED);
        addDataItem(" MOVEQ #$100,D0", new short[] { 0x7000 }, DATA_FOR_MOVEQ_OUT_OF_RANGE);
        addDataItem(" MOVEQ #-$81,D0", new short[] { 0x707F }, DATA_FOR_MOVEQ_OUT_OF_RANGE);
        addDataItem(" MOVEQ #$FFFFFF7F,D0", new short[] { 0x707F }, DATA_FOR_MOVEQ_OUT_OF_RANGE);
        addDataItem(" MOVEQ. #0,D0", new short[] { 0x7000 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" MOVEQ D1,D0", new short[] { 0x7000 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MOVEQ #0,A0", new short[] { 0x7000 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MOVEQ.B #0,D0", new short[] { 0x7000 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" MOVEQ.W #0,D0", new short[] { 0x7000 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" MOVEQ.L #0,D0", new short[] { 0x7000 });
        addDataItem(" MOVEQ.Z #0,D0", new short[] { 0x7000 }, INVALID_SIZE_ATTRIBUTE_Z);

        // MULS
        addDataItem(" MULS D1,D0", new short[] { (short) 0xC1C1 });
        addDataItem(" MULS.W D1,D0", new short[] { (short) 0xC1C1 });
        addDataItem(" MULS.L D1,D0", new short[] { 0x4C01, 0x0800 }, M68KArchitecture.CPU32);
        addDataItem(" MULS.L D2,D1-D0", new short[] { 0x4C02, 0x0C01 }, M68KArchitecture.CPU32);
        // --> see MULU for more tests

        // MULU
        addDataItem(" MULU", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MULU D1", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" MULU D1,D0", new short[] { (short) 0xC0C1 });
        addDataItem(" MULU D0,D1", new short[] { (short) 0xC2C0 });
        addDataItem(" MULU D0,D7", new short[] { (short) 0xCEC0 });
        addDataItem(" MULU A0,D0", new short[] { (short) 0xC0C8 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU (A0),D0", new short[] { (short) 0xC0D0 });
        addDataItem(" MULU D1,A0", new short[] { (short) 0xC0C1 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU D1,(A0)", new short[] { (short) 0xC0C1 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU D2,D1-D0", new short[] { (short) 0xC2C2 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU. D1,D0", new short[] { (short) 0xC0C1 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" MULU.B D1,D0", new short[] { (short) 0xC0C1 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" MULU.W D1,D0", new short[] { (short) 0xC0C1 });
        addDataItem(" MULU.L D1,D0", new short[] { 0x4C01, 0x0000 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" MULU.L D1,D0", new short[] { 0x4C01, 0x0000 }, M68KArchitecture.CPU32);
        addDataItem(" MULU.L D1,D0", new short[] { 0x4C01, 0x0000 }, M68KArchitecture.MC68020);
        addDataItem(" MULU.L D0,D1", new short[] { 0x4C00, 0x1001 }, M68KArchitecture.CPU32);
        addDataItem(" MULU.L D0,D7", new short[] { 0x4C00, 0x7007 }, M68KArchitecture.CPU32);
        addDataItem(" MULU.L A0,D0", new short[] { 0x4C08, 0x0000 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU.L (A0),D0", new short[] { 0x4C10, 0x0000 }, M68KArchitecture.CPU32);
        addDataItem(" MULU.L D2,D1-D0", new short[] { 0x4C02, 0x0401 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" MULU.L D2,D1-D0", new short[] { 0x4C02, 0x0401 }, M68KArchitecture.CPU32);
        addDataItem(" MULU.L D2,D1-D0", new short[] { 0x4C02, 0x0401 }, M68KArchitecture.MC68020);
        addDataItem(" MULU.L D2,D1 - D0", new short[] { 0x4C02, 0x0401 }, M68KArchitecture.MC68020);
        addDataItem(" MULU.L D2,D0-D0", new short[] { 0x4C02, 0x0400 }, M68KArchitecture.CPU32,
                new MultiplicationResultsUndefinedWarningMessage());
        addDataItem(" MULU.L D2,D0-D1", new short[] { 0x4C02, 0x1400 }, M68KArchitecture.CPU32);
        addDataItem(" MULU.L D0,D4-D7", new short[] { 0x4C00, 0x7404 }, M68KArchitecture.CPU32);
        addDataItem(" MULU.L A0,D1-D0", new short[] { 0x4C08, 0x0401 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU.L (A0),D1-D0", new short[] { 0x4C10, 0x0401 }, M68KArchitecture.CPU32);
        addDataItem(" MULU.L D2,A0-D0", new short[] { 0x4C02, 0x0400 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU.L D2,D0-A0", new short[] { 0x4C02, 0x0400 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU.L D2,D0-(A0)", new short[] { 0x4C02, 0x0400 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU.L D2,D1:D0", new short[] { 0x4C02, 0x1001 }, M68KArchitecture.CPU32, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" MULU.Z D1,D0", new short[] { (short) 0xC0C1 }, INVALID_SIZE_ATTRIBUTE_Z);

        // NBCD
        addDataItem(" NBCD", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" NBCD D0", new short[] { 0x4800 });
        addDataItem(" NBCD A0", new short[] { 0x4808 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" NBCD. D0", new short[] { 0x4800 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" NBCD.B D0", new short[] { 0x4800 });
        addDataItem(" NBCD.W D0", new short[] { 0x4800 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" NBCD.L D0", new short[] { 0x4800 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" NBCD.Z D0", new short[] { 0x4800 }, INVALID_SIZE_ATTRIBUTE_Z);

        // NEG
        addDataItem(" NEG D0", new short[] { 0x4440 });
        // --> see CLR for more tests

        // NEGX
        addDataItem(" NEGX D0", new short[] { 0x4040 });
        // --> see CLR for more tests

        // NOP
        addDataItem(" NOP", new short[] { 0x4E71 });
        addDataItem(" NOP 0", new short[] { 0x4E71 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" NOP 0,0", new short[] { 0x4E71 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" NOP 0,0,0", new short[] { 0x4E71 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" NOP.", new short[] { 0x4E71 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" NOP.B", new short[] { 0x4E71 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" NOP.W", new short[] { 0x4E71 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" NOP.L", new short[] { 0x4E71 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" NOP.Z", new short[] { 0x4E71 }, INVALID_SIZE_ATTRIBUTE_Z);

        // NOT
        addDataItem(" NOT D0", new short[] { 0x4640 });
        // --> see CLR for more tests

        // OR
        addDataItem(" OR D0,D0", new short[] { (short) 0x8040 });
        // --> see AND for more tests

        // ORI
        addDataItem(" ORI #$1234,D0", new short[] { 0x0040, 0x1234 });
        // --> see ANDI for more tests

        // PEA
        addDataItem(" PEA", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" PEA D0", new short[] { 0x4840 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" PEA (A0)", new short[] { 0x4850 });
        addDataItem(" PEA. (A0)", new short[] { 0x4850 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" PEA.B (A0)", new short[] { 0x4850 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" PEA.W (A0)", new short[] { 0x4850 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" PEA.L (A0)", new short[] { 0x4850 });
        addDataItem(" PEA.Z (A0)", new short[] { 0x4850 }, INVALID_SIZE_ATTRIBUTE_Z);

        // RESET
        addDataItem(" RESET", new short[] { 0x4E70 });
        // --> see NOP for more tests

        // ROL
        addDataItem(" ROL D0", new short[] { (short) 0xE358 });
        addDataItem(" ROL #1,D0", new short[] { (short) 0xE358 });
        addDataItem(" ROL D0,D0", new short[] { (short) 0xE178 });
        addDataItem(" ROL (A0)", new short[] { (short) 0xE7D0 });
        // --> see ASR for more tests

        // ROR
        addDataItem(" ROR D0", new short[] { (short) 0xE258 });
        addDataItem(" ROR #1,D0", new short[] { (short) 0xE258 });
        addDataItem(" ROR D0,D0", new short[] { (short) 0xE078 });
        addDataItem(" ROR (A0)", new short[] { (short) 0xE6D0 });
        // --> see ASR for more tests

        // ROXL
        addDataItem(" ROXL D0", new short[] { (short) 0xE350 });
        addDataItem(" ROXL #1,D0", new short[] { (short) 0xE350 });
        addDataItem(" ROXL D0,D0", new short[] { (short) 0xE170 });
        addDataItem(" ROXL (A0)", new short[] { (short) 0xE5D0 });
        // --> see ASR for more tests

        // ROXR
        addDataItem(" ROXR D0", new short[] { (short) 0xE250 });
        addDataItem(" ROXR #1,D0", new short[] { (short) 0xE250 });
        addDataItem(" ROXR D0,D0", new short[] { (short) 0xE070 });
        addDataItem(" ROXR (A0)", new short[] { (short) 0xE4D0 });
        // --> see ASR for more tests

        // RTD
        addDataItem(" RTD", NO_DATA, M68KArchitecture.MC68010, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" RTD #$1234", new short[] { (short) 0x4E74, 0x1234 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" RTD #$1234", new short[] { (short) 0x4E74, 0x1234 }, M68KArchitecture.MC68010);
        addDataItem(" RTD #$1234", new short[] { (short) 0x4E74, 0x1234 }, M68KArchitecture.MC68020);
        addDataItem(" RTD. #$1234", new short[] { (short) 0x4E74, 0x1234 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" RTD.B #$1234", new short[] { (short) 0x4E74, 0x0034 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_B,
                new ValueOutOfRangeErrorMessage(0x1234));
        addDataItem(" RTD.W #$1234", new short[] { (short) 0x4E74, 0x1234 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" RTD.L #$1234", new short[] { (short) 0x4E74, 0x1234 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" RTD.Z #$1234", new short[] { (short) 0x4E74, 0x1234 }, M68KArchitecture.MC68010, INVALID_SIZE_ATTRIBUTE_Z);

        // RTE
        addDataItem(" RTE", new short[] { 0x4E73 });
        // --> see NOP for more tests

        // RTM
        addDataItem(" RTM", NO_DATA, M68KArchitecture.MC68020, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" RTM D0", new short[] { 0x06C0 }, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" RTM D0", new short[] { 0x06C0 }, M68KArchitecture.MC68020);
        addDataItem(" RTM D0", new short[] { 0x06C0 }, M68KArchitecture.MC68030, NOT_SUPPORTED_ON_ARCHITECTURE);
        addDataItem(" RTM D7", new short[] { 0x06C7 }, M68KArchitecture.MC68020);
        addDataItem(" RTM A0", new short[] { 0x06C8 }, M68KArchitecture.MC68020);
        addDataItem(" RTM A7", new short[] { 0x06CF }, M68KArchitecture.MC68020);
        addDataItem(" RTM (A0)", new short[] { 0x06D0 }, M68KArchitecture.MC68020, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" RTM. D0", new short[] { 0x06C0 }, M68KArchitecture.MC68020, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" RTM.B D0", new short[] { 0x06C0 }, M68KArchitecture.MC68020, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" RTM.W D0", new short[] { 0x06C0 }, M68KArchitecture.MC68020, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" RTM.L D0", new short[] { 0x06C0 }, M68KArchitecture.MC68020, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" RTM.Z D0", new short[] { 0x06C0 }, M68KArchitecture.MC68020, INVALID_SIZE_ATTRIBUTE_Z);

        // RTR
        addDataItem(" RTR", new short[] { 0x4E77 });
        // --> see NOP for more tests

        // RTS
        addDataItem(" RTS", new short[] { 0x4E75 });
        // --> see NOP for more tests

        // SBCD
        addDataItem(" SBCD D0,D0", new short[] { (short) 0x8100 });
        // --> see ABCD for more tests

        // SCC
        addDataItem(" SCC D0", new short[] { 0x54C0 });
        // --> see ST for more tests

        // SCS
        addDataItem(" SCS D0", new short[] { 0x55C0 });
        // --> see ST for more tests

        // SEQ
        addDataItem(" SEQ D0", new short[] { 0x57C0 });
        // --> see ST for more tests

        // SF
        addDataItem(" SF D0", new short[] { 0x51C0 });
        // --> see ST for more tests

        // SGE
        addDataItem(" SGE D0", new short[] { 0x5CC0 });
        // --> see ST for more tests

        // SGT
        addDataItem(" SGT D0", new short[] { 0x5EC0 });
        // --> see ST for more tests

        // SHI
        addDataItem(" SHI D0", new short[] { 0x52C0 });
        // --> see ST for more tests

        // SHS
        addDataItem(" SHS D0", new short[] { 0x54C0 });
        // --> see ST for more tests

        // SLE
        addDataItem(" SLE D0", new short[] { 0x5FC0 });
        // --> see ST for more tests

        // SLO
        addDataItem(" SLO D0", new short[] { 0x55C0 });
        // --> see ST for more tests

        // SLS
        addDataItem(" SLS D0", new short[] { 0x53C0 });
        // --> see ST for more tests

        // SLT
        addDataItem(" SLT D0", new short[] { 0x5DC0 });
        // --> see ST for more tests

        // SMI
        addDataItem(" SMI D0", new short[] { 0x5BC0 });
        // --> see ST for more tests

        // SNE
        addDataItem(" SNE D0", new short[] { 0x56C0 });
        // --> see ST for more tests

        // SPL
        addDataItem(" SPL D0", new short[] { 0x5AC0 });
        // --> see ST for more tests

        // ST
        addDataItem(" ST", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ST D0", new short[] { 0x50C0 });
        addDataItem(" ST A0", new short[] { 0x50C8 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" ST (A0)", new short[] { 0x50D0 });
        addDataItem(" ST. D0", new short[] { 0x50C0 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" ST.B D0", new short[] { 0x50C0 });
        addDataItem(" ST.W D0", new short[] { 0x50C0 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" ST.L D0", new short[] { 0x50C0 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" ST.Z D0", new short[] { 0x50C0 }, INVALID_SIZE_ATTRIBUTE_Z);

        // STOP
        addDataItem(" STOP", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" STOP #$1234", new short[] { (short) 0x4E72, 0x1234 });
        addDataItem(" STOP. #$1234", new short[] { (short) 0x4E72, 0x1234 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" STOP.B #$1234", new short[] { (short) 0x4E72, 0x0034 }, INVALID_SIZE_ATTRIBUTE_B,
                new ValueOutOfRangeErrorMessage(0x1234));
        addDataItem(" STOP.W #$1234", new short[] { (short) 0x4E72, 0x1234 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" STOP.L #$1234", new short[] { (short) 0x4E72, 0x1234 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" STOP.Z #$1234", new short[] { (short) 0x4E72, 0x1234 }, INVALID_SIZE_ATTRIBUTE_Z);

        // SUB
        addDataItem(" SUB D0,D0", new short[] { (short) 0x9040 });
        addDataItem(" SUB D0,A0", new short[] { (short) 0x90C0 });
        addDataItem(" SUB #$1234,(A0)", new short[] { 0x0450, 0x1234 });
        // --> see ADD for more tests
        // --> see also OptimizeToAddqSubqTest for tests with the "optimizeToAddqSubq" option enabled

        // SUBA
        addDataItem(" SUBA D0,A0", new short[] { (short) 0x90C0 });
        // --> see ADDA for more tests
        // --> see also OptimizeToAddqSubqTest for tests with the "optimizeToAddqSubq" option enabled

        // SUBI
        addDataItem(" SUBI #$1234,D0", new short[] { 0x0440, 0x1234 });
        addDataItem(" SUBI #$1234,A0", new short[] { (short) 0x90FC, 0x1234 });
        // --> see ADDI for more tests
        // --> see also OptimizeToAddqSubqTest for tests with the "optimizeToAddqSubq" option enabled

        // SUBQ
        addDataItem(" SUBQ #2,D0", new short[] { 0x5540 });
        // --> see ADDQ for more tests

        // SUBX
        addDataItem(" SUBX D0,D0", new short[] { (short) 0x9140 });
        // --> see ABCD and ADDX for more tests

        // SVC
        addDataItem(" SVC D0", new short[] { 0x58C0 });
        // --> see ST for more tests

        // SVS
        addDataItem(" SVS D0", new short[] { 0x59C0 });
        // --> see ST for more tests

        // SWAP
        addDataItem(" SWAP", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" SWAP D0", new short[] { 0x4840 });
        addDataItem(" SWAP D7", new short[] { 0x4847 });
        addDataItem(" SWAP A0", new short[] { 0x4840 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" SWAP (A0)", new short[] { 0x4840 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" SWAP. D0", new short[] { 0x4840 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" SWAP.B D0", new short[] { 0x4840 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" SWAP.W D0", new short[] { 0x4840 });
        addDataItem(" SWAP.L D0", new short[] { 0x4840 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" SWAP.Z D0", new short[] { 0x4840 }, INVALID_SIZE_ATTRIBUTE_Z);

        // TAS
        addDataItem(" TAS", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" TAS D0", new short[] { 0x4AC0 });
        addDataItem(" TAS A0", new short[] { 0x4AC8 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" TAS 2(A0)", new short[] { 0x4AE8, 0x0002 });
        addDataItem(" TAS #0", new short[] { 0x4AFC, 0x0000 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" TAS. D0", new short[] { 0x4AC0 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" TAS.B D0", new short[] { 0x4AC0 });
        addDataItem(" TAS.W D0", new short[] { 0x4AC0 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" TAS.L D0", new short[] { 0x4AC0 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" TAS.Z D0", new short[] { 0x4AC0 }, INVALID_SIZE_ATTRIBUTE_Z);

        // TRAP
        addDataItem(" TRAP", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" TRAP #0", new short[] { 0x4E40 });
        addDataItem(" TRAP #15", new short[] { 0x4E4F });
        addDataItem(" TRAP #16", new short[] { 0x4E40 }, TRAP_VECTOR_OUT_OF_RANGE);
        addDataItem(" TRAP #-1", new short[] { 0x4E4F }, TRAP_VECTOR_OUT_OF_RANGE);
        addDataItem(" TRAP. #0", new short[] { 0x4E40 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" TRAP.B #0", new short[] { 0x4E40 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" TRAP.W #0", new short[] { 0x4E40 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" TRAP.L #0", new short[] { 0x4E40 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" TRAP.Z #0", new short[] { 0x4E40 }, INVALID_SIZE_ATTRIBUTE_Z);
        addDataItem(" TRAP D0", new short[] { 0x4E40 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);

        // TRAPV
        addDataItem(" TRAPV", new short[] { 0x4E76 });
        // --> see NOP for more tests

        // TST
        addDataItem(" TST", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" TST D0", new short[] { 0x4A40 });
        addDataItem(" TST A0", new short[] { 0x4A48 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" TST A0", new short[] { 0x4A48 }, M68KArchitecture.CPU32);
        addDataItem(" TST A0", new short[] { 0x4A48 }, M68KArchitecture.MC68020);
        addDataItem(" TST 2(A0)", new short[] { 0x4A68, 0x0002 });
        addDataItem(" TST 4(PC)", new short[] { 0x4A7A, 0x0002 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" TST 4(PC)", new short[] { 0x4A7A, 0x0002 }, M68KArchitecture.CPU32);
        addDataItem(" TST 4(PC)", new short[] { 0x4A7A, 0x0002 }, M68KArchitecture.MC68020);
        addDataItem(" TST #0", new short[] { 0x4A7C, 0x0000 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" TST #0", new short[] { 0x4A7C, 0x0000 }, M68KArchitecture.CPU32);
        addDataItem(" TST #0", new short[] { 0x4A7C, 0x0000 }, M68KArchitecture.MC68020);
        addDataItem(" TST. D0", new short[] { 0x4A40 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" TST.B D0", new short[] { 0x4A00 });
        addDataItem(" TST.B A0", new short[] { 0x4A08 }, M68KArchitecture.MC68020, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" TST.W D0", new short[] { 0x4A40 });
        addDataItem(" TST.L D0", new short[] { 0x4A80 });
        addDataItem(" TST.Z D0", new short[] { 0x4A40 }, INVALID_SIZE_ATTRIBUTE_Z);

        // UNLK
        addDataItem(" UNLK", NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" UNLK D0", new short[] { 0x4E58 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" UNLK A0", new short[] { 0x4E58 });
        addDataItem(" UNLK A7", new short[] { 0x4E5F });
        addDataItem(" UNLK (A0)", new short[] { 0x4E58 }, ADDRESSING_MODE_NOT_ALLOWED_HERE);
        addDataItem(" UNLK. A0", new short[] { 0x4E58 }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem(" UNLK.B A0", new short[] { 0x4E58 }, INVALID_SIZE_ATTRIBUTE_B);
        addDataItem(" UNLK.W A0", new short[] { 0x4E58 }, INVALID_SIZE_ATTRIBUTE_W);
        addDataItem(" UNLK.L A0", new short[] { 0x4E58 }, INVALID_SIZE_ATTRIBUTE_L);
        addDataItem(" UNLK.Z A0", new short[] { 0x4E58 }, INVALID_SIZE_ATTRIBUTE_Z);
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
        addDataItem(code, output, M68KArchitecture.MC68000, (AssemblyMessage) null);
    }

    private static void addDataItem(String code, short[] output, AssemblyMessage expectedMessage) {
        addDataItem(code, output, M68KArchitecture.MC68000, expectedMessage);
    }

    private static void addDataItem(String code, short[] output, AssemblyMessage... expectedMessages) {
        addDataItem(code, output, M68KArchitecture.MC68000, expectedMessages);
    }

    private static void addDataItem(String code, short[] output, M68KArchitecture architecture) {
        addDataItem(code, output, architecture, (AssemblyMessage) null);
    }

    private static void addDataItem(String code, short[] output, M68KArchitecture architecture, AssemblyMessage expectedMessage) {
        TEST_DATA.add(new Object[] { code, output, architecture, expectedMessage, null });
    }

    private static void addDataItem(String code, short[] output, M68KArchitecture architecture, AssemblyMessage... expectedMessages) {
        TEST_DATA.add(new Object[] { code, output, architecture, null, expectedMessages });
    }

    /**
     * Initializes a new InstructionsTest.
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
     *            priority over expectedMessage.
     */
    public InstructionsTest(String code, short[] output, M68KArchitecture architecture, AssemblyMessage expectedMessage,
            AssemblyMessage[] expectedMessages) {
        super(code, output, architecture, expectedMessage, expectedMessages);
    }

}
