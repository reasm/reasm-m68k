package org.reasm.m68k.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.source.BlockDirective;
import org.reasm.commons.source.BlockParser;
import org.reasm.commons.source.Parser;
import org.reasm.commons.source.Syntax;

import com.google.common.collect.ImmutableMap;

/**
 * The parser for M68000 family assembler source files.
 *
 * @author Francis Gagné
 */
@Immutable
public final class M68KParser extends Parser {

    /** The syntax rules for the M68000 family assembly language. */
    @Nonnull
    public static final Syntax SYNTAX;

    static {
        final int[] invalidIdentifierCodePoints = new int[] {

                '!', // logical NOT operator; "strictly different from" operator
                //'"', // string delimiter; allowed in identifiers, except as the first character
                //'#', // start of immediate data; allowed in identifiers, except as the first character
                //'$', // prefix for hexadecimal integer literals; allowed in identifiers, except as the first character
                '%', // modulo operator; prefix for binary integer literals
                '&', // bitwise AND operator; logical AND operator
                //'\'', // string delimiter; allowed in identifiers, except as the first character
                '(', // grouping left parenthesis; start of function call argument list
                ')', // grouping right parenthesis; end of function call argument list
                '*', // multiplication operator; part of index register scale specification
                '+', // addition operator; unary plus operator
                ',', // operand separator, argument separator
                '-', // subtraction operator; negation operator; part of register range specification
                //'.', // pseudo object member accessor (actually part of the symbol name)
                '/', // division operator; part of register list specification
                ':', // end of label; conditional operator, second part
                ';', // start of comment
                '<', // "less than" operator; "less than or equal to" operator; "different from" operator; bit shift left operator
                '=', // "equal to" operator; "strictly equal to" operator; "strictly different from" operator; "less than or equal to" operator; "greater than or equal to" operator
                '>', // "greater than" operator; "greater than or equal to" operator; "different from" operator; bit shift right operator
                '?', // conditional operator, first part
                //'@', // prefix for local symbols (part of the symbol name)
                '[', // start of array indexer
                '\\', // prefix for reference to a macro argument
                ']', // end of array indexer
                '^', // bitwise XOR operator
                //'`', // not assigned
                '{', // start of bit field specification
                '|', // bitwise OR operator; logical OR operator
                '}', // end of bit field specification
                '~', // bitwise NOT operator

        };

        final int[] invalidIdentifierInitialCodePoints = new int[] { '"', '#', '$', '\'' };

        SYNTAX = new Syntax(invalidIdentifierCodePoints, invalidIdentifierInitialCodePoints);
    }

    @Nonnull
    private static final ImmutableMap<BlockDirective, BlockParser> BLOCKS;

    static {
        final ImmutableMap.Builder<BlockDirective, BlockParser> blocks = ImmutableMap.builder();
        blocks.put(M68KBlockDirectives.DO, BlockParsers.DO);
        blocks.put(M68KBlockDirectives.FOR, BlockParsers.FOR);
        blocks.put(M68KBlockDirectives.IF, BlockParsers.IF);
        blocks.put(M68KBlockDirectives.MACRO, BlockParsers.MACRO);
        blocks.put(M68KBlockDirectives.NAMESPACE, BlockParsers.NAMESPACE);
        blocks.put(M68KBlockDirectives.OBJ, BlockParsers.OBJ);
        blocks.put(M68KBlockDirectives.PHASE, BlockParsers.PHASE);
        blocks.put(M68KBlockDirectives.REPT, BlockParsers.REPT);
        blocks.put(M68KBlockDirectives.TRANSFORM, BlockParsers.TRANSFORM);
        blocks.put(M68KBlockDirectives.WHILE, BlockParsers.WHILE);
        BLOCKS = blocks.build();
    }

    /** The single instance of the {@link M68KParser} class. */
    @Nonnull
    public static final M68KParser INSTANCE = new M68KParser();

    private M68KParser() {
        super(SYNTAX, M68KBlockDirectives.MAP, BLOCKS, M68KLogicalLineFactory.INSTANCE, M68KBlockDirectiveLineFactory.INSTANCE);
    }

    @Override
    public final String undecorateMnemonic(String mnemonic) {
        mnemonic = super.undecorateMnemonic(mnemonic);

        // If the mnemonic has a size attribute, remove it.
        final int indexOfPeriod = mnemonic.indexOf('.');
        if (indexOfPeriod != -1) {
            mnemonic = mnemonic.substring(0, indexOfPeriod);
        }

        return mnemonic;
    }

}
