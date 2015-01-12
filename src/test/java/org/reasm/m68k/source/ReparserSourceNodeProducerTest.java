package org.reasm.m68k.source;

import static org.hamcrest.Matchers.describedAs;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.reasm.SubstringBounds;
import org.reasm.m68k.parseerrors.UnclosedBlockParseError;
import org.reasm.m68k.testhelpers.EquivalentLogicalLine;
import org.reasm.m68k.testhelpers.EquivalentParseError;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.ParseError;
import org.reasm.source.SimpleCompositeSourceNode;
import org.reasm.source.SourceNode;

import ca.fragag.testhelpers.LinePrefixDescription;
import ca.fragag.text.Document;
import ca.fragag.text.DocumentReader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Test class for {@link ReparserSourceNodeProducer}.
 *
 * @author Francis Gagné
 */
@RunWith(Parameterized.class)
public class ReparserSourceNodeProducerTest {

    @Immutable
    private static final class BlockDirectiveLineMatcher extends SourceNodeMatcher {

        @Nonnull
        private final LogicalLineMatcher logicalLineMatcher;

        BlockDirectiveLineMatcher(@Nonnull LogicalLineMatcher logicalLineMatcher) {
            this.logicalLineMatcher = logicalLineMatcher;
        }

        @Override
        void describeTo(Description description, SourceNode oldDocumentRoot) {
            description.appendText("a block directive line wrapping ");
            this.logicalLineMatcher.describeTo(description, oldDocumentRoot);
        }

        @Override
        boolean matches(SourceNode node, SourceNode oldDocumentRoot, Description mismatchDescription) {
            if (!(node instanceof BlockDirectiveLine)) {
                mismatchDescription.appendText("has type " + node.getClass().getSimpleName());
                return false;
            }

            if (!this.logicalLineMatcher
                    .matches(((BlockDirectiveLine) node).getLogicalLine(), oldDocumentRoot, mismatchDescription)) {
                mismatchDescription.appendText("\n          in block directive line");
                return false;
            }

            return true;
        }

    }

    @Immutable
    private static final class CompositeSourceNodeMatcher extends SourceNodeMatcher {

        @Nonnull
        private static final Matcher<Object> NO_PARSE_ERROR = describedAs("no parse error", nullValue());

        @Nonnull
        private final Class<? extends CompositeSourceNode> clazz;
        @Nonnull
        private final ImmutableList<? extends SourceNodeMatcher> childMatchers;
        @Nonnull
        private final Matcher<? super ParseError> parseErrorMatcher;

        CompositeSourceNodeMatcher(@Nonnull Class<? extends CompositeSourceNode> clazz, @CheckForNull ParseError parseError,
                @Nonnull ImmutableList<? extends SourceNodeMatcher> childMatchers) {
            this.clazz = clazz;
            this.childMatchers = childMatchers;

            if (parseError == null) {
                this.parseErrorMatcher = NO_PARSE_ERROR;
            } else {
                this.parseErrorMatcher = new EquivalentParseError(parseError);
            }
        }

        @Override
        public void describeTo(Description description, SourceNode oldDocumentRoot) {
            description.appendText("a composite source node of type ").appendText(this.clazz.getSimpleName());
            description.appendText(", with ").appendDescriptionOf(this.parseErrorMatcher);
            description.appendText(", with ").appendText(String.valueOf(this.childMatchers.size())).appendText(" children:");

            final LinePrefixDescription linePrefixDescription = new LinePrefixDescription(description, "    ", false);
            for (SourceNodeMatcher childMatcher : this.childMatchers) {
                description.appendText("\n- ");
                childMatcher.describeTo(linePrefixDescription, oldDocumentRoot);
            }
        }

        @Override
        boolean matches(SourceNode node, SourceNode oldDocumentRoot, Description mismatchDescription) {
            if (node.getClass() != this.clazz) {
                mismatchDescription.appendText("has type ").appendValue(node.getClass());
                return false;
            }

            if (!this.parseErrorMatcher.matches(node.getParseError())) {
                mismatchDescription.appendText("has parse error ").appendValue(node.getParseError());
                return false;
            }

            final CompositeSourceNode composite = (CompositeSourceNode) node;
            if (composite.getChildNodes().size() != this.childMatchers.size()) {
                mismatchDescription.appendText("has ").appendValue(composite.getChildNodes().size()).appendText(" children");
                return false;
            }

            final UnmodifiableIterator<? extends SourceNodeMatcher> childMatchersIterator = this.childMatchers.iterator();
            final Iterator<SourceNode> compositeChildNodesIterator = composite.getChildNodes().iterator();
            int index = 0;
            while (childMatchersIterator.hasNext()) {
                if (!childMatchersIterator.next().matches(compositeChildNodesIterator.next(), oldDocumentRoot, mismatchDescription)) {
                    mismatchDescription.appendText("\n          in child at index ").appendText(String.valueOf(index));
                    return false;
                }

                index++;
            }

            return true;
        }

    }

    @Immutable
    private static final class LogicalLineMatcher extends SourceNodeMatcher {

        @Nonnull
        private final EquivalentLogicalLine matcher;

        public LogicalLineMatcher(@Nonnull EquivalentLogicalLine matcher) {
            this.matcher = matcher;
        }

        @Override
        void describeTo(Description description, SourceNode oldDocumentRoot) {
            this.matcher.describeTo(description);
        }

        @Override
        boolean matches(SourceNode node, SourceNode oldDocumentRoot, Description mismatchDescription) {
            if (!(node instanceof LogicalLine)) {
                mismatchDescription.appendText("has type " + node.getClass().getSimpleName());
                return false;
            }

            if (!this.matcher.matchesSafely((LogicalLine) node, mismatchDescription)) {
                return false;
            }

            return true;
        }

    }

    @Immutable
    private static final class RecycledSourceNodeMatcher extends SourceNodeMatcher {

        @Nonnull
        private final ImmutableList<Integer> indices;

        public RecycledSourceNodeMatcher(@Nonnull ImmutableList<Integer> indices) {
            this.indices = indices;
        }

        @Override
        public void describeTo(Description description, SourceNode oldDocumentRoot) {
            description.appendValue(this.getExpected(oldDocumentRoot));
        }

        @Override
        boolean matches(SourceNode node, SourceNode oldDocumentRoot, Description mismatchDescription) {
            if (node != this.getExpected(oldDocumentRoot)) {
                mismatchDescription.appendText("got a different node: ").appendValue(node);
                return false;
            }

            return true;
        }

        @Nonnull
        private SourceNode getExpected(@Nonnull SourceNode oldDocumentRoot) {
            SourceNode expected = oldDocumentRoot;
            for (Integer index : this.indices) {
                expected = ((CompositeSourceNode) expected).getChildNodes().get(index);
            }

            return expected;
        }

    }

    // This doesn't need to be public, even though ReparserSourceNodeProducerTest's constructor has a parameter of that type...
    @Immutable
    private static abstract class SourceNodeMatcher {

        SourceNodeMatcher() {
        }

        abstract void describeTo(@Nonnull Description description, @Nonnull SourceNode oldDocumentRoot);

        abstract boolean matches(@Nonnull SourceNode node, @Nonnull SourceNode oldDocumentRoot, Description mismatchDescription);

    }

    @Immutable
    private static final class SourceNodeMatcherHamcrestMatcherAdapter extends TypeSafeDiagnosingMatcher<SourceNode> {

        @Nonnull
        private final SourceNodeMatcher matcher;
        @Nonnull
        private final SourceNode oldDocumentRoot;

        SourceNodeMatcherHamcrestMatcherAdapter(@Nonnull SourceNodeMatcher matcher, @Nonnull SourceNode oldDocumentRoot) {
            this.matcher = matcher;
            this.oldDocumentRoot = oldDocumentRoot;
        }

        @Override
        public void describeTo(Description description) {
            this.matcher.describeTo(description, this.oldDocumentRoot);
        }

        @Override
        protected boolean matchesSafely(SourceNode item, Description mismatchDescription) {
            return this.matcher.matches(item, this.oldDocumentRoot, mismatchDescription);
        }

    }

    @Nonnull
    private static final ArrayList<Object[]> TEST_DATA = new ArrayList<>();

    @Nonnull
    private static final SubstringBounds[] NO_LABELS = new SubstringBounds[0];
    @Nonnull
    private static final SubstringBounds[] NO_OPERANDS = NO_LABELS;
    @Nonnull
    private static final int[] NO_CONTINUATION_CHARACTERS = new int[0];

    static {
        // substring bounds
        final SubstringBounds bounds_1_3 = bounds(1, 3);
        final SubstringBounds bounds_1_4 = bounds(1, 4);
        final SubstringBounds bounds_1_5 = bounds(1, 5);
        final SubstringBounds bounds_1_6 = bounds(1, 6);
        final SubstringBounds bounds_11_13 = bounds(11, 13);
        final SubstringBounds[] array_bounds_4_5 = array(bounds(4, 5));

        // logical line matchers
        final LogicalLineMatcher nop = logicalLine(4, null, NO_LABELS, bounds_1_4, NO_OPERANDS, null, NO_CONTINUATION_CHARACTERS);
        final LogicalLineMatcher nopPlusLF = logicalLine(5, null, NO_LABELS, bounds_1_4, NO_OPERANDS, null,
                NO_CONTINUATION_CHARACTERS);
        final LogicalLineMatcher nopPlusCR = nopPlusLF;
        final LogicalLineMatcher nopPlusCRLF = logicalLine(6, null, NO_LABELS, bounds_1_4, NO_OPERANDS, null,
                NO_CONTINUATION_CHARACTERS);
        final LogicalLineMatcher labelA = logicalLine(1, null, array(bounds(0, 1)), null, NO_OPERANDS, null,
                NO_CONTINUATION_CHARACTERS);

        // block directive line matchers
        final SourceNodeMatcher ifDirective2 = blockDirectiveLine(logicalLine(6, null, NO_LABELS, bounds_1_3, array_bounds_4_5,
                null, NO_CONTINUATION_CHARACTERS));

        // recycled matchers
        final SourceNodeMatcher recycled_0 = recycled(0);
        final SourceNodeMatcher recycled_1 = recycled(1);
        final SourceNodeMatcher recycled_1_0 = recycled(1, 0);
        final SourceNodeMatcher recycled_1_1_0 = recycled(1, 1, 0);
        final SourceNodeMatcher recycled_1_1_1 = recycled(1, 1, 1);
        final SourceNodeMatcher recycled_1_2 = recycled(1, 2);
        final SourceNodeMatcher recycled_2 = recycled(2);

        // composite matchers
        final SourceNodeMatcher nopFile = composite(Block.class, nop);
        final SourceNodeMatcher recycled_0_file = composite(Block.class, recycled_0);
        final SourceNodeMatcher recycled_0_labelA_file = composite(Block.class, recycled_0, labelA);

        // Add text to an empty source file
        addDataItem("", 0, 0, " NOP", " NOP", nopFile);

        // Alter the single line of a source file
        addDataItem(" NP", 2, 0, "O", " NOP", nopFile);

        // Append text to the single line of a source file
        addDataItem(" NO", 3, 0, "P", " NOP", nopFile);

        // Blank a source file
        addDataItem(" NOP", 0, 4, "", "", composite(Block.class));

        // Append text to a source file that ends with a line feed
        addDataItem(" NOP\n", 5, 0, "A", " NOP\nA", recycled_0_labelA_file);

        // Append text to a source file that ends with a carriage return
        addDataItem(" NOP\r", 5, 0, "A", " NOP\rA", recycled_0_labelA_file);

        // Append line feed to a source file that ends with a carriage return
        addDataItem(" NOP\rA", 5, 0, "\n", " NOP\r\nA", composite(Block.class, nopPlusCRLF, recycled_1));

        // Truncate last line
        addDataItem(" NOP\nABC", 6, 2, "", " NOP\nA", recycled_0_labelA_file);

        // Truncate after a line feed
        addDataItem(" NOP\nA", 5, 1, "", " NOP\n", recycled_0_file);

        // Truncate after a carriage return
        addDataItem(" NOP\rA", 5, 1, "", " NOP\r", recycled_0_file);

        // Truncate line feed after a carriage return
        addDataItem(" NOP\r\n", 5, 1, "", " NOP\r", composite(Block.class, nopPlusCR));

        // Insert a line break in the middle of a line
        addDataItem(" NOPA", 4, 0, "\n", " NOP\nA", composite(Block.class, nopPlusLF, labelA));

        // Insert a line
        addDataItem(
                " NOP\n MOVE.W D1,D0\n NOP",
                19,
                0,
                " MOVE.W D2,D1\n",
                " NOP\n MOVE.W D1,D0\n MOVE.W D2,D1\n NOP",
                composite(
                        Block.class,
                        recycled_0,
                        recycled_1,
                        logicalLine(14, null, NO_LABELS, bounds(1, 7), array(bounds(8, 10), bounds_11_13), null,
                                NO_CONTINUATION_CHARACTERS), recycled_2));

        // Remove a line
        addDataItem(" NOP\n MOVE.W D1,D0\n NOP", 5, 14, "", " NOP\n NOP", composite(Block.class, recycled_0, recycled_2));

        // Alter a line before an IF block
        addDataItem(" NOT\n IF 1\n MOVE #0,D0\n ENDIF\n NOP", 3, 1, "P", " NOP\n IF 1\n MOVE #0,D0\n ENDIF\n NOP",
                composite(Block.class, nopPlusLF, recycled_1, recycled_2));

        // Alter an IF directive
        addDataItem(
                " NOP\n IF 0\n MOVE #0,D0\n ENDIF\n NOP",
                9,
                1,
                "1",
                " NOP\n IF 1\n MOVE #0,D0\n ENDIF\n NOP",
                composite(
                        Block.class,
                        recycled_0,
                        composite(IfBlock.class, ifDirective2, composite(SimpleCompositeSourceNode.class, recycled_1_1_0),
                                recycled_1_2), recycled_2));

        // Alter a line within an IF block
        addDataItem(
                " NOP\n IF 1\n MOVE #0,D0\n ENDIF\n NOP",
                18,
                1,
                "123",
                " NOP\n IF 1\n MOVE #123,D0\n ENDIF\n NOP",
                composite(
                        Block.class,
                        recycled_0,
                        composite(
                                IfBlock.class,
                                recycled_1_0,
                                composite(
                                        SimpleCompositeSourceNode.class,
                                        logicalLine(14, null, NO_LABELS, bounds_1_5, array(bounds(6, 10), bounds_11_13), null,
                                                NO_CONTINUATION_CHARACTERS)), recycled_1_2), recycled_2));

        // Alter an ENDIF directive
        addDataItem(
                " NOP\n IF 0\n MOVE #0,D0\n ENDIF\n NOP",
                29,
                0,
                " ; IF 0",
                " NOP\n IF 0\n MOVE #0,D0\n ENDIF ; IF 0\n NOP",
                composite(
                        Block.class,
                        recycled_0,
                        composite(
                                IfBlock.class,
                                recycled_1_0,
                                composite(SimpleCompositeSourceNode.class, recycled_1_1_0),
                                blockDirectiveLine(logicalLine(14, null, NO_LABELS, bounds_1_6, NO_OPERANDS, bounds(7, 13),
                                        NO_CONTINUATION_CHARACTERS))), recycled_2));

        // Alter a line after an IF block
        addDataItem(" NOP\n IF 1\n MOVE #0,D0\n ENDIF\n NOT", 33, 1, "P", " NOP\n IF 1\n MOVE #0,D0\n ENDIF\n NOP",
                composite(Block.class, recycled_0, recycled_1, nop));

        // Create a (broken) block
        addDataItem(
                " NOP\n NOP",
                5,
                0,
                " IF 1\n",
                " NOP\n IF 1\n NOP",
                composite(
                        Block.class,
                        recycled_0,
                        composite(IfBlock.class, new UnclosedBlockParseError(BlockDirective.IF), ifDirective2,
                                composite(SimpleCompositeSourceNode.class, recycled_1))));

        // Close a block
        addDataItem(
                " NOP\n IF 1\n MOVE.W D1,D0\n NOP",
                25,
                0,
                " ENDIF\n",
                " NOP\n IF 1\n MOVE.W D1,D0\n ENDIF\n NOP",
                composite(
                        Block.class,
                        recycled_0,
                        composite(
                                IfBlock.class,
                                recycled_1_0,
                                composite(SimpleCompositeSourceNode.class, recycled_1_1_0),
                                blockDirectiveLine(logicalLine(7, null, NO_LABELS, bounds_1_6, NO_OPERANDS, null,
                                        NO_CONTINUATION_CHARACTERS))), recycled_1_1_1));

        // Insert an ELSE in the middle of an IF block
        addDataItem(
                " NOP\n IF 1\n MOVE.W D1,D0\n MOVE.W D2,D1\n ENDIF\n NOP",
                25,
                0,
                " ELSE\n",
                " NOP\n IF 1\n MOVE.W D1,D0\n ELSE\n MOVE.W D2,D1\n ENDIF\n NOP",
                composite(
                        Block.class,
                        recycled_0,
                        composite(
                                IfBlock.class,
                                recycled_1_0,
                                composite(SimpleCompositeSourceNode.class, recycled_1_1_0),
                                blockDirectiveLine(logicalLine(6, null, NO_LABELS, bounds_1_5, NO_OPERANDS, null,
                                        NO_CONTINUATION_CHARACTERS)), composite(SimpleCompositeSourceNode.class, recycled_1_1_1),
                                recycled_1_2), recycled_2));
    }

    /**
     * Gets the test data for this parameterized test.
     *
     * @return the test data
     */
    @Nonnull
    @Parameters
    public static List<Object[]> data() {
        return TEST_DATA;
    }

    private static void addDataItem(@Nonnull String oldText, int replaceOffset, int lengthToRemove, @Nonnull String textToInsert,
            @Nonnull String newText, @Nonnull SourceNodeMatcher nodeMatcher) {
        TEST_DATA.add(new Object[] { oldText, replaceOffset, lengthToRemove, textToInsert, newText, nodeMatcher });
    }

    @SafeVarargs
    private static <T> T[] array(T... items) {
        return items;
    }

    private static SourceNodeMatcher blockDirectiveLine(LogicalLineMatcher logicalLineMatcher) {
        return new BlockDirectiveLineMatcher(logicalLineMatcher);
    }

    private static SubstringBounds bounds(int start, int end) {
        return new SubstringBounds(start, end);
    }

    private static SourceNodeMatcher composite(Class<? extends CompositeSourceNode> clazz, ParseError parseError,
            SourceNodeMatcher... childrenMatchers) {
        return new CompositeSourceNodeMatcher(clazz, parseError, ImmutableList.copyOf(childrenMatchers));
    }

    private static SourceNodeMatcher composite(Class<? extends CompositeSourceNode> clazz, SourceNodeMatcher... childrenMatchers) {
        return new CompositeSourceNodeMatcher(clazz, null, ImmutableList.copyOf(childrenMatchers));
    }

    private static LogicalLineMatcher logicalLine(int length, @CheckForNull ParseError parseError,
            @Nonnull SubstringBounds[] labels, @CheckForNull SubstringBounds mnemonic, @Nonnull SubstringBounds[] operands,
            @CheckForNull SubstringBounds comment, @Nonnull int[] continuationCharacters) {
        return new LogicalLineMatcher(new EquivalentLogicalLine(new LogicalLine(length, parseError, labels, mnemonic, operands,
                comment, continuationCharacters)));
    }

    private static SourceNodeMatcher recycled(Integer... indices) {
        return new RecycledSourceNodeMatcher(ImmutableList.copyOf(indices));
    }

    @Nonnull
    private final String oldText;
    private final int replaceOffset;
    private final int lengthToRemove;
    @Nonnull
    private final String textToInsert;
    @Nonnull
    private final String newText;
    @Nonnull
    private final SourceNodeMatcher nodeMatcher;

    /**
     * Initializes a new ReparserSourceNodeProducerTest.
     *
     * @param oldText
     *            the old text
     * @param replaceOffset
     *            the position where the replacement occurs
     * @param lengthToRemove
     *            the number of characters to remove starting at the replace offset
     * @param textToInsert
     *            the text to insert at the replace offset (after removing the text to remove)
     * @param newText
     *            the expected new text
     * @param nodeMatcher
     *            a {@link SourceNodeMatcher} that matches the expected source node tree
     */
    public ReparserSourceNodeProducerTest(@Nonnull String oldText, int replaceOffset, int lengthToRemove,
            @Nonnull String textToInsert, @Nonnull String newText, @Nonnull SourceNodeMatcher nodeMatcher) {
        this.oldText = oldText;
        this.replaceOffset = replaceOffset;
        this.lengthToRemove = lengthToRemove;
        this.textToInsert = textToInsert;
        this.newText = newText;
        this.nodeMatcher = nodeMatcher;
    }

    /**
     * Test method for {@link ReparserSourceNodeProducer#next()}.
     */
    @Test
    public void next() {
        final Document oldDocument = new Document(this.oldText);
        final SourceNode oldNode = Parser.parse(oldDocument);
        final Document newDocument = oldDocument.replace(this.replaceOffset, this.lengthToRemove, this.textToInsert);

        // The new text is part of the test data so that you don't have to figure out manually where the replacement happens.
        assertThat(newDocument.toString(), is(this.newText));

        final ReparserSourceNodeProducer sourceNodeProducer = new ReparserSourceNodeProducer(new DocumentReader(newDocument),
                oldNode, this.replaceOffset, this.lengthToRemove, this.textToInsert.length());
        final SourceNode newNode = Parser.parse(sourceNodeProducer);
        assertThat(newNode.getLength(), is(newDocument.length()));

        assertThat(newNode, new SourceNodeMatcherHamcrestMatcherAdapter(this.nodeMatcher, oldNode));

        try {
            sourceNodeProducer.next();
            fail("ReparserSourceNodeProducer.next() should have thrown a NoSuchElementException");
        } catch (NoSuchElementException e) {
        }
    }

}
