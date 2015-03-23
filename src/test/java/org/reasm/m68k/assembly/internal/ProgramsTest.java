package org.reasm.m68k.assembly.internal;

import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.AssemblyMessage;
import org.reasm.commons.messages.UnrecognizedEscapeSequenceWarningMessage;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.messages.*;
import org.reasm.messages.DirectiveRequiresLabelErrorMessage;
import org.reasm.messages.ParseErrorMessage;
import org.reasm.messages.UnknownEncodingNameErrorMessage;
import org.reasm.messages.UnknownMnemonicErrorMessage;
import org.reasm.messages.UnresolvedSymbolReferenceErrorMessage;
import org.reasm.messages.WrongNumberOfArgumentsErrorMessage;
import org.reasm.source.parseerrors.UnterminatedStringParseError;

/**
 * Test class for short M68000 programs.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class ProgramsTest extends BaseProgramsTest {

    @Nonnull
    private static final AssemblyMessage ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE = new AlignmentMustNotBeZeroOrNegativeErrorMessage();
    @Nonnull
    private static final AssemblyMessage WRONG_NUMBER_OF_ARGUMENTS = new WrongNumberOfArgumentsErrorMessage();

    @Nonnull
    private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

    static {
        // ALIGN
        addDataItem(" ALIGN", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ALIGN -1", 2, NO_DATA, ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE);
        addDataItem(" ALIGN 0", 2, NO_DATA, ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE);
        addDataItem(" ALIGN 1", 2, NO_DATA);
        addDataItem(" ALIGN 2", 2, NO_DATA);
        addDataItem(" ALIGN UNDEFINED", 2, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" ALIGN ~", 2, NO_DATA, new InvalidExpressionErrorMessage("~"));
        addDataItem(" ALIGN.B 1", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" DC.B $77\n ALIGN 2", 3, new byte[] { 0x77, 0 });
        addDataItem(" DC.B $77\n ALIGN +2", 3, new byte[] { 0x77, 0 });
        addDataItem(" DC.B $77,$66\n ALIGN 2", 3, new byte[] { 0x77, 0x66 });
        addDataItem(" DC.B $77,$66,$55\n ALIGN 2\n DC.B $44", 4, new byte[] { 0x77, 0x66, 0x55, 0, 0x44 });
        addDataItem(" DC.B $77,$66,$55\n ALIGN 16\n DC.B $44", 4, new byte[] { 0x77, 0x66, 0x55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0x44 });

        // CNOP
        addDataItem(" CNOP", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CNOP 0", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" CNOP 0,-1", 2, NO_DATA, ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE);
        addDataItem(" CNOP 0,0", 2, NO_DATA, ALIGNMENT_MUST_NOT_BE_ZERO_OR_NEGATIVE);
        addDataItem(" CNOP 0,1", 2, NO_DATA);
        addDataItem(" CNOP 0,2", 2, NO_DATA);
        addDataItem(" CNOP 0,UNDEFINED", 2, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" CNOP 0,~", 2, NO_DATA, new InvalidExpressionErrorMessage("~"));
        addDataItem(" CNOP -1,2", 2, NO_DATA, new OffsetMustNotBeNegativeErrorMessage());
        addDataItem(" CNOP 4,2", 2, new byte[] { 0, 0, 0, 0 });
        addDataItem(" CNOP UNDEFINED,2", 2, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" CNOP ~,2", 2, NO_DATA, new InvalidExpressionErrorMessage("~"));
        addDataItem(" CNOP.B 0,1", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" DC.B $77\n CNOP 0,2", 3, new byte[] { 0x77, 0 });
        addDataItem(" DC.B $77\n CNOP 0,+2", 3, new byte[] { 0x77, 0 });
        addDataItem(" DC.B $77,$66\n CNOP 0,2", 3, new byte[] { 0x77, 0x66 });
        addDataItem(" DC.B $77,$66,$55\n CNOP 0,2\n DC.B $44", 4, new byte[] { 0x77, 0x66, 0x55, 0, 0x44 });
        addDataItem(" DC.B $77,$66,$55\n CNOP 0,16\n DC.B $44", 4, new byte[] { 0x77, 0x66, 0x55, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0x44 });
        addDataItem(" DCB.B 20,$FF\n CNOP 10,16\n DC.B $77", 4, new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0x77 });
        addDataItem(" DCB.B 20,$FF\n CNOP 2,16\n DC.B $77", 4, new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0x77 });

        // DEPHASE
        final DephaseWithoutPhaseErrorMessage dephaseWithoutPhase = new DephaseWithoutPhaseErrorMessage();
        addDataItem(" DEPHASE", 2, NO_DATA, dephaseWithoutPhase);
        addDataItem(" DEPHASE 0", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, dephaseWithoutPhase);
        addDataItem(" DEPHASE.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, dephaseWithoutPhase);

        // DO
        addDataItem(" DO\n DC.W $1234\n UNTIL", 6, new byte[] { 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DO\n DC.W $1234\n UNTIL 1", 6, new byte[] { 0x12, 0x34 });
        addDataItem(" DO\n DC.W $1234\n UNTIL 1,1", 6, new byte[] { 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DO 1\n DC.W $1234\n UNTIL 1", 6, new byte[] { 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" DO.W\n DC.W $1234\n UNTIL 1", 6, new byte[] { 0x12, 0x34 }, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem("I SET 0\n DO\n DC.W $1234\nI SET I + 1\n UNTIL I = 5", 28, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34,
                0x12, 0x34, 0x12, 0x34 });

        // ELSE
        final ElseWithoutIfErrorMessage elseWithoutIf = new ElseWithoutIfErrorMessage();
        addDataItem(" ELSE", 2, NO_DATA, elseWithoutIf);
        addDataItem(" ELSE 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, elseWithoutIf);
        addDataItem(" ELSE.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, elseWithoutIf);

        // ELSEIF
        final ElseifWithoutIfErrorMessage elseifWithoutIf = new ElseifWithoutIfErrorMessage();
        addDataItem(" ELSEIF 1", 2, NO_DATA, elseifWithoutIf);
        addDataItem(" ELSEIF.W 1", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, elseifWithoutIf);

        // ENCODING
        addDataItem(" ENCODING", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ENCODING 'US-ASCII'\n DC.B 'Hello'", 3, new byte[] { 'H', 'e', 'l', 'l', 'o' });
        addDataItem(" ENCODING 'UTF-16BE'\n DC.B '\u3053\u3093\u306B\u3061\u306F'", 3, new byte[] { 0x30, 0x53, 0x30, (byte) 0x93,
                0x30, 0x6B, 0x30, 0x61, 0x30, 0x6F });
        addDataItem(" ENCODING 'UNKNOWN'", 2, NO_DATA, new UnknownEncodingNameErrorMessage("UNKNOWN",
                new UnsupportedCharsetException("UNKNOWN")));
        addDataItem(" ENCODING '???'", 2, NO_DATA, new UnknownEncodingNameErrorMessage("???",
                new IllegalCharsetNameException("???")));
        addDataItem(" ENCODING UNDEFINED", 2, NO_DATA, UNDEFINED_SYMBOL);

        // END
        addDataItem(" END", 2, NO_DATA);
        addDataItem(" END 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" END.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" DC.B 1\n END\n DC.B 2", 3, new byte[] { 1 });

        // ENDIF
        final EndifWithoutIfErrorMessage endifWithoutIf = new EndifWithoutIfErrorMessage();
        addDataItem(" ENDIF", 2, NO_DATA, endifWithoutIf);
        addDataItem(" ENDIF 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endifWithoutIf);
        addDataItem(" ENDIF.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endifWithoutIf);

        // ENDM
        final EndmWithoutMacroErrorMessage endmWithoutMacro = new EndmWithoutMacroErrorMessage();
        addDataItem(" ENDM", 2, NO_DATA, endmWithoutMacro);
        addDataItem(" ENDM 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endmWithoutMacro);
        addDataItem(" ENDM.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endmWithoutMacro);

        // ENDNS
        final EndnsWithoutNamespaceErrorMessage endnsWithoutNamespace = new EndnsWithoutNamespaceErrorMessage();
        addDataItem(" ENDNS", 2, NO_DATA, endnsWithoutNamespace);
        addDataItem(" ENDNS 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endnsWithoutNamespace);
        addDataItem(" ENDNS.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endnsWithoutNamespace);

        // ENDR
        final EndrWithoutReptErrorMessage endrWithoutRept = new EndrWithoutReptErrorMessage();
        addDataItem(" ENDR", 2, NO_DATA, endrWithoutRept);
        addDataItem(" ENDR 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endrWithoutRept);
        addDataItem(" ENDR.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endrWithoutRept);

        // ENDTRANSFORM
        final EndtransformWithoutTransformErrorMessage endtransformWithoutTransform = new EndtransformWithoutTransformErrorMessage();
        addDataItem(" ENDTRANSFORM", 2, NO_DATA, endtransformWithoutTransform);
        addDataItem(" ENDTRANSFORM 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endtransformWithoutTransform);
        addDataItem(" ENDTRANSFORM.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endtransformWithoutTransform);

        // ENDW
        final EndwWithoutWhileErrorMessage endwWithoutWhile = new EndwWithoutWhileErrorMessage();
        addDataItem(" ENDW", 2, NO_DATA, endwWithoutWhile);
        addDataItem(" ENDW 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, endwWithoutWhile);
        addDataItem(" ENDW.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, endwWithoutWhile);

        // EVEN
        addDataItem(" EVEN", 2, NO_DATA);
        addDataItem(" EVEN 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" EVEN.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" DC.B 1, 2\n EVEN\n DC.B 3", 4, new byte[] { 1, 2, 3 });
        addDataItem(" DC.B 1\n EVEN\n DC.B 2", 4, new byte[] { 1, 0, 2 });

        // FOR
        addDataItem(" FOR\n DC.W $1234\n NEXT", 4, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" FOR 1\n DC.W $1234\n NEXT", 4, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" FOR 1,5\n DC.W $1234\n NEXT", 24, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 });
        addDataItem(" FOR 1,5,2\n DC.W $1234\n NEXT", 16, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 });
        addDataItem(" FOR 1,5,2,3\n DC.W $1234\n NEXT", 16, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 },
                WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" FOR 5,1,-2\n DC.W $1234\n NEXT", 16, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 });
        addDataItem(" FOR UNDEFINED,5\n DC.W $1234\n NEXT", 4, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" FOR 1,UNDEFINED\n DC.W $1234\n NEXT", 4, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" FOR 1,5,UNDEFINED\n DC.W $1234\n NEXT", 8, new byte[] { 0x12, 0x34 }, UNDEFINED_SYMBOL);
        addDataItem("I FOR 11,15\n DC.B I\n NEXT", 24, new byte[] { 11, 12, 13, 14, 15 });
        addDataItem("I: J: FOR 11,15\n DC.B I+J\n NEXT", 24, new byte[] { 22, 24, 26, 28, 30 });

        // FUNCTION
        addDataItem(" FUNCTION", 2, NO_DATA, new DirectiveRequiresLabelErrorMessage("FUNCTION"));
        addDataItem("F FUNCTION\n DC.B F()", 3, new byte[] { 0 }, WRONG_NUMBER_OF_OPERANDS,
                new UnresolvedSymbolReferenceErrorMessage("F"));
        addDataItem("F FUNCTION 2+3\n DC.B F()", 3, new byte[] { 5 });
        addDataItem("F FUNCTION <\n DC.B F()", 3, new byte[] { 0 }, new InvalidExpressionErrorMessage("<"));
        addDataItem("F FUNCTION A,A+3\n DC.B F(2)", 3, new byte[] { 5 });
        addDataItem("F FUNCTION A,B,A+B\n DC.B F(2,3)", 3, new byte[] { 5 });
        addDataItem("F FUNCTION 2,2+3\n DC.B F()", 3, new byte[] { 0 }, new FunctionParameterIsNotSimpleIdentifierErrorMessage("2"));
        addDataItem("F FUNCTION A!,2+3\n DC.B F()", 3, new byte[] { 0 }, new FunctionParameterIsNotSimpleIdentifierErrorMessage(
                "A!"));

        // IF
        addDataItem(" IF\n DC.W $1234\n ENDIF", 4, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" IF 0\n DC.W $1234\n ENDIF", 4, NO_DATA);
        addDataItem(" IF.W 0\n DC.W $1234\n ENDIF", 4, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" IF 1\n DC.W $1234\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF 1,1\n DC.W $1234\n ENDIF", 5, new byte[] { 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" IF 1\n DC.W $1234\n ENDC", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF UNDEFINED\n DC.W $1234\n ENDIF", 4, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem(" IF 0\n DC.W $1234\n ELSE\n DC.W $2345\n ENDIF", 6, new byte[] { 0x23, 0x45 });
        addDataItem(" IF 1\n DC.W $1234\n ELSE\n DC.W $2345\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF UNDEFINED\n DC.W $1234\n ELSE\n DC.W $2345\n ENDIF", 6, new byte[] { 0x23, 0x45 }, UNDEFINED_SYMBOL);
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF\n DC.W $2345\n ENDIF", 5, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 0\n DC.W $2345\n ENDIF", 5, NO_DATA);
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 1\n DC.W $2345\n ENDIF", 6, new byte[] { 0x23, 0x45 });
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 1,1\n DC.W $2345\n ENDIF", 6, new byte[] { 0x23, 0x45 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" IF 1\n DC.W $1234\n ELSEIF 0\n DC.W $2345\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF 1\n DC.W $1234\n ELSEIF 1\n DC.W $2345\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 0\n DC.W $2345\n ELSE\n DC.W $3456\n ENDIF", 7, new byte[] { 0x34, 0x56 });
        addDataItem(" IF 0\n DC.W $1234\n ELSEIF 1\n DC.W $2345\n ELSE\n DC.W $3456\n ENDIF", 6, new byte[] { 0x23, 0x45 });
        addDataItem(" IF 1\n DC.W $1234\n ELSEIF 0\n DC.W $2345\n ELSE\n DC.W $3456\n ENDIF", 5, new byte[] { 0x12, 0x34 });
        addDataItem(" IF 1\n DC.W $1234\n ELSEIF 1\n DC.W $2345\n ELSE\n DC.W $3456\n ENDIF", 5, new byte[] { 0x12, 0x34 });

        // MACRO
        addDataItem(" MACRO\n ENDM", 4, NO_DATA, new DirectiveRequiresLabelErrorMessage(Mnemonics.MACRO));
        addDataItem("A MACRO\n ENDM", 4, NO_DATA);
        addDataItem("A MACRO.W\n ENDM", 4, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem("A MACRO\n ENDM\n A", 6, NO_DATA);
        addDataItem("A MACRO Z\n ENDM\n A", 6, NO_DATA);
        addDataItem("A MACRO\n DC.B $7F\n ENDM\n A", 7, new byte[] { 0x7F });
        addDataItem("A MACRO\n DC.B NARG\n ENDM\n A", 7, new byte[] { 0 });
        addDataItem("A MACRO\n DC.B NARG\n ENDM\n A 0", 7, new byte[] { 1 });
        addDataItem("A MACRO\n DC.B NARG\n ENDM\n A 0,0,0,0,0,0,0,0,0,0,0,0,0", 7, new byte[] { 13 });
        addDataItem("A MACRO Z\n DC.B Z\n ENDM\n A", 7, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem("A MACRO Z\n DC.B Z\n ENDM\n A $7F", 7, new byte[] { 0x7F });
        addDataItem("A MACRO Z\n DC.B 0Z\n ENDM\n A $7F", 7, new byte[] { 0x00 }, new InvalidExpressionErrorMessage("0Z"));
        addDataItem("A MACRO Z\n MOVE.B #Z,D0\n ENDM\n A $7F", 7, new byte[] { 0x10, 0x3C, 0x00, 0x7F });
        addDataItem("A MACRO Z\n DC.B \\\n ENDM\n A $7F", 7, new byte[] { 0 }, new InvalidExpressionErrorMessage("\\"));
        addDataItem("A MACRO Z\n DC.B \\*\n ENDM\n A $7F", 7, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem("A MACRO Z\n DC.B \\*\n ENDM\nL A $7F\nL:", 16, new byte[] { 1 });
        addDataItem("A MACRO\n DC.B B\\@\nB\\@:\n ENDM\n A\n A", 24, new byte[] { 0x01, 0x02 });
        addDataItem("A MACRO Z\n DC.\\0 \\1\n ENDM\n A $7F", 7, new byte[] { 0x00, 0x7F }, INVALID_SIZE_ATTRIBUTE_EMPTY);
        addDataItem("A MACRO Z\n DC.\\0 \\1\n ENDM\n A.B $7F", 7, new byte[] { 0x7F });
        addDataItem("A MACRO Z\n DC.B \\1\n ENDM\n A $7F", 7, new byte[] { 0x7F });
        addDataItem("A MACRO\n DC.B \\1\n ENDM\n A $7F", 7, new byte[] { 0x7F });
        addDataItem("A MACRO\n DC.B \\10\n ENDM\n A ,,,,,,,,,$7F", 7, new byte[] { 0x7F });
        addDataItem("A MACRO\n DC.B \\2147483648\n ENDM\n A", 7, new byte[] { 0 },
                new InvalidExpressionErrorMessage("\\2147483648"));
        addDataItem("A MACRO Z\n DC.B \\{\n ENDM\n A $7F", 7, new byte[] { 0 }, new InvalidExpressionErrorMessage("\\{"));
        addDataItem("A MACRO Z\n DC.B \\{}\n ENDM\n A $7F", 7, new byte[] { 0 }, new InvalidExpressionErrorMessage("\\{}"));
        addDataItem("A MACRO Z\n DC.B \\{1\n ENDM\n A $7F", 7, new byte[] { 0 }, new InvalidExpressionErrorMessage("\\{1"));
        addDataItem("A MACRO Z\n DC.B \\{1}\n ENDM\n A $7F", 7, new byte[] { 0x7F });
        addDataItem("A MACRO\n DC.B \\{1}\n ENDM\n A $7F", 7, new byte[] { 0x7F });
        addDataItem("A MACRO\n DC.B \\{10}\n ENDM\n A ,,,,,,,,,$7F", 7, new byte[] { 0x7F });
        addDataItem("A MACRO\n DC.B \\{2147483648}\n ENDM\n A", 7, new byte[] { 0 }, new InvalidExpressionErrorMessage(
                "\\{2147483648}"));
        addDataItem("A MACRO Z\n DC.B \\{N}\n ENDM\n A $7F", 7, new byte[] { 0 }, new InvalidExpressionErrorMessage("\\{N}"));
        addDataItem("A MACRO Z\n DC.B \\{NARG}\n ENDM\n A 0,0,0,0,0,0,0,0,0,0,0,0,0", 7, new byte[] { 13 });
        addDataItem("A MACRO Z\n DC.B \\{Z}\n ENDM\n A $7F", 7, new byte[] { 0x7F });
        addDataItem("A MACRO Z\n DC.B 'Z'\n ENDM\n A $7F", 7, new byte[] { 'Z' });
        addDataItem("A MACRO Z\n DC.B '\\'\n ENDM\n A $7F", 7, NO_DATA, new ParseErrorMessage(new UnterminatedStringParseError(0)));
        addDataItem("A MACRO Z\n DC.B '\\*'\n ENDM\n A $7F", 7, NO_DATA);
        addDataItem("A MACRO Z\n DC.B '\\*'\n ENDM\nL A $7F", 7, new byte[] { 'L' });
        addDataItem("A MACRO Z\n DC.B '\\1'\n ENDM\n A $7F", 7, new byte[] { '1' }, new UnrecognizedEscapeSequenceWarningMessage(
                '1'));
        addDataItem("A MACRO Z\n DC.B '\\{'\n ENDM\n A $7F", 7, new byte[] { '{' }, new UnrecognizedEscapeSequenceWarningMessage(
                '{'));
        addDataItem("A MACRO Z\n DC.B '\\{}'\n ENDM\n A $7F", 7, new byte[] { '{', '}' },
                new UnrecognizedEscapeSequenceWarningMessage('{'));
        addDataItem("A MACRO Z\n DC.B '\\{1'\n ENDM\n A $7F", 7, new byte[] { '{', '1' },
                new UnrecognizedEscapeSequenceWarningMessage('{'));
        addDataItem("A MACRO Z\n DC.B '\\{1}'\n ENDM\n A $7F", 7, new byte[] { '$', '7', 'F' });
        addDataItem("A MACRO Z\n DC.B '\\{2147483648}'\n ENDM\n A $7F", 7, new byte[] { '{', '2', '1', '4', '7', '4', '8', '3',
                '6', '4', '8', '}' }, new UnrecognizedEscapeSequenceWarningMessage('{'));
        addDataItem("A MACRO Z\n DC.B '\\{N}'\n ENDM\n A $7F", 7, new byte[] { '{', 'N', '}' },
                new UnrecognizedEscapeSequenceWarningMessage('{'));
        addDataItem("A MACRO Z\n DC.B '\\{NARG}'\n ENDM\n A 0,0,0,0,0,0,0,0,0,0,0,0,0", 7, new byte[] { '1', '3' });
        addDataItem("A MACRO Z\n DC.B '\\{Z}'\n ENDM\n A $7F", 7, new byte[] { '$', '7', 'F' });
        addDataItem("A MACRO Z\n DC.B \"Z\"\n ENDM\n A $7F", 7, new byte[] { 'Z' });
        addDataItem("A MACRO Z\n DC.B \"\\*\"\n ENDM\nL A $7F", 7, new byte[] { 'L' });
        addDataItem("A MACRO Z\n DC.B \"\\1\"\n ENDM\n A $7F", 7, new byte[] { '1' }, new UnrecognizedEscapeSequenceWarningMessage(
                '1'));
        addDataItem("A MACRO Z\n DC.B \"\\{1}\"\n ENDM\n A $7F", 7, new byte[] { '$', '7', 'F' });
        addDataItem("A MACRO Z\n DC.B \"\\{NARG}\"\n ENDM\n A 0,0,0,0,0,0,0,0,0,0,0,0,0", 7, new byte[] { '1', '3' });
        addDataItem("A MACRO Z\n DC.B \"\\{Z}\"\n ENDM\n A $7F", 7, new byte[] { '$', '7', 'F' });
        addDataItem("A MACRO\n DC.B $7F\n ENDM\n !A", 5, NO_DATA, new UnknownMnemonicErrorMessage());
        addDataItem("NOP MACRO\n ENDM\n NOP\n DC.W $1111\n !NOP", 8, new byte[] { 0x11, 0x11, 0x4E, 0x71 });
        addDataItem("M MACRO A,...\n IF NARG >= 1\n DC.B $FF\n DC.B A\n M ...\n ENDIF\n ENDM\n M 1,2,3", 30, new byte[] {
                (byte) 0xFF, 0x01, (byte) 0xFF, 0x02, (byte) 0xFF, 0x03 });
        addDataItem("M MACRO ...,A\n IF NARG >= 1\n DC.B $FF\n DC.B A\n M ...\n ENDIF\n ENDM\n M 1,2,3", 30, new byte[] {
                (byte) 0xFF, 0x03, (byte) 0xFF, 0x02, (byte) 0xFF, 0x01 });
        addDataItem("M MACRO A,B,...\n IF NARG >= 2\n DC.B $FF\n DC.B A\n DC.B B\n M ...\n ENDIF\n ENDM\n M 1,2,3,4,5,6", 33,
                new byte[] { (byte) 0xFF, 0x01, 0x02, (byte) 0xFF, 0x03, 0x04, (byte) 0xFF, 0x05, 0x06 });
        addDataItem("M MACRO A,...,B\n IF NARG >= 2\n DC.B $FF\n DC.B A\n DC.B B\n M ...\n ENDIF\n ENDM\n M 1,2,3,4,5,6", 33,
                new byte[] { (byte) 0xFF, 0x01, 0x06, (byte) 0xFF, 0x02, 0x05, (byte) 0xFF, 0x03, 0x04 });
        addDataItem("M MACRO A,...\n IF NARG >= 1\n DC.B $FF\n DC.B A\n M \\{...}\n ENDIF\n ENDM\n M 1,2,3", 30, new byte[] {
                (byte) 0xFF, 0x01, (byte) 0xFF, 0x02, (byte) 0xFF, 0x03 });
        addDataItem("M MACRO A,...,B,...\n IF NARG >= 1\n DC.B $FF\n DC.B A\n M ...\n ENDIF\n ENDM\n M 1,2,3", 16, new byte[] {
                (byte) 0xFF, 0x01 }, new MultipleOperandPacksInMacroDefinitionErrorMessage());
        addDataItem("M MACRO A,B,...\n IF NARG >= 2\n DC.B $FF\n DC.B A\n DC.B B\n M ..\n ENDIF\n ENDM\n M 1,2,3,4,5,6", 17,
                new byte[] { (byte) 0xFF, 0x01, 0x02 });
        addDataItem("M MACRO A,B,...\n IF NARG >= 2\n DC.B $FF\n DC.B A\n DC.B B\n M .\n ENDIF\n ENDM\n M 1,2,3,4,5,6", 17,
                new byte[] { (byte) 0xFF, 0x01, 0x02 });
        addDataItem("M MACRO A,B\n IF NARG >= 2\n DC.B $FF\n DC.B A\n DC.B B\n M ...\n ENDIF\n ENDM\n M 1,2,3,4,5,6", 17,
                new byte[] { (byte) 0xFF, 0x01, 0x02 });
        addDataItem("M MACRO\n DC.B \\257\n ENDM\n M " + stringOf(',', 256) + "$7F", 7, new byte[] { 0x7F }); // bypass OperandSubstitutionSource.CACHE
        addDataItem(
                "M MACRO ...,A0,A1,A2,A3,A4,A5,A6,A7,A8,A9,A10,A11,A12,A13,A14,A15,A16,A17,A18,A19,A20,A21,A22,A23,A24,A25,A26,A27,A28,A29,A30,A31,A32,A33,A34,A35,A36,A37,A38,A39,A40,A41,A42,A43,A44,A45,A46,A47,A48,A49,A50,A51,A52,A53,A54,A55,A56,A57,A58,A59,A60,A61,A62,A63,A64,A65,A66,A67,A68,A69,A70,A71,A72,A73,A74,A75,A76,A77,A78,A79,A80,A81,A82,A83,A84,A85,A86,A87,A88,A89,A90,A91,A92,A93,A94,A95,A96,A97,A98,A99,A100,A101,A102,A103,A104,A105,A106,A107,A108,A109,A110,A111,A112,A113,A114,A115,A116,A117,A118,A119,A120,A121,A122,A123,A124,A125,A126,A127,A128,A129,A130,A131,A132,A133,A134,A135,A136,A137,A138,A139,A140,A141,A142,A143,A144,A145,A146,A147,A148,A149,A150,A151,A152,A153,A154,A155,A156,A157,A158,A159,A160,A161,A162,A163,A164,A165,A166,A167,A168,A169,A170,A171,A172,A173,A174,A175,A176,A177,A178,A179,A180,A181,A182,A183,A184,A185,A186,A187,A188,A189,A190,A191,A192,A193,A194,A195,A196,A197,A198,A199,A200,A201,A202,A203,A204,A205,A206,A207,A208,A209,A210,A211,A212,A213,A214,A215,A216,A217,A218,A219,A220,A221,A222,A223,A224,A225,A226,A227,A228,A229,A230,A231,A232,A233,A234,A235,A236,A237,A238,A239,A240,A241,A242,A243,A244,A245,A246,A247,A248,A249,A250,A251,A252,A253,A254,A255,A256\n DC.B A0\n ENDM\n"
                        + " M $7F" + stringOf(',', 256), 7, new byte[] { 0x7F }); // bypass OperandFromEndSubstitutionSource.CACHE

        // NEXT
        final NextWithoutForErrorMessage nextWithoutFor = new NextWithoutForErrorMessage();
        addDataItem(" NEXT", 2, NO_DATA, nextWithoutFor);
        addDataItem(" NEXT 1", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, nextWithoutFor);
        addDataItem(" NEXT.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, nextWithoutFor);

        // OBJ
        final InvalidDataTypeForOrgOrObjDirectiveErrorMessage invalidDataTypeForOrgOrObjDirective = new InvalidDataTypeForOrgOrObjDirectiveErrorMessage();
        addDataItem(" OBJ\n DC.L *\n OBJEND", 6, new byte[] { 0, 0, 0, 0 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" OBJ UNDEFINED\n DC.L *\n OBJEND", 6, new byte[] { 0, 0, 0, 0 }, UNDEFINED_SYMBOL);
        addDataItem(" OBJ $400000\n DC.L *\n OBJEND", 6, new byte[] { 0, 0x40, 0, 0 });
        addDataItem(" OBJ +$400000\n DC.L *\n OBJEND", 6, new byte[] { 0, 0x40, 0, 0 });
        addDataItem(" OBJ 3.14159\n DC.L *\n OBJEND", 6, new byte[] { 0, 0, 0, 0 }, invalidDataTypeForOrgOrObjDirective);
        addDataItem(" OBJ '4'\n DC.L *\n OBJEND", 6, new byte[] { 0, 0, 0, 0 }, invalidDataTypeForOrgOrObjDirective);
        // TODO: test with a built-in function symbol
        //addDataItem(" OBJ STRLEN\n DC.L *\n OBJEND", 6, new byte[] { 0, 0, 0, 0 }, invalidDataTypeForOrgOrObjDirective);
        addDataItem(" OBJ.W $400000\n DC.L *\n OBJEND", 6, new byte[] { 0, 0x40, 0, 0 }, SIZE_ATTRIBUTE_NOT_ALLOWED);

        // OBJEND
        final ObjendWithoutObjErrorMessage objendWithoutObj = new ObjendWithoutObjErrorMessage();
        addDataItem(" OBJEND", 2, NO_DATA, objendWithoutObj);
        addDataItem(" OBJEND 0", 2, NO_DATA, WRONG_NUMBER_OF_OPERANDS, objendWithoutObj);
        addDataItem(" OBJEND.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, objendWithoutObj);

        // ORG
        addDataItem(" ORG\n DC.L *", 3, new byte[] { 0, 0, 0, 0 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" ORG UNDEFINED\n DC.L *", 3, new byte[] { 0, 0, 0, 0 }, UNDEFINED_SYMBOL);
        addDataItem(" ORG $400000\n DC.L *", 3, new byte[] { 0, 0x40, 0, 0 });
        addDataItem(" ORG +$400000\n DC.L *", 3, new byte[] { 0, 0x40, 0, 0 });
        addDataItem(" ORG 3.14159\n DC.L *", 3, new byte[] { 0, 0, 0, 0 }, invalidDataTypeForOrgOrObjDirective);
        addDataItem(" ORG '4'\n DC.L *", 3, new byte[] { 0, 0, 0, 0 }, invalidDataTypeForOrgOrObjDirective);
        // TODO: test with a built-in function symbol
        //addDataItem(" ORG STRLEN\n DC.L *", 3, new byte[] { 0, 0, 0, 0 }, invalidDataTypeForOrgOrObjDirective);
        addDataItem(" ORG.W $400000\n DC.L *", 3, new byte[] { 0, 0x40, 0, 0 }, SIZE_ATTRIBUTE_NOT_ALLOWED);

        // PHASE
        addDataItem(" PHASE\n DC.L *\n DEPHASE", 6, new byte[] { 0, 0, 0, 0 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" PHASE UNDEFINED\n DC.L *\n DEPHASE", 6, new byte[] { 0, 0, 0, 0 }, UNDEFINED_SYMBOL);
        addDataItem(" PHASE $400000\n DC.L *\n DEPHASE", 6, new byte[] { 0, 0x40, 0, 0 });
        addDataItem(" PHASE +$400000\n DC.L *\n DEPHASE", 6, new byte[] { 0, 0x40, 0, 0 });
        addDataItem(" PHASE 3.14159\n DC.L *\n DEPHASE", 6, new byte[] { 0, 0, 0, 0 }, invalidDataTypeForOrgOrObjDirective);
        addDataItem(" PHASE '4'\n DC.L *\n DEPHASE", 6, new byte[] { 0, 0, 0, 0 }, invalidDataTypeForOrgOrObjDirective);
        // TODO: test with a built-in function symbol
        //addDataItem(" PHASE STRLEN\n DC.L *\n DEPHASE", 6, new byte[] { 0, 0, 0, 0 }, invalidDataTypeForOrgOrObjDirective);
        addDataItem(" PHASE.W $400000\n DC.L *\n DEPHASE", 6, new byte[] { 0, 0x40, 0, 0 }, SIZE_ATTRIBUTE_NOT_ALLOWED);

        // REPT
        addDataItem(" REPT\n DC.W $1234\n ENDR", 5, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" REPT -1\n DC.W $1234\n ENDR", 5, NO_DATA, new CountMustNotBeNegativeErrorMessage());
        addDataItem(" REPT 0\n DC.W $1234\n ENDR", 5, NO_DATA);
        addDataItem(" REPT.W 0\n DC.W $1234\n ENDR", 5, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem(" REPT 5\n DC.W $1234\n ENDR", 10, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34, 0x12, 0x34, 0x12, 0x34 });

        // UNTIL
        final UntilWithoutDoErrorMessage untilWithoutDo = new UntilWithoutDoErrorMessage();
        addDataItem(" UNTIL", 2, NO_DATA, untilWithoutDo);
        addDataItem(" UNTIL.W", 2, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED, untilWithoutDo);

        // WHILE
        addDataItem(" WHILE\n DC.W $1234\n ENDW", 4, NO_DATA, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" WHILE 0\n DC.W $1234\n ENDW", 4, NO_DATA);
        addDataItem(" WHILE.W 0\n DC.W $1234\n ENDW", 4, NO_DATA, SIZE_ATTRIBUTE_NOT_ALLOWED);
        addDataItem("I SET 0\n WHILE I < 5\n DC.W $1234\nI SET I + 1\n ENDW", 30, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12, 0x34,
                0x12, 0x34, 0x12, 0x34 });
        addDataItem("I SET 0\n WHILE I < 5, I > 2\n DC.W $1234\nI SET I + 1\n ENDW", 30, new byte[] { 0x12, 0x34, 0x12, 0x34, 0x12,
                0x34, 0x12, 0x34, 0x12, 0x34 }, WRONG_NUMBER_OF_OPERANDS);
        addDataItem(" WHILE UNDEFINED\n DC.W $1234\n ENDW", 4, NO_DATA, UNDEFINED_SYMBOL);

        // undefined mnemonic
        addDataItem(" UNDEFINED", 2, NO_DATA, UNDEFINED_SYMBOL);
        addDataItem("A: UNDEFINED\n DC.W A", 3, new byte[] { 0x00, 0x00 }, UNDEFINED_SYMBOL);

        // ! prefix on a block directive
        addDataItem(" !IF 0\n DC.W $0123\n !ELSEIF 1\n DC.W $1234\n !ENDIF", 6, new byte[] { 0x12, 0x34 });

        // M68KAssemblyContext.getRegisterAliasByName()
        addDataItem("A EQUR D0\n MOVE.W A,D1", 3, new byte[] { 0x32, 0x00 });
        addDataItem("A EQU 0\n MOVE.W A,D1", 3, new byte[] { 0x32, 0x38, 0x00, 0x00 });
        addDataItem(" MOVE.W UNDEFINED,D1", 2, new byte[] { 0x32, 0x38, 0x00, 0x00 }, UNDEFINED_SYMBOL, UNDEFINED_SYMBOL);

        // M68KAssemblyContext.getRegisterAliasOrRegisterListAliasSymbolByName()
        addDataItem("A REG D0-D3/A2-A4\n MOVEM.L A,(A0)", 3, new byte[] { 0x48, (byte) 0xD0, 0x1C, 0x0F });
        addDataItem("A EQUR D0\n MOVEM.L A,(A0)", 3, new byte[] { 0x48, (byte) 0xD0, 0x00, 0x01 });
        addDataItem("A EQU 0\n MOVEM.L A,D0", 3, new byte[] { 0x4C, (byte) 0xF8, 0x00, 0x01, 0x00, 0x00 });
        addDataItem(" MOVEM.L UNDEFINED,D0", 2, new byte[] { 0x4C, (byte) 0xF8, 0x00, 0x01, 0x00, 0x00 }, UNDEFINED_SYMBOL,
                UNDEFINED_SYMBOL, UNDEFINED_SYMBOL);

        // Mnemonic.identifyRegister()
        addDataItem("A EQUR D0\nB EQUR D3\n MOVEM A-B,(A0)", 4, new byte[] { 0x48, (byte) 0x90, 0x00, 0x0F });

        // Mnemonic.parseRegisterList()
        addDataItem("A EQUR D0\nB EQUR D3\nC EQUR A2\nA REG D0-D7\nB REG D0-D7\nC REG D0-D7\n MOVEM A/B/C,(A0)", 8, new byte[] {
                0x48, (byte) 0x90, 0x04, 0x09 });

        // UserFunction class
        addDataItem("F FUNCTION A,B,A+B\n DC.B F()", 3, new byte[] { 0 }, WRONG_NUMBER_OF_ARGUMENTS);
        addDataItem("Z EQU 7\nF FUNCTION A,Z*A\n DC.B F(3)", 4, new byte[] { 21 });
        addDataItem("Z.Y EQU 7\nX.W EQU 5\nF FUNCTION A,X.W*A.Y\n DC.B F()", 5, new byte[] { 0 }, WRONG_NUMBER_OF_ARGUMENTS);
        addDataItem("Z.Y EQU 7\nX.W EQU 5\nF FUNCTION A,X.W*A.Y\n DC.B F(Z)", 5, new byte[] { 35 });
        addDataItem("Z EQU 7\nF FUNCTION A,(Z)*(A)\n DC.B F(3)", 4, new byte[] { 21 });
        addDataItem("Z EQU 7\nF FUNCTION A,+Z*+A\n DC.B F(3)", 4, new byte[] { 21 });
        addDataItem("Z.Y EQU 7\nX.W EQU 5\nV.U EQU 3\nF FUNCTION A,B,Z . Y * A . W * V . B\n DC.B F(X, U)", 6, new byte[] { 105 });
        addDataItem("Z[0] EQU 7\nZ[1] EQU 5\nZ[2] EQU 3\nF FUNCTION A,B,Z[0] * Z[A] * B[2]\n DC.B F(1,Z)", 6, new byte[] { 105 });
        addDataItem("Z EQU 7\nF FUNCTION A,(Z*1)*(A*1)\n DC.B F(3)", 4, new byte[] { 21 });
        addDataItem("F FUNCTION 1?2:3\n DC.B F()", 3, new byte[] { 2 });
        addDataItem("F FUNCTION A,A?2:3\n DC.B F(1)", 3, new byte[] { 2 });
        addDataItem("F FUNCTION A,1?A:3\n DC.B F(2)", 3, new byte[] { 2 });
        addDataItem("F FUNCTION A,1?2:A\n DC.B F(3)", 3, new byte[] { 2 });
        addDataItem("G FUNCTION 1\nF FUNCTION G()\n DC.B F()", 4, new byte[] { 1 });
        addDataItem("G FUNCTION 1\nF FUNCTION A,A()\n DC.B F(G)", 4, new byte[] { 1 });
        addDataItem("G FUNCTION A,B,A*B\nF FUNCTION A,G(A,3)\n DC.B F(2)", 4, new byte[] { 6 });
        addDataItem("Z EQU 5\nN1 NAMESPACE\nZ EQU 7\nF FUNCTION Z\n ENDNS\n DC.B N1.F()", 9, new byte[] { 7 });
        addDataItem("Z EQU 5\nN1 NAMESPACE\nZ EQU 7\nF FUNCTION A,Z*A\n ENDNS\n DC.B N1.F(3)", 9, new byte[] { 21 });
        addDataItem("Z EQU 5\nF FUNCTION A,Z*A\nN1 NAMESPACE\nZ EQU 7\n DC.B F(Z)\n ENDNS", 9, new byte[] { 35 });
        addDataItem("Z.Y EQU 5\nF FUNCTION A,Z.Y*A.Y\nN1 NAMESPACE\nZ.Y EQU 7\n DC.B F(Z)\n ENDNS", 9, new byte[] { 35 });

        // continuation characters
        addDataItem(" DC.B &\n1", 2, new byte[] { 1 });
        addDataItem(" DC&\n.B 1", 2, new byte[] { 1 });
        addDataItem(" DC&\r.B 1", 2, new byte[] { 1 });
        addDataItem(" DC&\r\n.B 1", 2, new byte[] { 1 });
        addDataItem(" DC&\n .B 1", 2, new byte[] { 1 });
        addDataItem(" DC&\n\t.B 1", 2, new byte[] { 1 });
        addDataItem(" DC&\n\t\t  .B 1", 2, new byte[] { 1 });
        addDataItem(" DC.B 1&\n1", 2, new byte[] { 11 });
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
        addDataItem(code, steps, output, (AssemblyMessage) null);
    }

    private static void addDataItem(@Nonnull String code, int steps, @Nonnull byte[] output,
            @CheckForNull AssemblyMessage expectedMessage) {
        TEST_DATA.add(new Object[] { code, steps, output, expectedMessage, null });
    }

    private static void addDataItem(@Nonnull String code, int steps, @Nonnull byte[] output,
            @CheckForNull AssemblyMessage... expectedMessages) {
        TEST_DATA.add(new Object[] { code, steps, output, null, expectedMessages });
    }

    private static String stringOf(char c, int count) {
        char[] arr = new char[count];
        Arrays.fill(arr, c);
        return new String(arr);
    }

    /**
     * Initializes a new ProgramsTest.
     *
     * @param code
     *            assembly code to assemble
     * @param steps
     *            the number of steps the program is expected to take to assemble completely
     * @param output
     *            the program's output
     * @param expectedMessage
     *            an {@link AssemblyMessage} that is expected to be generated while assembling the code
     * @param expectedMessages
     *            an array of {@link AssemblyMessage AssemblyMessages} that are expected to be generated while assembling the code.
     *            Takes priority over <code>expectedMessage</code>.
     */
    public ProgramsTest(@Nonnull String code, int steps, @Nonnull byte[] output, @CheckForNull AssemblyMessage expectedMessage,
            @CheckForNull AssemblyMessage[] expectedMessages) {
        super(code, steps, output, M68KArchitecture.MC68000, expectedMessage, expectedMessages, null);
    }

}
