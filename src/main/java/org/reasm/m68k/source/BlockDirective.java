package org.reasm.m68k.source;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.reasm.m68k.assembly.internal.Mnemonics;
import org.reasm.source.SourceNode;

/**
 * Defines the directives recognized as block delimiters by the parser. These directives are recognized in <em>any</em> context.
 * Therefore, a macro whose name matches one of these directives cannot be invoked.
 *
 * @author Francis Gagné
 */
public enum BlockDirective {

    /** The <code>DEPHASE</code> directive. */
    DEPHASE(Mnemonics.DEPHASE),

    /** The <code>DO</code> directive. */
    DO(Mnemonics.DO),

    /** The <code>ELSE</code> directive. */
    ELSE(Mnemonics.ELSE),

    /** The <code>ELSEIF</code> directive. */
    ELSEIF(Mnemonics.ELSEIF),

    /** The <code>ENDC</code> directive. */
    ENDC(Mnemonics.ENDC),

    /** The <code>ENDIF</code> directive. */
    ENDIF(Mnemonics.ENDIF),

    /** The <code>ENDM</code> directive. */
    ENDM(Mnemonics.ENDM),

    /** The <code>ENDNS</code> directive. */
    ENDNS(Mnemonics.ENDNS),

    /** The <code>ENDR</code> directive. */
    ENDR(Mnemonics.ENDR),

    /** The <code>ENDTRANSFORM</code> directive. */
    ENDTRANSFORM(Mnemonics.ENDTRANSFORM),

    /** The <code>ENDW</code> directive. */
    ENDW(Mnemonics.ENDW),

    /** The <code>FOR</code> directive. */
    FOR(Mnemonics.FOR),

    /** The <code>IF</code> directive. */
    IF(Mnemonics.IF),

    /** The <code>MACRO</code> directive. */
    MACRO(Mnemonics.MACRO),

    /** The <code>NAMESPACE</code> directive. */
    NAMESPACE(Mnemonics.NAMESPACE),

    /** The <code>NEXT</code> directive. */
    NEXT(Mnemonics.NEXT),

    /** The <code>OBJ</code> directive. */
    OBJ(Mnemonics.OBJ),

    /** The <code>OBJEND</code> directive. */
    OBJEND(Mnemonics.OBJEND),

    /** The <code>PHASE</code> directive. */
    PHASE(Mnemonics.PHASE),

    /** The <code>REPT</code> directive. */
    REPT(Mnemonics.REPT),

    /** The <code>TRANSFORM</code> directive. */
    TRANSFORM(Mnemonics.TRANSFORM),

    /** The <code>UNTIL</code> directive. */
    UNTIL(Mnemonics.UNTIL),

    /** The <code>WHILE</code> directive. */
    WHILE(Mnemonics.WHILE);

    /** A map of mnemonics to the corresponding {@link BlockDirective}. The map uses a case-insensitive comparator for keys. */
    @Nonnull
    public static final Map<String, BlockDirective> MAP;

    static {
        final TreeMap<String, BlockDirective> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (BlockDirective blockDirective : values()) {
            map.put(blockDirective.mnemonic, blockDirective);
        }

        MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Gets the {@link BlockDirective} of a {@link SourceNode}, if it has one.
     *
     * @param sourceNode
     *            a {@link SourceNode}
     * @return {@link BlockDirectiveLine#getBlockDirective()} if the node is a {@link BlockDirectiveLine}, or <code>null</code>
     *         otherwise
     */
    public static BlockDirective getBlockDirective(SourceNode sourceNode) {
        if (sourceNode instanceof BlockDirectiveLine) {
            return ((BlockDirectiveLine) sourceNode).getBlockDirective();
        }

        return null;
    }

    @Nonnull
    private final String mnemonic;

    private BlockDirective(@Nonnull String mnemonic) {
        this.mnemonic = mnemonic;
    }

    /**
     * Gets the mnemonic of this block directive.
     *
     * @return the mnemonic
     */
    @Nonnull
    public final String getMnemonic() {
        return this.mnemonic;
    }

}
