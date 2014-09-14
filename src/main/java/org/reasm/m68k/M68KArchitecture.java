package org.reasm.m68k;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.*;
import org.reasm.expressions.EvaluationContext;
import org.reasm.expressions.SymbolLookup;
import org.reasm.m68k.source.Parser;
import org.reasm.source.AbstractSourceFile;
import org.reasm.source.SourceFile;
import org.reasm.source.SourceNode;

import ca.fragag.Consumer;
import ca.fragag.text.Document;

/**
 * One of the Motorola M68000 (M68K) family architectures.
 * <p>
 * Options can be specified by creating a {@link ConfigurationOptions} object and passing it to
 * {@link Configuration#Configuration(Environment, SourceFile, Architecture, FileFetcher, PredefinedSymbolTable, Map)}.
 *
 * @author Francis Gagné
 */
@Immutable
public final class M68KArchitecture extends Architecture {

    static final ArrayList<Architecture> ALL_ARCHITECTURES = new ArrayList<>();

    /** The MC68000 architecture. */
    public static final M68KArchitecture MC68000 = new M68KArchitecture(InstructionSet.MC68000, "68000", "MC68000", "68008",
            "MC68008");
    /** The MC68EC000 architecture. */
    public static final M68KArchitecture MC68EC000 = new M68KArchitecture(InstructionSet.MC68EC000, "68EC000", "MC68EC000");
    /** The MC68010 architecture. */
    public static final M68KArchitecture MC68010 = new M68KArchitecture(InstructionSet.MC68010, "68010", "MC68010");
    /** The CPU32 architecture. */
    public static final M68KArchitecture CPU32 = new M68KArchitecture(InstructionSet.CPU32, "CPU32");
    /** The MC68020 architecture. */
    public static final M68KArchitecture MC68020 = new M68KArchitecture(InstructionSet.MC68020, "68020", "MC68020", "68EC020",
            "MC68EC020");
    /** The MC68030 architecture. */
    public static final M68KArchitecture MC68030 = new M68KArchitecture(InstructionSet.MC68030, "68030", "MC68030");
    /** The MC68EC030 architecture. */
    public static final M68KArchitecture MC68EC030 = new M68KArchitecture(InstructionSet.MC68EC030, "68EC030", "MC68EC030");
    /** The MC68040 architecture. */
    public static final M68KArchitecture MC68040 = new M68KArchitecture(InstructionSet.MC68040, "68040", "MC68040", "68LC040",
            "MC68LC040");
    /** The MC68EC040 architecture. */
    public static final M68KArchitecture MC68EC040 = new M68KArchitecture(InstructionSet.MC68EC040, "68EC040", "MC68EC040");

    // TODO: FPU architectures

    /**
     * Determines whether a symbol name represents a local name.
     *
     * @param symbolName
     *            the symbol name to test
     * @return <code>true</code> if the symbol name starts with <code>@</code>; otherwise, <code>false</code>
     */
    public static boolean isLocalName(@Nonnull String symbolName) {
        return symbolName.length() > 0 && symbolName.charAt(0) == '@';
    }

    @Nonnull
    private final InstructionSet instructionSet;

    /**
     * Initializes a new M68KArchitecture.
     *
     * @param instructionSet
     *            the instruction set used by this architecture
     * @param names
     *            the names of this architecture
     */
    private M68KArchitecture(@Nonnull InstructionSet instructionSet, @Nonnull String... names) {
        super(Arrays.asList(names));
        this.instructionSet = instructionSet;
        ALL_ARCHITECTURES.add(this);
    }

    @CheckForNull
    @Override
    public final Value evaluateExpression(@Nonnull CharSequence expression, @Nonnull final Assembly assembly,
            @CheckForNull final Consumer<SymbolReference> symbolReferenceConsumer,
            @CheckForNull Consumer<AssemblyMessage> assemblyMessageConsumer) {
        if (expression == null) {
            throw new NullPointerException("expression");
        }

        if (assembly == null) {
            throw new NullPointerException("assembly");
        }

        final SymbolLookup symbolLookup = new SymbolLookup() {
            @Override
            public Symbol getSymbol(String name) {
                final SymbolReference symbolReference = assembly.resolveSymbolReference(SymbolContext.VALUE, name,
                        M68KArchitecture.isLocalName(name), false, null, null);

                if (symbolReferenceConsumer != null) {
                    symbolReferenceConsumer.accept(symbolReference);
                }

                return symbolReference.getSymbol();
            }
        };

        final EvaluationContext evaluationContext = new EvaluationContext(assembly, assembly.getProgramCounter(), symbolLookup,
                assemblyMessageConsumer);
        return Expressions.parse(expression, assemblyMessageConsumer).evaluate(evaluationContext);
    }

    /**
     * Gets the instruction set used by this architecture.
     *
     * @return the instruction set
     */
    @Nonnull
    public final InstructionSet getInstructionSet() {
        return this.instructionSet;
    }

    @Nonnull
    @Override
    public final SourceNode parse(@Nonnull Document text) {
        return Parser.parse(text);
    }

    @Nonnull
    @Override
    public final SourceNode reparse(@Nonnull Document text, @Nonnull AbstractSourceFile<?> oldSourceFile, int replaceOffset,
            int lengthToRemove, int lengthToInsert) {
        return Parser.reparse(text, oldSourceFile, replaceOffset, lengthToRemove, lengthToInsert);
    }

}
