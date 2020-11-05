package insilico.core.knn;

import insilico.core.exception.GenericFailureException;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.similarity.SimilarMolecule;
import insilico.core.similarity.Similarity;
import insilico.core.similarity.SimilarityDescriptorsBuilder;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Absract class
 * @author Alberto Manganaro <a.manganaro@kode-solutions.net>
 */
public abstract class InsilicoKnn {

    Logger logger = LoggerFactory.getLogger(InsilicoKnn.class);

    protected int NeighboursNumber;
    protected double MinSimilarity;
    protected double MinSimilarityForSingleResult;
    protected boolean UseExperimentalRange;
    protected double ExperimentalRange;
    protected int EnhanceWeightFactor;

    protected final SimilarityDescriptorsBuilder SimDescEngine;
    protected final Similarity SimCalculator;

    protected SimilarMolecule[] SimilarMols;

    /**
     * Constructor
     */
    public InsilicoKnn(){
        SimDescEngine = new SimilarityDescriptorsBuilder();
        SimCalculator = new Similarity();

        // Defaults
        MinSimilarity = 0.75;
        MinSimilarityForSingleResult = 0.85;
        UseExperimentalRange = false;
        ExperimentalRange = 1;
        EnhanceWeightFactor = 1;
    }

    // GETTERS AND SETTERS
    /**
     * @return the NeighboursNumber
     */
    public int getNeighboursNumber() {
        return NeighboursNumber;
    }

    /**
     * @param NeighboursNumber the NeighboursNumber to set
     */
    public void setNeighboursNumber(int NeighboursNumber) {
        this.NeighboursNumber = NeighboursNumber;
    }

    /**
     * @return the MinSimilarity
     */
    public double getMinSimilarity() {
        return MinSimilarity;
    }

    /**
     * @param MinSimilarity the MinSimilarity to set
     */
    public void setMinSimilarity(double MinSimilarity) {
        this.MinSimilarity = MinSimilarity;
    }

    /**
     * @return the MinSimilarityForSingleResult
     */
    public double getMinSimilarityForSingleResult() {
        return MinSimilarityForSingleResult;
    }

    /**
     * @param MinSimilarityForSingleResult the MinSimilarityForSingleResult to set
     */
    public void setMinSimilarityForSingleResult(double MinSimilarityForSingleResult) {
        this.MinSimilarityForSingleResult = MinSimilarityForSingleResult;
    }

    /**
     * @return the UseExperimentalRange
     */
    public boolean isUseExperimentalRange() {
        return UseExperimentalRange;
    }

    /**
     * @param UseExperimentalRange the UseExperimentalRange to set
     */
    public void setUseExperimentalRange(boolean UseExperimentalRange) {
        this.UseExperimentalRange = UseExperimentalRange;
    }

    /**
     * @return the ExperimentalRange
     */
    public double getExperimentalRange() {
        return ExperimentalRange;
    }

    /**
     * @param ExperimentalRange the ExperimentalRange to set
     */
    public void setExperimentalRange(double ExperimentalRange) {
        this.ExperimentalRange = ExperimentalRange;
    }

    /**
     * @return the EnhanceWeightFactor
     */
    public int getEnhanceWeightFactor() {
        return EnhanceWeightFactor;
    }

    /**
     * @param EnhanceWeightFactor the EnhanceWeightFactor to set
     */
    public void setEnhanceWeightFactor(int EnhanceWeightFactor) {
        this.EnhanceWeightFactor = EnhanceWeightFactor;
    }

    /**
     * @return the SimilarMols
     */
    public SimilarMolecule[] getSimilarMols() {
        return SimilarMols;
    }

    /**
     * @param SimilarMols the SimilarMols to set
     */
    public void setSimilarMols(SimilarMolecule[] SimilarMols) {
        this.SimilarMols = SimilarMols;
    }


    abstract protected InsilicoKnnPrediction CalculatePrediction(ArrayList<SimilarMolecule> Neighbours, iTrainingSet TrainSet) throws GenericFailureException;

    public InsilicoKnnPrediction Calculate(InsilicoMolecule mol, iTrainingSet TrainSet) throws GenericFailureException {
        return Calculate(mol, TrainSet, false);
    }

    public InsilicoKnnPrediction Calculate(InsilicoMolecule mol, iTrainingSet TrainSet, boolean SkipExp) throws GenericFailureException {

        // Init similarity for mol against Training Set
        CalculateSimilarity(mol, TrainSet);

        // Get the k molecules
        ArrayList<SimilarMolecule> KNeighbours = new ArrayList<>();
        int molIndex = 0;
        while(molIndex < NeighboursNumber) {
            if (molIndex >= SimilarMols.length)
                break;
            if (SimilarMols[molIndex].getSimilarity() < MinSimilarity) {
                break;
            }

            // Added to perform calculation in LOO mode

            if(SkipExp)
                if (SimilarMols[molIndex].getSimilarity() == 1.0) {
                    molIndex++;
                    continue;
                }

            KNeighbours.add(SimilarMols[molIndex]);
            molIndex++;
        }

        // Possibile outcomes
        InsilicoKnnPrediction prediction = new InsilicoKnnPrediction();

        // 1 - No Molecules
        if(KNeighbours.isEmpty()){
            prediction.setStatus(InsilicoKnnPrediction.KNN_MISSING_NO_MOLECULES);
            return prediction;
        }

        // 2 - Experimental Found
        if (KNeighbours.get(0).getSimilarity() == 1){
            prediction.setPrediction(TrainSet.getExperimentalValue((int) KNeighbours.get(0).getIndex()));
            prediction.getNeighbours().add(KNeighbours.get(0));
            prediction.setStatus(InsilicoKnnPrediction.KNN_EXPERIMENTAL);
        }

        // 3 - Prediction one molecule
        if (KNeighbours.size() == 1){
            if(KNeighbours.get(0).getSimilarity() < MinSimilarityForSingleResult){
                prediction.setStatus(InsilicoKnnPrediction.KNN_MISSING_NO_MOLECULES);
                return prediction;
            } else {
                prediction.setPrediction(TrainSet.getExperimentalValue((int) KNeighbours.get(0).getIndex()));
                prediction.getNeighbours().add(KNeighbours.get(0));
                prediction.setStatus(InsilicoKnnPrediction.KNN_NORMAL_PREDICTION);
            }
        }

        return CalculatePrediction(KNeighbours, TrainSet);
    }


    private void CalculateSimilarity(InsilicoMolecule mol, iTrainingSet TrainSet) throws GenericFailureException {

        if ((TrainSet == null) || (TrainSet.getMoleculesSize() == 0)) {
            SimilarMols = null;
            logger.warn("Unable to retrieve training set for AD similarity calculation");
            throw new GenericFailureException("Unable to retrieve training set");
        }

        Similarity sim = new Similarity();
        SimilarMols = new SimilarMolecule[TrainSet.getMoleculesSize()];

        // Obtain similarity for all molecules
        for (int i = 0; i < TrainSet.getMoleculesSize(); i++){
            double curSim;
            try {
                curSim = sim.Calculate(mol.GetSimilarityDescriptors(), TrainSet.getSimilarityDescriptor(i));

                // If similarity equals one, check exact isomorphism
                if(curSim == 1.0){
                    IAtomContainer moleculeA = mol.GetStructure();
                    IAtomContainer moleculeB = SmilesMolecule.Convert(TrainSet.getSMILES(i)).GetStructure();
                    if(!Similarity.CheckIsomorphism(moleculeA, moleculeB)){
                        curSim = 0.999;
                    }
                }
                // Adjust low similarities to avoid problems in some following indices
                if (curSim < 0.38)
                    curSim = 0.38;

            } catch (Throwable ex){
                logger.warn("AD similarity calculation: unable to calculate for training set molecule " + i + ": " + TrainSet.getSMILES(i));
                curSim = 0;
            }

            SimilarMols[i] = new SimilarMolecule(i, curSim);
        }

        Arrays.sort(SimilarMols);
    }


}
