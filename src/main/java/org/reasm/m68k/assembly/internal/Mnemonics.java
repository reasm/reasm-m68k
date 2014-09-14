package org.reasm.m68k.assembly.internal;

import java.util.Map;
import java.util.TreeMap;

import org.reasm.Symbol;
import org.reasm.SymbolReference;
import org.reasm.SymbolResolutionFallback;

/**
 * Exposes constants for the instructions and directives supported by the M68000 Family assembler.
 *
 * @author Francis Gagné
 */
@SuppressWarnings("javadoc")
public final class Mnemonics {

    static final class MnemonicMap {

        private final Map<String, MnemonicSymbol> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        MnemonicSymbol get(String mnemonicName) {
            return this.map.get(mnemonicName);
        }

        void put(String mnemonicName, Mnemonic mnemonicHandler) {
            this.map.put(mnemonicName, new MnemonicSymbol(mnemonicName, mnemonicHandler));
        }

    }

    public static final String ABCD = "ABCD";
    public static final String ADD = "ADD";
    public static final String ADDA = "ADDA";
    public static final String ADDI = "ADDI";
    public static final String ADDQ = "ADDQ";
    public static final String ADDX = "ADDX";
    public static final String AND = "AND";
    public static final String ANDI = "ANDI";
    public static final String ASL = "ASL";
    public static final String ASR = "ASR";
    public static final String BCC = "BCC";
    public static final String BCHG = "BCHG";
    public static final String BCLR = "BCLR";
    public static final String BCS = "BCS";
    public static final String BEQ = "BEQ";
    public static final String BGE = "BGE";
    public static final String BGND = "BGND";
    public static final String BGT = "BGT";
    public static final String BHI = "BHI";
    public static final String BHS = "BHS";
    public static final String BKPT = "BKPT";
    public static final String BLE = "BLE";
    public static final String BLO = "BLO";
    public static final String BLS = "BLS";
    public static final String BLT = "BLT";
    public static final String BMI = "BMI";
    public static final String BNE = "BNE";
    public static final String BPL = "BPL";
    public static final String BRA = "BRA";
    public static final String BSET = "BSET";
    public static final String BSR = "BSR";
    public static final String BTST = "BTST";
    public static final String BVC = "BVC";
    public static final String BVS = "BVS";
    public static final String CHK = "CHK";
    public static final String CLR = "CLR";
    public static final String CMP = "CMP";
    public static final String CMPA = "CMPA";
    public static final String CMPI = "CMPI";
    public static final String CMPM = "CMPM";
    public static final String DBCC = "DBCC";
    public static final String DBCS = "DBCS";
    public static final String DBEQ = "DBEQ";
    public static final String DBF = "DBF";
    public static final String DBGE = "DBGE";
    public static final String DBGT = "DBGT";
    public static final String DBHI = "DBHI";
    public static final String DBHS = "DBHS";
    public static final String DBLE = "DBLE";
    public static final String DBLO = "DBLO";
    public static final String DBLS = "DBLS";
    public static final String DBLT = "DBLT";
    public static final String DBMI = "DBMI";
    public static final String DBNE = "DBNE";
    public static final String DBPL = "DBPL";
    public static final String DBRA = "DBRA";
    public static final String DBT = "DBT";
    public static final String DBVC = "DBVC";
    public static final String DBVS = "DBVS";
    public static final String DIVS = "DIVS";
    public static final String DIVSL = "DIVSL";
    public static final String DIVU = "DIVU";
    public static final String DIVUL = "DIVUL";
    public static final String EOR = "EOR";
    public static final String EORI = "EORI";
    public static final String EXG = "EXG";
    public static final String EXT = "EXT";
    public static final String EXTB = "EXTB";
    public static final String ILLEGAL = "ILLEGAL";
    public static final String JMP = "JMP";
    public static final String JSR = "JSR";
    public static final String LEA = "LEA";
    public static final String LINK = "LINK";
    public static final String LPSTOP = "LPSTOP";
    public static final String LSL = "LSL";
    public static final String LSR = "LSR";
    public static final String MOVE = "MOVE";
    public static final String MOVEA = "MOVEA";
    public static final String MOVEM = "MOVEM";
    public static final String MOVEP = "MOVEP";
    public static final String MOVEQ = "MOVEQ";
    public static final String MULS = "MULS";
    public static final String MULU = "MULU";
    public static final String NBCD = "NBCD";
    public static final String NEG = "NEG";
    public static final String NEGX = "NEGX";
    public static final String NOP = "NOP";
    public static final String NOT = "NOT";
    public static final String OR = "OR";
    public static final String ORI = "ORI";
    public static final String PEA = "PEA";
    public static final String RESET = "RESET";
    public static final String ROL = "ROL";
    public static final String ROR = "ROR";
    public static final String ROXL = "ROXL";
    public static final String ROXR = "ROXR";
    public static final String RTD = "RTD";
    public static final String RTE = "RTE";
    public static final String RTM = "RTM";
    public static final String RTR = "RTR";
    public static final String RTS = "RTS";
    public static final String SBCD = "SBCD";
    public static final String SCC = "SCC";
    public static final String SCS = "SCS";
    public static final String SEQ = "SEQ";
    public static final String SF = "SF";
    public static final String SGE = "SGE";
    public static final String SGT = "SGT";
    public static final String SHI = "SHI";
    public static final String SHS = "SHS";
    public static final String SLE = "SLE";
    public static final String SLO = "SLO";
    public static final String SLS = "SLS";
    public static final String SLT = "SLT";
    public static final String SMI = "SMI";
    public static final String SNE = "SNE";
    public static final String SPL = "SPL";
    public static final String ST = "ST";
    public static final String STOP = "STOP";
    public static final String SUB = "SUB";
    public static final String SUBA = "SUBA";
    public static final String SUBI = "SUBI";
    public static final String SUBQ = "SUBQ";
    public static final String SUBX = "SUBX";
    public static final String SVC = "SVC";
    public static final String SVS = "SVS";
    public static final String SWAP = "SWAP";
    public static final String TAS = "TAS";
    public static final String TRAP = "TRAP";
    public static final String TRAPV = "TRAPV";
    public static final String TST = "TST";
    public static final String UNLK = "UNLK";

    public static final String ALIGN = "ALIGN";
    public static final String BINCLUDE = "BINCLUDE";
    public static final String CNOP = "CNOP";
    public static final String DC = "DC";
    public static final String DCB = "DCB";
    public static final String DEPHASE = "DEPHASE";
    public static final String DO = "DO";
    public static final String DS = "DS";
    public static final String ELSE = "ELSE";
    public static final String ELSEIF = "ELSEIF";
    public static final String END = "END";
    public static final String ENDC = "ENDC";
    public static final String ENDIF = "ENDIF";
    public static final String ENDM = "ENDM";
    public static final String ENDNS = "ENDNS";
    public static final String ENDR = "ENDR";
    public static final String ENDTRANSFORM = "ENDTRANSFORM";
    public static final String ENDW = "ENDW";
    public static final String EQU = "EQU";
    /** The string <code>"="</code>. */
    public static final String EQUALS = "=";
    public static final String EQUR = "EQUR";
    public static final String EVEN = "EVEN";
    public static final String FOR = "FOR";
    public static final String FUNCTION = "FUNCTION";
    public static final String HEX = "HEX";
    public static final String IF = "IF";
    public static final String INCBIN = "INCBIN";
    public static final String INCLUDE = "INCLUDE";
    public static final String MACRO = "MACRO";
    public static final String NAMESPACE = "NAMESPACE";
    public static final String NEXT = "NEXT";
    public static final String OBJ = "OBJ";
    public static final String OBJEND = "OBJEND";
    public static final String ORG = "ORG";
    public static final String PHASE = "PHASE";
    public static final String REG = "REG";
    public static final String REPT = "REPT";
    public static final String RS = "RS";
    public static final String RSRESET = "RSRESET";
    public static final String RSSET = "RSSET";
    public static final String SET = "SET";
    public static final String TRANSFORM = "TRANSFORM";
    public static final String UNTIL = "UNTIL";
    public static final String WHILE = "WHILE";

    static final MnemonicMap MAP;
    static final SymbolResolutionFallback SYMBOL_RESOLUTION_FALLBACK;

    static {
        final MnemonicMap map = new MnemonicMap();

        // Put the instructions in the dispatch map.
        map.put(ABCD, AddSubWithExtendInstruction.ABCD);
        map.put(ADD, AddSubInstruction.ADD);
        map.put(ADDA, AddaCmpaSubaInstruction.ADDA);
        map.put(ADDI, AddiCmpiSubiInstruction.ADDI);
        map.put(ADDQ, AddqSubqInstruction.ADDQ);
        map.put(ADDX, AddSubWithExtendInstruction.ADDX);
        map.put(AND, AndOrInstruction.AND);
        map.put(ANDI, AndiEoriOriInstruction.ANDI);
        map.put(ASL, ShiftRotateInstruction.ASL);
        map.put(ASR, ShiftRotateInstruction.ASR);
        map.put(BCC, BranchInstruction.BCC);
        map.put(BCHG, BitManipulationInstruction.BCHG);
        map.put(BCLR, BitManipulationInstruction.BCLR);
        map.put(BCS, BranchInstruction.BCS);
        map.put(BEQ, BranchInstruction.BEQ);
        map.put(BGE, BranchInstruction.BGE);
        map.put(BGND, SimpleInstruction.BGND);
        map.put(BGT, BranchInstruction.BGT);
        map.put(BHI, BranchInstruction.BHI);
        map.put(BHS, BranchInstruction.BHS);
        map.put(BKPT, OneEaInstruction.BKPT);
        map.put(BLE, BranchInstruction.BLE);
        map.put(BLO, BranchInstruction.BLO);
        map.put(BLS, BranchInstruction.BLS);
        map.put(BLT, BranchInstruction.BLT);
        map.put(BMI, BranchInstruction.BMI);
        map.put(BNE, BranchInstruction.BNE);
        map.put(BPL, BranchInstruction.BPL);
        map.put(BRA, BranchInstruction.BRA);
        map.put(BSET, BitManipulationInstruction.BSET);
        map.put(BSR, BranchInstruction.BSR);
        map.put(BTST, BitManipulationInstruction.BTST);
        map.put(BVC, BranchInstruction.BVC);
        map.put(BVS, BranchInstruction.BVS);
        map.put(CHK, ChkInstruction.CHK);
        map.put(CLR, OneEaInstruction.CLR);
        map.put(CMP, CmpInstruction.CMP);
        map.put(CMPA, AddaCmpaSubaInstruction.CMPA);
        map.put(CMPI, AddiCmpiSubiInstruction.CMPI);
        map.put(CMPM, CmpmInstruction.CMPM);
        map.put(DBCC, DecrementAndBranchInstruction.DBCC);
        map.put(DBCS, DecrementAndBranchInstruction.DBCS);
        map.put(DBEQ, DecrementAndBranchInstruction.DBEQ);
        map.put(DBF, DecrementAndBranchInstruction.DBF);
        map.put(DBGE, DecrementAndBranchInstruction.DBGE);
        map.put(DBGT, DecrementAndBranchInstruction.DBGT);
        map.put(DBHI, DecrementAndBranchInstruction.DBHI);
        map.put(DBHS, DecrementAndBranchInstruction.DBHS);
        map.put(DBLE, DecrementAndBranchInstruction.DBLE);
        map.put(DBLO, DecrementAndBranchInstruction.DBLO);
        map.put(DBLS, DecrementAndBranchInstruction.DBLS);
        map.put(DBLT, DecrementAndBranchInstruction.DBLT);
        map.put(DBMI, DecrementAndBranchInstruction.DBMI);
        map.put(DBNE, DecrementAndBranchInstruction.DBNE);
        map.put(DBPL, DecrementAndBranchInstruction.DBPL);
        map.put(DBRA, DecrementAndBranchInstruction.DBRA);
        map.put(DBT, DecrementAndBranchInstruction.DBT);
        map.put(DBVC, DecrementAndBranchInstruction.DBVC);
        map.put(DBVS, DecrementAndBranchInstruction.DBVS);
        map.put(DIVS, MultiplyDivideInstruction.DIVS);
        map.put(DIVSL, MultiplyDivideInstruction.DIVSL);
        map.put(DIVU, MultiplyDivideInstruction.DIVU);
        map.put(DIVUL, MultiplyDivideInstruction.DIVUL);
        map.put(EOR, EorInstruction.EOR);
        map.put(EORI, AndiEoriOriInstruction.EORI);
        map.put(EXG, ExgInstruction.EXG);
        map.put(EXT, OneEaInstruction.EXT);
        map.put(EXTB, OneEaInstruction.EXTB);
        map.put(ILLEGAL, SimpleInstruction.ILLEGAL);
        map.put(JMP, OneEaInstruction.JMP);
        map.put(JSR, OneEaInstruction.JSR);
        map.put(LEA, LeaInstruction.LEA);
        map.put(LINK, LinkInstruction.LINK);
        map.put(LPSTOP, OneEaInstruction.LPSTOP);
        map.put(LSL, ShiftRotateInstruction.LSL);
        map.put(LSR, ShiftRotateInstruction.LSR);
        map.put(MOVE, MoveInstruction.MOVE);
        map.put(MOVEA, MoveaInstruction.MOVEA);
        map.put(MOVEM, MovemInstruction.MOVEM);
        map.put(MOVEP, MovepInstruction.MOVEP);
        map.put(MOVEQ, MoveqInstruction.MOVEQ);
        map.put(MULS, MultiplyDivideInstruction.MULS);
        map.put(MULU, MultiplyDivideInstruction.MULU);
        map.put(NBCD, OneEaInstruction.NBCD);
        map.put(NEG, OneEaInstruction.NEG);
        map.put(NEGX, OneEaInstruction.NEGX);
        map.put(NOP, SimpleInstruction.NOP);
        map.put(NOT, OneEaInstruction.NOT);
        map.put(OR, AndOrInstruction.OR);
        map.put(ORI, AndiEoriOriInstruction.ORI);
        map.put(PEA, OneEaInstruction.PEA);
        map.put(RESET, SimpleInstruction.RESET);
        map.put(ROL, ShiftRotateInstruction.ROL);
        map.put(ROR, ShiftRotateInstruction.ROR);
        map.put(ROXL, ShiftRotateInstruction.ROXL);
        map.put(ROXR, ShiftRotateInstruction.ROXR);
        map.put(RTD, OneEaInstruction.RTD);
        map.put(RTE, SimpleInstruction.RTE);
        map.put(RTM, OneEaInstruction.RTM);
        map.put(RTR, SimpleInstruction.RTR);
        map.put(RTS, SimpleInstruction.RTS);
        map.put(SBCD, AddSubWithExtendInstruction.SBCD);
        map.put(SCC, OneEaInstruction.SCC);
        map.put(SCS, OneEaInstruction.SCS);
        map.put(SEQ, OneEaInstruction.SEQ);
        map.put(SF, OneEaInstruction.SF);
        map.put(SGE, OneEaInstruction.SGE);
        map.put(SGT, OneEaInstruction.SGT);
        map.put(SHI, OneEaInstruction.SHI);
        map.put(SHS, OneEaInstruction.SHS);
        map.put(SLE, OneEaInstruction.SLE);
        map.put(SLO, OneEaInstruction.SLO);
        map.put(SLS, OneEaInstruction.SLS);
        map.put(SLT, OneEaInstruction.SLT);
        map.put(SMI, OneEaInstruction.SMI);
        map.put(SNE, OneEaInstruction.SNE);
        map.put(SPL, OneEaInstruction.SPL);
        map.put(ST, OneEaInstruction.ST);
        map.put(STOP, OneEaInstruction.STOP);
        map.put(SUB, AddSubInstruction.SUB);
        map.put(SUBA, AddaCmpaSubaInstruction.SUBA);
        map.put(SUBI, AddiCmpiSubiInstruction.SUBI);
        map.put(SUBQ, AddqSubqInstruction.SUBQ);
        map.put(SUBX, AddSubWithExtendInstruction.SUBX);
        map.put(SVC, OneEaInstruction.SVC);
        map.put(SVS, OneEaInstruction.SVS);
        map.put(SWAP, OneEaInstruction.SWAP);
        map.put(TAS, OneEaInstruction.TAS);
        map.put(TRAP, OneEaInstruction.TRAP);
        map.put(TRAPV, SimpleInstruction.TRAPV);
        map.put(TST, OneEaInstruction.TST);
        map.put(UNLK, OneEaInstruction.UNLK);

        // Put the directives in the dispatch map.
        map.put(ALIGN, AlignDirective.ALIGN);
        map.put(BINCLUDE, IncbinDirective.INCBIN);
        map.put(CNOP, CnopDirective.CNOP);
        map.put(DC, DcDirective.DC);
        map.put(DCB, DcbDirective.DCB);
        map.put(DEPHASE, ObjendDirective.OBJEND);
        map.put(DO, DoDirective.DO);
        map.put(DS, DsRsDirective.DS);
        map.put(ELSEIF, IfElseifDirective.ELSEIF);
        map.put(ELSE, ElseDirective.ELSE);
        map.put(END, EndDirective.END);
        map.put(ENDC, EndifDirective.ENDIF);
        map.put(ENDIF, EndifDirective.ENDIF);
        map.put(ENDM, EndmDirective.ENDM);
        map.put(ENDNS, EndnsDirective.ENDNS);
        map.put(ENDR, EndrDirective.ENDR);
        map.put(ENDTRANSFORM, EndtransformDirective.ENDTRANSFORM);
        map.put(ENDW, EndwDirective.ENDW);
        map.put(EQU, EquSetDirective.EQU);
        map.put(EQUALS, EquSetDirective.EQUALS);
        map.put(EQUR, EqurDirective.EQUR);
        map.put(EVEN, EvenDirective.EVEN);
        map.put(FOR, ForDirective.FOR);
        map.put(FUNCTION, FunctionDirective.FUNCTION);
        map.put(HEX, HexDirective.HEX);
        map.put(IF, IfElseifDirective.IF);
        map.put(INCBIN, IncbinDirective.INCBIN);
        map.put(INCLUDE, IncludeDirective.INCLUDE);
        map.put(MACRO, MacroDirective.MACRO);
        map.put(NAMESPACE, NamespaceDirective.NAMESPACE);
        map.put(NEXT, NextDirective.NEXT);
        map.put(OBJ, ObjDirective.OBJ);
        map.put(OBJEND, ObjendDirective.OBJEND);
        map.put(ORG, OrgDirective.ORG);
        map.put(PHASE, ObjDirective.OBJ);
        map.put(REG, RegDirective.REG);
        map.put(REPT, ReptDirective.REPT);
        map.put(RS, DsRsDirective.RS);
        map.put(RSRESET, RsresetRssetDirective.RSRESET);
        map.put(RSSET, RsresetRssetDirective.RSSET);
        map.put(SET, EquSetDirective.SET);
        map.put(TRANSFORM, TransformDirective.TRANSFORM);
        map.put(UNTIL, UntilDirective.UNTIL);
        map.put(WHILE, WhileDirective.WHILE);

        MAP = map;

        SYMBOL_RESOLUTION_FALLBACK = new SymbolResolutionFallback() {
            @Override
            public Symbol resolve(SymbolReference symbolReference) {
                return MAP.get(symbolReference.getName());
            }
        };
    }

    // This class is not meant to be instantiated.
    private Mnemonics() {
    }

}
