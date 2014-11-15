package org.reasm.m68k.source;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.assembly.internal.Mnemonics;
import org.reasm.m68k.parseerrors.ElseOrElseIfAfterElseParseError;
import org.reasm.m68k.parseerrors.UnclosedBlockParseError;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;

@Immutable
final class IfBlockParser implements BlockParser {

    @Nonnull
    public static final IfBlockParser IF = new IfBlockParser();

    private IfBlockParser() {
    }

    @Override
    public SourceNode parseBlock(CharSequenceReader<?> reader, LogicalLine firstLine, String blockMnemonic) {
        // The child nodes of an IfBlock are structured like this:
        //   (LogicalLine SimpleCompositeSourceNode)+ LogicalLine?
        // The LogicalLine in the repetition block is an IF, ELSEIF or ELSE directive.
        // The SimpleCompositeSourceNode following it is the body for that branch.
        // The last LogicalLine is an ENDIF directive (it may be missing).

        final ArrayList<SourceNode> nodes = new ArrayList<>();
        nodes.add(new BlockDirectiveLine(firstLine));

        ParseError parseError = null;
        ArrayList<SourceNode> bodyNodes = new ArrayList<>();
        boolean gotElse = false;

        while (!reader.atEnd()) {
            final LogicalLine logicalLine = LogicalLineParser.parse(reader);

            // Get the mnemonic on this logical line, if any.
            boolean isElse = false;
            final String mnemonic = Parser.readBackMnemonic(reader, logicalLine);
            if (mnemonic == null) {
                bodyNodes.add(logicalLine);
            } else if (mnemonic.equalsIgnoreCase(Mnemonics.ENDIF) || mnemonic.equalsIgnoreCase(Mnemonics.ENDC)) {
                nodes.add(new SimpleCompositeSourceNode(bodyNodes));
                nodes.add(new BlockDirectiveLine(logicalLine));
                return new IfBlock(nodes, parseError);
            } else if ((isElse = mnemonic.equalsIgnoreCase(Mnemonics.ELSE)) || mnemonic.equalsIgnoreCase(Mnemonics.ELSEIF)) {
                nodes.add(new SimpleCompositeSourceNode(bodyNodes));
                nodes.add(new BlockDirectiveLine(logicalLine));

                // SimpleCompositeSourceNode's constructor copies the contents of the list it receives,
                // so we can reuse our list.
                bodyNodes.clear();

                // If this is the first ELSE or ELSEIF clause
                // following the first ELSE clause of this IF block,
                // raise an error.
                if (gotElse && parseError == null) {
                    parseError = new ElseOrElseIfAfterElseParseError(mnemonic);
                }

                // If this is an ELSE clause,
                // set a flag to raise an error on subsequent ELSE or ELSEIF clauses.
                if (isElse) {
                    gotElse = true;
                }
            } else {
                Parser.processBlockBodyLine(reader, bodyNodes, logicalLine, mnemonic);
            }
        }

        // We didn't find the end of the block: return with an error.
        nodes.add(new SimpleCompositeSourceNode(bodyNodes));
        return new IfBlock(nodes, new UnclosedBlockParseError(blockMnemonic));
    }

}
