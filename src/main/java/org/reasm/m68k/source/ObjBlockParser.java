package org.reasm.m68k.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.commons.source.BasicBlockParser;
import org.reasm.commons.source.BlockDirective;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

@Immutable
final class ObjBlockParser extends BasicBlockParser {

    ObjBlockParser(@Nonnull BlockDirective endingDirective) {
        super(endingDirective);
    }

    @Override
    public final Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
        return BlockParsers.OBJ_BLOCK_TYPES;
    }

    @Override
    protected final SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
        return new ObjBlock(childNodes, parseError);
    }

}
