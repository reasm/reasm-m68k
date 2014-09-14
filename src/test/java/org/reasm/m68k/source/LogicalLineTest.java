package org.reasm.m68k.source;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.reasm.SubstringBounds;

/**
 * Test class for {@link LogicalLine}.
 *
 * @author Francis Gagné
 */
public class LogicalLineTest {

    private static final LogicalLine LOGICAL_LINE = new LogicalLine(42, null, new SubstringBounds[0], new SubstringBounds(1, 5),
            new SubstringBounds[] { new SubstringBounds(6, 9), new SubstringBounds(12, 15), new SubstringBounds(17, 20),
                    new SubstringBounds(23, 26), new SubstringBounds(28, 31), new SubstringBounds(34, 37),
                    new SubstringBounds(39, 42) }, null, new int[] { 10, 21, 32 });

    /**
     * Asserts that {@link LogicalLine#isContinuationCharacter(int)} returns <code>true</code> when the character at the specified
     * position is a continuation character or <code>false</code> when it is not.
     */
    @Test
    public void isContinuationCharacter() {
        assertThat(LOGICAL_LINE.isContinuationCharacter(0), is(false));
        assertThat(LOGICAL_LINE.isContinuationCharacter(9), is(false));
        assertThat(LOGICAL_LINE.isContinuationCharacter(10), is(true));
        assertThat(LOGICAL_LINE.isContinuationCharacter(11), is(false));
        assertThat(LOGICAL_LINE.isContinuationCharacter(20), is(false));
        assertThat(LOGICAL_LINE.isContinuationCharacter(21), is(true));
        assertThat(LOGICAL_LINE.isContinuationCharacter(22), is(false));
        assertThat(LOGICAL_LINE.isContinuationCharacter(31), is(false));
        assertThat(LOGICAL_LINE.isContinuationCharacter(32), is(true));
    }

}
