package insilico.core.knn;

import insilico.core.exception.GenericFailureException;
import insilico.core.model.trainingset.iTrainingSet;
import insilico.core.similarity.SimilarMolecule;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Alberto Manganaro (a.manganaro@kode-solutions.net)
 */
public class insilicoKnnQualitative extends insilicoKnn {

    public insilicoKnnQualitative() {
        super();
    }


    @Override
    protected insilicoKnnPrediction CalculatePrediction(ArrayList<SimilarMolecule> Neighbours,
                                                        iTrainingSet TrainSet) throws GenericFailureException {

        HashMap<Double, Double> ClassWeights = new HashMap<>();

        for (SimilarMolecule curMol : Neighbours) {

            // Calculate weight of the similar molecule
            double curWeight = Math.pow(curMol.getSimilarity(), EnhanceWeightFactor);

            // Update current class occurences
            double curClass = TrainSet.getExperimentalValue((int)curMol.getIndex());
            if (ClassWeights.containsKey(curClass)) {
                double val = ClassWeights.get(curClass) + curWeight;
                ClassWeights.put(curClass, val);
            } else
                ClassWeights.put(curClass, curWeight);
        }

        // Classification result
        double MaxClass = 0;
        boolean firstLoop = true;
        boolean eq = false;
        for (Double key : ClassWeights.keySet()) {
            if (firstLoop) {
                MaxClass = key;
                firstLoop = false;
                continue;
            }

            if (ClassWeights.get(key) > ClassWeights.get(MaxClass)) {
                MaxClass = key;
                eq = false;
            } else if (ClassWeights.get(key) == ClassWeights.get(MaxClass)) {
                eq = true;
            }
        }

        if (eq == true) {

            insilicoKnnPrediction Prediction = new insilicoKnnPrediction();
            Prediction.setStatus(insilicoKnnPrediction.KNN_MISSING_EQUAL_CLASSESS);
            return Prediction;

        } else {

            insilicoKnnPrediction Prediction = new insilicoKnnPrediction();
            Prediction.setPrediction(MaxClass);
            Prediction.setNeighbours(Neighbours);
            Prediction.setStatus(insilicoKnnPrediction.KNN_NORMAL_PREDICTION);
            return Prediction;

        }

    }
}
