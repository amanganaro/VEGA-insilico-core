package insilico.core.ad;

import insilico.core.ad.item.ADIndexRange;
import insilico.core.exception.GenericFailureException;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.trainingset.iTrainingSet;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ADCheckDescriptorRange {
  
    public ADCheckDescriptorRange() {
        
    }
    
    public boolean Calculate(iTrainingSet ModelTrainingSet, double[] Descriptors,
                             InsilicoModelOutput output) {
        
        int nDesc = ModelTrainingSet.getDescriptorSize();
        if (nDesc != Descriptors.length)
            return false;
        
        int outDesc = 0;
        
        try {
            for (int i=0; i<nDesc; i++) {
                if (Math.round(Descriptors[i] * 100) < Math.round(ModelTrainingSet.getDescriptorMin(i) * 100)) {
                    outDesc++;
                    continue;
                }
                if (Math.round(Descriptors[i] * 100) > Math.round(ModelTrainingSet.getDescriptorMax(i) * 100)) {
                    outDesc++;
                }
            }
        } catch (GenericFailureException e) {
            return false;
        }
        
        ADIndexRange ADRange = new ADIndexRange();
        ADRange.setOutDescriptors(outDesc);
        output.addADIndex(ADRange);
        
        return true;
    }
}
