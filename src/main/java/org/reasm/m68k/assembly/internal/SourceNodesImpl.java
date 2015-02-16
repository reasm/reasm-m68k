package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.reasm.AssemblyBuilder;
import org.reasm.AssemblyStepLocation;
import org.reasm.BlockEvents;
import org.reasm.Symbol;
import org.reasm.commons.source.SourceLocationUtils;
import org.reasm.messages.UnknownMnemonicErrorMessage;
import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNode;

/**
 * Provides the implementation for assembling the various {@link SourceNode} subclasses defined in the <code>org.reasm.source</code>
 * package.
 *
 * @author Francis Gagné
 */
public final class SourceNodesImpl {

    /**
     * Assembles a directive that delimits a block.
     *
     * @param builder
     *            an assembly builder
     * @throws IOException
     *             an I/O exception occurred while assembling the directive
     */
    public static void assembleBlockDirectiveLine(@Nonnull AssemblyBuilder builder) throws IOException {
        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        context.setMnemonic();

        String mnemonicName = context.mnemonic;
        if (mnemonicName.startsWith("!")) {
            mnemonicName = mnemonicName.substring(1);
        }

        assembleMnemonic(context, Mnemonics.MAP.get(mnemonicName), true);
    }

    /**
     * Assembles a <code>DO</code> block.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleDoBlock(@Nonnull AssemblyBuilder builder) {
        final AssemblyStepLocation stepLocation = builder.getStep().getLocation();

        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        final DoBlockState doBlockState = new DoBlockState();
        final Iterable<SourceLocation> sourceLocations = new Iterable<SourceLocation>() {
            @Override
            public Iterator<SourceLocation> iterator() {
                final DynamicSourceLocationIterator iterator = new DynamicSourceLocationIterator(stepLocation.getSourceLocation()
                        .getChildSourceLocations().iterator());
                context.blockStateMap.put(stepLocation, doBlockState);
                return iterator;
            }
        };

        builder.enterBlock(sourceLocations, doBlockState, false, null);
    }

    /**
     * Assembles a <code>FOR</code> block.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleForBlock(@Nonnull AssemblyBuilder builder) {
        final AssemblyStepLocation stepLocation = builder.getStep().getLocation();

        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        final ForBlockState forBlockState = new ForBlockState();
        final Iterable<SourceLocation> sourceLocations = new Iterable<SourceLocation>() {
            @Override
            public Iterator<SourceLocation> iterator() {
                final DynamicSourceLocationIterator iterator = new DynamicSourceLocationIterator(stepLocation.getSourceLocation()
                        .getChildSourceLocations().iterator());
                forBlockState.iterator = iterator;
                context.blockStateMap.put(stepLocation, forBlockState);
                return iterator;
            }
        };

        builder.enterBlock(sourceLocations, forBlockState, false, null);
    }

    /**
     * Assembles an <code>IF</code> block.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleIfBlock(@Nonnull AssemblyBuilder builder) {
        final AssemblyStepLocation stepLocation = builder.getStep().getLocation();

        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        final Iterable<SourceLocation> sourceLocations = new Iterable<SourceLocation>() {
            @Override
            public Iterator<SourceLocation> iterator() {
                final DynamicSourceLocationIterator iterator = new DynamicSourceLocationIterator(stepLocation.getSourceLocation()
                        .getChildSourceLocations().iterator());
                context.blockStateMap.put(stepLocation, new IfBlockState(iterator));
                return iterator;
            }
        };

        builder.enterBlock(sourceLocations, null, false, null);
    }

    /**
     * Assembles a logical line.
     *
     * @param builder
     *            an assembly builder
     * @throws IOException
     *             an I/O exception occurred while assembling the logical line
     */
    public static void assembleLogicalLine(@Nonnull AssemblyBuilder builder) throws IOException {
        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        if (SourceLocationUtils.hasMnemonic(builder.getStep().getLocation().getSourceLocation())) {
            context.setMnemonic();

            final Symbol mnemonicSymbol;
            final boolean builtInMnemonic;

            // If the mnemonic starts with !, ignore macros and search only the built-in mnemonics.
            if (context.mnemonic.startsWith("!")) {
                mnemonicSymbol = Mnemonics.MAP.get(context.mnemonic.substring(1));
                builtInMnemonic = true;
            } else {
                mnemonicSymbol = context.getMnemonicSymbolByName(context.mnemonic);
                builtInMnemonic = false;
            }

            assembleMnemonic(context, mnemonicSymbol, builtInMnemonic);
        } else {
            context.defineLabels();
        }
    }

    /**
     * Assembles a <code>MACRO</code> block.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleMacroBlock(@Nonnull AssemblyBuilder builder) {
        final AssemblyStepLocation stepLocation = builder.getStep().getLocation();

        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        final Iterable<SourceLocation> sourceLocations = new Iterable<SourceLocation>() {
            @Override
            public Iterator<SourceLocation> iterator() {
                final DynamicSourceLocationIterator iterator = new DynamicSourceLocationIterator(stepLocation.getSourceLocation()
                        .getChildSourceLocations().iterator());
                context.blockStateMap.put(stepLocation, new MacroBlockState(iterator));
                return iterator;
            }
        };

        builder.enterBlock(sourceLocations, null, true, null);
    }

    /**
     * Assembles a <code>NAMESPACE</code> block.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleNamespaceBlock(@Nonnull final AssemblyBuilder builder) {
        builder.enterComposite(true, new ScopedEffectBlockEvents() {
            @Override
            void cancelEffect() {
                builder.exitNamespace();
            }
        });
    }

    /**
     * Assembles an <code>OBJ</code> or <code>PHASE</code> block.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleObjBlock(@Nonnull AssemblyBuilder builder) {
        final AssemblyStepLocation stepLocation = builder.getStep().getLocation();

        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        final ObjBlockState objBlockState = new ObjBlockState();
        context.blockStateMap.put(stepLocation, objBlockState);

        builder.enterBlock(stepLocation.getSourceLocation().getChildSourceLocations(), null, true, new BlockEvents() {
            @Override
            public void exitBlock() {
                if (objBlockState.programCounterOffset != 0) {
                    context.builder.setProgramCounter(context.builder.getAssembly().getProgramCounter()
                            + objBlockState.programCounterOffset);
                }
            }
        });
    }

    /**
     * Assembles a <code>REPT</code> block.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleReptBlock(@Nonnull AssemblyBuilder builder) {
        final AssemblyStepLocation stepLocation = builder.getStep().getLocation();

        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        context.blockStateMap.put(stepLocation, new ReptBlockState());

        builder.enterBlock(stepLocation.getSourceLocation().getChildSourceLocations(), null, true, null);
    }

    /**
     * Assembles a <code>REPT</code> block body.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleReptBody(@Nonnull AssemblyBuilder builder) {
        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        final Object blockState = context.getParentBlock();
        if (!(blockState instanceof ReptBlockState)) {
            throw new AssertionError();
        }

        builder.enterBlock(builder.getStep().getLocation().getSourceLocation().getChildSourceLocations(),
                (ReptBlockState) blockState, false, null);
    }

    /**
     * Assembles a <code>TRANSFORM</code> block.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleTransformBlock(@Nonnull final AssemblyBuilder builder) {
        builder.enterComposite(true, new ScopedEffectBlockEvents() {
            @Override
            void cancelEffect() throws IOException {
                builder.exitTransformationBlock();
            }
        });
    }

    /**
     * Assembles a <code>WHILE</code> block.
     *
     * @param builder
     *            an assembly builder
     */
    public static void assembleWhileBlock(@Nonnull AssemblyBuilder builder) {
        final AssemblyStepLocation stepLocation = builder.getStep().getLocation();

        // Get our assembly context for this assembly.
        final M68KAssemblyContext context = M68KAssemblyContext.getAssemblyContext(builder);

        final WhileBlockState whileBlockState = new WhileBlockState();
        final Iterable<SourceLocation> sourceLocations = new Iterable<SourceLocation>() {
            @Override
            public Iterator<SourceLocation> iterator() {
                final DynamicSourceLocationIterator iterator = new DynamicSourceLocationIterator(stepLocation.getSourceLocation()
                        .getChildSourceLocations().iterator());
                whileBlockState.iterator = iterator;
                context.blockStateMap.put(stepLocation, whileBlockState);
                return iterator;
            }
        };

        builder.enterBlock(sourceLocations, whileBlockState, false, null);
    }

    private static void assembleMnemonic(@Nonnull M68KAssemblyContext context, @CheckForNull Symbol mnemonicSymbol,
            boolean builtInMnemonic) throws IOException {
        final Mnemonic mnemonic;
        if (mnemonicSymbol != null && mnemonicSymbol.getValue() != null) {
            mnemonic = (Mnemonic) mnemonicSymbol.getValue();
        } else {
            mnemonic = null;
        }

        if (mnemonic != null) {
            mnemonic.defineLabels(context);
            mnemonic.checkInstructionSet(context);
            mnemonic.assemble(context);
        } else {
            if (builtInMnemonic) {
                context.addMessage(new UnknownMnemonicErrorMessage());
            }

            context.defineLabels();
        }
    }

    // This class is not meant to be instantiated.
    private SourceNodesImpl() {
    }

}
