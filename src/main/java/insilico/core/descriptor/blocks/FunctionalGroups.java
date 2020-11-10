package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.tools.utils.MoleculeUtilities;
import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.isomorphism.Mappings;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FunctionalGroups extends DescriptorBlock {

    private static final long serialVersionUID = 1L;
    private static final String BlockName = "Functional Groups";

    private Pattern[] Queries;

    // Definition of Fragments: #, name, description, SMARTS
    private final static String[][] FG_SMARTS = {
            {"1", "nCp", "terminal primary C(sp3)", "[$([C;D1]-[#6]),$([C;D1]-[*;!#6;D1]),$([C;D2](-[#6])[*;!#6;D1]),$([C;D2](-[*;!#6;D1])[*;!#6;D1]),$([C;D3](-[#6])([*;!#6;D1])[*;!#6;D1]),$([C;D3](-[*;!#6;D1])([*;!#6;D1])[*;!#6;D1]),$([C;D4](-[#6])([*;!#6;D1])([*;!#6;D1])[*;!#6;D1]),$([C;D4](-[*;!#6;D1])([*;!#6;D1])([*;!#6;D1])[*;!#6;D1])]"},
            {"2", "nCs", "total secondary C(sp3)", "[$([C;D2]([#6])[#6]),$([C;D3]([#6])([#6])[*;!#6]),$([C;D4]([#6])([#6])([*;!#6])[*;!#6])]"},
            {"3", "nCt", "total tertiary C(sp3)", "[$([C;D3]([#6])([#6])[#6]),$([C;D4]([#6])([#6])([#6])[*;!#6])]"},
            {"4", "nCq", "total quaternary C(sp3)", "[C;D4]([#6])([#6])([#6])[#6]"},
            {"5", "nCrs", "ring secondary C(sp3)", "[$([C;D2;H2](@[#6])@[#6]),$([C;D3;H](@[#6])(@[#6])[*;!#6]),$([C;D3;H](@[#6])(@[!#6])[#6]),$([C;D4](@[#6])(@[#6])([*;!#6])[*;!#6]),$([C;D4](@[#6])(@[!#6])([#6])[*;!#6]),$([C;D4](@[!#6])(@[!#6])([#6])[#6])]"},
            {"6", "nCrt", "ring tertiary C(sp3)", "[$([C;D3;H](@[#6])(@[#6])[#6]),$([C;D4](@[!#6])(@[#6])([#6])[#6]),$([C;D4](@[#6])(@[#6])([#6])[*;!#6])]"},
            {"7", "nCrq", "ring quaternary C(sp3)", "[C;D4](@[#6])(@[#6])([#6])[#6]"},
            {"8", "nCar", "aromatic C(sp2)", "[c]"},
            {"9", "nCbH", "unsubstituted benzene C(sp2)", "[$([c;D2]1ccccc1)]"},
            {"10", "nCb–", "substituted benzene C(sp2)", "[$([c;D3]1ccccc1)]"},
            {"11", "nCconj", "non-aromatic conjugated C(sp2)", "[$(C=CC=*),$(C(=*)C=*),$(C(=*)[a]),$(C=[*][a])]"},
            {"12", "nR=Cp", "terminal primary C(sp2)", "[$([C;D1]=C),$([C;D2]([!#6;D1])=C),$([C;D3]([!#6;D1])([!#6;D1])=C)]"},
            {"13", "nR=Cs", "aliphatic secondary C(sp2)", "[$([C;D2]([#6])=[#6]),$([C;D3]([!#6])([#6])=[#6])]"},
            {"14", "nR=Ct", "aliphatic tertiary C(sp2)", "[$([C;D3]([#6])([#6])=C)]"},
            {"15", "n=C=", "allenes groups", "[$([C;D2](=[#6])=[#6])]"},
            {"16", "nR#CH/X", "terminal C(sp)", "[$([C;D1]#[#6]),$([C;D2](#[#6])[*;!#6;D1])]"},
            {"17", "nR#C–", "non-terminal C(sp)", "[C;D2](#C)[C,$([*;!D1])]"},
            {"18", "nROCN", "cyanates (aliphatic)", "[$(C(#N)-O-[A])]"},
            {"19", "nArOCN", "cyanates (aromatic)", "[$(C(#N)-O-[a])]"},
            {"20", "nRNCO", "isocyanates (aliphatic)", "[$(C(=O)=N[A])]"},
            {"21", "nArNCO", "isocyanates (aromatic)", "[$(C(=O)=N[a])]"},
            {"22", "nRSCN", "thiocyanates (aliphatic)", "[$(C(#N)-S-[A])]"},
            {"23", "nArSCN", "thiocyanates (aromatic)", "[$(C(#N)-S-[a])]"},
            {"24", "nRNCS", "isothiocyanates (aliphatic)", "[$(C(=S)=N-[A])]"},
            {"25", "nArNCS", "isothiocyanates (aromatic)", "[$(C(=S)=N-[a])]"},
            {"26", "nRCOOH", "carboxylic acids (aliphatic)", "C(=O)([O;D1;!-])[A]"},
            {"27", "nArCOOH", "carboxylic acids (aromatic)", "C(=O)([O;D1;!-])[a]"},
            {"28", "nRCOOR", "esters (aliphatic)", "O=[$([C;D2]),$([C;D3]C)][O;D2][C,c]"},
            {"29", "nArCOOR", "esters (aromatic)", "O=C([a])[O;D2][C,c]"},
            {"30", "nRCONH2", "primary amides (aliphatic)", "[$([C;D3](=O)([N;D1;!+])[C;A]),$([C;D2](=O)[N;D1;!+])]"},
            {"31", "nArCONH2", "primary amides (aromatic)", "[$([C;D3](=O)([N;D1;!+])[a])]"},
            {"32", "nRCONHR", "secondary amides (aliphatic)", "[$([C;D2]),$([C;D3]C)](=O)[N;D2;!+][C,c;!$(C=O)]"},
            {"33", "nArCONHR", "secondary amides (aromatic)", "C([a])(=O)[N;D2;!+][C,c;!$(C=O)]"},
            {"34", "nRCONR2", "tertiary amides (aliphatic)", "O=[$([C;D2]),$([C;D3]C)][N;D3;!+]([C,c;!$(C=O)])[C,c;!$(C=O)]"},
            {"35", "nArCONR2", "tertiary amides (aromatic)", "O=C([a])[N;D3;!+]([C,c;!$(C=O)])[C,c;!$(C=O)]"},
            {"36", "nROCON", "(thio-) carbamates (aliphatic)", "C(=[O,S])([$([O;D1]),$([S;D1]),$([O;D2](C)[A]),$([S;D2](C)[A])])[$([N;D1]),$([N;D2](C)[A]),$([N;D3](C)([A])[A])]"},
            {"37", "nArOCON", "(thio-) carbamates (aromatic)", "[$(C(=[O,S])([O,S][a])N),$(C(=[O,S])([O,S])N[a])]"},
            {"38", "nRCOX", "acyl halogenides (aliphatic)", "[$(C(=O)([Cl,Br,F,I])[a])]"},
            {"39", "nArCOX", "acyl halogenides (aromatic)", "[$(C(=O)([Cl,Br,F,I])[a])]"},
            {"40", "nRCSOH", "thioacids (aliphatic)", "[$(C(=O)([S;D1])C),$(C(=S)([O;D1;!-])C)]"},
            {"41", "nArCSOH", "thioacids (aromatic)", "[$(C(=O)([S;D1])a),$(C(=S)([O;D1;!-])a)]"},
            {"42", "nRCSSH", "dithioacids (aliphatic)", "[$(C(=S)([SH])[A])]"},
            {"43", "nArCSSH", "dithioacids (aromatic)", "[$(C(=S)([SH])[A])]"},
            {"44", "nRCOSR", "thioesters (aliphatic)", "[$(C(=S)([O;D2][C,c])C),$([C;D2](=S)([O;D2][C,c])),$(C(=O)([S;D2][C,c])C),$([C;D2](=O)([S;D2][C,c]))]"},
            {"45", "nArCOSR", "thioesters (aromatic)", "[$(C(=S)([O;D2][C,c])[a]),$(C(=O)([S;D2][C,c])[a])]"},
            {"46", "nRCSSR", "dithioesters (aliphatic)", "[$(C(=S)([S;D2][C,c])C),$([C;D2](=S)([S;D2][C,c]))]"},
            {"47", "nArCSSR", "dithioesters (aromatic)", "[$(C(=S)([S;D2][C,c])[a])]"},
            {"48", "nRCHO", "aldehydes (aliphatic)", "[$([C;D2](=O)C)]"},
            {"49", "nArCHO", "aldehydes (aromatic)", "[$([C;D2](=O)[a])]"},
            {"50", "nRCO", "ketones (aliphatic)", "[C;D3](=O)([C])[C]"},
            {"51", "nArCO", "ketones (aromatic)", "[$([C;D3](=O)([a])[C,c])]"},
            {"52", "nCONN", "urea (-thio) derivatives", "C(=[O,S])([$([#7;D3;!+](*)(*)*),$([#7;D2;!+](*)*),$([#7;D1;!+])])[$([#7;D3;!+](*)(*)*),$([#7;D2;!+](*)*),$([#7;D1;!+])]"},
            {"53", "nC=O(OR)2", "carbonate (-thio) derivatives", "C(=[O,S])([$([O,S]);D2])[$([O,S]);D2]"},
            {"54", "nN=C-N<", "amidine derivatives", "[$([C;D2]),$([C;D3][C,c])](=[$([#7;D1]),$([#7;D2]);!+])[$([#7;D1]),$([#7;D2]),$([#7;D3]);!$([#7]=*);!+]"},
            {"55", "nC(=N)N2", "guanidine derivatives", "C([$([N;D1]),$([N;D2]),$([N;D3]);!$(N=*);!+])([$([N;D1]),$([N;D2]),$([N;D3]);!$(N=*);!+])=[$([N;D1]),$([N;D2]);!+]"},
            {"56", "nRC=N", "imines (aliphatic)", "[$([N;D1;!+]),$([N;!+]C),$([N;!+][N;D2]=*)]=[$([C;D1]),$([C;D2]C),$([C;D3](C)C)]"},
            {"57", "nArC=N", "imines (aromatic)", "[$([N;D1;!+]),$([N;!+]C),$([N;!+][N;D2]=*)]=[$([C;D2][a]),$([C;D3]([a])[C,c]),$([C;D3]([a])N=*)]"},
            {"58", "nRCNO", "oximes (aliphatic)", "[$([C;D1]),$([C;D2]C),$([C;D3](C)C)]=[N;D2]O"},
            {"59", "nArCNO", "oximes (aromatic)", "[$([C;D2][a]),$([C;D3]([C,c])[a])]=[N;D2]O"},
            {"60", "nRNH2", "primary amines (aliphatic)", "[N;D1][$([C;A]);!$(C=[O,S])]"},
            {"61", "nArNH2", "primary amines (aromatic)", "[N;D1][a]"},
            {"62", "nRNHR", "secondary amines (aliphatic)", "[N;D2]([$([C;A]);!$(C=[O,S])])[$([C;A]);!$(C=[O,S])]"},
            {"63", "nArNHR", "secondary amines (aromatic)", "[N;D2]([a])[$([c,C]);!$(C=[O,S])]"},
            {"64", "nRNR2", "tertiary amines (aliphatic)", "[N;D3]([$([C;A]);!$(C=[O,S])])([$([C;A]);!$(C=[O,S])])[$([C;A]);!$(C=[O,S])]"},
            {"65", "nArNR2", "tertiary amines (aromatic)", "[N;D3]([a])([$([C,c]);!$(C=[O,S])])[$([C,c]);!$(C=[O,S])]"},
            {"66", "nN-N", "N hydrazines", "[$([N;D1]),$([N;D2][C,c]),$([N;D3]([C,c])[C,c])]-[$([N;D1]),$([N;D2][C,c]),$([N;D3]([C,c])[C,c])]"},
            {"67", "nN=N", "N azo-derivatives", "[$([N;D1]),$([N;D2][C,c])]=[$([N;D1]),$([N;D2][C,c])]"},
            {"68", "nRCN", "nitriles (aliphatic)", "N#CC"},
            {"69", "nArCN", "nitriles (aromatic)", "N#C[a]"},
            {"70", "nN+", "positively charged N", "[$([#7+,#7++,#7+++]);!$([N+]([O-])=O)]"},
            {"71", "nNq", "quaternary N", "[N;D4]"},
            {"72", "nRNHO", "hydroxylamines (aliphatic)", "[$([N;D1;!+]),$([N;D2;!+]([!a])[!a]),$([N;D3;!+]([!a])([!a])[!a])]O"},
            {"73", "nArNHO", "hydroxylamines (aromatic)", "[$([N;D2;!+]([*])[*]),$([N;D3;!+]([*])([*])[*])]([a])O"},
            {"74", "nRNNOx", "N-nitroso groups (aliphatic)", "O=[N;!$([N+])]N([C,c])[C,c]"},
            {"75", "nArNNOx", "N-nitroso groups (aromatic)", "O=NN([A,a])a"},
            {"76", "nRNO", "nitroso groups (aliphatic)", "[C][N;D2;!+;!++;!-;!--]=O"},
            {"77", "nArNO", "nitroso groups (aromatic)", "[a][N;D2;!+;!++;!-;!--]=O"},
            {"78", "nRNO2", "nitro groups (aliphatic)", "[C][N+]([O-])=O"},
            {"79", "nArNO2", "nitro groups (aromatic)", "[a][N+]([O-])=O"},
            {"80", "nN(CO)2", "imides (-thio)", "[$([N;D2;!+]),$([N;D3](C)(C)[C,c])]([C;D3]=[O,S])[C;D3]=[O,S]"},
            {"81", "nC=N-N<", "hydrazones", "N(=[$([C;D1]),$([C;D2][C,c]),$([C;D3]([C,c])[C,c])])[$([N;D1]),$([N;D2][C,c]),$([N;D3]([C,c])[C,c])]"},
            {"82", "nROH", "hydroxyl groups", "[O;D1;!-]A"},
            {"83", "nArOH", "aromatic hydroxyls", "[O;D1;!-]a"},
            {"84", "nOHp", "primary alcohols", "[O;D1;!-][C;D2;H2][C,c]"},
            {"85", "nOHs", "secondary alcohols", "[O;D1;!-][C;D3;H1]([C,c])[C,c]"},
            {"86", "nOHt", "tertiary alcohols", "[O;D1;!-][C;D4]([C,c])([C,c])[C,c]"},
            {"87", "nROR", "ethers (aliphatic)", "[C;!$(C=[O,S]);!$(C#N)]O[C;!$(C=[O,S]);!$(C#N)]"},
            {"88", "nArOR", "ethers (aromatic)", "[#6;!$(C=O);!$(C#N)]O[a]"},
            {"89", "nROX", "hypohalogenides (aliphatic)", "[F,Cl,Br,I]O[A;!$(C=O);!$(C#N)]"},
            {"90", "nArOX", "hypohalogenides (aromatic)", "[F,Cl,Br,I]O[a]"},
            {"91", "nO(C=O)2", "anhydrides (-thio)", "[C;D3](=[O,S])O[C;D3]=[O,S]"},
            {"92", "nH2O", "water molecules", "[O;H2]"},
            {"93", "nSH", "thiols", "[S;D1][C;!$(C=*);!$(C#*)]"},
            {"94", "nC=S", "thioketones", "[S;D1]=C(C)C"},
            {"95", "nRSR", "sulfides", "[#6;!$(C=O);!$(C=S);!$(C#*)][S;D2][#6;!$(C=O);!$(C=S);!$(C#*)]"},
            {"96", "nRSSR", "disulfides", "[#6;!$(C=O);!$(C=S);!$(C#*)][S;D2][S;D2][#6;!$(C=O);!$(C=S);!$(C#*)]"},
            {"97", "nSO", "sulfoxides", "[$([S;D3](=O)([C,c])[C,c]),$([S;D2](=O)C),$([S;D2](=O)S)]"},
            {"98", "nS(=O)2", "sulfones", "[$([S;D4](=O)(=O)([*;!a])[*;!a]),$([S;D3](=O)(=O)=C),$([S;D3](=O)(=O)=S)]"},
            {"99", "nSOH", "sulfenic (thio-) acids", "[S;D2][O,S;D1]"},
            {"100", "nSOOH", "sulfinic (thio-/dithio-) acids", "[S;D3](=[O,S])[O,S;D1]"},
            {"101", "nSO2OH", "sulfonic (thio-/dithio-) acids", "[S;D4](=[S,O])(=[S,O])([S,O;D1])[*;!S;!O]"},
            {"102", "nSO3OH", "sulfuric (thio-/dithio-) acids", "[S,O;D1]S([S,O])(=[S,O])=[S,O]"},
            {"103", "nSO2", "sulfites (thio-/dithio-)", "[$([S;D3](=[S,O])[S,O;D2]),$([S;D3](=[S,O])([S,O;D2])[S,O;D2])]"},
            {"104", "nSO3", "sulfonates (thio-/dithio-)", "[S;D4](=[S,O])(=[S,O])([S,O;D2])[*;!S;!O]"},
            {"105", "nSO4", "sulfates (thio-/dithio-)", "[S;D4](=[S,O])(=[S,O])([S,O;D2])[S,O;D2]"},
            {"106", "nSO2N", "sulfonamides (thio-/dithio-)", "[$([S;D2]),$([S;D3]=[O,S]),$([S;D4](=[O,S])=O)]([C,c])[$([N;D1;!+]),$([N;D2;!+]([*])[*]),$([N;D3;!+]([*])([*])[*])]"},
            {"107", "nPO3", "phosphites/thiophosphites", "[P;D3]([$([O,S][A,a])])([$([O,S][A,a])])[$([O,S])]"},
            {"108", "nPO4", "phosphates/thiophosphates", "[P;D4](=[O,S])([$([O,S][A,a])])([$([O,S][A,a])])[$([O,S])]"},
            {"109", "nPR3", "phosphanes", "[$([P;D3]([C,Cl,Br,F,I])([C,Cl,Br,F,I])[C,Cl,Br,F,I]),$([P;D2]([C,Cl,Br,F,I])[C,Cl,Br,F,I]),$([P;D1][C,Cl,Br,F,I])]"},
            {"110", "nP(=O)O2R", "phosphonates (thio-)", "[$([P;D3]),$([P;D4][C,c,F,Cl,Br,I])]([O,S])(=[O,S])[O,S]"},
            {"111", "nP(=O)R3/nPR5", "phosphoranes (thio-)", "[$([#6,F,Cl,Br,I]P([#6,F,Cl,Br,I])([#6,F,Cl,Br,I])=[O,S]),$([#6,F,Cl,Br,I][P;D3]([#6,F,Cl,Br,I])=[O,S]),$([#6,F,Cl,Br,I][P;D2]=[O,S]),$([P;D1]=[O,S])]"},
            {"112", "nCH2RX", "CH2RX", "[C,c][C;D2;!R][Cl,Br,F,I]"},
            {"113", "nCHR2X", "CHR2X", "[C,c][C;D3;!R;!R2;!$(C(@[*])@[*])]([C,c])[Cl,Br,F,I]"},
            {"114", "nCR3X", "CR3X", "[C;D4;!R;!R2;!$(C(@[*])@[*])]([C,c])([C,c])([C,c])[Cl,Br,F,I]"},
            {"115", "nR=CHX", "R=CHX", "C=[C;D2;!R][Cl,Br,F,I]"},
            {"116", "nR=CRX", "R=CRX", "C=[C;!R;!R2;!$(C(@[*])@[*])]([C,c])[Cl,Br,F,I]"},
            {"117", "nR#CX", "R#CX", "C#C[Cl,Br,F,I]"},
            {"118", "nCHRX2", "CHRX2", "[C;D3;!R]([C,c])([Cl,Br,F,I])[Cl,Br,F,I]"},
            {"119", "nCR2X2", "CR2X2", "[C;D4;!R;!R2;!$(C(@[*])@[*])]([C,c])([C,c])([Cl,Br,F,I])[Cl,Br,F,I]"},
            {"120", "nR=CX2", "R=CX2", "C=[C;!R]([Cl,Br,F,I])[Cl,Br,F,I]"},
            {"121", "nCRX3", "CRX3", "[C;D4;!R]([C,c])([Cl,Br,F,I])([Cl,Br,F,I])[Cl,Br,F,I]"},
            {"122", "nArX", "X on aromatic ring", "a[Cl,Br,F,I]"},
            {"123", "nCXr", "X on ring C(sp3)", "[Cl,Br,F,I][$(C(@[*])@[*]);!$(C=*)]"},
            {"124", "nCXr=", "X on ring C(sp2)", "[Cl,Br,F,I][$(C(@[*])@[*]);$(C=*)]"},
            {"125", "nCconjX", "X on exo-conjugated C", "[Cl,Br,F,I][$(C(=[*])C=[*]),$(C=[*]C=[*]),$(C=[*][a])]"},
            {"126", "nAziridines", "Aziridines", "C1CN1"},
            {"127", "nOxiranes", "Oxiranes", "C1CO1"},
            {"128", "nThiranes", "Thiranes", "C1CS1"},
            {"129", "nAzetidines", "Azetidines", "C1C[N;!$(N@C=O)]C1"},
            {"130", "nOxetanes", "Oxetanes", "C1COC1"},
            {"131", "nThioethanes", "Thioethanes", "C1CSC1"},
            {"132", "nBeta-Lactams", "Beta-Lactams", "O=[$(C1CC2@*@*@*N12),$(C1CC2@*@*@*@*N12)]"},
            {"133", "nPyrrolidines", "Pyrrolidines", "C1CC[N;!$(N@C=O)]C1"},
            {"134", "nOxolanes", "Oxolanes", "C1CC[O;!$(O@C=O)]C1"},
            {"135", "nth-Thiophenes", "tetrahydro-thiophenes", "C1CCSC1"},
            {"136", "nPyrroles", "Pyrroles", "n1cccc1"},
            {"137", "nPyrazoles", "Pyrazoles", "n1cccn1"},
            {"138", "nImidazoles", "Imidazoles", "n1ccnc1"},
            {"139", "nFuranes", "Furanes", "c1ccoc1"},
            {"140", "nThiophenes", "Thiophenes", "c1ccsc1"},
            {"141", "nOxazoles", "Oxazoles", "c1cocn1"},
            {"142", "nIsoxazoles", "Isoxazoles", "c1cnoc1"},
            {"143", "nThiazoles", "Thiazoles", "c1cscn1"},
            {"144", "nIsothiazoles", "Isothiazoles", "c1cnsc1"},
            {"145", "nTriazoles", "Triazoles", "[$(c1ncnn1),$(c1cnnn1)]"},
            {"146", "nPyridines", "Pyridines", "c1cc[$([nX3]),$([nX2])]cc1"},
            {"147", "nPyridazines", "Pyridazines", "c1ccnnc1"},
            {"148", "nPyrimidines", "Pyrimidines", "c1cncnc1"},
            {"149", "nPyrazines", "Pyrazines", "c1cnccn1"},
            {"150", "n135-Triazines", "1-3-5-Triazines", "c1ncncn1"},
            {"151", "n124-Triazines", "1-2-4-Triazines", "c1cnncn1"},
            {"152", "nHDon", "donor atoms for H-bonds (N and O)", ""}, // not defined by SMARTS
            {"153", "nHAcc", "acceptor atoms for H-bonds (N,O,F)", ""}, // not defined by SMARTS
//            {"154", "nHBonds", "intramolecular H-bonds (with N,O,F)", ""}  // last fragment is a 3D frag
    };


    /**
     * Constructor.
     */
    public FunctionalGroups() {
        super();
        this.Name = FunctionalGroups.BlockName;

        // Init SMARTS
        int FGNumber = FG_SMARTS.length;
        Queries = new Pattern[FGNumber];
        for (int i = 0; i < FGNumber; i++) {
            try {
                Queries[i] = SmartsPattern.create(FG_SMARTS[i][3]).setPrepare(false);
            } catch (Exception e) {
                log.warn("Unable to parse SMARTS in functional groups: " + FG_SMARTS[i][3]);
                Queries[i] = null;
            }
        }
    }


    @Override
    protected final void GenerateDescriptors() {
        DescList.clear();
        int FGNumber = FG_SMARTS.length;
        for (int i=0; i<FGNumber; i++) {
            Add(FG_SMARTS[i][1], FG_SMARTS[i][2]);
        }
        SetAllValues(Descriptor.MISSING_VALUE);
    }


    /**
     * Calculate descriptors for the given molecule.
     *
     * @param mol molecule to be calculated
     */
    @Override
    public void Calculate(InsilicoMolecule mol) {

        GenerateDescriptors();

        try {

            // Performs SMARTS matching
            for (int i=0; i<this.GetSize(); i++) {

                // last two groups skipped (H-donor and H-acceptor)
                if (i>=151)
                    continue;

                // Check if query has been correctly initialized
                if (Queries[i] == null)
                    throw new Exception("Unable to init SMARTS query no. " + i);

                int nmatch = 0;
                List<Mappings> mappings = new ArrayList<>();
                boolean status;
                boolean err = false;

                try {
                    status = Queries[i].matches(mol.GetStructure());
                    if (status) {
                        mappings.add(Queries[i].matchAll(mol.GetStructure()));
                        nmatch = Queries[i].matchAll(mol.GetStructure()).countUnique();
                    }
                } catch (Exception e) {
                    log.warn("Unable to perform functional groups query no. " + (i+1) + " - " + e.getMessage());
                    this.SetByIndex(i, Descriptor.MISSING_VALUE);
                    continue;
                }

                // manual fix for triazoles
                if (i == 144) {
                    nmatch = nmatch / 2;
                }

                // Sets group
                this.SetByIndex(i, nmatch);
            }


            // Calculates H donor and acceptors
            IAtomContainer m = mol.GetStructure();
            int HAcc=0, HDon=0;
            int nSK = m.getAtomCount();
            for (int i=0; i<nSK; i++) {

                IAtom at = m.getAtom(i);
                int nH = 0;
                try {
                    nH = at.getImplicitHydrogenCount();
                } catch (Exception e) { }

                // H Donors: number of h linked to any N and O
                if ( (at.getSymbol().equalsIgnoreCase("O")) ||
                        (at.getSymbol().equalsIgnoreCase("N")) ) {
                    HDon += nH;
                }

                // H Acceptors
                if (at.getSymbol().equalsIgnoreCase("F"))
                    HAcc++;
                if (at.getSymbol().equalsIgnoreCase("O")) {
                    if ((6 - MoleculeUtilities.GetTotalBondOrder(at, m) - at.getFormalCharge()) > 0)
                        HAcc++;
                }
                if (at.getSymbol().equalsIgnoreCase("N")) {
                    if (MoleculeUtilities.GetTotalBondOrder(at, m) < 4)
                        HAcc++;
                    // TODO! Exclude pyrrole-like N
                }

            }
            this.SetByIndex(151, HDon);
            this.SetByIndex(152, HAcc);

        } catch (Exception e) {
            log.warn("Unable to calculate: " + this.Name + " - " + e.getMessage());
            this.SetAllValues(Descriptor.MISSING_VALUE);
        }

    }


    /**
     * Clones the actual descriptor block
     * @return a cloned copy of the actual object
     * @throws CloneNotSupportedException
     */
    public DescriptorBlock CreateClone() throws CloneNotSupportedException {
        FunctionalGroups block = new FunctionalGroups();
        block.CloneDetailsFrom(this);
        block.Queries = this.Queries.clone();
        return block;
    }

}
