package org.reasm.m68k.source;

import org.reasm.m68k.assembly.internal.Mnemonics;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

final class ObjBlockParser extends BasicBlockParser {

    static final ObjBlockParser OBJ = new ObjBlockParser(Mnemonics.OBJEND);
    static final ObjBlockParser PHASE = new ObjBlockParser(Mnemonics.DEPHASE);

    ObjBlockParser(String endingDirective) {
        super(endingDirective);
    }

    @Override
    SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
        return new ObjBlock(childNodes, parseError);
    }

}
