package org.reasm.m68k.source;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Nonnull;

import org.reasm.commons.source.BlockDirective;
import org.reasm.m68k.assembly.internal.Mnemonics;

/**
 * Defines the directives recognized as block delimiters by the parser. These directives are recognized in <em>any</em> context.
 * Therefore, a macro whose name matches one of these directives cannot be invoked.
 *
 * @author Francis Gagné
 */
public final class M68KBlockDirectives {

    /** The <code>DEPHASE</code> directive. */
    @Nonnull
    public static final BlockDirective DEPHASE;

    /** The <code>DO</code> directive. */
    @Nonnull
    public static final BlockDirective DO;

    /** The <code>ELSE</code> directive. */
    @Nonnull
    public static final BlockDirective ELSE;

    /** The <code>ELSEIF</code> directive. */
    @Nonnull
    public static final BlockDirective ELSEIF;

    /** The <code>ENDC</code> directive. */
    @Nonnull
    public static final BlockDirective ENDC;

    /** The <code>ENDIF</code> directive. */
    @Nonnull
    public static final BlockDirective ENDIF;

    /** The <code>ENDM</code> directive. */
    @Nonnull
    public static final BlockDirective ENDM;

    /** The <code>ENDNS</code> directive. */
    @Nonnull
    public static final BlockDirective ENDNS;

    /** The <code>ENDR</code> directive. */
    @Nonnull
    public static final BlockDirective ENDR;

    /** The <code>ENDTRANSFORM</code> directive. */
    @Nonnull
    public static final BlockDirective ENDTRANSFORM;

    /** The <code>ENDW</code> directive. */
    @Nonnull
    public static final BlockDirective ENDW;

    /** The <code>FOR</code> directive. */
    @Nonnull
    public static final BlockDirective FOR;

    /** The <code>IF</code> directive. */
    @Nonnull
    public static final BlockDirective IF;

    /** The <code>MACRO</code> directive. */
    @Nonnull
    public static final BlockDirective MACRO;

    /** The <code>NAMESPACE</code> directive. */
    @Nonnull
    public static final BlockDirective NAMESPACE;

    /** The <code>NEXT</code> directive. */
    @Nonnull
    public static final BlockDirective NEXT;

    /** The <code>OBJ</code> directive. */
    @Nonnull
    public static final BlockDirective OBJ;

    /** The <code>OBJEND</code> directive. */
    @Nonnull
    public static final BlockDirective OBJEND;

    /** The <code>PHASE</code> directive. */
    @Nonnull
    public static final BlockDirective PHASE;

    /** The <code>REPT</code> directive. */
    @Nonnull
    public static final BlockDirective REPT;

    /** The <code>TRANSFORM</code> directive. */
    @Nonnull
    public static final BlockDirective TRANSFORM;

    /** The <code>UNTIL</code> directive. */
    @Nonnull
    public static final BlockDirective UNTIL;

    /** The <code>WHILE</code> directive. */
    @Nonnull
    public static final BlockDirective WHILE;

    /** A map of mnemonics to the corresponding {@link BlockDirective}. The map uses a case-insensitive comparator for keys. */
    @Nonnull
    public static final Map<String, BlockDirective> MAP;

    static {
        final Map<String, BlockDirective> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        DEPHASE = define(map, Mnemonics.DEPHASE);
        DO = define(map, Mnemonics.DO);
        ELSE = define(map, Mnemonics.ELSE);
        ELSEIF = define(map, Mnemonics.ELSEIF);
        ENDC = define(map, Mnemonics.ENDC);
        ENDIF = define(map, Mnemonics.ENDIF);
        ENDM = define(map, Mnemonics.ENDM);
        ENDNS = define(map, Mnemonics.ENDNS);
        ENDR = define(map, Mnemonics.ENDR);
        ENDTRANSFORM = define(map, Mnemonics.ENDTRANSFORM);
        ENDW = define(map, Mnemonics.ENDW);
        FOR = define(map, Mnemonics.FOR);
        IF = define(map, Mnemonics.IF);
        MACRO = define(map, Mnemonics.MACRO);
        NAMESPACE = define(map, Mnemonics.NAMESPACE);
        NEXT = define(map, Mnemonics.NEXT);
        OBJ = define(map, Mnemonics.OBJ);
        OBJEND = define(map, Mnemonics.OBJEND);
        PHASE = define(map, Mnemonics.PHASE);
        REPT = define(map, Mnemonics.REPT);
        TRANSFORM = define(map, Mnemonics.TRANSFORM);
        UNTIL = define(map, Mnemonics.UNTIL);
        WHILE = define(map, Mnemonics.WHILE);

        MAP = Collections.unmodifiableMap(map);
    }

    @Nonnull
    private static BlockDirective define(@Nonnull Map<String, BlockDirective> map, @Nonnull String mnemonic) {
        final BlockDirective blockDirective = new BlockDirective(mnemonic);
        map.put(blockDirective.getMnemonic(), blockDirective);
        return blockDirective;
    }

    // This class is not meant to be instantiated.
    private M68KBlockDirectives() {
    }

}
