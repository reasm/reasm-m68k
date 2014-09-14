package org.reasm.m68k.assembly.internal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.reasm.Architecture;
import org.reasm.Assembly;
import org.reasm.AssemblyCompletionStatus;
import org.reasm.AssemblyMessage;
import org.reasm.Configuration;
import org.reasm.Environment;
import org.reasm.FileFetcher;
import org.reasm.PredefinedSymbolTable;
import org.reasm.m68k.ConfigurationOptions;
import org.reasm.m68k.ConfigurationOptionsTest;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.AddressingModeNotAllowedHereErrorMessage;
import org.reasm.m68k.messages.BranchTargetOutOfRangeErrorMessage;
import org.reasm.m68k.messages.BreakpointNumberOutOfRangeErrorMessage;
import org.reasm.m68k.messages.LabelExpectedErrorMessage;
import org.reasm.m68k.messages.TrapVectorOutOfRangeErrorMessage;
import org.reasm.source.SourceFile;
import org.reasm.testhelpers.EquivalentAssemblyMessage;

import com.google.common.collect.ImmutableMap;

/**
 * Base test class for individual M68000 family instructions.
 *
 * @author Francis Gagné
 */
public abstract class BaseInstructionsTest {

    static final short[] NO_DATA = new short[0];
    static final AssemblyMessage ADDRESSING_MODE_NOT_ALLOWED_HERE = new AddressingModeNotAllowedHereErrorMessage();
    static final AssemblyMessage BREAKPOINT_NUMBER_OUT_OF_RANGE = new BreakpointNumberOutOfRangeErrorMessage();
    static final AssemblyMessage BRANCH_TARGET_OUT_OF_RANGE = new BranchTargetOutOfRangeErrorMessage();
    static final AssemblyMessage LABEL_EXPECTED = new LabelExpectedErrorMessage();
    static final AssemblyMessage TRAP_VECTOR_OUT_OF_RANGE = new TrapVectorOutOfRangeErrorMessage();

    private final String code;
    private final short[] output;
    private final M68KArchitecture architecture;
    private final AssemblyMessage expectedMessage;
    private final AssemblyMessage[] expectedMessages;

    /**
     * Initializes a new BaseInstructionsTest.
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
    public BaseInstructionsTest(String code, short[] output, M68KArchitecture architecture, AssemblyMessage expectedMessage,
            AssemblyMessage[] expectedMessages) {
        this.code = code;
        this.output = output;
        this.architecture = architecture;
        this.expectedMessage = expectedMessage;
        this.expectedMessages = expectedMessages;
    }

    /**
     * Asserts that an instruction assembles correctly.
     *
     * @throws IOException
     *             an I/O exception occurred
     */
    @Test
    public void assemble() throws IOException {
        try {
            final Environment environment = Environment.DEFAULT;
            final SourceFile mainSourceFile = new SourceFile(this.code, null);
            final Configuration configuration = new Configuration(environment, mainSourceFile, this.architecture)
                    .setCustomConfigurationOptions(this.getCustomConfigurationOptions());
            final Assembly assembly = new Assembly(configuration);

            // The code should contain only one instruction.
            assertThat(assembly.step(), is(AssemblyCompletionStatus.PENDING));
            assertThat(assembly.step(), is(AssemblyCompletionStatus.COMPLETE));

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
            assertThat(outputBytes.length, is(this.output.length * 2));
            for (int i = 0; i < this.output.length; i++) {
                assertThat(outputBytes[i * 2 + 0], is((byte) (this.output[i] >>> 8)));
                assertThat(outputBytes[i * 2 + 1], is((byte) (this.output[i] >>> 0)));
            }
        } catch (AssertionError e) {
            throw new AssertionError(this.code + e.getMessage(), e);
        }
    }

    /**
     * Gets a {@link Map} of configuration options to pass to
     * {@link Configuration#Configuration(Environment, SourceFile, Architecture, FileFetcher, PredefinedSymbolTable, Map)}.
     *
     * @return the {@link Map} of configuration options
     */
    protected Map<Object, Object> getCustomConfigurationOptions() {
        final Map<String, Object> m68kOptions = this.getM68KConfigurationOptions();
        if (m68kOptions != null) {
            final HashMap<Object, Object> map = new HashMap<>();
            map.put(ConfigurationOptions.KEY, ConfigurationOptions.create(m68kOptions, ConfigurationOptionsTest.FAILING_CONSUMER));
            return map;
        }

        return ImmutableMap.of();
    }

    /**
     * Gets a {@link Map} of M68K-specific configuration options to use for the test class.
     *
     * @return the {@link Map} of configuration options, or <code>null</code> to use the default options
     */
    protected Map<String, Object> getM68KConfigurationOptions() {
        return null;
    }

}
