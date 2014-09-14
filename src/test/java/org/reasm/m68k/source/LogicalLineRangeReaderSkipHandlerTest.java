package org.reasm.m68k.source;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.source.SourceFile;
import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNodeRangeReader;

/**
 * Test class for {@link LogicalLineRangeReaderSkipHandler}.
 *
 * @author Francis Gagné
 */
public class LogicalLineRangeReaderSkipHandlerTest {

    /**
     * Asserts that {@link LogicalLineRangeReaderSkipHandler#skipCurrentCodePoint()} skips continuation characters as well as the
     * line break and subsequent whitespace following them.
     */
    @Test
    public void skipCurrentCodePoint() {
        final SourceFile sourceFile = new SourceFile("a\tb c&d&\ne&\rf&\r\ng&\n\t h", "");
        final SourceLocation sourceLocation = sourceFile.getSourceLocations(M68KArchitecture.MC68000).get(0)
                .getChildSourceLocations().get(0);
        final SourceNodeRangeReader reader = new SourceNodeRangeReader(sourceLocation, 0, sourceFile.getText().length(),
                new LogicalLineRangeReaderSkipHandler());
        assertThat(reader.getCurrentPosition(), is(0));
        assertThat(reader.getCurrentCodePoint(), is((int) 'a'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(1));
        assertThat(reader.getCurrentCodePoint(), is((int) '\t'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(2));
        assertThat(reader.getCurrentCodePoint(), is((int) 'b'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(3));
        assertThat(reader.getCurrentCodePoint(), is((int) ' '));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(4));
        assertThat(reader.getCurrentCodePoint(), is((int) 'c'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(5));
        assertThat(reader.getCurrentCodePoint(), is((int) '&'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(6));
        assertThat(reader.getCurrentCodePoint(), is((int) 'd'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(9));
        assertThat(reader.getCurrentCodePoint(), is((int) 'e'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(12));
        assertThat(reader.getCurrentCodePoint(), is((int) 'f'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(16));
        assertThat(reader.getCurrentCodePoint(), is((int) 'g'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(21));
        assertThat(reader.getCurrentCodePoint(), is((int) 'h'));
        reader.advance();
        assertThat(reader.getCurrentPosition(), is(22));
        assertThat(reader.getCurrentCodePoint(), is(-1));
    }

}
