package org.reasm.m68k;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.reasm.*;
import org.reasm.source.SourceFile;
import org.reasm.testhelpers.UserSymbolMatcher;

import ca.fragag.Consumer;

import com.google.common.collect.ImmutableList;

/**
 * Test class for {@link M68KArchitecture}.
 *
 * @author Francis Gagné
 */
public class M68KArchitectureTest {

    @Nonnull
    private static final UnsignedIntValue ONE_HUNDRED = new UnsignedIntValue(100);
    @Nonnull
    private static final UnsignedIntValue TWENTY = new UnsignedIntValue(20);

    @Nonnull
    private static final Consumer<AssemblyMessage> FAILING_ASSEMBLY_MESSAGE_CONSUMER = new Consumer<AssemblyMessage>() {
        @Override
        public void accept(AssemblyMessage message) {
            fail();
        }
    };

    @Nonnull
    private static final Consumer<SymbolReference> FAILING_SYMBOL_REFERENCE_CONSUMER = new Consumer<SymbolReference>() {
        @Override
        public void accept(SymbolReference reference) {
            fail();
        }
    };

    @Nonnull
    private static Assembly createAssembly1() {
        final PredefinedSymbol fooSymbol = new PredefinedSymbol(SymbolContext.VALUE, "foo", SymbolType.CONSTANT, ONE_HUNDRED);
        final PredefinedSymbol barSymbol = new PredefinedSymbol(SymbolContext.VALUE, "bar", SymbolType.CONSTANT, TWENTY);
        final PredefinedSymbolTable predefinedSymbols = new PredefinedSymbolTable(Arrays.asList(fooSymbol, barSymbol));

        final Configuration configuration = new Configuration(Environment.DEFAULT, new SourceFile("", ""), M68KArchitecture.MC68000)
                .setPredefinedSymbols(predefinedSymbols);
        final Assembly assembly = new Assembly(configuration);
        return assembly;
    }

    /**
     * Asserts that {@link M68KArchitecture#evaluateExpression(CharSequence, Assembly, Consumer, Consumer)} throws a
     * {@link NullPointerException} when the <code>assembly</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void evaluateExpressionNullAssembly() {
        M68KArchitecture.MC68000.evaluateExpression("2+2", null, null, null);
    }

    /**
     * Asserts that {@link M68KArchitecture#evaluateExpression(CharSequence, Assembly, Consumer, Consumer)} throws a
     * {@link NullPointerException} when the <code>expression</code> argument is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void evaluateExpressionNullExpression() {
        final Configuration configuration = new Configuration(Environment.DEFAULT, new SourceFile("", ""), M68KArchitecture.MC68000);
        final Assembly assembly = new Assembly(configuration);
        M68KArchitecture.MC68000.evaluateExpression(null, assembly, null, null);
    }

    /**
     * Asserts that {@link M68KArchitecture#evaluateExpression(CharSequence, Assembly, Consumer, Consumer)} evaluates an expression.
     */
    @Test
    public void evaluateExpressionNullSymbolReferenceConsumer() {
        final Assembly assembly = createAssembly1();

        final Value value = M68KArchitecture.MC68000.evaluateExpression("foo+bar+3", assembly, null,
                FAILING_ASSEMBLY_MESSAGE_CONSUMER);
        assertThat(value, is((Value) new UnsignedIntValue(123)));
    }

    /**
     * Asserts that {@link M68KArchitecture#evaluateExpression(CharSequence, Assembly, Consumer, Consumer)} correctly evaluates a
     * simple expression.
     */
    @Test
    public void evaluateExpressionSimple() {
        final Assembly assembly = createAssembly1();
        final Value value = M68KArchitecture.MC68000.evaluateExpression("2+7*3", assembly, FAILING_SYMBOL_REFERENCE_CONSUMER,
                FAILING_ASSEMBLY_MESSAGE_CONSUMER);
        assertThat(value, is((Value) new UnsignedIntValue(23)));
    }

    /**
     * Asserts that {@link M68KArchitecture#evaluateExpression(CharSequence, Assembly, Consumer, Consumer)} correctly evaluates an
     * expression that contains symbol references.
     */
    @Test
    public void evaluateExpressionWithSymbols() {
        final Assembly assembly = createAssembly1();

        final List<SymbolReference> symbolReferences = new ArrayList<>();
        final Consumer<SymbolReference> symbolReferenceConsumer = new Consumer<SymbolReference>() {
            @Override
            public void accept(SymbolReference symbolReference) {
                symbolReferences.add(symbolReference);
            }
        };

        final Value value = M68KArchitecture.MC68000.evaluateExpression("foo+bar+3", assembly, symbolReferenceConsumer,
                FAILING_ASSEMBLY_MESSAGE_CONSUMER);
        assertThat(value, is((Value) new UnsignedIntValue(123)));

        assertThat(symbolReferences.size(), is(2));

        SymbolReference symbolReference;

        symbolReference = symbolReferences.get(0);
        assertThat(symbolReference, is(notNullValue()));
        assertThat(symbolReference.getContexts(), contains((Object) SymbolContext.VALUE));
        assertThat(symbolReference.getName(), is("foo"));
        assertThat(symbolReference.getSymbol(), is(notNullValue()));
        assertThat(symbolReference.getSymbol(), is(instanceOf(UserSymbol.class)));
        assertThat((UserSymbol) symbolReference.getSymbol(), is(new UserSymbolMatcher<>(SymbolContext.VALUE, "foo",
                SymbolType.CONSTANT, ONE_HUNDRED)));

        symbolReference = symbolReferences.get(1);
        assertThat(symbolReference, is(notNullValue()));
        assertThat(symbolReference.getContexts(), contains((Object) SymbolContext.VALUE));
        assertThat(symbolReference.getName(), is("bar"));
        assertThat(symbolReference.getSymbol(), is(notNullValue()));
        assertThat(symbolReference.getSymbol(), is(instanceOf(UserSymbol.class)));
        assertThat((UserSymbol) symbolReference.getSymbol(), is(new UserSymbolMatcher<>(SymbolContext.VALUE, "bar",
                SymbolType.CONSTANT, TWENTY)));
    }

    /**
     * Asserts that {@link M68KArchitecture#isLocalName(String)} returns <code>true</code> when the specified symbol name represents
     * a local symbol or <code>false</code> when it doesn't.
     */
    @Test
    public void isLocalName() {
        assertThat(M68KArchitecture.isLocalName(""), is(false));
        assertThat(M68KArchitecture.isLocalName("A"), is(false));
        assertThat(M68KArchitecture.isLocalName("@"), is(true));
        assertThat(M68KArchitecture.isLocalName("@A"), is(true));
    }

    /**
     * Asserts that {@link M68KArchitecture#M68KArchitecture(InstructionSet, String[])} correctly initializes an
     * {@link M68KArchitecture}.
     */
    @Test
    public void m68kArchitecture() {
        assertThat(M68KArchitecture.MC68000.getInstructionSet(), is(sameInstance(InstructionSet.MC68000)));
        assertThat(M68KArchitecture.MC68000.getNames(), containsInAnyOrder(ImmutableList.<Matcher<? super String>> of(
                equalTo("68000"), equalTo("MC68000"), equalTo("68008"), equalTo("MC68008"))));
    }

}
