package insilico.core.ad;

import insilico.core.ad.item.ADIndexAccuracy;
import insilico.core.ad.item.ADIndexConcordance;
import insilico.core.ad.item.ADIndexMaxError;
import insilico.core.ad.item.ADIndexSimilarity;
import insilico.core.descriptor.Descriptor;
import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelector;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.similarity.SimilarMolecule;
import insilico.core.tools.utils.logger.InsilicoLogger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
@Slf4j
public class ADCheckIndicesQuantitative extends ADCheckIndices {


    protected double IndexSimilarity;
    protected double IndexAccuracy;
    protected double IndexConcordance;
    protected double IndexMaxErr;
    protected double IndexADI;

    
    public ADCheckIndicesQuantitative(iTrainingSet ModelTrainingSet) {
        super(ModelTrainingSet);
    }
    
    
    /**
     * Initialize similarity values, calculate similarity of all training set
     * against the given molecule, set the most similar molecules in the 
     * output object and return the list (for further use inside AD objects).
     * 
     * @param Mol
     * @param output
     * @return
     * @throws GenericFailureException 
     */
    public SimilarMolecule[] SetSimilarMolecules(InsilicoMolecule Mol, InsilicoModelOutput output)
        throws GenericFailureException {
        
        try {
            
            // Calculate similarity values with current Training set
            CalculateSimilarity(Mol);


            // Find most similar compounds to be shown 
            // (both from training and test set)
            int MaxSimMolecules = this.MoleculesToShowSize;
            if (this.MoleculesForIndexSize > MaxSimMolecules)
                MaxSimMolecules = this.MoleculesForIndexSize;

            SimilarMolecule[] MostSimilar = GetMostSimilarMolecules(MaxSimMolecules);

            // Set similar molecules in the output object
            for (int i=0; i<MoleculesToShowSize; i++)
                output.addSimilarMolecule((SimilarMolecule)MostSimilar[i].clone());        

            // Sets experimental value
            if (MostSimilar[0].getSimilarity() == 1) {
                double CurExp = TrainSet.getExperimentalValue((int)MostSimilar[0].getIndex());
                output.setExperimental(CurExp);
                output.setExperimentalFormatted(TrainSet.FormatValue(CurExp));
            }
            
            // Return the list of most similar molecules
            return MostSimilar;
            
        } catch (CloneNotSupportedException e) {
            log.warn(String.format(StringSelector.getString("ad_check_qualitative_warn"), Mol.GetSMILES(), e.getMessage()));
            throw new GenericFailureException(String.format(StringSelector.getString("ad_check_qualitative_exception"), e.getMessage()));
        }
    }
    
    
    public boolean Calculate(InsilicoMolecule Mol, InsilicoModelOutput output) {
        
        double Prediction = output.getMainResultValue();
        
        // Preliminary check
        if ((!Mol.IsValid()) || (Prediction == Descriptor.MISSING_VALUE))
            return false;
        
        boolean MisValInAllPredictions = false;
            
        try {

            // Init similarity and get list of most similar compounds
            SimilarMolecule[] MostSimilar = SetSimilarMolecules(Mol, output);
           
            // Calculate basic indices for quantitative model
            
            if (MostSimilar[0].getSimilarity() == 1) {
                
                // If exact compound is found, stats are fixed
                
                double CurExp = TrainSet.getExperimentalValue((int)MostSimilar[0].getIndex());
                double CurPred = TrainSet.getPredictedValue((int)MostSimilar[0].getIndex());
                if (CurPred == Descriptor.MISSING_VALUE)
                    MisValInAllPredictions = true;
                
                // Similarity:
                IndexSimilarity = 1;

                // Accuracy: difference between maching molecule prediction and experimental
                IndexAccuracy = Math.abs(CurExp - CurPred);

                // Concordance: difference between matching molecule's experimental and current prediction
                IndexConcordance = Math.abs(CurExp - Prediction);

                // Max Err:
                IndexMaxErr = Math.abs(CurExp - CurPred);

                // ADI index - for now just equal to similarity index
                IndexADI = IndexSimilarity;
                
            } else {
                
                // Similarity:
                IndexSimilarity = 0;
                for (int i=0; i<MoleculesForIndexSize; i++)
                    IndexSimilarity += MostSimilar[i].getSimilarity();
                IndexSimilarity = IndexSimilarity/(double)MoleculesForIndexSize;
                
                // Similarity is adjusted with diameter
                double Diam = MostSimilar[0].getSimilarity() - MostSimilar[MoleculesForIndexSize-1].getSimilarity();
                IndexSimilarity = IndexSimilarity * (1 - (Math.pow(Diam, 2)));

                IndexAccuracy = 0;
                IndexConcordance = 0;
                IndexADI = 0;
                IndexMaxErr = 0;

                double ErrIndicesSum=0; double DiffIndicesSum=0;
                int ValidPredictions=0;

                for (int i=0; i<MoleculesForIndexSize; i++) {

                    double CurExp = TrainSet.getExperimentalValue((int)MostSimilar[i].getIndex());
                    double CurPred = TrainSet.getPredictedValue((int)MostSimilar[i].getIndex());
                    
                    if (CurPred != Descriptor.MISSING_VALUE) {
                        ValidPredictions++;
                        
                        double ErrAbs = Math.abs(CurExp - CurPred);
                        ErrIndicesSum += ErrAbs;

                        if (ErrAbs>IndexMaxErr)
                            IndexMaxErr = ErrAbs;
                    }

                    double DiffAbs = Math.abs(CurExp - Prediction);
                    DiffIndicesSum += DiffAbs;

                }

                // Accuracy: mean of prediction error for similar compounds
                if (ValidPredictions > 0)
                    IndexAccuracy = ErrIndicesSum / (double)ValidPredictions;

                // Concordance: mean of difference from experimental value of similar compounds
                IndexConcordance = DiffIndicesSum / MoleculesForIndexSize;

                // ADI index - for now just equal to similarity index
                IndexADI = IndexSimilarity;

                if (ValidPredictions == 0)
                    MisValInAllPredictions = true;

            }
            
        } catch (Throwable e) {
            return false;
        }

        // Stores resulting object (indices) in the results list
        ADIndexSimilarity ADSim = new ADIndexSimilarity();
        ADSim.SetIndexValue(IndexSimilarity);
        output.addADIndex(ADSim);
        
        ADIndexAccuracy ADAcc = new ADIndexAccuracy();
        ADAcc.setQuantitativeMode(true);
        if (!MisValInAllPredictions)
            ADAcc.SetIndexValue(IndexAccuracy);
        else
            ADAcc.SetAllMissingValues();
        output.addADIndex(ADAcc);
        
        ADIndexConcordance ADConc = new ADIndexConcordance();
        ADConc.setQuantitativeMode(true);
        ADConc.SetIndexValue(IndexConcordance);
        output.addADIndex(ADConc);
        
        ADIndexMaxError ADErr = new ADIndexMaxError();
        if (!MisValInAllPredictions)
            ADErr.SetIndexValue(IndexMaxErr);
        else
            ADErr.SetAllMissingValues();
        output.addADIndex(ADErr);
        
        return true;
    }

    /**
     * @return the IndexADI
     */
    public double getIndexADI() {
        return IndexADI;
    }
}
