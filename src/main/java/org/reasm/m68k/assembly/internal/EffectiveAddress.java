package org.reasm.m68k.assembly.internal;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.Stack;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.AssemblyMessage;
import org.reasm.Function;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.expressions.*;
import org.reasm.m68k.expressions.internal.ExpressionParser;
import org.reasm.m68k.expressions.internal.InvalidTokenException;
import org.reasm.m68k.expressions.internal.TokenType;
import org.reasm.m68k.expressions.internal.Tokenizer;
import org.reasm.m68k.messages.*;

import ca.fragag.Consumer;

/**
 * Provides methods and constants to work with effective addresses.
 *
 * @author Francis Gagné
 */
final class EffectiveAddress {

    static final class IntegerValueVisitor implements ValueVisitor<Integer> {

        @Nonnull
        private final InstructionSize instructionSize;
        @Nonnull
        private final Consumer<AssemblyMessage> assemblyMessageConsumer;

        IntegerValueVisitor(@Nonnull InstructionSize instructionSize, @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer) {
            this.instructionSize = instructionSize;
            this.assemblyMessageConsumer = assemblyMessageConsumer;
        }

        @Override
        public Integer visitFloat(double value) {
            if (value != (int) value) {
                this.assemblyMessageConsumer.accept(new LossyConversionFromRealToIntegerWarningMessage(value));
            }

            return this.visitUnsignedInt((int) value);
        }

        @Override
        public Integer visitFunction(Function value) {
            this.assemblyMessageConsumer.accept(new FunctionCannotBeConvertedToIntegerErrorMessage());
            return null;
        }

        @Override
        public Integer visitSignedInt(long value) {
            return this.visitUnsignedInt(value);
        }

        @Override
        public Integer visitString(String value) {
            int maxLength;

            switch (this.instructionSize) {
            case BYTE:
                maxLength = 1;
                break;

            default:
                maxLength = 2;
                break;

            case LONG:
                maxLength = 4;
                break;
            }

            // TODO Make the encoding configurable
            final ByteBuffer stringBytes = Charset.defaultCharset().encode(value);

            if (stringBytes.limit() > maxLength) {
                this.assemblyMessageConsumer.accept(new StringTooLongErrorMessage(value));
            }

            if (maxLength > stringBytes.limit()) {
                maxLength = stringBytes.limit();
            }

            int result = 0;
            for (; maxLength != 0; maxLength--) {
                result <<= 8;
                result |= stringBytes.get() & 0xFF;
            }

            return result;
        }

        @Override
        public Integer visitUndetermined() {
            return null;
        }

        @Override
        public Integer visitUnsignedInt(long value) {
            int intValue = (int) value;
            switch (this.instructionSize) {
            case BYTE:
                if (intValue < -0x80 || intValue > 0xFF) {
                    this.assemblyMessageConsumer.accept(new ValueOutOfRangeErrorMessage(value));
                }

                return (int) (short) (intValue & 0xFF);

            default:
                if (intValue < -0x8000 || intValue > 0xFFFF) {
                    this.assemblyMessageConsumer.accept(new ValueOutOfRangeErrorMessage(value));
                }

                return (int) (short) intValue;

            case LONG:
                return intValue;
            }
        }

    }

    enum MemoryIndirectMode {
        NONE, PREINDEXED, POSTINDEXED
    }

    enum MemoryIndirectState {
        NONE, INCOMPLETE, COMPLETE
    }

    static final class QuadParser {

        @CheckForNull
        Expression baseDisplacementExpression;
        boolean haveBaseRegister;
        int baseRegister;
        @CheckForNull
        Expression baseRegisterExpression;
        boolean haveIndexRegister;
        int indexRegister;
        boolean preindexed;
        @CheckForNull
        Expression outerDisplacementExpression;
        @Nonnull
        MemoryIndirectState memoryIndirectState = MemoryIndirectState.NONE;

        QuadParser() {
        }

        void encode(@Nonnull Set<AddressingMode> validAddressingModes, int offsetToExtensionWords,
                @Nonnull EvaluationContext evaluationContext, @Nonnull M68KBasicAssemblyContext context,
                @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer, @Nonnull EffectiveAddress result) {
            final MemoryIndirectMode memoryIndirectMode;
            switch (this.memoryIndirectState) {
            case NONE:
                memoryIndirectMode = MemoryIndirectMode.NONE;
                break;

            case COMPLETE:
                if (!this.haveIndexRegister || this.preindexed) {
                    memoryIndirectMode = MemoryIndirectMode.PREINDEXED;
                } else {
                    memoryIndirectMode = MemoryIndirectMode.POSTINDEXED;
                }

                break;

            default:
                throw new AssertionError();
            }

            final boolean haveBaseDisplacement = this.baseDisplacementExpression != null;
            int baseDisplacement;
            if (this.haveBaseRegister && (this.baseRegister == 8 || this.baseRegister == 9)) {
                baseDisplacement = (int) context.programCounter;
            } else {
                baseDisplacement = 0;
            }

            if (haveBaseDisplacement) {
                final Integer value = Value.accept(this.baseDisplacementExpression.evaluate(evaluationContext),
                        new IntegerValueVisitor(InstructionSize.LONG, assemblyMessageConsumer));
                if (value != null) {
                    baseDisplacement = value;
                }
            }

            final boolean haveOuterDisplacement = this.outerDisplacementExpression != null;
            int outerDisplacement = 0;
            if (haveOuterDisplacement) {
                final Integer value = Value.accept(this.outerDisplacementExpression.evaluate(evaluationContext),
                        new IntegerValueVisitor(InstructionSize.LONG, assemblyMessageConsumer));
                if (value != null) {
                    outerDisplacement = value;
                }
            }

            encodeQuad(haveBaseDisplacement, baseDisplacement, this.haveBaseRegister, this.baseRegister, this.haveIndexRegister,
                    this.indexRegister, outerDisplacement, memoryIndirectMode, validAddressingModes, offsetToExtensionWords,
                    context, assemblyMessageConsumer, result);
        }

        boolean processExpression(@Nonnull Expression expression, @Nonnull EvaluationContext evaluationContext,
                @Nonnull M68KBasicAssemblyContext context, @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer) {
            if (!this.haveBaseRegister || !this.haveIndexRegister) {
                if (expression instanceof IdentifierExpression) {
                    final String identifier = ((IdentifierExpression) expression).getIdentifier();

                    if (!this.haveBaseRegister || this.baseRegister < 8) {
                        final short reg = identifyPcOrZpcRegister(identifier);
                        if (reg != -1) {
                            // If we have a base register, move it to the index register.
                            if (this.haveBaseRegister) {
                                this.haveIndexRegister = true;
                                this.indexRegister = 0b10000000 | this.baseRegister << 4;
                            }

                            this.haveBaseRegister = true;
                            this.baseRegister = reg + 8;
                            this.baseRegisterExpression = expression;
                            return true;
                        }
                    }

                    final short reg = identifyDataOrAddressRegister(identifier, false, context);
                    if (reg != -1) {
                        if ((reg & MODE_MASK) == MODE_ADDRESS_REGISTER_DIRECT && !this.haveBaseRegister) {
                            this.haveBaseRegister = true;
                            this.baseRegister = reg & REGISTER_MASK;
                            this.baseRegisterExpression = expression;
                            return true;
                        }

                        if (!this.haveIndexRegister) {
                            this.haveIndexRegister = true;
                            this.indexRegister = reg << 4;

                            if (this.memoryIndirectState == MemoryIndirectState.INCOMPLETE) {
                                this.preindexed = true;
                            }

                            return true;
                        }
                    }
                }
            }

            if (!this.haveIndexRegister) {
                int indexReg = parseIndexRegister(expression, evaluationContext, context, assemblyMessageConsumer);
                if (indexReg != -1) {
                    this.haveIndexRegister = true;
                    this.indexRegister = indexReg;

                    if (this.memoryIndirectState == MemoryIndirectState.INCOMPLETE) {
                        this.preindexed = true;
                    }

                    return true;
                }
            }

            if (this.baseDisplacementExpression == null && this.memoryIndirectState != MemoryIndirectState.COMPLETE
                    || this.outerDisplacementExpression == null && this.memoryIndirectState == MemoryIndirectState.COMPLETE) {
                if (this.baseDisplacementExpression == null && this.memoryIndirectState != MemoryIndirectState.COMPLETE) {
                    this.baseDisplacementExpression = expression;
                } else {
                    this.outerDisplacementExpression = expression;
                }

                return true;
            }

            return false;
        }

    }

    private enum AbsoluteAddressingSize {
        DEFAULT, WORD, LONG
    }

    /** The mask for the register in an effective address. */
    static final int REGISTER_MASK = 0b000111;
    /** The mask for the mode in an effective address. */
    static final int MODE_MASK = 0b111000;
    /** The mask for the effective address. */
    static final int EA_MASK = MODE_MASK | REGISTER_MASK;

    /** The data register direct mode (e.g. d0). */
    static final int MODE_DATA_REGISTER_DIRECT = 0b000000;
    /** The address register direct mode (e.g. a0). */
    static final int MODE_ADDRESS_REGISTER_DIRECT = 0b001000;
    /** The address register indirect mode (e.g. (a0)). */
    static final int MODE_ADDRESS_REGISTER_INDIRECT = 0b010000;
    /** The address register indirect with postincrement mode (e.g. (a0)+). */
    static final int MODE_ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT = 0b011000;
    /** The address register indirect with predecrement mode (e.g. -(a0)). */
    static final int MODE_ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT = 0b100000;
    /** The address register indirect with displacement mode (e.g. 2(a0), (2,a0)). */
    static final int MODE_ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT = 0b101000;
    /** The address register indirect indexed mode (e.g. (a0,d0)). */
    static final int MODE_ADDRESS_REGISTER_INDIRECT_INDEXED = 0b110000;
    /** The mode value for other modes (in these modes, the register part of the effective address is not actually a register). */
    static final int MODE_OTHERS = 0b111000;

    /** The absolute short addressing mode (e.g. ($1000).w). */
    static final int EA_ABSOLUTE_SHORT_ADDRESSING = 0b111000;
    /** The absolute long addressing mode (e.g. ($100000).l). */
    static final int EA_ABSOLUTE_LONG_ADDRESSING = 0b111001;
    /** The program counter indirect with displacement mode (e.g. -4(pc), (-4,pc)). */
    static final int EA_PROGRAM_COUNTER_INDIRECT_WITH_DISPLACEMENT = 0b111010;
    /** The program counter indirect indexed mode (e.g. (pc,d0), -4(pc,d0), (-4,pc,d0)). */
    static final int EA_PROGRAM_COUNTER_INDIRECT_INDEXED = 0b111011;
    /** The immediate data mode (e.g. #$ABCD). */
    static final int EA_IMMEDIATE_DATA = 0b111100;

    // Single effective address operation word format
    // ----------------------------------------------
    //
    // Bits 5-3: mode
    // Bits 2-0: register

    // Brief extension word format
    // ---------------------------
    //
    // Bit 15: index register type (D/A)
    //   0: data register
    //   1: address register
    // Bits 14-12: index register
    // Bit 11: word/long-word index size
    //   0: word size
    //   1: long size
    // Bits 10-9: scale
    //   0: Xn*1
    //   1: Xn*2
    //   2: Xn*4
    //   3: Xn*8
    // Bit 8: 0
    // Bits 7-0: displacement

    // Full extension word format
    // --------------------------
    //
    // Bit 15: index register type (D/A)
    //   0: data register
    //   1: address register
    // Bits 14-12: index register
    // Bit 11: word/long-word index size
    //   0: word size
    //   1: long size
    // Bits 10-9: scale
    //   0: Xn*1
    //   1: Xn*2
    //   2: Xn*4
    //   3: Xn*8
    // Bit 8: 1
    // Bit 7: base register suppress
    //   0: base register is present
    //   1: base register is suppressed
    // Bit 6: index suppress
    //   0: index register is present
    //   1: index register is suppressed
    // Bits 5-4: base displacement size
    //   00: reserved
    //   01: null base displacement
    //   10: word-sized base displacement
    //   11: long-sized base displacement
    // Bit 3: 0
    // Bits 2-0: index/indirect selection
    //   if index register is present:
    //     000: no memory indirect action
    //     001: memory indirect preindexed with null outer displacement
    //     010: memory indirect preindexed with word-sized outer displacement
    //     011: memory indirect preindexed with long-sized outer displacement
    //     100: reserved
    //     101: memory indirect postindexed with null outer displacement
    //     110: memory indirect postindexed with word-sized outer displacement
    //     111: memory indirect postindexed with long-sized outer displacement
    //   if index register is suppressed:
    //     000: no memory indirect action
    //     001: memory indirect with null outer displacement
    //     010: memory indirect with word-sized outer displacement
    //     011: memory indirect with long-sized outer displacement
    //     100-111: reserved

    static void encodeQuad(boolean haveBaseDisplacement, int baseDisplacement, boolean haveBaseRegister, int baseRegister,
            boolean haveIndexRegister, int indexRegister, int outerDisplacement, @Nonnull MemoryIndirectMode memoryIndirectMode,
            @Nonnull Set<AddressingMode> validAddressingModes, int offsetToExtensionWords,
            @Nonnull M68KBasicAssemblyContext context, @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer,
            @Nonnull EffectiveAddress result) {
        // If we have a base register, but it's ZPC, clear the haveBaseRegister flag.
        if (haveBaseRegister && baseRegister == 9) {
            haveBaseRegister = false;
        }

        // If the base register is PC or ZPC, the base displacement is interpreted as an absolute address,
        // while the encoded value is relative to the address of the (first) extension word.
        if (baseRegister == 8 || baseRegister == 9) {
            haveBaseDisplacement = true;
            baseDisplacement -= (int) context.programCounter + offsetToExtensionWords;
        }

        final boolean haveAddressRegister = haveBaseRegister && baseRegister < 8;
        final int baseDisplacementSize = calculateDisplacementSize(baseDisplacement);
        final int outerDisplacementSize = calculateDisplacementSize(outerDisplacement);

        // If the instruction set doesn't support a large base displacement,
        // and an addressing mode with a shorter encoding would be valid if the base displacement wasn't out of range,
        // we use that addressing mode to keep the size of the instruction more stable,
        // which helps reduce the number of passes required to assemble the source.
        // We also produce a more specific error (BaseDisplacementOutOfRangeErrorMessage)
        // if the base displacement is indeed out of range.
        final boolean supportsFullExtensionWordFormat = context.instructionSet.supportsFullExtensionWordFormat();

        if (memoryIndirectMode == MemoryIndirectMode.NONE && haveBaseRegister) {
            // Look for shorter encodings.
            if (haveBaseDisplacement && !haveIndexRegister && (!supportsFullExtensionWordFormat || baseDisplacementSize <= 1)) {
                if (haveAddressRegister) {
                    boolean optimizeZeroDisplacement = context.optimizeZeroDisplacement;
                    if (optimizeZeroDisplacement && baseDisplacement == 0) {
                        // (0,An) --> (An)
                        encodeAddressRegisterIndirect(baseRegister, validAddressingModes, assemblyMessageConsumer, result);
                        return;
                    }

                    // (d16,An)
                    result.word0 = (short) (MODE_ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT | baseRegister);

                    validateAddressingMode(validAddressingModes, AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT,
                            assemblyMessageConsumer);
                } else {
                    // (d16,PC)
                    result.word0 = EA_PROGRAM_COUNTER_INDIRECT_WITH_DISPLACEMENT;

                    validateAddressingMode(validAddressingModes, AddressingMode.PROGRAM_COUNTER_INDIRECT_WITH_DISPLACEMENT,
                            assemblyMessageConsumer);
                }

                result.numberOfWords = 2;
                result.word1 = (short) baseDisplacement;

                if (!supportsFullExtensionWordFormat && baseDisplacementSize > 1) {
                    assemblyMessageConsumer.accept(new BaseDisplacementOutOfRangeErrorMessage());
                }

                return;
            }

            // Do not check for the presence of the base displacement; use 0 if it was not specified explicitly.
            if (haveIndexRegister && (!supportsFullExtensionWordFormat || fitsInByte(baseDisplacement))) {
                if (haveAddressRegister) {
                    // (d8,An,Xn)
                    result.word0 = (short) (MODE_ADDRESS_REGISTER_INDIRECT_INDEXED | baseRegister);

                    validateAddressingMode(validAddressingModes, AddressingMode.ADDRESS_REGISTER_INDIRECT_INDEXED,
                            assemblyMessageConsumer);
                } else {
                    // (d8,PC,Xn)
                    result.word0 = EA_PROGRAM_COUNTER_INDIRECT_INDEXED;

                    validateAddressingMode(validAddressingModes, AddressingMode.PROGRAM_COUNTER_INDIRECT_INDEXED,
                            assemblyMessageConsumer);
                }

                result.numberOfWords = 2;
                result.word1 = (short) (indexRegister << 8 | baseDisplacement & 0xFF);

                // If indexRegister encodes a scale, validate that the target architecture supports it.
                if ((indexRegister & 0b00000110) != 0 && !context.instructionSet.supportsScale()) {
                    assemblyMessageConsumer.accept(new ScaleSpecificationNotSupportedErrorMessage());
                }

                if (!supportsFullExtensionWordFormat && !fitsInByte(baseDisplacement)) {
                    assemblyMessageConsumer.accept(new BaseDisplacementOutOfRangeErrorMessage());
                }

                return;
            }
        }

        result.numberOfWords = 2;

        if (baseRegister < 8) {
            result.word0 = (short) (MODE_ADDRESS_REGISTER_INDIRECT_INDEXED | baseRegister);

            validateAddressingMode(validAddressingModes, AddressingMode.ADDRESS_REGISTER_INDIRECT_INDEXED, assemblyMessageConsumer);
        } else {
            result.word0 = EA_PROGRAM_COUNTER_INDIRECT_INDEXED;

            validateAddressingMode(validAddressingModes, AddressingMode.PROGRAM_COUNTER_INDIRECT_INDEXED, assemblyMessageConsumer);
        }

        result.word1 = (short) (indexRegister << 8 | 0b00000001_00000000 | baseDisplacementSize + 1 << 4);

        if (!haveBaseRegister) {
            result.word1 |= 0b00000000_10000000;
        }

        if (!haveIndexRegister) {
            result.word1 |= 0b00000000_01000000;
        }

        if (memoryIndirectMode == MemoryIndirectMode.POSTINDEXED) {
            result.word1 |= 0b00000000_00000100;
        }

        if (memoryIndirectMode != MemoryIndirectMode.NONE) {
            result.word1 |= outerDisplacementSize + 1;
        }

        if (baseDisplacementSize == 1) {
            result.setWord(result.numberOfWords++, (short) baseDisplacement);
        } else if (baseDisplacementSize == 2) {
            result.setWord(result.numberOfWords++, (short) (baseDisplacement >> 16));
            result.setWord(result.numberOfWords++, (short) baseDisplacement);
        }

        if (outerDisplacementSize == 1) {
            result.setWord(result.numberOfWords++, (short) outerDisplacement);
        } else if (outerDisplacementSize == 2) {
            result.setWord(result.numberOfWords++, (short) (outerDisplacement >> 16));
            result.setWord(result.numberOfWords++, (short) outerDisplacement);
        }

        if (memoryIndirectMode == MemoryIndirectMode.NONE) {
            if (!supportsFullExtensionWordFormat) {
                assemblyMessageConsumer.accept(new AddressingModeNotSupportedErrorMessage());
            }
        } else {
            if (!context.instructionSet.supportsMemoryIndirect()) {
                assemblyMessageConsumer.accept(new AddressingModeNotSupportedErrorMessage());
            }
        }
    }

    static void getEffectiveAddress(@Nonnull Tokenizer tokenizer, @Nonnull Set<AddressingMode> validAddressingModes,
            boolean expectBitFieldSpecificationAtEnd, @Nonnull InstructionSize instructionSize, int offsetToExtensionWords,
            @Nonnull EvaluationContext evaluationContext, @Nonnull M68KBasicAssemblyContext context,
            @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer, @Nonnull EffectiveAddress result) {
        // Clear the result.
        result.numberOfWords = 0;
        result.word0 = 0;
        result.word1 = 0;
        result.word2 = 0;
        result.word3 = 0;
        result.word4 = 0;
        result.word5 = 0;

        switch (instructionSize) {
        case BYTE:
        case WORD:
        case LONG:
            break;

        case DEFAULT:
            instructionSize = InstructionSize.WORD;
            break;

        default:
            throw new AssertionError("Unexpected instruction size: " + instructionSize);
        }

        try {
            if (tokenizer.getTokenType() == TokenType.IMMEDIATE) {
                switch (instructionSize) {
                default:
                    result.numberOfWords = 2;
                    break;

                case LONG:
                    result.numberOfWords = 3;
                    break;
                }

                result.word0 = EA_IMMEDIATE_DATA;

                tokenizer.advance();
                final Expression expression = ExpressionParser.parse(tokenizer, assemblyMessageConsumer);
                if (expression == null) {
                    assemblyMessageConsumer.accept(new ExpressionExpectedErrorMessage());
                    return;
                }

                final Value value = expression.evaluate(evaluationContext);
                Integer intValue = Value.accept(value, new IntegerValueVisitor(instructionSize, assemblyMessageConsumer));
                if (intValue != null) {
                    switch (instructionSize) {
                    default:
                        result.word1 = intValue.shortValue();
                        break;

                    case LONG:
                        result.word1 = (short) (intValue >> 16);
                        result.word2 = intValue.shortValue();
                        break;
                    }
                }

                if (!endOfEA(tokenizer, expectBitFieldSpecificationAtEnd)) {
                    assemblyMessageConsumer.accept(new EndOfExpressionExpectedErrorMessage());
                }

                validateAddressingMode(validAddressingModes, AddressingMode.IMMEDIATE_DATA, assemblyMessageConsumer);
            } else {
                final Expression expression = ExpressionParser.parse(tokenizer, assemblyMessageConsumer);
                if (expression == null) {
                    if (tokenizer.getTokenType() == TokenType.OPENING_PARENTHESIS) {
                        parseQuad(tokenizer, expectBitFieldSpecificationAtEnd, null, validAddressingModes, offsetToExtensionWords,
                                evaluationContext, context, assemblyMessageConsumer, result);
                    }
                } else {
                    // If the token following the expression is a (, parse a quad using the expression as the base displacement.
                    // Otherwise, analyze the expression and turn it into an effective address.

                    if (tokenizer.getTokenType() == TokenType.OPENING_PARENTHESIS) {
                        parseQuad(tokenizer, expectBitFieldSpecificationAtEnd, expression, validAddressingModes,
                                offsetToExtensionWords, evaluationContext, context, assemblyMessageConsumer, result);
                    } else if (tokenizer.tokenEqualsString("+")) {
                        if (expression instanceof GroupingExpression) {
                            int addressRegister = parseAddressRegisterIndirect((GroupingExpression) expression, context);
                            if (addressRegister != -1) {
                                tokenizer.advance();
                                if (endOfEA(tokenizer, expectBitFieldSpecificationAtEnd)) {
                                    result.numberOfWords = 1;
                                    result.word0 = (short) (MODE_ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT | addressRegister);

                                    validateAddressingMode(validAddressingModes,
                                            AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT, assemblyMessageConsumer);
                                }
                            }
                        }
                    } else if (endOfEA(tokenizer, expectBitFieldSpecificationAtEnd)) {
                        // Analyze the expression to see if it matches an effective address.
                        if (expression instanceof IdentifierExpression) {
                            // Check if the identifier matches a register name.
                            final String identifier = ((IdentifierExpression) expression).getIdentifier();
                            final short reg = identifyDataOrAddressRegister(identifier, false, context);
                            if (reg != -1) {
                                result.numberOfWords = 1;
                                result.word0 = reg;

                                validateAddressingMode(validAddressingModes,
                                        (reg & MODE_MASK) == MODE_DATA_REGISTER_DIRECT ? AddressingMode.DATA_REGISTER_DIRECT
                                                : AddressingMode.ADDRESS_REGISTER_DIRECT, assemblyMessageConsumer);
                            }

                            final int absoluteAddressingSize = parseIndexRegisterOrAbsoluteAddressingSize(identifier);
                            if (absoluteAddressingSize != -1) {
                                encodeAbsoluteAddressing(
                                        new IdentifierExpression(identifier.substring(0, identifier.length() - 2)),
                                        absoluteAddressingSize == 0 ? AbsoluteAddressingSize.WORD : AbsoluteAddressingSize.LONG,
                                        validAddressingModes, evaluationContext, assemblyMessageConsumer, result,
                                        offsetToExtensionWords, context);
                            }
                        } else if (expression instanceof GroupingExpression) {
                            final Expression childExpression = ((GroupingExpression) expression).getChildExpression();

                            if (childExpression instanceof IdentifierExpression) {
                                // Check if the identifier matches a register name.
                                final String identifier = ((IdentifierExpression) childExpression).getIdentifier();
                                short reg = identifyPcOrZpcRegister(identifier);
                                if (reg != -1) {
                                    encodeQuad(false, 0, true, reg + 8, false, 0, 0, MemoryIndirectMode.NONE, validAddressingModes,
                                            offsetToExtensionWords, context, assemblyMessageConsumer, result);
                                } else {
                                    reg = identifyDataOrAddressRegister(identifier, false, context);
                                    if (reg != -1 && (reg & MODE_MASK) == MODE_ADDRESS_REGISTER_DIRECT) {
                                        encodeAddressRegisterIndirect(reg & REGISTER_MASK, validAddressingModes,
                                                assemblyMessageConsumer, result);
                                    }
                                }
                            }

                            if (result.numberOfWords == 0) {
                                int indexReg = parseIndexRegister(((GroupingExpression) expression).getChildExpression(),
                                        evaluationContext, context, assemblyMessageConsumer);
                                if (indexReg != -1) {
                                    encodeQuad(false, 0, false, 0, true, indexReg, 0, MemoryIndirectMode.NONE,
                                            validAddressingModes, offsetToExtensionWords, context, assemblyMessageConsumer, result);
                                }
                            }
                        } else if (expression instanceof UnaryOperatorExpression) {
                            final Expression operand = ((UnaryOperatorExpression) expression).getOperand();
                            if (((UnaryOperatorExpression) expression).getOperator() == UnaryOperator.NEGATION
                                    && operand instanceof GroupingExpression) {
                                int addressRegister = parseAddressRegisterIndirect((GroupingExpression) operand, context);
                                if (addressRegister != -1) {
                                    result.numberOfWords = 1;
                                    result.word0 = (short) (MODE_ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT | addressRegister);

                                    validateAddressingMode(validAddressingModes,
                                            AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT, assemblyMessageConsumer);
                                }
                            }
                        }

                        if (result.numberOfWords == 0) {
                            // MRI syntax is problematic because when we parse it as an expression, precedence rules can make the
                            // parentheses around the base and index registers hidden deep in the expression tree.
                            // For example, in:
                            //     2+4(A0)
                            // the expression tree looks like this:
                            //     BinaryOperatorExpression
                            //         operator=ADDITION
                            //         left=ValueExpression (value=2)
                            //         right=FunctionCallExpression
                            //             function=ValueExpression (value=4)
                            //             arguments[0]=IdentifierExpression (identifier=A0)
                            // However, what we want is an expression for 2+4:
                            //     BinaryOperatorExpression
                            //         operator=ADDITION
                            //         left=ValueExpression (value=2)
                            //         right=ValueExpression (value=4)
                            // To get this, we need to "deconstruct" the expression and construct a new one.
                            // But first, we need to find the FunctionCallExpression.
                            // It can be the operand of an UnaryOperatorExpression, the right operand of a BinaryOperatorExpression
                            // or the false part of a ConditionalExpression, recursively.
                            //
                            // The same process also applies for absolute addressing when the parentheses are omitted (e.g. 2+2.W).

                            Stack<Expression> expressionStack = null;
                            Expression currentExpression = expression;

                            for (;;) {
                                final Expression newExpression;
                                if (currentExpression instanceof FunctionCallExpression) {
                                    final Expression[] arguments = ((FunctionCallExpression) currentExpression).getArguments();
                                    if (arguments.length > 0) {
                                        boolean isValid = true;
                                        QuadParser quad = new QuadParser();
                                        quad.baseDisplacementExpression = reconstructExpressionWithNewTail(expressionStack,
                                                ((FunctionCallExpression) currentExpression).getFunction());

                                        for (Expression argument : arguments) {
                                            if (!quad.processExpression(argument, evaluationContext, context,
                                                    assemblyMessageConsumer)) {
                                                isValid = false;
                                                break;
                                            }
                                        }

                                        if (isValid) {
                                            quad.encode(validAddressingModes, offsetToExtensionWords, evaluationContext, context,
                                                    assemblyMessageConsumer, result);
                                        }
                                    }

                                    break;
                                } else if (currentExpression instanceof PeriodExpression) {
                                    final Expression leftExpression = ((PeriodExpression) currentExpression).getLeftExpression();
                                    final Expression rightExpression = ((PeriodExpression) currentExpression).getRightExpression();
                                    if (rightExpression instanceof IdentifierExpression) {
                                        final String identifier = ((IdentifierExpression) rightExpression).getIdentifier();
                                        if (identifier.length() == 1) {
                                            if (equalsAsciiCaseInsensitive(identifier.charAt(0), 'W')) {
                                                encodeAbsoluteAddressing(
                                                        reconstructExpressionWithNewTail(expressionStack, leftExpression),
                                                        AbsoluteAddressingSize.WORD, validAddressingModes, evaluationContext,
                                                        assemblyMessageConsumer, result, offsetToExtensionWords, context);
                                            } else if (equalsAsciiCaseInsensitive(identifier.charAt(0), 'L')) {
                                                encodeAbsoluteAddressing(
                                                        reconstructExpressionWithNewTail(expressionStack, leftExpression),
                                                        AbsoluteAddressingSize.LONG, validAddressingModes, evaluationContext,
                                                        assemblyMessageConsumer, result, offsetToExtensionWords, context);
                                            }
                                        }
                                    }

                                    break;
                                } else if (currentExpression instanceof UnaryOperatorExpression) {
                                    if (expressionStack == null) {
                                        expressionStack = new Stack<>();
                                    }

                                    expressionStack.push(currentExpression);
                                    newExpression = ((UnaryOperatorExpression) currentExpression).getOperand();
                                } else if (currentExpression instanceof BinaryOperatorExpression) {
                                    if (expressionStack == null) {
                                        expressionStack = new Stack<>();
                                    }

                                    expressionStack.push(currentExpression);
                                    newExpression = ((BinaryOperatorExpression) currentExpression).getOperand2();
                                } else if (currentExpression instanceof ConditionalExpression) {
                                    if (expressionStack == null) {
                                        expressionStack = new Stack<>();
                                    }

                                    expressionStack.push(currentExpression);
                                    newExpression = ((ConditionalExpression) currentExpression).getFalsePart();
                                } else {
                                    break;
                                }

                                currentExpression = newExpression;
                            }
                        }

                        if (result.numberOfWords == 0) {
                            encodeAbsoluteAddressing(expression, AbsoluteAddressingSize.DEFAULT, validAddressingModes,
                                    evaluationContext, assemblyMessageConsumer, result, offsetToExtensionWords, context);
                        }
                    }
                }

                if (result.numberOfWords == 0) {
                    assemblyMessageConsumer.accept(new SyntaxErrorInEffectiveAddressErrorMessage());
                }
            }
        } catch (InvalidTokenException e) {
            assemblyMessageConsumer.accept(e.createAssemblyErrorMessage());
        }
    }

    static short identifyDataOrAddressRegister(@Nonnull String identifier, boolean mayHaveSizeSuffix,
            @Nonnull M68KBasicAssemblyContext context) {
        {
            final GeneralPurposeRegister reg = GeneralPurposeRegister.identify(identifier);
            if (reg != null) {
                return (short) reg.ordinal();
            }
        }

        if (mayHaveSizeSuffix || parseIndexRegisterOrAbsoluteAddressingSize(identifier) == -1) {
            final GeneralPurposeRegister reg = context.getRegisterAliasByName(identifier);
            if (reg != null) {
                return (short) reg.ordinal();
            }
        }

        return -1;
    }

    static short identifyPcOrZpcRegister(@Nonnull String identifier) {
        if (identifier.length() == 2) {
            if (equalsAsciiCaseInsensitive(identifier.charAt(0), 'P') && equalsAsciiCaseInsensitive(identifier.charAt(1), 'C')) {
                return 0;
            }
        } else if (identifier.length() == 3) {
            if (equalsAsciiCaseInsensitive(identifier.charAt(0), 'Z') && equalsAsciiCaseInsensitive(identifier.charAt(1), 'P')
                    && equalsAsciiCaseInsensitive(identifier.charAt(2), 'C')) {
                return 1;
            }
        }

        return -1;
    }

    static int parseIndexRegister(@Nonnull Expression expression, @Nonnull EvaluationContext evaluationContext,
            @Nonnull M68KBasicAssemblyContext context, @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer) {
        if (expression instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression binaryOperatorExpression = (BinaryOperatorExpression) expression;
            if (binaryOperatorExpression.getOperator() == BinaryOperator.MULTIPLICATION) {
                final int indexReg = parseIndexRegisterName(binaryOperatorExpression.getOperand1(), context);
                if (indexReg != -1) {
                    final Integer scale = Value.accept(binaryOperatorExpression.getOperand2().evaluate(evaluationContext),
                            new IntegerValueVisitor(InstructionSize.LONG, assemblyMessageConsumer));
                    final int scaleEncoding;
                    if (scale != null) {
                        switch (scale) {
                        case 1:
                            scaleEncoding = 0;
                            break;

                        case 2:
                            scaleEncoding = 1;
                            break;

                        case 4:
                            scaleEncoding = 2;
                            break;

                        case 8:
                            scaleEncoding = 3;
                            break;

                        default:
                            assemblyMessageConsumer.accept(new InvalidScaleValueErrorMessage(scale.intValue()));
                            scaleEncoding = 0;
                            break;
                        }
                    } else {
                        scaleEncoding = 0;
                    }

                    return indexReg | scaleEncoding << 1;
                }
            }
        } else {
            final int indexReg = parseIndexRegisterName(expression, context);
            if (indexReg != -1) {
                return indexReg;
            }
        }

        return -1;
    }

    private static int calculateDisplacementSize(int displacement) {
        if (displacement == 0) {
            return 0;
        }

        if (fitsInWord(displacement)) {
            return 1;
        }

        return 2;
    }

    private static void encodeAbsoluteAddressing(@Nonnull Expression expression, @Nonnull AbsoluteAddressingSize size,
            @Nonnull Set<AddressingMode> validAddressingModes, @Nonnull EvaluationContext evaluationContext,
            @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer, @Nonnull EffectiveAddress result,
            int offsetToExtensionWords, @Nonnull M68KBasicAssemblyContext context) {
        Integer intValue = Value.accept(expression.evaluate(evaluationContext), new IntegerValueVisitor(
                size == AbsoluteAddressingSize.WORD ? InstructionSize.WORD : InstructionSize.LONG, assemblyMessageConsumer));
        int value = intValue != null ? intValue : 0;
        if (size == AbsoluteAddressingSize.DEFAULT) {
            if (fitsInWord(value)) {
                size = AbsoluteAddressingSize.WORD;
            } else {
                if (context.optimizeUnsizedAbsoluteAddressingToPcRelative
                        && validAddressingModes.contains(AddressingMode.PROGRAM_COUNTER_INDIRECT_WITH_DISPLACEMENT)
                        && fitsInWord(value - ((int) context.programCounter + offsetToExtensionWords))) {
                    encodeQuad(true, value, true, 8, false, 0, 0, MemoryIndirectMode.NONE, validAddressingModes,
                            offsetToExtensionWords, context, assemblyMessageConsumer, result);
                    return;
                }

                size = AbsoluteAddressingSize.LONG;
            }
        }

        if (size == AbsoluteAddressingSize.WORD) {
            result.numberOfWords = 2;
            result.word0 = EA_ABSOLUTE_SHORT_ADDRESSING;
            result.word1 = (short) value;
        } else {
            result.numberOfWords = 3;
            result.word0 = EA_ABSOLUTE_LONG_ADDRESSING;
            result.word1 = (short) (value >> 16);
            result.word2 = (short) value;
        }

        validateAddressingMode(validAddressingModes, AddressingMode.ABSOLUTE, assemblyMessageConsumer);
    }

    private static void encodeAddressRegisterIndirect(int registerNumber, @Nonnull Set<AddressingMode> validAddressingModes,
            @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer, @Nonnull EffectiveAddress result) {
        if (!validAddressingModes.contains(AddressingMode.ADDRESS_REGISTER_INDIRECT)) {
            // If (An) is not allowed, but (d16,An) is, use that mode instead.
            // This is used by the MOVEP instruction.
            if (validAddressingModes.contains(AddressingMode.ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT)) {
                result.numberOfWords = 2;
                result.word0 = (short) (MODE_ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT | registerNumber);
                result.word1 = 0;
                return;
            }

            assemblyMessageConsumer.accept(new AddressingModeNotAllowedHereErrorMessage());
        }

        result.numberOfWords = 1;
        result.word0 = (short) (MODE_ADDRESS_REGISTER_INDIRECT | registerNumber);
    }

    private static boolean endOfEA(@Nonnull Tokenizer tokenizer, boolean expectBitFieldSpecificationAtEnd) {
        if (expectBitFieldSpecificationAtEnd) {
            return tokenizer.getTokenType() == TokenType.OPENING_BRACE;
        }

        return tokenizer.getTokenType() == TokenType.END;
    }

    private static boolean equalsAsciiCaseInsensitive(char a, char b) {
        assert b >= 'A' && b <= 'Z';
        return a == b || a == (b | 0x20);
    }

    private static boolean fitsInByte(int value) {
        return value >= -0x80 && value <= 0x7F;
    }

    private static boolean fitsInWord(int value) {
        return value >= -0x8000 && value <= 0x7FFF;
    }

    private static int parseAddressRegisterIndirect(@Nonnull GroupingExpression expression,
            @Nonnull M68KBasicAssemblyContext context) {
        final Expression childExpression = expression.getChildExpression();

        if (childExpression instanceof IdentifierExpression) {
            // Check if the identifier matches a register name.
            final String identifier = ((IdentifierExpression) childExpression).getIdentifier();
            final short reg = identifyDataOrAddressRegister(identifier, false, context);
            if (reg != -1 && (reg & MODE_MASK) == MODE_ADDRESS_REGISTER_DIRECT) {
                return reg & REGISTER_MASK;
            }
        }

        return -1;
    }

    private static int parseIndexRegisterName(@Nonnull Expression expression, @Nonnull M68KBasicAssemblyContext context) {
        if (expression instanceof IdentifierExpression) {
            final String identifier = ((IdentifierExpression) expression).getIdentifier();
            final int indexSize = parseIndexRegisterOrAbsoluteAddressingSize(identifier);
            if (indexSize == -1) {
                final short reg = identifyDataOrAddressRegister(identifier, true, context);
                if (reg != -1) {
                    return reg << 4;
                }
            } else {
                final short reg = identifyDataOrAddressRegister(identifier.substring(0, identifier.length() - 2), true, context);
                if (reg != -1) {
                    return reg << 4 | indexSize << 3;
                }
            }
        }

        return -1;
    }

    private static int parseIndexRegisterOrAbsoluteAddressingSize(@Nonnull String identifier) {
        if (identifier.length() > 2) {
            final String suffix = identifier.substring(identifier.length() - 2);
            if (suffix.charAt(0) == '.') {
                if (equalsAsciiCaseInsensitive(suffix.charAt(1), 'W')) {
                    return 0;
                } else if (equalsAsciiCaseInsensitive(suffix.charAt(1), 'L')) {
                    return 1;
                }
            }
        }

        return -1;
    }

    private static void parseQuad(@Nonnull Tokenizer tokenizer, boolean expectBitFieldSpecificationAtEnd,
            @CheckForNull Expression baseDisplacementExpression, @Nonnull Set<AddressingMode> validAddressingModes,
            int offsetToExtensionWords, @Nonnull EvaluationContext evaluationContext, @Nonnull M68KBasicAssemblyContext context,
            @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer, @Nonnull EffectiveAddress result)
            throws InvalidTokenException {
        QuadParser quad = new QuadParser();
        quad.baseDisplacementExpression = baseDisplacementExpression;

        tokenizer.advance();

        for (;;) {
            for (;;) { // not actually a loop
                if (tokenizer.getTokenType() == TokenType.OPENING_BRACKET) {
                    if (quad.memoryIndirectState != MemoryIndirectState.NONE) {
                        return;
                    }

                    quad.memoryIndirectState = MemoryIndirectState.INCOMPLETE;

                    // If we have a base displacement, move it to the outer displacement.
                    if (quad.baseDisplacementExpression != null) {
                        quad.outerDisplacementExpression = quad.baseDisplacementExpression;
                        quad.baseDisplacementExpression = null;
                    }

                    // If we have a base register, move it to the index register.
                    if (quad.haveBaseRegister) {
                        // If the base register is PC or ZPC, move it to the outer displacement instead.
                        if (quad.baseRegister >= 8) {
                            if (quad.outerDisplacementExpression != null) {
                                return;
                            }

                            quad.outerDisplacementExpression = quad.baseRegisterExpression;
                        } else {
                            quad.haveIndexRegister = true;
                            quad.indexRegister = 0b10000000 | quad.baseRegister << 4;
                        }

                        quad.haveBaseRegister = false;
                        quad.baseRegister = 0;
                        quad.baseRegisterExpression = null;
                    }

                    tokenizer.advance();

                    if (tokenizer.getTokenType() == TokenType.CLOSING_BRACKET) {
                        break;
                    }
                }

                final Expression expression = ExpressionParser.parse(tokenizer, assemblyMessageConsumer);
                if (expression == null) {
                    return;
                }

                if (!quad.processExpression(expression, evaluationContext, context, assemblyMessageConsumer)) {
                    return;
                }

                break;
            }

            TokenType tokenType = tokenizer.getTokenType();
            if (tokenType == TokenType.CLOSING_BRACKET) {
                if (quad.memoryIndirectState != MemoryIndirectState.INCOMPLETE) {
                    return;
                }

                quad.memoryIndirectState = MemoryIndirectState.COMPLETE;

                tokenizer.advance();
                tokenType = tokenizer.getTokenType();
            }

            if (tokenType == TokenType.CLOSING_PARENTHESIS) {
                if (quad.memoryIndirectState == MemoryIndirectState.INCOMPLETE) {
                    return;
                }

                tokenizer.advance();
                break;
            }

            if (tokenType != TokenType.COMMA) {
                return;
            }

            tokenizer.advance();
        }

        quad.encode(validAddressingModes, offsetToExtensionWords, evaluationContext, context, assemblyMessageConsumer, result);

        if (!endOfEA(tokenizer, expectBitFieldSpecificationAtEnd)) {
            assemblyMessageConsumer.accept(new SyntaxErrorInEffectiveAddressErrorMessage());
        }
    }

    @Nonnull
    private static Expression reconstructExpressionWithNewTail(@Nonnull Stack<Expression> expressionStack,
            @Nonnull Expression newTailExpression) {
        if (expressionStack != null) {
            while (!expressionStack.isEmpty()) {
                final Expression ancestorExpression = expressionStack.pop();
                final Expression reconstructedExpression;
                if (ancestorExpression instanceof UnaryOperatorExpression) {
                    reconstructedExpression = new UnaryOperatorExpression(
                            ((UnaryOperatorExpression) ancestorExpression).getOperator(), newTailExpression);
                } else if (ancestorExpression instanceof BinaryOperatorExpression) {
                    final BinaryOperatorExpression binOpExpression = (BinaryOperatorExpression) ancestorExpression;
                    reconstructedExpression = new BinaryOperatorExpression(binOpExpression.getOperator(),
                            binOpExpression.getOperand1(), newTailExpression);
                } else if (ancestorExpression instanceof ConditionalExpression) {
                    final ConditionalExpression condExpression = (ConditionalExpression) ancestorExpression;
                    reconstructedExpression = new ConditionalExpression(condExpression.getCondition(),
                            condExpression.getTruePart(), newTailExpression);
                } else {
                    throw new RuntimeException("Unexpected expression type for expression: " + ancestorExpression.toString()); // unreachable
                }

                newTailExpression = reconstructedExpression;
            }
        }

        return newTailExpression;
    }

    private static void validateAddressingMode(@Nonnull Set<AddressingMode> validAddressingModes,
            @Nonnull AddressingMode addressingMode, @Nonnull Consumer<AssemblyMessage> assemblyMessageConsumer) {
        if (!validAddressingModes.contains(addressingMode)) {
            assemblyMessageConsumer.accept(new AddressingModeNotAllowedHereErrorMessage());
        }
    }

    // word0 through word5 are to be treated as the elements of an array of shorts. numberOfWords specifies the number of
    // elements in the array. word0 is always in the single effective address operation word format. The next word, if present,
    // is either in the brief extension word format or the full extension word format. The next words are the base displacement
    // and/or the outer displacement, in 0, 1 or 2 words each.

    short numberOfWords;
    short word0, word1, word2, word3, word4, word5;

    int getMode() {
        return this.word0 & MODE_MASK;
    }

    int getRegister() {
        return this.word0 & REGISTER_MASK;
    }

    short getWord(int i) {
        switch (i) {
        case 0:
            return this.word0;
        case 1:
            return this.word1;
        case 2:
            return this.word2;
        case 3:
            return this.word3;
        case 4:
            return this.word4;
        case 5:
            return this.word5;
        }

        throw new IndexOutOfBoundsException("expected index between 0 and 5 (inclusive), got " + i);
    }

    boolean isAddressRegisterDirect() {
        return (this.word0 & MODE_MASK) == MODE_ADDRESS_REGISTER_DIRECT;
    }

    boolean isAddressRegisterIndirectWithDisplacement() {
        return (this.word0 & MODE_MASK) == MODE_ADDRESS_REGISTER_INDIRECT_WITH_DISPLACEMENT;
    }

    boolean isAddressRegisterIndirectWithPostincrement() {
        return (this.word0 & MODE_MASK) == MODE_ADDRESS_REGISTER_INDIRECT_WITH_POSTINCREMENT;
    }

    boolean isAddressRegisterIndirectWithPredecrement() {
        return (this.word0 & MODE_MASK) == MODE_ADDRESS_REGISTER_INDIRECT_WITH_PREDECREMENT;
    }

    boolean isDataRegisterDirect() {
        return (this.word0 & MODE_MASK) == MODE_DATA_REGISTER_DIRECT;
    }

    boolean isImmediateData() {
        return (this.word0 & EA_MASK) == EA_IMMEDIATE_DATA;
    }

    private void setWord(int i, short value) {
        switch (i) {
        case 0:
            this.word0 = value;
            return;
        case 1:
            this.word1 = value;
            return;
        case 2:
            this.word2 = value;
            return;
        case 3:
            this.word3 = value;
            return;
        case 4:
            this.word4 = value;
            return;
        case 5:
            this.word5 = value;
            return;
        }

        throw new IndexOutOfBoundsException("expected index between 0 and 5 (inclusive), got " + i);
    }

}
