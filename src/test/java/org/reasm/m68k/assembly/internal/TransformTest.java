package org.reasm.m68k.assembly.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.Environment;
import org.reasm.IdentityTransformation;
import org.reasm.OutputTransformation;
import org.reasm.OutputTransformationFactory;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.messages.InvalidTransformationArgumentsErrorMessage;
import org.reasm.messages.UnknownTransformationMethodErrorMessage;
import org.reasm.testhelpers.ReverseTransformation;

import ca.fragag.Consumer;

/**
 * Test class for the <code>TRANSFORM</code> directive.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class TransformTest extends BaseProgramsTest {

    @Nonnull
    private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // TRANSFORM
        addDataItem(" TRANSFORM\n ENDTRANSFORM", 5, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" TRANSFORM UNKNOWN\n ENDTRANSFORM", 5, NO_DATA, new UnknownTransformationMethodErrorMessage("UNKNOWN"));
        addDataItem(" TRANSFORM REVERSE\n ENDTRANSFORM", 5, NO_DATA);
        addDataItem(" TRANSFORM TEST1\n ENDTRANSFORM", 5, NO_DATA, new InvalidTransformationArgumentsErrorMessage("TEST1",
                new String[0]));
        addDataItem(" TRANSFORM TEST1,0,0\n ENDTRANSFORM", 5, NO_DATA);
    }

    /**
     * Gets the test data for this parameterized test.
     *
     * @return the test data
     */
    @Nonnull
    @Parameters
    public static List<Object[]> data() {
        return TEST_DATA;
    }

    private static void addDataItem(@Nonnull String code, int steps, @Nonnull byte[] output) {
        addDataItem(code, steps, output, null);
    }

    private static void addDataItem(@Nonnull String code, int steps, @Nonnull byte[] output,
            @CheckForNull AssemblyMessage expectedMessage) {
        TEST_DATA.add(new Object[] { code, steps, output, expectedMessage });
    }

    /**
     * Initializes a new TransformTest.
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
    public TransformTest(@Nonnull String code, int steps, @Nonnull byte[] output, @CheckForNull AssemblyMessage expectedMessage) {
        super(code, steps, output, M68KArchitecture.MC68000, expectedMessage, null, null);
    }

    @Override
    protected Environment getEnvironment() {
        final OutputTransformationFactory reverseOutputTransformationFactory = new OutputTransformationFactory(
                Collections.singleton("REVERSE")) {
            @Override
            public OutputTransformation create(String[] arguments, Consumer<AssemblyMessage> assemblyMessageConsumer) {
                return ReverseTransformation.INSTANCE;
            }
        };

        final OutputTransformationFactory test1OutputTransformationFactory = new OutputTransformationFactory(
                Collections.singleton("TEST1")) {
            @Override
            public OutputTransformation create(String[] arguments, Consumer<AssemblyMessage> assemblyMessageConsumer) {
                if (arguments.length == 2 && "0".equals(arguments[0]) && "0".equals(arguments[1])) {
                    return IdentityTransformation.INSTANCE;
                }

                return null;
            }
        };

        return super.getEnvironment().addOutputTransformationFactory(reverseOutputTransformationFactory)
                .addOutputTransformationFactory(test1OutputTransformationFactory);
    }

}
