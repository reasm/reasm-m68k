package org.reasm.m68k.assembly.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reasm.*;
import org.reasm.expressions.EvaluationContext;
import org.reasm.expressions.SymbolLookup;
import org.reasm.m68k.ConfigurationOptions;
import org.reasm.m68k.M68KArchitecture;
import org.reasm.m68k.expressions.internal.Tokenizer;
import org.reasm.m68k.messages.InvalidSizeAttributeErrorMessage;
import org.reasm.m68k.messages.SizeAttributeNotAllowedErrorMessage;
import org.reasm.m68k.source.LogicalLine;
import org.reasm.m68k.source.SourceLocationUtils;
import org.reasm.messages.WrongNumberOfOperandsErrorMessage;
import org.reasm.source.SourceLocation;
import org.reasm.source.SourceNode;

import ca.fragag.Consumer;

import com.google.common.collect.ImmutableList;

/**
 * Stores all the contextual data for the M68000 family for a specific assembly.
 *
 * @author Francis Gagné
 */
final class M68KAssemblyContext extends M68KBasicAssemblyContext implements Consumer<AssemblyMessage>, CustomAssemblyData,
        SymbolLookup, SymbolResolutionFallback {

    static final Object KEY = new Object();

    /** The symbol context for register aliases. Register aliases can be used in place of a standard register name. */
    static final SymbolContext<GeneralPurposeRegister> REGISTER_ALIAS = new SymbolContext<>(GeneralPurposeRegister.class);

    /**
     * The symbol context for register list aliases. Register list aliases can be used where a register list is expected (e.g. in
     * the <code>MOVEM</code> instruction).
     */
    static final SymbolContext<RegisterList> REGISTER_LIST_ALIAS = new SymbolContext<>(RegisterList.class);

    private static final ImmutableList<SymbolContext<?>> REGISTER_ALIAS_LOOKUP_CONTEXTS = ImmutableList.of(REGISTER_ALIAS,
            SymbolContext.VALUE);

    private static final ImmutableList<SymbolContext<?>> REGISTER_LIST_ALIAS_LOOKUP_CONTEXTS = ImmutableList.of(
            REGISTER_LIST_ALIAS, REGISTER_ALIAS, SymbolContext.VALUE);

    /** The symbol context for mnemonics. */
    static final SymbolContext<Mnemonic> MNEMONIC = new SymbolContext<>(Mnemonic.class);

    static M68KAssemblyContext getAssemblyContext(AssemblyBuilder builder) {
        M68KAssemblyContext context = (M68KAssemblyContext) builder.getCustomAssemblyData(KEY);
        if (context == null) {
            // If it doesn't exist yet, create it.
            context = new M68KAssemblyContext(builder);

            // Initialize it with the configuration options.
            final ConfigurationOptions configurationOptions = (ConfigurationOptions) builder.getAssembly().getConfiguration()
                    .getCustomConfigurationOptions(ConfigurationOptions.KEY);
            if (configurationOptions != null) {
                context.automaticEven = configurationOptions.automaticEven();
                context.optimizeCmpiToTst = configurationOptions.optimizeCmpiToTst();
                context.optimizeMoveToMoveq = configurationOptions.optimizeMoveToMoveq();
                context.optimizeToAddqSubq = configurationOptions.optimizeToAddqSubq();
                context.optimizeUnsizedAbsoluteAddressingToPcRelative = configurationOptions
                        .optimizeUnsizedAbsoluteAddressingToPcRelative();
                context.optimizeUnsizedBranches = configurationOptions.optimizeUnsizedBranches();
                context.optimizeZeroDisplacement = configurationOptions.optimizeZeroDisplacement();
            }

            builder.setCustomAssemblyData(KEY, context);
        }

        // Initialize the context.
        context.initialize(builder.getStep());
        return context;
    }

    final AssemblyBuilder builder;

    // Configuration options that can be changed during assembly
    boolean automaticEven;
    boolean optimizeCmpiToTst;
    boolean optimizeMoveToMoveq;
    boolean optimizeToAddqSubq;
    boolean optimizeUnsizedBranches;

    // Context of the current logical line being assembled
    // They are assigned in initialize(AssemblyStep)
    AssemblyStep step;
    SourceLocation sourceLocation;
    LogicalLine logicalLine;
    int numberOfLabels;
    int numberOfOperands;
    String mnemonic;
    String attribute;
    private EvaluationContext evaluationContext;

    // Reusable objects
    final LogicalLineReader logicalLineReader = new LogicalLineReader();
    final Tokenizer tokenizer = new Tokenizer();
    final EffectiveAddress ea0 = new EffectiveAddress();
    final EffectiveAddress ea1 = new EffectiveAddress();
    final BranchLabelValueVisitor branchLabelValueVisitor = new BranchLabelValueVisitor(this);
    final CardinalValueVisitor cardinalValueVisitor = new CardinalValueVisitor(this);
    final DcFloatValueVisitor dcFloatValueVisitor = new DcFloatValueVisitor(this);
    final DcIntegerValueVisitor dcIntegerValueVisitor = new DcIntegerValueVisitor(this);
    final IntegerValueVisitor integerValueVisitor = new IntegerValueVisitor(this);
    final StringValueVisitor stringValueVisitor = new StringValueVisitor(this);

    // Persistent state
    final Map<AssemblyStepLocation, Object> blockStateMap = new HashMap<>();
    final Map<AssemblyStepLocation, MacroInvocation> macroInvocationsByLocation = new HashMap<>();
    // - Special symbols
    final RsSymbol rs = new RsSymbol();

    M68KAssemblyContext(AssemblyBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void accept(AssemblyMessage message) {
        this.builder.addTentativeMessage(message);
    }

    @Override
    public void completed() {
    }

    @Override
    public Symbol getSymbol(String name) {
        return this.getSymbolByContextAndName(SymbolContext.VALUE, name, this);
    }

    @Override
    public Symbol resolve(SymbolReference symbolReference) {
        // TODO: built-in symbols (functions, etc.)
        return null;
    }

    @Override
    public void startedNewPass() {
        this.rs.set(0, false);
    }

    void addInvalidSizeAttributeErrorMessage() {
        this.addMessage(new InvalidSizeAttributeErrorMessage(this.attribute));
    }

    void addMessage(AssemblyMessage message) {
        this.builder.addMessage(message);
    }

    void addTentativeMessage(AssemblyMessage message) {
        this.builder.addTentativeMessage(message);
    }

    void addWrongNumberOfOperandsErrorMessage() {
        this.addMessage(new WrongNumberOfOperandsErrorMessage());
    }

    void appendByte(byte by) throws IOException {
        this.builder.appendAssembledData(by);
    }

    void appendEffectiveAddress(EffectiveAddress ea) throws IOException {
        this.appendEffectiveAddress(ea, 0);
    }

    void appendEffectiveAddress(EffectiveAddress ea, int firstWord) throws IOException {
        for (int i = firstWord; i < ea.numberOfWords; i++) {
            this.appendWord(ea.getWord(i));
        }
    }

    void appendLong(int longWord) throws IOException {
        this.builder.appendAssembledData((byte) (longWord >>> 24));
        this.builder.appendAssembledData((byte) (longWord >>> 16));
        this.builder.appendAssembledData((byte) (longWord >>> 8));
        this.builder.appendAssembledData((byte) (longWord >>> 0));
    }

    void appendQuad(long quadWord) throws IOException {
        this.builder.appendAssembledData((byte) (quadWord >>> 56));
        this.builder.appendAssembledData((byte) (quadWord >>> 48));
        this.builder.appendAssembledData((byte) (quadWord >>> 40));
        this.builder.appendAssembledData((byte) (quadWord >>> 32));
        this.builder.appendAssembledData((byte) (quadWord >>> 24));
        this.builder.appendAssembledData((byte) (quadWord >>> 16));
        this.builder.appendAssembledData((byte) (quadWord >>> 8));
        this.builder.appendAssembledData((byte) (quadWord >>> 0));
    }

    void appendWord(short word) throws IOException {
        this.builder.appendAssembledData((byte) (word >>> 8));
        this.builder.appendAssembledData((byte) word);
    }

    void automaticEven() throws IOException {
        if (this.automaticEven && (this.programCounter & 1) != 0) {
            this.builder.appendAssembledData((byte) 0);
            this.programCounter++;
        }
    }

    /**
     * Defines all the labels on the logical line of the current assembly step except the last one with the current program counter
     * as their value.
     */
    void defineExtraLabels() {
        final int numberOfLabels = this.numberOfLabels;
        for (int i = 0; i < numberOfLabels - 1; i++) {
            this.defineLabel(i);
        }
    }

    /**
     * Defines all the labels on the logical line of the current assembly step with the current program counter as their value.
     */
    void defineLabels() {
        final int numberOfLabels = this.numberOfLabels;
        for (int i = 0; i < numberOfLabels; i++) {
            this.defineLabel(i);
        }
    }

    <TValue> void defineSymbol(SymbolContext<TValue> symbolContext, String symbolName, SymbolType symbolType, TValue value) {
        final boolean isLocalName = M68KArchitecture.isLocalName(symbolName);
        this.builder.defineSymbol(symbolContext, symbolName, isLocalName, symbolType, value);
    }

    <TValue> void defineSymbols(SymbolContext<TValue> symbolContext, SymbolType symbolType, TValue value) {
        for (int i = 0; i < this.numberOfLabels; i++) {
            this.defineSymbol(symbolContext, this.getLabelText(i), symbolType, value);
        }
    }

    DcValueVisitor getDcValueVisitor(InstructionSize size) {
        switch (size) {
        case DEFAULT:
        case BYTE:
        case WORD:
        case LONG:
        case QUAD:
        default:
            return this.dcIntegerValueVisitor;

        case SINGLE:
        case DOUBLE:
        case EXTENDED:
        case PACKED:
            return this.dcFloatValueVisitor;
        }
    }

    void getEffectiveAddress(String operand, Set<AddressingMode> validAddressingModes, InstructionSize size, EffectiveAddress ea) {
        this.getEffectiveAddress(operand, validAddressingModes, size, 2, ea);
    }

    void getEffectiveAddress(String operand, Set<AddressingMode> validAddressingModes, InstructionSize size,
            int offsetToExtensionWords, EffectiveAddress ea) {
        this.tokenizer.setCharSequence(operand);
        EffectiveAddress.getEffectiveAddress(this.tokenizer, validAddressingModes, false, size, offsetToExtensionWords,
                this.getEvaluationContext(), this, this, ea);
    }

    EvaluationContext getEvaluationContext() {
        if (this.evaluationContext == null) {
            this.evaluationContext = new EvaluationContext(this.builder.getAssembly(), this.programCounter, this, this);
        }

        return this.evaluationContext;
    }

    String getLabelText(int index) {
        this.logicalLineReader.setRange(this.sourceLocation, this.logicalLine, this.logicalLine.getLabelBounds(index));
        return this.logicalLineReader.readToString();
    }

    Symbol getMnemonicSymbolByName(String name) {
        return this.getSymbolByContextAndName(MNEMONIC, name, Mnemonics.SYMBOL_RESOLUTION_FALLBACK);
    }

    String getMnemonicText() {
        final SubstringBounds mnemonicBounds = this.logicalLine.getMnemonicBounds();
        assert mnemonicBounds != null;
        this.logicalLineReader.setRange(this.sourceLocation, this.logicalLine, mnemonicBounds);
        return this.logicalLineReader.readToString();
    }

    String getOperandText(int index) {
        this.prepareOperandReader(index);
        return this.logicalLineReader.readToString();
    }

    Object getParentBlock() {
        return this.blockStateMap.get(this.step.getLocation().getParent());
    }

    SourceNode getParentNode() {
        final AssemblyStepLocation parent = this.step.getLocation().getParent();
        assert parent != null;
        return parent.getSourceLocation().getSourceNode();
    }

    @Override
    GeneralPurposeRegister getRegisterAliasByName(String name) {
        final Symbol symbol = this.builder.resolveSymbolReference(REGISTER_ALIAS_LOOKUP_CONTEXTS, name,
                M68KArchitecture.isLocalName(name), false, null, null).getSymbol();

        if (symbol != null && symbol.getValue() instanceof GeneralPurposeRegister) {
            return (GeneralPurposeRegister) symbol.getValue();
        }

        return null;
    }

    Symbol getRegisterAliasOrRegisterListAliasSymbolByName(String name) {
        final Symbol symbol = this.builder.resolveSymbolReference(REGISTER_LIST_ALIAS_LOOKUP_CONTEXTS, name,
                M68KArchitecture.isLocalName(name), false, null, null).getSymbol();

        if (symbol != null && ((UserSymbol) symbol).getContext() != SymbolContext.VALUE) {
            return symbol;
        }

        return null;
    }

    Symbol getRegisterListAliasSymbolByName(String name) {
        return this.getSymbolByContextAndName(REGISTER_LIST_ALIAS, name, null);
    }

    InstructionSize parseInstructionSize() {
        if (this.attribute == null) {
            return InstructionSize.DEFAULT;
        }

        if (this.attribute.length() != 1) {
            return InstructionSize.INVALID;
        }

        switch (this.attribute.charAt(0)) {
        case 'B':
        case 'b':
            return InstructionSize.BYTE;

        case 'W':
        case 'w':
            return InstructionSize.WORD;

        case 'L':
        case 'l':
            return InstructionSize.LONG;

        case 'Q':
        case 'q':
            return InstructionSize.QUAD;

        case 'S':
        case 's':
            return InstructionSize.SINGLE;

        case 'D':
        case 'd':
            return InstructionSize.DOUBLE;

        case 'X':
        case 'x':
            return InstructionSize.EXTENDED;

        case 'P':
        case 'p':
            return InstructionSize.PACKED;

        default:
            return InstructionSize.INVALID;
        }
    }

    InstructionSize parseIntegerInstructionSize() {
        if (this.attribute == null) {
            return InstructionSize.DEFAULT;
        }

        if (this.attribute.length() != 1) {
            return InstructionSize.INVALID;
        }

        switch (this.attribute.charAt(0)) {
        case 'B':
        case 'b':
            return InstructionSize.BYTE;

        case 'W':
        case 'w':
            return InstructionSize.WORD;

        case 'L':
        case 'l':
            return InstructionSize.LONG;

        default:
            return InstructionSize.INVALID;
        }
    }

    void prepareOperandReader(int index) {
        this.logicalLineReader.setRange(this.sourceLocation, this.logicalLine, this.logicalLine.getOperandBounds(index));
    }

    boolean requireNumberOfOperands(int requiredNumberOfOperands) {
        if (this.numberOfOperands != requiredNumberOfOperands) {
            this.addWrongNumberOfOperandsErrorMessage();
        }

        return this.numberOfOperands >= requiredNumberOfOperands;
    }

    void setMnemonic() {
        final String mnemonic = this.getMnemonicText();

        final int indexOfPeriod = mnemonic.indexOf('.');
        if (indexOfPeriod == -1) {
            this.mnemonic = mnemonic;
            this.attribute = null;
        } else {
            this.mnemonic = mnemonic.substring(0, indexOfPeriod);
            this.attribute = mnemonic.substring(indexOfPeriod + 1);
        }
    }

    void sizeNotAllowed() {
        if (this.attribute != null) {
            this.addMessage(new SizeAttributeNotAllowedErrorMessage());
        }
    }

    void validateForByteAccess(EffectiveAddress ea) {
        if (ea.isAddressRegisterDirect()) {
            this.addInvalidSizeAttributeErrorMessage();
        }
    }

    /**
     * Defines a label on the logical line of the current assembly step with the current program counter as its value.
     *
     * @param index
     *            the index of the label to define
     */
    private void defineLabel(int index) {
        final String label = this.getLabelText(index);
        this.defineSymbol(SymbolContext.VALUE, label, SymbolType.CONSTANT, new UnsignedIntValue(this.programCounter));
    }

    private <TValue> Symbol getSymbolByContextAndName(SymbolContext<TValue> context, String name,
            SymbolResolutionFallback symbolResolutionFallback) {
        return this.builder.resolveSymbolReference(context, name, M68KArchitecture.isLocalName(name), false, null,
                symbolResolutionFallback).getSymbol();
    }

    private void initialize(AssemblyStep step) {
        this.step = step;
        this.programCounter = step.getProgramCounter();
        this.sourceLocation = step.getLocation().getSourceLocation();
        this.instructionSet = ((M68KArchitecture) this.sourceLocation.getArchitecture()).getInstructionSet();

        final LogicalLine logicalLine = SourceLocationUtils.getLogicalLine(this.sourceLocation);
        if (logicalLine != null) {
            this.logicalLine = logicalLine;
            this.numberOfLabels = this.logicalLine.getNumberOfLabels();
            this.numberOfOperands = this.logicalLine.getNumberOfOperands();
        } else {
            this.logicalLine = null;
            this.numberOfLabels = 0;
            this.numberOfOperands = 0;
        }

        this.mnemonic = null;
        this.attribute = null;

        // Set the evaluation context to null. It will be created on demand in getEvaluationContext().
        this.evaluationContext = null;
    }

}
