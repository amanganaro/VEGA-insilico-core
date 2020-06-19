package insilico.core.knn;

import insilico.core.exception.GenericFailureException;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.similarity.SimilarMolecule;

import java.util.ArrayList;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class InsilicoKnnQuantitative extends InsilicoKnn {

    public InsilicoKnnQuantitative(){
        super();
    }

    @Override
    protected InsilicoKnnPrediction CalculatePrediction(ArrayList<SimilarMolecule> Neighbours, iTrainingSet TrainSet) throws GenericFailureException {

        double pred = 0;
        double weight = 0;
        double min = TrainSet.getExperimentalValue((int)Neighbours.get(0).getIndex());
        double max = min;

        for (SimilarMolecule curMol : Neighbours) {

            // Calculate weight of the similar molecule
            double curWeight = Math.pow(curMol.getSimilarity(), EnhanceWeightFactor);

            // Update prediction and weight sums
            double curNeighExp = TrainSet.getExperimentalValue((int)curMol.getIndex());
            pred += curNeighExp * curWeight;
            weight += curWeight;

            // Update ranges (min and max
            min = Math.min(curNeighExp, min);
            max = Math.max(curNeighExp, max);
        }

        // Normalize prediction on weight sum
        pred /= weight;


        if (UseExperimentalRange)
            if ( Math.abs(max-min) > ExperimentalRange ) {
                InsilicoKnnPrediction Prediction = new InsilicoKnnPrediction();
                Prediction.setStatus(InsilicoKnnPrediction.KNN_MISSING_RANGE);
                return Prediction;
            }

        InsilicoKnnPrediction Prediction = new InsilicoKnnPrediction();
        Prediction.setPrediction(pred);
        Prediction.setNeighbours(Neighbours);
        Prediction.setStatus(InsilicoKnnPrediction.KNN_NORMAL_PREDICTION);
        return Prediction;
    }
}
