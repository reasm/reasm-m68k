package org.reasm.m68k.source;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import org.reasm.commons.source.BasicBlockParser;
import org.reasm.commons.source.BlockParser;
import org.reasm.source.ParseError;
import org.reasm.source.SourceNode;

final class BlockParsers {

    @Nonnull
    static final Set<Class<? extends SourceNode>> DO_BLOCK_TYPES = singleType(DoBlock.class);
    @Nonnull
    static final Set<Class<? extends SourceNode>> FOR_BLOCK_TYPES = singleType(ForBlock.class);
    @Nonnull
    static final Set<Class<? extends SourceNode>> IF_BLOCK_TYPES = singleType(IfBlock.class);
    @Nonnull
    static final Set<Class<? extends SourceNode>> MACRO_BLOCK_TYPES = singleType(MacroBlock.class);
    @Nonnull
    static final Set<Class<? extends SourceNode>> NAMESPACE_BLOCK_TYPES = singleType(NamespaceBlock.class);
    @Nonnull
    static final Set<Class<? extends SourceNode>> OBJ_BLOCK_TYPES = singleType(ObjBlock.class);
    @Nonnull
    static final Set<Class<? extends SourceNode>> REPT_BLOCK_TYPES = singleType(ReptBlock.class);
    @Nonnull
    static final Set<Class<? extends SourceNode>> TRANSFORM_BLOCK_TYPES = singleType(TransformBlock.class);
    @Nonnull
    static final Set<Class<? extends SourceNode>> WHILE_BLOCK_TYPES = singleType(WhileBlock.class);

    @Nonnull
    static final BlockParser DO = new BasicBlockParser(M68KBlockDirectives.UNTIL) {
        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return DO_BLOCK_TYPES;
        }

        @Override
        protected SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new DoBlock(childNodes, parseError);
        };
    };

    @Nonnull
    static final BlockParser FOR = new BasicBlockParser(M68KBlockDirectives.NEXT) {
        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return FOR_BLOCK_TYPES;
        }

        @Override
        protected SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new ForBlock(childNodes, parseError);
        };
    };

    @Nonnull
    static final BlockParser IF = M68KIfBlockParser.INSTANCE;

    @Nonnull
    static final BlockParser MACRO = new BasicBlockParser(M68KBlockDirectives.ENDM) {
        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return MACRO_BLOCK_TYPES;
        }

        @Override
        protected SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new MacroBlock(childNodes, parseError);
        };

        @Override
        protected SourceNode createBodyBlock(Iterable<? extends SourceNode> childNodes) {
            return new MacroBody(childNodes);
        }
    };

    @Nonnull
    static final BlockParser NAMESPACE = new BasicBlockParser(M68KBlockDirectives.ENDNS) {
        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return NAMESPACE_BLOCK_TYPES;
        }

        @Override
        protected SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new NamespaceBlock(childNodes, parseError);
        }
    };

    @Nonnull
    static final BlockParser OBJ = new ObjBlockParser(M68KBlockDirectives.OBJEND);

    @Nonnull
    static final BlockParser PHASE = new ObjBlockParser(M68KBlockDirectives.DEPHASE);

    @Nonnull
    static final BlockParser REPT = new BasicBlockParser(M68KBlockDirectives.ENDR) {
        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return REPT_BLOCK_TYPES;
        }

        @Override
        protected SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new ReptBlock(childNodes, parseError);
        }

        @Override
        protected SourceNode createBodyBlock(Iterable<? extends SourceNode> childNodes) {
            return new ReptBody(childNodes);
        }
    };

    @Nonnull
    static final BlockParser TRANSFORM = new BasicBlockParser(M68KBlockDirectives.ENDTRANSFORM) {
        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return TRANSFORM_BLOCK_TYPES;
        }

        @Override
        protected SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new TransformBlock(childNodes, parseError);
        }
    };

    @Nonnull
    static final BlockParser WHILE = new BasicBlockParser(M68KBlockDirectives.ENDW) {
        @Override
        public Iterable<Class<? extends SourceNode>> getOutputNodeTypes() {
            return WHILE_BLOCK_TYPES;
        }

        @Override
        protected SourceNode createBlock(Iterable<? extends SourceNode> childNodes, ParseError parseError) {
            return new WhileBlock(childNodes, parseError);
        }
    };

    @Nonnull
    private static Set<Class<? extends SourceNode>> singleType(@Nonnull Class<? extends SourceNode> type) {
        return Collections.<Class<? extends SourceNode>> singleton(type);
    }

    // This class is not meant to be instantiated.
    private BlockParsers() {
    }

}
