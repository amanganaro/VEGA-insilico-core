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
import insilico.core.exception.PropertyNotFoundException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.molecule.tools.Depiction;
import java.util.ArrayList;

import org.openscience.cdk.isomorphism.Pattern;
import org.openscience.cdk.smarts.SmartsPattern;

/**
 *
 * @author User
 */
public class SAMicronucleusInVivo extends AlertBlockFromSMARTS implements iAlertBlock {
    
    public final static int SA_REL_HIGH = 1;
    public final static int SA_REL_MODERATE = 2;
    public final static int SA_REL_LOW = 3;
    public final static String INACTIVE = StringSelectorCore.getString("sa_inactive");
    public final static String ACTIVE = StringSelectorCore.getString("sa_active");
    
    private Pattern[] SA;
    
    final static Object[][] SMARTS = {
        {"c1ccc(F)cc1",SA_REL_HIGH,INACTIVE,1.0},
        {"n1nccc1",SA_REL_HIGH,INACTIVE,1.0},
        {"Oc1ccc(cc1Cl)",SA_REL_HIGH,INACTIVE,1.0},
        {"Nc1ccc(cc1)N(C)",SA_REL_HIGH,INACTIVE,1.0},
        {"c1ncnc(O)c1",SA_REL_HIGH,INACTIVE,1.0},
        {"O=NNC(=O)",SA_REL_HIGH,ACTIVE,1.0},
        {"N1CC1",SA_REL_HIGH,ACTIVE,1.0},
        {"N(CCCl)CCCl",SA_REL_HIGH,ACTIVE,1.0},
        {"COS(=O)(=O)C",SA_REL_HIGH,ACTIVE,1.0},
        {"N(=N)N",SA_REL_HIGH,ACTIVE,1.0},
        {"C(c1ccc(cc1)Cl)CC",SA_REL_HIGH,INACTIVE,1.0},
        {"O=S(=O)(N)c1ccccc1C",SA_REL_HIGH,INACTIVE,1.0},
        {"c2ncccc2Cl",SA_REL_HIGH,INACTIVE,1.0},
        {"NC(C)(C)C",SA_REL_HIGH,INACTIVE,1.0},
        {"O=C(O)CCCCCCCC",SA_REL_HIGH,INACTIVE,1.0},
        {"C(N(CC))S",SA_REL_HIGH,INACTIVE,1.0},
        {"NN(CCC)CC",SA_REL_HIGH,INACTIVE,1.0},
        {"C(c1ccccc1)c2ccc(O)cc2O",SA_REL_HIGH,INACTIVE,1.0},
        {"c1ccc(c(c1)[N+](=O)[O-])C",SA_REL_HIGH,INACTIVE,1.0},
        {"N1CNCC1",SA_REL_HIGH,INACTIVE,1.0},
        {"OCCN(CCO)CC",SA_REL_HIGH,INACTIVE,1.0},
        {"Oc1ccc(N)cc1N",SA_REL_HIGH,INACTIVE,1.0},
        {"n1ncnc1",SA_REL_MODERATE,INACTIVE,0.875},
        {"O=[N+]([O-])c1ccccc1N",SA_REL_MODERATE,INACTIVE,0.89},
        {"C(=O)COc3ccccc3",SA_REL_MODERATE,INACTIVE,0.89},
        {"N#C",SA_REL_MODERATE,INACTIVE,0.92},
        {"O=Cc1cnccc1",SA_REL_MODERATE,INACTIVE,0.88},
        {"Nc1ccc(N)cc1",SA_REL_MODERATE,INACTIVE,0.83},
        {"C([O-])",SA_REL_MODERATE,INACTIVE,0.88},
        {"C=CCCCC=C",SA_REL_MODERATE,INACTIVE,0.91},
        {"c1cccc(c1)C(O)CN",SA_REL_MODERATE,INACTIVE,0.89},
        {"N(C)S",SA_REL_MODERATE,INACTIVE,0.77},
        {"O=C(N)Nc1ccc(cc1)",SA_REL_MODERATE,INACTIVE,0.82},
        {"O=C(N(c1c(cccc1))CC)",SA_REL_MODERATE,INACTIVE,0.89},
        {"n1cccc(c1)C",SA_REL_MODERATE,INACTIVE,0.84},
        {"C(O)(C(OC))C(C)C",SA_REL_MODERATE,ACTIVE,1.00},
        {"SCCCC",SA_REL_MODERATE,INACTIVE,0.89},
        {"c1ccc(c(c1)C)S",SA_REL_MODERATE,INACTIVE,0.77},
        {"C(OP(=O)(OC))C",SA_REL_MODERATE,ACTIVE,0.86},
        {"O=Cc1ccccc1N",SA_REL_MODERATE,INACTIVE,0.78},
        {"C(O)C=Cc1ccccc1",SA_REL_MODERATE,INACTIVE,0.82},
        {"Oc1ccc(OC)cc1",SA_REL_MODERATE,INACTIVE,0.71},
        {"CC(C(=O))CCO",SA_REL_MODERATE,ACTIVE,1.00},
        {"CC(=C)Cl",SA_REL_MODERATE,INACTIVE,0.80},
        {"O=C(NC)CC",SA_REL_MODERATE,INACTIVE,0.74},
        {"C(C)CCC=C(C)C",SA_REL_MODERATE,INACTIVE,0.82},
        {"c1ccc(cc1)C(C)(C)C",SA_REL_MODERATE,INACTIVE,0.76},
        {"c1c(OC)cc(O)cc1C",SA_REL_MODERATE,ACTIVE,1.00},
        {"c1ccc(cc1)C(c2ccccc2)(C)",SA_REL_MODERATE,INACTIVE,0.73},
        {"O(c1cccc(N)c1)",SA_REL_MODERATE,INACTIVE,0.71},
        {"OCc1ccccc1",SA_REL_MODERATE,INACTIVE,0.67},
        {"PC",SA_REL_MODERATE,INACTIVE,0.86},
        {"O=S(=O)(O)c1ccccc1",SA_REL_MODERATE,INACTIVE,0.77},
        {"[O-][N+]",SA_REL_MODERATE,INACTIVE,0.63},
        {"O=C(O)c1ccccc1C",SA_REL_MODERATE,INACTIVE,0.64},
        {"O=CC(=C)CCCC",SA_REL_MODERATE,INACTIVE,0.79},
        {"c1cc(c(cc1)Cl)Cl",SA_REL_MODERATE,INACTIVE,0.67},
        {"C1N(C)CCC1",SA_REL_MODERATE,ACTIVE,0.79},
        {"CC(C)CC(O)CCCCC",SA_REL_MODERATE,ACTIVE,1.00},
        {"N=C(C)C",SA_REL_MODERATE,INACTIVE,0.73},
        {"C(Cl)Cl",SA_REL_MODERATE,INACTIVE,0.72},
        {"OC(=O)C=C",SA_REL_MODERATE,INACTIVE,0.70},
        {"O=C",SA_REL_MODERATE,INACTIVE,0.65},
        {"O=CN(C)C",SA_REL_MODERATE,INACTIVE,0.56},
        {"c2cc(ccc2O)C",SA_REL_MODERATE,INACTIVE,0.61},
        {"CC(O)c1ccccc1",SA_REL_MODERATE,INACTIVE,0.65},
        {"OC",SA_REL_MODERATE,INACTIVE,0.65},
        {"OCC(C)(C)CC",SA_REL_MODERATE,INACTIVE,0.65},
        {"OCC(O)C(O)C(O)CCO",SA_REL_MODERATE,INACTIVE,0.67},
        {"Cc2ccc(OC)cc2",SA_REL_MODERATE,INACTIVE,0.59},
        {"CCCC",SA_REL_MODERATE,INACTIVE,0.66},
        {"CCCCCC",SA_REL_MODERATE,INACTIVE,0.65},
        {"C(C)C",SA_REL_MODERATE,INACTIVE,0.66},
        {"O=CC=CCCC",SA_REL_MODERATE,INACTIVE,0.68},
        {"O1CC1C",SA_REL_MODERATE,ACTIVE,0.71},
        {"c1ccc2c(c1)c(c3ccccc3c2)",SA_REL_MODERATE,ACTIVE,0.63},
        {"O=P",SA_REL_MODERATE,ACTIVE,0.54},
        {"n1c[nH]cc1",SA_REL_MODERATE,ACTIVE,0.60},
        {"Nc1ccc(cc1)c2ccc(cc2)",SA_REL_MODERATE,ACTIVE,0.57},
        {"CC(O)C(O)CC",SA_REL_MODERATE,ACTIVE,0.61},
        {"Cn1cncc1",SA_REL_MODERATE,ACTIVE,0.61},
        {"NCC=C",SA_REL_MODERATE,ACTIVE,0.58},
        {"Nc1ccc(cc1)Cl",SA_REL_MODERATE,ACTIVE,0.71},
        {"CCN(CC)CC",SA_REL_MODERATE,ACTIVE,0.59},
        {"c1ccccc1",SA_REL_LOW,ACTIVE,0.51},
        {"NC(=S)",SA_REL_LOW,ACTIVE,0.45},
        {"NN",SA_REL_MODERATE,ACTIVE,0.58},
        {"C=CC(=C)",SA_REL_LOW,ACTIVE,1.00},
        {"SCC",SA_REL_LOW,ACTIVE,1.00}
    };
    
    
    
    public SAMicronucleusInVivo() throws InitFailureException {
        super(InsilicoConstants.SA_BLOCK_MICRONUCLEUS_INVIVO, StringSelectorCore.getString("sa_micronucleus_vivo_initialization"));
    }
    
    
    @Override
    protected void BuildSAList() throws InitFailureException {

        int idx = 0;
        
        for (int i=0; i<SMARTS.length; i++) {
            Alert curSA = new Alert(BlockIndex, AlertEncoding.BuildAlertId(BlockIndex, (idx+1)));
            curSA.setName(String.format(StringSelectorCore.getString("sa_micronucleus_vivo_name"), i+1, SMARTS[i][2]));
            curSA.setDescription(String.format(StringSelectorCore.getString("sa_micronucleus_vivo_description"),SMARTS[i][0],SMARTS[i][2]));
            curSA.setImageURL("/insilico/core/alerts/png/invivomicronucleus/MNVIVO_" + (idx+1) + ".png");
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_MICRONUCLEUS_ACTIVE, (SMARTS[i][2].equals(ACTIVE)));
            curSA.setBoolProperty(InsilicoConstants.KEY_ALERT_MICRONUCLEUS_INACTIVE, (SMARTS[i][2].equals(INACTIVE)));
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_MICRONUCLEUS_INVIVO_SA_BLOCK, (Integer)SMARTS[i][1]);
            curSA.setNumericProperty(InsilicoConstants.KEY_ALERT_VALUE_ACCURACY, (Double)SMARTS[i][3]);
            Alerts.add(curSA);
            idx++;
        }

    }
    
    
    @Override
    protected void InitSMARTS() throws InitFailureException {

        try {

            SA = new Pattern[SMARTS.length];
            
            for (int idx=0; idx<SMARTS.length; idx++) {
                SA[idx] = SmartsPattern.create((String)SMARTS[idx][0]).setPrepare(false);
            }
            
        } catch (Exception e) {
            throw new InitFailureException(StringSelectorCore.getString("sa_exception_smarts_initialization"));
        }    
    }

    
    @Override
    protected AlertList CalculateSAMatches() throws GenericFailureException {
        AlertList Res = new AlertList();
        
        // match and return ONLY the alerts from the highest reliable block
        
        boolean noMatches = true;
        int curBlock = -1;
        
        try {

            int nFragments = SMARTS.length;
            
            for (int i=0; i<nFragments; i++) 
                if ((SA[i].matches(CurMol.GetStructure()))) {
                    if (noMatches) {
                        noMatches = false;
                        try {
                            curBlock = (int)Alerts.get(i).getNumericProperty(InsilicoConstants.KEY_ALERT_MICRONUCLEUS_INVIVO_SA_BLOCK);
                        } catch (PropertyNotFoundException e) { throw new GenericFailureException(); }
                    } else {
                        try {
                            int curAlertBlock = (int)Alerts.get(i).getNumericProperty(InsilicoConstants.KEY_ALERT_MICRONUCLEUS_INVIVO_SA_BLOCK);
                            if (curAlertBlock > curBlock)
                                continue;
                        } catch (PropertyNotFoundException e) { throw new GenericFailureException(); }
                    }
                    Res.add((Alert)Alerts.get(i).clone());
                }
            
        } catch (CloneNotSupportedException | InvalidMoleculeException e) {
            return null;
        }
        
        return Res; 
    }

    public void SaveSmartsPNG() {

        ArrayList<String> list = new ArrayList<>();
        for (int idx=0; idx<SMARTS.length; idx++)
            list.add((String)SMARTS[idx][0]);
                
        int idx = 1;
        for (String s : list) {
            try {
                InsilicoMolecule mol = SmilesMolecule.Convert(s);
                Depiction.SaveImageAsPNG(Depiction.DepictMolecule(mol, 200, 200), "MNVIVO_" + (idx) + ".png");
            } catch (Exception e) {
                System.out.println(String.format(StringSelectorCore.getString("sa_save_smarts_error"), idx, s, e.getMessage()));
            }
            idx++;
        }
    }    
}