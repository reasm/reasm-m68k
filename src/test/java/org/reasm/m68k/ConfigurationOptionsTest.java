package org.reasm.m68k;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.junit.Assert;
import org.junit.Test;

import ca.fragag.Consumer;

/**
 * Test class for {@link ConfigurationOptions}.
 *
 * @author Francis Gagné
 */
public class ConfigurationOptionsTest {

    private final class InvalidEntriesConsumer implements Consumer<Map.Entry<String, Object>> {

        private boolean gotExpectedEntry;
        @Nonnull
        private final String key;

        InvalidEntriesConsumer(@Nonnull String key) {
            this.key = key;
        }

        @Override
        public void accept(Map.Entry<String, Object> entry) {
            assertThat(this.gotExpectedEntry, is(false));
            assertThat(entry.getKey(), is(this.key));
            assertThat(entry.getValue(), is(sameInstance(AN_OBJECT)));
            this.gotExpectedEntry = true;
        }

        void assertGotExpectedEntry() {
            assertThat(this.gotExpectedEntry, is(true));
        }

    }

    /**
     * A {@link Consumer} of {@link ConfigurationOptions} map entries that {@linkplain Assert#fail(String) fails} when it is called.
     */
    @Nonnull
    public static final Consumer<Map.Entry<String, Object>> FAILING_CONSUMER = new Consumer<Map.Entry<String, Object>>() {
        @Override
        public void accept(Map.Entry<String, Object> entry) {
            fail("Invalid configuration options entry: " + entry.getKey() + "=" + String.valueOf(entry.getValue()));
        }
    };

    @Nonnull
    static final Object AN_OBJECT = new Object();

    private static void automaticEven(boolean value) {
        final ConfigurationOptions configurationOptions = validOption(ConfigurationOptions.AUTOMATIC_EVEN, value);
        assertThat(configurationOptions.automaticEven(), is(value));
    }

    private static ConfigurationOptions invalidOption(@Nonnull String option, @Nonnull InvalidEntriesConsumer invalidEntriesConsumer) {
        final Map<String, Object> optionsMap = new HashMap<>();
        optionsMap.put(option, AN_OBJECT);
        final ConfigurationOptions configurationOptions = ConfigurationOptions.create(optionsMap, invalidEntriesConsumer);
        return configurationOptions;
    }

    private static void optimizeCmpiToTst(boolean value) {
        final ConfigurationOptions configurationOptions = validOption(ConfigurationOptions.OPTIMIZE_CMPI_TO_TST, value);
        assertThat(configurationOptions.optimizeCmpiToTst(), is(value));
    }

    private static void optimizeMoveToMoveq(boolean value) {
        final ConfigurationOptions configurationOptions = validOption(ConfigurationOptions.OPTIMIZE_MOVE_TO_MOVEQ, value);
        assertThat(configurationOptions.optimizeMoveToMoveq(), is(value));
    }

    private static void optimizeToAddqSubq(boolean value) {
        final ConfigurationOptions configurationOptions = validOption(ConfigurationOptions.OPTIMIZE_TO_ADDQ_SUBQ, value);
        assertThat(configurationOptions.optimizeToAddqSubq(), is(value));
    }

    private static void optimizeUnsizedAbsoluteAddressingToPcRelative(boolean value) {
        final ConfigurationOptions configurationOptions = validOption(
                ConfigurationOptions.OPTIMIZE_UNSIZED_ABSOLUTE_ADDRESSING_TO_PC_RELATIVE, value);
        assertThat(configurationOptions.optimizeUnsizedAbsoluteAddressingToPcRelative(), is(value));
    }

    private static void optimizeUnsizedBranches(boolean value) {
        final ConfigurationOptions configurationOptions = validOption(ConfigurationOptions.OPTIMIZE_UNSIZED_BRANCHES, value);
        assertThat(configurationOptions.optimizeUnsizedBranches(), is(value));
    }

    private static void optimizeZeroDisplacement(boolean value) {
        final ConfigurationOptions configurationOptions = validOption(ConfigurationOptions.OPTIMIZE_ZERO_DISPLACEMENT, value);
        assertThat(configurationOptions.optimizeZeroDisplacement(), is(value));
    }

    private static ConfigurationOptions validOption(@Nonnull String option, @Nonnull Object value) {
        final Map<String, Object> optionsMap = new HashMap<>();
        optionsMap.put(option, value);
        return ConfigurationOptions.create(optionsMap, FAILING_CONSUMER);
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} treats the {@link ConfigurationOptions#AUTOMATIC_EVEN} option
     * set to an {@link Object} value as invalid.
     */
    @Test
    public void automaticEvenBad() {
        final InvalidEntriesConsumer invalidEntriesConsumer = new InvalidEntriesConsumer(ConfigurationOptions.AUTOMATIC_EVEN);
        final ConfigurationOptions configurationOptions = invalidOption(ConfigurationOptions.AUTOMATIC_EVEN, invalidEntriesConsumer);
        invalidEntriesConsumer.assertGotExpectedEntry();
        assertThat(configurationOptions.automaticEven(), is(false));
    }

    /**
     * Asserts that {@link ConfigurationOptions#automaticEven()} returns <code>false</code> when the {@link ConfigurationOptions} is
     * created with the {@link ConfigurationOptions#AUTOMATIC_EVEN} option set to <code>false</code>.
     */
    @Test
    public void automaticEvenFalse() {
        automaticEven(false);
    }

    /**
     * Asserts that {@link ConfigurationOptions#automaticEven()} returns <code>true</code> when the {@link ConfigurationOptions} is
     * created with the {@link ConfigurationOptions#AUTOMATIC_EVEN} option set to <code>true</code>.
     */
    @Test
    public void automaticEvenTrue() {
        automaticEven(true);
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} initializes a {@link ConfigurationOptions} object with
     * default attributes when the <code>options</code> argument is an empty {@link Map}.
     */
    @Test
    public void create() {
        final Map<String, Object> optionsMap = new HashMap<>();
        final ConfigurationOptions configurationOptions = ConfigurationOptions.create(optionsMap, FAILING_CONSUMER);
        assertThat(configurationOptions.automaticEven(), is(false));
        assertThat(configurationOptions.optimizeCmpiToTst(), is(false));
        assertThat(configurationOptions.optimizeMoveToMoveq(), is(false));
        assertThat(configurationOptions.optimizeToAddqSubq(), is(false));
        assertThat(configurationOptions.optimizeUnsizedAbsoluteAddressingToPcRelative(), is(false));
        assertThat(configurationOptions.optimizeUnsizedBranches(), is(false));
        assertThat(configurationOptions.optimizeZeroDisplacement(), is(false));
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} does not throw an exception when the option map contains an
     * invalid entry and the <code>invalidEntriesConsumer</code> argument is <code>null</code>.
     */
    @Test
    public void createInvalidEntry() {
        final InvalidEntriesConsumer invalidEntriesConsumer = new InvalidEntriesConsumer("invalidOption");
        invalidOption("invalidOption", invalidEntriesConsumer);
        invalidEntriesConsumer.assertGotExpectedEntry();
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} does not throw an exception when the option map contains an
     * invalid entry and the <code>invalidEntriesConsumer</code> argument is <code>null</code>.
     */
    @Test
    public void createInvalidEntryNoInvalidEntriesConsumer() {
        final Map<String, Object> optionsMap = new HashMap<>();
        optionsMap.put("invalidOption", AN_OBJECT);
        ConfigurationOptions.create(optionsMap, null);
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} throws a {@link NullPointerException} when the
     * <code>options</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void createNullOptions() {
        ConfigurationOptions.create(null, FAILING_CONSUMER);
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} treats the {@link ConfigurationOptions#OPTIMIZE_CMPI_TO_TST}
     * option set to an {@link Object} value as invalid.
     */
    @Test
    public void optimizeCmpiToTstBad() {
        final InvalidEntriesConsumer invalidEntriesConsumer = new InvalidEntriesConsumer(ConfigurationOptions.OPTIMIZE_CMPI_TO_TST);
        final ConfigurationOptions configurationOptions = invalidOption(ConfigurationOptions.OPTIMIZE_CMPI_TO_TST,
                invalidEntriesConsumer);
        invalidEntriesConsumer.assertGotExpectedEntry();
        assertThat(configurationOptions.optimizeCmpiToTst(), is(false));
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeCmpiToTst()} returns <code>false</code> when the
     * {@link ConfigurationOptions} is created with the {@link ConfigurationOptions#OPTIMIZE_CMPI_TO_TST} option set to
     * <code>false</code>.
     */
    @Test
    public void optimizeCmpiToTstFalse() {
        optimizeCmpiToTst(false);
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeCmpiToTst()} returns <code>true</code> when the {@link ConfigurationOptions}
     * is created with the {@link ConfigurationOptions#OPTIMIZE_CMPI_TO_TST} option set to <code>true</code>.
     */
    @Test
    public void optimizeCmpiToTstTrue() {
        optimizeCmpiToTst(true);
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} treats the
     * {@link ConfigurationOptions#OPTIMIZE_MOVE_TO_MOVEQ} option set to an {@link Object} value as invalid.
     */
    @Test
    public void optimizeMoveToMoveqBad() {
        final InvalidEntriesConsumer invalidEntriesConsumer = new InvalidEntriesConsumer(
                ConfigurationOptions.OPTIMIZE_MOVE_TO_MOVEQ);
        final ConfigurationOptions configurationOptions = invalidOption(ConfigurationOptions.OPTIMIZE_MOVE_TO_MOVEQ,
                invalidEntriesConsumer);
        invalidEntriesConsumer.assertGotExpectedEntry();
        assertThat(configurationOptions.optimizeMoveToMoveq(), is(false));
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeMoveToMoveq()} returns <code>false</code> when the
     * {@link ConfigurationOptions} is created with the {@link ConfigurationOptions#OPTIMIZE_MOVE_TO_MOVEQ} option set to
     * <code>false</code>.
     */
    @Test
    public void optimizeMoveToMoveqFalse() {
        optimizeMoveToMoveq(false);
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeMoveToMoveq()} returns <code>true</code> when the
     * {@link ConfigurationOptions} is created with the {@link ConfigurationOptions#OPTIMIZE_MOVE_TO_MOVEQ} option set to
     * <code>true</code>.
     */
    @Test
    public void optimizeMoveToMoveqTrue() {
        optimizeMoveToMoveq(true);
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} treats the {@link ConfigurationOptions#OPTIMIZE_TO_ADDQ_SUBQ}
     * option set to an {@link Object} value as invalid.
     */
    @Test
    public void optimizeToAddqSubqBad() {
        final InvalidEntriesConsumer invalidEntriesConsumer = new InvalidEntriesConsumer(ConfigurationOptions.OPTIMIZE_TO_ADDQ_SUBQ);
        final ConfigurationOptions configurationOptions = invalidOption(ConfigurationOptions.OPTIMIZE_TO_ADDQ_SUBQ,
                invalidEntriesConsumer);
        invalidEntriesConsumer.assertGotExpectedEntry();
        assertThat(configurationOptions.optimizeToAddqSubq(), is(false));
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeToAddqSubq()} returns <code>false</code> when the
     * {@link ConfigurationOptions} is created with the {@link ConfigurationOptions#OPTIMIZE_TO_ADDQ_SUBQ} option set to
     * <code>false</code>.
     */
    @Test
    public void optimizeToAddqSubqFalse() {
        optimizeToAddqSubq(false);
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeToAddqSubq()} returns <code>true</code> when the
     * {@link ConfigurationOptions} is created with the {@link ConfigurationOptions#OPTIMIZE_TO_ADDQ_SUBQ} option set to
     * <code>true</code>.
     */
    @Test
    public void optimizeToAddqSubqTrue() {
        optimizeToAddqSubq(true);
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} treats the
     * {@link ConfigurationOptions#OPTIMIZE_UNSIZED_ABSOLUTE_ADDRESSING_TO_PC_RELATIVE} option set to an {@link Object} value as
     * invalid.
     */
    @Test
    public void optimizeUnsizedAbsoluteAddressingToPcRelativeBad() {
        final InvalidEntriesConsumer invalidEntriesConsumer = new InvalidEntriesConsumer(
                ConfigurationOptions.OPTIMIZE_UNSIZED_ABSOLUTE_ADDRESSING_TO_PC_RELATIVE);
        final ConfigurationOptions configurationOptions = invalidOption(
                ConfigurationOptions.OPTIMIZE_UNSIZED_ABSOLUTE_ADDRESSING_TO_PC_RELATIVE, invalidEntriesConsumer);
        invalidEntriesConsumer.assertGotExpectedEntry();
        assertThat(configurationOptions.optimizeUnsizedAbsoluteAddressingToPcRelative(), is(false));
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeUnsizedAbsoluteAddressingToPcRelative()} returns <code>false</code> when the
     * {@link ConfigurationOptions} is created with the
     * {@link ConfigurationOptions#OPTIMIZE_UNSIZED_ABSOLUTE_ADDRESSING_TO_PC_RELATIVE} option set to <code>false</code>.
     */
    @Test
    public void optimizeUnsizedAbsoluteAddressingToPcRelativeFalse() {
        optimizeUnsizedAbsoluteAddressingToPcRelative(false);
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeUnsizedAbsoluteAddressingToPcRelative()} returns <code>true</code> when the
     * {@link ConfigurationOptions} is created with the
     * {@link ConfigurationOptions#OPTIMIZE_UNSIZED_ABSOLUTE_ADDRESSING_TO_PC_RELATIVE} option set to <code>true</code>.
     */
    @Test
    public void optimizeUnsizedAbsoluteAddressingToPcRelativeTrue() {
        optimizeUnsizedAbsoluteAddressingToPcRelative(true);
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} treats the
     * {@link ConfigurationOptions#OPTIMIZE_UNSIZED_BRANCHES} option set to an {@link Object} value as invalid.
     */
    @Test
    public void optimizeUnsizedBranchesBad() {
        final InvalidEntriesConsumer invalidEntriesConsumer = new InvalidEntriesConsumer(
                ConfigurationOptions.OPTIMIZE_UNSIZED_BRANCHES);
        final ConfigurationOptions configurationOptions = invalidOption(ConfigurationOptions.OPTIMIZE_UNSIZED_BRANCHES,
                invalidEntriesConsumer);
        invalidEntriesConsumer.assertGotExpectedEntry();
        assertThat(configurationOptions.optimizeUnsizedBranches(), is(false));
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeUnsizedBranches()} returns <code>false</code> when the
     * {@link ConfigurationOptions} is created with the {@link ConfigurationOptions#OPTIMIZE_UNSIZED_BRANCHES} option set to
     * <code>false</code>.
     */
    @Test
    public void optimizeUnsizedBranchesFalse() {
        optimizeUnsizedBranches(false);
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeUnsizedBranches()} returns <code>true</code> when the
     * {@link ConfigurationOptions} is created with the {@link ConfigurationOptions#OPTIMIZE_UNSIZED_BRANCHES} option set to
     * <code>true</code>.
     */
    @Test
    public void optimizeUnsizedBranchesTrue() {
        optimizeUnsizedBranches(true);
    }

    /**
     * Asserts that {@link ConfigurationOptions#create(Map, Consumer)} treats the
     * {@link ConfigurationOptions#OPTIMIZE_ZERO_DISPLACEMENT} option set to an {@link Object} value as invalid.
     */
    @Test
    public void optimizeZeroDisplacementBad() {
        final InvalidEntriesConsumer invalidEntriesConsumer = new InvalidEntriesConsumer(
                ConfigurationOptions.OPTIMIZE_ZERO_DISPLACEMENT);
        final ConfigurationOptions configurationOptions = invalidOption(ConfigurationOptions.OPTIMIZE_ZERO_DISPLACEMENT,
                invalidEntriesConsumer);
        invalidEntriesConsumer.assertGotExpectedEntry();
        assertThat(configurationOptions.optimizeZeroDisplacement(), is(false));
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeZeroDisplacement()} returns <code>false</code> when the
     * {@link ConfigurationOptions} is created with the {@link ConfigurationOptions#OPTIMIZE_ZERO_DISPLACEMENT} option set to
     * <code>false</code>.
     */
    @Test
    public void optimizeZeroDisplacementFalse() {
        optimizeZeroDisplacement(false);
    }

    /**
     * Asserts that {@link ConfigurationOptions#optimizeZeroDisplacement()} returns <code>true</code> when the
     * {@link ConfigurationOptions} is created with the {@link ConfigurationOptions#OPTIMIZE_ZERO_DISPLACEMENT} option set to
     * <code>true</code>.
     */
    @Test
    public void optimizeZeroDisplacementTrue() {
        optimizeZeroDisplacement(true);
    }

}
