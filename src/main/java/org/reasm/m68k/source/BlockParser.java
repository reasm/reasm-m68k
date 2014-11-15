package org.reasm.m68k.source;

import javax.annotation.Nonnull;

import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;

interface BlockParser {

    @Nonnull
    SourceNode parseBlock(@Nonnull CharSequenceReader<?> reader, @Nonnull LogicalLine firstLine, @Nonnull String blockMnemonic);

}
