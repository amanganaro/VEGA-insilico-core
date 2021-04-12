package insilico.core.descriptor.blocks;

import insilico.core.descriptor.Descriptor;
import insilico.core.descriptor.DescriptorBlock;
import insilico.core.localization.StringSelectorCore;
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
    private static final String BlockName = StringSelectorCore.getString("descriptors_functionalgroups_name");

    private Pattern[] Queries;

    // Definition of Fragments: #, name, description, SMARTS
    private final static String[][] FG_SMARTS = {
            {"1", "nCp", StringSelectorCore.getString("descriptors_fg_nCp"), "[$([C;D1]-[#6]),$([C;D1]-[*;!#6;D1]),$([C;D2](-[#6])[*;!#6;D1]),$([C;D2](-[*;!#6;D1])[*;!#6;D1]),$([C;D3](-[#6])([*;!#6;D1])[*;!#6;D1]),$([C;D3](-[*;!#6;D1])([*;!#6;D1])[*;!#6;D1]),$([C;D4](-[#6])([*;!#6;D1])([*;!#6;D1])[*;!#6;D1]),$([C;D4](-[*;!#6;D1])([*;!#6;D1])([*;!#6;D1])[*;!#6;D1])]"},
            {"2", "nCs", StringSelectorCore.getString("descriptors_fg_nCs"), "[$([C;D2]([#6])[#6]),$([C;D3]([#6])([#6])[*;!#6]),$([C;D4]([#6])([#6])([*;!#6])[*;!#6])]"},
            {"3", "nCt", StringSelectorCore.getString("descriptors_fg_ncT"), "[$([C;D3]([#6])([#6])[#6]),$([C;D4]([#6])([#6])([#6])[*;!#6])]"},
            {"4", "nCq", StringSelectorCore.getString("descriptors_fg_nCq"), "[C;D4]([#6])([#6])([#6])[#6]"},
            {"5", "nCrs", StringSelectorCore.getString("descriptors_fg_nCrs"), "[$([C;D2;H2](@[#6])@[#6]),$([C;D3;H](@[#6])(@[#6])[*;!#6]),$([C;D3;H](@[#6])(@[!#6])[#6]),$([C;D4](@[#6])(@[#6])([*;!#6])[*;!#6]),$([C;D4](@[#6])(@[!#6])([#6])[*;!#6]),$([C;D4](@[!#6])(@[!#6])([#6])[#6])]"},
            {"6", "nCrt", StringSelectorCore.getString("descriptors_fg_nCrt"), "[$([C;D3;H](@[#6])(@[#6])[#6]),$([C;D4](@[!#6])(@[#6])([#6])[#6]),$([C;D4](@[#6])(@[#6])([#6])[*;!#6])]"},
            {"7", "nCrq", StringSelectorCore.getString("descriptors_fg_nCrq"), "[C;D4](@[#6])(@[#6])([#6])[#6]"},
            {"8", "nCar", StringSelectorCore.getString("descriptors_fg_nCar"), "[c]"},
            {"9", "nCbH", StringSelectorCore.getString("descriptors_fg_nCbh"), "[$([c;D2]1ccccc1)]"},
            {"10", "nCb–", StringSelectorCore.getString("descriptors_fg_nCb"), "[$([c;D3]1ccccc1)]"},
            {"11", "nCconj", StringSelectorCore.getString("descriptors_fg_nCconj"), "[$(C=CC=*),$(C(=*)C=*),$(C(=*)[a]),$(C=[*][a])]"},
            {"12", "nR=Cp", StringSelectorCore.getString("descriptors_fg_nR_Cp"), "[$([C;D1]=C),$([C;D2]([!#6;D1])=C),$([C;D3]([!#6;D1])([!#6;D1])=C)]"},
            {"13", "nR=Cs", StringSelectorCore.getString("descriptors_fg_nR_Cs"), "[$([C;D2]([#6])=[#6]),$([C;D3]([!#6])([#6])=[#6])]"},
            {"14", "nR=Ct", StringSelectorCore.getString("descriptors_fg_nR_Ct"), "[$([C;D3]([#6])([#6])=C)]"},
            {"15", "n=C=", StringSelectorCore.getString("descriptors_fg_n_C"), "[$([C;D2](=[#6])=[#6])]"},
            {"16", "nR#CH/X", StringSelectorCore.getString("descriptors_fg_nRCHX"), "[$([C;D1]#[#6]),$([C;D2](#[#6])[*;!#6;D1])]"},
            {"17", "nR#C–", StringSelectorCore.getString("descriptors_fg_nRC"), "[C;D2](#C)[C,$([*;!D1])]"},
            {"18", "nROCN", StringSelectorCore.getString("descriptors_fg_nROCN"), "[$(C(#N)-O-[A])]"},
            {"19", "nArOCN", StringSelectorCore.getString("descriptors_fg_nArOCN"), "[$(C(#N)-O-[a])]"},
            {"20", "nRNCO", StringSelectorCore.getString("descriptors_fg_nRNCO"), "[$(C(=O)=N[A])]"},
            {"21", "nArNCO", StringSelectorCore.getString("descriptors_fg_nArNCO"), "[$(C(=O)=N[a])]"},
            {"22", "nRSCN", StringSelectorCore.getString("descriptors_fg_nRSCN"), "[$(C(#N)-S-[A])]"},
            {"23", "nArSCN", StringSelectorCore.getString("descriptors_fg_nArSCN"), "[$(C(#N)-S-[a])]"},
            {"24", "nRNCS", StringSelectorCore.getString("descriptors_fg_nRNCS"), "[$(C(=S)=N-[A])]"},
            {"25", "nArNCS", StringSelectorCore.getString("descriptors_fg_nArNCS"), "[$(C(=S)=N-[a])]"},
            {"26", "nRCOOH", StringSelectorCore.getString("descriptors_fg_nRCOOH"), "C(=O)([O;D1;!-])[A]"},
            {"27", "nArCOOH", StringSelectorCore.getString("descriptors_fg_nArCOOH"), "C(=O)([O;D1;!-])[a]"},
            {"28", "nRCOOR", StringSelectorCore.getString("descriptors_fg_nRCOOR"), "O=[$([C;D2]),$([C;D3]C)][O;D2][C,c]"},
            {"29", "nArCOOR", StringSelectorCore.getString("descriptors_fg_nArCOOR"), "O=C([a])[O;D2][C,c]"},
            {"30", "nRCONH2", StringSelectorCore.getString("descriptors_fg_nRCONH2"), "[$([C;D3](=O)([N;D1;!+])[C;A]),$([C;D2](=O)[N;D1;!+])]"},
            {"31", "nArCONH2", StringSelectorCore.getString("descriptors_fg_nArCONH2"), "[$([C;D3](=O)([N;D1;!+])[a])]"},
            {"32", "nRCONHR", StringSelectorCore.getString("descriptors_fg_nRCONHR"), "[$([C;D2]),$([C;D3]C)](=O)[N;D2;!+][C,c;!$(C=O)]"},
            {"33", "nArCONHR", StringSelectorCore.getString("descriptors_fg_nArCONHR"), "C([a])(=O)[N;D2;!+][C,c;!$(C=O)]"},
            {"34", "nRCONR2", StringSelectorCore.getString("descriptors_fg_nRCONR2"), "O=[$([C;D2]),$([C;D3]C)][N;D3;!+]([C,c;!$(C=O)])[C,c;!$(C=O)]"},
            {"35", "nArCONR2", StringSelectorCore.getString("descriptors_fg_nArCONR2"), "O=C([a])[N;D3;!+]([C,c;!$(C=O)])[C,c;!$(C=O)]"},
            {"36", "nROCON", StringSelectorCore.getString("descriptors_fg_nROCON"), "C(=[O,S])([$([O;D1]),$([S;D1]),$([O;D2](C)[A]),$([S;D2](C)[A])])[$([N;D1]),$([N;D2](C)[A]),$([N;D3](C)([A])[A])]"},
            {"37", "nArOCON", StringSelectorCore.getString("descriptors_fg_nArOCON"), "[$(C(=[O,S])([O,S][a])N),$(C(=[O,S])([O,S])N[a])]"},
            {"38", "nRCOX", StringSelectorCore.getString("descriptors_fg_nRCOX"), "[$(C(=O)([Cl,Br,F,I])[a])]"},
            {"39", "nArCOX", StringSelectorCore.getString("descriptors_fg_nArCOX"), "[$(C(=O)([Cl,Br,F,I])[a])]"},
            {"40", "nRCSOH", StringSelectorCore.getString("descriptors_fg_nRCSOH"), "[$(C(=O)([S;D1])C),$(C(=S)([O;D1;!-])C)]"},
            {"41", "nArCSOH", StringSelectorCore.getString("descriptors_fg_nArCSOH"), "[$(C(=O)([S;D1])a),$(C(=S)([O;D1;!-])a)]"},
            {"42", "nRCSSH", StringSelectorCore.getString("descriptors_fg_nRCSSH"), "[$(C(=S)([SH])[A])]"},
            {"43", "nArCSSH", StringSelectorCore.getString("descriptors_fg_nArCSSH"), "[$(C(=S)([SH])[A])]"},
            {"44", "nRCOSR", StringSelectorCore.getString("descriptors_fg_nRCOSR"), "[$(C(=S)([O;D2][C,c])C),$([C;D2](=S)([O;D2][C,c])),$(C(=O)([S;D2][C,c])C),$([C;D2](=O)([S;D2][C,c]))]"},
            {"45", "nArCOSR", StringSelectorCore.getString("descriptors_fg_nArCOSR"), "[$(C(=S)([O;D2][C,c])[a]),$(C(=O)([S;D2][C,c])[a])]"},
            {"46", "nRCSSR", StringSelectorCore.getString("descriptors_fg_nRCSSR"), "[$(C(=S)([S;D2][C,c])C),$([C;D2](=S)([S;D2][C,c]))]"},
            {"47", "nArCSSR", StringSelectorCore.getString("descriptors_fg_nArCSSR"), "[$(C(=S)([S;D2][C,c])[a])]"},
            {"48", "nRCHO", StringSelectorCore.getString("descriptors_fg_nRCHO"), "[$([C;D2](=O)C)]"},
            {"49", "nArCHO", StringSelectorCore.getString("descriptors_fg_nArCHO"), "[$([C;D2](=O)[a])]"},
            {"50", "nRCO", StringSelectorCore.getString("descriptors_fg_nRCO"), "[C;D3](=O)([C])[C]"},
            {"51", "nArCO", StringSelectorCore.getString("descriptors_fg_nArCO"), "[$([C;D3](=O)([a])[C,c])]"},
            {"52", "nCONN", StringSelectorCore.getString("descriptors_fg_nCONN"), "C(=[O,S])([$([#7;D3;!+](*)(*)*),$([#7;D2;!+](*)*),$([#7;D1;!+])])[$([#7;D3;!+](*)(*)*),$([#7;D2;!+](*)*),$([#7;D1;!+])]"},
            {"53", "nC=O(OR)2", StringSelectorCore.getString("descriptors_fg_nCOOR2"), "C(=[O,S])([$([O,S]);D2])[$([O,S]);D2]"},
            {"54", "nN=C-N<", StringSelectorCore.getString("descriptors_fg_nNCN"), "[$([C;D2]),$([C;D3][C,c])](=[$([#7;D1]),$([#7;D2]);!+])[$([#7;D1]),$([#7;D2]),$([#7;D3]);!$([#7]=*);!+]"},
            {"55", "nC(=N)N2", StringSelectorCore.getString("descriptors_fg_nCNN2"), "C([$([N;D1]),$([N;D2]),$([N;D3]);!$(N=*);!+])([$([N;D1]),$([N;D2]),$([N;D3]);!$(N=*);!+])=[$([N;D1]),$([N;D2]);!+]"},
            {"56", "nRC=N", StringSelectorCore.getString("descriptors_fg_nRC_N"), "[$([N;D1;!+]),$([N;!+]C),$([N;!+][N;D2]=*)]=[$([C;D1]),$([C;D2]C),$([C;D3](C)C)]"},
            {"57", "nArC=N", StringSelectorCore.getString("descriptors_fg_nArC_N"), "[$([N;D1;!+]),$([N;!+]C),$([N;!+][N;D2]=*)]=[$([C;D2][a]),$([C;D3]([a])[C,c]),$([C;D3]([a])N=*)]"},
            {"58", "nRCNO", StringSelectorCore.getString("descriptors_fg_nRCNO"), "[$([C;D1]),$([C;D2]C),$([C;D3](C)C)]=[N;D2]O"},
            {"59", "nArCNO", StringSelectorCore.getString("descriptors_fg_nArCNO"), "[$([C;D2][a]),$([C;D3]([C,c])[a])]=[N;D2]O"},
            {"60", "nRNH2", StringSelectorCore.getString("descriptors_fg_nRNH2"), "[N;D1][$([C;A]);!$(C=[O,S])]"},
            {"61", "nArNH2", StringSelectorCore.getString("descriptors_fg_nArNH2"), "[N;D1][a]"},
            {"62", "nRNHR", StringSelectorCore.getString("descriptors_fg_nRNHR"), "[N;D2]([$([C;A]);!$(C=[O,S])])[$([C;A]);!$(C=[O,S])]"},
            {"63", "nArNHR", StringSelectorCore.getString("descriptors_fg_nArNHR"), "[N;D2]([a])[$([c,C]);!$(C=[O,S])]"},
            {"64", "nRNR2", StringSelectorCore.getString("descriptors_fg_nRNR2"), "[N;D3]([$([C;A]);!$(C=[O,S])])([$([C;A]);!$(C=[O,S])])[$([C;A]);!$(C=[O,S])]"},
            {"65", "nArNR2", StringSelectorCore.getString("descriptors_fg_nArNR2"), "[N;D3]([a])([$([C,c]);!$(C=[O,S])])[$([C,c]);!$(C=[O,S])]"},
            {"66", "nN-N", StringSelectorCore.getString("descriptors_fg_nNN"), "[$([N;D1]),$([N;D2][C,c]),$([N;D3]([C,c])[C,c])]-[$([N;D1]),$([N;D2][C,c]),$([N;D3]([C,c])[C,c])]"},
            {"67", "nN=N", StringSelectorCore.getString("descriptors_fg_nN_N"), "[$([N;D1]),$([N;D2][C,c])]=[$([N;D1]),$([N;D2][C,c])]"},
            {"68", "nRCN", StringSelectorCore.getString("descriptors_fg_nRCN"), "N#CC"},
            {"69", "nArCN", StringSelectorCore.getString("descriptors_fg_nArCN"), "N#C[a]"},
            {"70", "nN+", StringSelectorCore.getString("descriptors_fg_nN+"), "[$([#7+,#7++,#7+++]);!$([N+]([O-])=O)]"},
            {"71", "nNq", StringSelectorCore.getString("descriptors_fg_nNq"), "[N;D4]"},
            {"72", "nRNHO", StringSelectorCore.getString("descriptors_fg_nRNHO"), "[$([N;D1;!+]),$([N;D2;!+]([!a])[!a]),$([N;D3;!+]([!a])([!a])[!a])]O"},
            {"73", "nArNHO", StringSelectorCore.getString("descriptors_fg_nArNHO"), "[$([N;D2;!+]([*])[*]),$([N;D3;!+]([*])([*])[*])]([a])O"},
            {"74", "nRNNOx", StringSelectorCore.getString("descriptors_fg_nRNNOx"), "O=[N;!$([N+])]N([C,c])[C,c]"},
            {"75", "nArNNOx", StringSelectorCore.getString("descriptors_fg_nArNNOx"), "O=NN([A,a])a"},
            {"76", "nRNO", StringSelectorCore.getString("descriptors_fg_nRNO"), "[C][N;D2;!+;!++;!-;!--]=O"},
            {"77", "nArNO", StringSelectorCore.getString("descriptors_fg_nArNO"), "[a][N;D2;!+;!++;!-;!--]=O"},
            {"78", "nRNO2", StringSelectorCore.getString("descriptors_fg_nRNO2"), "[C][N+]([O-])=O"},
            {"79", "nArNO2", StringSelectorCore.getString("descriptors_fg_nArNO2"), "[a][N+]([O-])=O"},
            {"80", "nN(CO)2", StringSelectorCore.getString("descriptors_fg_nNCO2"), "[$([N;D2;!+]),$([N;D3](C)(C)[C,c])]([C;D3]=[O,S])[C;D3]=[O,S]"},
            {"81", "nC=N-N<", StringSelectorCore.getString("descriptors_fg_nCNN"), "N(=[$([C;D1]),$([C;D2][C,c]),$([C;D3]([C,c])[C,c])])[$([N;D1]),$([N;D2][C,c]),$([N;D3]([C,c])[C,c])]"},
            {"82", "nROH", StringSelectorCore.getString("descriptors_fg_nROH"), "[O;D1;!-]A"},
            {"83", "nArOH", StringSelectorCore.getString("descriptors_fg_nArOH"), "[O;D1;!-]a"},
            {"84", "nOHp", StringSelectorCore.getString("descriptors_fg_nOHp"), "[O;D1;!-][C;D2;H2][C,c]"},
            {"85", "nOHs", StringSelectorCore.getString("descriptors_fg_nOHs"), "[O;D1;!-][C;D3;H1]([C,c])[C,c]"},
            {"86", "nOHt", StringSelectorCore.getString("descriptors_fg_nOHt"), "[O;D1;!-][C;D4]([C,c])([C,c])[C,c]"},
            {"87", "nROR", StringSelectorCore.getString("descriptors_fg_nROR"), "[C;!$(C=[O,S]);!$(C#N)]O[C;!$(C=[O,S]);!$(C#N)]"},
            {"88", "nArOR", StringSelectorCore.getString("descriptors_fg_nArOR"), "[#6;!$(C=O);!$(C#N)]O[a]"},
            {"89", "nROX", StringSelectorCore.getString("descriptors_fg_nROX"), "[F,Cl,Br,I]O[A;!$(C=O);!$(C#N)]"},
            {"90", "nArOX", StringSelectorCore.getString("descriptors_fg_nArOX"), "[F,Cl,Br,I]O[a]"},
            {"91", "nO(C=O)2", StringSelectorCore.getString("descriptors_fg_nOCO2"), "[C;D3](=[O,S])O[C;D3]=[O,S]"},
            {"92", "nH2O", StringSelectorCore.getString("descriptors_fg_nH2O"), "[O;H2]"},
            {"93", "nSH", StringSelectorCore.getString("descriptors_fg_nSH"), "[S;D1][C;!$(C=*);!$(C#*)]"},
            {"94", "nC=S", StringSelectorCore.getString("descriptors_fg_nCS"), "[S;D1]=C(C)C"},
            {"95", "nRSR", StringSelectorCore.getString("descriptors_fg_nRSR"), "[#6;!$(C=O);!$(C=S);!$(C#*)][S;D2][#6;!$(C=O);!$(C=S);!$(C#*)]"},
            {"96", "nRSSR", StringSelectorCore.getString("descriptors_fg_nRSSR"), "[#6;!$(C=O);!$(C=S);!$(C#*)][S;D2][S;D2][#6;!$(C=O);!$(C=S);!$(C#*)]"},
            {"97", "nSO", StringSelectorCore.getString("descriptors_fg_nSO"), "[$([S;D3](=O)([C,c])[C,c]),$([S;D2](=O)C),$([S;D2](=O)S)]"},
            {"98", "nS(=O)2", StringSelectorCore.getString("descriptors_fg_nSO2b"), "[$([S;D4](=O)(=O)([*;!a])[*;!a]),$([S;D3](=O)(=O)=C),$([S;D3](=O)(=O)=S)]"},
            {"99", "nSOH", StringSelectorCore.getString("descriptors_fg_nSOH"), "[S;D2][O,S;D1]"},
            {"100", "nSOOH", StringSelectorCore.getString("descriptors_fg_nSOOH"), "[S;D3](=[O,S])[O,S;D1]"},
            {"101", "nSO2OH", StringSelectorCore.getString("descriptors_fg_nSO2OH"), "[S;D4](=[S,O])(=[S,O])([S,O;D1])[*;!S;!O]"},
            {"102", "nSO3OH", StringSelectorCore.getString("descriptors_fg_nSO3OH"), "[S,O;D1]S([S,O])(=[S,O])=[S,O]"},
            {"103", "nSO2", StringSelectorCore.getString("descriptors_fg_nSO2"), "[$([S;D3](=[S,O])[S,O;D2]),$([S;D3](=[S,O])([S,O;D2])[S,O;D2])]"},
            {"104", "nSO3", StringSelectorCore.getString("descriptors_fg_nSO3"), "[S;D4](=[S,O])(=[S,O])([S,O;D2])[*;!S;!O]"},
            {"105", "nSO4", StringSelectorCore.getString("descriptors_fg_nSO4"), "[S;D4](=[S,O])(=[S,O])([S,O;D2])[S,O;D2]"},
            {"106", "nSO2N", StringSelectorCore.getString("descriptors_fg_nSO2N"), "[$([S;D2]),$([S;D3]=[O,S]),$([S;D4](=[O,S])=O)]([C,c])[$([N;D1;!+]),$([N;D2;!+]([*])[*]),$([N;D3;!+]([*])([*])[*])]"},
            {"107", "nPO3", StringSelectorCore.getString("descriptors_fg_nPO3"), "[P;D3]([$([O,S][A,a])])([$([O,S][A,a])])[$([O,S])]"},
            {"108", "nPO4", StringSelectorCore.getString("descriptors_fg_nPO4"), "[P;D4](=[O,S])([$([O,S][A,a])])([$([O,S][A,a])])[$([O,S])]"},
            {"109", "nPR3", StringSelectorCore.getString("descriptors_fg_nPR3"), "[$([P;D3]([C,Cl,Br,F,I])([C,Cl,Br,F,I])[C,Cl,Br,F,I]),$([P;D2]([C,Cl,Br,F,I])[C,Cl,Br,F,I]),$([P;D1][C,Cl,Br,F,I])]"},
            {"110", "nP(=O)O2R", StringSelectorCore.getString("descriptors_fg_nP(O)O2R"), "[$([P;D3]),$([P;D4][C,c,F,Cl,Br,I])]([O,S])(=[O,S])[O,S]"},
            {"111", "nP(=O)R3/nPR5", StringSelectorCore.getString("descriptors_fg_nP(O)R3nPR5"), "[$([#6,F,Cl,Br,I]P([#6,F,Cl,Br,I])([#6,F,Cl,Br,I])=[O,S]),$([#6,F,Cl,Br,I][P;D3]([#6,F,Cl,Br,I])=[O,S]),$([#6,F,Cl,Br,I][P;D2]=[O,S]),$([P;D1]=[O,S])]"},
            {"112", "nCH2RX", StringSelectorCore.getString("descriptors_fg_nCH2RX"), "[C,c][C;D2;!R][Cl,Br,F,I]"},
            {"113", "nCHR2X", StringSelectorCore.getString("descriptors_fg_nCHR2X"), "[C,c][C;D3;!R;!R2;!$(C(@[*])@[*])]([C,c])[Cl,Br,F,I]"},
            {"114", "nCR3X", StringSelectorCore.getString("descriptors_fg_nCR3X"), "[C;D4;!R;!R2;!$(C(@[*])@[*])]([C,c])([C,c])([C,c])[Cl,Br,F,I]"},
            {"115", "nR=CHX", StringSelectorCore.getString("descriptors_fg_nR_CHX"), "C=[C;D2;!R][Cl,Br,F,I]"},
            {"116", "nR=CRX", StringSelectorCore.getString("descriptors_fg_nR_CRX"), "C=[C;!R;!R2;!$(C(@[*])@[*])]([C,c])[Cl,Br,F,I]"},
            {"117", "nR#CX", StringSelectorCore.getString("descriptors_fg_nR#CX"), "C#C[Cl,Br,F,I]"},
            {"118", "nCHRX2", StringSelectorCore.getString("descriptors_fg_nCHRX2"), "[C;D3;!R]([C,c])([Cl,Br,F,I])[Cl,Br,F,I]"},
            {"119", "nCR2X2", StringSelectorCore.getString("descriptors_fg_nCR2X2"), "[C;D4;!R;!R2;!$(C(@[*])@[*])]([C,c])([C,c])([Cl,Br,F,I])[Cl,Br,F,I]"},
            {"120", "nR=CX2", StringSelectorCore.getString("descriptors_fg_nR_CX2"), "C=[C;!R]([Cl,Br,F,I])[Cl,Br,F,I]"},
            {"121", "nCRX3", StringSelectorCore.getString("descriptors_fg_nCRX3"), "[C;D4;!R]([C,c])([Cl,Br,F,I])([Cl,Br,F,I])[Cl,Br,F,I]"},
            {"122", "nArX", StringSelectorCore.getString("descriptors_fg_nArX"), "a[Cl,Br,F,I]"},
            {"123", "nCXr", StringSelectorCore.getString("descriptors_fg_nCXr"), "[Cl,Br,F,I][$(C(@[*])@[*]);!$(C=*)]"},
            {"124", "nCXr=", StringSelectorCore.getString("descriptors_fg_nCXr_"), "[Cl,Br,F,I][$(C(@[*])@[*]);$(C=*)]"},
            {"125", "nCconjX", StringSelectorCore.getString("descriptors_fg_nCconjX"), "[Cl,Br,F,I][$(C(=[*])C=[*]),$(C=[*]C=[*]),$(C=[*][a])]"},
            {"126", "nAziridines", StringSelectorCore.getString("descriptors_fg_nAziridines"), "C1CN1"},
            {"127", "nOxiranes", StringSelectorCore.getString("descriptors_fg_nOxiranes"), "C1CO1"},
            {"128", "nThiranes", StringSelectorCore.getString("descriptors_fg_nThiranes"), "C1CS1"},
            {"129", "nAzetidines", StringSelectorCore.getString("descriptors_fg_nAzetidines"), "C1C[N;!$(N@C=O)]C1"},
            {"130", "nOxetanes", StringSelectorCore.getString("descriptors_fg_nOxetanes"), "C1COC1"},
            {"131", "nThioethanes", StringSelectorCore.getString("descriptors_fg_nThioethanes"), "C1CSC1"},
            {"132", "nBeta-Lactams", StringSelectorCore.getString("descriptors_fg_nBeta-Lactams"), "O=[$(C1CC2@*@*@*N12),$(C1CC2@*@*@*@*N12)]"},
            {"133", "nPyrrolidines", StringSelectorCore.getString("descriptors_fg_nPyrrolidines"), "C1CC[N;!$(N@C=O)]C1"},
            {"134", "nOxolanes", StringSelectorCore.getString("descriptors_fg_nOxolanes"), "C1CC[O;!$(O@C=O)]C1"},
            {"135", "nth-Thiophenes", StringSelectorCore.getString("descriptors_fg_nth-Thiophenes"), "C1CCSC1"},
            {"136", "nPyrroles", StringSelectorCore.getString("descriptors_fg_nPyrroles"), "n1cccc1"},
            {"137", "nPyrazoles", StringSelectorCore.getString("descriptors_fg_nPyrazoles"), "n1cccn1"},
            {"138", "nImidazoles", StringSelectorCore.getString("descriptors_fg_nImidazoles"), "n1ccnc1"},
            {"139", "nFuranes", StringSelectorCore.getString("descriptors_fg_nFuranes"), "c1ccoc1"},
            {"140", "nThiophenes", StringSelectorCore.getString("descriptors_fg_nThiophenes"), "c1ccsc1"},
            {"141", "nOxazoles", StringSelectorCore.getString("descriptors_fg_nOxazoles"), "c1cocn1"},
            {"142", "nIsoxazoles", StringSelectorCore.getString("descriptors_fg_nIsoxazoles"), "c1cnoc1"},
            {"143", "nThiazoles", StringSelectorCore.getString("descriptors_fg_nThiazoles"), "c1cscn1"},
            {"144", "nIsothiazoles", StringSelectorCore.getString("descriptors_fg_nIsothiazoles"), "c1cnsc1"},
            {"145", "nTriazoles", StringSelectorCore.getString("descriptors_fg_nTriazoles"), "[$(c1ncnn1),$(c1cnnn1)]"},
            {"146", "nPyridines", StringSelectorCore.getString("descriptors_fg_nPyridines"), "c1cc[$([nX3]),$([nX2])]cc1"},
            {"147", "nPyridazines", StringSelectorCore.getString("descriptors_fg_nPyridazines"), "c1ccnnc1"},
            {"148", "nPyrimidines", StringSelectorCore.getString("descriptors_fg_nPyrimidines"), "c1cncnc1"},
            {"149", "nPyrazines", StringSelectorCore.getString("descriptors_fg_nPyrazines"), "c1cnccn1"},
            {"150", "n135-Triazines", StringSelectorCore.getString("descriptors_fg_n135-Triazines"), "c1ncncn1"},
            {"151", "n124-Triazines", StringSelectorCore.getString("descriptors_fg_n124-Triazines"), "c1cnncn1"},
            {"152", "nHDon", StringSelectorCore.getString("descriptors_fg_nHDon"), ""}, // not defined by SMARTS
            {"153", "nHAcc", StringSelectorCore.getString("descriptors_fg_nHAcc"), ""} // not defined by SMARTS
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
                log.warn(String.format(StringSelectorCore.getString("descriptors_parsing_smarts_error"), FG_SMARTS[i][3]));
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
                    throw new Exception(String.format(StringSelectorCore.getString("descriptors_query_init_fail"), i));

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
                    log.warn(String.format(StringSelectorCore.getString("descriptors_fgquery_init_fail"), i+1, e.getMessage()));
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
            log.warn(String.format(StringSelectorCore.getString("descriptors_unable_calculate"), this.Name, e.getMessage()));
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
