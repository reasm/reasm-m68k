package org.reasm.m68k.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.source.BlockDirective;
import org.reasm.commons.source.IfBlockParser;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

@Immutable
final class M68KIfBlockParser extends IfBlockParser {

    /** The single instance of the {@link M68KIfBlockParser} class. */
    @Nonnull
    static final M68KIfBlockParser INSTANCE = new M68KIfBlockParser();

    private M68KIfBlockParser() {
    }

    @Override
    public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
        return BlockParsers.IF_BLOCK_TYPES;
    }

    @Override
    protected CompositeSourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
        return new IfBlock(childNodes, parseError);
    }

    @Override
    protected boolean isElseDirective(BlockDirective blockDirective) {
        return blockDirective == M68KBlockDirectives.ELSE;
    }

    @Override
    protected boolean isElseIfDirective(BlockDirective blockDirective) {
        return blockDirective == M68KBlockDirectives.ELSEIF;
    }

    @Override
    protected boolean isEndIfDirective(BlockDirective blockDirective) {
        return blockDirective == M68KBlockDirectives.ENDIF || blockDirective == M68KBlockDirectives.ENDC;
    }

}
