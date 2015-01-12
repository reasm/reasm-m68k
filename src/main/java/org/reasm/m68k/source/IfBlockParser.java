package org.reasm.m68k.source;

import java.util.ArrayList;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.parseerrors.ElseOrElseIfAfterElseParseError;
import org.reasm.m68k.parseerrors.UnclosedBlockParseError;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

@Immutable
final class IfBlockParser implements BlockParser {

    @Nonnull
    public static final IfBlockParser IF = new IfBlockParser();

    private IfBlockParser() {
    }

    @Override
    public final SourceNode parseBlock(SourceNodeProducer sourceNodeProducer, BlockDirectiveLine firstLine,
            BlockDirective startingBlockDirective) {
        // The child nodes of an IfBlock are structured like this:
        //   (LogicalLine SimpleCompositeSourceNode)+ LogicalLine?
        // The LogicalLine in the repetition block is an IF, ELSEIF or ELSE directive.
        // The SimpleCompositeSourceNode following it is the body for that branch.
        // The last LogicalLine is an ENDIF directive (it may be missing).

        final ArrayList<SourceNode> nodes = new ArrayList<>();
        nodes.add(firstLine);

        ParseError parseError = null;
        ArrayList<SourceNode> bodyNodes = new ArrayList<>();
        boolean gotElse = false;

        while (!sourceNodeProducer.atEnd()) {
            final SourceNode sourceNode = sourceNodeProducer.next();

            // Check if this logical line has a block directive.
            final BlockDirective blockDirective = BlockDirective.getBlockDirective(sourceNode);

            // Get the mnemonic on this logical line, if any.
            boolean isElse = false;
            if (blockDirective == BlockDirective.ENDIF || blockDirective == BlockDirective.ENDC) {
                nodes.add(new SimpleCompositeSourceNode(bodyNodes));
                nodes.add(sourceNode);
                return new IfBlock(nodes, parseError);
            } else if ((isElse = blockDirective == BlockDirective.ELSE) || blockDirective == BlockDirective.ELSEIF) {
                nodes.add(new SimpleCompositeSourceNode(bodyNodes));
                nodes.add(sourceNode);

                // SimpleCompositeSourceNode's constructor copies the contents of the list it receives,
                // so we can reuse our list.
                bodyNodes.clear();

                // If this is the first ELSE or ELSEIF clause
                // following the first ELSE clause of this IF block,
                // raise an error.
                if (gotElse && parseError == null) {
                    parseError = new ElseOrElseIfAfterElseParseError(blockDirective);
                }

                // If this is an ELSE clause,
                // set a flag to raise an error on subsequent ELSE or ELSEIF clauses.
                if (isElse) {
                    gotElse = true;
                }
            } else {
                Parser.processBlockBodyLine(sourceNodeProducer, bodyNodes, sourceNode, blockDirective);
            }
        }

        // We didn't find the end of the block: return with an error.
        nodes.add(new SimpleCompositeSourceNode(bodyNodes));
        return new IfBlock(nodes, new UnclosedBlockParseError(startingBlockDirective));
    }

}
