package org.reasm.m68k.assembly.internal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.reasm.m68k.assembly.internal.CommonExpectedMessages.INVALID_SIZE_ATTRIBUTE_EMPTY;
import static org.reasm.m68k.assembly.internal.CommonExpectedMessages.INVALID_SIZE_ATTRIBUTE_Z;
import static org.reasm.m68k.assembly.internal.CommonExpectedMessages.UNDEFINED_SYMBOL;
import static org.reasm.m68k.assembly.internal.CommonExpectedMessages.WRONG_NUMBER_OF_OPERANDS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.*;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.CountMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.InvalidExpressionErrorMessage;
import org.reasm.m68k.messages.RegisterExpectedErrorMessage;
import org.reasm.m68k.messages.RegisterListExpectedErrorMessage;
import org.reasm.messages.DirectiveRequiresLabelErrorMessage;
import org.reasm.source.SourceFile;
import org.reasm.testhelpers.EquivalentAssemblyMessage;
import org.reasm.testhelpers.UserSymbolMatcher;

/**
 * Test class for short M68000 programs consisting of directives that define symbols.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class SymbolsTest {

    private static final UnsignedIntValue UINT_0 = new UnsignedIntValue(0);
    private static final UnsignedIntValue UINT_1 = new UnsignedIntValue(1);
    private static final RegisterList REGISTER_LIST_D0 = new RegisterList(EnumSet.of(GeneralPurposeRegister.D0));

    private static final UserSymbolMatcher<Value> FOO_CONSTANT_UINT_0 = new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
            SymbolType.CONSTANT, UINT_0);

    private static UserSymbolMatcher<?>[] NO_SYMBOLS = new UserSymbolMatcher[0];

    private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // =
        addDataItem("foo = 0", 2, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.VARIABLE, UINT_0) });
        // --> see SET for more tests

        // EQU
        addDataItem(" EQU", 2, NO_SYMBOLS, new DirectiveRequiresLabelErrorMessage("EQU"));
        addDataItem("foo EQU", 2, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem("foo EQU 0", 2, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0 });
        addDataItem("foo bar: EQU 0", 2, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, UINT_0) });
        addDataItem("foo EQU UNDEFINED", 2, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, null) }, UNDEFINED_SYMBOL);
        addDataItem("foo EQU ~", 2, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, null) }, new InvalidExpressionErrorMessage("~"));

        // EQUR
        addDataItem(" EQUR", 2, NO_SYMBOLS, new DirectiveRequiresLabelErrorMessage("EQUR"));
        addDataItem("foo EQUR", 2, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem("foo EQUR D0", 2, new UserSymbolMatcher[] { new UserSymbolMatcher<>(M68KAssemblyContext.REGISTER_ALIAS, "foo",
                SymbolType.CONSTANT, GeneralPurposeRegister.D0) });
        addDataItem("foo bar: EQUR D0", 2,
                new UserSymbolMatcher[] {
                        new UserSymbolMatcher<>(M68KAssemblyContext.REGISTER_ALIAS, "foo", SymbolType.CONSTANT,
                                GeneralPurposeRegister.D0),
                        new UserSymbolMatcher<>(M68KAssemblyContext.REGISTER_ALIAS, "bar", SymbolType.CONSTANT,
                                GeneralPurposeRegister.D0) });
        addDataItem("foo EQUR 0", 2, NO_SYMBOLS, new RegisterExpectedErrorMessage());

        // REG
        addDataItem(" REG", 2, NO_SYMBOLS, new DirectiveRequiresLabelErrorMessage("REG"));
        addDataItem("foo REG", 2, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem("foo REG D0", 2, new UserSymbolMatcher[] { new UserSymbolMatcher<>(M68KAssemblyContext.REGISTER_LIST_ALIAS,
                "foo", SymbolType.CONSTANT, REGISTER_LIST_D0) });
        addDataItem(
                "foo REG D0-A7",
                2,
                new UserSymbolMatcher[] { new UserSymbolMatcher<>(M68KAssemblyContext.REGISTER_LIST_ALIAS, "foo",
                        SymbolType.CONSTANT, new RegisterList(EnumSet.range(GeneralPurposeRegister.D0, GeneralPurposeRegister.A7))) });
        addDataItem("foo REG D0/A7", 2, new UserSymbolMatcher[] { new UserSymbolMatcher<>(M68KAssemblyContext.REGISTER_LIST_ALIAS,
                "foo", SymbolType.CONSTANT, new RegisterList(EnumSet.of(GeneralPurposeRegister.D0, GeneralPurposeRegister.A7))) });
        addDataItem("foo bar: REG D0", 2, new UserSymbolMatcher[] {
                new UserSymbolMatcher<>(M68KAssemblyContext.REGISTER_LIST_ALIAS, "foo", SymbolType.CONSTANT, REGISTER_LIST_D0),
                new UserSymbolMatcher<>(M68KAssemblyContext.REGISTER_LIST_ALIAS, "bar", SymbolType.CONSTANT, REGISTER_LIST_D0) });
        addDataItem("foo REG 0", 2, NO_SYMBOLS, new RegisterListExpectedErrorMessage());

        // RS
        addDataItem("foo RS", 2, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem("foo RS 0", 2, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0 });
        addDataItem(" RS 0", 2, NO_SYMBOLS);
        addDataItem("foo RS 0\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, UINT_0) });
        addDataItem("foo RS 1", 2, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0 });
        addDataItem("foo RS 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(2)) });
        addDataItem(" RS 1\nbar RS 0", 3, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "bar",
                SymbolType.CONSTANT, new UnsignedIntValue(2)) });
        addDataItem("foo RS 13\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(26)) });
        addDataItem("foo RS $8000000000000000\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, UINT_0) });
        addDataItem("foo RS -1", 2, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0 }, new CountMustNotBeNegativeErrorMessage());
        addDataItem("foo RS ~", 2, NO_SYMBOLS, new InvalidExpressionErrorMessage("~"));
        addDataItem("foo RS 0, 0", 2, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem("foo RS. 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(2)) },
                INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem("foo RS.B 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, UINT_1) });
        addDataItem("foo RS.W 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(2)) });
        addDataItem("foo RS.L 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(4)) });
        addDataItem("foo RS.Q 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(8)) });
        addDataItem("foo RS.S 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(4)) });
        addDataItem("foo RS.D 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(8)) });
        addDataItem("foo RS.X 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(12)) });
        addDataItem("foo RS.P 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(12)) });
        addDataItem("foo RS.Z 1\nbar RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(2)) },
                INVALID_SIZE_ATTRIBUTE_Z);

        // RSRESET
        addDataItem(" RSRESET", 2, NO_SYMBOLS);
        addDataItem(" RSRESET 0", 2, NO_SYMBOLS);
        addDataItem(" RSRESET 0,0", 2, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" RSRESET\nfoo RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0 });
        addDataItem("foo RS 2\n RSRESET\nbar RS 0", 4, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, UINT_0) });
        addDataItem(" RSRESET $100\nfoo RS 0", 3, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, new UnsignedIntValue(0x100)) });
        addDataItem(" RSRESET -$100\nfoo RS 0", 3, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, new SignedIntValue(-0x100)) });
        addDataItem("foo RS 2\n RSRESET $100\nbar RS 0", 4, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(0x100)) });
        addDataItem(" RSRESET ~", 2, NO_SYMBOLS, new InvalidExpressionErrorMessage("~"));

        // RSSET
        addDataItem(" RSSET", 2, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" RSSET 0", 2, NO_SYMBOLS);
        addDataItem(" RSSET 0,0", 2, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" RSSET 0\nfoo RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0 });
        addDataItem("foo RS 2\n RSSET 0\nbar RS 0", 4, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, UINT_0) });
        addDataItem(" RSSET $100\nfoo RS 0", 3, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, new UnsignedIntValue(0x100)) });
        addDataItem(" RSSET -$100\nfoo RS 0", 3, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, new SignedIntValue(-0x100)) });
        addDataItem("foo RS 2\n RSSET $100\nbar RS 0", 4, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(0x100)) });
        addDataItem(" RSSET ~", 2, NO_SYMBOLS, new InvalidExpressionErrorMessage("~"));

        // SET
        addDataItem(" SET", 2, NO_SYMBOLS, new DirectiveRequiresLabelErrorMessage("SET"));
        addDataItem("foo SET", 2, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem("foo SET 0", 2, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.VARIABLE, UINT_0) });
        addDataItem("foo bar: SET 0", 2, new UserSymbolMatcher[] {
                new UserSymbolMatcher<>(SymbolContext.VALUE, "foo", SymbolType.VARIABLE, UINT_0),
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.VARIABLE, UINT_0) });
        addDataItem("foo SET UNDEFINED", 2, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.VARIABLE, null) }, UNDEFINED_SYMBOL);
        addDataItem("foo SET ~", 2, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.VARIABLE, null) }, new InvalidExpressionErrorMessage("~"));
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

    private static void addDataItem(String code, int steps, UserSymbolMatcher<?>[] symbols) {
        addDataItem(code, steps, symbols, (AssemblyMessage) null);
    }

    private static void addDataItem(String code, int steps, UserSymbolMatcher<?>[] symbols, AssemblyMessage expectedMessage) {
        TEST_DATA.add(new Object[] { code, steps, symbols, expectedMessage, null });
    }

    @SuppressWarnings("unused")
    private static void addDataItem(String code, int steps, UserSymbolMatcher<?>[] symbols, AssemblyMessage... expectedMessages) {
        TEST_DATA.add(new Object[] { code, steps, symbols, null, expectedMessages });
    }

    private final String code;
    private final int steps;
    private final UserSymbolMatcher<?>[] symbolMatchers;
    private final AssemblyMessage expectedMessage;
    private final AssemblyMessage[] expectedMessages;

    /**
     * Initializes a new SymbolsTest.
     *
     * @param code
     *            assembly code to assemble
     * @param steps
     *            the number of steps the program is expected to take to assemble completely
     * @param symbolMatchers
     *            an array of matchers for the symbols that are expected to be defined in the program
     * @param expectedMessage
     *            an {@link AssemblyMessage} that is expected to be generated while assembling the code
     * @param expectedMessages
     *            an array of {@link AssemblyMessage} that is expected to be generated while assembling the code
     */
    public SymbolsTest(String code, int steps, UserSymbolMatcher<?>[] symbolMatchers, AssemblyMessage expectedMessage,
            AssemblyMessage... expectedMessages) {
        this.code = code;
        this.steps = steps;
        this.symbolMatchers = symbolMatchers;
        this.expectedMessage = expectedMessage;
        this.expectedMessages = expectedMessages;
    }

    /**
     * Asserts that a program defines the expected symbols.
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
                final EquivalentAssemblyMessage[] matchers = new EquivalentAssemblyMessage[this.expectedMessages.length];
                for (int i = 0; i < this.expectedMessages.length; i++) {
                    matchers[i] = new EquivalentAssemblyMessage(this.expectedMessages[i]);
                }

                assertThat(assembly.getMessages(), contains(matchers));
            } else if (this.expectedMessage != null) {
                assertThat(assembly.getMessages(), contains(new EquivalentAssemblyMessage(this.expectedMessage)));
            } else {
                assertThat(assembly.getMessages(), is(empty()));
            }

            assertThat(assembly.getSymbols(), containsInAnyOrder(this.symbolMatchers));

            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            assembly.writeAssembledDataTo(out);
            assertThat(out.size(), is(0));
        } catch (AssertionError e) {
            throw new AssertionError(this.code + e.getMessage(), e);
        }
    }

}
