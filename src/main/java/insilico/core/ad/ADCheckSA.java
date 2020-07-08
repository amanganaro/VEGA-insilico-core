package insilico.core.ad;

import insilico.core.alerts.Alert;
import insilico.core.alerts.AlertEncoding;
import insilico.core.alerts.AlertList;
import insilico.core.exception.GenericFailureException;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.similarity.SimilarMolecule;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ADCheckSA {
    
    private final iTrainingSet TS;
    private int MoleculesToShowSize;

    
    public ADCheckSA(iTrainingSet ModelTrainingSet) {
        TS = ModelTrainingSet;
        MoleculesToShowSize = 3; // Default
    }
    

    /**
     * Takes as input the filtered list of alerts to be
     * added (along with the similar molecules).
     * 
     * @param Mol
     * @param FoundSAs
     * @param output
     * @param TSSimilarMols
     * @return 
     */
    public boolean Calculate(InsilicoMolecule Mol, AlertList FoundSAs,
                             InsilicoModelOutput output, SimilarMolecule[] TSSimilarMols) {
        
        if (!Mol.IsValid())
            return false;

        // If no alerts for the given class are found, just returns
        if (FoundSAs.size() == 0)
            return true;
        
        try {
            
            for (Alert CurAlert : FoundSAs.getSAList()) {

                // Retrieve and add similar molecules to SA
                int mols = 0;
                for (int i=0;  i<TSSimilarMols.length; i++) {

                    String TSMolAlerts = TS.getAlerts((int)TSSimilarMols[i].getIndex());
                    if (AlertEncoding.ContainsAlert(TSMolAlerts, CurAlert.getId())) {
                        CurAlert.AddSimilarMolecule(TSSimilarMols[i]);
                        mols++;
                    }

                    if (mols == MoleculesToShowSize)
                        break;
                }

            }

            output.setSAList(FoundSAs);
            
        } catch (GenericFailureException e) {
            return false;
        }
        
        return true;
    }    
    
   
    /**
     * @return the MoleculesToShowSize
     */
    public int getMoleculesToShowSize() {
        return MoleculesToShowSize;
    }

    /**
     * @param MoleculesToShowSize the MoleculesToShowSize to set
     */
    public void setMoleculesToShowSize(int MoleculesToShowSize) {
        this.MoleculesToShowSize = MoleculesToShowSize;
    }
    
    
}
