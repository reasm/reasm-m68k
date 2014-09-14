package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.reasm.Function;
import org.reasm.Symbol;
import org.reasm.Value;
import org.reasm.ValueVisitor;
import org.reasm.expressions.Expression;
import org.reasm.m68k.Identifier;
import org.reasm.m68k.expressions.internal.ExpressionParser;
import org.reasm.m68k.expressions.internal.InvalidTokenException;
import org.reasm.m68k.expressions.internal.TokenType;
import org.reasm.m68k.expressions.internal.Tokenizer;
import org.reasm.m68k.messages.DuplicateRegistersInRegisterListWarningMessage;
import org.reasm.m68k.messages.InvalidDataTypeForOrgOrObjDirectiveErrorMessage;
import org.reasm.m68k.messages.InvalidExpressionErrorMessage;
import org.reasm.m68k.messages.NotSupportedOnArchitectureErrorMessage;

/**
 * Base class for all instructions and directives provided by the M68000 architecture family.
 *
 * @author Francis Gagné
 */
abstract class Mnemonic {

    /**
     * A value visitor for values that must be unsigned integers. Signed integers are accepted, but treated as unsigned integers.
     *
     * @author Francis Gagné
     */
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

    private static final GeneralPurposeRegister[] GENERAL_PURPOSE_REGISTERS = GeneralPurposeRegister.values();

    static void checkInstructionSet(InstructionSetCheck instructionSetCheck, M68KAssemblyContext context) {
        if (!instructionSetCheck.isSupported(context.instructionSet)) {
            context.addMessage(new NotSupportedOnArchitectureErrorMessage());
        }
    }

    static short encodeIntegerSizeStandard(InstructionSize size) {
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

    static Value evaluateExpressionOperand(M68KAssemblyContext context, int operandIndex) {
        final Expression expression = parseExpressionOperand(context, operandIndex);
        if (expression != null) {
            return expression.evaluate(context.getEvaluationContext());
        }

        return null;
    }

    static Expression parseExpressionOperand(M68KAssemblyContext context, int operandIndex) {
        final String operandText = context.getOperandText(operandIndex);
        final Tokenizer tokenizer = context.tokenizer;

        tokenizer.setCharSequence(operandText);
        try {
            final Expression expression = ExpressionParser.parse(tokenizer, context);
            if (tokenizer.getTokenType() == TokenType.END) {
                return expression;
            }
        } catch (InvalidTokenException e) {
        }

        context.addMessage(new InvalidExpressionErrorMessage(operandText));
        return null;
    }

    static GeneralPurposeRegister parseRegister(M68KAssemblyContext context, LogicalLineReader operandReader) {
        final String registerIdentifier = parseRegisterIdentifier(operandReader);
        if (registerIdentifier != null) {
            return identifyRegister(context, registerIdentifier);
        }

        return null;
    }

    static String parseRegisterIdentifier(LogicalLineReader operandReader) {
        if (!operandReader.atEnd() && operandReader.getCurrentChar() != '.'
                && !Identifier.isDigit(operandReader.getCurrentCodePoint())
                && Identifier.isValidIdentifierCodePoint(operandReader.getCurrentCodePoint())) {
            final StringBuilder sb = new StringBuilder();
            sb.appendCodePoint(operandReader.getCurrentCodePoint());
            operandReader.advance();

            while (!operandReader.atEnd() && operandReader.getCurrentChar() != '.'
                    && Identifier.isValidIdentifierCodePoint(operandReader.getCurrentCodePoint())) {
                sb.appendCodePoint(operandReader.getCurrentCodePoint());
                operandReader.advance();
            }

            return sb.toString();
        }

        return null;
    }

    static Set<GeneralPurposeRegister> parseRegisterList(M68KAssemblyContext context, int operandIndex) {
        context.prepareOperandReader(operandIndex);
        return parseRegisterList(context, context.logicalLineReader);
    }

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

    static boolean parseSpecialRegister(M68KAssemblyContext context, int operandIndex, String registerName) {
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

    static Long readSingleUnsignedIntOperand(M68KAssemblyContext context) {
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

    private static void addRegister(GeneralPurposeRegister register, EnumSet<GeneralPurposeRegister> registers,
            EnumSet<GeneralPurposeRegister> duplicateRegisters) {
        if (!registers.add(register)) {
            duplicateRegisters.add(register);
        }
    }

    private static GeneralPurposeRegister identifyRegister(M68KAssemblyContext context, String registerIdentifier) {
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
     */
    abstract void assemble(M68KAssemblyContext context) throws IOException;

    void checkInstructionSet(M68KAssemblyContext context) {
        checkInstructionSet(InstructionSetCheck.M68000_FAMILY, context);
    }

    void defineLabels(M68KAssemblyContext context) {
        context.defineLabels();
    }

}
