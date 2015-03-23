package org.reasm.m68k.assembly.internal;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.meta.When;

import org.reasm.commons.source.Syntax;
import org.reasm.m68k.messages.MultipleOperandPacksInMacroDefinitionErrorMessage;
import org.reasm.m68k.source.M68KParser;
import org.reasm.source.MacroInstantiation;
import org.reasm.source.SourceLocation;

import ca.fragag.text.DocumentReader;
import ca.fragag.text.RangedCharSequenceReader;

/**
 * A user-defined macro.
 *
 * @author Francis Gagné
 */
@Immutable
class Macro extends Mnemonic {

    @Immutable
    private static class OperandFromEndSubstitutionSource extends SubstitutionSource {

        private static final int MAX_CACHE_SIZE = 256;
        @Nonnull
        private static final ArrayList<OperandFromEndSubstitutionSource> CACHE = new ArrayList<>();

        @Nonnull
        static OperandFromEndSubstitutionSource get(int operandIndex) {
            int cacheIndex = ~operandIndex;
            if (cacheIndex >= MAX_CACHE_SIZE) {
                return new OperandFromEndSubstitutionSource(operandIndex);
            }

            while (cacheIndex >= CACHE.size()) {
                CACHE.add(new OperandFromEndSubstitutionSource(~CACHE.size()));
            }

            return CACHE.get(cacheIndex);
        }

        // operandIndex is always a negative value.
        // -1 means the last operand, -2 means the second to last operand, etc.
        @Nonnegative(when = When.NEVER)
        private final int operandIndex;

        private OperandFromEndSubstitutionSource(@Nonnegative(when = When.NEVER) int operandIndex) {
            this.operandIndex = operandIndex;
        }

        @Override
        String substitute(M68KAssemblyContext context, int macroCounter, Macro macro) {
            final int realOperandIndex = context.numberOfOperands + this.operandIndex;
            if (realOperandIndex >= 0) {
                return context.getOperandText(realOperandIndex);
            }

            return "";
        }

    }

    @Immutable
    private static final class OperandSubstitutionSource extends SubstitutionSource {

        private static final int MAX_CACHE_SIZE = 256;
        @Nonnull
        private static final ArrayList<OperandSubstitutionSource> CACHE = new ArrayList<>();

        @Nonnull
        static OperandSubstitutionSource get(int operandIndex) {
            if (operandIndex >= MAX_CACHE_SIZE) {
                return new OperandSubstitutionSource(operandIndex);
            }

            while (operandIndex >= CACHE.size()) {
                CACHE.add(new OperandSubstitutionSource(CACHE.size()));
            }

            return CACHE.get(operandIndex);
        }

        private final int operandIndex;

        private OperandSubstitutionSource(int operandIndex) {
            this.operandIndex = operandIndex;
        }

        @Override
        String substitute(M68KAssemblyContext context, int macroCounter, Macro macro) {
            if (this.operandIndex < context.numberOfOperands) {
                return context.getOperandText(this.operandIndex);
            }

            return "";
        }

    }

    @Immutable
    private static final class Substitution {

        final int offset;
        final int length;
        @Nonnull
        final SubstitutionSource source;

        Substitution(int offset, int length, @Nonnull SubstitutionSource source) {
            this.offset = offset;
            this.length = length;
            this.source = source;
        }

    }

    @Immutable
    private static abstract class SubstitutionSource {

        /** Expands to the attribute on the macro invocation. */
        @Nonnull
        static final SubstitutionSource ATTRIBUTE = new SubstitutionSource() {
            @Override
            String substitute(M68KAssemblyContext context, int macroCounter, Macro macro) {
                if (context.attribute != null) {
                    return context.attribute;
                }

                return "";
            }
        };

        /** Expands to the last label on the macro invocation. */
        @Nonnull
        static final SubstitutionSource LABEL = new SubstitutionSource() {
            @Override
            String substitute(M68KAssemblyContext context, int macroCounter, Macro macro) {
                if (context.numberOfLabels > 0) {
                    return context.getLabelText(context.numberOfLabels - 1);
                }

                return "";
            }
        };

        /** Expands to the macro counter for the current macro invocation. */
        @Nonnull
        static final SubstitutionSource COUNTER = new SubstitutionSource() {
            @Override
            String substitute(M68KAssemblyContext context, int macroCounter, Macro macro) {
                return "_" + macroCounter;
            }
        };

        /** Expands to the number of operands on the macro invocation. */
        @Nonnull
        static final SubstitutionSource NARG = new SubstitutionSource() {
            @Override
            String substitute(M68KAssemblyContext context, int macroCounter, Macro macro) {
                return Integer.toString(context.numberOfOperands);
            }
        };

        /** Expands to the operands that are in the operand pack. */
        @Nonnull
        static final SubstitutionSource PACK = new SubstitutionSource() {
            @Override
            String substitute(M68KAssemblyContext context, int macroCounter, Macro macro) {
                final StringBuilder sb = new StringBuilder();

                // On the macro definition, if the operand pack is preceded by x operands and followed by y operands,
                // then the pack includes all operands except the first x ones and the last y ones.

                // macro.numberOfNamedOperands also counts the pack operand.
                for (int i = 0; i <= context.numberOfOperands - macro.numberOfNamedOperands; i++) {
                    if (i > 0) {
                        sb.append(',');
                    }

                    sb.append(context.getOperandText(macro.packOperandIndex + i));
                }

                return sb.toString();
            }
        };

        SubstitutionSource() {
        }

        @Nonnull
        abstract String substitute(@Nonnull M68KAssemblyContext context, int macroCounter, @Nonnull Macro macro);

    }

    private static void addPositionalSubstitution(@Nonnull ArrayList<Substitution> substitutions, int startPosition,
            int endPosition, int i) {
        final SubstitutionSource source;
        if (i == 0) {
            source = SubstitutionSource.ATTRIBUTE;
        } else {
            source = OperandSubstitutionSource.get(i - 1);
        }

        substitutions.add(new Substitution(startPosition, endPosition - startPosition, source));
    }

    private static void findNamedSubstitution(@Nonnull String[] operands, int packOperandIndex, @Nonnull String name,
            int startPosition, int endPosition, @Nonnull ArrayList<Substitution> substitutions) {
        if ("NARG".equalsIgnoreCase(name)) {
            substitutions.add(new Substitution(startPosition, endPosition - startPosition, SubstitutionSource.NARG));
            return;
        }

        for (int i = 0; i < operands.length; i++) {
            if (operands[i].equalsIgnoreCase(name)) {
                final SubstitutionSource source;
                if (i == packOperandIndex) {
                    source = SubstitutionSource.PACK;
                } else if (packOperandIndex != -1 && i > packOperandIndex) {
                    source = OperandFromEndSubstitutionSource.get(i - operands.length);
                } else {
                    source = OperandSubstitutionSource.get(i);
                }

                substitutions.add(new Substitution(startPosition, endPosition - startPosition, source));
                break;
            }
        }
    }

    private static int findPackOperand(@Nonnull M68KAssemblyContext context, @Nonnull String[] operands) {
        int result = -1;
        for (int i = 0; i < operands.length; i++) {
            if (operands[i].equals("...")) {
                if (result == -1) {
                    result = i;
                } else {
                    context.addMessage(new MultipleOperandPacksInMacroDefinitionErrorMessage());
                    break;
                }
            }
        }

        return result;
    }

    @Nonnull
    private static ArrayList<Substitution> identifySubstitutions(@Nonnull String[] operands, int packOperandIndex,
            @Nonnull SourceLocation body) {
        // There are a few patterns that will get substituted in macros.
        //
        // The following patterns are matched anywhere in the macro body:
        // - \{xyz}   - if xyz is an integer, gets substituted to the nth operand (\0 is the attribute);
        //            - if xyz matches the name of an operand, gets substituted with the corresponding operand in a macro invocation
        //            - if xyz is ..., gets substituted with the operands that are included in the operand pack
        // - \*       gets substituted with the last label on the macro invocation line
        // - \@       gets substituted with an increasing counter value, prefixed with an underscore (the counter is global to the assembly)
        //
        // The following patterns are matched outside of string literals only:
        // - xyz      if xyz is an identifier that matches the name of an operand, gets substituted with the corresponding operand
        //            in a macro invocation
        // - \xyz     if xyz is an integer, gets substituted with the nth operand (\0 is the attribute)
        // - ...      gets substituted with the operands that are included in the operand pack

        final ArrayList<Substitution> substitutions = new ArrayList<>();
        final RangedCharSequenceReader reader = new RangedCharSequenceReader(new DocumentReader(body.getFile().getText()),
                body.getTextPosition(), body.getTextPosition() + body.getSourceNode().getLength());
        int inString = -1;

        while (!reader.atEnd()) {
            final int startPosition = reader.getCurrentPosition();
            int codePoint = reader.getCurrentCodePoint();

            if (codePoint == '\\') {
                reader.advance();
                codePoint = reader.getCurrentCodePoint();

                if (codePoint == '{') {
                    // Read until we find a '}'.
                    do {
                        reader.advance();
                        codePoint = reader.getCurrentCodePoint();
                    } while (codePoint != -1 && codePoint != '}');

                    if (codePoint == '}') {
                        reader.advance();
                        final int endPosition = reader.getCurrentPosition();

                        // Read the name starting after "\{" and ending before "}".
                        // Trim it because operands are always trimmed.
                        reader.setCurrentPosition(startPosition + 2);
                        final String name = reader.readSubstring(endPosition - startPosition - 3).trim();

                        final Integer i = tryParseInt(name);
                        if (i != null) {
                            addPositionalSubstitution(substitutions, startPosition, endPosition, i);
                        } else {
                            // Check if the text between the braces matches the name of an operand.
                            findNamedSubstitution(operands, packOperandIndex, name, startPosition, endPosition, substitutions);
                        }
                    }

                    continue;
                }

                if (codePoint == '*') {
                    reader.advance();
                    substitutions.add(new Substitution(startPosition, reader.getCurrentPosition() - startPosition,
                            SubstitutionSource.LABEL));
                    continue;
                }

                if (codePoint == '@') {
                    reader.advance();
                    substitutions.add(new Substitution(startPosition, reader.getCurrentPosition() - startPosition,
                            SubstitutionSource.COUNTER));
                    continue;
                }

                if (inString == -1) {
                    if (Syntax.isDigit(codePoint)) {
                        // Read until we find a non-digit.
                        do {
                            reader.advance();
                        } while (Syntax.isDigit(reader.getCurrentCodePoint()));

                        final int endPosition = reader.getCurrentPosition();
                        reader.setCurrentPosition(startPosition + 1);
                        final String name = reader.readSubstring(endPosition - startPosition - 1).trim();

                        final Integer i = tryParseInt(name);
                        if (i != null) {
                            addPositionalSubstitution(substitutions, startPosition, endPosition, i);
                        }

                        continue;
                    }
                } else {
                    // Skip the next code point. This allows single and double quotes, in particular, to be escaped.
                    reader.advance();
                }
            } else {
                if (inString == -1) {
                    if (codePoint == '\'' || codePoint == '"') {
                        inString = codePoint;
                    } else if (codePoint == '.') {
                        reader.advance();
                        if (reader.getCurrentCodePoint() == '.') {
                            reader.advance();
                            if (reader.getCurrentCodePoint() == '.') {
                                reader.advance();
                                if (packOperandIndex != -1) {
                                    substitutions.add(new Substitution(startPosition, reader.getCurrentPosition() - startPosition,
                                            SubstitutionSource.PACK));
                                }
                            }
                        }

                        continue;
                    } else {
                        boolean startsWithDigit = Syntax.isDigit(codePoint);
                        if (startsWithDigit || M68KParser.SYNTAX.isValidIdentifierInitialCodePoint(codePoint)) {
                            // Read an identifier.
                            do {
                                reader.advance();
                            } while (M68KParser.SYNTAX.isValidIdentifierCodePoint(reader.getCurrentCodePoint()));

                            if (!startsWithDigit) {
                                final int endPosition = reader.getCurrentPosition();
                                reader.setCurrentPosition(startPosition);
                                final String identifier = reader.readSubstring(endPosition - startPosition);

                                // Check if the identifier matches the name of an operand.
                                findNamedSubstitution(operands, packOperandIndex, identifier, startPosition, endPosition,
                                        substitutions);
                            }

                            continue;
                        }
                    }
                } else if (inString == codePoint) {
                    inString = -1;
                }
            }

            reader.advance();
        }

        return substitutions;
    }

    @CheckForNull
    private static Integer tryParseInt(@Nonnull String name) {
        final int length = name.length();
        if (length == 0) {
            return null;
        }

        long result = 0;
        for (int i = 0; i < length; i++) {
            final char ch = name.charAt(i);
            if (!Syntax.isDigit(ch)) {
                return null;
            }

            result = result * 10 + (ch - '0');

            // Overflow?
            if (result > Integer.MAX_VALUE) {
                return null;
            }
        }

        return (int) result;
    }

    final int numberOfNamedOperands;
    final int packOperandIndex;
    @Nonnull
    private final SourceLocation body;
    @Nonnull
    private final ArrayList<Substitution> substitutions;
    @Nonnull
    private final boolean hasLabelSubstitutions;

    Macro(@Nonnull M68KAssemblyContext context, @Nonnull String[] operands, @Nonnull SourceLocation body) {
        this.numberOfNamedOperands = operands.length;
        this.packOperandIndex = findPackOperand(context, operands);

        this.body = body;
        this.substitutions = identifySubstitutions(operands, this.packOperandIndex, body);
        boolean hasLabelSubstitutions = false;

        for (Substitution substitution : this.substitutions) {
            if (substitution.source == SubstitutionSource.LABEL) {
                hasLabelSubstitutions = true;
                break;
            }
        }

        this.hasLabelSubstitutions = hasLabelSubstitutions;
    }

    @Override
    void assemble(M68KAssemblyContext context) {
        final MacroInstantiation macroInstantiation = this.substituteMacroOperands(context);
        context.builder.enterFile(macroInstantiation, this.body.getArchitecture());
    }

    @Override
    void defineLabels(M68KAssemblyContext context) {
        if (this.hasLabelSubstitutions) {
            context.defineExtraLabels();
        } else {
            context.defineLabels();
        }
    }

    @Nonnull
    private final MacroInstantiation substituteMacroOperands(@Nonnull M68KAssemblyContext context) {
        MacroInstantiation result = new MacroInstantiation(this.body);
        int correction = 0;

        int macroCounter = context.builder.incrementMacroCounter();

        for (Substitution substitution : this.substitutions) {
            final String substitutedText = substitution.source.substitute(context, macroCounter, this);
            result = result.replaceText(substitution.offset + correction, substitution.length, substitutedText);
            correction += substitutedText.length() - substitution.length;
        }

        return result;
    }

}
