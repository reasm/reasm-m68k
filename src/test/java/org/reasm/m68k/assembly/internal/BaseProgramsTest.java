package org.reasm.m68k.assembly.internal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.Test;
import org.reasm.Architecture;
import org.reasm.Assembly;
import org.reasm.AssemblyCompletionStatus;
import org.reasm.AssemblyMessage;
import org.reasm.Configuration;
import org.reasm.Environment;
import org.reasm.UserSymbol;
import org.reasm.m68k.ConfigurationOptions;
import org.reasm.m68k.ConfigurationOptionsTest;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.InvalidSizeAttributeErrorMessage;
import org.reasm.m68k.messages.NotSupportedOnArchitectureErrorMessage;
import org.reasm.m68k.messages.SizeAttributeNotAllowedErrorMessage;
import org.reasm.messages.UnresolvedSymbolReferenceErrorMessage;
import org.reasm.messages.WrongNumberOfOperandsErrorMessage;
import org.reasm.source.SourceFile;
import org.reasm.testhelpers.EquivalentAssemblyMessage;
import org.reasm.testhelpers.UserSymbolMatcher;

import com.google.common.collect.ImmutableMap;

/**
 * Base test class for short M68000 programs.
 *
 * @author Francis Gagné
 */
public abstract class BaseProgramsTest {

    static final byte[] NO_DATA = new byte[0];
    static final UserSymbolMatcher<?>[] NO_SYMBOLS = new UserSymbolMatcher[0];

    static final AssemblyMessage WRONG_NUMBER_OF_OPERANDS = new WrongNumberOfOperandsErrorMessage();
    static final AssemblyMessage NOT_SUPPORTED_ON_ARCHITECTURE = new NotSupportedOnArchitectureErrorMessage();
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_EMPTY = new InvalidSizeAttributeErrorMessage("");
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_B = new InvalidSizeAttributeErrorMessage("B");
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_W = new InvalidSizeAttributeErrorMessage("W");
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_L = new InvalidSizeAttributeErrorMessage("L");
    static final AssemblyMessage INVALID_SIZE_ATTRIBUTE_Z = new InvalidSizeAttributeErrorMessage("Z");
    static final AssemblyMessage SIZE_ATTRIBUTE_NOT_ALLOWED = new SizeAttributeNotAllowedErrorMessage();
    static final AssemblyMessage UNDEFINED_SYMBOL = new UnresolvedSymbolReferenceErrorMessage("UNDEFINED");

    private final String code;
    private final int steps;
    private final byte[] output;
    private final M68KArchitecture architecture;
    private final AssemblyMessage expectedMessage;
    private final AssemblyMessage[] expectedMessages;
    private final UserSymbolMatcher<?>[] symbolMatchers;

    /**
     * Initializes a new BaseProgramsTest.
     *
     * @param code
     *            assembly code to assemble
     * @param steps
     *            the number of steps the program is expected to take to assemble completely
     * @param output
     *            the program's output
     * @param architecture
     *            the target architecture
     * @param expectedMessage
     *            an {@link AssemblyMessage} that is expected to be generated while assembling the code
     * @param expectedMessages
     *            an array of {@link AssemblyMessage AssemblyMessages} that are expected to be generated while assembling the code.
     *            Takes priority over expectedMessage.
     * @param symbolMatchers
     *            an array of {@link UserSymbolMatcher UserSymbolMatchers} that match the {@link UserSymbol UserSymbols} that are
     *            expected to be generated while assembling the code, or <code>null</code> to omit checking the generated symbols
     */
    public BaseProgramsTest(String code, int steps, byte[] output, M68KArchitecture architecture, AssemblyMessage expectedMessage,
            AssemblyMessage[] expectedMessages, UserSymbolMatcher<?>[] symbolMatchers) {
        this.code = code;
        this.steps = steps;
        this.output = output;
        this.architecture = architecture;
        this.expectedMessage = expectedMessage;
        this.expectedMessages = expectedMessages;
        this.symbolMatchers = symbolMatchers;
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
            final Environment environment = this.getEnvironment();
            final SourceFile mainSourceFile = new SourceFile(this.code, null);
            final Configuration configuration = new Configuration(environment, mainSourceFile, this.architecture)
                    .setCustomConfigurationOptions(this.getCustomConfigurationOptions());
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

            if (this.symbolMatchers != null) {
                assertThat(assembly.getSymbols(), containsInAnyOrder(this.symbolMatchers));
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

    /**
     * Gets a {@link Map} of configuration options to pass to
     * {@link Configuration#Configuration(Environment, SourceFile, Architecture)}.
     *
     * @return the {@link Map} of configuration options
     */
    @Nonnull
    protected Map<Object, Object> getCustomConfigurationOptions() {
        final Map<String, Object> m68kOptions = this.getM68KConfigurationOptions();
        if (m68kOptions != null) {
            return ImmutableMap.of(ConfigurationOptions.KEY,
                    (Object) ConfigurationOptions.create(m68kOptions, ConfigurationOptionsTest.FAILING_CONSUMER));
        }

        return ImmutableMap.of();
    }

    /**
     * Gets the {@link Environment} to pass to {@link Configuration#Configuration(Environment, SourceFile, Architecture)}.
     *
     * @return the {@link Environment}
     */
    @Nonnull
    protected Environment getEnvironment() {
        return Environment.DEFAULT;
    }

    /**
     * Gets a {@link Map} of M68K-specific configuration options to use for the test class.
     *
     * @return the {@link Map} of configuration options, or <code>null</code> to use the default options
     */
    @CheckForNull
    protected Map<String, Object> getM68KConfigurationOptions() {
        return null;
    }

}
