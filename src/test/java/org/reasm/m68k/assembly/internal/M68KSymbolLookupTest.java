package org.reasm.m68k.assembly.internal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.reasm.*;
import org.reasm.expressions.EvaluationContext;
import org.reasm.expressions.Expression;
import org.reasm.expressions.ValueExpression;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.source.SourceFile;
import org.reasm.testhelpers.ValueVisitorAdapter;

import ca.fragag.testhelpers.ObjectHashCodeEqualsContract;

/**
 * Test class for {@link M68KSymbolLookup}.
 *
 * @author Francis Gagné
 */
public class M68KSymbolLookupTest extends ObjectHashCodeEqualsContract {

    @Nonnull
    private static final Object MAIN_OBJECT;
    @Nonnull
    private static final Object OTHER_EQUAL_OBJECT;
    @Nonnull
    private static final Object ANOTHER_EQUAL_OBJECT;
    @Nonnull
    private static final Object DIFFERENT_OBJECT_0;
    @Nonnull
    private static final Object DIFFERENT_OBJECT_1;

    static {
        final Object[] objects = new Object[5];

        final Function magic = new Function() {
            @Override
            public Expression call(Expression[] arguments, EvaluationContext evaluationContext) {
                final M68KAssemblyContext context = (M68KAssemblyContext) evaluationContext.getAssemblyMessageConsumer();

                final ValueVisitor<Void> argumentVisitor = new ValueVisitorAdapter<Void>() {
                    @Override
                    public Void visitUnsignedInt(long value) {
                        if (value == 0) {
                            objects[0] = context.createSymbolLookup();
                            objects[1] = context.createSymbolLookup();
                            objects[2] = context.createSymbolLookup();
                        } else if (value == 1) {
                            // This SymbolLookup has a different symbolLookupContext.
                            objects[3] = context.createSymbolLookup();
                        } else if (value == 2) {
                            // This SymbolLookup has a different context.
                            objects[4] = context.createSymbolLookup();
                        } else {
                            throw new AssertionError();
                        }

                        return null;
                    }
                };

                Value.accept(arguments[0].evaluate(evaluationContext), argumentVisitor);

                return new ValueExpression(new UnsignedIntValue(0));
            }
        };

        final List<PredefinedSymbol> symbols = Arrays.asList(new PredefinedSymbol(SymbolContext.VALUE, "MAGIC",
                SymbolType.CONSTANT, new FunctionValue(magic)));
        final PredefinedSymbolTable predefinedSymbols = new PredefinedSymbolTable(symbols);

        {
            final Configuration configuration = new Configuration(Environment.DEFAULT, new SourceFile(
                    "A EQU MAGIC(0)\nB EQU MAGIC(1)", null), M68KArchitecture.MC68000).setPredefinedSymbols(predefinedSymbols);
            final Assembly assembly = new Assembly(configuration);

            assertThat(assembly.step(), is(AssemblyCompletionStatus.PENDING));
            assertThat(assembly.step(), is(AssemblyCompletionStatus.PENDING));
            assertThat(assembly.step(), is(AssemblyCompletionStatus.COMPLETE));
            assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        }

        {
            final Configuration configuration = new Configuration(Environment.DEFAULT, new SourceFile("C EQU MAGIC(2)", null),
                    M68KArchitecture.MC68000).setPredefinedSymbols(predefinedSymbols);
            final Assembly assembly = new Assembly(configuration);

            assertThat(assembly.step(), is(AssemblyCompletionStatus.PENDING));
            assertThat(assembly.step(), is(AssemblyCompletionStatus.COMPLETE));
            assertThat(assembly.getGravity(), is(MessageGravity.NONE));
        }

        MAIN_OBJECT = objects[0];
        OTHER_EQUAL_OBJECT = objects[1];
        ANOTHER_EQUAL_OBJECT = objects[2];
        DIFFERENT_OBJECT_0 = objects[3];
        DIFFERENT_OBJECT_1 = objects[4];
    }

    /**
     * Initializes a new M68KSymbolLookupTest.
     */
    public M68KSymbolLookupTest() {
        super(MAIN_OBJECT, OTHER_EQUAL_OBJECT, ANOTHER_EQUAL_OBJECT, new Object[] { DIFFERENT_OBJECT_0, DIFFERENT_OBJECT_1,
                new Object() });
    }

}
