package org.reasm.m68k.assembly.internal;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.SignedIntValue;
import org.reasm.SymbolContext;
import org.reasm.SymbolType;
import org.reasm.UnsignedIntValue;
import org.reasm.Value;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.CountMustNotBeNegativeErrorMessage;
import org.reasm.m68k.messages.InvalidExpressionErrorMessage;
import org.reasm.m68k.messages.RegisterExpectedErrorMessage;
import org.reasm.m68k.messages.RegisterListExpectedErrorMessage;
import org.reasm.messages.DirectiveRequiresLabelErrorMessage;
import org.reasm.testhelpers.UserSymbolMatcher;

/**
 * Test class for short M68000 programs consisting of directives that define symbols.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class SymbolsTest extends BaseProgramsTest {

    private static final UnsignedIntValue UINT_0 = new UnsignedIntValue(0);
    private static final UnsignedIntValue UINT_1 = new UnsignedIntValue(1);
    private static final RegisterList REGISTER_LIST_D0 = new RegisterList(EnumSet.of(GeneralPurposeRegister.D0));

    private static final UserSymbolMatcher<Value> FOO_CONSTANT_UINT_0 = new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
            SymbolType.CONSTANT, UINT_0);

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

        // NAMESPACE
        addDataItem(" NAMESPACE\n ENDNS", 5, NO_SYMBOLS, new DirectiveRequiresLabelErrorMessage("NAMESPACE"));
        addDataItem("A NAMESPACE\n ENDNS", 5, NO_SYMBOLS);
        addDataItem("A NAMESPACE.W\n ENDNS", 5, NO_SYMBOLS, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem("A NAMESPACE 1\n ENDNS", 5, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem("A NAMESPACE\nB EQU 1\n ENDNS", 6, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE,
                "A.B", SymbolType.CONSTANT, UINT_1) });
        addDataItem("A NAMESPACE\nB EQU 1\n ENDNS\nC EQU 1", 7, new UserSymbolMatcher[] {
                new UserSymbolMatcher<>(SymbolContext.VALUE, "A.B", SymbolType.CONSTANT, UINT_1),
                new UserSymbolMatcher<>(SymbolContext.VALUE, "C", SymbolType.CONSTANT, UINT_1) });
        addDataItem("A: B: NAMESPACE\nC EQU 1\n ENDNS", 6, new UserSymbolMatcher[] {
                new UserSymbolMatcher<>(SymbolContext.VALUE, "A", SymbolType.CONSTANT, UINT_0),
                new UserSymbolMatcher<>(SymbolContext.VALUE, "B.C", SymbolType.CONSTANT, UINT_1) });
        addDataItem(" NAMESPACE\nA EQU 1\n ENDNS", 6, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "A",
                SymbolType.CONSTANT, UINT_1) }, new DirectiveRequiresLabelErrorMessage("NAMESPACE"));

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
        addDataItem(" RSRESET UNDEFINED", 2, NO_SYMBOLS, UNDEFINED_SYMBOL);
        addDataItem(" RSRESET 0", 2, NO_SYMBOLS);
        addDataItem(" RSRESET 0,0", 2, NO_SYMBOLS, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" RSRESET\nfoo RS 0", 3, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0 });
        addDataItem("foo RS 2\n RSRESET\nbar RS 0", 4, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, UINT_0) });
        addDataItem(" RSRESET $100\nfoo RS 0", 3, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, new UnsignedIntValue(0x100)) });
        addDataItem(" RSRESET -$100\nfoo RS 0", 3, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, new SignedIntValue(-0x100)) });
        addDataItem(" RSRESET '256'\nfoo RS 0", 3, new UserSymbolMatcher[] { new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, new SignedIntValue(0x100)) });
        addDataItem("foo RS 2\n RSRESET $100\nbar RS 0", 4, new UserSymbolMatcher[] { FOO_CONSTANT_UINT_0,
                new UserSymbolMatcher<>(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, new UnsignedIntValue(0x100)) });
        addDataItem(" RSRESET ~", 2, NO_SYMBOLS, new InvalidExpressionErrorMessage("~"));
        // TODO: test with a built-in function symbol
        //addDataItem(" RSRESET STRLEN", 2, NO_SYMBOLS, new FunctionCannotBeConvertedToIntegerErrorMessage());

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
            AssemblyMessage[] expectedMessages) {
        super(code, steps, NO_DATA, M68KArchitecture.MC68000, expectedMessage, expectedMessages, symbolMatchers);
    }

}
