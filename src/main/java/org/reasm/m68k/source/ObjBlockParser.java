package org.reasm.m68k.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.m68k.assembly.internal.Mnemonics;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

@Immutable
final class ObjBlockParser extends BasicBlockParser {

    @Nonnull
    static final ObjBlockParser OBJ = new ObjBlockParser(Mnemonics.OBJEND);
    @Nonnull
    static final ObjBlockParser PHASE = new ObjBlockParser(Mnemonics.DEPHASE);

    ObjBlockParser(@Nonnull String endingDirective) {
        super(endingDirective);
    }

    @Override
    SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
        return new ObjBlock(childNodes, parseError);
    }

}
