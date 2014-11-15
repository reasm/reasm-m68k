package org.reasm.m68k.source;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.SubstringBounds;
import org.reasm.m68k.assembly.internal.Mnemonics;
import org.reasm.source.AbstractSourceFile;
import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;
import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;

/**
 * The parser for M68000 family assembler source files.
 *
 * @author Francis Gagné
 */
public final class Parser {

    @Nonnull
    private static final Map<String, BlockParser> BLOCKS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    static {
        BLOCKS.put(Mnemonics.DO, BasicBlockParser.DO);
        BLOCKS.put(Mnemonics.FOR, BasicBlockParser.FOR);
        BLOCKS.put(Mnemonics.IF, IfBlockParser.IF);
        BLOCKS.put(Mnemonics.MACRO, BasicBlockParser.MACRO);
        BLOCKS.put(Mnemonics.NAMESPACE, BasicBlockParser.NAMESPACE);
        BLOCKS.put(Mnemonics.OBJ, ObjBlockParser.OBJ);
        BLOCKS.put(Mnemonics.PHASE, ObjBlockParser.PHASE);
        BLOCKS.put(Mnemonics.REPT, BasicBlockParser.REPT);
        BLOCKS.put(Mnemonics.TRANSFORM, BasicBlockParser.TRANSFORM);
        BLOCKS.put(Mnemonics.WHILE, BasicBlockParser.WHILE);
    }

    /**
     * Determines whether the specified code point represents whitespace or not.
     *
     * @param codePoint
     *            the code point to test
     * @return <code>true</code> if the code point represents whitespace, otherwise <code>false</code>
     */
    public static boolean isWhitespace(int codePoint) {
        switch (codePoint) {
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
            return true;
        }

        return false;
    }

    /**
     * Parses the contents of a source file.
     *
     * @param text
     *            the contents of the source file
     * @return a {@link SourceNode} that is the root of the source file's abstract syntax tree
     */
    @Nonnull
    public static SourceNode parse(@Nonnull Document text) {
        final CharSequenceReader<?> reader = new DocumentReader(text);

        final ArrayList<SourceNode> nodes = new ArrayList<>();
        while (!reader.atEnd()) {
            final LogicalLine logicalLine = LogicalLineParser.parse(reader);

            // Get the mnemonic on this logical line, if any.
            final String mnemonic = readBackMnemonic(reader, logicalLine);
            if (mnemonic == null) {
                nodes.add(logicalLine);
            } else {
                processBlockBodyLine(reader, nodes, logicalLine, mnemonic);
            }
        }

        return new Block(nodes, null);
    }

    /**
     * Re-parses the contents of a source file after it has been altered.
     *
     * @param text
     *            the new contents of the source file
     * @param oldSourceFile
     *            the old source file
     * @param replaceOffset
     *            the offset at which the replace occurred
     * @param lengthToRemove
     *            the length of text from the old source file that was removed
     * @param lengthToInsert
     *            the length of text from the new source file that was inserted
     * @return a {@link SourceNode} that is the root of the source file's abstract syntax tree
     */
    @Nonnull
    public static SourceNode reparse(@Nonnull Document text, @Nonnull AbstractSourceFile<?> oldSourceFile, int replaceOffset,
            int lengthToRemove, int lengthToInsert) {
        // TODO Implement incremental re-parsing
        return parse(text);
    }

    static void processBlockBodyLine(@Nonnull CharSequenceReader<?> reader, @Nonnull ArrayList<SourceNode> childNodes,
            @Nonnull LogicalLine logicalLine, @Nonnull String mnemonic) {
        // Check if this mnemonic starts a block.
        final BlockParser blockParser = BLOCKS.get(mnemonic);

        // If the mnemonic doesn't start a block, add the logical line to the child nodes list.
        // Otherwise, parse a block.
        if (blockParser == null) {
            childNodes.add(logicalLine);
        } else {
            childNodes.add(blockParser.parseBlock(reader, logicalLine, mnemonic));
        }
    }

    @CheckForNull
    static String readBackMnemonic(@Nonnull CharSequenceReader<?> reader, @Nonnull LogicalLine logicalLine) {
        String mnemonic;
        final SubstringBounds mnemonicBounds = logicalLine.getMnemonicBounds();
        if (mnemonicBounds == null) {
            // There's no mnemonic on this line.
            mnemonic = null;
        } else {
            final int backupPosition = reader.getCurrentPosition();
            try {
                // Temporarily move back the reader to the start of the mnemonic and read the mnemonic.
                reader.setCurrentPosition(backupPosition - logicalLine.getLength() + mnemonicBounds.getStart());
                mnemonic = reader.readSubstring(mnemonicBounds.getEnd() - mnemonicBounds.getStart());

                // If the mnemonic starts with '!', remove that character.
                // '!' is used to bypass macros, but block directives always bypass macros anyway.
                if (mnemonic.startsWith("!")) {
                    mnemonic = mnemonic.substring(1);
                }

                // If the mnemonic has a size attribute, remove it.
                // If someone writes something silly like "IF.W", we'll still open an IF block
                // because the IF directive expects to be in an IF block.
                final int indexOfPeriod = mnemonic.indexOf('.');
                if (indexOfPeriod != -1) {
                    mnemonic = mnemonic.substring(0, indexOfPeriod);
                }
            } finally {
                // Restore the reader's position.
                reader.setCurrentPosition(backupPosition);
            }
        }

        return mnemonic;
    }

    // This class is not meant to be instantiated.
    private Parser() {
    }

}
