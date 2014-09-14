package org.reasm.m68k.assembly.internal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
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
import org.reasm.m68k.messages.AlignmentMustNotBeZeroOrNegativeErrorMessage;
import org.reasm.m68k.messages.InvalidExpressionErrorMessage;
import org.reasm.m68k.messages.OffsetMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.SizeAttributeNotAllowedErrorMessage;
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
        addDataItem(" ALIGN.B 1", 2, NO_DATA, new SizeAttributeNotAllowedErrorMessage());
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
        addDataItem(" CNOP.B 0,1", 2, NO_DATA, new SizeAttributeNotAllowedErrorMessage());
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
        addDataItem(code, steps, output, null);
    }

    private static void addDataItem(String code, int steps, byte[] output, AssemblyMessage expectedMessage) {
        TEST_DATA.add(new Object[] { code, steps, output, expectedMessage });
    }

    private final String code;
    private final int steps;
    private final byte[] output;
    private final AssemblyMessage expectedMessage;

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
     */
    public ProgramsTest(String code, int steps, byte[] output, AssemblyMessage expectedMessage) {
        this.code = code;
        this.steps = steps;
        this.output = output;
        this.expectedMessage = expectedMessage;
    }

    /**
     * Asserts that a program assembles correctly.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void assemble() throws IOException {
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

        //if (this.expectedMessages != null) {
        //    EquivalentAssemblyMessage[] matchers = new EquivalentAssemblyMessage[this.expectedMessages.length];
        //    for (int i = 0; i < this.expectedMessages.length; i++) {
        //        matchers[i] = new EquivalentAssemblyMessage(this.expectedMessages[i]);
        //    }
        //
        //    assertThat(assembly.getMessages(), contains(matchers));
        //} else
        if (this.expectedMessage != null) {
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
    }

}
