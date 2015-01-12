package org.reasm.m68k.source;

import javax.annotation.Nonnull;

import org.reasm.source.SourceNode;

interface BlockParser {

    @Nonnull
    SourceNode parseBlock(@Nonnull SourceNodeProducer sourceNodeProducer, @Nonnull BlockDirectiveLine firstLine,
            @Nonnull BlockDirective startingBlockDirective);

}
