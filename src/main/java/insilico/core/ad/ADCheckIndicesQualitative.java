package insilico.core.ad;

import insilico.core.ad.item.ADIndexAccuracy;
import insilico.core.ad.item.ADIndexConcordance;
import insilico.core.ad.item.ADIndexSimilarity;
import insilico.core.descriptor.Descriptor;
import insilico.core.exception.GenericFailureException;
import insilico.core.model.InsilicoModelOutput;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.similarity.SimilarMolecule;
import insilico.core.tools.utils.logger.InsilicoLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class ADCheckIndicesQualitative extends ADCheckIndices {

    Logger logger = LoggerFactory.getLogger(ADCheckIndicesQualitative.class);

    protected double IndexSimilarity;
    protected double IndexAccuracy;
    protected double IndexConcordance;
    protected double IndexADI;
    
    private boolean UseMapping;
    
    private final ArrayList<Double> MappingPos;
    private final ArrayList<Double> MappingNeg;
    private final int VALUE_POS = 1;
    private final int VALUE_NEG = 0;
    private final int VALUE_NA = -1;

    
    public ADCheckIndicesQualitative(iTrainingSet ModelTrainingSet, boolean UseValueMapping) {
        super(ModelTrainingSet);
        UseMapping = UseValueMapping;
        MappingPos = new ArrayList<>();
        MappingNeg = new ArrayList<>();
    }
    
    
    public ADCheckIndicesQualitative(iTrainingSet ModelTrainingSet) {
        this(ModelTrainingSet, true);
    }
    
    
    public void AddMappingToPositiveValue(double Value) {
        this.MappingPos.add(Value);
    }
    
    
    public void AddMappingToNegativeValue(double Value) {
        this.MappingNeg.add(Value);
    }
    
    
    private int MapValue(double Value) {
        
        // If no mapping, just return the int value
        if (!UseMapping)
            return (int) Value;
        
        for (Double d : MappingPos)
            if (d == Value)
                return VALUE_POS;
        for (Double d : MappingNeg)
            if (d == Value)
                return VALUE_NEG;
        return VALUE_NA;
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
                output.setExperimental(TrainSet.getExperimentalValue((int)MostSimilar[0].getIndex()));
                output.setExperimentalFormatted(TrainSet.getClassLabel(TrainSet.getExperimentalValue((int)MostSimilar[0].getIndex())));

            }
            
            // Return the list of most similar molecules
            return MostSimilar;
            
        } catch (CloneNotSupportedException e) {
            logger.warn("Error in AD: calculation of similarity for molecule " + Mol.GetSMILES() + " - " + e.getMessage());
            throw new GenericFailureException("Unable to calculate similarity - " + e.getMessage());
        }
    }
    
    
    public boolean Calculate(InsilicoMolecule Mol, InsilicoModelOutput output) {
        
        double Prediction = output.getMainResultValue();
        
        // Preliminary check
        if ((!Mol.IsValid()) || (Prediction == Descriptor.MISSING_VALUE))
            return false;
        
        try {
            
            // Init similarity and get list of most similar compounds
            SimilarMolecule[] MostSimilar = SetSimilarMolecules(Mol, output);
           
            
            // Calculate basic indices for quantitative model
            
            if (MostSimilar[0].getSimilarity() == 1) {
                
                // If exact compound is found, stats are fixed
                
                int CurExp = MapValue(TrainSet.getExperimentalValue((int)MostSimilar[0].getIndex()));
                int CurPred = MapValue(TrainSet.getPredictedValue((int)MostSimilar[0].getIndex()));
                
                IndexSimilarity = 1;
                
                if (CurExp == CurPred) 
                    IndexAccuracy = 1;
                else
                    IndexAccuracy = 0;
                
                if (CurExp == MapValue(Prediction)) 
                    IndexConcordance = 1;
                else
                    IndexConcordance = 0;
                
                // ADI index - for now just equal to concordance
                IndexADI = IndexConcordance;
                
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

                Prediction = MapValue(Prediction);

                double SimDenom = 0;
                for (int i=0; i<MoleculesForIndexSize; i++) {

                    int CurExp = MapValue(TrainSet.getExperimentalValue((int)MostSimilar[i].getIndex()));
                    int CurPred = MapValue(TrainSet.getPredictedValue((int)MostSimilar[i].getIndex()));

                    SimDenom += (1 + Math.log(MostSimilar[i].getSimilarity()));
                    if (CurExp == CurPred) {
                        IndexAccuracy += (1 + Math.log(MostSimilar[i].getSimilarity()));
                    }
                    if (CurExp == Prediction)
                        IndexConcordance += (1 + Math.log(MostSimilar[i].getSimilarity()));
                    
                }

                // Accuracy: mean of prediction error for similar compounds
                IndexAccuracy = IndexAccuracy / SimDenom;

                // Concordance: mean of difference from experimental value of similar compounds
                IndexConcordance = IndexConcordance / SimDenom;

                // ADI index: weighted mean of previous indices
                IndexADI = 
                    Math.pow(IndexSimilarity, 0.5) * Math.pow(IndexAccuracy, 0.25) * Math.pow(IndexConcordance, 0.25);
                        
            }
            
        } catch (Throwable e) {
            return false;
        }

        // Stores resulting object (indices) in the results list
        ADIndexSimilarity ADSim = new ADIndexSimilarity();
        ADSim.SetIndexValue(IndexSimilarity);
        output.addADIndex(ADSim);
        
        ADIndexAccuracy ADAcc = new ADIndexAccuracy();
        ADAcc.setQuantitativeMode(false);
        ADAcc.SetIndexValue(IndexAccuracy);
        output.addADIndex(ADAcc);
        
        ADIndexConcordance ADConc = new ADIndexConcordance();
        ADConc.setQuantitativeMode(false);
        ADConc.SetIndexValue(IndexConcordance);
        output.addADIndex(ADConc);
        
        return true;
    }

    
    /**
     * @return the IndexADI
     */
    public double getIndexADI() {
        return IndexADI;
    }
    
}
