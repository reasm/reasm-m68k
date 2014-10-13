package org.reasm.m68k.assembly.internal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.reasm.m68k.assembly.internal.CommonExpectedMessages.SIZE_ATTRIBUTE_NOT_ALLOWED;
import static org.reasm.m68k.assembly.internal.CommonExpectedMessages.UNDEFINED_SYMBOL;
import static org.reasm.m68k.assembly.internal.CommonExpectedMessages.WRONG_NUMBER_OF_OPERANDS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.Assembly;
import org.reasm.AssemblyCompletionStatus;
import org.reasm.AssemblyMessage;
import org.reasm.Configuration;
import org.reasm.Environment;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.*;
import org.reasm.source.SourceFile;
import org.reasm.testhelpers.EquivalentAssemblyMessage;

/**
 * Test class for short M68000 programs.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class ProgramsTest {

    static final byte[] NO_DATA = new byte[0];

    private static final AssemblyMessage ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE = new AlignmentMustNotBeZeroOrNegativeErrorMessage();

    private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // ALIGN
        addDataItem(" ALIGN", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ALIGN -1", 2, NO_DATA, ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE);
        addDataItem(" ALIGN 0", 2, NO_DATA, ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE);
        addDataItem(" ALIGN 1", 2, NO_DATA);
        addDataItem(" ALIGN 2", 2, NO_DATA);
        addDataItem(" ALIGN UNDEFINED", 2, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" ALIGN ~", 2, NO_DATA, new InvalidExpressionErrorMessage("~"));
        addDataItem(" ALIGN.B 1", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" DC.B $77\n ALIGN 2", 3, new byte[] { 0x77, 0 });
        addDataItem(" DC.B $77\n ALIGN +2", 3, new byte[] { 0x77, 0 });
        addDataItem(" DC.B $77,$66\n ALIGN 2", 3, new byte[] { 0x77, 0x66 });
        addDataItem(" DC.B $77,$66,$55\n ALIGN 2\n DC.B $44", 4, new byte[] { 0x77, 0x66, 0x55, 0, 0x44 });
        addDataItem(" DC.B $77,$66,$55\n ALIGN 16\n DC.B $44", 4, new byte[] { 0x77, 0x66, 0x55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0x44 });

        // CNOP
        addDataItem(" CNOP", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CNOP 0", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CNOP 0,-1", 2, NO_DATA, ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE);
        addDataItem(" CNOP 0,0", 2, NO_DATA, ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE);
        addDataItem(" CNOP 0,1", 2, NO_DATA);
        addDataItem(" CNOP 0,2", 2, NO_DATA);
        addDataItem(" CNOP 0,UNDEFINED", 2, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" CNOP 0,~", 2, NO_DATA, new InvalidExpressionErrorMessage("~"));
        addDataItem(" CNOP -1,2", 2, NO_DATA, new OffsetMustNotBeNegativeErrorMessage());
        addDataItem(" CNOP 4,2", 2, new byte[] { 0, 0, 0, 0 });
        addDataItem(" CNOP UNDEFINED,2", 2, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" CNOP ~,2", 2, NO_DATA, new InvalidExpressionErrorMessage("~"));
        addDataItem(" CNOP.B 0,1", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" DC.B $77\n CNOP 0,2", 3, new byte[] { 0x77, 0 });
        addDataItem(" DC.B $77\n CNOP 0,+2", 3, new byte[] { 0x77, 0 });
        addDataItem(" DC.B $77,$66\n CNOP 0,2", 3, new byte[] { 0x77, 0x66 });
        addDataItem(" DC.B $77,$66,$55\n CNOP 0,2\n DC.B $44", 4, new byte[] { 0x77, 0x66, 0x55, 0, 0x44 });
        addDataItem(" DC.B $77,$66,$55\n CNOP 0,16\n DC.B $44", 4, new byte[] { 0x77, 0x66, 0x55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0x44 });
        addDataItem(" DCB.B 20,$FF\n CNOP 10,16\n DC.B $77", 4, new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0x77 });
        addDataItem(" DCB.B 20,$FF\n CNOP 2,16\n DC.B $77", 4, new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x77 });

        // DO
        addDataItem(" DO\n DC.W $1234\n UNTIL", 6, new byte[] { 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DO\n DC.W $1234\n UNTIL 1", 6, new byte[] { 0x12, 0x34 });
        addDataItem(" DO\n DC.W $1234\n UNTIL 1,1", 6, new byte[] { 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DO 1\n DC.W $1234\n UNTIL 1", 6, new byte[] { 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DO.W\n DC.W $1234\n UNTIL 1", 6, new byte[] { 0x12, 0x34 }, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem("I SET 0\n DO\n DC.W $1234\nI SET I + 1\n UNTIL I = 5", 28, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34,
                0x12, 0x34, 0x12, 0x34 });

        // ELSE
        final ElseWithoutIfErrorMessage elseWithoutIf = new ElseWithoutIfErrorMessage();
        addDataItem(" ELSE", 2, NO_DATA, elseWithoutIf);
        addDataItem(" ELSE 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, elseWithoutIf);
        addDataItem(" ELSE.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, elseWithoutIf);

        // ELSEIF
        final ElseifWithoutIfErrorMessage elseifWithoutIf = new ElseifWithoutIfErrorMessage();
        addDataItem(" ELSEIF 1", 2, NO_DATA, elseifWithoutIf);
        addDataItem(" ELSEIF.W 1", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, elseifWithoutIf);

        // ENDIF
        final EndifWithoutIfErrorMessage endifWithoutIf = new EndifWithoutIfErrorMessage();
        addDataItem(" ENDIF", 2, NO_DATA, endifWithoutIf);
        addDataItem(" ENDIF 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endifWithoutIf);
        addDataItem(" ENDIF.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endifWithoutIf);

        // ENDNS
        final EndnsWithoutNamespaceErrorMessage endnsWithoutNamespace = new EndnsWithoutNamespaceErrorMessage();
        addDataItem(" ENDNS", 2, NO_DATA, endnsWithoutNamespace);
        addDataItem(" ENDNS 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endnsWithoutNamespace);
        addDataItem(" ENDNS.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endnsWithoutNamespace);

        // ENDR
        final EndrWithoutReptErrorMessage endrWithoutRept = new EndrWithoutReptErrorMessage();
        addDataItem(" ENDR", 2, NO_DATA, endrWithoutRept);
        addDataItem(" ENDR 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endrWithoutRept);
        addDataItem(" ENDR.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endrWithoutRept);

        // ENDW
        final EndwWithoutWhileErrorMessage endwWithoutWhile = new EndwWithoutWhileErrorMessage();
        addDataItem(" ENDW", 2, NO_DATA, endwWithoutWhile);
        addDataItem(" ENDW 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endwWithoutWhile);
        addDataItem(" ENDW.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endwWithoutWhile);

        // FOR
        addDataItem(" FOR\n DC.W $1234\n NEXT", 4, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" FOR 1\n DC.W $1234\n NEXT", 4, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" FOR 1,5\n DC.W $1234\n NEXT", 24, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 });
        addDataItem(" FOR 1,5,2\n DC.W $1234\n NEXT", 16, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 });
        addDataItem(" FOR 1,5,2,3\n DC.W $1234\n NEXT", 16, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 },
                WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" FOR 5,1,-2\n DC.W $1234\n NEXT", 16, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 });
        addDataItem(" FOR UNDEFINED,5\n DC.W $1234\n NEXT", 4, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" FOR 1,UNDEFINED\n DC.W $1234\n NEXT", 4, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" FOR 1,5,UNDEFINED\n DC.W $1234\n NEXT", 8, new byte[] { 0x12, 0x34 }, UNDEFINED_SYMBOL);
        addDataItem("I FOR 11,15\n DC.B I\n NEXT", 24, new byte[] { 11, 12, 13, 14, 15 });
        addDataItem("I: J: FOR 11,15\n DC.B I+J\n NEXT", 24, new byte[] { 22, 24, 26, 28, 30 });

        // IF
        addDataItem(" IF\n DC.W $1234\n ENDIF", 4, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" IF 0\n DC.W $1234\n ENDIF", 4, NO_DATA);
        addDataItem(" IF.W 0\n DC.W $1234\n ENDIF", 4, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" IF 1\n DC.W $1234\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF 1,1\n DC.W $1234\n ENDIF", 5, new byte[] { 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" IF 1\n DC.W $1234\n ENDC", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF UNDEFINED\n DC.W $1234\n ENDIF", 4, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" IF 0\n DC.W $1234\n ELSE\n DC.W $2345\n ENDIF", 6, new byte[] { 0x23, 0x45 });
        addDataItem(" IF 1\n DC.W $1234\n ELSE\n DC.W $2345\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF UNDEFINED\n DC.W $1234\n ELSE\n DC.W $2345\n ENDIF", 6, new byte[] { 0x23, 0x45 }, UNDEFINED_SYMBOL);
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF\n DC.W $2345\n ENDIF", 5, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 0\n DC.W $2345\n ENDIF", 5, NO_DATA);
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 1\n DC.W $2345\n ENDIF", 6, new byte[] { 0x23, 0x45 });
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 1,1\n DC.W $2345\n ENDIF", 6, new byte[] { 0x23, 0x45 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" IF 1\n DC.W $1234\n ELSEIF 0\n DC.W $2345\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF 1\n DC.W $1234\n ELSEIF 1\n DC.W $2345\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 0\n DC.W $2345\n ELSE\n DC.W $3456\n ENDIF", 7, new byte[] { 0x34, 0x56 });
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 1\n DC.W $2345\n ELSE\n DC.W $3456\n ENDIF", 6, new byte[] { 0x23, 0x45 });
        addDataItem(" IF 1\n DC.W $1234\n ELSEIF 0\n DC.W $2345\n ELSE\n DC.W $3456\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF 1\n DC.W $1234\n ELSEIF 1\n DC.W $2345\n ELSE\n DC.W $3456\n ENDIF", 5, new byte[] { 0x12, 0x34 });

        // NEXT
        final NextWithoutForErrorMessage nextWithoutFor = new NextWithoutForErrorMessage();
        addDataItem(" NEXT", 2, NO_DATA, nextWithoutFor);
        addDataItem(" NEXT 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, nextWithoutFor);
        addDataItem(" NEXT.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, nextWithoutFor);

        // REPT
        addDataItem(" REPT\n DC.W $1234\n ENDR", 5, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" REPT -1\n DC.W $1234\n ENDR", 5, NO_DATA, new CountMustNotBeNegativeErrorMessage());
        addDataItem(" REPT 0\n DC.W $1234\n ENDR", 5, NO_DATA);
        addDataItem(" REPT.W 0\n DC.W $1234\n ENDR", 5, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" REPT 5\n DC.W $1234\n ENDR", 10, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 });

        // UNTIL
        final UntilWithoutDoErrorMessage untilWithoutDo = new UntilWithoutDoErrorMessage();
        addDataItem(" UNTIL", 2, NO_DATA, untilWithoutDo);
        addDataItem(" UNTIL.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, untilWithoutDo);

        // WHILE
        addDataItem(" WHILE\n DC.W $1234\n ENDW", 4, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" WHILE 0\n DC.W $1234\n ENDW", 4, NO_DATA);
        addDataItem(" WHILE.W 0\n DC.W $1234\n ENDW", 4, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem("I SET 0\n WHILE I < 5\n DC.W $1234\nI SET I + 1\n ENDW", 30, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34,
                0x12, 0x34, 0x12, 0x34 });
        addDataItem("I SET 0\n WHILE I < 5, I > 2\n DC.W $1234\nI SET I + 1\n ENDW", 30, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12,
                0x34, 0x12, 0x34, 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" WHILE UNDEFINED\n DC.W $1234\n ENDW", 4, NO_DATA, UNDEFINED_SYMBOL);
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
        TEST_DATA.add(new Object[] { code, steps, output, expectedMessage, null });
    }

    private static void addDataItem(String code, int steps, byte[] output, AssemblyMessage... expectedMessages) {
        TEST_DATA.add(new Object[] { code, steps, output, null, expectedMessages });
    }

    private final String code;
    private final int steps;
    private final byte[] output;
    private final AssemblyMessage expectedMessage;
    private final AssemblyMessage[] expectedMessages;

    /**
     * Initializes a new ProgramsTest.
     *
     * @param code
     *            assembly code to assemble
     * @param steps
     *            the number of steps the program is expected to take to assemble completely
     * @param output
     *            the program's output
     * @param expectedMessage
     *            an {@link AssemblyMessage} that is expected to be generated while assembling the code
     * @param expectedMessages
     *            the {@link AssemblyMessage}s that are expected to be generated while assembling the code
     */
    public ProgramsTest(String code, int steps, byte[] output, AssemblyMessage expectedMessage, AssemblyMessage... expectedMessages) {
        this.code = code;
        this.steps = steps;
        this.output = output;
        this.expectedMessage = expectedMessage;
        this.expectedMessages = expectedMessages;
    }

    /**
     * Asserts that a program assembles correctly.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void assemble() throws IOException {
        try {
            final Environment environment = Environment.DEFAULT;
            final SourceFile mainSourceFile = new SourceFile(this.code, null);
            final Configuration configuration = new Configuration(environment, mainSourceFile, M68KArchitecture.MC68000);
            final Assembly assembly = new Assembly(configuration);

            int steps = this.steps;
            AssemblyCompletionStatus status;
            do {
                assertThat("The assembly is performing more steps than expected (expecting " + this.steps + " steps).", steps,
                        is(not(0)));

                status = assembly.step();
                --steps;
            } while (status != AssemblyCompletionStatus.COMPLETE);

            assertThat("The assembly is performing fewer steps than expected (expecting " + this.steps + " steps).", steps, is(0));

            if (this.expectedMessages != null) {
                EquivalentAssemblyMessage[] matchers = new EquivalentAssemblyMessage[this.expectedMessages.length];
                for (int i = 0; i < this.expectedMessages.length; i++) {
                    matchers[i] = new EquivalentAssemblyMessage(this.expectedMessages[i]);
                }

                assertThat(assembly.getMessages(), contains(matchers));
            } else if (this.expectedMessage != null) {
                assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(this.expectedMessage)));
            } else {
                assertThat(assembly.getMessages(), is(empty()));
            }

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            assembly.writeAssembledDataTo(out);
            final byte[] outputBytes = out.toByteArray();
            assertThat(outputBytes.length, is(this.output.length));
            for (int i = 0; i < this.output.length; i++) {
                assertThat(outputBytes[i], is(this.output[i]));
            }
        } catch (AssertionError e) {
            throw new AssertionError(this.code + "\n" + e.getMessage(), e);
        }
    }

}
