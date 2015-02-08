package org.reasm.m68k.source;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.parseerrors.UnclosedBlockParseError;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

@Immutable
class BasicBlockParser implements BlockParser {

    @Nonnull
    static final BasicBlockParser DO = new BasicBlockParser(BlockDirective.UNTIL) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new DoBlock(childNodes, parseError);
        }
    };

    @Nonnull
    static final BasicBlockParser FOR = new BasicBlockParser(BlockDirective.NEXT) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new ForBlock(childNodes, parseError);
        };
    };

    @Nonnull
    static final BasicBlockParser MACRO = new BasicBlockParser(BlockDirective.ENDM) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new MacroBlock(childNodes, parseError);
        };

        @Override
        SourceNode createBodyBlock(Iterable<? extends SourceNode> childNodes) {
            return new MacroBody(childNodes);
        }
    };

    @Nonnull
    static final BasicBlockParser NAMESPACE = new BasicBlockParser(BlockDirective.ENDNS) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new NamespaceBlock(childNodes, parseError);
        }
    };

    @Nonnull
    static final BasicBlockParser REPT = new BasicBlockParser(BlockDirective.ENDR) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new ReptBlock(childNodes, parseError);
        }

        @Override
        SourceNode createBodyBlock(Iterable<? extends SourceNode> childNodes) {
            return new ReptBody(childNodes);
        }
    };

    @Nonnull
    static final BasicBlockParser TRANSFORM = new BasicBlockParser(BlockDirective.ENDTRANSFORM) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new TransformBlock(childNodes, parseError);
        }
    };

    @Nonnull
    static final BasicBlockParser WHILE = new BasicBlockParser(BlockDirective.ENDW) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new WhileBlock(childNodes, parseError);
        }
    };

    @Nonnull
    private final BlockDirective endingDirective;

    BasicBlockParser(@Nonnull BlockDirective endingDirective) {
        this.endingDirective = endingDirective;
    }

    @Override
    public final SourceNode parseBlock(SourceNodeProducer sourceNodeProducer, BlockDirectiveLine firstLine,
            BlockDirective startingBlockDirective) {
        final ArrayList<SourceNode> nodes = new ArrayList<>(3);
        nodes.add(firstLine);

        final ArrayList<SourceNode> bodyNodes = new ArrayList<>();

        while (!sourceNodeProducer.atEnd()) {
            final SourceNode sourceNode = sourceNodeProducer.next();

            // Check if this logical line has a block directive.
            final BlockDirective blockDirective = BlockDirective.getBlockDirective(sourceNode);

            if (blockDirective == this.endingDirective) {
                nodes.add(this.createBodyBlock(bodyNodes));
                nodes.add(sourceNode);
                return this.createBlock(nodes, null);
            }

            Parser.processBlockBodyLine(sourceNodeProducer, bodyNodes, sourceNode, blockDirective);
        }

        // We didn't find the end of the block: return with an error.
        nodes.add(this.createBodyBlock(bodyNodes));
        return this.createBlock(nodes, new UnclosedBlockParseError(startingBlockDirective));
    }

    @Nonnull
    SourceNode createBlock(@Nonnull Iterable<? extends SourceNode> childNodes, @CheckForNull ParseError parseError) {
        return new Block(childNodes, parseError);
    }

    @Nonnull
    SourceNode createBodyBlock(@Nonnull Iterable<? extends SourceNode> childNodes) {
        return new SimpleCompositeSourceNode(childNodes);
    }

}
