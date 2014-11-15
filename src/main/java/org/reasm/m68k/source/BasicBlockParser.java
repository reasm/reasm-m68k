package org.reasm.m68k.source;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.assembly.internal.Mnemonics;
import org.reasm.m68k.parseerrors.UnclosedBlockParseError;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;

@Immutable
class BasicBlockParser implements BlockParser {

    @Nonnull
    static final BasicBlockParser DO = new BasicBlockParser(Mnemonics.UNTIL) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new DoBlock(childNodes, parseError);
        }
    };

    @Nonnull
    static final BasicBlockParser FOR = new BasicBlockParser(Mnemonics.NEXT) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new ForBlock(childNodes, parseError);
        };

        @Override
        void missingEndDirective(ArrayList<SourceNode> nodes) {
            nodes.add(ImplicitNextNode.INSTANCE);
        };
    };

    @Nonnull
    static final BasicBlockParser MACRO = new BasicBlockParser(Mnemonics.ENDM) {
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
    static final BasicBlockParser NAMESPACE = new BasicBlockParser(Mnemonics.ENDNS) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new NamespaceBlock(childNodes, parseError);
        }
    };

    @Nonnull
    static final BasicBlockParser REPT = new BasicBlockParser(Mnemonics.ENDR) {
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
    static final BasicBlockParser TRANSFORM = new BasicBlockParser(Mnemonics.ENDTRANSFORM) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new TransformBlock(childNodes, parseError);
        }
    };

    @Nonnull
    static final BasicBlockParser WHILE = new BasicBlockParser(Mnemonics.ENDW) {
        @Override
        SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new WhileBlock(childNodes, parseError);
        }
    };

    @Nonnull
    private final String endingDirective;

    BasicBlockParser(@Nonnull String endingDirective) {
        this.endingDirective = endingDirective;
    }

    @Override
    public final SourceNode parseBlock(CharSequenceReader<?> reader, LogicalLine firstLine, String blockMnemonic) {
        final ArrayList<SourceNode> nodes = new ArrayList<>(3);
        nodes.add(new BlockDirectiveLine(firstLine));

        final ArrayList<SourceNode> bodyNodes = new ArrayList<>();

        while (!reader.atEnd()) {
            final LogicalLine logicalLine = LogicalLineParser.parse(reader);

            // Get the mnemonic on this logical line, if any.
            final String mnemonic = Parser.readBackMnemonic(reader, logicalLine);

            if (mnemonic == null) {
                bodyNodes.add(logicalLine);
            } else if (mnemonic.equalsIgnoreCase(this.endingDirective)) {
                nodes.add(this.createBodyBlock(bodyNodes));
                nodes.add(new BlockDirectiveLine(logicalLine));
                return this.createBlock(nodes, null);
            } else {
                Parser.processBlockBodyLine(reader, bodyNodes, logicalLine, mnemonic);
            }
        }

        // We didn't find the end of the block: return with an error.
        nodes.add(this.createBodyBlock(bodyNodes));
        this.missingEndDirective(nodes);
        return this.createBlock(nodes, new UnclosedBlockParseError(blockMnemonic));
    }

    @Nonnull
    SourceNode createBlock(@Nonnull Iterable<? extends SourceNode> childNodes, @CheckForNull ParseError parseError) {
        return new Block(childNodes, parseError);
    }

    @Nonnull
    SourceNode createBodyBlock(@Nonnull Iterable<? extends SourceNode> childNodes) {
        return new SimpleCompositeSourceNode(childNodes);
    }

    void missingEndDirective(@Nonnull @SuppressWarnings("unused") ArrayList<SourceNode> nodes) {
    }

}
