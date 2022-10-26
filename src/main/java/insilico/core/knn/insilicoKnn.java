package insilico.core.knn;

import insilico.core.exception.GenericFailureException;
import insilico.core.localization.StringSelectorCore;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.molecule.InsilicoMolecule;
import insilico.core.molecule.conversion.SmilesMolecule;
import insilico.core.similarity.SimilarMolecule;
import insilico.core.similarity.Similarity;
import insilico.core.similarity.SimilarityDescriptorsBuilder;

import lombok.extern.slf4j.Slf4j;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Absract class
 * @author Alberto Manganaro <a.manganaro@kode-solutions.net>
 */
@Slf4j
public abstract class insilicoKnn {

    protected int NeighboursNumber;
    protected double MinSimilarity; // Use 0 to disable this feature
    protected double MinSimilarityForSingleResult; // Use 0 to disable this feature
    protected boolean UseExperimentalRange;
    protected double ExperimentalRange;
    protected int EnhanceWeightFactor;

    protected final SimilarityDescriptorsBuilder SimDescEngine;
    protected final Similarity SimCalculator;

    protected SimilarMolecule[] SimilarMols;


    /**
     * Constructor.
     */
    public insilicoKnn() {
        SimDescEngine = new SimilarityDescriptorsBuilder();
        SimCalculator = new Similarity();

        // Defaults
        MinSimilarity = 0.75;
        MinSimilarityForSingleResult = 0.85;
        UseExperimentalRange = false;
        ExperimentalRange = 1;
        EnhanceWeightFactor = 1;
    }


    private void CalculateSimilarity(InsilicoMolecule Mol, iTrainingSet TrainSet) throws GenericFailureException {

        if ((TrainSet == null)||(TrainSet.getMoleculesSize()==0)) {
            SimilarMols = null;
            log.warn(StringSelectorCore.getString("knn_ts_retrieve_fail"));
            throw new GenericFailureException(StringSelectorCore.getString("knn_ts_retrieve_fail"));
        }

        Similarity SIM = new Similarity();
        SimilarMols = new SimilarMolecule[TrainSet.getMoleculesSize()];

        // Calculates similarity for all molecules
        for (int idx=0; idx<TrainSet.getMoleculesSize(); idx++) {
            double curSim;
            try {
                curSim = SIM.Calculate(Mol.GetSimilarityDescriptors(),
                        TrainSet.getSimilarityDescriptor(idx));
                log.debug(String.format(StringSelectorCore.getString("knn_debug_similarity_calculated"), Mol.GetSMILES(), TrainSet.getSMILES(idx), curSim));

                // If similarity is equal to 1, checks if mols are really identical
                if (curSim == 1.0) {
                    IAtomContainer A = Mol.GetStructure();
                    IAtomContainer B = SmilesMolecule.Convert(TrainSet.getSMILES(idx)).GetStructure();
                    boolean AreIsomorph = Similarity.CheckIsomorphism(A, B);
                    if (!AreIsomorph)
                        curSim = 0.999;
                }

                // Adjust low similarities to avoid problems in some following indices
                if (curSim < 0.38)
                    curSim = 0.38;

            } catch (Throwable e) {
//                log.warn("AD similarity calculation: unable to calculate for training set molecule "
//                        + idx + ": " + TrainSet.getSMILES(idx));
                curSim = 0;
            }
            SimilarMols[idx] = new SimilarMolecule(idx, curSim);
        }

        // Sorts the array of similar molecules (index=0 means most similar molecule)
        Arrays.sort(SimilarMols);
    }


    public insilicoKnnPrediction Calculate(InsilicoMolecule mol, iTrainingSet TrainSet) throws GenericFailureException {
        return Calculate(mol, TrainSet, false);
    }


    public insilicoKnnPrediction Calculate(InsilicoMolecule mol, iTrainingSet TrainSet, boolean SkipExp) throws GenericFailureException {

        // Init similarity for mol against training set
        CalculateSimilarity(mol, TrainSet);

        // Get the k molecules
        ArrayList<SimilarMolecule> KNeighbours = new ArrayList<>();
        int molIdx = 0;
        while (KNeighbours.size() < NeighboursNumber) {
//        for (int i=0; i<NeighboursNumber; i++) {
            if (molIdx >= SimilarMols.length)
                break;
            if (SimilarMols[molIdx].getSimilarity() < MinSimilarity)
                break;

            // Added to perform calculation in LOO mode
            if (SkipExp)
                if (SimilarMols[molIdx].getSimilarity() == 1.0) {
                    molIdx++;
                    continue;
                }

            KNeighbours.add(SimilarMols[molIdx]);
            molIdx++;
        }

        //// Possible outcomes

        insilicoKnnPrediction Prediction = new insilicoKnnPrediction();

        // 1 - No molecules
        if (KNeighbours.isEmpty()) {
            Prediction.setStatus(insilicoKnnPrediction.KNN_MISSING_NO_MOLECULES);
            return Prediction;
        }


        // 2 - Experimental found
        if (KNeighbours.get(0).getSimilarity() == 1) {
            Prediction.setPrediction(TrainSet.getExperimentalValue((int)KNeighbours.get(0).getIndex()));
            Prediction.getNeighbours().add(KNeighbours.get(0));
            Prediction.setStatus(insilicoKnnPrediction.KNN_EXPERIMENTAL);
            return Prediction;
        }


        // 3 - Prediction on one molecule
        if (KNeighbours.size() == 1) {
            if (KNeighbours.get(0).getSimilarity() < MinSimilarityForSingleResult) {
                Prediction.setStatus(insilicoKnnPrediction.KNN_MISSING_NO_MOLECULES);
                return Prediction;
            } else {
                Prediction.setPrediction(TrainSet.getExperimentalValue((int)KNeighbours.get(0).getIndex()));
                Prediction.getNeighbours().add(KNeighbours.get(0));
                Prediction.setStatus(insilicoKnnPrediction.KNN_NORMAL_PREDICTION);
                return Prediction;
            }
        }


        // 4 - Prediction on more molecules
        return CalculatePrediction(KNeighbours, TrainSet);
    }


    abstract protected insilicoKnnPrediction CalculatePrediction(ArrayList<SimilarMolecule> Neighbours, iTrainingSet TrainSet) throws GenericFailureException;


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



}
