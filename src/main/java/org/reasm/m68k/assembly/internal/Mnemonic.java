package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.Block;
import org.reasm.BlockEvents;
import org.reasm.Function;
import org.reasm.Symbol;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.expressions.Expression;
import org.reasm.m68k.expressions.internal.ExpressionParser;
import org.reasm.m68k.expressions.internal.InvalidTokenException;
import org.reasm.m68k.expressions.internal.TokenType;
import org.reasm.m68k.expressions.internal.Tokenizer;
import org.reasm.m68k.messages.DuplicateRegistersInRegisterListWarningMessage;
import org.reasm.m68k.messages.InvalidDataTypeForOrgOrObjDirectiveErrorMessage;
import org.reasm.m68k.messages.InvalidExpressionErrorMessage;
import org.reasm.m68k.messages.NotSupportedOnArchitectureErrorMessage;
import org.reasm.m68k.source.M68KParser;

/**
 * Base class for all instructions and directives provided by the M68000 architecture family.
 *
 * @author Francis Gagné
 */
@Immutable
abstract class Mnemonic {

    /**
     * A value visitor for values that must be unsigned integers. Signed integers are accepted, but treated as unsigned integers.
     *
     * @author Francis Gagné
     */
    @Immutable
    private static final class UnsignedIntValueVisitor implements ValueVisitor<Long> {

        public static final UnsignedIntValueVisitor INSTANCE = new UnsignedIntValueVisitor();

        private UnsignedIntValueVisitor() {
        }

        @Override
        public Long visitFloat(double value) {
            return null;
        }

        @Override
        public Long visitFunction(Function value) {
            return null;
        }

        @Override
        public Long visitSignedInt(long value) {
            return value;
        }

        @Override
        public Long visitString(String value) {
            return null;
        }

        @Override
        public Long visitUndetermined() {
            return null;
        }

        @Override
        public Long visitUnsignedInt(long value) {
            return value;
        }

    }

    @Nonnull
    private static final GeneralPurposeRegister[] GENERAL_PURPOSE_REGISTERS = GeneralPurposeRegister.values();

    static void checkInstructionSet(@Nonnull InstructionSetCheck instructionSetCheck, @Nonnull M68KAssemblyContext context) {
        if (!instructionSetCheck.isSupported(context.instructionSet)) {
            context.addMessage(new NotSupportedOnArchitectureErrorMessage());
        }
    }

    static short encodeIntegerSizeStandard(@Nonnull InstructionSize size) {
        switch (size) {
        case BYTE:
            return 0b00 << 6;

        case WORD:
        case DEFAULT:
        default:
            return 0b01 << 6;

        case LONG:
            return 0b10 << 6;
        }
    }

    @CheckForNull
    static Value evaluateExpressionOperand(@Nonnull M68KAssemblyContext context, int operandIndex) {
        final Expression expression = parseExpressionOperand(context, operandIndex);
        if (expression != null) {
            return expression.evaluate(context.getEvaluationContext());
        }

        return null;
    }

    @Nonnull
    static ScopedEffectBlockEvents getScopedEffectBlockEvents(@Nonnull M68KAssemblyContext context) {
        final Block block = context.builder.getCurrentBlock();
        if (block == null) {
            throw new AssertionError();
        }

        final BlockEvents blockEvents = block.getEvents();
        if (blockEvents == null || !(blockEvents instanceof ScopedEffectBlockEvents)) {
            throw new AssertionError();
        }

        return (ScopedEffectBlockEvents) blockEvents;
    }

    @CheckForNull
    static Expression parseExpressionOperand(@Nonnull M68KAssemblyContext context, int operandIndex) {
        final String operandText = context.getOperandText(operandIndex);
        final Tokenizer tokenizer = context.tokenizer;

        tokenizer.setCharSequence(operandText);
        try {
            final Expression expression = ExpressionParser.parse(tokenizer, context.createSymbolLookup(), context);
            if (expression != null && tokenizer.getTokenType() == TokenType.END) {
                return expression;
            }
        } catch (InvalidTokenException e) {
        }

        context.addMessage(new InvalidExpressionErrorMessage(operandText));
        return null;
    }

    @CheckForNull
    static GeneralPurposeRegister parseRegister(@Nonnull M68KAssemblyContext context, @Nonnull LogicalLineReader operandReader) {
        final String registerIdentifier = parseRegisterIdentifier(operandReader);
        if (registerIdentifier != null) {
            return identifyRegister(context, registerIdentifier);
        }

        return null;
    }

    @CheckForNull
    static String parseRegisterIdentifier(@Nonnull LogicalLineReader operandReader) {
        if (!operandReader.atEnd() && M68KParser.SYNTAX.isValidIdentifierInitialCodePoint(operandReader.getCurrentCodePoint())) {
            final StringBuilder sb = new StringBuilder();
            sb.appendCodePoint(operandReader.getCurrentCodePoint());
            operandReader.advance();

            while (!operandReader.atEnd() && operandReader.getCurrentChar() != '.'
                    && M68KParser.SYNTAX.isValidIdentifierCodePoint(operandReader.getCurrentCodePoint())) {
                sb.appendCodePoint(operandReader.getCurrentCodePoint());
                operandReader.advance();
            }

            return sb.toString();
        }

        return null;
    }

    @CheckForNull
    static Set<GeneralPurposeRegister> parseRegisterList(@Nonnull M68KAssemblyContext context, int operandIndex) {
        context.prepareOperandReader(operandIndex);
        return parseRegisterList(context, context.logicalLineReader);
    }

    @CheckForNull
    static Set<GeneralPurposeRegister> parseRegisterList(M68KAssemblyContext context, LogicalLineReader operandReader) {
        final int initialPosition = operandReader.backupPosition();

        try {
            boolean firstIteration = true;
            final EnumSet<GeneralPurposeRegister> registers = EnumSet.noneOf(GeneralPurposeRegister.class);
            final EnumSet<GeneralPurposeRegister> duplicateRegisters = EnumSet.noneOf(GeneralPurposeRegister.class);
            for (;;) {
                // Parse a single register (Rx) or a register range (Rx-Ry).
                final String registerIdentifier = parseRegisterIdentifier(operandReader);
                if (registerIdentifier == null) {
                    return null;
                }

                GeneralPurposeRegister firstRegisterOfRange = GeneralPurposeRegister.identify(registerIdentifier);
                if (firstRegisterOfRange == null) {
                    if (firstIteration && operandReader.atEnd()) {
                        // Check if the identifier is a register list alias.
                        final Symbol symbol = context.getRegisterAliasOrRegisterListAliasSymbolByName(registerIdentifier);
                        if (symbol != null) {
                            final Object value = symbol.getValue();
                            if (value instanceof RegisterList) {
                                return ((RegisterList) value).getRegisters();
                            }

                            if (value instanceof GeneralPurposeRegister) {
                                firstRegisterOfRange = (GeneralPurposeRegister) value;
                            }
                        }
                    } else {
                        firstRegisterOfRange = context.getRegisterAliasByName(registerIdentifier);
                    }
                }

                if (firstRegisterOfRange == null) {
                    return null;
                }

                operandReader.skipWhitespace();
                if (!operandReader.atEnd() && operandReader.getCurrentChar() == '-') {
                    operandReader.advance();
                    operandReader.skipWhitespace();
                    GeneralPurposeRegister secondRegisterOfRange = parseRegister(context, operandReader);
                    if (secondRegisterOfRange == null) {
                        return null;
                    }

                    if (secondRegisterOfRange.compareTo(firstRegisterOfRange) < 0) {
                        // Swap the registers.
                        GeneralPurposeRegister temp = secondRegisterOfRange;
                        secondRegisterOfRange = firstRegisterOfRange;
                        firstRegisterOfRange = temp;
                    }

                    for (int i = firstRegisterOfRange.ordinal(); i <= secondRegisterOfRange.ordinal(); i++) {
                        addRegister(GENERAL_PURPOSE_REGISTERS[i], registers, duplicateRegisters);
                    }
                } else {
                    addRegister(firstRegisterOfRange, registers, duplicateRegisters);
                }

                operandReader.skipWhitespace();

                // If the operand ends here, we have successfully parsed a register list.
                if (operandReader.atEnd()) {
                    break;
                }

                // A slash separates register ranges.
                if (operandReader.getCurrentChar() != '/') {
                    return null;
                }

                firstIteration = false;
                operandReader.advance();
                operandReader.skipWhitespace();
            }

            if (!duplicateRegisters.isEmpty()) {
                context.addTentativeMessage(new DuplicateRegistersInRegisterListWarningMessage());
            }

            return Collections.unmodifiableSet(registers);
        } finally {
            operandReader.restorePosition(initialPosition);
        }
    }

    static boolean parseSpecialRegister(@Nonnull M68KAssemblyContext context, int operandIndex, @Nonnull String registerName) {
        final LogicalLineReader reader = context.logicalLineReader;
        context.prepareOperandReader(operandIndex);
        for (int i = 0; i < registerName.length(); i++, reader.advance()) {
            if (reader.atEnd()) {
                return false;
            }

            final char actualChar = reader.getCurrentChar();
            final char expectedChar = registerName.charAt(i);
            assert expectedChar >= 'A' && expectedChar <= 'Z';
            if (actualChar != expectedChar && actualChar != (expectedChar | 0x20)) {
                return false;
            }
        }

        return reader.atEnd();
    }

    @CheckForNull
    static Long readSingleUnsignedIntOperand(@Nonnull M68KAssemblyContext context) {
        if (context.requireNumberOfOperands(1)) {
            final Value value = evaluateExpressionOperand(context, 0);
            if (value != null) {
                final Long longValue = Value.accept(value, UnsignedIntValueVisitor.INSTANCE);
                if (longValue == null) {
                    context.addTentativeMessage(new InvalidDataTypeForOrgOrObjDirectiveErrorMessage());
                }

                return longValue;
            }
        }

        return null;
    }

    private static void addRegister(@Nonnull GeneralPurposeRegister register, @Nonnull EnumSet<GeneralPurposeRegister> registers,
            @Nonnull EnumSet<GeneralPurposeRegister> duplicateRegisters) {
        if (!registers.add(register)) {
            duplicateRegisters.add(register);
        }
    }

    @CheckForNull
    private static GeneralPurposeRegister identifyRegister(@Nonnull M68KAssemblyContext context, @Nonnull String registerIdentifier) {
        final GeneralPurposeRegister reg = GeneralPurposeRegister.identify(registerIdentifier);
        if (reg != null) {
            return reg;
        }

        return context.getRegisterAliasByName(registerIdentifier);
    }

    /**
     * Assembles the directive or instruction on the logical line of the context's current assembly step.
     *
     * @param context
     *            the assembly context
     * @throws IOException
     *             an I/O exception occurred
     */
    abstract void assemble(@Nonnull M68KAssemblyContext context) throws IOException;

    void checkInstructionSet(@Nonnull M68KAssemblyContext context) {
        checkInstructionSet(InstructionSetCheck.M68000_FAMILY, context);
    }

    void defineLabels(@Nonnull M68KAssemblyContext context) {
        context.defineLabels();
    }

}
