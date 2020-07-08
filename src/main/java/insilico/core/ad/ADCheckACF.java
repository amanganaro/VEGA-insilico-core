package insilico.core.ad;

import insilico.core.ad.item.ADIndexACF;
import insilico.core.ad.reasoning.ACFAnalysis;
import insilico.core.exception.GenericFailureException;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.acf.ACFItem;
import insilico.core.molecule.acf.ACFItemList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ADCheckACF {
    
    private static final int DEFAULT_FREQ_THRESHOLD = 2;
    
    private int FrequencyThreshold;
    private iTrainingSet TS;

    private ACFItemList ACFRare;
    private ACFItemList ACFMissing;
    

    public ADCheckACF(iTrainingSet ModelTrainingSet) {
        this(ModelTrainingSet, DEFAULT_FREQ_THRESHOLD);
    }
    
    
    public ADCheckACF(iTrainingSet ModelTrainingSet,
                      int RareFrequencyThreshold) {
        TS = ModelTrainingSet;
        FrequencyThreshold = RareFrequencyThreshold;
        ACFRare = new ACFItemList();
        ACFMissing = new ACFItemList();
    }
    
    
    /**
     * Note: also add the reasoning item
     * 
     * @param Mol
     * @param output
     * @return 
     */
    public boolean Calculate(InsilicoMolecule Mol, InsilicoModelOutput output) {
        
        if (!Mol.IsValid())
            return false;
        
        ACFItemList TSList;
        try {
            TSList = TS.getACF();
        } catch (GenericFailureException e) {
            return false;
        }
        
        for (ACFItem curACF : Mol.GetACF().getList()) {

            int Freq = 0;
            
            for (ACFItem TSACF : TSList.getList()) {
                if (curACF.getACF().equals(TSACF.getACF())) {
                    Freq = TSACF.getFrequency();
                    break;
                }
            }
            
            if (Freq == 0)
                getACFMissing().AddItem(curACF);
            else if (Freq <= FrequencyThreshold)
                getACFRare().AddItem(curACF);
            
        }   
        
        ADIndexACF ADACF = new ADIndexACF();
        ADACF.SetACF(getACFRare().size(), getACFMissing().size());
        output.addADIndex(ADACF);
        
        if (ADACF.GetIndexValue() < 1) {
            ACFAnalysis ACFAn = new ACFAnalysis(getACFRare(), getACFMissing());
            output.addReasoningItem(ACFAn);
        }
        
        return true;
    }

    /**
     * @return the ACFRare
     */
    public ACFItemList getACFRare() {
        return ACFRare;
    }

    /**
     * @return the ACFMissing
     */
    public ACFItemList getACFMissing() {
        return ACFMissing;
    }
    
    
}
