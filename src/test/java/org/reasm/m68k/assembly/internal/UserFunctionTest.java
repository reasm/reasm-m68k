package org.reasm.m68k.assembly.internal;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.annotation.Nonnull;

import org.reasm.Assembly;
import org.reasm.AssemblyCompletionStatus;
import org.reasm.Configuration;
import org.reasm.Environment;
import org.reasm.Function;
import org.reasm.FunctionValue;
import org.reasm.SymbolContext;
import org.reasm.Value;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.source.SourceFile;
import org.reasm.testhelpers.ValueVisitorAdapter;

import ca.fragag.testhelpers.HasType;
import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link UserFunction}.
 *
 * @author Francis Gagné
 */
public class UserFunctionTest extends ObjectHashCodeEqualsContract {

    @Nonnull
    private static final Object MAIN_OBJECT;
    @Nonnull
    private static final Object OTHER_EQUAL_OBJECT;
    @Nonnull
    private static final Object ANOTHER_EQUAL_OBJECT;
    @Nonnull
    private static final Object DIFFERENT_OBJECT_0;
    @Nonnull
    private static final Object DIFFERENT_OBJECT_1;
    @Nonnull
    private static final Object DIFFERENT_OBJECT_2;

    static {
        // On passes 1-3, the ELSE block is assembled. This gives us 3 identical function definitions.
        // On pass 4, the ELSEIF X block is assembled. The function definition in this block swaps the parameter names.
        // On pass 5, the IF W block is assembled. The function definition in this block changes the expression.
        final Configuration configuration = new Configuration(Environment.DEFAULT, new SourceFile(" IF W\n"
                + "F FUNCTION A,B,A-B\n" + " ELSEIF X\n" + "F FUNCTION B,A,A+B\n" + " ELSE\n" + "F FUNCTION A,B,A+B\n" + " ENDIF\n"
                + "W EQU X\n" + "X EQU Y\n" + "Y EQU Z\n" + "Z EQU 1", null), M68KArchitecture.MC68000);
        final Assembly assembly = new Assembly(configuration);

        final int numberOfPasses = 5;
        final Object[] functions = new Object[numberOfPasses];

        for (int i = 0; i < numberOfPasses; i++) {
            int stepLimit = 11;
            AssemblyCompletionStatus status;
            do {
                status = assembly.step();
                --stepLimit;
                if (stepLimit < 0) {
                    fail("The assembly is performing too many steps.");
                }
            } while (status == AssemblyCompletionStatus.PENDING);

            if (i == numberOfPasses - 1) {
                // Normally, the result should be COMPLETE.
                // However, if UserFunction.equals() or UserFunction.hashCode() has a bug,
                // the assembly may be performing infinite passes
                // because the UserFunction created in a subsequent pass
                // is not equal to the UserFunction created in a prior pass.
                // In that case, we'll let the class initialize
                // and errors will be reported when running the test cases.
                assertThat(status,
                        is(either(equalTo(AssemblyCompletionStatus.COMPLETE))
                                .or(equalTo(AssemblyCompletionStatus.STARTED_NEW_PASS))));
            } else {
                assertThat(status, is(AssemblyCompletionStatus.STARTED_NEW_PASS));
            }

            functions[i] = getFunction(assembly, "F");
        }

        MAIN_OBJECT = functions[0];
        OTHER_EQUAL_OBJECT = functions[1];
        ANOTHER_EQUAL_OBJECT = functions[2];

        // This UserFunction has a different parameterNames.
        DIFFERENT_OBJECT_0 = functions[3];

        // This UserFunction has a different functionExpression.
        DIFFERENT_OBJECT_1 = functions[4];
    }

    static {
        final Configuration configuration = new Configuration(Environment.DEFAULT, new SourceFile("F FUNCTION A,B,A+B", null),
                M68KArchitecture.MC68000);
        final Assembly assembly = new Assembly(configuration);

        assertThat(assembly.step(), is(AssemblyCompletionStatus.PENDING));
        assertThat(assembly.step(), is(AssemblyCompletionStatus.COMPLETE));

        // This UserFunction has a different context.
        DIFFERENT_OBJECT_2 = getFunction(assembly, "F");
    }

    @Nonnull
    private static Object getFunction(@Nonnull Assembly assembly, @Nonnull String symbolName) {
        final Object symbolValue = assembly.resolveSymbolReference(SymbolContext.VALUE, symbolName, false, false, null, null)
                .getValue();
        assertThat(symbolValue, HasType.hasType(FunctionValue.class));

        return Value.accept((FunctionValue) symbolValue, new ValueVisitorAdapter<Function>() {
            @Override
            public Function visitFunction(Function value) {
                return value;
            }
        });
    }

    /**
     * Initializes a new UserFunctionTest.
     */
    public UserFunctionTest() {
        super(MAIN_OBJECT, OTHER_EQUAL_OBJECT, ANOTHER_EQUAL_OBJECT, new Object[] { DIFFERENT_OBJECT_0, DIFFERENT_OBJECT_1,
                DIFFERENT_OBJECT_2, new Object() });
    }

}
