package org.reasm.m68k.assembly.internal;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.Syntax;
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
    private static final class Substitution {

        static final int ATTRIBUTE = -1;
        static final int LABEL = -2;
        static final int NARG = -3;

        final int offset;
        final int length;
        final int operandIndex;

        Substitution(int offset, int length, int operandIndex) {
            this.offset = offset;
            this.length = length;
            this.operandIndex = operandIndex;
        }

    }

    private static void findNamedSubstitution(@Nonnull String[] operands, @Nonnull String name, int startPosition, int endPosition,
            @Nonnull ArrayList<Substitution> substitutions) {
        if ("NARG".equalsIgnoreCase(name)) {
            substitutions.add(new Substitution(startPosition, endPosition - startPosition, Substitution.NARG));
            return;
        }

        for (int i = 0; i < operands.length; i++) {
            if (operands[i].equalsIgnoreCase(name)) {
                substitutions.add(new Substitution(startPosition, endPosition - startPosition, i));
                break;
            }
        }
    }

    @Nonnull
    private static ArrayList<Substitution> identifySubstitutions(@Nonnull String[] operands, @Nonnull SourceLocation body) {
        // There are a few patterns that will get substituted in macros.
        //
        // The following patterns are matched anywhere in the macro body:
        // - \{xyz}   if xyz is an integer, gets substituted to the nth operand (\0 is the attribute); otherwise, if xyz matches the
        //            name of an operand, gets substituted with the corresponding operand in a macro invocation
        // - \*       gets substituted with the last label on the macro invocation line
        //
        // The following patterns are matched outside of string literals only:
        // - xyz      if xyz is an identifier that matches the name of an operand, gets substituted with the corresponding operand
        //            in a macro invocation
        // - \xyz     if xyz is an integer, gets substituted to the nth operand (\0 is the attribute)

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
                            // NOTE: i - 1 is ATTRIBUTE when i == 0
                            substitutions.add(new Substitution(startPosition, endPosition - startPosition, i - 1));
                        } else {
                            // Check if the text between the braces matches the name of an operand.
                            findNamedSubstitution(operands, name, startPosition, endPosition, substitutions);
                        }
                    }

                    continue;
                }

                if (codePoint == '*') {
                    reader.advance();
                    substitutions.add(new Substitution(startPosition, reader.getCurrentPosition() - startPosition,
                            Substitution.LABEL));
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
                            // NOTE: i - 1 is ATTRIBUTE when i == 0
                            substitutions.add(new Substitution(startPosition, endPosition - startPosition, i - 1));
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
                    } else if (Syntax.isValidIdentifierCodePoint(codePoint)) {
                        boolean startsWithDigit = Syntax.isDigit(codePoint);

                        // Read an identifier.
                        do {
                            reader.advance();
                        } while (Syntax.isValidIdentifierCodePoint(reader.getCurrentCodePoint()));

                        if (!startsWithDigit) {
                            final int endPosition = reader.getCurrentPosition();
                            reader.setCurrentPosition(startPosition);
                            final String identifier = reader.readSubstring(endPosition - startPosition);

                            // Check if the identifier matches the name of an operand.
                            findNamedSubstitution(operands, identifier, startPosition, endPosition, substitutions);
                        }

                        continue;
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

    @Nonnull
    private final SourceLocation body;
    @Nonnull
    private final ArrayList<Substitution> substitutions;
    @Nonnull
    private final boolean hasLabelSubstitutions;

    Macro(@Nonnull String[] operands, @Nonnull SourceLocation body) {
        this.body = body;
        this.substitutions = identifySubstitutions(operands, body);
        boolean hasLabelSubstitutions = false;

        for (Substitution substitution : this.substitutions) {
            if (substitution.operandIndex == Substitution.LABEL) {
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

        for (Substitution substitution : this.substitutions) {
            String substitutedText = "";

            switch (substitution.operandIndex) {
            case Substitution.NARG:
                substitutedText = Integer.toString(context.numberOfOperands);
                break;

            case Substitution.LABEL:
                if (context.numberOfLabels > 0) {
                    substitutedText = context.getLabelText(context.numberOfLabels - 1);
                }

                break;

            case Substitution.ATTRIBUTE:
                if (context.attribute != null) {
                    substitutedText = context.attribute;
                }

                break;

            default:
                if (substitution.operandIndex < context.numberOfOperands) {
                    substitutedText = context.getOperandText(substitution.operandIndex);
                }

                break;
            }

            result = result.replaceText(substitution.offset + correction, substitution.length, substitutedText);
            correction += substitutedText.length() - substitution.length;
        }

        return result;
    }

}
