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

    public insilicoKnnQualitative(){
        super();
    }


    @Override
    protected insilicoKnnPrediction CalculatePrediction(ArrayList<SimilarMolecule> Neighbours, iTrainingSet TrainSet) throws GenericFailureException {

        HashMap<Double, Double> ClassWeights = new HashMap<>();

        for(SimilarMolecule curMol : Neighbours) {

            // Weight of similar molecule
            double curWeight = Math.pow(curMol.getSimilarity(), EnhanceWeightFactor);

            // update current class occurences
            double curClass = TrainSet.getExperimentalValue((int) curMol.getIndex());
            if(ClassWeights.containsKey(curClass)){
                double val = ClassWeights.get((curClass) + curWeight);
                ClassWeights.put(curClass, val);
            } else {
                ClassWeights.put(curClass, curWeight);
            }
        }

        // Classification result
        double MaxClass = 0;
        boolean firstLoop = true;
        boolean eq = false;

        for (Double key : ClassWeights.keySet()) {
            if(firstLoop){
                MaxClass = key;
                firstLoop = false;
                continue;
            }

            if(ClassWeights.get(key) > ClassWeights.get(MaxClass)){
                MaxClass = key;
                eq = false;
            } else if (ClassWeights.get(key).equals(ClassWeights.get(MaxClass)))
                eq = true;
        }

        if (eq) {
            insilicoKnnPrediction prediction = new insilicoKnnPrediction();
            prediction.setStatus(insilicoKnnPrediction.KNN_MISSING_EQUAL_CLASSESS);
            return prediction;
        } else {
            insilicoKnnPrediction prediction = new insilicoKnnPrediction();
            prediction.setPrediction(MaxClass);
            prediction.setNeighbours(Neighbours);
            prediction.setStatus(insilicoKnnPrediction.KNN_NORMAL_PREDICTION);
            return prediction;
        }

    }
}
