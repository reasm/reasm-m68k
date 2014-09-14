package org.reasm.m68k.source;

import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;

interface BlockParser {

    SourceNode parseBlock(CharSequenceReader<?> reader, LogicalLine firstLine, String blockMnemonic);

}
