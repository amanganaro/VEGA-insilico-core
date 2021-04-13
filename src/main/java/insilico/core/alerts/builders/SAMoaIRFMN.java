package insilico.core.alerts.builders;

import insilico.core.exception.InitFailureException;
import insilico.core.alerts.AlertBlockFromSMARTS;
import insilico.core.alerts.Alert;
import insilico.core.alerts.AlertList;
import insilico.core.alerts.AlertEncoding;
import insilico.core.alerts.iAlertBlock;
import insilico.core.constant.InsilicoConstants;
import insilico.core.exception.GenericFailureException;
import insilico.core.exception.InvalidMoleculeException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import java.util.HashMap;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.isomorphism.matchers.QueryAtomContainer;
import org.openscience.cdk.smarts.SmartsPattern;
import org.openscience.cdk.smiles.smarts.parser.SMARTSParser;

/**
 *
 * @author User
 */
public class SAMoaIRFMN extends AlertBlockFromSMARTS implements iAlertBlock {
    
    public final HashMap<String, String> MoAList;
    
    private Pattern[] SA;
    private int nRules;

    public final static int IDX_SMARTS = 0;
    public final static int IDX_GROUP = 1;
    public final static int IDX_MODE = 2;
    
    public final static String[][] Rules = {
        {"[C;!R](=O)(NC)O[a,N]", StringSelectorCore.getString("sa_moa_name_1"), StringSelectorCore.getString("sa_moa_description_1")},
        {"O(C(=O)[N]a1aa[a;H1](aa1))C", StringSelectorCore.getString("sa_moa_name_2"), StringSelectorCore.getString("sa_moa_description_2")}, // FIXED: N to [N] and explicit H
        {"N(C(CCl)=O)([CH2]*)a(aC)(aC)", StringSelectorCore.getString("sa_moa_name_3"), StringSelectorCore.getString("sa_moa_description_3")},
        {"C1(C(-,=C(CC(C1)*)-,=O)-,=C(-,=NO*)*)=O", StringSelectorCore.getString("sa_moa_name_4"), StringSelectorCore.getString("sa_moa_description_4")},
        {"c1(c(c(c(c(c1)[CH2,$(S(=O)(=O)*),$(C(F)(F)F)]))N(~O)~O)N[CH2])N(~O)~O", StringSelectorCore.getString("sa_moa_name_5"), StringSelectorCore.getString("sa_moa_description_5")},
        {"c1(c(c(c(cc1)N(~O)~O))N(~O)~O)O", StringSelectorCore.getString("sa_moa_name_6"), StringSelectorCore.getString("sa_moa_description_6")},
        {"c1(c(c(c(cc1[CH2])N(~O)~O)[$([H]),$(C(C)C)])N(~O)~O)O", StringSelectorCore.getString("sa_moa_name_7"), StringSelectorCore.getString("sa_moa_description_7")},
        {"C(-,=[NH1][NH1]*)(-,=O)O*", StringSelectorCore.getString("sa_moa_name_8"), StringSelectorCore.getString("sa_moa_description_8")},
        {"c1cn(cn1)CC(c2c(cc(cc2)Cl)Cl)O*", StringSelectorCore.getString("sa_moa_name_9"), StringSelectorCore.getString("sa_moa_description_9")},
        {"c1cn(cn1)[C,$(CC)](c2c(cc(cc2)[Cl,$([H])])Cl)[O,S]*", StringSelectorCore.getString("sa_moa_name_10"), StringSelectorCore.getString("sa_moa_description_10")},
        {"c1[c,n]n(cn1)[C,$(CC)](c2c(cc(cc2)[Cl,F,$([H])])[Cl,F])[O,S]*", StringSelectorCore.getString("sa_moa_name_11"), StringSelectorCore.getString("sa_moa_description_11")},
        {"c1[c,n]n(cn1)CC(c2c(cc(cc2)[Cl,F])[Cl,F])[O,S]*", StringSelectorCore.getString("sa_moa_name_12"), StringSelectorCore.getString("sa_moa_description_12")},
        {"C12(CCCCC1)OCC(O2)CN", StringSelectorCore.getString("sa_moa_name_13"), StringSelectorCore.getString("sa_moa_description_13")},
        {"C1C([C,O]C(CN1[C;!R][C;!R][C;!R]))", StringSelectorCore.getString("sa_moa_name_14"), StringSelectorCore.getString("sa_moa_description_14")},
        {"C(-,=[N,C][$(C#N),$(N(~O)~O)])(-,=NC[R])[N,S,C]", StringSelectorCore.getString("sa_moa_name_15"), StringSelectorCore.getString("sa_moa_description_15")},
        {"C12(C(-,=C(C(C(C1C)C)(C2(Cl)Cl)Cl)Cl)Cl)Cl", StringSelectorCore.getString("sa_moa_name_16"), StringSelectorCore.getString("sa_moa_description_16")},
        {"[$(c1(ccc([Cl])cc1)),$(c1(ccc(OC)cc1)),$(c1(c([Cl])cccc1)),$(c1(c(OC)cccc1))]C([$(c1(ccc([Cl])cc1)),$(c1(ccc(OC)cc1)),$(c1(c([Cl])cccc1)),$(c1(c(OC)cccc1))])-,=C(Cl)Cl", StringSelectorCore.getString("sa_moa_name_17"), StringSelectorCore.getString("sa_moa_description_17")}, // FIXED: problems with nested $[...]
        {"C12(C(-,=C(C(C(C1-,=C)-,=C)(C2(Cl)Cl)Cl)Cl)Cl)Cl", StringSelectorCore.getString("sa_moa_name_18"), StringSelectorCore.getString("sa_moa_description_18")},
        {"[R]1([R]([R]([R]([R]([R]1Cl)Cl)Cl)Cl)Cl)Cl", StringSelectorCore.getString("sa_moa_name_19"), StringSelectorCore.getString("sa_moa_description_19")},
        {"[P](=[O,S])([O,S][C,c])([O,S][C,c])[O,S,N,C,c]", StringSelectorCore.getString("sa_moa_name_20"), StringSelectorCore.getString("sa_moa_description_20")},
        {"N(C(=O)N([H1,C])(c1ccc([Cl,Br,O,$(C(F)(F)F)])cc1))(N=C([R,C])[$(c1ccc([Cl,Br,O,$(C(F)(F)F)])cc1),$(c1cc([Cl,Br,O,$(C(F)(F)F)])ccc1)])[H1,R]", StringSelectorCore.getString("sa_moa_name_20bis"), StringSelectorCore.getString("sa_moa_description_20bis")},
        {"c1(c(cc(cc1)Cl)[F,Cl,a])O[$(CC(=O)O),$(CCC(=O)O),$(CCCC(=O)O)]", StringSelectorCore.getString("sa_moa_name_21"), StringSelectorCore.getString("sa_moa_description_21")},
        {"c1(nc(c(c(c1)[NH2])Cl)[$(C(=O)O),$(OCC(=O)O)])[CH3,Cl]", StringSelectorCore.getString("sa_moa_name_22"), StringSelectorCore.getString("sa_moa_description_22")},
        {"c1(c(ccc(c1[Cl,$(O[CH3]),$([NH2])])Cl)Cl)C(=O)O", StringSelectorCore.getString("sa_moa_name_23"), StringSelectorCore.getString("sa_moa_description_23")},
        {"c1([n,c]cccc1)a[$(C(=O)O),$(CC(=O)O)]", StringSelectorCore.getString("sa_moa_name_24"), StringSelectorCore.getString("sa_moa_description_24")},
        {"[$(C#N),$(C(O)=O)]c1nn(c(N)c1SC(F)(F)F)c2c(cc(cc2Cl)C(F)(F)F)Cl", StringSelectorCore.getString("sa_moa_name_25"), StringSelectorCore.getString("sa_moa_description_25")},
        {"[P](=S)(OC)(OC)S[C,c]", StringSelectorCore.getString("sa_moa_name_26"), StringSelectorCore.getString("sa_moa_description_26")},
        {"c1([c,n]n([c,n]c1[$(C(N[a])=O),$(C(NC[a])=O),$([CH1]=N[a,O])]))[Br,Cl,C]", StringSelectorCore.getString("sa_moa_name_27"), StringSelectorCore.getString("sa_moa_description_27")}, // FIX problems with nested $[...]
        {"O=C(OC1C(=C(C(=O)C1)CC=CC)C)C2C(C=C(C)C)C2(C)C", StringSelectorCore.getString("sa_moa_name_28"), StringSelectorCore.getString("sa_moa_description_28")},
        {"O(C(=O)C1C(C-,=C)C1(C)C)[$(C-a),$(C[R]1[R]=,-[R]=,-[R][R]1),$([R]1[R]=,-[R][R][R]1)]", StringSelectorCore.getString("sa_moa_name_29"), StringSelectorCore.getString("sa_moa_description_29")},
        {"C(OC[$(C(C)C),$(CC(C)C)](c1ccc(cc1)[!#1]))a", StringSelectorCore.getString("sa_moa_name_30"), StringSelectorCore.getString("sa_moa_description_30")},
        {"[C,c]N1N=CC([O,N,S,a])=CC1=O", StringSelectorCore.getString("sa_moa_name_31"), StringSelectorCore.getString("sa_moa_description_31")},
        {"c1(cncnc1)C(O)(c2ccc([F,Cl])cc2)c3ccccc3", StringSelectorCore.getString("sa_moa_name_32"), StringSelectorCore.getString("sa_moa_description_32")},
        {"[nr5]c[c,$(C=O)]cccCl", StringSelectorCore.getString("sa_moa_name_33"), StringSelectorCore.getString("sa_moa_description_33")},
        {"[S](=O)(=O)(NC(=O)Nc1nc([!#1])[n,c]c(n1)[!#1])a", StringSelectorCore.getString("sa_moa_name_34"), StringSelectorCore.getString("sa_moa_description_34")},
        {"[$(C#N),$([S,C]=O)]N=[S](=O)(C)[$([N,a]),$(C[N,a]),$(CC[N,a])]", StringSelectorCore.getString("sa_moa_name_35"), StringSelectorCore.getString("sa_moa_description_35")},
        {"O=[S](=O)(N-a)*", StringSelectorCore.getString("sa_moa_name_36"), StringSelectorCore.getString("sa_moa_description_36")},
        {"O=C(N(CC)CC)SC[C,c]", StringSelectorCore.getString("sa_moa_name_37"), StringSelectorCore.getString("sa_moa_description_37")},
        {"n1c(nc(nc1NC-C)[$(SC),$(OC),Cl])NC-C", StringSelectorCore.getString("sa_moa_name_38"), StringSelectorCore.getString("sa_moa_description_38")},
        {"C(~[$(C),$(CC),$(CCC)]c1c(cc(cc1)[Cl,F])[Cl,F,$([H])])[n]2ncnc2", StringSelectorCore.getString("sa_moa_name_39"), StringSelectorCore.getString("sa_moa_description_39")},
        {"O=[C;!R](NC)N-a", StringSelectorCore.getString("sa_moa_name_40"), StringSelectorCore.getString("sa_moa_description_40")}
    };

    public SAMoaIRFMN() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MOA_IRFMN, StringSelectorCore.getString("sa_moa_irfmn_init"));

        // build public hasmap of MoA association with alerts
        MoAList = new HashMap<>();
        for (int i=0; i<nRules; i++) {
            String id = AlertEncoding.BuildAlertId(BlockIndex, (i+1));
            String moa = Rules[i][2] + String.format(StringSelectorCore.getString("sa_moa_chem_group"), Rules[i][1]);
            MoAList.put(id, moa);
        }
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        nRules = Rules.length;
        
        for (int i=0; i<nRules; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (i+1)));
            curSA.setName(StringSelectorCore.getString("sa_moa_alert_no") + (i+1));
            curSA.setDescription(String.format(StringSelectorCore.getString("sa_moa_alert_description"),Rules[i][IDX_MODE], Rules[i][IDX_GROUP], Rules[i][IDX_SMARTS]  ));
            curSA.setImageURL("/insilico/core/alerts/png/moairfmn/MOAMN_" + (i+1) + ".png");
            curSA.setNumericProperty("id", i);
            Alerts.add(curSA);
        }        
    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new Pattern[nRules];
            for (int i=0; i<nRules; i++) {
                String s = Rules[i][0]; 
                SA[i] = SmartsPattern.create(s, DefaultChemObjectBuilder.getInstance()).setPrepare(false);
            }
            
        } catch (Exception e) {
            throw new InitFailureException(StringSelectorCore.getString("sa_exception_smarts_initialization"));
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        try {

            for (int i=0; i<nRules; i++) {
                int matches = SA[i].matchAll(CurMol.GetStructure()).countUnique();
                if (matches > 0)
                    Res.add((Alert)Alerts.get(i).clone());
            }
                        
        } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            return null;
        }
        
        return Res; 
    }


    public void SaveSmartsPNG() {
    
        int idx = 1;

        for (int i=0; i<nRules; i++) {
            String s = Rules[i][0];
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "MOAMN_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }
    }
    
}