package org.reasm.m68k.source;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.SubstringBounds;
import org.reasm.source.SourceNode;

import ca.fragag.text.CharSequenceReader;

class SourceNodeProducer {

    @Nonnull
    private final CharSequenceReader<?> reader;

    SourceNodeProducer(@Nonnull CharSequenceReader<?> reader) {
        this.reader = reader;
    }

    final boolean atEnd() {
        return this.reader.atEnd();
    }

    final CharSequenceReader<?> getReader() {
        return this.reader;
    }

    @Nonnull
    SourceNode next() {
        final LogicalLine logicalLine = LogicalLineParser.parse(this.reader);
        final String mnemonic = this.getMnemonic(logicalLine);
        final BlockDirective blockDirective;
        if (mnemonic != null && (blockDirective = BlockDirective.MAP.get(mnemonic)) != null) {
            return new BlockDirectiveLine(logicalLine, blockDirective);
        }

        return logicalLine;
    }

    @CheckForNull
    private final String getMnemonic(@Nonnull LogicalLine logicalLine) {
        final SubstringBounds mnemonicBounds = logicalLine.getMnemonicBounds();
        if (mnemonicBounds == null) {
            // There's no mnemonic on this line.
            return null;
        }

        final int backupPosition = this.reader.getCurrentPosition();
        try {
            // Temporarily move back the reader to the start of the mnemonic and read the mnemonic.
            this.reader.setCurrentPosition(backupPosition - logicalLine.getLength() + mnemonicBounds.getStart());
            String mnemonic = this.reader.readSubstring(mnemonicBounds.getEnd() - mnemonicBounds.getStart());

            // If the mnemonic starts with '!', remove that character.
            // '!' is used to bypass macros, but block directives always bypass macros anyway.
            if (mnemonic.startsWith("!")) {
                mnemonic = mnemonic.substring(1);
            }

            // If the mnemonic has a size attribute, remove it.
            // If someone writes something silly like "IF.W", we'll still open an IF block
            // because the IF directive expects to be in an IF block.
            final int indexOfPeriod = mnemonic.indexOf('.');
            if (indexOfPeriod != -1) {
                mnemonic = mnemonic.substring(0, indexOfPeriod);
            }

            return mnemonic;
        } finally {
            // Restore the reader's position.
            this.reader.setCurrentPosition(backupPosition);
        }
    }

}
