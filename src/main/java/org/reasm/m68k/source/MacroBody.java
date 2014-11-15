package org.reasm.m68k.source;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.reasm.AssemblyBuilder;
import org.reasm.source.CompositeSourceNode;
import org.reasm.source.SourceNode;

/**
 * The body of a <code>MACRO</code> block.
 *
 * @author Francis Gagné
 */
@Immutable
public final class MacroBody extends CompositeSourceNode {

    /**
     * Initializes a new MacroBody.
     *
     * @param childNodes
     *            the child nodes
     */
    public MacroBody(@Nonnull Iterable<? extends SourceNode> childNodes) {
        super(childNodes, null);
    }

    @Override
    protected void assembleCore(AssemblyBuilder builder) {
        builder.enterComposite(false, null);
    }

}
