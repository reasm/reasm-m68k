package org.reasm.m68k.assembly.internal;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.Function;
import org.reasm.FunctionValue;
import org.reasm.StaticSymbol;
import org.reasm.Symbol;
import org.reasm.UnsignedIntValue;
import org.reasm.expressions.EvaluationContext;
import org.reasm.expressions.Expression;
import org.reasm.expressions.SymbolLookup;
import org.reasm.expressions.ValueExpression;
import org.reasm.m68k.InstructionSet;
import org.reasm.m68k.expressions.internal.Tokenizer;
import org.reasm.m68k.messages.*;
import org.reasm.testhelpers.AssemblyMessageCollector;
import org.reasm.testhelpers.DummySymbolLookup;
import org.reasm.testhelpers.EquivalentAssemblyMessage;
import org.reasm.testhelpers.SingleSymbolLookup;

/**
 * Test class for {@link EffectiveAddress}.
 *
 * @author Francis Gagné
 */
@SuppressWarnings("javadoc")
public class EffectiveAddressTest {

    public static abstract class BaseSuccessfulTest extends BaseTestWithOutputCheck<BaseTestWithOutputCheck.DataItem> {

        protected BaseSuccessfulTest(@Nonnull DataItem data) {
            super(data);
        }

        @Override
        protected void checkMessages(ArrayList<AssemblyMessage> messages) {
            assertThat(messages, is(empty()));
        }

    }

    public static abstract class BaseTest<TDataItem extends BaseTest.DataItem> {

        public static class DataItem {

            @Nonnull
            private static final M68KTestAssemblyContext EMPTY_ASSEMBLY_CONTEXT = new M68KTestAssemblyContext();

            @Nonnull
            final String text;
            @Nonnull
            final Set<AddressingMode> validAddressingModes;
            final boolean expectBitFieldSpecification;
            @Nonnull
            final InstructionSize instructionSize;
            @Nonnull
            final InstructionSet instructionSet;
            @CheckForNull
            final SymbolLookup symbolLookup;
            @Nonnull
            final M68KTestAssemblyContext context;

            public DataItem(@Nonnull String text, @Nonnull Set<AddressingMode> validAddressingModes,
                    boolean expectBitFieldSpecification, @Nonnull InstructionSize instructionSize,
                    @Nonnull InstructionSet instructionSet, @CheckForNull SymbolLookup symbolLookup,
                    @CheckForNull M68KTestAssemblyContext context) {
                this.text = text;
                this.validAddressingModes = validAddressingModes;
                this.expectBitFieldSpecification = expectBitFieldSpecification;
                this.instructionSize = instructionSize;
                this.instructionSet = instructionSet;
                this.symbolLookup = symbolLookup;
                this.context = context != null ? context : EMPTY_ASSEMBLY_CONTEXT;
            }

        }

        @Nonnull
        final TDataItem data;

        protected BaseTest(@Nonnull TDataItem data) {
            this.data = data;
        }

        @Test
        public void getEffectiveAddress() {
            try {
                final Tokenizer tokenizer = new Tokenizer();
                final EffectiveAddress ea = new EffectiveAddress();
                final ArrayList<AssemblyMessage> messages = new ArrayList<>();
                tokenizer.setCharSequence(this.data.text);
                this.data.context.instructionSet = this.data.instructionSet;
                EffectiveAddress.getEffectiveAddress(tokenizer, this.data.symbolLookup, this.data.validAddressingModes,
                        this.data.expectBitFieldSpecification, this.data.instructionSize, 2, new EvaluationContext(null, 0, null),
                        this.data.context, new AssemblyMessageCollector(messages), ea);
                this.checkMessages(messages);
                this.checkOutput(ea);
            } catch (AssertionError e) {
                throw new AssertionError("text: " + this.data.text + e.getMessage(), e);
            }
        }

        protected abstract void checkMessages(@Nonnull ArrayList<AssemblyMessage> messages);

        protected abstract void checkOutput(@Nonnull EffectiveAddress ea);

    }

    public static abstract class BaseTestWithOutputCheck<TDataItem extends BaseTestWithOutputCheck.DataItem> extends
            BaseTest<TDataItem> {

        public static class DataItem extends BaseTest.DataItem {

            @Nonnull
            final short[] words;

            public DataItem(@Nonnull String text, @Nonnull Set<AddressingMode> validAddressingModes,
                    boolean expectBitFieldSpecification, @Nonnull InstructionSize instructionSize,
                    @Nonnull InstructionSet instructionSet, @CheckForNull SymbolLookup symbolLookup,
                    @CheckForNull M68KTestAssemblyContext context, short[] words) {
                super(text, validAddressingModes, expectBitFieldSpecification, instructionSize, instructionSet, symbolLookup,
                        context);
                this.words = words;
            }

        }

        protected BaseTestWithOutputCheck(@Nonnull TDataItem data) {
            super(data);
        }

        @Override
        protected void checkOutput(@Nonnull EffectiveAddress ea) {
            assertThat((int) ea.numberOfWords, is(this.data.words.length));
            for (int i = 0; i < ea.numberOfWords; i++) {
                assertThat(ea.getWord(i), is(this.data.words[i]));
            }
        }

    }

    @RunWith(Parameterized.class)
    public static class ExtendedSyntaxTest extends BaseSuccessfulTest {

        @Nonnull
        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            // Address register indirect with displacement
            // - MRI syntax
            addDataItem("0(A0)", new short[] { 0b101000, 0x0000 });
            addDataItem("0(a0)", new short[] { 0b101000, 0x0000 });
            addDataItem("0(A7)", new short[] { 0b101111, 0x0000 });
            addDataItem("-1(A0)", new short[] { 0b101000, -0x0001 });
            addDataItem("-1(A7)", new short[] { 0b101111, -0x0001 });
            addDataItem("--1(A0)", new short[] { 0b101000, 0x0001 });
            addDataItem("2+2(A0)", new short[] { 0b101000, 0x0004 });
            addDataItem("2+2*2(A0)", new short[] { 0b101000, 0x0006 });
            addDataItem("1?2:4(A0)", new short[] { 0b101000, 0x0002 });
            addDataItem("1?2:4?8:16(A0)", new short[] { 0b101000, 0x0002 });
            // - reordered parts
            addDataItem("(A0,0)", new short[] { 0b101000, 0x0000 });

            // Address register indirect with index (8-bit displacement)
            // - MRI syntax
            addDataItem("0(A0,D0)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("0(A0,d0)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("0(A0,D0.W)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("0(A0,D0.w)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("0(A0,d0.W)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("0(A0,d0.w)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("0(A0,D0*1)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("0(A0,D0*2)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000010_00000000 });
            addDataItem("0(A0,D0*4)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000100_00000000 });
            addDataItem("0(A0,D0*8)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000110_00000000 });
            addDataItem("0(A0,D0*8)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000110_00000000 });
            addDataItem("0(A0,A7.L)", new short[] { 0b110000, (short) 0b11111000_00000000 });
            addDataItem("0(A7,D0)", new short[] { 0b110111, 0b00000000_00000000 });
            addDataItem("-1(A0,D0)", new short[] { 0b110000, 0b00000000_11111111 });
            addDataItem("-1(A7,A7.L)", new short[] { 0b110111, (short) 0b11111000_11111111 });
            addDataItem("-1(A7,A7.L*8)", InstructionSet.CPU32, new short[] { 0b110111, (short) 0b11111110_11111111 });
            addDataItem("-1(A7,A7.L*8)", InstructionSet.MC68020, new short[] { 0b110111, (short) 0b11111110_11111111 });
            // - reordered parts
            addDataItem("(D0,A0)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(D0.W,A0)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(D0.W*8,A0)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000110_00000000 });
            addDataItem("(A0.W,A0)", new short[] { 0b110000, (short) 0b10000000_00000000 });
            addDataItem("(0,D0,A0)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(D0,0,A0)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(D0,A0,0)", new short[] { 0b110000, 0b00000000_00000000 });
            // - MRI syntax + reordered parts
            addDataItem("0(D0,A0)", new short[] { 0b110000, 0b00000000_00000000 });

            // Address register indirect with index (base displacement)
            // - MRI syntax
            addDataItem("$80(A0,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("$80(A0,D0.W)", InstructionSet.MC68020, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("$80(A0,D0.W*1)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("$80(A0,D0.W*8)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000111_00100000, 0x0080 });
            addDataItem("$80(A0,A7.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b11110001_00100000, 0x0080 });
            addDataItem("$80(A7,D0.W)", InstructionSet.CPU32, new short[] { 0b110111, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("$7FFF(A0,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x7FFF });
            addDataItem("$8000(A0,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00110000, 0x0000,
                    (short) 0x8000 });
            addDataItem("$FFFF7FFF(A0,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00110000,
                    (short) 0xFFFF, 0x7FFF });
            addDataItem("-$81(A7,A7.L*8)", InstructionSet.CPU32, new short[] { 0b110111, (short) 0b11111111_00100000, -0x0081 });
            addDataItem("-$8001(A7,A7.L*8)", InstructionSet.CPU32, new short[] { 0b110111, (short) 0b11111111_00110000,
                    (short) 0xFFFF, 0x7FFF });
            addDataItem("$10000(A0)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_01110000, 0x0001, 0x0000 });
            addDataItem("1(D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10100000, 0x0001 });
            addDataItem("1(D0.W*8)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000111_10100000, 0x0001 });
            addDataItem("$7FFF(D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10100000, 0x7FFF });
            addDataItem("$FFFF8000(D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10100000, -0x8000 });
            addDataItem("$FFFFFFFF(D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10100000, -0x0001 });
            addDataItem("-$8000(D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_10100000, -0x8000 });
            addDataItem("-1(D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_10100000, -1 });
            addDataItem("-1(A7.L*8)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b11111111_10100000, -1 });
            addDataItem("$8000(D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10110000, 0x0000, (short) 0x8000 });
            addDataItem("$FFFF7FFF(D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10110000, (short) 0xFFFF,
                    0x7FFF });
            addDataItem("$FFFF7FFF(A7.L*8)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b11111111_10110000,
                    (short) 0xFFFF, 0x7FFF });
            // - reordered parts
            addDataItem("($80,D0.W,A0)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("(D0.W,$80,A0)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("(D0.W,A0,$80)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });
            // - MRI syntax + reordered parts
            addDataItem("$80(D0.W,A0)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });

            // Memory indirect
            // - MRI syntax
            addDataItem("1([A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010010, 0x0001 });
            addDataItem("$10000([A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010011, 0x0001, 0x0000 });
            addDataItem("1([A0],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010110, 0x0001 });
            addDataItem("$10000([A0],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010111, 0x0001, 0x0000 });
            addDataItem("1([1,A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("$10000([1,A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("1([1,A0],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("$10000([1,A0],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100111, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("1([$10000,A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("$10000([$10000,A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("1([$10000,A0],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110110, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("$10000([$10000,A0],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110111, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("1([A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01010010, 0x0001 });
            addDataItem("$10000([A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01010011, 0x0001, 0x0000 });
            addDataItem("1([1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01100010, 0x0001, 0x0001 });
            addDataItem("$10000([1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("1([$10000,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("$10000([$10000,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01110011, 0x0001, 0x0000,
                    0x0001, 0x0000 });
            addDataItem("1([D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010010, 0x0001 });
            addDataItem("$10000([D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010011, 0x0001, 0x0000 });
            addDataItem("1([],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010110, 0x0001 });
            addDataItem("$10000([],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010111, 0x0001, 0x0000 });
            addDataItem("1([1,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100010, 0x0001, 0x0001 });
            addDataItem("$10000([1,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("1([1],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100110, 0x0001, 0x0001 });
            addDataItem("$10000([1],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100111, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("1([$10000,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("$10000([$10000,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("1([$10000],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110110, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("$10000([$10000],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110111, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("1([])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11010010, 0x0001 });
            addDataItem("$10000([])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11010011, 0x0001, 0x0000 });
            addDataItem("1([1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11100010, 0x0001, 0x0001 });
            addDataItem("$10000([1])", InstructionSet.MC68020,
                    new short[] { 0b110000, 0b00000001_11100011, 0x0001, 0x0001, 0x0000 });
            addDataItem("1([$10000])", InstructionSet.MC68020,
                    new short[] { 0b110000, 0b00000001_11110010, 0x0001, 0x0000, 0x0001 });
            addDataItem("$10000([$10000])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11110011, 0x0001, 0x0000,
                    0x0001, 0x0000 });
            // - reordered parts
            addDataItem("([D0.W,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010001 });
            addDataItem("(1,[A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010010, 0x0001 });
            addDataItem("(D0.W,[A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010101 });
            addDataItem("(D0.W,1,[A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010110, 0x0001 });
            addDataItem("(1,D0.W,[A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010110, 0x0001 });
            addDataItem("([D0.W,1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100001, 0x0001 });
            addDataItem("([D0.W,A0,1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100001, 0x0001 });
            addDataItem("(1,[1,A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("(D0.W,[1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100101, 0x0001 });
            addDataItem("(D0.W,1,[1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("(1,D0.W,[1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("(1,[A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01010010, 0x0001 });
            addDataItem("([A0,1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01100001, 0x0001 });
            addDataItem("(1,[1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01100010, 0x0001, 0x0001 });
            addDataItem("(1,[D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010010, 0x0001 });
            addDataItem("(D0.W,[])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010101 });
            addDataItem("(D0.W,[],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010110, 0x0001 });
            addDataItem("(1,D0.W,[])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010110, 0x0001 });
            addDataItem("([D0.W,1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100001, 0x0001 });
            addDataItem("(1,[1,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100010, 0x0001, 0x0001 });
            addDataItem("(D0.W,[1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100101, 0x0001 });
            addDataItem("(A0,[1])", InstructionSet.MC68020, new short[] { 0b110000, (short) 0b10000001_10100101, 0x0001 });
            addDataItem("(D0.W,[1],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100110, 0x0001, 0x0001 });
            addDataItem("(D0.W,1,[1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100110, 0x0001, 0x0001 });
            addDataItem("(1,[])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11010010, 0x0001 });
            addDataItem("(1,[1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11100010, 0x0001, 0x0001 });
            // - MRI syntax + reordered parts
            addDataItem("1([A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010010, 0x0001 });
            addDataItem("1(D0.W,[A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010110, 0x0001 });
            addDataItem("1(D0.W,[A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010110, 0x0001 });
            addDataItem("1([1,A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("1(D0.W,[1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("1(D0.W,[1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("1([A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01010010, 0x0001 });
            addDataItem("1([1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01100010, 0x0001, 0x0001 });
            addDataItem("1([D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010010, 0x0001 });
            addDataItem("1(D0.W,[])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010110, 0x0001 });
            addDataItem("1([1,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100010, 0x0001, 0x0001 });
            addDataItem("1(D0.W,[1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100110, 0x0001, 0x0001 });
            addDataItem("1([])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11010010, 0x0001 });
            addDataItem("1([1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11100010, 0x0001, 0x0001 });

            // Absolute short addressing
            // - without parentheses, with explicit size specification
            addDataItem("0.W", new short[] { 0b111000, 0x0000 });
            addDataItem("0.w", new short[] { 0b111000, 0x0000 });
            addDataItem("$FFFFFFFF.W", new short[] { 0b111000, -0x0001 });
            addDataItem("-1.W", new short[] { 0b111000, -0x0001 });
            addDataItem("2+2.W", new short[] { 0b111000, 0x0004 });
            // - with parentheses, without explicit size specification
            addDataItem("(0)", new short[] { 0b111000, 0x0000 });
            addDataItem("($FFFFFFFF)", new short[] { 0b111000, -0x0001 });
            addDataItem("(-1)", new short[] { 0b111000, -0x0001 });
            // - without parentheses or explicit size specification
            addDataItem("0", new short[] { 0b111000, 0x0000 });
            addDataItem("$FFFFFFFF", new short[] { 0b111000, -0x0001 });
            addDataItem("-1", new short[] { 0b111000, -0x0001 });

            // Absolute long addressing
            // - without parentheses, with explicit size specification
            addDataItem("0.L", new short[] { 0b111001, 0x0000, 0x0000 });
            addDataItem("0.l", new short[] { 0b111001, 0x0000, 0x0000 });
            addDataItem("$8000.L", new short[] { 0b111001, 0x0000, (short) 0x8000 });
            addDataItem("$FFFF7FFF.L", new short[] { 0b111001, (short) 0xFFFF, 0x7FFF });
            addDataItem("$FFFFFFFF.L", new short[] { 0b111001, (short) 0xFFFF, (short) 0xFFFF });
            addDataItem("-1.L", new short[] { 0b111001, (short) 0xFFFF, (short) 0xFFFF });
            addDataItem("2+2.L", new short[] { 0b111001, 0x0000, 0x0004 });
            // - with parentheses, without explicit size specification
            addDataItem("($8000)", new short[] { 0b111001, 0x0000, (short) 0x8000 });
            addDataItem("($FFFF7FFF)", new short[] { 0b111001, (short) 0xFFFF, 0x7FFF });
            // - without parentheses or explicit size specification
            addDataItem("$8000", new short[] { 0b111001, 0x0000, (short) 0x8000 });
            addDataItem("$FFFF7FFF", new short[] { 0b111001, (short) 0xFFFF, 0x7FFF });

            // Program counter indirect with displacement
            // - MRI syntax
            addDataItem("0(PC)", new short[] { 0b111010, -0x0002 });
            addDataItem("1(PC)", new short[] { 0b111010, -0x0001 });
            addDataItem("2(PC)", new short[] { 0b111010, 0x0000 });
            addDataItem("3(PC)", new short[] { 0b111010, 0x0001 });
            addDataItem("$8001(PC)", new short[] { 0b111010, 0x7FFF });
            addDataItem("$FFFF8002(PC)", new short[] { 0b111010, -0x8000 });
            addDataItem("1(PC)", new short[] { 0b111010, -0x0001 });
            addDataItem("-$7FFE(PC)", new short[] { 0b111010, -0x8000 });
            addDataItem("-1(PC)", new short[] { 0b111010, -0x0003 });
            // - reordered parts
            addDataItem("(PC,1)", new short[] { 0b111010, -0x0001 });

            // Program counter indirect with index (8-bit displacement)
            // - MRI syntax
            addDataItem("0(PC,D0)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("0(PC,A7.L)", new short[] { 0b111011, (short) 0b11111000_11111110 });
            addDataItem("$81(PC,D0)", new short[] { 0b111011, 0b00000000_01111111 });
            addDataItem("$FFFFFF82(PC,D0)", new short[] { 0b111011, 0b00000000_10000000 });
            addDataItem("1(PC,D0)", new short[] { 0b111011, 0b00000000_11111111 });
            addDataItem("-$7E(PC,D0)", new short[] { 0b111011, 0b00000000_10000000 });
            addDataItem("1(PC,D0)", new short[] { 0b111011, 0b00000000_11111111 });
            addDataItem("1(PC,A7.L)", new short[] { 0b111011, (short) 0b11111000_11111111 });
            addDataItem("1(PC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111110_11111111 });
            addDataItem("1(PC,A7.L*8)", InstructionSet.MC68020, new short[] { 0b111011, (short) 0b11111110_11111111 });
            // - reordered parts
            addDataItem("(D0,PC)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("(A0,PC)", new short[] { 0b111011, (short) 0b10000000_11111110 });
            addDataItem("(D0,PC,0)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("(D0,0,PC)", new short[] { 0b111011, 0b00000000_11111110 });
            // - MRI syntax + reordered parts
            addDataItem("0(D0,PC)", new short[] { 0b111011, 0b00000000_11111110 });

            // Program counter indirect with index (base displacement)
            // - MRI syntax
            addDataItem("$82(PC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("$82(PC,D0.W)", InstructionSet.MC68020, new short[] { 0b111011, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("$82(PC,D0.W*1)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("$82(PC,D0.W*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000111_00100000, 0x0080 });
            addDataItem("$82(PC,A7.W)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11110001_00100000, 0x0080 });
            addDataItem("$8001(PC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000001_00100000, 0x7FFF });
            addDataItem("$8002(PC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000001_00110000, 0x0000,
                    (short) 0x8000 });
            addDataItem("$FFFF8001(PC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000001_00110000,
                    (short) 0xFFFF, 0x7FFF });
            addDataItem("-$7F(PC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111111_00100000, -0x0081 });
            addDataItem("-$7FFF(PC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111111_00110000,
                    (short) 0xFFFF, 0x7FFF });
            addDataItem("$10002(PC)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000001_01110000, 0x0001, 0x0000 });
            addDataItem("3(ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, 0x0001 });
            addDataItem("3(ZPC,D0.W*8)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000111_10100000, 0x0001 });
            addDataItem("$8001(ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, 0x7FFF });
            addDataItem("$FFFF8002(ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, -0x8000 });
            addDataItem("1(ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, -0x0001 });
            addDataItem("-$7FFE(ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000001_10100000, -0x8000 });
            addDataItem("1(ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000001_10100000, -1 });
            addDataItem("1(ZPC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111111_10100000, -1 });
            addDataItem("$8002(ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10110000, 0x0000,
                    (short) 0x8000 });
            addDataItem("$FFFF8001(ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10110000, (short) 0xFFFF,
                    0x7FFF });
            addDataItem("$FFFF8001(ZPC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111111_10110000,
                    (short) 0xFFFF, 0x7FFF });
            // - reordered parts
            addDataItem("($82,D0.W,PC)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_00100000, 0x0080 });
            addDataItem("(D0.W,PC,$82)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_00100000, 0x0080 });
            addDataItem("(D0.W,ZPC)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, -0x0002 });
            addDataItem("(A0,ZPC)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b10000001_10100000, -0x0002 });
            addDataItem("(ZPC,3,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, 0x0001 });
            addDataItem("(ZPC,D0.W,3)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, 0x0001 });
            // - MRI syntax + reordered parts
            addDataItem("$82(D0.W,PC)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_00100000, 0x0080 });
            addDataItem("3(D0.W,ZPC)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, 0x0001 });

            // Program counter memory indirect
            // - MRI syntax
            addDataItem("1([PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, -0x0002, 0x0001 });
            addDataItem("$10000([PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100011, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("1([PC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100110, -0x0002, 0x0001 });
            addDataItem("$10000([PC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100111, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("1([3,PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("$10000([3,PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("1([3,PC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("$10000([3,PC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100111, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("1([$10002,PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("$10000([$10002,PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("1([$10002,PC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110110, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("$10000([$10002,PC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110111, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("1([PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100010, -0x0002, 0x0001 });
            addDataItem("$10000([PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100011, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("1([3,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100010, 0x0001, 0x0001 });
            addDataItem("$10000([3,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("1([$10002,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("$10000([$10002,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01110011, 0x0001, 0x0000,
                    0x0001, 0x0000 });
            addDataItem("1([ZPC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100010, -0x0002, 0x0001 });
            addDataItem("$10000([ZPC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100011, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("1([ZPC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100110, -0x0002, 0x0001 });
            addDataItem("$10000([ZPC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100111, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("1([3,ZPC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100010, 0x0001, 0x0001 });
            addDataItem("$10000([3,ZPC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100011, 0x0001,
                    0x0001, 0x0000 });
            addDataItem("1([3,ZPC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100110, 0x0001, 0x0001 });
            addDataItem("$10000([3,ZPC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100111, 0x0001,
                    0x0001, 0x0000 });
            addDataItem("1([$10002,ZPC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10110010, 0x0001,
                    0x0000, 0x0001 });
            addDataItem("$10000([$10002,ZPC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("1([$10002,ZPC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10110110, 0x0001,
                    0x0000, 0x0001 });
            addDataItem("$10000([$10002,ZPC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10110111, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("1([ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100010, -0x0002, 0x0001 });
            addDataItem("$10000([ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100011, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("1([3,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100010, 0x0001, 0x0001 });
            addDataItem("$10000([3,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("1([$10002,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("$10000([$10002,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            // - reordered parts
            addDataItem("([D0.W,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100001, -0x0002 });
            addDataItem("([D0.W,PC],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, -0x0002, 0x0001 });
            addDataItem("(D0.W,[PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100101, -0x0002 });
            addDataItem("([PC,D0.W,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100001, 0x0001 });
            addDataItem("([D0.W,PC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100001, 0x0001 });
            addDataItem("(1,[3,PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("(1,[3,D0.W,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("([PC,3],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100101, 0x0001 });
            addDataItem("(D0.W,[PC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100101, 0x0001 });
            addDataItem("(D0.W,1,[3,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("(D0.W,[3,PC],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("(1,[PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100010, -0x0002, 0x0001 });
            addDataItem("([PC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100001, 0x0001 });
            addDataItem("(1,[PC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100010, 0x0001, 0x0001 });
            addDataItem("([D0.W,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100001, -0x0002 });
            addDataItem("(1,[D0.W,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100010, -0x0002, 0x0001 });
            addDataItem("(D0.W,[ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100101, -0x0002 });
            addDataItem("(D0.W,[ZPC],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100110, -0x0002, 0x0001 });
            addDataItem("([ZPC,3,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100001, 0x0001 });
            addDataItem("(1,[3,D0.W,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100010, 0x0001, 0x0001 });
            addDataItem("(D0.W,[ZPC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100101, 0x0001 });
            addDataItem("(1,D0.W,[3,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100110, 0x0001, 0x0001 });
            addDataItem("(1,[ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100010, -0x0002, 0x0001 });
            addDataItem("([ZPC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100001, 0x0001 });
            addDataItem("(1,[ZPC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100010, 0x0001, 0x0001 });
            // - MRI syntax + reordered parts
            addDataItem("1([D0.W,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, -0x0002, 0x0001 });
            addDataItem("1([3,D0.W,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("1([D0.W,3,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("1(D0.W,[3,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("1(D0.W,[PC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("1([PC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100010, 0x0001, 0x0001 });
            addDataItem("1([D0.W,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100010, -0x0002, 0x0001 });
            addDataItem("1(D0.W,[ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100110, -0x0002, 0x0001 });
            addDataItem("1([ZPC,D0.W,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100010, 0x0001, 0x0001 });
            addDataItem("1(D0.W,[ZPC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100110, 0x0001, 0x0001 });
            addDataItem("1([ZPC,3])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100010, 0x0001, 0x0001 });

            // Immediate data
            // - float literal
            addDataItem("#0.0", new short[] { 0b111100, 0x0000 });

            // Optimizations
            // - zero displacement optimization
            M68KTestAssemblyContext context = new M68KTestAssemblyContext();
            context.optimizeZeroDisplacement = true;
            addDataItem("0(A0)", context, new short[] { 0b010000 });
            addDataItem("0(a0)", context, new short[] { 0b010000 });
            addDataItem("(A0,0)", context, new short[] { 0b010000 });
            addDataItem("$7FFF(A0)", context, new short[] { 0b101000, 0x7FFF });
            addDataItem("2(PC)", context, new short[] { 0b111010, 0x0000 });
        }

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSet instructionSet, @Nonnull short[] words) {
            addDataItem(text, InstructionSize.DEFAULT, instructionSet, null, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSize instructionSize,
                @Nonnull InstructionSet instructionSet, @CheckForNull M68KTestAssemblyContext context, short[] words) {
            TEST_DATA.add(new Object[] { new DataItem(text, AddressingModeCategory.ALL, false, instructionSize, instructionSet,
                    DummySymbolLookup.DEFAULT, context, words) });
        }

        private static void addDataItem(@Nonnull String text, @CheckForNull M68KTestAssemblyContext context, @Nonnull short[] words) {
            addDataItem(text, InstructionSize.DEFAULT, InstructionSet.MC68000, context, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull short[] words) {
            addDataItem(text, InstructionSize.DEFAULT, InstructionSet.MC68000, null, words);
        }

        public ExtendedSyntaxTest(@Nonnull DataItem data) {
            super(data);
        }

    }

    @RunWith(Parameterized.class)
    public static class MessageTest extends BaseTestWithOutputCheck<MessageTest.DataItem> {

        public static class DataItem extends BaseTestWithOutputCheck.DataItem {

            @Nonnull
            final Matcher<AssemblyMessage>[] messageMatchers;

            public DataItem(@Nonnull String text, boolean expectBitFieldSpecification, @Nonnull InstructionSize instructionSize,
                    @Nonnull InstructionSet instructionSet, @CheckForNull SymbolLookup symbolLookup,
                    @Nonnull Matcher<AssemblyMessage>[] messageMatchers, @Nonnull short[] words) {
                super(text, AddressingModeCategory.ALL, expectBitFieldSpecification, instructionSize, instructionSet, symbolLookup,
                        null, words);
                this.messageMatchers = messageMatchers;
            }

        }

        @Nonnull
        private static final short[] NO_WORDS = new short[0];

        @Nonnull
        private static final EquivalentAssemblyMessage[] SYNTAX_ERROR = new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                new SyntaxErrorInEffectiveAddressErrorMessage()) };

        @Nonnull
        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            SymbolLookup symbolLookup;

            // Syntax errors
            addDataItem("", SYNTAX_ERROR, NO_WORDS);
            addDataItem("(", SYNTAX_ERROR, NO_WORDS);
            addDataItem("(0", SYNTAX_ERROR, NO_WORDS);
            addDataItem("(A0)-", SYNTAX_ERROR, NO_WORDS);
            addDataItem("(0,0)", SYNTAX_ERROR, NO_WORDS);
            addDataItem("([A0],0,0)", SYNTAX_ERROR, NO_WORDS);
            addDataItem("(A0,D0.W)!", SYNTAX_ERROR, new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("([0)", SYNTAX_ERROR, NO_WORDS);
            addDataItem("([0,[0]])", SYNTAX_ERROR, NO_WORDS);
            addDataItem("(0])", SYNTAX_ERROR, NO_WORDS);
            addDataItem("(PC,0,[A0])", SYNTAX_ERROR, NO_WORDS);
            addDataItem("2+", SYNTAX_ERROR, NO_WORDS);
            addDataItem("(D0)+", SYNTAX_ERROR, NO_WORDS);
            addDataItem("(A0)+!", SYNTAX_ERROR, NO_WORDS);
            addDataItem("((A0))+", SYNTAX_ERROR, NO_WORDS);
            addDataItem("D0{1:1}", SYNTAX_ERROR, NO_WORDS);
            addDataItem("D0", true, SYNTAX_ERROR, NO_WORDS);

            addDataItem("#",
                    new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(new ExpressionExpectedErrorMessage()) },
                    new short[] { 0b111100, 0x0000 });
            addDataItem("#0!", new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new EndOfExpressionExpectedErrorMessage()) }, new short[] { 0b111100, 0x0000 });

            addDataItem("$G",
                    new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(new InvalidTokenErrorMessage("$G")) }, NO_WORDS);

            // Validations against the scale on an index register
            addDataItem("(A0,D0.W*3)", InstructionSet.CPU32, new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new InvalidScaleValueErrorMessage(3)) }, new short[] { 0b110000, 0b00000000_00000000 });

            // Validations against the instruction set
            addDataItem("(A0,D0.W*8)", new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new ScaleSpecificationNotSupportedErrorMessage()) }, new short[] { 0b110000, 0b00000110_00000000 });
            addDataItem("($10000,A0)", new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new BaseDisplacementOutOfRangeErrorMessage()) }, new short[] { 0b101000, 0b00000000_00000000 });
            addDataItem("($100,A0,D0.W)", new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new BaseDisplacementOutOfRangeErrorMessage()) }, new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("($10,D0.W)", new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new AddressingModeNotSupportedErrorMessage()) }, new short[] { 0b110000, 0b00000001_10100000, 0x0010 });
            addDataItem("([A0])", new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new AddressingModeNotSupportedErrorMessage()) }, new short[] { 0b110000, 0b00000001_01010001 });
            addDataItem("([A0])", InstructionSet.CPU32, new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new AddressingModeNotSupportedErrorMessage()) }, new short[] { 0b110000, 0b00000001_01010001 });

            // Bound validations
            addDataItem("#$100", InstructionSize.BYTE, new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new ValueOutOfRangeErrorMessage(0x100)) }, new short[] { 0b111100, 0x0000 });
            addDataItem("#-$81", InstructionSize.BYTE, new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new ValueOutOfRangeErrorMessage(-0x81)) }, new short[] { 0b111100, 0x007F });
            addDataItem("#$10000", InstructionSize.WORD, new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new ValueOutOfRangeErrorMessage(0x10000)) }, new short[] { 0b111100, 0x0000 });
            addDataItem("#-$8001", InstructionSize.WORD, new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new ValueOutOfRangeErrorMessage(-0x8001)) }, new short[] { 0b111100, 0x7FFF });

            // String length validation
            addDataItem("#'ABCDE'", InstructionSize.LONG, new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new StringTooLongErrorMessage("ABCDE")) }, new short[] { 0b111100, 0x4142, 0x4344 });

            // Conversion of function to integer
            symbolLookup = new SingleSymbolLookup("foo", ONE_FUNCTION);
            addDataItem("#foo", symbolLookup, new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new FunctionCannotBeConvertedToIntegerErrorMessage()) }, new short[] { 0b111100, 0x0000 });

            // Loss of precision
            addDataItem("#0.5", new EquivalentAssemblyMessage[] { new EquivalentAssemblyMessage(
                    new LossyConversionFromRealToIntegerWarningMessage(0.5)) }, new short[] { 0b111100, 0x0000 });
        }

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static void addDataItem(@Nonnull String text, boolean expectBitFieldSpecification,
                @Nonnull InstructionSize instructionSize, @Nonnull InstructionSet instructionSet,
                @CheckForNull SymbolLookup symbolLookup, @Nonnull Matcher<AssemblyMessage>[] messageMatchers, @Nonnull short[] words) {
            TEST_DATA.add(new Object[] { new DataItem(text, expectBitFieldSpecification, instructionSize, instructionSet,
                    symbolLookup, messageMatchers, words) });
        }

        private static void addDataItem(@Nonnull String text, boolean expectBitFieldSpecification,
                @Nonnull Matcher<AssemblyMessage>[] messageMatchers, @Nonnull short[] words) {
            addDataItem(text, expectBitFieldSpecification, InstructionSize.DEFAULT, InstructionSet.MC68000, null, messageMatchers,
                    words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSet instructionSet,
                @Nonnull Matcher<AssemblyMessage>[] messageMatchers, @Nonnull short[] words) {
            addDataItem(text, false, InstructionSize.DEFAULT, instructionSet, null, messageMatchers, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSize instructionSize,
                @Nonnull EquivalentAssemblyMessage[] messageMatchers, @Nonnull short[] words) {
            addDataItem(text, false, instructionSize, InstructionSet.MC68000, null, messageMatchers, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull Matcher<AssemblyMessage>[] messageMatchers,
                @Nonnull short[] words) {
            addDataItem(text, false, InstructionSize.DEFAULT, InstructionSet.MC68000, null, messageMatchers, words);
        }

        private static void addDataItem(@Nonnull String text, @CheckForNull SymbolLookup symbolLookup,
                @Nonnull EquivalentAssemblyMessage[] messageMatchers, @Nonnull short[] words) {
            addDataItem(text, false, InstructionSize.DEFAULT, InstructionSet.MC68000, symbolLookup, messageMatchers, words);
        }

        public MessageTest(@Nonnull DataItem data) {
            super(data);
        }

        @Override
        protected void checkMessages(ArrayList<AssemblyMessage> messages) {
            assertThat(messages, contains(this.data.messageMatchers));
        }

    }

    @RunWith(Parameterized.class)
    public static class RegisterAliasTest extends BaseSuccessfulTest {

        @Nonnull
        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            M68KTestAssemblyContext context;

            context = new M68KTestAssemblyContext();
            context.registerAliases.put("foo", GeneralPurposeRegister.D0);
            addDataItem("foo", context, new short[] { 0b000000 });
            addDataItem("(foo)", InstructionSet.CPU32, context, new short[] { 0b110000, 0b00000001_10010000 });
            addDataItem("(foo.W)", InstructionSet.CPU32, context, new short[] { 0b110000, 0b00000001_10010000 });
            addDataItem("(foo.w)", InstructionSet.CPU32, context, new short[] { 0b110000, 0b00000001_10010000 });
            addDataItem("(foo.L)", InstructionSet.CPU32, context, new short[] { 0b110000, 0b00001001_10010000 });
            addDataItem("(foo.L)", InstructionSet.CPU32, context, new short[] { 0b110000, 0b00001001_10010000 });

            context = new M68KTestAssemblyContext();
            context.registerAliases.put("foo", GeneralPurposeRegister.A0);
            addDataItem("foo", context, new short[] { 0b001000 });
            addDataItem("(foo)", InstructionSet.CPU32, context, new short[] { 0b010000 });
            addDataItem("(foo.W)", InstructionSet.CPU32, context, new short[] { 0b110000, (short) 0b10000001_10010000 });
            addDataItem("(foo.w)", InstructionSet.CPU32, context, new short[] { 0b110000, (short) 0b10000001_10010000 });
            addDataItem("(foo.L)", InstructionSet.CPU32, context, new short[] { 0b110000, (short) 0b10001001_10010000 });
            addDataItem("(foo.L)", InstructionSet.CPU32, context, new short[] { 0b110000, (short) 0b10001001_10010000 });

            context = new M68KTestAssemblyContext();
            context.registerAliases.put("sx", GeneralPurposeRegister.A6);
            addDataItem("sx", context, new short[] { 0b001110 });
        }

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSet instructionSet,
                @Nonnull M68KTestAssemblyContext context, @Nonnull short[] words) {
            addDataItem(text, InstructionSize.DEFAULT, instructionSet, context, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSize instructionSize,
                @Nonnull InstructionSet instructionSet, @Nonnull M68KTestAssemblyContext context, @Nonnull short[] words) {
            TEST_DATA.add(new Object[] { new DataItem(text, AddressingModeCategory.ALL, false, instructionSize, instructionSet,
                    DummySymbolLookup.DEFAULT, context, words) });
        }

        private static void addDataItem(@Nonnull String text, @Nonnull M68KTestAssemblyContext context, @Nonnull short[] words) {
            addDataItem(text, InstructionSize.DEFAULT, InstructionSet.MC68000, context, words);
        }

        public RegisterAliasTest(@Nonnull DataItem data) {
            super(data);
        }

    }

    @RunWith(Parameterized.class)
    public static class StandardSyntaxTest extends BaseSuccessfulTest {

        @Nonnull
        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            // Data register direct
            addDataItem("D0", new short[] { 0b000000 });
            addDataItem("D1", new short[] { 0b000001 });
            addDataItem("D2", new short[] { 0b000010 });
            addDataItem("D3", new short[] { 0b000011 });
            addDataItem("D4", new short[] { 0b000100 });
            addDataItem("D5", new short[] { 0b000101 });
            addDataItem("D6", new short[] { 0b000110 });
            addDataItem("D7", new short[] { 0b000111 });
            addDataItem("d0", new short[] { 0b000000 });

            // Address register direct
            addDataItem("A0", new short[] { 0b001000 });
            addDataItem("A1", new short[] { 0b001001 });
            addDataItem("A2", new short[] { 0b001010 });
            addDataItem("A3", new short[] { 0b001011 });
            addDataItem("A4", new short[] { 0b001100 });
            addDataItem("A5", new short[] { 0b001101 });
            addDataItem("A6", new short[] { 0b001110 });
            addDataItem("A7", new short[] { 0b001111 });
            addDataItem("a0", new short[] { 0b001000 });
            addDataItem("SP", new short[] { 0b001111 });
            addDataItem("sp", new short[] { 0b001111 });

            // Address register indirect
            addDataItem("(A0)", new short[] { 0b010000 });
            addDataItem("(A7)", new short[] { 0b010111 });
            addDataItem("(A0)", EnumSet.of(AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT), new short[] { 0b101000,
                    0x0000 });
            addDataItem("(A7)", EnumSet.of(AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT), new short[] { 0b101111,
                    0x0000 });

            // Address register indirect with postincrement
            addDataItem("(A0)+", new short[] { 0b011000 });
            addDataItem("(A7)+", new short[] { 0b011111 });

            // Address register indirect with predecrement
            addDataItem("-(A0)", new short[] { 0b100000 });
            addDataItem("-(A7)", new short[] { 0b100111 });

            // Address register indirect with displacement
            addDataItem("(0,A0)", new short[] { 0b101000, 0x0000 });
            addDataItem("(0,A7)", new short[] { 0b101111, 0x0000 });
            addDataItem("($7FFF,A0)", new short[] { 0b101000, 0x7FFF });
            addDataItem("($FFFF8000,A0)", new short[] { 0b101000, -0x8000 });
            addDataItem("($FFFFFFFF,A0)", new short[] { 0b101000, -0x0001 });
            addDataItem("(-$8000,A0)", new short[] { 0b101000, -0x8000 });
            addDataItem("(-1,A0)", new short[] { 0b101000, -1 });
            addDataItem("(-1,A7)", new short[] { 0b101111, -1 });

            // Address register indirect with index (8-bit displacement)
            addDataItem("(A0,D0)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(A0,d0)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(A0,D0.W)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(A0,D0.w)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(A0,d0.W)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(A0,d0.w)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(A0,D0.L)", new short[] { 0b110000, 0b00001000_00000000 });
            addDataItem("(A0,D0.l)", new short[] { 0b110000, 0b00001000_00000000 });
            addDataItem("(A0,d0.L)", new short[] { 0b110000, 0b00001000_00000000 });
            addDataItem("(A0,d0.l)", new short[] { 0b110000, 0b00001000_00000000 });
            addDataItem("(A0,D0.W*1)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(A0,D0.W*2)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000010_00000000 });
            addDataItem("(A0,D0.W*4)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000100_00000000 });
            addDataItem("(A0,D0.W*8)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000110_00000000 });
            addDataItem("(A0,D0.W * 8)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000110_00000000 });
            addDataItem("(A0,D0.W*8)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000110_00000000 });
            addDataItem("(A0,D1)", new short[] { 0b110000, 0b00010000_00000000 });
            addDataItem("(A0,D2)", new short[] { 0b110000, 0b00100000_00000000 });
            addDataItem("(A0,D4)", new short[] { 0b110000, 0b01000000_00000000 });
            addDataItem("(A0,D6)", new short[] { 0b110000, 0b01100000_00000000 });
            addDataItem("(A0,D7)", new short[] { 0b110000, 0b01110000_00000000 });
            addDataItem("(A0,A0)", new short[] { 0b110000, (short) 0b10000000_00000000 });
            addDataItem("(A0,A0.W)", new short[] { 0b110000, (short) 0b10000000_00000000 });
            addDataItem("(A0,A0.w)", new short[] { 0b110000, (short) 0b10000000_00000000 });
            addDataItem("(A0,a0.W)", new short[] { 0b110000, (short) 0b10000000_00000000 });
            addDataItem("(A0,a0.w)", new short[] { 0b110000, (short) 0b10000000_00000000 });
            addDataItem("(A0,A0.L)", new short[] { 0b110000, (short) 0b10001000_00000000 });
            addDataItem("(A0,A0.l)", new short[] { 0b110000, (short) 0b10001000_00000000 });
            addDataItem("(A0,a0.L)", new short[] { 0b110000, (short) 0b10001000_00000000 });
            addDataItem("(A0,a0.l)", new short[] { 0b110000, (short) 0b10001000_00000000 });
            addDataItem("(A0,A1)", new short[] { 0b110000, (short) 0b10010000_00000000 });
            addDataItem("(A0,A2)", new short[] { 0b110000, (short) 0b10100000_00000000 });
            addDataItem("(A0,A4)", new short[] { 0b110000, (short) 0b11000000_00000000 });
            addDataItem("(A0,A6)", new short[] { 0b110000, (short) 0b11100000_00000000 });
            addDataItem("(A0,A7)", new short[] { 0b110000, (short) 0b11110000_00000000 });
            addDataItem("(A7,D0)", new short[] { 0b110111, 0b00000000_00000000 });
            addDataItem("(A7,A7.L)", new short[] { 0b110111, (short) 0b11111000_00000000 });
            addDataItem("(A7,A7.L*8)", InstructionSet.CPU32, new short[] { 0b110111, (short) 0b11111110_00000000 });
            addDataItem("(0,A0,D0)", new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(0,A0,A7.L)", new short[] { 0b110000, (short) 0b11111000_00000000 });
            addDataItem("(0,A7,D0)", new short[] { 0b110111, 0b00000000_00000000 });
            addDataItem("($7F,A0,D0)", new short[] { 0b110000, 0b00000000_01111111 });
            addDataItem("($FFFFFF80,A0,D0)", new short[] { 0b110000, 0b00000000_10000000 });
            addDataItem("($FFFFFFFF,A0,D0)", new short[] { 0b110000, 0b00000000_11111111 });
            addDataItem("(-$80,A0,D0)", new short[] { 0b110000, 0b00000000_10000000 });
            addDataItem("(-1,A0,D0)", new short[] { 0b110000, 0b00000000_11111111 });
            addDataItem("(-1,A7,A7.L)", new short[] { 0b110111, (short) 0b11111000_11111111 });
            addDataItem("(-1,A7,A7.L*8)", InstructionSet.CPU32, new short[] { 0b110111, (short) 0b11111110_11111111 });
            addDataItem("(-1,A7,A7.L*8)", InstructionSet.MC68020, new short[] { 0b110111, (short) 0b11111110_11111111 });

            // Address register indirect with index (base displacement)
            addDataItem("($80,A0,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("($80,A0,D0.W)", InstructionSet.MC68020, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("($80,A0,D0.W*1)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("($80,A0,D0.W*8)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000111_00100000, 0x0080 });
            addDataItem("($80,A0,A7.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b11110001_00100000, 0x0080 });
            addDataItem("($80,A7,D0.W)", InstructionSet.CPU32, new short[] { 0b110111, (short) 0b00000001_00100000, 0x0080 });
            addDataItem("($7FFF,A0,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00100000, 0x7FFF });
            addDataItem("($8000,A0,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00110000, 0x0000,
                    (short) 0x8000 });
            addDataItem("($FFFF7FFF,A0,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_00110000,
                    (short) 0xFFFF, 0x7FFF });
            addDataItem("(-$81,A7,A7.L*8)", InstructionSet.CPU32, new short[] { 0b110111, (short) 0b11111111_00100000, -0x0081 });
            addDataItem("(-$8001,A7,A7.L*8)", InstructionSet.CPU32, new short[] { 0b110111, (short) 0b11111111_00110000,
                    (short) 0xFFFF, 0x7FFF });
            addDataItem("($10000,A0)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_01110000, 0x0001, 0x0000 });
            addDataItem("(D0)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10010000 });
            addDataItem("(D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10010000 });
            addDataItem("(D0.W*8)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000111_10010000 });
            addDataItem("(A0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b10000001_10010000 });
            addDataItem("(1,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10100000, 0x0001 });
            addDataItem("(1,D0.W*8)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000111_10100000, 0x0001 });
            addDataItem("($7FFF,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10100000, 0x7FFF });
            addDataItem("($FFFF8000,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10100000, -0x8000 });
            addDataItem("($FFFFFFFF,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10100000, -0x0001 });
            addDataItem("(-$8000,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_10100000, -0x8000 });
            addDataItem("(-1,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b00000001_10100000, -1 });
            addDataItem("(-1,A7.L*8)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b11111111_10100000, -1 });
            addDataItem("($8000,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10110000, 0x0000, (short) 0x8000 });
            addDataItem("($FFFF7FFF,D0.W)", InstructionSet.CPU32, new short[] { 0b110000, 0b00000001_10110000, (short) 0xFFFF,
                    0x7FFF });
            addDataItem("($FFFF7FFF,A7.L*8)", InstructionSet.CPU32, new short[] { 0b110000, (short) 0b11111111_10110000,
                    (short) 0xFFFF, 0x7FFF });

            // Memory indirect
            addDataItem("([A0,D0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010001 });
            addDataItem("([A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010001 });
            addDataItem("([A0,D0.W],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010010, 0x0001 });
            addDataItem("([A0,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010011, 0x0001, 0x0000 });
            addDataItem("([A0],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010101 });
            addDataItem("([A0],D0.W,1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010110, 0x0001 });
            addDataItem("([A0],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00010111, 0x0001, 0x0000 });
            addDataItem("([1,A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100001, 0x0001 });
            addDataItem("([1,A0,D0.W],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("([1,A0,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100011, 0x0001,
                    0x0001, 0x0000 });
            addDataItem("([1,A0],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100101, 0x0001 });
            addDataItem("([1,A0],D0.W,1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("([1,A0],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00100111, 0x0001,
                    0x0001, 0x0000 });
            addDataItem("([$10000,A0,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110001, 0x0001, 0x0000 });
            addDataItem("([$10000,A0,D0.W],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110010, 0x0001,
                    0x0000, 0x0001 });
            addDataItem("([$10000,A0,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([$10000,A0],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110101, 0x0001, 0x0000 });
            addDataItem("([$10000,A0],D0.W,1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110110, 0x0001,
                    0x0000, 0x0001 });
            addDataItem("([$10000,A0],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_00110111, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01010001 });
            addDataItem("([A0],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01010010, 0x0001 });
            addDataItem("([A0],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01010011, 0x0001, 0x0000 });
            addDataItem("([1,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01100001, 0x0001 });
            addDataItem("([1,A0],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01100010, 0x0001, 0x0001 });
            addDataItem("([1,A0],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("([$10000,A0])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01110001, 0x0001, 0x0000 });
            addDataItem("([$10000,A0],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("([$10000,A0],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_01110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010001 });
            addDataItem("([D0.W],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010010, 0x0001 });
            addDataItem("([D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010011, 0x0001, 0x0000 });
            addDataItem("([],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010101 });
            addDataItem("([],D0.W,1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010110, 0x0001 });
            addDataItem("([],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10010111, 0x0001, 0x0000 });
            addDataItem("([1,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100001, 0x0001 });
            addDataItem("([1,D0.W],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100010, 0x0001, 0x0001 });
            addDataItem("([1,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("([1],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100101, 0x0001 });
            addDataItem("([1],D0.W,1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100110, 0x0001, 0x0001 });
            addDataItem("([1],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10100111, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("([$10000,D0.W])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110001, 0x0001, 0x0000 });
            addDataItem("([$10000,D0.W],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("([$10000,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([$10000],D0.W)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110101, 0x0001, 0x0000 });
            addDataItem("([$10000],D0.W,1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110110, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("([$10000],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_10110111, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11010001 });
            addDataItem("([],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11010010, 0x0001 });
            addDataItem("([],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11010011, 0x0001, 0x0000 });
            addDataItem("([1])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11100001, 0x0001 });
            addDataItem("([1],1)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11100010, 0x0001, 0x0001 });
            addDataItem("([1],$10000)", InstructionSet.MC68020,
                    new short[] { 0b110000, 0b00000001_11100011, 0x0001, 0x0001, 0x0000 });
            addDataItem("([$10000])", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11110001, 0x0001, 0x0000 });
            addDataItem("([$10000],1)", InstructionSet.MC68020,
                    new short[] { 0b110000, 0b00000001_11110010, 0x0001, 0x0000, 0x0001 });
            addDataItem("([$10000],$10000)", InstructionSet.MC68020, new short[] { 0b110000, 0b00000001_11110011, 0x0001, 0x0000,
                    0x0001, 0x0000 });

            // Absolute short addressing
            addDataItem("(0).W", new short[] { 0b111000, 0x0000 });
            addDataItem("(0).w", new short[] { 0b111000, 0x0000 });
            addDataItem("(1).W", new short[] { 0b111000, 0x0001 });
            addDataItem("($7FFF).W", new short[] { 0b111000, 0x7FFF });
            addDataItem("($FFFF8000).W", new short[] { 0b111000, -0x8000 });
            addDataItem("($FFFFFFFF).W", new short[] { 0b111000, -0x0001 });
            addDataItem("(-$8000).W", new short[] { 0b111000, -0x8000 });
            addDataItem("(-1).W", new short[] { 0b111000, -0x0001 });

            // Absolute long addressing
            addDataItem("(0).L", new short[] { 0b111001, 0x0000, 0x0000 });
            addDataItem("(0).l", new short[] { 0b111001, 0x0000, 0x0000 });
            addDataItem("(1).L", new short[] { 0b111001, 0x0000, 0x0001 });
            addDataItem("($7FFF).L", new short[] { 0b111001, 0x0000, 0x7FFF });
            addDataItem("($8000).L", new short[] { 0b111001, 0x0000, (short) 0x8000 });
            addDataItem("($10000).L", new short[] { 0b111001, 0x0001, 0x0000 });
            addDataItem("($7FFFFFFF).L", new short[] { 0b111001, 0x7FFF, (short) 0xFFFF });
            addDataItem("($80000000).L", new short[] { 0b111001, (short) 0x8000, 0x0000 });
            addDataItem("($FFFF0000).L", new short[] { 0b111001, (short) 0xFFFF, 0x0000 });
            addDataItem("($FFFF7FFF).L", new short[] { 0b111001, (short) 0xFFFF, 0x7FFF });
            addDataItem("($FFFF8000).L", new short[] { 0b111001, (short) 0xFFFF, (short) 0x8000 });
            addDataItem("($FFFFFFFF).L", new short[] { 0b111001, (short) 0xFFFF, (short) 0xFFFF });
            addDataItem("(-$FFFFFFFF).L", new short[] { 0b111001, 0x0000, 0x0001 });
            addDataItem("(-1).L", new short[] { 0b111001, (short) 0xFFFF, (short) 0xFFFF });

            // Program counter indirect with displacement
            addDataItem("(PC)", new short[] { 0b111010, -0x0002 });
            addDataItem("(pc)", new short[] { 0b111010, -0x0002 });
            addDataItem("(0,PC)", new short[] { 0b111010, -0x0002 });
            addDataItem("(1,PC)", new short[] { 0b111010, -0x0001 });
            addDataItem("(2,PC)", new short[] { 0b111010, 0x0000 });
            addDataItem("($8001,PC)", new short[] { 0b111010, 0x7FFF });
            addDataItem("($FFFF8002,PC)", new short[] { 0b111010, -0x8000 });
            addDataItem("($FFFFFFFF,PC)", new short[] { 0b111010, -0x0003 });
            addDataItem("(-$7FFE,PC)", new short[] { 0b111010, -0x8000 });
            addDataItem("(-1,PC)", new short[] { 0b111010, -0x0003 });

            // Program counter indirect with index (8-bit displacement)
            addDataItem("(PC,D0)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("(PC,d0)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("(PC,D0.W)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("(PC,D0.w)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("(PC,d0.W)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("(PC,d0.w)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("(PC,D0.L)", new short[] { 0b111011, 0b00001000_11111110 });
            addDataItem("(PC,D0.l)", new short[] { 0b111011, 0b00001000_11111110 });
            addDataItem("(PC,d0.L)", new short[] { 0b111011, 0b00001000_11111110 });
            addDataItem("(PC,d0.l)", new short[] { 0b111011, 0b00001000_11111110 });
            addDataItem("(PC,D0.W*1)", new short[] { 0b111011, 0b00000000_11111110 });
            addDataItem("(PC,D0.W*2)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000010_11111110 });
            addDataItem("(PC,D0.W*4)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000100_11111110 });
            addDataItem("(PC,D0.W*8)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000110_11111110 });
            addDataItem("(PC,D0.W * 8)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000110_11111110 });
            addDataItem("(PC,D0.W*8)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000110_11111110 });
            addDataItem("(PC,D7)", new short[] { 0b111011, 0b01110000_11111110 });
            addDataItem("(PC,A0)", new short[] { 0b111011, (short) 0b10000000_11111110 });
            addDataItem("(PC,A7)", new short[] { 0b111011, (short) 0b11110000_11111110 });
            addDataItem("(PC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111110_11111110 });
            addDataItem("(2,PC,D0)", new short[] { 0b111011, 0b00000000_00000000 });
            addDataItem("(2,PC,A7.L)", new short[] { 0b111011, (short) 0b11111000_00000000 });
            addDataItem("($81,PC,D0)", new short[] { 0b111011, 0b00000000_01111111 });
            addDataItem("($FFFFFF82,PC,D0)", new short[] { 0b111011, 0b00000000_10000000 });
            addDataItem("(1,PC,D0)", new short[] { 0b111011, 0b00000000_11111111 });
            addDataItem("(-$7E,PC,D0)", new short[] { 0b111011, 0b00000000_10000000 });
            addDataItem("(1,PC,D0)", new short[] { 0b111011, 0b00000000_11111111 });
            addDataItem("(1,PC,A7.L)", new short[] { 0b111011, (short) 0b11111000_11111111 });
            addDataItem("(1,PC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111110_11111111 });
            addDataItem("(1,PC,A7.L*8)", InstructionSet.MC68020, new short[] { 0b111011, (short) 0b11111110_11111111 });

            // Program counter indirect with index (base displacement)
            addDataItem("($82,PC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_00100000, 0x0080 });
            addDataItem("($82,PC,D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100000, 0x0080 });
            addDataItem("($82,PC,D0.W*1)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_00100000, 0x0080 });
            addDataItem("($82,PC,D0.W*8)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000111_00100000, 0x0080 });
            addDataItem("($82,PC,A7.W)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11110001_00100000, 0x0080 });
            addDataItem("($8001,PC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_00100000, 0x7FFF });
            addDataItem("($8002,PC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_00110000, 0x0000,
                    (short) 0x8000 });
            addDataItem("($FFFF8001,PC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_00110000, (short) 0xFFFF,
                    0x7FFF });
            addDataItem("(-$7F,PC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111111_00100000, -0x0081 });
            addDataItem("(-$7FFF,PC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111111_00110000,
                    (short) 0xFFFF, 0x7FFF });
            addDataItem("($10002,PC)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b00000001_01110000, 0x0001, 0x0000 });
            addDataItem("(ZPC)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_11100000, -0x0002 });
            addDataItem("(zpc)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_11100000, -0x0002 });
            addDataItem("(ZPC,D0)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, -0x0002 });
            addDataItem("(ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, -0x0002 });
            addDataItem("(ZPC,D0.W*8)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000111_10100000, -0x0002 });
            addDataItem("(ZPC,A0)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b10000001_10100000, -0x0002 });
            addDataItem("(ZPC,A0.W)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b10000001_10100000, -0x0002 });
            addDataItem("(2,ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10010000 });
            addDataItem("(2,ZPC,D0.W*8)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000111_10010000 });
            addDataItem("($8001,ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, 0x7FFF });
            addDataItem("($FFFF8002,ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, -0x8000 });
            addDataItem("(1,ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, -0x0001 });
            addDataItem("(-$7FFE,ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10100000, -0x8000 });
            addDataItem("(1,ZPC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111111_10100000, -1 });
            addDataItem("($8002,ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10110000, 0x0000,
                    (short) 0x8000 });
            addDataItem("($FFFF8001,ZPC,D0.W)", InstructionSet.CPU32, new short[] { 0b111011, 0b00000001_10110000, (short) 0xFFFF,
                    0x7FFF });
            addDataItem("($FFFF8001,ZPC,A7.L*8)", InstructionSet.CPU32, new short[] { 0b111011, (short) 0b11111111_10110000,
                    (short) 0xFFFF, 0x7FFF });

            // Program counter memory indirect
            addDataItem("([PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100001, -0x0002 });
            addDataItem("([PC,D0.W],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, -0x0002, 0x0001 });
            addDataItem("([PC,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100011, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("([PC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100101, -0x0002 });
            addDataItem("([PC],D0.W,1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100110, -0x0002, 0x0001 });
            addDataItem("([PC],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100111, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("([3,PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100001, 0x0001 });
            addDataItem("([3,PC,D0.W],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100010, 0x0001, 0x0001 });
            addDataItem("([3,PC,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100011, 0x0001,
                    0x0001, 0x0000 });
            addDataItem("([3,PC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100101, 0x0001 });
            addDataItem("([3,PC],D0.W,1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100110, 0x0001, 0x0001 });
            addDataItem("([3,PC],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00100111, 0x0001,
                    0x0001, 0x0000 });
            addDataItem("([$10002,PC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110001, 0x0001, 0x0000 });
            addDataItem("([$10002,PC,D0.W],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110010, 0x0001,
                    0x0000, 0x0001 });
            addDataItem("([$10002,PC,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([$10002,PC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110101, 0x0001, 0x0000 });
            addDataItem("([$10002,PC],D0.W,1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110110, 0x0001,
                    0x0000, 0x0001 });
            addDataItem("([$10002,PC],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_00110111, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100001, -0x0002 });
            addDataItem("([PC],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100010, -0x0002, 0x0001 });
            addDataItem("([PC],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100011, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("([3,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100001, 0x0001 });
            addDataItem("([3,PC],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100010, 0x0001, 0x0001 });
            addDataItem("([3,PC],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("([$10002,PC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01110001, 0x0001, 0x0000 });
            addDataItem("([$10002,PC],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("([$10002,PC],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_01110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([ZPC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100001, -0x0002 });
            addDataItem("([ZPC,D0.W],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100010, -0x0002, 0x0001 });
            addDataItem("([ZPC,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100011, -0x0002,
                    0x0001, 0x0000 });
            addDataItem("([ZPC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100101, -0x0002 });
            addDataItem("([ZPC],D0.W,1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100110, -0x0002, 0x0001 });
            addDataItem("([ZPC],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100111, -0x0002,
                    0x0001, 0x0000 });
            addDataItem("([3,ZPC,D0.W])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100001, 0x0001 });
            addDataItem("([3,ZPC,D0.W],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100010, 0x0001, 0x0001 });
            addDataItem("([3,ZPC,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100011, 0x0001,
                    0x0001, 0x0000 });
            addDataItem("([3,ZPC],D0.W)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100101, 0x0001 });
            addDataItem("([3,ZPC],D0.W,1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100110, 0x0001, 0x0001 });
            addDataItem("([3,ZPC],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10100111, 0x0001,
                    0x0001, 0x0000 });
            addDataItem("([$10002,ZPC,D0.W])", InstructionSet.MC68020,
                    new short[] { 0b111011, 0b00000001_10110001, 0x0001, 0x0000 });
            addDataItem("([$10002,ZPC,D0.W],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10110010, 0x0001,
                    0x0000, 0x0001 });
            addDataItem("([$10002,ZPC,D0.W],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([$10002,ZPC],D0.W)", InstructionSet.MC68020,
                    new short[] { 0b111011, 0b00000001_10110101, 0x0001, 0x0000 });
            addDataItem("([$10002,ZPC],D0.W,1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10110110, 0x0001,
                    0x0000, 0x0001 });
            addDataItem("([$10002,ZPC],D0.W,$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_10110111, 0x0001,
                    0x0000, 0x0001, 0x0000 });
            addDataItem("([ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100001, -0x0002 });
            addDataItem("([ZPC],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100010, -0x0002, 0x0001 });
            addDataItem("([ZPC],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100011, -0x0002, 0x0001,
                    0x0000 });
            addDataItem("([3,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100001, 0x0001 });
            addDataItem("([3,ZPC],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100010, 0x0001, 0x0001 });
            addDataItem("([3,ZPC],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11100011, 0x0001, 0x0001,
                    0x0000 });
            addDataItem("([$10002,ZPC])", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11110001, 0x0001, 0x0000 });
            addDataItem("([$10002,ZPC],1)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11110010, 0x0001, 0x0000,
                    0x0001 });
            addDataItem("([$10002,ZPC],$10000)", InstructionSet.MC68020, new short[] { 0b111011, 0b00000001_11110011, 0x0001,
                    0x0000, 0x0001, 0x0000 });

            // Immediate
            // - byte instruction size
            addDataItem("#0", InstructionSize.BYTE, new short[] { 0b111100, 0x0000 });
            addDataItem("#1", InstructionSize.BYTE, new short[] { 0b111100, 0x0001 });
            addDataItem("#$7F", InstructionSize.BYTE, new short[] { 0b111100, 0x007F });
            addDataItem("#$80", InstructionSize.BYTE, new short[] { 0b111100, 0x0080 });
            addDataItem("#$FF", InstructionSize.BYTE, new short[] { 0b111100, 0x00FF });
            addDataItem("#$FFFFFF80", InstructionSize.BYTE, new short[] { 0b111100, 0x0080 });
            addDataItem("#$FFFFFFFF", InstructionSize.BYTE, new short[] { 0b111100, 0x00FF });
            addDataItem("#-$80", InstructionSize.BYTE, new short[] { 0b111100, 0x0080 });
            addDataItem("#-1", InstructionSize.BYTE, new short[] { 0b111100, 0x00FF });
            addDataItem("#'A'", InstructionSize.BYTE, new short[] { 0b111100, 0x0041 });
            // - word instruction size
            addDataItem("#0", InstructionSize.WORD, new short[] { 0b111100, 0x0000 });
            addDataItem("#1", InstructionSize.WORD, new short[] { 0b111100, 0x0001 });
            addDataItem("#$7FFF", InstructionSize.WORD, new short[] { 0b111100, 0x7FFF });
            addDataItem("#$8000", InstructionSize.WORD, new short[] { 0b111100, (short) 0x8000 });
            addDataItem("#$FFFF", InstructionSize.WORD, new short[] { 0b111100, (short) 0xFFFF });
            addDataItem("#$FFFF8000", InstructionSize.WORD, new short[] { 0b111100, (short) 0x8000 });
            addDataItem("#$FFFFFFFF", InstructionSize.WORD, new short[] { 0b111100, (short) 0xFFFF });
            addDataItem("#-$8000", InstructionSize.WORD, new short[] { 0b111100, (short) 0x8000 });
            addDataItem("#-1", InstructionSize.WORD, new short[] { 0b111100, (short) 0xFFFF });
            addDataItem("#'A'", InstructionSize.WORD, new short[] { 0b111100, 0x0041 });
            addDataItem("#'AB'", InstructionSize.WORD, new short[] { 0b111100, 0x4142 });
            // - long instruction size
            addDataItem("#0", InstructionSize.LONG, new short[] { 0b111100, 0x0000, 0x0000 });
            addDataItem("#1", InstructionSize.LONG, new short[] { 0b111100, 0x0000, 0x0001 });
            addDataItem("#$7FFFFFFF", InstructionSize.LONG, new short[] { 0b111100, 0x7FFF, (short) 0xFFFF });
            addDataItem("#$80000000", InstructionSize.LONG, new short[] { 0b111100, (short) 0x8000, 0x0000 });
            addDataItem("#$FFFFFFFF", InstructionSize.LONG, new short[] { 0b111100, (short) 0xFFFF, (short) 0xFFFF });
            addDataItem("#-$80000000", InstructionSize.LONG, new short[] { 0b111100, (short) 0x8000, 0x0000 });
            addDataItem("#-1", InstructionSize.LONG, new short[] { 0b111100, (short) 0xFFFF, (short) 0xFFFF });
            addDataItem("#'ABC'", InstructionSize.LONG, new short[] { 0b111100, 0x0041, 0x4243 });
            addDataItem("#'ABCD'", InstructionSize.LONG, new short[] { 0b111100, 0x4142, 0x4344 });

            // Optimizations
            M68KTestAssemblyContext context;

            // - zero displacement optimization
            context = new M68KTestAssemblyContext();
            context.optimizeZeroDisplacement = true;
            addDataItem("(0,A0)", context, new short[] { 0b010000 });
            addDataItem("(0,A0)", EnumSet.of(AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT), context, new short[] {
                    0b101000, 0x0000 });
            addDataItem("($7FFF,A0)", context, new short[] { 0b101000, 0x7FFF });
            addDataItem("(2,PC)", context, new short[] { 0b111010, 0x0000 });

            // - optimizeUnsizedAbsoluteAddressingToPcRelative
            context = new M68KTestAssemblyContext();
            context.optimizeUnsizedAbsoluteAddressingToPcRelative = true;
            context.programCounter = 0x40000;
            addDataItem("$38000", context, new short[] { 0b111001, 0x0003, (short) 0x8000 });
            addDataItem("$38002", context, new short[] { 0b111010, -0x8000 });
            addDataItem("$40000", context, new short[] { 0b111010, -0x0002 });
            addDataItem("$40000", AddressingModeCategory.ALTERABLE, context, new short[] { 0b111001, 0x0004, 0x0000 });
            addDataItem("$40002", context, new short[] { 0b111010, 0x0000 });
            addDataItem("$48001", context, new short[] { 0b111010, 0x7FFF });
            addDataItem("$48002", context, new short[] { 0b111001, 0x0004, (short) 0x8002 });

            // With bit field specification
            addDataItem("D0{1:1}", true, new short[] { 0b000000 });
        }

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static void addDataItem(@Nonnull String text, boolean expectBitFieldSpecification, @Nonnull short[] words) {
            addDataItem(text, AddressingModeCategory.ALL, expectBitFieldSpecification, InstructionSize.DEFAULT,
                    InstructionSet.MC68000, null, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSet instructionSet, @Nonnull short[] words) {
            addDataItem(text, AddressingModeCategory.ALL, false, InstructionSize.DEFAULT, instructionSet, null, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSize instructionSize, @Nonnull short[] words) {
            addDataItem(text, AddressingModeCategory.ALL, false, instructionSize, InstructionSet.MC68000, null, words);
        }

        private static void addDataItem(@Nonnull String text, @CheckForNull M68KTestAssemblyContext context, @Nonnull short[] words) {
            addDataItem(text, AddressingModeCategory.ALL, false, InstructionSize.DEFAULT, InstructionSet.MC68000, context, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull Set<AddressingMode> validAddressingModes,
                boolean expectBitFieldSpecification, @Nonnull InstructionSize instructionSize,
                @Nonnull InstructionSet instructionSet, @CheckForNull M68KTestAssemblyContext context, @Nonnull short[] words) {
            TEST_DATA.add(new Object[] { new DataItem(text, validAddressingModes, expectBitFieldSpecification, instructionSize,
                    instructionSet, DummySymbolLookup.DEFAULT, context, words) });
        }

        private static void addDataItem(@Nonnull String text, @Nonnull Set<AddressingMode> validAddressingModes,
                @CheckForNull M68KTestAssemblyContext context, @Nonnull short[] words) {
            addDataItem(text, validAddressingModes, false, InstructionSize.DEFAULT, InstructionSet.MC68000, context, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull Set<AddressingMode> validAddressingModes,
                @Nonnull short[] words) {
            addDataItem(text, validAddressingModes, false, InstructionSize.DEFAULT, InstructionSet.MC68000, null, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull short[] words) {
            addDataItem(text, AddressingModeCategory.ALL, false, InstructionSize.DEFAULT, InstructionSet.MC68000, null, words);
        }

        public StandardSyntaxTest(@Nonnull DataItem data) {
            super(data);
        }

    }

    @RunWith(Parameterized.class)
    public static class SymbolTest extends BaseSuccessfulTest {

        @Nonnull
        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            SymbolLookup symbolLookup;

            symbolLookup = new SingleSymbolLookup("foo", ONE);
            addDataItem("(foo).W", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("(foo).w", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("(foo).L", symbolLookup, new short[] { 0b111001, 0x0000, 0x0001 });
            addDataItem("(foo).L", symbolLookup, new short[] { 0b111001, 0x0000, 0x0001 });
            addDataItem("(foo)", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("foo.W", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("foo.w", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("foo.L", symbolLookup, new short[] { 0b111001, 0x0000, 0x0001 });
            addDataItem("foo.l", symbolLookup, new short[] { 0b111001, 0x0000, 0x0001 });
            addDataItem("foo", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("#foo", symbolLookup, new short[] { 0b111100, 0x0001 });
            addDataItem("~foo", symbolLookup, new short[] { 0b111000, ~0x0001 });
            addDataItem("-(foo)", symbolLookup, new short[] { 0b111000, -0x0001 });
            addDataItem("(foo*1)", symbolLookup, new short[] { 0b111000, 0x0001 });

            symbolLookup = new SingleSymbolLookup("foo", UNDEFINED);
            addDataItem("foo", symbolLookup, new short[] { 0b111000, 0x0000 });
            addDataItem("#foo", symbolLookup, new short[] { 0b111100, 0x0000 });
            addDataItem("(A0,D0.W*foo)", InstructionSet.CPU32, symbolLookup, new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("(foo,A0,D0.W)", symbolLookup, new short[] { 0b110000, 0b00000000_00000000 });
            addDataItem("([A0],foo)", InstructionSet.MC68020, symbolLookup, new short[] { 0b110000, 0b00000001_01010001 });

            symbolLookup = new SingleSymbolLookup("D0", ONE);
            addDataItem("#D0", symbolLookup, new short[] { 0b111100, 0x0001 });
            addDataItem("(D0+1)", symbolLookup, new short[] { 0b111000, 0x0002 });
            addDataItem("(D0,D0)", InstructionSet.CPU32, symbolLookup, new short[] { 0b110000, 0b00000001_10100000, 0x0001 });

            symbolLookup = new SingleSymbolLookup("D.", ONE);
            addDataItem("D.", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("#D.", symbolLookup, new short[] { 0b111100, 0x0001 });

            symbolLookup = new SingleSymbolLookup("D8", ONE);
            addDataItem("D8", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("#D8", symbolLookup, new short[] { 0b111100, 0x0001 });

            symbolLookup = new SingleSymbolLookup("PS", ONE);
            addDataItem("(PS)", symbolLookup, new short[] { 0b111000, 0x0001 });

            symbolLookup = new SingleSymbolLookup("ZPS", ONE);
            addDataItem("(ZPS)", symbolLookup, new short[] { 0b111000, 0x0001 });

            symbolLookup = new SingleSymbolLookup("ZZZ", ONE);
            addDataItem("(ZZZ)", symbolLookup, new short[] { 0b111000, 0x0001 });

            symbolLookup = new SingleSymbolLookup("PC", ONE);
            addDataItem("(PC,PC)", symbolLookup, new short[] { 0b111010, -0x0001 });
            addDataItem("(PC,[A0])", InstructionSet.MC68020, symbolLookup, new short[] { 0b110000, 0b00000001_01010010, 0x0001 });

            symbolLookup = new SingleSymbolLookup("foo.0", ONE);
            addDataItem("foo . 0", symbolLookup, new short[] { 0b111000, 0x0001 });

            symbolLookup = new SingleSymbolLookup("foo.bar", ONE);
            addDataItem("foo . bar", symbolLookup, new short[] { 0b111000, 0x0001 });

            symbolLookup = new SingleSymbolLookup("foo.z", ONE);
            addDataItem("foo . z", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("(A0,foo.z)", symbolLookup, new short[] { 0b101000, 0x0001 });

            symbolLookup = new SingleSymbolLookup("foo.w", ONE);
            addDataItem("(A0,foo.w)", symbolLookup, new short[] { 0b101000, 0x0001 });

            symbolLookup = new SingleSymbolLookup("foo", ONE_FUNCTION);
            addDataItem("foo()", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("foo(0)", symbolLookup, new short[] { 0b111000, 0x0001 });
            addDataItem("foo(0, 0)", symbolLookup, new short[] { 0b111000, 0x0001 });
        }

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSet instructionSet,
                @CheckForNull SymbolLookup symbolLookup, @Nonnull short[] words) {
            addDataItem(text, InstructionSize.DEFAULT, instructionSet, symbolLookup, words);
        }

        private static void addDataItem(@Nonnull String text, @Nonnull InstructionSize instructionSize,
                @Nonnull InstructionSet instructionSet, @CheckForNull SymbolLookup symbolLookup, @Nonnull short[] words) {
            TEST_DATA.add(new Object[] { new DataItem(text, AddressingModeCategory.ALL, false, instructionSize, instructionSet,
                    symbolLookup, null, words) });
        }

        private static void addDataItem(@Nonnull String text, @CheckForNull SymbolLookup symbolLookup, @Nonnull short[] words) {
            addDataItem(text, InstructionSize.DEFAULT, InstructionSet.MC68000, symbolLookup, words);
        }

        public SymbolTest(@Nonnull DataItem data) {
            super(data);
        }

    }

    @RunWith(Parameterized.class)
    public static class ValidateAddressingModeTest extends BaseTest<ValidateAddressingModeTest.DataItem> {

        public static class DataItem extends BaseTest.DataItem {

            final boolean expectedValid;

            DataItem(@Nonnull String text, @Nonnull Set<AddressingMode> validAddressingModes, boolean expectedValid) {
                super(text, validAddressingModes, false, InstructionSize.DEFAULT, InstructionSet.MC68020, null, null);
                this.expectedValid = expectedValid;
            }

        }

        private static final int CLASS_CONSTANT_MODIFIERS = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

        @Nonnull
        private static final EquivalentAssemblyMessage ADDRESSING_MODE_NOT_ALLOWED_HERE = new EquivalentAssemblyMessage(
                new AddressingModeNotAllowedHereErrorMessage());

        @Nonnull
        private static final ArrayList<Set<AddressingMode>> CATEGORIES = loadCategories();
        @Nonnull
        private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

        static {
            addDataItem("D0", AddressingMode.DATA_REGISTER_DIRECT);
            addDataItem("A0", AddressingMode.ADDRESS_REGISTER_DIRECT);
            addDataItem("(A0)", AddressingMode.ADDRESS_REGISTER_INDIRECT);
            addDataItem("(A0)+", AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT);
            addDataItem("-(A0)", AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT);
            addDataItem("(2,A0)", AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT);
            addDataItem("(A0,D0)", AddressingMode.ADDRESS_REGISTER_INDIRECT_INDEXED);
            addDataItem("(0).W", AddressingMode.ABSOLUTE);
            addDataItem("(2,PC)", AddressingMode.PROGRAM_COUNTER_INDIRECT_WITH_DISPLACEMENT);
            addDataItem("(PC,D0.W)", AddressingMode.PROGRAM_COUNTER_INDIRECT_INDEXED);
            addDataItem("#0", AddressingMode.IMMEDIATE_DATA);
        }

        @Nonnull
        @Parameters
        public static List<Object[]> data() {
            return TEST_DATA;
        }

        private static void addDataItem(@Nonnull String text, @Nonnull AddressingMode addressingMode) {
            for (Set<AddressingMode> category : CATEGORIES) {
                TEST_DATA.add(new Object[] { new DataItem(text, category, category.contains(addressingMode)) });
            }
        }

        @Nonnull
        @SuppressWarnings("unchecked")
        private static ArrayList<Set<AddressingMode>> loadCategories() {
            try {
                ArrayList<Set<AddressingMode>> categories = new ArrayList<>();
                final Field[] categoryFields = AddressingModeCategory.class.getDeclaredFields();
                for (Field categoryField : categoryFields) {
                    if ((categoryField.getModifiers() & CLASS_CONSTANT_MODIFIERS) == CLASS_CONSTANT_MODIFIERS) {
                        final Object object = categoryField.get(null);
                        if (object instanceof Set) {
                            categories.add((Set<AddressingMode>) object);
                        }
                    }
                }

                return categories;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public ValidateAddressingModeTest(@Nonnull DataItem data) {
            super(data);
        }

        @Override
        protected void checkMessages(ArrayList<AssemblyMessage> messages) {
            if (this.data.expectedValid) {
                assertThat(messages, is(empty()));
            } else {
                assertThat(messages, contains(ADDRESSING_MODE_NOT_ALLOWED_HERE));
            }
        }

        @Override
        protected void checkOutput(EffectiveAddress ea) {
        }

    }

    @Nonnull
    static final UnsignedIntValue ONE_VALUE = new UnsignedIntValue(1);
    @Nonnull
    static final ValueExpression ONE_EXPRESSION = new ValueExpression(ONE_VALUE);

    @Nonnull
    static final Symbol UNDEFINED = new StaticSymbol(null);
    @Nonnull
    static final Symbol ONE = new StaticSymbol(ONE_VALUE);
    @Nonnull
    static final Symbol ONE_FUNCTION = new StaticSymbol(new FunctionValue(new Function() {
        @Override
        public Expression call(Expression[] arguments, EvaluationContext evaluationContext) {
            return ONE_EXPRESSION;
        }
    }));

}
